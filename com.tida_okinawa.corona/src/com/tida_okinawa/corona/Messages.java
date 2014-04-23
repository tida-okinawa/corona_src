/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2013/02/16
 * @author shingo-kuniyoshi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 *         okinawa.tida_okinawa.coronaプロジェクト内の外部文字列を定義する
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.messages"; //$NON-NLS-1$
    /**
     * 文字コード
     */
    public static String AbstractInputValidator_encodeType;

    /**
     * 入力可能半角文字数
     */
    public static String AbstractInputValidator_labelByteLengthLimit;

    /**
     * 入力可能全角文字数
     */
    public static String AbstractInputValidator_labelLengthLimit;


    /**
     * 入力可能文字オーバー文言
     */
    public static String AbstractInputValidator_labelLengthOver;


    /**
     * 値未入力時
     */
    public static String AbstractInputValidator_labelNonValue;


    /**
     * 実行ラベル
     */
    public static String PreviewDialog_labelExecute;

    /**
     * プレビューラベル
     */
    public static String PreviewDialog_labelPreview;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
