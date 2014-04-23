/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/21 18:06:40
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem.preference;

import org.eclipse.osgi.util.NLS;

/**
 * @author s.takuro
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.morphem.preference.messages"; //$NON-NLS-1$
    public static String MorphemePreferencePage_buttonSettingTest;
    public static String MorphemePreferencePage_fieldEditorConvSJIS;
    public static String MorphemePreferencePage_fieldEditorJumanDicFolder;
    public static String MorphemePreferencePage_fieldEditorJumanFolder;
    public static String MorphemePreferencePage_fieldEditorJumanOption;
    public static String MorphemePreferencePage_fieldEditorJumanPath;
    public static String MorphemePreferencePage_fieldEditorJumanSetting;
    public static String MorphemePreferencePage_fieldEditorKnp;
    public static String MorphemePreferencePage_fieldEditorKnpFolder;
    public static String MorphemePreferencePage_fieldEditorKnpOption;
    public static String MorphemePreferencePage_fieldEditorKnpPath;
    public static String MorphemePreferencePage_fieldEditorKnpServerSetting;
    public static String MorphemePreferencePage_fieldEditorPassword;
    public static String MorphemePreferencePage_fieldEditorUserName;
    public static String MorphemePreferencePage_fieldEditorUseServerMode;
    public static String MorphemePreferencePage_textSendDic;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
