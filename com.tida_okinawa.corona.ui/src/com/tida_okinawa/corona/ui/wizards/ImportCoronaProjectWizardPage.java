/**
 * @version $Id: ImportCoronaProjectWizardPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/02 17:01:09
 * @author miyaguni
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.util.ValidateUtil;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProject;

/**
 * DB登録済みプロジェクトのインポートウィザードクラス
 * 
 * @author miyaguni
 */
public class ImportCoronaProjectWizardPage extends WizardPageBase {

    private IStructuredSelection selectedProjectList;


    protected ImportCoronaProjectWizardPage(String pageName) {
        super(pageName);
        setTitle(Messages.ImportCoronaProjectWizardPage_titleImportProject);
        setDescription(Messages.ImportCoronaProjectWizardPage_descriptionImportProject);

    }


    @Override
    public void createControl(Composite parent) {
        Composite composite = CompositeUtil.defaultComposite(parent, 1);

        Composite projectGroup = CompositeUtil.defaultComposite(composite, 1);
        ((GridData) projectGroup.getLayoutData()).grabExcessVerticalSpace = true;
        ((GridData) projectGroup.getLayoutData()).grabExcessHorizontalSpace = true;
        createProjectGroup(projectGroup);

        setSelectedProjectList(null);
        setControl(composite);
        setPageComplete(fieldValidate());
    }


    /**
     * DB登録済みプロジェクト一覧を表示する Composite を作成する
     * 
     * @param parent
     */
    private void createProjectGroup(Composite parent) {
        Composite composite = CompositeUtil.defaultComposite(parent, 1);
        Label l = new Label(composite, SWT.NONE);
        l.setText(Messages.ImportCoronaProjectWizardPage_compImportableProjectList);

        TableViewer projectListViewer = new TableViewer(composite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        projectListViewer.setContentProvider(new ArrayContentProvider());

        projectListViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof ICoronaProject) {
                    return ((ICoronaProject) element).getName();
                }
                return super.getText(element);
            }
        });

        projectListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setSelectedProjectList((IStructuredSelection) event.getSelection());
                setPageComplete(fieldValidate());
            }
        });

        projectListViewer.setInput(existingProjectList());

        Table t = projectListViewer.getTable();
        TableColumn col = new TableColumn(t, SWT.NONE);
        col.pack();
        if (col.getWidth() < 300) {
            col.setWidth(300);
        }
        t.setLayout(new GridLayout());
        t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }


    /**
     * バリデーションチェック
     * 
     * <pre>
     * 1. DB に登録済みのプロジェクトがない
     * 2. プロジェクトが未選択
     * </pre>
     * 
     * @return エラーが無ければ true 、あれば false
     */
    boolean fieldValidate() {
        if (existingProjectList().isEmpty()) {
            setErrorMessage(Messages.ImportCoronaProjectWizardPage_errorMessageNotImportableProject);
            return false;
        }

        IStructuredSelection selection = getSelectedProjectList();

        if (selection == null || selection.isEmpty()) {
            return false;
        }

        setErrorMessage(null);
        return true;
    }


    /**
     * インポート可能なプロジェクトリストを返す
     * <p>
     * ワークスペースに存在するプロジェクトは除外する
     * 
     * @return プロジェクトリスト
     */
    private static List<ICoronaProject> existingProjectList() {
        List<ICoronaProject> list = IoActivator.getService().getProjects();
        List<ICoronaProject> projects = new ArrayList<ICoronaProject>();

        for (ICoronaProject p : list) {
            if ((ValidateUtil.DUPLICATE_WS & ValidateUtil.isValidProjectName(p.getName())) == 0) {
                projects.add(p);
            }
        }

        return projects;
    }


    @Override
    public void setFocus() {
        // TODO 自動生成されたメソッド・スタブ

    }


    /**
     * @param selectedProject
     *            セットする selectedProject
     */
    void setSelectedProjectList(IStructuredSelection selection) {
        this.selectedProjectList = selection;
    }


    /**
     * @return selectedProject
     */
    public IStructuredSelection getSelectedProjectList() {
        return selectedProjectList;
    }
}
