/**
 * @version $Id: DicImportWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/03 9:50:58
 * @author kenta-uechi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.tida_okinawa.corona.ui.ViewUtil;

/**
 * 辞書インポートウィザード
 * 
 * @author kenta-uechi
 */
public class DicImportWizard extends Wizard implements IImportWizard {

    DicImportWizardPage mainPage;


    /**
     * 辞書インポートウィザードのコンストラクター
     * デフォルトタイトルをセットする
     */
    public DicImportWizard() {
        setWindowTitle("外部辞書のインポート");
    };


    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("辞書ファイル・インポート");
        setNeedsProgressMonitor(false);
    }


    @Override
    public boolean performFinish() {
        setNeedsProgressMonitor(true);
        mainPage.createNewFile();
        ViewUtil.refreshDatabaseView(0);
        return true;
    }


    @Override
    public void addPages() {
        /* ウィザードページを追加 */
        mainPage = new DicImportWizardPage("外部からの辞書インポート");
        addPage(mainPage);
    }
}
