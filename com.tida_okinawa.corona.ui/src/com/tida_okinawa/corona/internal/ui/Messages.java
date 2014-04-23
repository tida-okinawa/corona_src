/**
 * @version $Id$
 * 
 * 2013/03/05 18:19:00
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author kousuke-morishima
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.internal.ui.messages"; //$NON-NLS-1$
    public static String CoronaConstants_FolderName_ClaimData;
    public static String CoronaConstants_FolderName_CommonDictionary;
    public static String CoronaConstants_FolderName_Correction;
    public static String CoronaConstants_FolderName_Dictionary;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
