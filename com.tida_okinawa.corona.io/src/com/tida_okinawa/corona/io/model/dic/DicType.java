/**
 * @version $Id: DicType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

/**
 * ユーザー辞書タイプを定義した列挙型オブジェクト
 * 
 * @author shingo-takahashi
 * 
 */
public enum DicType {
    /** JUMAN辞書 */
    JUMAN("JUMAN辞書", 0, "jdic"),
    /** 固有辞書 */
    SPECIAL("固有辞書", 1, "dic"),
    /** 分野辞書 */
    CATEGORY("分野辞書", 2, "ddic"),
    /** 一般辞書 */
    COMMON("一般辞書", 3, "cdic"),
    /** ラベル辞書 */
    LABEL("ラベル辞書", 4, "ldic"),
    /** ゆらぎ辞書 */
    FLUC("ゆらぎ辞書", 5, "fdic"),
    /** 同義語辞書 */
    SYNONYM("同義語辞書", 6, "sdic"),
    /** パターン辞書 */
    PATTERN("パターン辞書", 7, "pdic");

    private String name;
    private int intValue;
    private String ext;


    /** このクラスは列挙型なのでユーザーがインスタンス化できない */
    private DicType(String name, final int anIntValue, String ext) {
        this.name = name;
        this.intValue = anIntValue;
        this.ext = ext;
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
     * 定義された拡張子を取得する
     * 
     * @return 拡張子を示す文字列
     */
    public String getExtension() {
        return ext;
    }


    /**
     * 整数値から列挙型定数を取得
     * 
     * @param anIntValue
     *            定義する整数値
     * @return 列挙型定数
     */
    public static DicType valueOf(final int anIntValue) {
        for (DicType d : values()) {
            if (d.getIntValue() == anIntValue) {
                return d;
            }
        }
        return null;
    }


    /**
     * 拡張子から列挙型定数を取得
     * 
     * @param ext
     *            定義する拡張子
     * @return 列挙型定数
     */
    public static DicType valueOfExt(final String ext) {
        for (DicType d : values()) {
            if (d.getExtension().equals(ext)) {
                return d;
            }
        }
        return null;
    }
}
