/**
 * @version $Id: DbViewDBEntryHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 13:46:00
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.ui.wizards.EntryWizard;

/**
 * DBへ問い合わせデータを登録するウィザードダイアログを開くハンドラー
 * 
 * @author kyohei-miyazato
 */
public class DbViewDBEntryHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        /* 問合せデータ登録ダイアログ表示 */
        Shell shell = HandlerUtil.getActiveShell(event);
        EntryWizard wizard = new EntryWizard();
        WizardDialog wiz = new WizardDialog(shell, wizard);
        wiz.open();
        return null;
    }


    @Override
    public boolean isEnabled() {
        return true;
    }
}
