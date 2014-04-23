/**
 * @version $Id: DbViewWebEntryHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/19 19:51:42
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.webentry.ui.WebEntryWizard;

/**
 * ウェブから問い合わせデータを登録するウィザードを開くハンドラー
 * 
 * @author yukihiro-kinjo
 * 
 */
public class DbViewWebEntryHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        /* Webから登録ダイアログ表示 */
        Shell shell = HandlerUtil.getActiveShell(event);
        WebEntryWizard entryWizard = new WebEntryWizard();
        WizardDialog wizardDialog = new WizardDialog(shell, entryWizard);
        wizardDialog.open();
        return null;
    }

}
