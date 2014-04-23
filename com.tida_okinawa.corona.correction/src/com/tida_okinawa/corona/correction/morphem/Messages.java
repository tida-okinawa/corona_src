/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2013/02/19
 * @author shingo-kuniyoshi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.correction.morphem;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.morphem.messages"; //$NON-NLS-1$

    /**
     * 係り先を表す
     */
    public static String MODIFICATION_DESTINATION;

    /**
     * 係り元を表す
     */
    public static String MODIFICATION_SOURCE;

    /**
     * アルファベットを表す
     */
    public static String MorphemeRelationReader_ALPHABET;

    /**
     * カタカナを表す
     */
    public static String MorphemeRelationReader_KATAKANA;

    /**
     * その他を表す
     */
    public static String MorphemeRelationReader_OTHER_WORD;

    /**
     * 変更品詞を表す
     */
    public static String MorphemeRelationReader_PART_CHANGE;

    /**
     * 未定義後を表す
     */
    public static String MorphemeRelationReader_UNDEFINED_WORD;

    /**
     * 表記を表す
     */
    public static String SyntaxStructure_NOTATION;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
