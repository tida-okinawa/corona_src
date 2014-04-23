/**
 * @version $Id: QuantifierType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/20 17:37:29
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

/**
 * 
 * @author kyohei-miyazato
 */
public enum QuantifierType {
    /* TODO QUANT_ANYはβで対応？ */
    /**
     * 0..1
     */
    QUANT_ONE("0..1", 1),
    // QUANT_ANY("1..*",2),
    /**
     * なし
     */
    QUANT_NONE("なし", -1);

    private static final QuantifierType[] ARRAY = { QUANT_NONE, QUANT_ONE };
    private int intValue;
    private String name;


    private QuantifierType(String name, final int anIntValue) {
        this.name = name;
        this.intValue = anIntValue;
    }


    public int getIntValue() {
        return intValue;
    }


    /**
     * 整数からenum定数へ変換<br />
     * {@link #NONE}のintValueは-1だが、anIntValueに0を指定した時も、 {@link #NONE}が返る<br />
     * intValueと一致しない場合も{@link #NONE}を返す
     * 
     * @param anIntValue
     * @return
     */
    public static QuantifierType valueOf(final int anIntValue) {
        if ((anIntValue > 0) && (anIntValue < ARRAY.length)) {
            return ARRAY[anIntValue];
        }
        return QUANT_NONE;
    }


    /**
     * 名称からenum定数へ変換
     * 
     * @param name
     * @return
     */
    public static QuantifierType valueOfName(String name) {
        for (QuantifierType d : values()) {
            if (d.name.equals(name)) {
                return d;
            }
        }
        return QUANT_NONE;
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
