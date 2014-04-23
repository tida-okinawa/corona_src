/**
 * @version $Id: DbViewImportHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 13:46:18
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.ui.wizards.DicImportWizard;


/**
 * 
 * @author kyohei-miyazato
 */
public class DbViewImportHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // TODO 辞書インポート処理
        // IWorkbenchWindow window =
        // HandlerUtil.getActiveWorkbenchWindowChecked(event);
        // MessageDialog.openInformation(window.getShell(), "辞書インポート",
        // "import");
        /*** 試作1号機 ****************************************************************/
        Shell shell = HandlerUtil.getActiveShell(event);
        DicImportWizard wizard = new DicImportWizard();
        WizardDialog d = new WizardDialog(shell, wizard);
        if (d.open() != Dialog.OK) {
            return false;
        }
        /*** 試作1豪鬼 ****************************************************************/

        return true;
    }


    @Override
    public boolean isEnabled() {

        return true;
    }
}
