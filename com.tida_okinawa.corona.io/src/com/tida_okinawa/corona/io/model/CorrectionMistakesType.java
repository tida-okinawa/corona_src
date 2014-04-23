/**
 * @version $Id: CorrectionMistakesType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/16 13:48:35
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

/**
 * 誤記補正タイプ
 * 
 * @author shingo-takahashi
 */
public enum CorrectionMistakesType {

    /**
     * なし
     */
    NONE("なし", 0),

    /**
     * 外部補正
     */
    EXTERNAL_CORRECTION("外部補正", 1),

    /**
     * 内部補正
     */
    INTERNAL_CORRECTION("内部補正", 2);

    private int intValue;
    private String name;


    private CorrectionMistakesType(String name, final int anIntValue) {
        this.name = name;
        this.intValue = anIntValue;
    }


    /**
     * enum定数から整数へ変換
     * 
     * @return 誤記補正タイプIDを返す
     */
    public int getIntValue() {
        return intValue;
    }


    /**
     * 整数からenum定数へ変換
     * 
     * @param anIntValue
     * @return 誤記補正タイプの定数を返す
     */
    public static CorrectionMistakesType valueOf(final int anIntValue) {
        for (CorrectionMistakesType d : values()) {
            if (d.getIntValue() == anIntValue) {
                return d;
            }
        }
        return null;
    }


    /**
     * 名称取得
     * 
     * @return 誤記補正タイプ名を返す
     */
    public String getName() {
        return name;
    }
}
