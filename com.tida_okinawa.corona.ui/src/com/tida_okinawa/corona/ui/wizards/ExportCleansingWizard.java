/**
 * @version $Id: ExportCleansingWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/29 14:26:01
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author kousuke-morishima
 */
public class ExportCleansingWizard extends Wizard implements IExportWizard {

    public ExportCleansingWizard() {
        setWindowTitle("クレンジング結果エクスポート");
        setDialogSettings(UIActivator.getDefault().getDialogSettings());
    }

    IStructuredSelection selection;
    ExportCleansingWizardPage page1;


    @Override
    public void addPages() {
        page1 = new ExportCleansingWizardPage("", selection);
        addPage(page1);
    }


    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }


    @Override
    public boolean performFinish() {
        return page1.finish();
    }

}
