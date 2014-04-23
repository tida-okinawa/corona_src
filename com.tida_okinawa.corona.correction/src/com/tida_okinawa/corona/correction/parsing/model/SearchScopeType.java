/**
 * @version $Id: SearchScopeType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/21 15:57:45
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;


/**
 * 
 * @author kyohei-miyazato
 */
public enum SearchScopeType {
    SEARCH_ALL("全体", 0), SEARCH_SENTENCE("一文", 1), SEARCH_SEGMENT("文節", 2);

    private static final SearchScopeType[] ARRAY = { SEARCH_ALL, SEARCH_SENTENCE, SEARCH_SEGMENT };
    private int intValue;
    private String name;


    private SearchScopeType(String name, final int anIntValue) {
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
    public static SearchScopeType valueOf(final int anIntValue) {
        if ((anIntValue >= 0) && (anIntValue < ARRAY.length)) {
            return ARRAY[anIntValue];
        }
        return SEARCH_ALL;
    }


    /**
     * 名称からenum定数へ変換
     * 
     * @param name
     * @return
     */
    public static SearchScopeType valueOfName(String name) {
        for (SearchScopeType d : values()) {
            if (d.name.equals(name)) {
                return d;
            }
        }
        return SEARCH_ALL;
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
