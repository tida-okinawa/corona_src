/**
 * @version $Id: TermPart.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03 17:49:04
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;


/**
 * 用語に付随する品詞区分を定義した列挙型
 * 
 * @author shingo-takahashi
 */
public enum TermPart {
    // TODO NONEを0にする
    /** 特殊 */
    SPECIAL(1, "特殊"),
    /** 動詞 */
    VERB(2, "動詞"),
    /** 形容詞 */
    ADJECTIVE(3, "形容詞"),
    /** 判定詞 */
    DETERMINE(4, "判定詞"),
    /** 助動詞 */
    AUXILIARY_VERB(5, "助動詞"),
    /** 名詞 */
    NOUN(6, "名詞"),
    /** 指示詞 */
    DEMONSTRATIVE(7, "指示詞"),
    /** 副詞 */
    ADVERB(8, "副詞"),
    /** 助詞 */
    POSTPOSITIONAL_PARTICLE(9, "助詞"),
    /** 接続詞 */
    CONJUNCTION(10, "接続詞"),
    /** 連体詞 */
    RENTAI(11, "連体詞"),
    /** 感動詞 */
    INTERJECTION(12, "感動詞"),
    /** 接頭辞 */
    PREFIX(13, "接頭辞"),
    /** 接尾辞 */
    SUFFIX(14, "接尾辞"),
    /** 未定義語 */
    UNKNOWN(15, "未定義語"),
    /** NONE */
    NONE(-1, "");

    /**
     * 検索性を上げるためのフィールド
     */
    private static final TermPart[] ARRAY = { NONE, SPECIAL, VERB, ADJECTIVE, DETERMINE, AUXILIARY_VERB, NOUN, DEMONSTRATIVE, ADVERB, POSTPOSITIONAL_PARTICLE,
            CONJUNCTION, RENTAI, INTERJECTION, PREFIX, SUFFIX, UNKNOWN };

    private int intValue;
    private String name;


    /** このクラスは列挙型なのでユーザーがインスタンス化できない */
    private TermPart(final int anIntValue, String name) {
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
     * 整数値から列挙型定数を取得する.<br />
     * {@link #NONE}のintValueは-1だが、anIntValueに0を指定した時も、 {@link #NONE}が返る<br />
     * intValueと一致しない場合も{@link #NONE}を返す
     * 
     * @param anIntValue
     *            定義する整数値
     * @return 列挙型定数
     */
    public static TermPart valueOf(final int anIntValue) {
        if ((anIntValue > 0) && (anIntValue < ARRAY.length)) {
            return ARRAY[anIntValue];
        }
        return NONE;
    }


    /**
     * 名前から列挙型定数を取得する
     * 
     * @param name
     *            定義する名前
     * @return 列挙型定数
     */
    public static TermPart valueOfName(String name) {
        for (TermPart d : values()) {
            if (d.name.equals(name)) {
                return d;
            }
        }
        return NONE;
    }
}
