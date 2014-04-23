/**
 * @version $Id: UserDicFieldType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/25 13:12:54
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

/**
 * ユーザー辞書のフィールド項目を定義する列挙型
 * 
 * @author shingo-takahashi
 */
public enum UserDicFieldType {

    /** NONE */
    NONE("", 0),

    /** 見出し語 */
    HEADER("NAME", 1),

    /** 読み */
    READING("READING", 2),

    /** 品詞 */
    PART("PART_ID", 3),

    /** 品詞詳細 */
    CLASS("CLASS_ID", 4),

    /** 活用形 */
    CFORM("CFORM_ID", 5),

    /** ラベル */
    LABEL("LABEL", 6);

    private int intValue;
    private String name;


    /** このクラスは列挙型なのでユーザーがインスタンス化できない */
    private UserDicFieldType(String name, final int anIntValue) {
        this.name = name;
        this.intValue = anIntValue;
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
    public static UserDicFieldType valueOf(final int anIntValue) {
        for (UserDicFieldType d : values()) {
            if (d.getIntValue() == anIntValue) {
                return d;
            }
        }
        return null;
    }
}
