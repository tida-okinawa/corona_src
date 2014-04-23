/**
 * @version $Id: TermCForm.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03 17:49:04
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.ArrayList;
import java.util.List;

/**
 * 用語に付随する活用形を定義した列挙型
 * 
 * @author shingo-takahashi
 */
public enum TermCForm {
    /** NONE */
    NONE(-1, "", -1, -1),
    /** 母音動詞 */
    CFORM01(1, "母音動詞", 2, -1),
    /** 子音動詞カ行 */
    CFORM02(2, "子音動詞カ行", 2, -1),
    /** 子音動詞カ行促音便形 */
    CFORM03(3, "子音動詞カ行促音便形", 2, -1),
    /** 子音動詞ガ行 */
    CFORM04(4, "子音動詞ガ行", 2, -1),
    /** 子音動詞サ行 */
    CFORM05(5, "子音動詞サ行", 2, -1),
    /** 子音動詞タ行 */
    CFORM06(6, "子音動詞タ行", 2, -1),
    /** 子音動詞ナ行 */
    CFORM07(7, "子音動詞ナ行", 2, -1),
    /** 子音動詞バ行 */
    CFORM08(8, "子音動詞バ行", 2, -1),
    /** 子音動詞マ行 */
    CFORM09(9, "子音動詞マ行", 2, -1),
    /** 子音動詞ラ行 */
    CFORM10(10, "子音動詞ラ行", 2, -1),
    /** 子音動詞ラ行イ形 */
    CFORM11(11, "子音動詞ラ行イ形", 2, -1),
    /** 子音動詞ワ行 */
    CFORM12(12, "子音動詞ワ行", 2, -1),
    /** 子音動詞ワ行文語音便形 */
    CFORM13(13, "子音動詞ワ行文語音便形", 2, -1),
    /** カ変動詞 */
    CFORM14(14, "カ変動詞", 2, -1),
    /** カ変動詞来 */
    CFORM15(15, "カ変動詞来", 2, -1),
    /** サ変動詞 */
    CFORM16(16, "サ変動詞", 2, -1),
    /** ザ変動詞 */
    CFORM17(17, "ザ変動詞", 2, -1),
    /** 動詞性接尾辞ます型 */
    CFORM18(18, "動詞性接尾辞ます型", 2, -1),
    /** イ形容詞アウオ段 */
    CFORM19(19, "イ形容詞アウオ段", 3, -1),
    /** イ形容詞イ段 */
    CFORM20(20, "イ形容詞イ段", 3, -1),
    /** イ形容詞イ段特殊 */
    CFORM21(21, "イ形容詞イ段特殊", 3, -1),
    /** ナ形容詞 */
    CFORM22(22, "ナ形容詞", 3, -1),
    /** ナ形容詞特殊 */
    CFORM23(23, "ナ形容詞特殊", 3, -1),
    /** ナノ形容詞 */
    CFORM24(24, "ナノ形容詞", 3, -1),
    /** タル形容詞 */
    CFORM25(25, "タル形容詞", 3, -1),
    /** 判定詞 */
    CFORM26(26, "判定詞", 4, -1),
    /** イ形容詞イ段 */
    CFORM27(27, "イ形容詞イ段", 5, -1),
    /** ナ形容詞 */
    CFORM28(28, "ナ形容詞", 5, -1),
    /** ナ形容詞特殊 */
    CFORM29(29, "ナ形容詞特殊", 5, -1),
    /** ナノ形容詞 */
    CFORM30(30, "ナノ形容詞", 5, -1),
    /** 判定詞 */
    CFORM31(31, "判定詞", 5, -1),
    /** 無活用型 */
    CFORM32(32, "無活用型", 5, -1),
    /** 助動詞ぬ型 */
    CFORM33(33, "助動詞ぬ型", 5, -1),
    /** 助動詞だろう型 */
    CFORM34(34, "助動詞だろう型", 5, -1),
    /** 助動詞そうだ型 */
    CFORM35(35, "助動詞そうだ型", 5, -1),
    /** 助動詞く型 */
    CFORM36(36, "助動詞く型", 5, -1),
    /** ナ形容詞 */
    CFORM37(37, "ナ形容詞", 14, 47),
    /** ナノ形容詞 */
    CFORM38(38, "ナノ形容詞", 14, 47),
    /** イ形容詞アウオ段 */
    CFORM39(39, "イ形容詞アウオ段", 14, 47),
    /** ナ形容詞 */
    CFORM40(40, "ナ形容詞", 14, 48),
    /** ナノ形容詞 */
    CFORM41(41, "ナノ形容詞", 14, 48),
    /** イ形容詞アウオ段 */
    CFORM42(42, "イ形容詞アウオ段", 14, 48),
    /** 母音動詞 */
    CFORM43(43, "母音動詞", 14, 49),
    /** 子音動詞カ行 */
    CFORM44(44, "子音動詞カ行", 14, 49),
    /** 子音動詞カ行促音便形 */
    CFORM45(45, "子音動詞カ行促音便形", 14, 49),
    /** 子音動詞サ行 */
    CFORM46(46, "子音動詞サ行", 14, 49),
    /** 子音動詞マ行 */
    CFORM47(47, "子音動詞マ行", 14, 49),
    /** 子音動詞ラ行 */
    CFORM48(48, "子音動詞ラ行", 14, 49),
    /** 子音動詞ラ行イ形 */
    CFORM49(49, "子音動詞ラ行イ形", 14, 49),
    /** 子音動詞ワ行 */
    CFORM50(50, "子音動詞ワ行", 14, 49),
    /** カ変動詞 */
    CFORM51(51, "カ変動詞", 14, 49),
    /** カ変動詞来 */
    CFORM52(52, "カ変動詞来", 14, 49),
    /** サ変動詞 */
    CFORM53(53, "サ変動詞", 14, 49),
    /** 動詞性接尾辞ます型 */
    CFORM54(54, "動詞性接尾辞ます型", 14, 49),
    /** 動詞性接尾辞うる型 */
    CFORM55(55, "動詞性接尾辞うる型", 14, 49),
    /** 動詞性接尾辞得る型 */
    CFORM56(56, "動詞性接尾辞得る型", 14, 49);

    private int intValue;
    private String name;
    private int termPart;
    private int termClass;


    /** このクラスは列挙型なのでユーザーがインスタンス化できない */
    private TermCForm(final int anIntValue, String name, final int tPart, final int tClass) {
        this.name = name;
        this.intValue = anIntValue;
        this.termPart = tPart;
        this.termClass = tClass;
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
    public static TermCForm valueOf(final int anIntValue) {
        for (TermCForm d : values()) {
            if (d.getIntValue() == anIntValue) {
                return d;
            }
        }
        return NONE;
    }


    /**
     * 名前と品詞区分と品詞詳細区分から列挙型定数を取得
     * 
     * @param name
     *            定義する名前
     * @param tPart
     *            品詞区分値
     * @param tClass
     *            品詞詳細区分値
     * @return 列挙型定数
     */
    public static TermCForm valueOf(String name, TermPart tPart, TermClass tClass) {
        int clsValue = -1;
        if (tPart == null)
            return NONE;
        if (tClass != null)
            clsValue = tClass.getIntValue();

        for (TermCForm d : values()) {
            if (d.termPart == tPart.getIntValue() && d.termClass == clsValue && d.name.equals(name)) {
                return d;
            }
        }
        return NONE;
    }


    /**
     * 指定した品詞区分・品詞詳細区分に紐づく活用形列挙型定数Listを取得する
     * 
     * @param tPart
     *            品詞区分値
     * @param tClass
     *            　品詞詳細区分値。無い場合は-1を指定
     * @return 活用形列挙型定数のList
     */
    public static List<TermCForm> values(final int tPart, final int tClass) {
        List<TermCForm> result = new ArrayList<TermCForm>();
        for (TermCForm d : values()) {
            if (d.termPart == tPart && d.termClass == tClass) {
                result.add(d);
            }
        }
        return result;
    }


    /**
     * 名前から列挙型定数を取得
     * 
     * @param name
     *            定義する名前
     * @return 列挙型定数
     */
    public static TermCForm valueOfName(String name) {
        for (TermCForm d : values()) {
            if (d.name.equals(name)) {
                return d;
            }
        }
        return NONE;
    }
}
