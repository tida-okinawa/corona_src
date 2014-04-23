/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/21 20:55:36
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author s.takuro
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.ui.messages"; //$NON-NLS-1$
    public static String ConnectionPreferencePage_connectH2DbUser;
    public static String ConnectionPreferencePage_connectH2DbPassWord;
    public static String ConnectionPreferencePage_buttonConnectComeback;
    public static String ConnectionPreferencePage_buttonConnectComebackToolTip;
    public static String ConnectionPreferencePage_buttonConnectionTest;
    public static String ConnectionPreferencePage_buttonConnectTest;
    public static String ConnectionPreferencePage_buttonEdit;
    public static String ConnectionPreferencePage_buttonMarkup;
    public static String ConnectionPreferencePage_buttonRemove;
    public static String ConnectionPreferencePage_columnConnectionName;
    public static String ConnectionPreferencePage_columnConnectionString;
    //public static String ConnectionPreferencePage_connectionParameter;
    public static String ConnectionPreferencePage_connectionH2DbParameter;
    public static String ConnectionPreferencePage_messageConnectComeback;
    public static String ConnectionPreferencePage_messageConnectFail;
    public static String ConnectionPreferencePage_messageConnectFailDetail;
    public static String ConnectionPreferencePage_messageConnectionDatabase;
    public static String ConnectionPreferencePage_messageConnectionDatabaseFail;
    public static String ConnectionPreferencePage_messageConnectOk;
    public static String ConnectionPreferencePage_messageConnectSuccess;
    public static String ConnectionPreferencePage_messageConnectTest;
    public static String ConnectionPreferencePage_messageEntryConnectionName;
    public static String ConnectionPreferencePage_messageEntryConnectionString;
    public static String ConnectionPreferencePage_messageEntryConnectionUser;
    public static String ConnectionPreferencePage_messageOk;
    public static String ConnectionPreferencePage_rightSquareBracket;
    public static String ConnectionPreferencePage_shellConnectDatabaseSetting;
    public static String ConnectionPreferencePage_textConnectionName;
    public static String ConnectionPreferencePage_textConnectionPassword;
    public static String ConnectionPreferencePage_textConnectionString;
    public static String ConnectionPreferencePage_textConnectionUser;
    public static String ConnectionPreferencePage_textDatabaseOutline;
    public static String ConnectionPreferencePage_textNew;
    public static String ConnectionPreferencePage_textWarningComma;
    public static String Icons_ErrorMessage_CannotUseThisClass;
    public static String TIDA_openEditorMessage01;
    public static String TIDA_openEditorMessage02;
    public static String TIDA_openEditorMessage03;
    public static String TIDA_openEditorMessage04;
    public static String TIDA_openEditorCMessage01;
    public static String TIDA_openEditorCMessage02;
    public static String TIDA_openEditorCMessage03;
    public static String TIDA_openEditorCMessage04;
    public static String TIDA_eventExceptionMessage;
    public static String TIDA_exceptionWarning000_message;
    public static String TIDA_exceptionWarning000_title;
    public static String TIDA_title_Detail;
    public static String UIActivator_connectErrDbInfo;
    public static String UIActivator_connectErrMessage;
    public static String UIActivator_connectErrTitle;
    public static String UIActivator_LastSep;
    public static String UIActivator_VerErrDbInfo;
    public static String UIActivator_VerErrDBNonInfo;
    public static String UIActivator_VerErrDBValue;
    public static String UIActivator_VerErrMessage;
    public static String UIActivator_VerErrMessageNonTable;
    public static String UIActivator_VerErrMessageSqlErr;
    public static String UIActivator_VerErrTitle;
    public static String UIActivator_VerErrValue;
    public static String UIActivator_VerWarMessage;
    public static String UIActivator_VerWarTitle;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
