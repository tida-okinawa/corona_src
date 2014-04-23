/**
 * @version $$Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $$
 * 
 * 2012/10/15 09:35:15
 * @author yukihiro-kinjyo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.help.context;

import org.eclipse.osgi.util.NLS;

/**
 * 文字列定義クラス
 * 
 * @author yukihiro-kinjo
 * 
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.help.context.messages"; //$NON-NLS-1$
    public static String ManualPdfOpenAction_ERROR;
    public static String ManualPdfOpenAction_NOT_FOUND_MANUAL;
    public static String ManualPdfOpenAction_OPEN_FAILED_MANUAL;
    public static String PatternPdfOpenAction_ERROR;
    public static String PatternPdfOpenAction_NOT_FOUND_PATTERN_MANUAL;
    public static String PatternPdfOpenAction_OPEN_FAILED_PATTERN_MANUAL;
    public static String TutorialPdfOpenAction_ERROR;
    public static String TutorialPdfOpenAction_NOT_FOUND_TUTORIAL;
    public static String TutorialPdfOpenAction_OPEN_FAILED_TUTORIAL;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
