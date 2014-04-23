/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/12 21:24:45
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import org.eclipse.osgi.util.NLS;

/**
 * @author s.takuro
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.template.messages"; //$NON-NLS-1$
    /** (係り受け) */
    public static String TEMPLATE_MODIFICATION_STRING;
    /** (係り */
    public static String TEMPLATE_MODIFICATION_STRING1;
    /** ) */
    public static String TEMPLATE_MODIFICATION_STRING2;
    /** 係り */
    public static String TEMPLATE_MODIFICATION_TYPE;
    /** 依存 */
    public static String TEMPLATE_MODIFICATION_TYPE_DEPEND;
    /** 先 */
    public static String TEMPLATE_MODIFICATION_TYPE_DEST;
    /** 並列 */
    public static String TEMPLATE_MODIFICATION_TYPE_PARALLEL;
    /** 元 */
    public static String TEMPLATE_MODIFICATION_TYPE_SOURCE;
    /** (And) */
    public static String TEMPLATE_AND_STRING;
    /** : */
    public static String TEMPLATE_COLON1;
    /** : */
    public static String TEMPLATE_COLON2;
    /** (Compare) */
    public static String TEMPLATE_COMPARE_STRING_EN;
    /** (比較) */
    public static String TEMPLATE_COMPARE_STRING_JP;
    /** encoding */
    public static String TEMPLATE_DECODER_ENCODING;
    /** CDATA */
    public static String TEMPLATE_ENCODER_CDDATA;
    /** file.encoding */
    public static String TEMPLATE_ENCODER_ENCODING;
    /** 変換元のelementがnullです */
    public static String TEMPLATE_ENCODER_STRING_NULL;
    /** (参照) */
    public static String TEMPLATE_LINK_STRING;
    /** (Not) */
    public static String TEMPLATE_NOT_STRING;
    /** (Or) */
    public static String TEMPLATE_OR_STRING;
    /** (順序) */
    public static String TEMPLATE_ORDER_STRING;
    /** (連続) */
    public static String TEMPLATE_SEQUENCE_STRING;
    /** 検索範囲 ： */
    public static String TEMPLATE_STRING_SEARCH_SCOPE;
    /** (単語) */
    public static String TEMPLATE_TERM_STRING;
    /** ISLABEL */
    public static String TEMPLATE_TERM_TYPE_LABEL;
    /** ISNULL */
    public static String TEMPLATE_TERM_TYPE_NULL;
    /** ISWORD */
    public static String TEMPLATE_TERM_TYPE_WORD;
    /** BASE */
    public static String TEMPLATE_UTIL_ATTR_BASE;
    /** CLASS */
    public static String TEMPLATE_UTIL_ATTR_CLASS;
    /** EXEC */
    public static String TEMPLATE_UTIL_ATTR_EXEC;
    /** FALSE */
    public static String TEMPLATE_UTIL_ATTR_FALSE;
    /** FIX */
    public static String TEMPLATE_UTIL_ATTR_FIX;
    /** ID */
    public static String TEMPLATE_UTIL_ATTR_ID;
    /** ISLABEL */
    public static String TEMPLATE_UTIL_ATTR_ISLABEL;
    /** ISNULL */
    public static String TEMPLATE_UTIL_ATTR_ISNULL;
    /** ISWORD */
    public static String TEMPLATE_UTIL_ATTR_ISWORD;
    /** KIND */
    public static String TEMPLATE_UTIL_ATTR_KIND;
    /** LABEL */
    public static String TEMPLATE_UTIL_ATTR_LABEL;
    /** PART */
    public static String TEMPLATE_UTIL_ATTR_PART;
    /** QUANTIFIER */
    public static String TEMPLATE_UTIL_ATTR_QUANTIFIER;
    /** SEARCHSCOPE */
    public static String TEMPLATE_UTIL_ATTR_SEARCHSCOPE;
    /** TRUE */
    public static String TEMPLATE_UTIL_ATTR_TRUE;
    /** TYPE */
    public static String TEMPLATE_UTIL_ATTR_TYPE;
    /** VALUE */
    public static String TEMPLATE_UTIL_ATTR_VALUE;
    /** AND */
    public static String TEMPLATE_UTIL_TAG_AND;
    /** AND */
    public static String TEMPLATE_UTIL_TAG_COMPARE;
    /** DESTINATION */
    public static String TEMPLATE_UTIL_TAG_DESTINATION;
    /** LINK */
    public static String TEMPLATE_UTIL_TAG_LINK;
    /** MODIFICATION */
    public static String TEMPLATE_UTIL_TAG_MODIFICATION;
    /** NOT */
    public static String TEMPLATE_UTIL_TAG_NOT;
    /** OR */
    public static String TEMPLATE_UTIL_TAG_OR;
    /** ORDER */
    public static String TEMPLATE_UTIL_TAG_ORDER;
    /** PATTERN */
    public static String TEMPLATE_UTIL_TAG_PATTERN;
    /** QUANTIFIER0 */
    public static String TEMPLATE_UTIL_TAG_QUANTIFIER0;
    /** SEQUENCE */
    public static String TEMPLATE_UTIL_TAG_SEQUENCE;
    /** SOURCE */
    public static String TEMPLATE_UTIL_TAG_SOURCE;
    /** TERM */
    public static String TEMPLATE_UTIL_TAG_TERM;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
