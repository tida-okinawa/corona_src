/**
 * @version $Id: PatternParseElementType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/05 16:54:24
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing;

/**
 * @author shingo-takahashi
 */
public enum PatternParseElementType {
    SENTENCE("SENTENCE", 1), CHUNK("CHUNK", 2), TERM("TERM", 3), LABEL("LABEL", 4), ID("ID", 5), INDEX("INDEX", 6), LINK("LINK", 7), REL("REL", 8), READ(
            "READ", 9), BASE("BASE", 10), PART("PART", 11), CLASS("CLASS", 12), CFORM("CFORM", 13);


    private int intValue;
    private String name;


    private PatternParseElementType(String name, final int anIntValue) {
        this.name = name;
        this.intValue = anIntValue;
    }


    /**
     * enum定数から整数へ変換
     * 
     * @return
     */
    public int getIntValue() {
        return intValue;
    }


    /**
     * 整数からenum定数へ変換
     * 
     * @param anIntValue
     * @return
     */
    public static PatternParseElementType valueOf(final int anIntValue) {
        for (PatternParseElementType d : values()) {
            if (d.getIntValue() == anIntValue) {
                return d;
            }
        }
        return null;
    }


    /**
     * 名称取得
     * 
     * @return
     */
    public String getName() {
        return name;
    }
}
