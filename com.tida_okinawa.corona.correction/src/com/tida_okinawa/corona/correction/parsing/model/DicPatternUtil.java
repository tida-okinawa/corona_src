/**
 * @version $Id: DicPatternUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/01 17:16:17
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import org.xml.sax.Attributes;

/**
 * @author kousuke-morishima
 */
public class DicPatternUtil {
    static final String TAG_TERM = "TERM";
    static final String TAG_ORDER = "ORDER";
    static final String TAG_SEQUENCE = "SEQUENCE";
    static final String TAG_OR = "OR";
    static final String TAG_AND = "AND";
    static final String TAG_NOT = "NOT";
    static final String TAG_LINK = "LINK";
    static final String TAG_SOURCE = "SOURCE";
    static final String TAG_DEST = "DESTINATION";
    static final String TAG_MODIFICATION = "MODIFICATION";
    static final String TAG_PATTERN = "PATTERN";
    static final String TAG_COMPARE = "COMPARE";
    static final String TAG_QUANTIFIER0 = "QUANTIFIER0";

    static final String ATTR_BASE = "BASE";
    static final String ATTR_PART = "PART";
    static final String ATTR_CLASS = "CLASS";
    static final String ATTR_LABEL = "LABEL";
    static final String ATTR_ID = "ID";
    static final String ATTR_TYPE = "TYPE";
    static final String ATTR_EXEC = "EXEC";
    static final String ATTR_VALUE = "VALUE";
    static final String ATTR_QUANT = "QUANTIFIER";
    static final String ATTR_SEARCH = "SEARCHSCOPE";

    private static IModelParser<Pattern, String> patternParser = new PatternModelParser();


    /**
     * パターン辞書の要素(IPattern)を作成する
     * 
     * @param record
     * @return
     */
    public static String convertFrom(Pattern pattern) {
        String patternString = patternParser.getEncoder().encode(pattern);
        return patternString;
    }


    /**
     * @param patternString
     * @return may be null
     */
    public static Pattern convertFrom(String patternString) {
        /* Decoderでencodeするのかぁ・・・実に悩ましい矛盾だ */
        Pattern ret = patternParser.getDecoder().encode(patternString);
        return ret;
    }


    /**
     * @param attr
     * @param label
     *            属性名
     * @param defaultValue
     *            intに変換できなかった場合の戻り値
     * @return
     */
    static int getIntValue(Attributes attr, String label, int defaultValue) {
        String value = attr.getValue(label);
        int ret = defaultValue;
        try {
            ret = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // nothing to do
        }
        return ret;
    }
}
