/**
 * @version $Id: PatternElementType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
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
public enum PatternElementType {
    PATTERN("PATTERN", 0), TERM("TERM", 1), AND("AND", 2), OR("OR", 3), NOT("NOT", 4), ORDER("ORDER", 5), SEQUENCE("SEQUENCE", 6), MODIFICATION("MODIFICATION",
            7), SOURCE("SOURCE", 8), DESTINATION("DESTINATION", 9), LINK("LINK", 10), QUANTIFIER0("QUANTIFIER0", 11), SCOPE("SCOPE", 12);

    private int intValue;
    private String name;


    private PatternElementType(String name, final int anIntValue) {
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
    public static PatternElementType valueOf(final int anIntValue) {
        for (PatternElementType d : values()) {
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
