/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/26 19:42:51
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.preference;

import org.eclipse.osgi.util.NLS;

/**
 * @author kousuke-morishima
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.preference.messages"; //$NON-NLS-1$
    public static String AutoRunSchedulePage_ColumnTitle_Duration;
    public static String AutoRunSchedulePage_ColumnTitle_NextRun;
    public static String AutoRunSchedulePage_ColumnTitle_Run;
    public static String AutoRunSchedulePage_ColumnTitle_Task;
    public static String AutoRunSchedulePage_DialogMessage_ConfirmRestore;
    public static String AutoRunSchedulePage_DialogTitle_ConfirmRestore;
    public static String AutoRunSchedulePage_Label_Delete_D;
    public static String AutoRunSchedulePage_Label_Edit_E;
    public static String AutoRunSchedulePage_Label_EndOfMonth;
    public static String AutoRunSchedulePage_Label_Friday;
    public static String AutoRunSchedulePage_Label_Monday;
    public static String AutoRunSchedulePage_Label_New_N;
    public static String AutoRunSchedulePage_Label_NoFile;
    public static String AutoRunSchedulePage_Label_NoTask;
    public static String AutoRunSchedulePage_Label_Saturday;
    public static String AutoRunSchedulePage_Label_Sunday;
    public static String AutoRunSchedulePage_Label_Thursday;
    public static String AutoRunSchedulePage_Label_Tuesday;
    public static String AutoRunSchedulePage_Label_Wednesday;
    public static String AutoRunSchedulePage_PageTitle;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
