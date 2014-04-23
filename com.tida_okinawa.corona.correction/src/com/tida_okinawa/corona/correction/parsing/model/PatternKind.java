/**
 * @version $Id: PatternKind.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/24 10:21:29
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

/**
 * パターンの種別を表す。パターン辞書作成時、作成できる子を制限するために使用する。
 * 
 * @author kousuke-morishima
 */
public enum PatternKind {
    /** 未定義状態 */
    NONE("未定義"),
    /** PatternRecordを表す */
    ROOT("パターン"),
    /** 単語 */
    TERM("単語"),
    /** And */
    AND("And"),
    /** Or */
    OR("Or"),
    /** Not */
    NOT("Not"),
    /** 連続 */
    SEQUENCE("連続"),
    /** 順序 */
    ORDER("順序"),
    /** 係り受け */
    MODIFICATION("係り受け"),
    /** 係り元 */
    MODIFICATION_SOURCE("係り元"),
    /** 係り先 */
    MODIFICATION_DESTINATION("係り先"),
    /** 参照 */
    LINK("参照");

    private String value;


    private PatternKind(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return value;
    }
}