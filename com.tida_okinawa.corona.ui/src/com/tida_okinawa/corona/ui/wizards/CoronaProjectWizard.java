/**
 * @version $Id: CoronaProjectWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/01 13:28:20
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;


import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.tida_okinawa.corona.ui.ViewUtil;

/**
 * Corona Project 新規作成ウィザードクラス
 */
public class CoronaProjectWizard extends AbstractCoronaProjectWizard implements INewWizard {

    private NewCoronaProjectCreationPage page = null;


    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("新規Coronaプロジェクト");
    }


    @Override
    public void addPages() {
        page = new NewCoronaProjectCreationPage("corona project creation page");
        addPage(page);
    }


    /**
     * 完了アクション(プロジェクト作成)
     * <p>
     * NewCoronaProjectCreationPage で入力した名称でプロジェクトを作成する
     */
    @Override
    public boolean performFinish() {
        String name = page.getProjectName();
        IProgressMonitor monitor = new NullProgressMonitor();

        try {
            if (page.useDefaults()) {
                createProject(name, null, monitor);
            } else {
                /* プロジェクト名をロケーションにくっつける */
                URI url = page.getLocationURI();
                try {
                    url = new URI(url.toString() + "/" + name);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                createProject(name, url, monitor);
            }
            ViewUtil.refreshDatabaseView(0);
            return true;
        } catch (CoreException e) {
            e.printStackTrace();
            MessageDialog.openError(getShell(), "Error", e.getLocalizedMessage());
            return false;
        }
    }
}
