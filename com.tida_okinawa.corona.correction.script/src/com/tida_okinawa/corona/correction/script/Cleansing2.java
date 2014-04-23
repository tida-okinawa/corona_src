/**
 * @version $Id: Cleansing2.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/05 20:20:06
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import com.tida_okinawa.corona.ui.controllers.ClaimWorkDataController;
import com.tida_okinawa.corona.ui.data.ClaimWorkDataRecord;

/**
 * クレンジング処理をコンソールから行うクラスの基底クラス。
 * Morpheme、Pattern、Synonym、ExportCSVに適用される
 * 
 * @author kousuke-morishima
 * 
 * @param <TS>
 *            入力データタイプ
 * @param <TD>
 *            出力データタイプ
 */
public abstract class Cleansing2<TS extends ClaimWorkDataRecord, TD extends ClaimWorkDataRecord> extends Cleansing {

    protected static final String TYPE_MOR = "Morpheme"; //$NON-NLS-1$
    protected static final String TYPE_DEP = "Dependency"; //$NON-NLS-1$
    protected static final String TYPE_SYN = "Synonym"; //$NON-NLS-1$


    /**
     * 自動実行用
     * 
     * @param args
     *            引数
     */
    Cleansing2(String[] args) {
        super(args);
    }


    /** 指定されたプロジェクトがないときのエラーコード */
    public static final int ERROR_CODE_NO_PROJECT = 5;
    /** 指定されたターゲットがないときのエラーコード */
    public static final int ERROR_CODE_NO_TARGET = 6;


    @Override
    String getErrorMessage(int errorCode) {
        switch (errorCode) {
        case ERROR_CODE_NO_PROJECT:
            return Messages.Cleansing2_NoProject;
        case ERROR_CODE_NO_TARGET:
            return Messages.Cleansing2_NoProduct;
        default:
            return super.getErrorMessage(errorCode);
        }
    }


    boolean run(ClaimWorkDataController<TS, TD> controller) {
        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell());
        try {
            dialog.run(true, true, controller);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            errorExit(ERROR_CODE_ANY_EXCEPTION, e.getLocalizedMessage());
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            errorExit(ERROR_CODE_INTERRUPTED, e.getLocalizedMessage());
            return false;
        }
        return true;
    }
}
