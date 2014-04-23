/**
 * @version $Id: ImportCoronaProjectWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/02 17:01:09
 * @author miyaguni
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.ui.ViewUtil;

/**
 * Corona プロジェクトのインポートウィザードクラス
 * 
 * @author miyaguni
 * 
 */
public class ImportCoronaProjectWizard extends AbstractCoronaProjectWizard implements IImportWizard {

    private ImportCoronaProjectWizardPage page = null;


    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(Messages.ImportCoronaProjectWizard_windowTitleImportDatabase);
        setNeedsProgressMonitor(false);
    }


    @Override
    public void addPages() {
        page = new ImportCoronaProjectWizardPage(Messages.ImportCoronaProjectWizard_wizardPageImportDatabase);
        addPage(page);
    }


    /**
     * 完了アクション(プロジェクト作成)
     * <p>
     * 選択したプロジェクト(複数可)を作成する。<br/>
     * 作成時にエラーが起きたプロジェクトは作成されず、 エラーダイアログで「エラーが起きたプロジェクト名: エラーメッセージ」を表示する
     */
    @Override
    public boolean performFinish() {
        IProgressMonitor monitor = new NullProgressMonitor();
        boolean result = true;
        StringBuilder message = new StringBuilder(Messages.ImportCoronaProjectWizard_messageCreateProjectFail);

        for (Object pObject : page.getSelectedProjectList().toArray()) {
            String name = ((ICoronaProject) pObject).getName();

            try {
                createProject(name, ResourcesPlugin.getWorkspace().getRoot().getLocationURI(), monitor);
            } catch (CoreException e) {
                message.append(name + ": " + e.getLocalizedMessage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
                e.printStackTrace();
                result = false;
            }
        }

        if (!result) {
            MessageDialog.openError(getShell(), Messages.ImportCoronaProjectWizard_messageError, message.toString());
        }

        ViewUtil.refreshDatabaseView(0);
        return result;
    }
}
