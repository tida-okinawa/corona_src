/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/25 10:55:20
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.dashboard;

import org.eclipse.osgi.util.NLS;

/**
 * @author kousuke-morishima
 * 
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.dashboard.messages"; //$NON-NLS-1$
    public static String AbstractResultPatternExportGraphActionDelegate_extension;
    public static String AbstractResultPatternExportGraphActionDelegate_templateName;
    public static String ResultPatternExportGraph_Delimiter;
    public static String ResultPatternExportGraph_DialogTitle_FailedGetTemplateFile;
    public static String ResultPatternExportGraph_DialogTitle_FailedOpenFile;
    public static String ResultPatternExportGraph_DialogTitle_OutputFile;
    public static String ResultPatternExportGraph_ErrorMessage_FailedExportGraph;
    public static String ResultPatternExportGraph_ErrorMessage_FileNotFound;
    public static String ResultPatternExportGraph_JobName_OutputResultPattern;
    public static String ResultPatternExportGraph_TaskName_ExportGraph;
    public static String ResultPatternExportGraphAction_ColumnName_PatternName;
    public static String ResultPatternExportGraphAction_ColumnName_PatternType;
    public static String ResultPatternExportGraphAction_ColumnName_RecordId;
    public static String ResultPatternExportGraphAction_ColumnName_Text;
    public static String ResultPatternExportGraphAction_TaskName_DataLayout;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
