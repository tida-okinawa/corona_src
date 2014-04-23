/**
 * @version $Id: Erratum.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/01 16:21:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.TableType;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;
import com.tida_okinawa.corona.ui.controllers.ErratumController;

/**
 * 誤記補正をコンソールから行うためのクラス
 * 
 * @author kousuke-morishima
 * 
 */
public class Erratum extends Cleansing {
    // TODO FindBugs

    /**
     * @param args
     *            前から順に、データベース接続URL、データベースユーザ名、データベースパスワード、誤記補正対象問い合わせデータ名、
     *            誤記補正対象フィールドID（複数可）
     */
    public static void main(String[] args) {
        try {
            new Erratum(args).run();
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
    private Erratum(String[] args) {
        super(args);
    }

    /** 引数の数がおかしいときのエラーコード */
    public static final int ERROR_CODE_ARGS = 5;
    /** 指定された問い合わせデータがないときのエラーコード */
    public static final int ERROR_CODE_NO_CLAIM = 6;
    /** 指定されたプロジェクトが存在しないときのエラーコード */
    public static final int ERROR_CODE_PROJECT_NON_EXISTS = 7;
    /** 指定された誤記補正対象フィールドの値がおかしいときのエラーコード */
    public static final int ERROR_CODE_INVALID_FIELDS = 8;

    private static final int URL = 0;
    private static final int USER = 1;
    private static final int PASS = 2;
    private static final int DATANAME = 3;
    private static final int PROJECT = 4;
    private static final int FIELDS = 5;

    private IClaimData claim;
    private List<IFieldHeader> fields;
    private ICoronaProject project;


    @Override
    int check(String[] args) {
        if (args.length < 6) {
            return ERROR_CODE_ARGS;
        }

        service = createService(args[URL], args[USER], args[PASS]);
        if (service == null) {
            return ERROR_CODE_NO_DATABASE_CONNECTION;
        }

        String tableName = CoronaIoUtils.createWorkTableName(args[DATANAME], TableType.CLAIM_DATA, 0);
        claim = getClaimData(tableName);
        if (claim == null) {
            return ERROR_CODE_NO_CLAIM;
        }

        project = searchProject(args[PROJECT]);
        if (project == null) {
            return ERROR_CODE_PROJECT_NON_EXISTS;
        }

        fields = getFields(claim, args);
        if (fields == null) {
            return ERROR_CODE_INVALID_FIELDS;
        }

        return CODE_OK;
    }


    @Override
    String getErrorMessage(int errorCode) {
        switch (errorCode) {
        case ERROR_CODE_ARGS:
            return Messages.bind(Messages.ErrorMessage_ArgsNum, new Object[] { Messages.Name_Erratum, 6, Messages.Erratum_ErrorMessage_ArgsDetail });
        case ERROR_CODE_NO_CLAIM:
            return Messages.Erratum_ErrorMessage_NoData;
        case ERROR_CODE_PROJECT_NON_EXISTS:
            return Messages.Erratum_ErrorMessage_NoProject;
        case ERROR_CODE_INVALID_FIELDS:
            return Messages.Erratum_ErrorMessage_InvalidFieldsId;
        default:
            return super.getErrorMessage(errorCode);
        }
    }


    void run() {
        Shell parentShell = new Shell();
        final ErratumController controller = new ErratumController(parentShell, claim, fields, null);
        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(parentShell);
        try {
            dialog.run(true, true, controller);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            errorExit(ERROR_CODE_ANY_EXCEPTION, getErrorMessage(ERROR_CODE_ANY_EXCEPTION));
        } catch (InterruptedException e) {
            e.printStackTrace();
            errorExit(ERROR_CODE_INTERRUPTED, getErrorMessage(ERROR_CODE_INTERRUPTED));
        }

        /* 問い合わせデータをプロジェクトに登録する(ターゲットを作る) */
        AutoCleansingUtil.registerProject(project, claim, fields);
    }


    /* テーブル名に該当する問い合わせデータがなかったらnullを返す */
    private IClaimData getClaimData(String tableName) {
        for (IClaimData claim : service.getClaimDatas()) {
            if (claim.getTableName().equalsIgnoreCase(tableName)) {
                return claim;
            }
        }
        return null;
    }


    /* 不正な引数が与えられたときは、nullを返す */
    private static List<IFieldHeader> getFields(IClaimData claim, String[] args) {
        List<IFieldHeader> fields = new ArrayList<IFieldHeader>(args.length - FIELDS);
        for (int i = FIELDS; i < args.length; i++) {
            try {
                IFieldHeader field = claim.getFieldInformation(Integer.parseInt(args[i]));
                fields.add(field);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return fields;
    }
}
