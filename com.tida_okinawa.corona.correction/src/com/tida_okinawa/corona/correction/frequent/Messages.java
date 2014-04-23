/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/20 13:00:08
 * @author shingo-kuniyoshi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.frequent;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.frequent.messages"; //$NON-NLS-1$
    /**
     * 形容詞を表す
     */
    public static String FrequentCounter_ADJECTIVE;

    /**
     * 名詞を表す
     */
    public static String FrequentCounter_NOUN;

    /**
     * 未定義語
     */
    public static String FrequentCounter_UNDEFINED_WORD;

    /**
     * 動詞を表す
     */
    public static String FrequentCounter_VERB;

    /**
     * 品詞細分類を表す
     */
    public static String FrequentRecord_CLASS;

    /**
     * カウントを表す
     */
    public static String FrequentRecord_COUNT;

    /**
     * 所属辞書を表す
     */
    public static String FrequentRecord_DICS;

    /**
     * 活用形を表す
     */
    public static String FrequentRecord_FORM;

    /**
     * 品詞を表す
     */
    public static String FrequentRecord_PART;

    /**
     * 読みを表す
     */
    public static String FrequentRecord_READING;

    /**
     * 未定義語を表す
     */
    public static String FrequentRecord_UNDEFINED_WORD;

    /**
     * 単語を表す
     */
    public static String FrequentRecord_WORD;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
