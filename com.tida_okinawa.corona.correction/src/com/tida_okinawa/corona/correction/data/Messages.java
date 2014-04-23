/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/10 13:54:49
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.data;

import org.eclipse.osgi.util.NLS;

/**
 * @author kousuke-morishima
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.data.messages"; //$NON-NLS-1$
    public static String CoronaDocumentDefinition_StringValueOf_Char;
    public static String CoronaDocumentDefinition_StringValueOf_False;
    public static String CoronaDocumentDefinition_StringValueOf_Phrase;
    public static String CoronaDocumentDefinition_StringValueOf_String;
    public static String CoronaDocumentDefinition_StringValueOf_True;
    public static String CoronaDocumentDefinition_StringValueOf_Whole;
    public static String CoronaDocumentInformation_DefaultValue_Extension;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
