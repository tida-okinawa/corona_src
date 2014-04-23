/**
 * @version $Id: TermClass.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03 17:49:15
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.ArrayList;
import java.util.List;

/**
 * 用語・品詞区分に付随する品詞詳細区分を定義した列挙型
 * 
 * @author shingo-takahashi
 */
public enum TermClass {
    /** NONE */
    NONE(-1, "", -1),
    /** 句点 */
    PERIOD(1, "句点", 1),
    /** 読点 */
    COMMA(2, "読点", 1),
    /** 括弧始 */
    START_BRACKETS(3, "括弧始", 1),
    /** 括弧終 */
    END_BRACKETS(4, "括弧終", 1),
    /** 記号 */
    SIGN(5, "記号", 1),

    /** 普通名詞 */
    COMMON_NOUN(11, "普通名詞", 6),
    /** サ変名詞 */
    SAHEN_NOUN(12, "サ変名詞", 6),
    /** 固有名詞 */
    PROPER_NOUN(13, "固有名詞", 6),
    /** 地名 */
    PLACE(14, "地名", 6),
    /** 人名 */
    PERSONS(15, "人名", 6),
    /** 組織名 */
    ORGANIZATION(16, "組織名", 6),
    /** 数詞 */
    NUMERAL(17, "数詞", 6),
    /** 形式名詞 */
    NOUN_FORM(18, "形式名詞", 6),
    /** 副詞的名詞 */
    CLS19(19, "副詞的名詞", 6),
    /** 時相名詞 */
    CLS20(20, "時相名詞", 6),
    /** 名詞形態指示詞 */
    CLS21(21, "名詞形態指示詞", 7),
    /** 連体詞形態指示詞 */
    CLS22(22, "連体詞形態指示詞", 7),
    /** 副詞形態指示詞 */
    CLS23(23, "副詞形態指示詞", 7),
    /** 様態副詞 */
    CLS24(24, "様態副詞", 8),
    /** 程度副詞 */
    CLS25(25, "程度副詞", 8),
    /** 量副詞 */
    CLS26(26, "量副詞", 8),
    /** 頻度副詞 */
    CLS27(27, "頻度副詞", 8),
    /** 時制相副詞 */
    CLS28(28, "時制相副詞", 8),
    /** 陳述副詞 */
    CLS29(29, "陳述副詞", 8),
    /** 評価副詞 */
    CLS30(30, "評価副詞", 8),
    /** 発言副詞 */
    CLS31(31, "発言副詞", 8),
    /** 格助詞 */
    CLS32(32, "格助詞", 9),
    /** 副助詞 */
    CLS33(33, "副助詞", 9),
    /** 接続助詞 */
    CLS34(34, "接続助詞", 9),
    /** 終助詞 */
    CLS35(35, "終助詞", 9),
    /** 名詞接頭辞 */
    CLS39(39, "名詞接頭辞", 13),
    /** 動詞接頭辞 */
    CLS40(40, "動詞接頭辞", 13),
    /** イ形容詞接頭辞 */
    CLS41(41, "イ形容詞接頭辞", 13),
    /** ナ形容詞接頭辞 */
    CLS42(42, "ナ形容詞接頭辞", 13),
    /** 名詞性述語接尾辞 */
    CLS43(43, "名詞性述語接尾辞", 14),
    /** 名詞性名詞接尾辞 */
    CLS44(44, "名詞性名詞接尾辞", 14),
    /** 名詞性名詞助数辞 */
    CLS45(45, "名詞性名詞助数辞", 14),
    /** 名詞性特殊接尾辞 */
    CLS46(46, "名詞性特殊接尾辞", 14),
    /** 形容詞性述語接尾辞 */
    CLS47(47, "形容詞性述語接尾辞", 14),
    /** 形容詞性名詞接尾辞 */
    CLS48(48, "形容詞性名詞接尾辞", 14),
    /** 動詞性接尾辞 */
    CLS49(49, "動詞性接尾辞", 14),
    /** その他 */
    OTHER(50, "その他", 15),
    /** カタカナ */
    KATAKANA(51, "カタカナ", 15),
    /** アルファベット */
    ALPHABET(52, "アルファベット", 15);

    private int intValue;
    private String name;
    private int termPart;


    /** このクラスは列挙型なのでユーザーがインスタンス化できない */
    private TermClass(final int anIntValue, String name, final int tPart) {
        this.name = name;
        this.intValue = anIntValue;
        this.termPart = tPart;
    }


    /**
     * 定義された整数値を取得する
     * 
     * @return 列挙型で定義された整数値
     */
    public int getIntValue() {
        return intValue;
    }


    /**
     * 定義された名前を取得する
     * 
     * @return 名前を示す文字列
     */
    public String getName() {
        return name;
    }


    /**
     * 整数値から列挙型定数を取得
     * 
     * @param anIntValue
     *            定義する整数値
     * @return 列挙型定数
     */
    public static TermClass valueOf(final int anIntValue) {
        for (TermClass d : values()) {
            if (d.getIntValue() == anIntValue) {
                return d;
            }
        }
        return NONE;
    }


    /**
     * 名前と品詞区分から列挙型定数を取得
     * 
     * @param name
     *            定義する名前
     * @param tPart
     *            品詞区分値
     * @return 列挙型定数
     */
    public static TermClass valueOf(String name, TermPart tPart) {
        if (tPart == null)
            return NONE;

        for (TermClass d : values()) {
            if (d.name.equals(name) && d.termPart == tPart.getIntValue()) {
                return d;
            }
        }
        return NONE;
    }


    /**
     * 名前から列挙型定数を取得
     * 
     * @param name
     *            定義する名前
     * @return 列挙型定数
     */
    public static TermClass valueOfName(String name) {
        for (TermClass d : values()) {
            if (d.name.equals(name)) {
                return d;
            }
        }
        return NONE;
    }


    /**
     * 指定した品詞区分に紐づく品詞詳細列挙型定数のListを取得する
     * 
     * @param tPart
     *            品詞区分値
     * @return 品詞詳細定数List
     */
    public static List<TermClass> values(final int tPart) {
        List<TermClass> result = new ArrayList<TermClass>();
        for (TermClass d : values()) {
            if (d.termPart == tPart) {
                result.add(d);
            }
        }
        return result;
    }
}
