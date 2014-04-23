/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/02 15:25:53
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry;

import org.eclipse.osgi.util.NLS;

/**
 * プラグイン内文字列定義クラス
 * 
 * @author yukihiro-kinjo
 * 
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.webentry.messages"; //$NON-NLS-1$
    public static String CreateClaimTableValidator_Characters;
    public static String CreateClaimTableValidator_DataName;
    public static String CreateClaimTableValidator_DataNameCharacterError;
    public static String CreateClaimTableValidator_PleaseInputDataName;
    public static String TwitterConnection_ErrorMessage_Failed_GetSearchRateLimit;
    public static String TwitterFinishProgress_ConnectTwitter;
    public static String TwitterFinishProgress_DataBaseImport;
    public static String TwitterFinishProgress_DataImportFailed;
    public static String TwitterFinishProgress_Error;
    public static String TwitterFinishProgress_ExecErratum;
    public static String TwitterFinishProgress_FailedQuery;
    public static String TwitterFinishProgress_GetCountOfZero;
    public static String TwitterFinishProgress_SearchExecTwitter;
    public static String TwitterFinishProgress_TwitterDataConvert;
    public static String TwitterFinishProgress_WebDataEntry;
    public static String TwitterPinInputDialog_AuthStartFailed;
    public static String TwitterPinInputDialog_button_OpenTwitterLoginPage;
    public static String TwitterPinInputDialog_Error;
    public static String TwitterPinInputDialog_InputPin;
    public static String TwitterPinInputDialog_label_EnterThePinGot;
    public static String TwitterQuerySettings_DataBaseNameExists;
    public static String TwitterQuerySettings_DataNameIsNull;
    public static String TwitterQuerySettings_GetCountLengthError;
    public static String TwitterQuerySettings_PleaseInputProductName;
    public static String TwitterQuerySettings_PleaseInputQuery;
    public static String TwitterQuerySettings_ProductNameCharacterError;
    public static String TwitterQuerySettings_ProductNameIsNull;
    public static String TwitterQuerySettings_ProductNameLengthError;
    public static String TwitterQuerySettings_QueryIsNull;
    public static String TwitterQuerySettings_QueryLengthError;
    public static String TwitterStatus_FailedLogin;
    public static String TwitterStatus_NoSettingAccount;
    public static String TwitterStatus_OkLogin;
    public static String TwitterStatus_SettingAccount;
    public static String TwitterStatus_Twitter;
    public static String WebEntryExternalControllerForTwitter_DataConvertFailed;
    public static String WebEntryExternalControllerForTwitter_DataErratumFailed;
    public static String WebEntryExternalControllerForTwitter_DataImportFailed;
    public static String WebEntryExternalControllerForTwitter_FailedQuery;
    public static String WebEntryExternalControllerForTwitter_GetCountOfZero;
    public static String WebEntryExternalControllerForTwitter_InvalidQuerySetting;
    public static String WebEntryExternalControllerForTwitter_UserCanceled;
    public static String WebEntryServiceSettingPage_ApiAccessFailed;
    public static String WebEntryServiceSettingPage_ApiExecFailed;
    public static String WebEntryServiceSettingPage_AuthError;
    public static String WebEntryServiceSettingPage_BrowserOpenFailed;
    public static String WebEntryServiceSettingPage_button_CreateTwitterAccount;
    public static String WebEntryServiceSettingPage_button_DeleteTwitterAccountSetting;
    public static String WebEntryServiceSettingPage_button_LoginTwitter;
    public static String WebEntryServiceSettingPage_Error;
    public static String WebEntryServiceSettingPage_group_SettingTwitterAccount;
    public static String WebEntryServiceSettingPage_label_AccountSettingStatus;
    public static String WebEntryServiceSettingPage_label_ApiRemit;
    public static String WebEntryServiceSettingPage_label_LoginStatus;
    public static String WebEntryServiceSettingPage_label_WebService;
    public static String WebEntryServiceSettingPage_NewSettingAuthFailed;
    public static String WebEntryServiceSettingPage_PageMessage;
    public static String WebEntryServiceSettingPage_PageTitle;
    public static String WebEntryServiceSettingPage_PleaseTwitterAccountSettingComplete;
    public static String WebEntryServiceSettingPage_QuestionDeleteTwitterAccountSetting;
    public static String WebEntryServiceSettingPage_TwitterConnectionFailedPleaseReSetting;
    public static String WebEntryServiceSettingPage_WebServiceSelect;
    public static String WebEntryServiceSettingPage_WebServiceSetting;
    public static String WebEntryTwitterSearchSettingPage_button_Body;
    public static String WebEntryTwitterSearchSettingPage_button_JapaneseOnly;
    public static String WebEntryTwitterSearchSettingPage_button_NowDateTimeInput;
    public static String WebEntryTwitterSearchSettingPage_button_PostDateTime;
    public static String WebEntryTwitterSearchSettingPage_button_ScreenName;
    public static String WebEntryTwitterSearchSettingPage_button_UserName;
    public static String WebEntryTwitterSearchSettingPage_DataNameExists;
    public static String WebEntryTwitterSearchSettingPage_group_GetItem;
    public static String WebEntryTwitterSearchSettingPage_label_DataName;
    public static String WebEntryTwitterSearchSettingPage_label_GetCount;
    public static String WebEntryTwitterSearchSettingPage_label_ProductName;
    public static String WebEntryTwitterSearchSettingPage_label_Query;
    public static String WebEntryTwitterSearchSettingPage_PageMessage;
    public static String WebEntryTwitterSearchSettingPage_PageTitle;
    public static String WebEntryTwitterSearchSettingPage_PleaseInputProductName;
    public static String WebEntryTwitterSearchSettingPage_PleaseInputQuery;
    public static String WebEntryTwitterSearchSettingPage_ProductNameCharacterError;
    public static String WebEntryTwitterSearchSettingPage_ProductNameLengthError;
    public static String WebEntryTwitterSearchSettingPage_QueryLengthError;
    public static String WebEntryWizard_WebEntryWizardTitle;
    public static String WebEntryTwitterSearchSettingPage_button_Follow;
    public static String WebEntryTwitterSearchSettingPage_button_Follower;
    public static String WebEntryTwitterSearchSettingPage_button_Profile;
    public static String WebEntryTwitterSearchSettingPage_button_TcoExpand;
    public static String WebEntryTwitterSearchSettingPage_button_text;
    public static String WebEntryTwitterSearchSettingPage_group_text;
    public static String WebEntryTwitterSearchSettingPage_button_text_1;
    public static String WebEntryTwitterSearchSettingPage_btnCheckButton_text_1;
    public static String WebEntryTwitterSearchSettingPage_button_text_2;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
