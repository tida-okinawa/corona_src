/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/07 17:47:38
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction;

import org.eclipse.osgi.util.NLS;

/**
 * @author kousuke-morishima
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.messages"; //$NON-NLS-1$

    /**
     * コンボボックス表示名（全ての空白を削除する場合）
     */
    public static String CleansingPreferencePage_comboAllSpace;

    /**
     * コンボボックス表示名（空白を削除しない場合）
     */
    public static String CleansingPreferencePage_comboNoSpace;

    /**
     * コンボボックス表示名（冗長な空白のみを削除する場合）
     */
    public static String CleansingPreferencePage_comboVervoseSpace;

    /**
     * 設定画面表示項目（全ての空白を削除）
     */
    public static String CleansingPreferencePage_labelAllSpace;

    /**
     * 設定画面表示項目（連語抽出時　最低ヒット回数）
     */
    public static String CleansingPreferencePage_labelHitCount;

    /**
     * 設定画面表示項目（空白を削除しない）
     */
    public static String CleansingPreferencePage_labelNoSpace;

    /**
     * 設定画面表示項目（削除対象とする空白）
     */
    public static String CleansingPreferencePage_labelTargetSpace;

    /**
     * 設定画面表示項目（冗長な空白を削除する）
     */
    public static String CleansingPreferencePage_labelVerboseSpace;

    public static String DocumentPreferencePage_ColumnName_Definition;
    public static String DocumentPreferencePage_ColumnName_Position;
    public static String DocumentPreferencePage_ColumnName_Remove;
    public static String DocumentPreferencePage_ColumnName_Target;
    public static String DocumentPreferencePage_ComboItem_Insert;
    public static String DocumentPreferencePage_DialogLabel_Definition;
    public static String DocumentPreferencePage_DialogLabel_DefinitionName;
    public static String DocumentPreferencePage_DialogLabel_ExsampleOfFileExtension;
    public static String DocumentPreferencePage_DialogLabel_FileExtension;
    public static String DocumentPreferencePage_DialogLabel_Position;
    public static String DocumentPreferencePage_DialogLabel_Remove;
    public static String DocumentPreferencePage_DialogLabel_Target;
    public static String DocumentPreferencePage_DialogTitle_DefinitionContent;
    public static String DocumentPreferencePage_DialogTitle_DefinitionInformation;
    public static String DocumentPreferencePage_ErrorMessage_IllegalFileExtension;
    public static String DocumentPreferencePage_ErrorMessage_MultipleSpecialDefinition;
    public static String DocumentPreferencePage_ErrorMessage_NoDefinition;
    public static String DocumentPreferencePage_ErrorMessage_NoDefinitionName;
    public static String DocumentPreferencePage_ErrorMessage_NoFileExtension;
    public static String DocumentPreferencePage_Label_DefinitionContents;
    public static String DocumentPreferencePage_Label_DefinitionInformation;
    public static String DocumentPreferencePage_Label_Edit_E;
    public static String DocumentPreferencePage_Label_Edit_Q;
    public static String DocumentPreferencePage_Label_Export_X;
    public static String DocumentPreferencePage_Label_Import_I;
    public static String DocumentPreferencePage_Label_New_N;
    public static String DocumentPreferencePage_Label_New_W;
    public static String DocumentPreferencePage_Label_Remove_D;
    public static String DocumentPreferencePage_Label_Remove_R;
    public static String DocumentPreferencePage_labelAllType;
    public static String DocumentPreferencePage_labelXmlType;
    public static String DocumentPreferencePage_Regex_FileExtension;
    /** 文字列をひとつバインドできる */
    public static String ErrorMessage_FailedReadFile;
    /** 文字列をひとつバインドできる */
    public static String ErrorMessage_FailedWriteFile;
    /** 文字列をひとつバインドできる */
    public static String ErrorMessage_FileNotFound;
    public static String ErrorTitle_FailedReadFile;
    public static String ErrorTitle_FailedWriteFile;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
