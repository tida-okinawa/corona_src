package com.tida_okinawa.corona.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @version $Id: DataBaseUserDicImportWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/04 15:43:25
 * @author kenta-uechi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */

/**
 * @author kenta-uechi
 */
public class DataBaseUserDicImportWizard extends Wizard implements IImportWizard {

    DataBaseUserDicImportWizardPage mainPage;


    /**
     * コンストラクター
     */
    public DataBaseUserDicImportWizard() {
    }


    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(Messages.DataBaseUserDicImportWizard_windowTitleImportDic);
        setNeedsProgressMonitor(false);
        mainPage = new DataBaseUserDicImportWizardPage(Messages.DataBaseUserDicImportWizard_wizardPageImportDic, selection);
    }


    @Override
    public boolean performFinish() {
        mainPage.importDics();
        return true;
    }


    @Override
    public void addPages() {
        addPage(mainPage);
    }

}
