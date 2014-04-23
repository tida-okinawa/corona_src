/**
 * @version $Id: TemplateUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/26 22:59:13
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import org.xml.sax.Attributes;

import com.tida_okinawa.corona.correction.parsing.model.IModelParser;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateUtil {
    static final String TAG_TERM = Messages.TEMPLATE_UTIL_TAG_TERM;
    static final String TAG_ORDER = Messages.TEMPLATE_UTIL_TAG_ORDER;
    static final String TAG_SEQUENCE = Messages.TEMPLATE_UTIL_TAG_SEQUENCE;
    static final String TAG_OR = Messages.TEMPLATE_UTIL_TAG_OR;
    static final String TAG_AND = Messages.TEMPLATE_UTIL_TAG_AND;
    static final String TAG_NOT = Messages.TEMPLATE_UTIL_TAG_NOT;
    static final String TAG_LINK = Messages.TEMPLATE_UTIL_TAG_LINK;
    static final String TAG_SOURCE = Messages.TEMPLATE_UTIL_TAG_SOURCE;
    static final String TAG_DEST = Messages.TEMPLATE_UTIL_TAG_DESTINATION;
    static final String TAG_MODIFICATION = Messages.TEMPLATE_UTIL_TAG_MODIFICATION;
    static final String TAG_PATTERN = Messages.TEMPLATE_UTIL_TAG_PATTERN;
    static final String TAG_COMPARE = Messages.TEMPLATE_UTIL_TAG_COMPARE;
    static final String TAG_QUANTIFIER0 = Messages.TEMPLATE_UTIL_TAG_QUANTIFIER0;

    static final String ATTR_BASE = Messages.TEMPLATE_UTIL_ATTR_BASE;
    static final String ATTR_PART = Messages.TEMPLATE_UTIL_ATTR_PART;
    static final String ATTR_CLASS = Messages.TEMPLATE_UTIL_ATTR_CLASS;
    static final String ATTR_LABEL = Messages.TEMPLATE_UTIL_ATTR_LABEL;
    static final String ATTR_ID = Messages.TEMPLATE_UTIL_ATTR_ID;
    static final String ATTR_TYPE = Messages.TEMPLATE_UTIL_ATTR_TYPE;
    static final String ATTR_EXEC = Messages.TEMPLATE_UTIL_ATTR_EXEC;
    static final String ATTR_VALUE = Messages.TEMPLATE_UTIL_ATTR_VALUE;
    static final String ATTR_QUANT = Messages.TEMPLATE_UTIL_ATTR_QUANTIFIER;
    static final String ATTR_SEARCH = Messages.TEMPLATE_UTIL_ATTR_SEARCHSCOPE;
    static final String ATTR_FIX = Messages.TEMPLATE_UTIL_ATTR_FIX;
    static final String ATTR_FIX_TRUE = Messages.TEMPLATE_UTIL_ATTR_TRUE;
    static final String ATTR_FIX_FALSE = Messages.TEMPLATE_UTIL_ATTR_FALSE;
    static final String ATTR_KIND = Messages.TEMPLATE_UTIL_ATTR_KIND;
    static final String ATTR_KIND_NULL = Messages.TEMPLATE_UTIL_ATTR_ISNULL;
    /** 種類が単語（ISWORD） */
    public static final String ATTR_KIND_WORD = Messages.TEMPLATE_UTIL_ATTR_ISWORD;
    /** 種類がラベル（ISLABEL） */
    public static final String ATTR_KIND_LABEL = Messages.TEMPLATE_UTIL_ATTR_ISLABEL;

    private static IModelParser<Template, String> templateParser = new TemplateModelParser();


    /**
     * 変換（ひな型⇒文字列）
     * 
     * @param template
     *            ひな型
     * @return 文字列
     */
    public static String convertFrom(Template template) {
        String templateString = templateParser.getEncoder().encode(template);
        return templateString;
    }


    /**
     * 変換（文字列⇒ひな型）
     * 
     * @param templateString
     *            文字列
     * @return ひな型
     */
    public static Template convertFrom(String templateString) {
        Template ret = templateParser.getDecoder().encode(templateString);
        return ret;
    }


    /**
     * @param attr
     *            属性
     * @param label
     *            属性名
     * @param defaultValue
     *            intに変換できなかった場合の戻り値
     * @return
     *         int
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
