/**
 * @version $Id: ClaimWorkDataType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import com.tida_okinawa.corona.common.CleansingNameVariable;


/**
 * 問い合わせデータタイプ
 * 
 * @author shingo-takahashi
 * 
 */
public enum ClaimWorkDataType {

    /**
     * なし
     */
    NONE("なし", 0),

    /**
     * 原文
     */
    BASE("原文", 1),

    /**
     * 誤記補正
     */
    CORRECTION_MISTAKES(CleansingNameVariable.MISTAKE_CORRECT, 2),

    /**
     * 形態素
     */
    MORPHOLOGICAL("形態素", 3),

    /**
     * 係り受け
     */
    DEPENDENCY_STRUCTURE(CleansingNameVariable.MORPH_DEPEND, 4),

    /**
     * ゆらぎ補正
     */
    CORRECTION_FLUC("ゆらぎ補正", 5),

    /**
     * 同義語補正
     */
    CORRECTION_SYNONYM(CleansingNameVariable.FLUC_SYNONYM, 6),

    /**
     * パターン結果
     */
    RESLUT_PATTERN(CleansingNameVariable.PATTERN_PARSING, 7),

    /**
     * 最新データ
     */
    LASTED("最新データ", 8),

    /**
     * 最新データ
     */
    LASTED_EXEC("最新データ2", 10),

    /**
     * 頻出用語
     */
    FREQUENTLY_APPERING(CleansingNameVariable.FREQUENT, 9),

    /**
     * 連語抽出
     */
    COLLOCATION(CleansingNameVariable.COLLOCATION, 11);

    private int intValue;
    private String name;


    private ClaimWorkDataType(String name, final int anIntValue) {
        this.name = name;
        this.intValue = anIntValue;
    }


    /**
     * enum定数から整数へ変換
     * 
     * @return 問い合わせデータタイプIDを返す
     */
    public int getIntValue() {
        return intValue;
    }


    /**
     * 整数からenum定数へ変換
     * 
     * @param anIntValue
     *            問い合わせデータタイプID
     * @return 問い合わせデータタイプの定数を返す
     */
    public static ClaimWorkDataType valueOf(final int anIntValue) {
        for (ClaimWorkDataType d : values()) {
            if (d.getIntValue() == anIntValue) {
                return d;
            }
        }
        return null;
    }


    /**
     * 文字列からenum定数へ変換
     * 
     * @param anStringValue
     *            問い合わせデータタイプ名
     * @return 問い合わせデータタイプの定数を返す
     */
    public static ClaimWorkDataType valueOfName(final String anStringValue) {
        for (ClaimWorkDataType d : values()) {
            if (d.getName().equals(anStringValue)) {
                return d;
            }
        }
        return null;
    }


    /**
     * 名称取得
     * 
     * @return 問い合わせデータタイプ名を返す
     */
    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return name;
    }
}
