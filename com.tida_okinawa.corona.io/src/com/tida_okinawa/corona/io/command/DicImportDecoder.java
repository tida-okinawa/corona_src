/**
 * @version $$Id: DicImportDecoder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $$
 * 
 * 2011/11/11 9:18:27
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.dic.IDepend;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * 辞書インポート用デコーダー
 * 
 * @author shingo-takahashi
 * 
 */
class DicImportDecoder {

    private String encode;
    private int index = 0;
    private List<Object> output = new ArrayList<Object>();
    private List<String> errs = new ArrayList<String>();


    DicImportDecoder(InputStream in, Class<?> cls, String encode) throws IOException {
        errs = new ArrayList<String>();
        this.encode = encode;

        /* デコード */
        decode(in, cls, errs);

        /* ログ出力 */
        if (errs.size() > 0) {
            CoronaIoUtils.setErrorLogs(IStatus.WARNING, Messages.DicImportDecoder_errLogDecoder, errs, null);
        }
    }


    private void decode(InputStream in, Class<?> cls, List<String> errs) throws IOException {
        if (IPattern.class.isAssignableFrom(cls)) {
            decodeXml(in, errs);
        } else {
            decodeCsv(in, cls);
        }
    }


    private void decodeXml(InputStream in, List<String> errs) {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in, Encoding.MS932.toString()));
        } catch (UnsupportedEncodingException e1) {
            errs.add(Messages.DicImportDecoder_errLogCharcter + e1.toString());
            e1.printStackTrace();
            return;
        }
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while (null != (line = reader.readLine())) {
                sb.append(line);
            }
        } catch (IOException e1) {
            errs.add(Messages.DicImportDecoder_errLogIO + e1.toString());
            e1.printStackTrace();
        }

        /* パターンノードを読み込み */
        Document doc = null;
        try {
            /* ドキュメントビルダーファクトリを生成 */
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            dbfactory.setIgnoringComments(true);
            dbfactory.setIgnoringElementContentWhitespace(true);
            /* ドキュメントビルダーを生成 */
            DocumentBuilder builder = dbfactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(sb.toString()));
            doc = builder.parse(is);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            errs.add(Messages.DicImportDecoder_errLogParse + e.toString());
        } catch (SAXException e) {
            e.printStackTrace();
            errs.add(Messages.DicImportDecoder_errLogSAX + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            errs.add(Messages.DicImportDecoder_errLogIO + e.toString());
        }
        if (doc == null) {
            CoronaIoUtils.setErrorLogs(IStatus.WARNING, Messages.DicImportDecoder_errLogDecoder, errs, null);
            return;
        }

        Element eleDic = doc.getDocumentElement();

        /* パターン分類検索の高速化用マップ */
        Map<String, Integer> typeSearchMap = new HashMap<String, Integer>();
        for (PatternType type : PatternType.getPatternTypes()) {
            typeSearchMap.put(type.getPatternName(), type.getId());
        }

        NodeList patternList = eleDic.getChildNodes();
        for (int i = 0; i < patternList.getLength(); i++) {
            /* XML要素のみを対象として取り扱う */
            Node node = patternList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element ptn = (Element) patternList.item(i);
                String dicName = ptn.getAttribute("dicname"); //$NON-NLS-1$
                String label = ptn.getAttribute("name"); //$NON-NLS-1$
                String parts = ptn.getAttribute("parts"); //$NON-NLS-1$

                /* パターン分類名をパターン分類IDへ変換する */
                Integer patternType = typeSearchMap.get(ptn.getAttribute("type")); //$NON-NLS-1$
                if (patternType == null) {/* 分類パターンがなければ追加 */
                    PatternType pType = IoService.getInstance().addPatternType(ptn.getAttribute("type")); //$NON-NLS-1$
                    patternType = pType.getId();

                    /* 新規追加分を検査用バッファに登録(多重登録防止) */
                    typeSearchMap.put(pType.getPatternName(), pType.getId());
                }

                String text = ""; //$NON-NLS-1$
                if (ptn.getFirstChild() != null) {
                    /* パターン部分をStringに戻す */
                    StringWriter writer = new StringWriter();
                    try {
                        Transformer transformer = null;
                        transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.transform(new DOMSource(ptn.getFirstChild()), new StreamResult(writer));
                    } catch (TransformerException e) {
                        errs.add(Messages.bind(Messages.DicImportDecoder_errLogPattern, new String[] { label }));
                        continue;
                    }
                    text = writer.toString();
                }

                String[] item = { dicName, label, text, Integer.toString(patternType), parts };
                output.add(item);
            }
        }

    }


    private void decodeCsv(InputStream in, Class<?> cls) throws IOException {
        this.index = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, encode));
        try {
            String readStr;
            int split_len = -1;

            if (IDepend.class.isAssignableFrom(cls)) {
                /* 行サイズの調整 */
                split_len = DicIEConstants.DEPEND_DIC_COLUMNS;
            }
            /* ストリームの行読み込み */
            while ((readStr = reader.readLine()) != null) {
                /*
                 * ヘッダ行を読み飛ばす
                 * memo 行が”ID"で始まるものをヘッダ行として認識している為
                 * DB構造が変わった場合は都度対応が必要
                 */
                if (readStr.matches(".*" + Messages.DicExport_csvHeader + ".*")) { //$NON-NLS-1$ //$NON-NLS-2$
                    continue;
                }

                if (readStr.length() != 0) {
                    String[] strs = readStr.split(DicIEConstants.SPLIT, split_len);
                    output.add(strs);
                }
            }
        } finally {
            /* ファイルクローズ */
            reader.close();
        }
    }


    public Object readObject() throws ArrayIndexOutOfBoundsException {
        return output.get(index++);
    }


    public List<Object> readArray() {
        return output;
    }
}