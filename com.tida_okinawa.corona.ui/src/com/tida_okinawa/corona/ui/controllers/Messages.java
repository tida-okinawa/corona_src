/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2013/02/22 ‏‎11:00:03
 * @author shingo-kuniyoshi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.ui.controllers.messages"; //$NON-NLS-1$
    public static String MorphemeController_body;
    public static String MorphemeController_ellipsis;
    public static String MorphemeController_errNullRecord;
    public static String MorphemeController_PERIOD;
    public static String MorphemeController_QUESTION;
    public static String MorphemeController_stringCount;
    public static String MorphemeController_tooManyBunsetsu;
    public static String MorphemeController_tooManyMrph;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
