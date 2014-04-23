/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/06 09:45:38
 * @author shingo-kuniyoshi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.ui.handlers.messages"; //$NON-NLS-1$
    public static String CleansingHandler_ErrorMessage_NoInputData_StartsWithFolder;
    public static String CleansingHandler_ErrorMessage_NoInputData_StartsWithTarget;
    public static String DbViewDeleteHandler_dialogDelData;
    public static String DbViewDeleteHandler_labelPatternDic;
    public static String DbViewDeleteHandler_labelRefPatternDic;
    public static String DbViewDeleteHandler_logEndOfDelJob;
    public static String DbViewDeleteHandler_titleRefPatternDic;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
