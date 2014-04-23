/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/21 22:03:04
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import org.eclipse.osgi.util.NLS;

/**
 * @author s.takuro
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.internal.ui.views.model.impl.messages"; //$NON-NLS-1$
    public static String CoronaModel_errorNotAvailable;
    public static String CoronaModel_errorProjectId;
    public static String CoronaModel_leftParenthesis;
    public static String CoronaModel_RightParenthesis;
    public static String FileContent_errorGetDataFile;
    public static String FileContent_errorGetDic;
    public static String FileContent_errorGetResult;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
