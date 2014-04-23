/**
 * @version $Id: WebEntryServiceSettingPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/19 19:42:17
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import twitter4j.TwitterException;

import com.tida_okinawa.corona.webentry.Messages;
import com.tida_okinawa.corona.webentry.twitter.TwitterConnection;
import com.tida_okinawa.corona.webentry.twitter.TwitterStatus;

/**
 * データベースに取り込むWebサービスを設定するウィザードページ
 * 
 * @author yukihiro-kinjo
 * 
 */
public class WebEntryServiceSettingPage extends WizardPageBase {

    private Label labelAccountSettingStatus;
    private Label labelLoginStatus;
    private Label labelApiStatus;
    private Combo comboWebService;
    private Button buttonDeleteAccount;


    protected WebEntryServiceSettingPage(String pageName) {
        super(pageName);
        setTitle(Messages.WebEntryServiceSettingPage_PageTitle);
        setMessage(Messages.WebEntryServiceSettingPage_PageMessage);
    }


    @Override
    public void createControl(Composite parent) {
        /* UI初期化 */
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new FormLayout());

        Label labelWebService = new Label(composite, SWT.NONE);
        FormData fd_labelWebService = new FormData();
        fd_labelWebService.top = new FormAttachment(0, 10);
        fd_labelWebService.left = new FormAttachment(0, 10);
        labelWebService.setLayoutData(fd_labelWebService);
        labelWebService.setText(Messages.WebEntryServiceSettingPage_label_WebService);

        comboWebService = new Combo(composite, SWT.READ_ONLY);
        FormData fd_comboWebService = new FormData();
        fd_comboWebService.top = new FormAttachment(labelWebService, -3, SWT.TOP);
        fd_comboWebService.right = new FormAttachment(labelWebService, 250, SWT.RIGHT);
        fd_comboWebService.left = new FormAttachment(labelWebService, 20);
        comboWebService.setLayoutData(fd_comboWebService);
        comboWebService.add(TwitterStatus.TWITTER);
        comboWebService.setText(TwitterStatus.TWITTER);

        Group groupAccountSetting = new Group(composite, SWT.NONE);
        groupAccountSetting.setText(Messages.WebEntryServiceSettingPage_group_SettingTwitterAccount);
        FormData fd_groupAccountSetting = new FormData();
        fd_groupAccountSetting.bottom = new FormAttachment(comboWebService, 270);
        fd_groupAccountSetting.right = new FormAttachment(100, -10);
        fd_groupAccountSetting.top = new FormAttachment(comboWebService, 15);
        fd_groupAccountSetting.left = new FormAttachment(0, 10);
        groupAccountSetting.setLayoutData(fd_groupAccountSetting);
        groupAccountSetting.setLayout(new FormLayout());

        Label labelAccountSetting = new Label(groupAccountSetting, SWT.NONE);
        FormData fd_labelAccountSetting = new FormData();
        fd_labelAccountSetting.top = new FormAttachment(0, 10);
        fd_labelAccountSetting.left = new FormAttachment(0, 10);
        labelAccountSetting.setLayoutData(fd_labelAccountSetting);
        labelAccountSetting.setText(Messages.WebEntryServiceSettingPage_label_AccountSettingStatus);

        labelAccountSettingStatus = new Label(groupAccountSetting, SWT.NONE);
        FormData fd_labelAccountSettingStatus = new FormData();
        fd_labelAccountSettingStatus.top = new FormAttachment(labelAccountSetting, 0, SWT.TOP);
        fd_labelAccountSettingStatus.left = new FormAttachment(labelAccountSetting, 6);
        fd_labelAccountSettingStatus.right = new FormAttachment(100, -5);
        labelAccountSettingStatus.setLayoutData(fd_labelAccountSettingStatus);

        Label labelLogin = new Label(groupAccountSetting, SWT.NONE);
        FormData fd_labelLogin = new FormData();
        fd_labelLogin.top = new FormAttachment(labelAccountSetting, 10);
        fd_labelLogin.left = new FormAttachment(0, 10);
        labelLogin.setLayoutData(fd_labelLogin);
        labelLogin.setText(Messages.WebEntryServiceSettingPage_label_LoginStatus);

        labelLoginStatus = new Label(groupAccountSetting, SWT.NONE);
        FormData fd_labelLoginStatus = new FormData();
        fd_labelLoginStatus.top = new FormAttachment(labelLogin, 0, SWT.TOP);
        fd_labelLoginStatus.left = new FormAttachment(labelAccountSetting, 6);
        fd_labelLoginStatus.right = new FormAttachment(100, -5);
        labelLoginStatus.setLayoutData(fd_labelLoginStatus);

        Label labelApi = new Label(groupAccountSetting, SWT.NONE);
        FormData fd_labelApi = new FormData();
        fd_labelApi.top = new FormAttachment(labelLogin, 10);
        fd_labelApi.left = new FormAttachment(0, 10);
        labelApi.setLayoutData(fd_labelApi);
        labelApi.setText(Messages.WebEntryServiceSettingPage_label_ApiRemit);

        labelApiStatus = new Label(groupAccountSetting, SWT.NONE);
        FormData fd_labelApiStatus = new FormData();
        fd_labelApiStatus.top = new FormAttachment(labelApi, 0, SWT.TOP);
        fd_labelApiStatus.left = new FormAttachment(labelAccountSetting, 6);
        fd_labelApiStatus.right = new FormAttachment(100, -5);
        labelApiStatus.setLayoutData(fd_labelApiStatus);

        Button buttonCreateTwitterAccount = new Button(groupAccountSetting, SWT.NONE);
        buttonCreateTwitterAccount.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* Twitterアカウントを新規作成するページを開く */
                if (!(TwitterConnection.openCreateTwitterAccountPage())) {
                    MessageDialog.openWarning(getShell(), Messages.WebEntryServiceSettingPage_Error, Messages.WebEntryServiceSettingPage_BrowserOpenFailed);
                }
            }
        });
        FormData fd_buttonCreateTwitterAccount = new FormData();
        fd_buttonCreateTwitterAccount.left = new FormAttachment(0, 10);
        fd_buttonCreateTwitterAccount.top = new FormAttachment(labelApi, 16);
        buttonCreateTwitterAccount.setLayoutData(fd_buttonCreateTwitterAccount);
        buttonCreateTwitterAccount.setText(Messages.WebEntryServiceSettingPage_button_CreateTwitterAccount);

        Button buttonLoginTwitter = new Button(groupAccountSetting, SWT.NONE);
        buttonLoginTwitter.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* Twitterで認証を行うためのダイアログを開く */
                TwitterConnection connection = new TwitterConnection();
                TwitterPinInputDialog pinDialog = new TwitterPinInputDialog(getShell(), connection);
                pinDialog.setBlockOnOpen(true);
                pinDialog.open();
                if (pinDialog.getReturnCode() == TwitterPinInputDialog.OK) {
                    /* ダイアログに入力されたPINで認証を完了させる */
                    if (!(connection.twitterFirstConnectPinInput(pinDialog.getPinValue()))) {
                        MessageDialog.openError(getShell(), Messages.WebEntryServiceSettingPage_AuthError,
                                Messages.WebEntryServiceSettingPage_NewSettingAuthFailed);
                    }
                }
                refreshTwitterAccountStatus();
            }
        });
        FormData fd_buttonLoginTwitter = new FormData();
        fd_buttonLoginTwitter.top = new FormAttachment(buttonCreateTwitterAccount, 6);
        fd_buttonLoginTwitter.left = new FormAttachment(labelLogin, 0, SWT.LEFT);
        buttonLoginTwitter.setLayoutData(fd_buttonLoginTwitter);
        buttonLoginTwitter.setText(Messages.WebEntryServiceSettingPage_button_LoginTwitter);

        buttonDeleteAccount = new Button(groupAccountSetting, SWT.NONE);
        buttonDeleteAccount.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox message = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                message.setText(Messages.WebEntryServiceSettingPage_WebServiceSetting);
                message.setMessage(Messages.WebEntryServiceSettingPage_QuestionDeleteTwitterAccountSetting);
                if (message.open() == SWT.NO) {
                    return;
                }
                TwitterConnection.deleteAccessToken();
                refreshTwitterAccountStatus();
            }
        });
        FormData fd_buttonDeleteAccount = new FormData();
        fd_buttonDeleteAccount.top = new FormAttachment(buttonLoginTwitter, 6);
        fd_buttonDeleteAccount.left = new FormAttachment(labelLogin, 0, SWT.LEFT);
        buttonDeleteAccount.setLayoutData(fd_buttonDeleteAccount);
        buttonDeleteAccount.setText(Messages.WebEntryServiceSettingPage_button_DeleteTwitterAccountSetting);

        refreshTwitterAccountStatus();
        setControl(composite);
    }


    /**
     * Twitterアカウントへの接続可否を取得しラベルを書き換える
     */
    public void refreshTwitterAccountStatus() {
        TwitterConnection connection = new TwitterConnection();
        if (!(connection.isSaveAccessToken())) {
            labelAccountSettingStatus.setText(TwitterStatus.NO_SETTING_ACCOUNT);
            labelLoginStatus.setText(""); //$NON-NLS-1$
            labelApiStatus.setText(""); //$NON-NLS-1$
            setMessage(Messages.WebEntryServiceSettingPage_PleaseTwitterAccountSettingComplete);
            setPageComplete(false);
            buttonDeleteAccount.setEnabled(false);
            return;
        }
        buttonDeleteAccount.setEnabled(true);
        if (connection.twitterSubsequentConnect()) {
            labelAccountSettingStatus.setText(TwitterStatus.SETTING_ACCOUNT);
            labelLoginStatus.setText(TwitterStatus.OK_LOGIN);
            try {
                labelApiStatus.setText(connection.getApiRateLimitStatusFormat());
            } catch (TwitterException e) {
                labelApiStatus.setText(Messages.WebEntryServiceSettingPage_ApiExecFailed);
                setErrorMessage(Messages.WebEntryServiceSettingPage_ApiAccessFailed);
                setPageComplete(false);
                return;
            }
            setMessage(Messages.WebEntryServiceSettingPage_WebServiceSelect);
            setPageComplete(true);
        } else {
            labelAccountSettingStatus.setText(TwitterStatus.SETTING_ACCOUNT);
            labelLoginStatus.setText(TwitterStatus.FAILED_LOGIN);
            labelApiStatus.setText(""); //$NON-NLS-1$
            setErrorMessage(Messages.WebEntryServiceSettingPage_TwitterConnectionFailedPleaseReSetting);
            setPageComplete(false);
        }
    }


    @Override
    public void setFocus() {
        if (comboWebService != null) {
            comboWebService.setFocus();
        }
    }
}
