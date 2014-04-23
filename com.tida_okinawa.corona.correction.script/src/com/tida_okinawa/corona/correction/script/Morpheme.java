/**
 * @version $Id: Morpheme.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/02 10:04:24
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import java.io.File;

import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.ui.controllers.MorphemeController;
import com.tida_okinawa.corona.ui.data.ErratumCorrectionRecord;
import com.tida_okinawa.corona.ui.data.MorphemeRecord;

/**
 * 形態素解析をコンソールから行うためのクラス
 * 
 * @author kousuke-morishima
 */
public class Morpheme extends Cleansing2<ErratumCorrectionRecord, MorphemeRecord> {

    /**
     * @param args
     *            前から順に、データベース接続URL、データベースユーザ名、データベースパスワード、対象プロジェクト名
     *            対象ターゲット名、KNPを実行するか(true/false)、ExternalのバンドルID
     */
    public static void main(String[] args) {
        try {
            Morpheme m = new Morpheme(args);
            m.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(Cleansing.ERROR_CODE_ANY_EXCEPTION);
        }
        System.exit(0);
    }


    /**
     * @param args
     *            前から順に、データベース接続URL、データベースユーザ名、データベースパスワード、対象プロジェクト名
     *            対象ターゲット名、KNPを実行するか(true/false)、ExternalのバンドルID
     */
    public Morpheme(String[] args) {
        super(args);
    }

    /** 引数の数がおかしいときのエラーコード */
    public static final int ERROR_CODE_ARGS = 7;
    /** 指定されたKNP実行フラグの値がおかしいときのエラーコード */
    public static final int ERROR_CODE_INVALID_BOOL = 8;
    /** 指定されたバンドルIDがおかしいときのエラーコード */
    public static final int ERROR_CODE_INVALID_BUNDLE = 9;
    /** 指定されたパスにjuman.exeがないときのエラーコード */
    public static final int ERROR_CODE_INVALID_JUMAN_LOCATION = 10;
    /** 指定されたパスにknp.exeがないときのエラーコード */
    public static final int ERROR_CODE_INVALID_KNP_LOCATION = 11;
    /** インストールフォルダーがないときのエラーコード */
    public static final int ERROR_CODE_NO_INSTALL_FOLDER = 12;

    private static final int URL = 0;
    private static final int USER = 1;
    private static final int PASS = 2;
    private static final int PROJECT = 3;
    private static final int TARGET = 4;
    private static final int KNP = 5;
    private static final int INSTALL = 6;
    private static final int BUNDLE_ID = 7;
    private ICoronaProject project;
    private ICoronaProduct target;
    private int bundleId;
    private String bundleLocation;
    private boolean doKnp;
    private boolean convertSJIS;


    @Override
    int check(String[] args) {
        if (args.length != 8) {
            return ERROR_CODE_ARGS;
        }

        /* DB接続のテスト */
        service = createService(args[URL], args[USER], args[PASS]);
        if (service == null) {
            return ERROR_CODE_NO_DATABASE_CONNECTION;
        }

        /* プロジェクトチェック */
        project = searchProject(args[PROJECT]);
        if (project == null) {
            return ERROR_CODE_NO_PROJECT;
        }

        /* ターゲットチェック */
        target = searchProduct(project, args[TARGET]);
        if (target == null) {
            return ERROR_CODE_NO_TARGET;
        }

        /* KNP実行フラグの確認 */
        String boolValue = args[KNP];
        if ("true".equalsIgnoreCase(boolValue)) { //$NON-NLS-1$
            doKnp = true;
        } else if ("false".equalsIgnoreCase(boolValue)) { //$NON-NLS-1$
            doKnp = false;
        } else {
            return ERROR_CODE_INVALID_BOOL;
        }

        /* バンドルIDの確認 */
        try {
            bundleId = Integer.parseInt(args[BUNDLE_ID]);
        } catch (NumberFormatException e) {
            return ERROR_CODE_INVALID_BUNDLE;
        }
        /* インストール場所の確認 */
        File installPath = new File(args[INSTALL]);
        if (!installPath.exists()) {
            return ERROR_CODE_NO_INSTALL_FOLDER;
        }
        this.bundleLocation = installPath.getAbsolutePath() + "\\configuration\\org.eclipse.osgi\\bundles\\" + bundleId + "\\1\\.cp\\"; //$NON-NLS-1$ //$NON-NLS-2$

        if (!new File(bundleLocation + "juman7\\juman.exe").exists()) { //$NON-NLS-1$
            return ERROR_CODE_INVALID_JUMAN_LOCATION;
        }
        if (!new File(bundleLocation + "knp4\\knp.exe").exists()) { //$NON-NLS-1$
            return ERROR_CODE_INVALID_KNP_LOCATION;
        }
        convertSJIS = false;

        return CODE_OK;
    }


    @Override
    String getErrorMessage(int errorCode) {
        switch (errorCode) {
        case ERROR_CODE_ARGS:
            return Messages.bind(Messages.ErrorMessage_ArgsNum, new Object[] { Messages.NameMorpheme, 8, Messages.Morpheme_ErrorMessage_ArgsDetail });
        case ERROR_CODE_INVALID_BOOL:
            return Messages.Morpheme_ErrorMessage_InvalidBoolean;
        case ERROR_CODE_INVALID_BUNDLE:
            return Messages.Morpheme_ErrorMessage_InvalidBundleId;
        case ERROR_CODE_INVALID_JUMAN_LOCATION:
            return Messages.bind(Messages.Morpheme_ErrorMessage_NoJumanExe, bundleLocation);
        case ERROR_CODE_INVALID_KNP_LOCATION:
            return Messages.bind(Messages.Morpheme_ErrorMessage_NoKnpExe, bundleLocation);
        case ERROR_CODE_NO_INSTALL_FOLDER:
            return Messages.Morpheme_ErrorMessage_NoInstallFolder;
        default:
            return super.getErrorMessage(errorCode);
        }
    }


    void run() {
        MorphemeController controller = new MorphemeController(target, doKnp, bundleLocation, convertSJIS, null);
        run(controller);
    }

}
