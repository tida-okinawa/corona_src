/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2013/01/10
 * @author shingo-kuniyoshi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.actions;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.internal.ui.actions.messages"; //$NON-NLS-1$
    public static String DeleteAction_jobDelete;
    public static String DeleteAction_jobSearchDeleteItems;
    public static String DeleteAction_labelDelDBProject;
    public static String DeleteAction_labelDelete;
    public static String DeleteAction_labelDeleteCommond;
    public static String DeleteAction_labelDelNonDBProject;
    public static String DeleteAction_labelDelPatternDics;
    public static String DeleteAction_labelDelProduct;
    public static String DeleteAction_labelDelProductDics;
    public static String DeleteAction_labelDelProducts;
    public static String DeleteAction_labelDelProjectDics;
    public static String DeleteAction_labelDelProjects;
    public static String DeleteAction_labelDelRefPattern;
    public static String DeleteAction_labelDelUIDics;
    public static String DeleteAction_monitorDelete;
    public static String DeleteAction_monitorSearchdeleteItems;
    public static String DeleteAction_titleRefPattern;
    public static String dialogLabel;
    public static String errLogCsvExportFail;
    public static String errLogExportFail;
    public static String errLogReason;
    public static String labelUnsave;
    public static String monitorFileCheck;
    public static String PartPatternModifyAction_labelRefPattern;
    public static String RenameAction_errorFindItem;
    public static String RenameAction_errorInputCancel;
    public static String RenameAction_errorNotFile_1;
    public static String RenameAction_errorNotFile_2;
    public static String RenameAction_newName;
    public static String RenameAction_rename;
    public static String RenameAction_renameAndKey;
    public static String ResultPatternEditorActionDelegate_claim;
    public static String ResultPatternEditorActionDelegate_createTable;
    public static String ResultPatternEditorActionDelegate_div;
    public static String ResultPatternEditorActionDelegate_field;
    public static String ResultPatternEditorActionDelegate_pattern;
    public static String ResultPatternEditorActionDelegate_record;
    public static String ResultPatternEditorActionDelegate_text;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
