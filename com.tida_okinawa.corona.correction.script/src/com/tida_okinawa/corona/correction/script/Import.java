/**
 * @version $Id: Import.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/31 9:21:38
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.ui.views.db.CreateClaimTableValidator;

/**
 * @author kousuke-morishima
 * 
 */
public class Import extends Cleansing {
    // TODO FindBugs

    /**
     * @param args
     *            前から順に、データベース接続URL、データベースユーザ名、データベースパスワード、
     *            データフォルダ、データ登録名、ID列番号、ターゲット列番号、登録するデータの先頭行がデータかどうか（
     *            列名ならfalseを指定する）
     *            、 登録先プロジェクト名
     */
    public static void main(String[] args) {
        try {
            Import i = new Import(args);
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
    private Import(String[] args) {
        super(args);
    }


    /** 引数の数がおかしいときのエラーコード */
    public static final int ERROR_CODE_ARGS = 5;
    /** 指定されたフォルダがないときのエラーコード */
    public static final int ERROR_CODE_NO_FOLDER = 6;
    /** 問い合わせデータ名がおかしいときのエラーコード */
    public static final int ERROR_CODE_NAME = 7;
    /** ID列、ターゲット列の値が数値ではないときのエラーコード */
    public static final int ERROR_CODE_INVALID_COLUMN = 8;
    /** 先頭行がデータかどうかの値がboolではないときのエラーコード */
    public static final int ERROR_CODE_INVALID_BOOL = 9;
    /** 登録する問い合わせデータファイル(csv)がないときのエラーコード */
    public static final int ERROR_CODE_NO_DATAFILE = 10;
    /** 定義ファイル(sql)がないときのエラーコード */
    public static final int ERROR_CODE_NO_SQLFILE = 11;


    private static final int URL = 0;
    private static final int USER = 1;
    private static final int PASS = 2;
    private static final int FOLDER = 3;
    private static final int NAME = 4;
    private static final int ID = 5;
    private static final int TARGET = 6;
    private static final int HEAD = 7;

    private File dir;
    private String name;
    private int idColumn;
    private int targetColumn;
    private boolean headIsData;


    /**
     * 解析を実行する
     */
    public void run() {
        File dataFile = null;
        File defineFile = null;
        long lastModify = 0;
        File[] files = dir.listFiles();
        for (File f : files) {
            String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1); //$NON-NLS-1$
            if ("csv".equalsIgnoreCase(ext)) { //$NON-NLS-1$
                if (lastModify < f.lastModified()) {
                    dataFile = f;
                    lastModify = f.lastModified();
                }
            } else if ("sql".equalsIgnoreCase(ext)) { //$NON-NLS-1$
                defineFile = f;
            }
        }
        if (dataFile == null) {
            errorExit(ERROR_CODE_NO_DATAFILE, getErrorMessage(ERROR_CODE_NO_DATAFILE));
        }
        if (defineFile == null) {
            errorExit(ERROR_CODE_NO_SQLFILE, getErrorMessage(ERROR_CODE_NO_SQLFILE));
        }

        IClaimData claim = null;
        try {
            claim = service.importClaimData(dataFile.getAbsolutePath(), defineFile.getAbsolutePath(), name, headIsData);
            claim.setDispIdField(idColumn);
            claim.setProductField(targetColumn);
            claim.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            errorExit(ERROR_CODE_ANY_EXCEPTION, e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
            errorExit(ERROR_CODE_ANY_EXCEPTION, e.getLocalizedMessage());
        }
    }


    @Override
    int check(String[] args) {
        if (args.length != 8) {
            return ERROR_CODE_ARGS;
        }
        service = createService(args[URL], args[USER], args[PASS]);
        if (service == null) {
            return ERROR_CODE_NO_DATABASE_CONNECTION;
        }

        dir = new File(args[FOLDER]);
        if (dir.isFile() || !dir.exists()) {
            return ERROR_CODE_NO_FOLDER;
        }

        name = args[NAME];
        CreateClaimTableValidator validator = new CreateClaimTableValidator();
        if (validator.isValid(name) != null) {
            return ERROR_CODE_NAME;
        } else if (validator.exists(name)) {
            return ERROR_CODE_NAME;
        }

        try {
            idColumn = Integer.parseInt(args[ID]);
            targetColumn = Integer.parseInt(args[TARGET]);
        } catch (NumberFormatException e) {
            return ERROR_CODE_INVALID_COLUMN;
        }

        /* 先頭行がデータかどうかのフラグの確認 */
        String boolValue = args[HEAD];
        if ("true".equalsIgnoreCase(boolValue)) { //$NON-NLS-1$
            headIsData = true;
        } else if ("false".equalsIgnoreCase(boolValue)) { //$NON-NLS-1$
            headIsData = false;
        } else {
            return ERROR_CODE_INVALID_BOOL;
        }

        return CODE_OK;
    }


    @Override
    String getErrorMessage(int errorCode) {
        switch (errorCode) {
        case ERROR_CODE_ARGS:
            return Messages.bind(Messages.ErrorMessage_ArgsNum, new Object[] { Messages.Name_Import, 8, Messages.Import_ErrorMessage_ArgsDetail });
        case ERROR_CODE_NO_FOLDER:
            return Messages.bind(Messages.Import_ErrorMessage_NoExistsFolder, dir.getAbsolutePath());
        case ERROR_CODE_NAME:
            CreateClaimTableValidator validator = new CreateClaimTableValidator();
            if (validator.isValid(name) != null) {
                return validator.isValid(name);
            } else if (validator.exists(name)) {
                return Messages.bind(Messages.Import_ErrorMessage_ExistsDataName, name);
            }
            // non reached code
        case ERROR_CODE_INVALID_COLUMN:
            return Messages.ErrorMessage_InvalidColumnNumber;
        case ERROR_CODE_INVALID_BOOL:
            return Messages.Import_ErrorMessage_InvalidBoolean;
        case ERROR_CODE_NO_DATAFILE:
            return Messages.Import_ErrorMessage_NoCsvFile;
        case ERROR_CODE_NO_SQLFILE:
            return Messages.Import_ErrorMessage_No_SqlFile;
        default:
            return super.getErrorMessage(errorCode);
        }
    }
}
