/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/06 09:45:38
 * @author shingo-kuniyoshi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.db.action;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.ui.views.db.action.messages"; //$NON-NLS-1$
    public static String DeletePatternTypeAction_labelCategory1;
    public static String DeletePatternTypeAction_labelCategory2;
    public static String DeletePatternTypeAction_labelCategory3;
    public static String DeletePatternTypeAction_labelDelPatternCategory;
    public static String DeletePatternTypeAction_labelId;
    public static String DeletePatternTypeAction_labelNoValue;
    public static String DeletePatternTypeAction_labelPatternCategory;
    public static String DeletePatternTypeAction_logCantDel;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
