/**
 * @version $Id:
 *
 * 2012/08/01
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.data;

/**
 * ユーザー辞書タイプを定義した列挙型オブジェクト
 * 
 * @author shingo-takahashi
 * 
 */
public enum DocumentSplitType {
    // TODO 外部化

    // TODO ${kana} 出は行けない理由が、DocumentSpliter にあった。文字数を5と決め打ちにしてる
    // TODO カタカナといいつつ、ひらがなでも検出していた。
    /** 数値を表す特殊文字 */
    NUMBER("${num}", 1, "数値"),
    /** アルファベットを表す特殊文字 */
    ABC("${abc}", 2, "アルファベット"),
    /** カタカナを表す特殊文字 */
    KANA("${kan}", 3, "カタカナ"),
    /** キャプションを表す特殊文字 */
    CAPTION("${cap}", 4, "キャプション");

    private String value;
    private int intValue;
    private String disp;


    /**
     * このクラスは列挙型なのでユーザーがインスタンス化できない
     * 
     * @param value
     *            値
     * @param anIntValue
     *            数値
     * @param disp
     *            表示名
     */
    private DocumentSplitType(String value, final int anIntValue, String disp) {
        this.value = value;
        this.intValue = anIntValue;
        this.disp = disp;
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
    public String getValue() {
        return value;
    }


    /**
     * @return 表示名
     */
    public String getDisp() {
        return disp;
    }


    /**
     * 整数値から列挙型定数を取得
     * 
     * @param anIntValue
     *            定義する整数値
     * @return 列挙型定数
     */
    public static DocumentSplitType valueOf(final int anIntValue) {
        for (DocumentSplitType d : values()) {
            if (d.getIntValue() == anIntValue) {
                return d;
            }
        }
        return null;
    }


    /**
     * 定義中の特殊文字開始位置を返却する
     * 
     * @param definition
     *            　定義内容
     * @return 特殊文字開始位置：int
     */
    public static int getStartPoint(String definition) {
        if (definition.indexOf(NUMBER.getValue()) != -1) {
            return definition.indexOf(NUMBER.getValue());
        } else if (definition.indexOf(ABC.getValue()) != -1) {
            return definition.indexOf(ABC.getValue());
        } else if (definition.indexOf(KANA.getValue()) != -1) {
            return definition.indexOf(KANA.getValue());
        } else if (definition.indexOf(CAPTION.getValue()) != 1) {
            return definition.indexOf(CAPTION.getValue());
        }
        return -1;
    }
}
