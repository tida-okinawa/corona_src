/**
 * @version $Id: WebEntryTwitterSearchSettingPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/19 19:55:16
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry.ui;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.webentry.Messages;
import com.tida_okinawa.corona.webentry.TwitterQuerySettings;
import com.tida_okinawa.corona.webentry.util.CreateClaimTableValidator;

/**
 * Twitter検索の設定を行いデータベースへ取得するウィザードページ
 * 
 * @author yukihiro-kinjo
 * 
 */
public class WebEntryTwitterSearchSettingPage extends WizardPageBase {

    private String sectionName;
    private IClaimData claimData;

    private Text textDataName;
    private Text textProductName;
    private Combo comboSearchWord;
    private Spinner spinnerSearchMax;
    private Button buttonUserScreenName;
    private Button buttonUserName;
    private Button buttonBody;
    private Button buttonPostDateTime;
    private Button buttonJapaneseOnly;

    private Button buttonRetweet;
    private Button buttonFollow;
    private Button buttonFollower;
    private Button buttonProfile;
    private Button buttonTcoExpand;
    private Button buttonFilterdRetweet;
    private Button buttonFilterdReply;

    private static final String STORE_KEY_SEARCH_WORD = "STORE_KEY_SEARCH_WORD"; //$NON-NLS-1$
    private static final String STORE_KEY_SEARCH_MAX = "STORE_KEY_SEARCH_MAX"; //$NON-NLS-1$
    private static final String STORE_KEY_USE_SCREEN_NAME = "STORE_KEY_USE_SCREEN_NAME"; //$NON-NLS-1$
    private static final String STORE_KEY_USE_USER_NAME = "STORE_KEY_USE_USER_NAME"; //$NON-NLS-1$
    private static final String STORE_KEY_USE_POST_DATE_TIME = "STORE_KEY_USE_POST_DATE_TIME"; //$NON-NLS-1$
    private static final String STORE_KEY_JAPANESE_ONLY = "STORE_KEY_JAPANESE_ONLY"; //$NON-NLS-1$
    private static final String STORE_KEY_NO_DEFAULT_SETUP = "STORE_KEY_DEFAULT_SETUP"; //$NON-NLS-1$


    //    private static final String STORE_KEY_TCO_EXPAND = "STORE_KEY_TCO_EXPAND";
    //    private static final String STORE_KEY_USE_RETWEET = "STORE_KEY_USE_RETWEET";
    //    private static final String STORE_KEY_USE_FOLLOW = "STORE_KEY_USE_FOLLOW";
    //    private static final String STORE_KEY_USE_FOLLOWER = "STORE_KEY_USE_FOLLOWER";
    //    private static final String STORE_KEY_USE_PROFILE = "STORE_KEY_USE_PROFILE";
    //    private static final String STORE_KEY_FILTER_RETWEET = "STORE_KEY_FILTER_RETWEET";


    protected WebEntryTwitterSearchSettingPage(String pageName) {
        super(pageName);
        this.sectionName = pageName;
        setTitle(Messages.WebEntryTwitterSearchSettingPage_PageTitle);
        setMessage(Messages.WebEntryTwitterSearchSettingPage_PageMessage);
        this.claimData = null;
    }


    @Override
    public void createControl(Composite parent) {

        super.createControl(parent);
        /* UI初期化 */
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new FormLayout());

        Label labelDataName = new Label(composite, SWT.NONE);
        FormData fd_labelDataName = new FormData();
        fd_labelDataName.left = new FormAttachment(0, 10);
        labelDataName.setLayoutData(fd_labelDataName);
        labelDataName.setText(Messages.WebEntryTwitterSearchSettingPage_label_DataName);

        textDataName = new Text(composite, SWT.BORDER);
        fd_labelDataName.top = new FormAttachment(textDataName, 3, SWT.TOP);
        FormData fd_textDataName = new FormData();
        fd_textDataName.left = new FormAttachment(labelDataName, 20);
        fd_textDataName.top = new FormAttachment(0, 7);
        textDataName.setLayoutData(fd_textDataName);
        textDataName.setTextLimit(30);
        textDataName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateInputField();
            }
        });

        Button buttonNowTimeInput = new Button(composite, SWT.NONE);
        fd_textDataName.right = new FormAttachment(buttonNowTimeInput, -6);
        FormData fd_buttonNowTimeInput = new FormData();
        fd_buttonNowTimeInput.top = new FormAttachment(0, 5);
        fd_buttonNowTimeInput.right = new FormAttachment(100, -10);
        buttonNowTimeInput.setLayoutData(fd_buttonNowTimeInput);
        buttonNowTimeInput.setText(Messages.WebEntryTwitterSearchSettingPage_button_NowDateTimeInput);
        final Text finalText = textDataName;
        buttonNowTimeInput.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* 問い合わせデータ名テキストボックスに日付を付与する */
                final String dateFormatString = "_yyyyMMdd_HHmm"; //$NON-NLS-1$
                final String dateFormatRegEx = "_\\d{8}_\\d{4}"; //$NON-NLS-1$
                SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
                String tempDataName = finalText.getText().trim();
                tempDataName = tempDataName.replaceAll(dateFormatRegEx, ""); //$NON-NLS-1$
                tempDataName = tempDataName + dateFormat.format(Calendar.getInstance().getTime());
                if (tempDataName.length() > 30) {
                    tempDataName = tempDataName.substring(0, 30);
                }
                finalText.setText(tempDataName);
            }
        });

        Label labelProductName = new Label(composite, SWT.NONE);
        FormData fd_labelProductName = new FormData();
        fd_labelProductName.left = new FormAttachment(0, 10);
        labelProductName.setLayoutData(fd_labelProductName);
        labelProductName.setText(Messages.WebEntryTwitterSearchSettingPage_label_ProductName);

        textProductName = new Text(composite, SWT.BORDER);
        fd_labelProductName.top = new FormAttachment(textProductName, 3, SWT.TOP);
        FormData fd_textProductName = new FormData();
        fd_textProductName.right = new FormAttachment(textDataName, 0, SWT.RIGHT);
        fd_textProductName.top = new FormAttachment(textDataName, 6);
        fd_textProductName.left = new FormAttachment(textDataName, 0, SWT.LEFT);
        textProductName.setLayoutData(fd_textProductName);
        textProductName.setTextLimit(128);
        textProductName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateInputField();
            }
        });

        Label labelSearchWord = new Label(composite, SWT.NONE);
        FormData fd_labelSearchWord = new FormData();
        fd_labelSearchWord.left = new FormAttachment(0, 10);
        labelSearchWord.setLayoutData(fd_labelSearchWord);
        labelSearchWord.setText(Messages.WebEntryTwitterSearchSettingPage_label_Query);

        comboSearchWord = new Combo(composite, SWT.BORDER);
        fd_labelSearchWord.top = new FormAttachment(comboSearchWord, 3, SWT.TOP);
        FormData fd_comboSearchWord = new FormData();
        fd_comboSearchWord.right = new FormAttachment(textDataName, 0, SWT.RIGHT);
        fd_comboSearchWord.top = new FormAttachment(textProductName, 6);
        fd_comboSearchWord.left = new FormAttachment(textDataName, 0, SWT.LEFT);
        comboSearchWord.setLayoutData(fd_comboSearchWord);
        comboSearchWord.setTextLimit(256);
        comboSearchWord.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateInputField();
            }
        });

        spinnerSearchMax = new Spinner(composite, SWT.BORDER);
        spinnerSearchMax.setPageIncrement(1000);
        spinnerSearchMax.setMinimum(1);
        spinnerSearchMax.setMaximum(TwitterQuerySettings.TWITTER_SEARCH_MAX);
        spinnerSearchMax.setSelection(10000);
        FormData fd_spinnerSearchMax = new FormData();
        fd_spinnerSearchMax.top = new FormAttachment(comboSearchWord, 6);
        fd_spinnerSearchMax.left = new FormAttachment(textDataName, 0, SWT.LEFT);
        spinnerSearchMax.setLayoutData(fd_spinnerSearchMax);

        Label labelGetCount = new Label(composite, SWT.NONE);
        FormData fd_labelGetCount = new FormData();
        fd_labelGetCount.left = new FormAttachment(labelDataName, 0, SWT.LEFT);
        labelGetCount.setLayoutData(fd_labelGetCount);
        labelGetCount.setText(Messages.WebEntryTwitterSearchSettingPage_label_GetCount);

        buttonJapaneseOnly = new Button(composite, SWT.CHECK);
        FormData fd_buttonJapaneseOnly = new FormData();
        fd_buttonJapaneseOnly.top = new FormAttachment(spinnerSearchMax, 6);
        fd_buttonJapaneseOnly.left = new FormAttachment(textDataName, 0, SWT.LEFT);
        buttonJapaneseOnly.setLayoutData(fd_buttonJapaneseOnly);
        buttonJapaneseOnly.setText(Messages.WebEntryTwitterSearchSettingPage_button_JapaneseOnly);

        Group groupGetItemSetting = new Group(composite, SWT.NONE);
        fd_labelGetCount.bottom = new FormAttachment(groupGetItemSetting, -40);
        groupGetItemSetting.setText(Messages.WebEntryTwitterSearchSettingPage_group_GetItem);
        groupGetItemSetting.setLayout(new FormLayout());
        FormData fd_groupGetItemSetting = new FormData();
        fd_groupGetItemSetting.bottom = new FormAttachment(100, -10);
        fd_groupGetItemSetting.top = new FormAttachment(buttonJapaneseOnly, 16);
        fd_groupGetItemSetting.left = new FormAttachment(0, 10);
        fd_groupGetItemSetting.right = new FormAttachment(0, 274);
        groupGetItemSetting.setLayoutData(fd_groupGetItemSetting);

        buttonUserScreenName = new Button(groupGetItemSetting, SWT.CHECK);
        buttonUserScreenName.setSelection(true);
        FormData fd_buttonUserScreenName = new FormData();
        fd_buttonUserScreenName.top = new FormAttachment(0, 10);
        fd_buttonUserScreenName.left = new FormAttachment(0, 10);
        buttonUserScreenName.setLayoutData(fd_buttonUserScreenName);
        buttonUserScreenName.setText(Messages.WebEntryTwitterSearchSettingPage_button_ScreenName);

        buttonUserName = new Button(groupGetItemSetting, SWT.CHECK);
        buttonUserName.setSelection(true);
        FormData fd_buttonUserName = new FormData();
        fd_buttonUserName.top = new FormAttachment(buttonUserScreenName, 0, SWT.TOP);
        fd_buttonUserName.left = new FormAttachment(buttonUserScreenName, 40);
        buttonUserName.setLayoutData(fd_buttonUserName);
        buttonUserName.setText(Messages.WebEntryTwitterSearchSettingPage_button_UserName);

        buttonBody = new Button(groupGetItemSetting, SWT.CHECK);
        buttonBody.setEnabled(false);
        buttonBody.setSelection(true);
        FormData fd_buttonBody = new FormData();
        fd_buttonBody.top = new FormAttachment(buttonUserScreenName, 6);
        fd_buttonBody.left = new FormAttachment(buttonUserScreenName, 0, SWT.LEFT);
        buttonBody.setLayoutData(fd_buttonBody);
        buttonBody.setText(Messages.WebEntryTwitterSearchSettingPage_button_Body);

        buttonPostDateTime = new Button(groupGetItemSetting, SWT.CHECK);
        buttonPostDateTime.setSelection(true);
        FormData fd_buttonPostDateTime = new FormData();
        fd_buttonPostDateTime.top = new FormAttachment(buttonUserName, 6);
        fd_buttonPostDateTime.left = new FormAttachment(buttonUserName, 0, SWT.LEFT);
        buttonPostDateTime.setLayoutData(fd_buttonPostDateTime);
        buttonPostDateTime.setText(Messages.WebEntryTwitterSearchSettingPage_button_PostDateTime);

        buttonFollow = new Button(groupGetItemSetting, SWT.CHECK);
        FormData fd_buttonFollow = new FormData();
        fd_buttonFollow.left = new FormAttachment(buttonUserScreenName, 0, SWT.LEFT);
        buttonFollow.setLayoutData(fd_buttonFollow);
        buttonFollow.setText(Messages.WebEntryTwitterSearchSettingPage_button_Follow);

        buttonFollower = new Button(groupGetItemSetting, SWT.CHECK);
        FormData fd_buttonFollower = new FormData();
        fd_buttonFollower.top = new FormAttachment(buttonPostDateTime, 6);
        fd_buttonFollower.left = new FormAttachment(buttonUserName, 0, SWT.LEFT);
        buttonFollower.setLayoutData(fd_buttonFollower);
        buttonFollower.setText(Messages.WebEntryTwitterSearchSettingPage_button_Follower);

        buttonProfile = new Button(groupGetItemSetting, SWT.CHECK);
        FormData fd_buttonProfile = new FormData();
        fd_buttonProfile.top = new FormAttachment(buttonFollow, 0, SWT.TOP);
        fd_buttonProfile.left = new FormAttachment(buttonUserName, 0, SWT.LEFT);
        buttonProfile.setLayoutData(fd_buttonProfile);
        buttonProfile.setText(Messages.WebEntryTwitterSearchSettingPage_button_Profile);

        buttonRetweet = new Button(groupGetItemSetting, SWT.CHECK);
        fd_buttonFollow.top = new FormAttachment(buttonRetweet, 6);
        FormData fd_buttonRetweet = new FormData();
        fd_buttonRetweet.top = new FormAttachment(buttonBody, 6);
        fd_buttonRetweet.left = new FormAttachment(buttonUserScreenName, 0, SWT.LEFT);
        buttonRetweet.setLayoutData(fd_buttonRetweet);
        buttonRetweet.setText(Messages.WebEntryTwitterSearchSettingPage_button_text);

        textDataName.setFocus();
        restoreInputField();
        validateInputField();
        setControl(composite);

        buttonTcoExpand = new Button(composite, SWT.CHECK);
        FormData fd_buttonTcoExpand = new FormData();
        fd_buttonTcoExpand.bottom = new FormAttachment(buttonJapaneseOnly, 0, SWT.BOTTOM);
        fd_buttonTcoExpand.left = new FormAttachment(buttonJapaneseOnly, 6);
        buttonTcoExpand.setLayoutData(fd_buttonTcoExpand);
        buttonTcoExpand.setText(Messages.WebEntryTwitterSearchSettingPage_button_TcoExpand);

        Group groupFilter = new Group(composite, SWT.NONE);
        groupFilter.setText(Messages.WebEntryTwitterSearchSettingPage_group_text);
        FormData fd_groupFilter = new FormData();
        fd_groupFilter.right = new FormAttachment(textDataName, 0, SWT.RIGHT);
        fd_groupFilter.bottom = new FormAttachment(groupGetItemSetting, 0, SWT.BOTTOM);
        fd_groupFilter.top = new FormAttachment(buttonJapaneseOnly, 16);
        fd_groupFilter.left = new FormAttachment(groupGetItemSetting, 18);
        groupFilter.setLayoutData(fd_groupFilter);

        buttonFilterdRetweet = new Button(groupFilter, SWT.CHECK);
        buttonFilterdRetweet.setBounds(10, 28, 137, 18);
        buttonFilterdRetweet.setText(Messages.WebEntryTwitterSearchSettingPage_button_text_1);

        buttonFilterdReply = new Button(groupFilter, SWT.CHECK);
        buttonFilterdReply.setBounds(10, 52, 102, 18);
        buttonFilterdReply.setText(Messages.WebEntryTwitterSearchSettingPage_button_text_2);
    }


    @Override
    public void setFocus() {
        if (textDataName != null) {
            textDataName.setFocus();
        }
    }


    /**
     * データベース取り込み処理の起動。<br />
     * 処理に問題が発生した場合内容をダイアログで表示したうえで
     * 戻り値としてnullを返却する
     * 
     * @return 取り込んだ問い合わせデータのハンドル。問題が発生した場合nullを返却
     */
    public IClaimData finished() {
        /* 終了処理用入力データの用意 */
        final String searchWord = comboSearchWord.getText();
        final int searchMax = spinnerSearchMax.getSelection();
        final boolean japaneseOnly = buttonJapaneseOnly.getSelection();
        final String dataName = textDataName.getText();
        final String productName = textProductName.getText();
        final boolean useScreenName = buttonUserScreenName.getSelection();
        final boolean useName = buttonUserName.getSelection();
        final boolean useDateTime = buttonPostDateTime.getSelection();

        final boolean useRetweet = buttonRetweet.getSelection();
        final boolean useFollow = buttonFollow.getSelection();
        final boolean useFollower = buttonFollower.getSelection();
        final boolean useProfile = buttonProfile.getSelection();
        final boolean tcoExpand = buttonTcoExpand.getSelection();
        final boolean filterRetweet = buttonFilterdRetweet.getSelection();
        final boolean filterReply = buttonFilterdReply.getSelection();

        /* 終了処理の実行 */
        TwitterFinishProgress finishProgress = new TwitterFinishProgress(dataName, useName, searchMax, productName, japaneseOnly, useDateTime, useScreenName,
                searchWord, getShell(), useRetweet, useFollow, useFollower, useProfile, tcoExpand, filterRetweet, filterReply);
        IWizardContainer container = getContainer();
        try {
            container.run(true, true, finishProgress);
            claimData = finishProgress.getClaimData();
        } catch (InvocationTargetException | InterruptedException e) {
            /* 何もしない */
        }

        /* 入力項目を保存 */
        sotoreInputField();

        return claimData;
    }


    /**
     * 入力内容チェック
     */
    void validateInputField() {
        /* Table名チェック */
        /* 重複 */
        CreateClaimTableValidator tableValidator = new CreateClaimTableValidator();
        if (tableValidator.exists(textDataName.getText())) {
            setPageComplete(false);
            setErrorMessage(Messages.WebEntryTwitterSearchSettingPage_DataNameExists);
            return;
        }
        /* 正しい形式の名前か */
        String warning = tableValidator.isValid(textDataName.getText());
        if (textDataName.getText().trim().length() == 0 || (warning != null)) {
            /* textDataNameに入力なし(length==0) */
            setPageComplete(false);
            setErrorMessage(warning);
            return;
        }
        /* ターゲット名チェック */
        if (textProductName.getText().isEmpty()) {
            setPageComplete(false);
            setErrorMessage(Messages.WebEntryTwitterSearchSettingPage_PleaseInputProductName);
            return;
        }
        if (textProductName.getText().length() > 128) {
            setPageComplete(false);
            setErrorMessage(Messages.WebEntryTwitterSearchSettingPage_ProductNameLengthError);
            return;
        }
        if (textProductName.getText().indexOf(",") != -1) { //$NON-NLS-1$
            setPageComplete(false);
            setErrorMessage(Messages.WebEntryTwitterSearchSettingPage_ProductNameCharacterError);
            return;
        }
        /* 検索ワードチェック */
        if (comboSearchWord.getText().isEmpty()) {
            setPageComplete(false);
            setErrorMessage(Messages.WebEntryTwitterSearchSettingPage_PleaseInputQuery);
            return;
        }
        if (comboSearchWord.getText().length() > 256) {
            setPageComplete(false);
            setErrorMessage(Messages.WebEntryTwitterSearchSettingPage_QueryLengthError);
            return;
        }
        setPageComplete(true);
        setErrorMessage(null);
    }


    /**
     * 入力内容を保存する
     */
    private void sotoreInputField() {
        section = getSection(sectionName, true);
        saveCombo(comboSearchWord.getText(), STORE_KEY_SEARCH_WORD);
        section.put(STORE_KEY_SEARCH_MAX, spinnerSearchMax.getSelection());
        section.put(STORE_KEY_JAPANESE_ONLY, buttonJapaneseOnly.getSelection());
        section.put(STORE_KEY_USE_SCREEN_NAME, buttonUserScreenName.getSelection());
        section.put(STORE_KEY_USE_USER_NAME, buttonUserName.getSelection());
        section.put(STORE_KEY_USE_POST_DATE_TIME, buttonPostDateTime.getSelection());
        section.put(STORE_KEY_NO_DEFAULT_SETUP, true);

        //        section.put(STORE_KEY_TCO_EXPAND, buttonTcoExpand.getSelection());
        //        section.put(STORE_KEY_USE_RETWEET, buttonRetweet.getSelection());
        //        section.put(STORE_KEY_USE_FOLLOW, buttonFollow.getSelection());
        //        section.put(STORE_KEY_USE_FOLLOWER, buttonFollower.getSelection());
        //        section.put(STORE_KEY_USE_PROFILE, buttonProfile.getSelection());
        //        section.put(STORE_KEY_FILTER_RETWEET, buttonFilterdRetweet.getSelection());
    }


    /**
     * 入力内容を復元する
     */
    private void restoreInputField() {
        section = getSection(sectionName, false);
        if (section != null) {
            if (section.getArray(STORE_KEY_SEARCH_WORD) != null) {
                comboSearchWord.setItems(section.getArray(STORE_KEY_SEARCH_WORD));
                if (section.getBoolean(STORE_KEY_NO_DEFAULT_SETUP)) {
                    try {
                        spinnerSearchMax.setSelection(section.getInt(STORE_KEY_SEARCH_MAX));
                        buttonJapaneseOnly.setSelection(section.getBoolean(STORE_KEY_JAPANESE_ONLY));
                        buttonUserScreenName.setSelection(section.getBoolean(STORE_KEY_USE_SCREEN_NAME));
                        buttonUserName.setSelection(section.getBoolean(STORE_KEY_USE_USER_NAME));
                        buttonPostDateTime.setSelection(section.getBoolean(STORE_KEY_USE_POST_DATE_TIME));

                        //                        buttonTcoExpand.setSelection(section.getBoolean(STORE_KEY_TCO_EXPAND));
                        //                        buttonRetweet.setSelection(section.getBoolean(STORE_KEY_USE_RETWEET));
                        //                        buttonFollow.setSelection(section.getBoolean(STORE_KEY_USE_FOLLOW));
                        //                        buttonFollower.setSelection(section.getBoolean(STORE_KEY_USE_FOLLOWER));
                        //                        buttonProfile.setSelection(section.getBoolean(STORE_KEY_USE_PROFILE));
                        //                        buttonFilterdRetweet.setSelection(section.getBoolean(STORE_KEY_FILTER_RETWEET));
                    } catch (NumberFormatException e) {
                        /* 何もしない */
                    }
                }
            }
        }
    }
}
