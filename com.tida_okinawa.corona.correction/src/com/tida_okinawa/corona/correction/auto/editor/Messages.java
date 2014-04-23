/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2013/02/19
 * @author shingo-kuniyoshi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.correction.auto.editor;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.auto.editor.messages"; //$NON-NLS-1$

    /**
     * 文字コード
     */
    public static String CleansingFlowEditor_encode;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
