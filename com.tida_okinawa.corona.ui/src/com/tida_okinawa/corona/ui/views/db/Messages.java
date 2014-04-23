/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/21 18:12:22
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.db;

import org.eclipse.osgi.util.NLS;

/**
 * @author s.takuro
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.ui.views.db.messages"; //$NON-NLS-1$
    public static String DataBaseView_8;
    public static String DataBaseView_inputDialogChangeName;
    public static String DataBaseView_inputDialogNewName;
    public static String DataBaseView_leftParenthesis;
    public static String DataBaseView_messageDeleteData;
    public static String DataBaseView_messageDeleteDatabase;
    public static String DataBaseView_messageFailDelete;
    public static String DataBaseView_messageFailDeleteDic;
    public static String DataBaseView_messageWarning;
    public static String DataBaseView_rightParenthesis;
    public static String DataBaseViewLabelProvider_Claim_TableAndFile;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
