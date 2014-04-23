/**
 * @version $Id: ImportTwitter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/31 9:21:38
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.ui.views.db.CreateClaimTableValidator;
import com.tida_okinawa.corona.webentry.TwitterQuerySettings;
import com.tida_okinawa.corona.webentry.WebEntryException;
import com.tida_okinawa.corona.webentry.WebEntryExternalControllerForTwitter;

/**
 * @author yukihiro-kinjo
 * 
 */
public class ImportTwitter extends Cleansing {

    /**
     * @param args
     *            前から順に、データベース接続URL、データベースユーザ名、データベースパスワード、
     *            データ登録名、ターゲット名、登録先プロジェクト名、検索クエリ
     */
    public static void main(String[] args) {
        try {
            ImportTwitter i = new ImportTwitter(args);
            i.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(Cleansing.ERROR_CODE_ANY_EXCEPTION);
        }
        System.exit(0);
    }


    /**
     * 自動実行用
     * 
     * @param args
     *            引数
     */
    private ImportTwitter(String[] args) {
        super(args);
    }

    /** 引数の数がおかしいときのエラーコード */
    public static final int ERROR_CODE_ARGS = 5;
    /** 問い合わせデータ名がおかしいときのエラーコード */
    public static final int ERROR_CODE_NAME = 7;
    /** 指定されたプロジェクトが存在しないときのエラーコード */
    public static final int ERROR_CODE_PROJECT_NON_EXISTS = 8;
    /** Twitterアクセストークンがないときのエラーコード */
    public static final int ERROR_CODE_NO_TWITTER_ACCESS_TOKEN = 10;
    /** 認証失敗 */
    public static final int ERROR_CODE_AUTHENTICATION_FAILED = 11;
    /** Twitter検索で何かしらのエラーが発生した */
    public static final int ERROR_CODE_ANY_TWITTER_ERROR = 12;
    /** 指定されたワークスペースが存在しない */
    public static final int ERROR_CODE_NO_WORKSPACE = 13;
    /** Twitterアクセストークン保管場所が存在しない */
    public static final int ERROR_CODE_NO_TEMPORARY_LOCATION = 14;


    private static final int URL = 0;
    private static final int USER = 1;
    private static final int PASS = 2;
    private static final int WORKSPACE_PATH = 3;
    private static final int DATANAME = 4;
    private static final int TARGET = 5;
    private static final int PROJECT = 6;
    private static final int QUERY = 7;
    private static final int OPTIONS = 8;

    private String stateLocation;
    private String dataName;
    private String targetName;
    private ICoronaProject project;
    private String query;

    private boolean isFilterRt;
    private boolean isFilterReply;
    private boolean isUseRtCount;
    private boolean isUseFollowers;
    private boolean isUseFollows;
    private boolean isUseProfile;
    private boolean isTcoExpand;
    private boolean isJpOnly;

    private WebEntryExternalControllerForTwitter twitterController;


    /**
     * 解析を実行する
     */
    void run() {
        TwitterQuerySettings setting = new TwitterQuerySettings(dataName, targetName, query);
        setting.setJapaneseOnly(isJpOnly);
        setting.setUseDateTime(true);
        setting.setUseName(true);
        setting.setUseScreenName(true);

        setting.setFilterReply(isFilterReply);
        setting.setFilterRt(isFilterRt);
        setting.setUseRtCount(isUseRtCount);
        setting.setUseFollowers(isUseFollowers);
        setting.setUseFollows(isUseFollows);
        setting.setUseProfile(isUseProfile);
        setting.setTcoExpand(isTcoExpand);

        boolean result = twitterController.searchTwitterAndImportDataBase(setting, null);
        if (!result) {
            WebEntryException e = twitterController.getLastException();
            if (e != null) {
                e.printStackTrace();
                errorExit(ERROR_CODE_ANY_TWITTER_ERROR, e.getLocalizedMessage());
            }
            errorExit(ERROR_CODE_ANY_TWITTER_ERROR, getErrorMessage(ERROR_CODE_ANY_TWITTER_ERROR));
            return;
        }
        IClaimData claim = twitterController.getClaimData();
        List<IFieldHeader> miningFields = new ArrayList<IFieldHeader>(1);
        for (Integer id : claim.getCorrectionMistakesFields()) {
            miningFields.add(claim.getFieldInformation(id));
        }
        AutoCleansingUtil.registerProject(project, claim, miningFields);
    }


    @Override
    int check(String[] args) {
        if ((args.length != 8) && (args.length != 9)) {
            return ERROR_CODE_ARGS;
        }
        service = createService(args[URL], args[USER], args[PASS]);
        if (service == null) {
            return ERROR_CODE_NO_DATABASE_CONNECTION;
        }

        File check = new File(args[WORKSPACE_PATH]);
        if (!check.exists()) {
            return ERROR_CODE_NO_WORKSPACE;
        }
        stateLocation = check.getAbsolutePath() + "\\.metadata\\.plugins\\org.eclipse.core.resources"; //$NON-NLS-1$
        if (!new File(stateLocation).exists()) {
            return ERROR_CODE_NO_TEMPORARY_LOCATION;
        }

        dataName = args[DATANAME];
        CreateClaimTableValidator validator = new CreateClaimTableValidator();
        if (validator.isValid(dataName) != null) {
            return ERROR_CODE_NAME;
        } else if (validator.exists(dataName)) {
            return ERROR_CODE_NAME;
        }

        targetName = args[TARGET];

        project = searchProject(args[PROJECT]);
        if (project == null) {
            return ERROR_CODE_PROJECT_NON_EXISTS;
        }
        query = args[QUERY];

        twitterController = new WebEntryExternalControllerForTwitter(stateLocation);
        if (!twitterController.isSaveAccessToken()) {
            return ERROR_CODE_NO_TWITTER_ACCESS_TOKEN;
        }
        if (!twitterController.twitterSubsequentConnect()) {
            return ERROR_CODE_AUTHENTICATION_FAILED;
        }
        isFilterRt = false;
        isFilterReply = false;
        isUseRtCount = false;
        isUseFollowers = false;
        isUseFollows = false;
        isUseProfile = false;
        isTcoExpand = false;
        isJpOnly = true;
        if (args.length == 9) {
            initSearchOptions(args[OPTIONS]);
        }
        return CODE_OK;
    }


    private void initSearchOptions(String options) {
        String[] optionArray = options.split(";");
        for (String option : optionArray) {
            option = option.trim();
            if (option.equalsIgnoreCase("FilterRt")) {
                isFilterRt = true;
            } else if (option.equalsIgnoreCase("FilterReply")) {
                isFilterReply = true;
            } else if (option.equalsIgnoreCase("UseRtCount")) {
                isUseRtCount = true;
            } else if (option.equalsIgnoreCase("UseFollowers")) {
                isUseFollowers = true;
            } else if (option.equalsIgnoreCase("UseFollows")) {
                isUseFollows = true;
            } else if (option.equalsIgnoreCase("UseProfile")) {
                isUseProfile = true;
            } else if (option.equalsIgnoreCase("TcoExpand")) {
                isTcoExpand = true;
            } else if (option.equalsIgnoreCase("Global")) {
                isJpOnly = false;
            }
        }
    }


    @Override
    String getErrorMessage(int errorCode) {
        switch (errorCode) {
        case ERROR_CODE_ARGS:
            return Messages
                    .bind(Messages.ErrorMessage_ArgsNum, new Object[] { Messages.Name_TwitterImport, 8, Messages.ImportTwitter_ErrorMessage_ArgsDetail });
        case ERROR_CODE_NAME:
            CreateClaimTableValidator validator = new CreateClaimTableValidator();
            if (validator.isValid(dataName) != null) {
                return validator.isValid(dataName);
            } else if (validator.exists(dataName)) {
                return Messages.bind(Messages.Import_ErrorMessage_ExistsDataName, dataName);
            }
            return null;
        case ERROR_CODE_NO_TWITTER_ACCESS_TOKEN:
            return Messages.ImportTwitter_ErrorMessage_TryManually;
        case ERROR_CODE_AUTHENTICATION_FAILED:
            return Messages.ImportTwitter_ErrorMessage_Failed_Auth;
        case ERROR_CODE_ANY_TWITTER_ERROR:
            return Messages.ImportTwitter_ErrorMessage_UnExpectedErrorOccurred;
        case ERROR_CODE_NO_WORKSPACE:
            return Messages.ImportTwitter_ErrorMessage_NoWorkspace;
        case ERROR_CODE_NO_TEMPORARY_LOCATION:
            return Messages.bind(Messages.Import_ErrorMessage_NoExistsFolder, stateLocation);
        case ERROR_CODE_PROJECT_NON_EXISTS:
            return Messages.Cleansing2_NoProject;
        default:
            return super.getErrorMessage(errorCode);
        }
    }
}
