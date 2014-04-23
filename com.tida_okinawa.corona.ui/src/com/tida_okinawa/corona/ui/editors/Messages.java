/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/21 18:13:59
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.osgi.util.NLS;

/**
 * @author s.takuro
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.ui.editors.messages"; //$NON-NLS-1$
    public static String FlucDicEditor_messageCycle;
    public static String FlucDicEditor_messageErrorNotTerm;
    public static String FlucDicEditor_messageRegistered;
    public static String FlucDicEditor_messageSelectDependentTerm;
    public static String FlucDicEditor_messageSelectMainTerm;
    public static String FlucDicEditor_titleSelectDependentTerm;
    public static String FlucDicEditor_titleSelectMainTerm;
    public static String FlucDicEditor_unsavedItem;
    public static String FrequentTermEditor_labelWarning_1;
    public static String FrequentTermEditor_labelWarning_2;
    public static String FrequentTermEditor_messageConfirm;
    public static String FrequentTermEditor_messageError;
    public static String FrequentTermEditor_messageErrorInput;
    public static String FrequentTermEditor_messageNotDisplayNext;
    public static String FrequentTermEditor_messageSetedTerm;
    public static String FrequentTermEditor_messageUndefinedTerm;
    public static String LabelRelationGroup_textFailShowLabel;
    public static String SynonymDicEditor_messageCycle;
    public static String SynonymDicEditor_messageRegistered;
    public static String SynonymDicEditor_messageSaveFail;
    public static String SynonymDicEditor_messageSelectDependentTerm;
    public static String SynonymDicEditor_messageSelectMainTerm;
    public static String SynonymDicEditor_titleSelectDependentTerm;
    public static String SynonymDicEditor_titleSelectMainTerm;
    public static String SynonymDicEditor_unsavedItem;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
