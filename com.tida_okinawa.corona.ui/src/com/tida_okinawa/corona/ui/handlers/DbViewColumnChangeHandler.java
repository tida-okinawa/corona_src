/**
 * @version $Id: DbViewColumnChangeHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 13:46:10
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.ui.wizards.ColumnSelectWizard;

/**
 * 
 * @author kyohei-miyazato
 */
public class DbViewColumnChangeHandler extends AbstractHandler {
    private IWorkbenchWindow window = null;
    private IStructuredSelection selection = null;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        /* IDカラム/ターゲットカラムを選択し直す処理 */
        Shell shell = HandlerUtil.getActiveShell(event);

        if (selection.getFirstElement() instanceof IClaimData) {
            IClaimData iClaimData = (IClaimData) selection.getFirstElement();
            /* ターゲットカラム選択ダイアログ呼び出し */
            columnChange(shell, iClaimData);
        }
        return null;
    }


    @Override
    public boolean isEnabled() {
        window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        /*
         * 何も選択されていなければfalse
         * 単/複数選択時、フォルダ、プロジェクト、辞書、を含んで選択している場合はfalse
         * 問い合わせデータを選択している場合true
         */
        if (window == null)
            return false;
        selection = (IStructuredSelection) window.getActivePage().getSelection();
        if (selection != null && selection.size() == 1) {
            for (Iterator<?> itr = selection.iterator(); itr.hasNext();) {
                Object item = itr.next();
                if (item instanceof IClaimData)
                    return true;
            }
        }
        return false;
    }


    /**
     * IDカラム、ターゲットカラム選択wizard呼び出し
     * 
     * @param shell
     * @param claim
     * @return IDカラム、ターゲットカラム選択を行ったらtrue
     */
    public static boolean columnChange(Shell shell, IClaimData claim) {
        ColumnSelectWizard wizard = new ColumnSelectWizard();
        wizard.setClaimData(claim);
        WizardDialog d = new WizardDialog(shell, wizard);
        if (d.open() != Dialog.OK) {
            return false;
        }
        return true;
    }
}
