/**
 * @version $Id: TableType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/23 14:08:31
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.table;

/**
 * @author shingo-takahashi
 */
public enum TableType {
    /**
     * 問い合わせデータのDBテーブル名
     */
    CLAIM_DATA("USR_CLAIM_", 1),

    /**
     * 誤記補正データのDBテーブル名
     */
    CORRECTION_MISTAKES_DATA("USR_CM_", 2),

    /**
     * 形態素解析データのDBテーブル名
     */
    WORK_DATA("USR_WORK_", 3),

    /**
     * リレーションデータのDBテーブル名
     */
    RESULT_DATA("USR_RELPTN_", 4);


    private int intValue;
    private String name;


    private TableType(String name, final int anIntValue) {
        this.name = name;
        intValue = anIntValue;
    }


    /**
     * enum定数から整数へ変換
     * 
     * @return intValue
     */
    public int getIntValue() {
        return intValue;
    }


    /**
     * 整数からenum定数へ変換
     * 
     * @param anIntValue
     * @return enum定数
     */
    public static TableType valueOf(final int anIntValue) {
        for (TableType d : values()) {
            if (d.getIntValue() == anIntValue) {
                return d;
            }
        }
        return null;
    }


    /**
     * 名称取得
     * 
     * @return テーブル名
     */
    public String getName() {
        return name;
    }

}
