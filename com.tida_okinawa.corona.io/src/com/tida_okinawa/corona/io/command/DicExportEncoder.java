/**
 * @version $Id: DicExportEncoder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/11 9:18:27
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.command;

import java.beans.Encoder;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDepend;
import com.tida_okinawa.corona.io.model.dic.IDependSub;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ITerm;

/**
 * 指定された文字コードでファイル出力する、辞書用エクスポート用エンコーダー
 * 
 * @author shingo-takahashi
 * 
 */
class DicExportEncoder extends Encoder {

    private Map<Integer, List<ILabel>> labelMap;


    /**
     * 辞書エクスポート用エンコーダーの初期化
     * 
     * @param filePath
     *            出力先ファイルパス
     * @param labelMap
     *            ラベルマップ
     * @param encode
     *            出力文字エンコード
     * @throws IOException
     *             指定されたエンコーディングがサポートされていない。
     *             指定されたファイルが見つからない。
     */
    public DicExportEncoder(String filePath, Map<Integer, List<ILabel>> labelMap, String encode) throws IOException {
        super();
        this.labelMap = labelMap;
    }


    /**
     * 各出力コンバート
     * 
     * @param obj
     *            　
     * @return 用語体系に基づいたオブジェクトを返す
     */
    private Object convert(Object obj) {
        if (obj instanceof ITerm) {
            return convert((ITerm) obj);
        } else if (obj instanceof IDepend) {
            return convert((IDepend) obj);
        } else if (obj instanceof String) {
            return obj;
        }
        return obj;
    }


    /**
     * ユーザー辞書コンバート
     * 
     * @param item
     * @return
     *         用語に紐づくラベル情報を
     *         "ラベル名（Tree構造）"|"ラベル名（Tree構造）"
     *         で返す
     */
    private Object convert(ITerm item) {
        StringBuffer buf = new StringBuffer(200);
        /* 用語をセット */
        buf.append(getTermFormat(item));

        /* ラベルをセット */
        List<ILabel> list = this.labelMap.get(item.getId());
        Integer i = 0;
        if (list != null && list.size() > 0) {
            for (ILabel label : list) {
                i++;
                /* ラベル名(Tree構造) | ラベル名(Tree構造)で追加 */
                ICoronaDic usrDic = IoActivator.getService().getDictionary(item.getComprehensionDicId());
                String usrChkDic = usrDic.getName().substring(0, usrDic.getName().indexOf(".")); //$NON-NLS-1$*
                ICoronaDic lblDic = IoActivator.getService().getDictionary(label.getComprehensionDicId());
                String lblChkDic = lblDic.getName().substring(0, lblDic.getName().indexOf(".")); //$NON-NLS-1$*
                if (!lblChkDic.equals(usrChkDic)) {
                    buf.append(DicIEConstants.QUOTATION).append(lblDic) /* ラベル辞書 */
                    .append(DicIEConstants.COLON).append(label.getTreeName()).append(DicIEConstants.QUOTATION);
                } else {
                    buf.append(DicIEConstants.QUOTATION).append(label.getTreeName()).append(DicIEConstants.QUOTATION);
                }
                if (list.size() > i)
                    buf.append(DicIEConstants.PIPE);
            }
        } else {
            buf.append(DicIEConstants.QUOTATION).append(DicIEConstants.QUOTATION);
        }
        buf.append(DicIEConstants.CRLF);
        return buf;
    }


    /**
     * ゆらぎ/同義語共通コンバート
     * 
     * @param item
     * @return 代表語、従属語を設定しCSV形式で返す
     */
    private static Object convert(IDepend item) {

        // TODO 20131218 代表語に関連する情報を追加する。（品詞詳細、活用形）

        StringBuilder buf = new StringBuilder(200);

        ICoronaDic yrgDic = IoActivator.getService().getDictionary(item.getComprehensionDicId());

        /* 代表語をセット */
        //        buf.append(getDependFormat(item.getMain(), item)).append(DicIEConstants.SPLIT).append(DicIEConstants.SPLIT).append(DicIEConstants.SPLIT)
        //                .append(DicIEConstants.SPLIT).append(DicIEConstants.CRLF);
        buf.append(getDependFormat(item.getMain(), item)).append(DicIEConstants.SPLIT).append(DicIEConstants.SPLIT).append(DicIEConstants.SPLIT)
                .append(DicIEConstants.SPLIT).append(DicIEConstants.SPLIT).append(DicIEConstants.SPLIT).append(DicIEConstants.CRLF);

        /* 従属語をセット */
        for (Entry<Integer, IDependSub> entry : item.getSubs().entrySet()) {
            String yrgChkDic = yrgDic.getName().substring(0, yrgDic.getName().indexOf(".")); //$NON-NLS-1$*
            ICoronaDic usrDic = IoActivator.getService().getDictionary(item.getMain().getComprehensionDicId());
            String usrChkDic = usrDic.getName().substring(0, usrDic.getName().indexOf(".")); //$NON-NLS-1$*
            if (!usrChkDic.equals(yrgChkDic)) {
                buf.append(getDependFormat(entry.getValue().getTerm(), item)).append(DicIEConstants.SPLIT).append(usrDic) /* ユーザ辞書名 */
                .append(DicIEConstants.SPLIT).append(item.getMain().getValue()).append(DicIEConstants.SPLIT)
                        .append(item.getMain().getReading())
                        //                        .append(DicIEConstants.SPLIT).append(item.getMain().getTermPart().getName()).append(DicIEConstants.CRLF);
                        .append(DicIEConstants.SPLIT).append(item.getMain().getTermPart().getName()).append(DicIEConstants.SPLIT)
                        .append(item.getMain().getTermClass().getName()).append(DicIEConstants.SPLIT).append(item.getMain().getCform().getName())
                        .append(DicIEConstants.CRLF);
            } else {
                buf.append(getDependFormat(entry.getValue().getTerm(), item)).append(DicIEConstants.SPLIT).append(DicIEConstants.SPLIT)
                        .append(item.getMain().getValue()).append(DicIEConstants.SPLIT).append(item.getMain().getReading()).append(DicIEConstants.SPLIT)
                        //                        .append(item.getMain().getTermPart().getName()).append(DicIEConstants.CRLF);
                        .append(item.getMain().getTermPart().getName()).append(DicIEConstants.SPLIT).append(item.getMain().getTermClass().getName())
                        .append(DicIEConstants.SPLIT).append(item.getMain().getCform().getName()).append(DicIEConstants.CRLF);
            }
        }
        return buf;
    }


    /**
     * ゆらぎ・同義用語フォーマット取得
     * 
     * @param term
     * @param depend
     * @return ゆらぎ・同義語辞書の基本フォーマットをCSV形式で返す
     */
    private static Object getDependFormat(ITerm term, IDepend depend) {
        StringBuffer buf = new StringBuffer(200);
        /* 用語をセット */
        ICoronaDic yrgDic = IoActivator.getService().getDictionary(depend.getComprehensionDicId());
        String yrgChkDic = yrgDic.getName().substring(0, yrgDic.getName().indexOf(".")); //$NON-NLS-1$*
        ICoronaDic usrDic = IoActivator.getService().getDictionary(term.getComprehensionDicId());
        String usrChkDic = usrDic.getName().substring(0, usrDic.getName().indexOf(".")); //$NON-NLS-1$*
        if (!usrChkDic.equals(yrgChkDic)) {
            buf.append(usrDic) /* ユーザ辞書名 */
            .append(DicIEConstants.SPLIT).append(term.getValue()).append(DicIEConstants.SPLIT).append(term.getReading()).append(DicIEConstants.SPLIT);
        } else {
            buf.append(DicIEConstants.SPLIT).append(term.getValue()).append(DicIEConstants.SPLIT).append(term.getReading()).append(DicIEConstants.SPLIT);
        }

        buf.append(term.getTermPart().getName()).append(DicIEConstants.SPLIT).append(term.getTermClass().getName()).append(DicIEConstants.SPLIT);
        buf.append(term.getCform().getName());
        return buf;
    }


    /**
     * 用語フォーマット取得
     * 
     * @param item
     * @return 一般辞書の基本フォーマットをCSV形式で返す
     */
    private static Object getTermFormat(ITerm item) {
        StringBuffer buf = new StringBuffer(200);
        /* 用語をセット */
        buf.append(item.getValue()).append(DicIEConstants.SPLIT).append(item.getReading()).append(DicIEConstants.SPLIT);
        buf.append(item.getTermPart().getName()).append(DicIEConstants.SPLIT).append(item.getTermClass().getName()).append(DicIEConstants.SPLIT);
        buf.append(item.getCform().getName()).append(DicIEConstants.SPLIT);
        return buf;
    }


    /**
     * XLS出力用コンバートメイン処理
     * 
     * @param obj
     * @return　作成された結果をString形式で返す
     */
    public String createobject(Object obj) {
        StringBuffer buf = new StringBuffer(200);
        buf.append(convert(obj).toString());
        String str = buf.toString();
        return str;
    }
}