/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/28 10:24:14
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.collocation;

import org.eclipse.osgi.util.NLS;

/**
 * @author wataru-higa
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.ui.editors.collocation.messages"; //$NON-NLS-1$
    public static String CollocationTermPage_EDITOR_COLUMN_DIC;
    public static String CollocationTermPage_EDITOR_COLUMN_HIT;
    public static String CollocationTermPage_EDITOR_COLUMN_ORG;
    public static String CollocationTermPage_EDITOR_COLUMN_READ;
    public static String CollocationTermPage_EDITOR_COLUMN_TERM_PART;
    public static String CollocationTermPage_EDITOR_COLUMN_TERM_PART_DITAIL;
    public static String CollocationTermPage_EDITOR_TAB;
    public static String CollocationTermPage_EDITOR_TITLE;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
