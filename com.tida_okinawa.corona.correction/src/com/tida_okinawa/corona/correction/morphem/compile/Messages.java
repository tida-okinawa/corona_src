/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/22 21:00:08
 * @author shingo-kuniyoshi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem.compile;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.morphem.compile.messages"; //$NON-NLS-1$
    public static String DicCompileExecution_logEndCode;
    public static String DicCompileExecution_logJumanUpdate;
    public static String DicCompileExecution_logNoJumanUpdate;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
