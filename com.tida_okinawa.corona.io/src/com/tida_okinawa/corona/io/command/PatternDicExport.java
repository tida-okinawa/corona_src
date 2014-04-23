/**
 * @version $Id: PatternDicExport.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/17 17:32:31
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.command;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;

/**
 * 構文パターン辞書エクスポート
 * 
 * @author yukihiro-kinjo
 * 
 */
public class PatternDicExport {

    private String encode;


    /**
     * コンストラクター
     * 
     * @param encode
     *            出力文字エンコード
     */
    public PatternDicExport(String encode) {
        super();
        this.encode = encode;
    }


    /**
     * パターン辞書エクスポート
     * 
     * @param filePath
     *            辞書エクスポートファイルパス
     * @param dic
     *            エクスポートするパターン辞書
     * @param monitor
     *            進捗表示用モニター
     * @return 結果ステータス
     * @throws IOException
     *             パターンノードの読み込みがIO関連の処理により失敗
     * @throws ParserConfigurationException
     *             ドキュメントビルダーの生成に失敗
     * @throws SAXException
     *             パターンノードの解析に失敗
     */
    public IStatus export(String filePath, IPatternDic dic, IProgressMonitor monitor) throws IOException, ParserConfigurationException, SAXException {

        /* ドキュメントビルダーファクトリを生成 */
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        /* ドキュメントビルダーを生成 */
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        /* 出力用ドキュメントを作成 */
        Document doc = builder.newDocument();

        IStatus resultStatus = Status.OK_STATUS;

        /* XML文章を結合 */
        StringBuilder xml = new StringBuilder();
        Boolean loopFlg = false;
        HashMap<Integer, IPattern> linkMap = new HashMap<Integer, IPattern>();

        /* エクスポート用のHashMapを作成 */
        do { /* while後判定 */
            loopFlg = false;
            if (linkMap.isEmpty()) {
                /* 初期処理 */
                for (IDicItem item : dic.getItems()) {
                    linkMap.put(item.getId(), ((IPattern) item));
                    xml.append(((IPattern) item).getText());
                }
            }

            /* 正規表現でLINKIDを抽出してHashMapへ展開 */
            Pattern linkReg = Pattern.compile("<LINK ID=\""); //$NON-NLS-1$
            Matcher match = linkReg.matcher(xml);
            while (match.find()) {
                int idLength = xml.substring(match.end()).indexOf("\""); //$NON-NLS-1$
                String linkid = xml.substring(match.end(), match.end() + idLength);
                /* 既にKeyがある場合はスルーするー（笑） */
                if (!linkMap.containsKey(Integer.parseInt(linkid))) {
                    linkMap.put(Integer.parseInt(linkid), null);
                }
            }

            /* HashMapへ実データを格納 */
            for (Map.Entry<Integer, IPattern> link : linkMap.entrySet()) {
                /* nullのvalueへ値を格納 */
                if (link.getValue() == null) {
                    if (dic.getItem(link.getKey()) != null) {
                        /* 自辞書内にアイテムが存在する場合 */
                        link.setValue((IPattern) dic.getItem(link.getKey()));
                    } else {
                        /* 自辞書内にアイテムが存在しなかった場合 */
                        IDicItem newItem = IoActivator.getDicUtil().getItem(link.getKey(), DicType.PATTERN);
                        link.setValue((IPattern) newItem);
                        try {
                            xml.append(link.getValue().getText());
                        } catch (Exception e) {
                            return new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, Messages.PatternDicExport_errNonLink, e);
                        }
                        loopFlg = true;
                    }
                }
            }
        } while (loopFlg);

        /* HashMapを元にElementを作成 */
        Element eleDic = doc.createElement(DicIEConstants.PATTERNDIC);
        doc.appendChild(eleDic);

        for (Map.Entry<Integer, IPattern> link : linkMap.entrySet()) {

            /* 辞書名を取得 */
            IDicItem newItem = IoActivator.getDicUtil().getItem(link.getValue().getId(), DicType.PATTERN);
            int dicid = newItem.getComprehensionDicId();
            String dicname = IoService.getInstance().getDictionary(dicid).getName();
            String chkdic = dicname.substring(0, dicname.indexOf(".")); //$NON-NLS-1$
            String ptnsetdic = dic.toString();
            String ptnchkdic = ptnsetdic.substring(0, ptnsetdic.indexOf(".")); //$NON-NLS-1$
            if (chkdic.equals(ptnchkdic)) {
                dicname = ""; //$NON-NLS-1$
            }

            /* パターン情報の出力 */
            Element elePtn = doc.createElement(DicIEConstants.PATTERN);
            elePtn.setAttribute(DicIEConstants.DICNAME, String.valueOf(dicname));
            elePtn.setAttribute(DicIEConstants.NAME, link.getValue().getLabel());
            elePtn.setAttribute(DicIEConstants.PARTS, String.valueOf(link.getValue().isParts()));

            /* 分類パターンIDを元に分類パターン名を設定 */
            PatternType patternType = PatternType.getPatternType(link.getValue().getPatternType());
            elePtn.setAttribute("type", patternType.getPatternName()); //$NON-NLS-1$

            eleDic.appendChild(elePtn);

            /* テキストを『>』文字でSPLITする。 */
            String chgText = link.getValue().getText();
            String[] splitstrAry = chgText.split(DicIEConstants.SPLITEND);

            /* SPLIT内容をLOOP */
            for (int j = 0; j < splitstrAry.length; j++) {
                if (splitstrAry[j] != null && splitstrAry[j].length() > 0) {
                    /* 文字列の先頭４文字を取得 */
                    String chkText = splitstrAry[j].substring(0, 4);

                    /* 先頭文字列が『<LIN』の場合、編集処理を実施 */
                    if ((DicIEConstants.XMLCHK).equals(chkText)) {

                        /* ID番号を取得 */
                        int Arylength = splitstrAry[j].length();
                        int strcolm = 10;
                        int endcolm = Arylength - 2;
                        int sertchnum = Integer.parseInt(splitstrAry[j].substring(strcolm, endcolm));

                        /* ID番号を基にlinkmapを取得 */
                        if (linkMap.containsKey(sertchnum)) {
                            IPattern chklink = linkMap.get(sertchnum);

                            /* linkmapを基に新LINK情報に必要な情報を取得 */
                            IDicItem cnvItem = IoActivator.getDicUtil().getItem(sertchnum, DicType.PATTERN);
                            int cnvdicid = cnvItem.getComprehensionDicId();
                            String cnvdicname = IoService.getInstance().getDictionary(cnvdicid).getName();
                            String cnvchkdic = cnvdicname.substring(0, cnvdicname.indexOf(".")); //$NON-NLS-1$
                            if (cnvchkdic.equals(ptnchkdic)) {
                                cnvdicname = ""; //$NON-NLS-1$
                            }
                            String cnvname = chklink.getLabel();
                            boolean cnvparts = chklink.isParts();
                            PatternType cnvptype = PatternType.getPatternType(chklink.getPatternType());

                            /* 取得した各種情報を基に新LINK用レイアウトを作成 */
                            StringBuilder chgbuf = new StringBuilder(200);
                            chgbuf.append(DicIEConstants.LINK).append(DicIEConstants.SDICNAME).append(DicIEConstants.QUOTATION)
                                    .append(String.valueOf(cnvdicname)).append(DicIEConstants.QUOTATION).append(DicIEConstants.SNAME)
                                    .append(DicIEConstants.QUOTATION).append(cnvname).append(DicIEConstants.QUOTATION).append(DicIEConstants.SPARTS)
                                    .append(DicIEConstants.QUOTATION).append(String.valueOf(cnvparts)).append(DicIEConstants.QUOTATION)
                                    .append(DicIEConstants.STYPE).append(DicIEConstants.QUOTATION).append(cnvptype.getPatternName())
                                    .append(DicIEConstants.QUOTATION).append(DicIEConstants.LINKEND);

                            /* 作成した新LINK用レイアウトに置き換える */
                            splitstrAry[j] = chgbuf.toString();
                        }

                    }
                }
            }

            /* 上記SPLITした内容を元に戻す */
            StringBuilder setbuf = new StringBuilder(200);
            for (int i = 0; i < splitstrAry.length; i++) {
                setbuf.append(splitstrAry[i]).append(DicIEConstants.SPLITEND);

            }

            /* パターンノードを読み込み */
            try {
                Document docPattern = builder.parse(new ByteArrayInputStream(setbuf.toString().getBytes("utf-8"))); //$NON-NLS-1$
                /* 子ノードに割り当て？ */
                elePtn.appendChild(doc.importNode(docPattern.getDocumentElement(), true));
            } catch (SAXException e) {
                /* パターンの中身がない場合にここにくる */
                CoronaActivator.debugLog("Error : '" + link.getValue().getLabel() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        monitor.subTask(Messages.PatternDicExport_monitorFileOutput);
        monitor.worked(2);

        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tfactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, this.encode); // エンコード指定
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            File outfile = new File(filePath);
            transformer.transform(new DOMSource(doc), new StreamResult(outfile));

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            resultStatus = new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, Messages.PatternDicExport_errLogCsvOutputFail, e);
        } catch (TransformerException e) {
            e.printStackTrace();
            resultStatus = new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, Messages.PatternDicExport_errLogCsvOutputFail, e);
        }
        monitor.worked(1);
        monitor.done();
        return resultStatus;
    }

}
