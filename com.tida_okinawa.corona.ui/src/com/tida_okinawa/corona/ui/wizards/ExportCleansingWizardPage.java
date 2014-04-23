/**
 * @version $Id: ExportCleansingWizardPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/29 14:36:45
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.internal.ui.actions.CleansingExportOperation;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author kousuke-morishima
 */
public class ExportCleansingWizardPage extends ExportWizardPage {

    public ExportCleansingWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, CoronaModel.toContainer(selection));
        setTitle("クレンジング結果　エクスポート");
    }


    @Override
    public boolean finish() {
        final CleansingExportOperation op = new CleansingExportOperation();
        List<?> selectedResources = getSelectedResources();
        final IUIWork[] subjects = new IUIWork[selectedResources.size()];
        Iterator<?> itr = selectedResources.iterator();
        for (int i = 0; i < subjects.length; i++) {
            subjects[i] = (IUIWork) model.adapter((IResource) itr.next(), false);
        }
        final File exportDir = new File(destinationField.getText());

        Job exportJob = new Job("エクスポート") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                IStatus status = null;
                try {
                    status = op.execute(subjects, exportDir, monitor);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, e.getLocalizedMessage());
                }

                if (status.getSeverity() == IStatus.CANCEL) {
                    CoronaActivator.getDefault().getLogger().getOutStream().println("処理がキャンセルされました。");
                }

                return status;
            }


            @Override
            public boolean belongsTo(Object family) {
                return false;
            }
        };
        exportJob.setUser(true);
        exportJob.setSystem(false);
        exportJob.schedule();

        try {
            exportJob.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        saveWidgetValues();
        return (Status.OK == exportJob.getResult().getSeverity());
    }


    /* ****************************************
     * validate
     */
    @Override
    protected boolean validateSourceGroup() {
        if (!super.validateSourceGroup()) {
            return false;
        }

        List<?> resources = getSelectedResources();
        for (Object o : resources) {
            if (!isWork((IResource) o)) {
                setErrorMessage("エクスポート可能なクレンジング結果ではないファイルが含まれています。\n（構文解析のみ出力可能）");
                return false;
            }
        }

        return true;
    }

    CoronaModel model = CoronaModel.INSTANCE;


    private boolean isWork(IResource resource) {
        if (!(resource instanceof IFile)) {
            return false;
        }

        IUIElement element = model.adapter(resource, false);
        if (element instanceof IUIWork) {
            return isValidWork((IUIWork) element);
        }

        if (element != null) {
            /* その他のCoronaObjectが選択されている */
            return false;
        }

        /* 以下、プロジェクトツリーを未展開だと、adapterがうまく働かないことへの対応 */
        IProject project = resource.getProject();
        IUIProject uiProject = (IUIProject) model.adapter(project, false);
        if (uiProject == null) {
            return false;
        }
        IUIProduct[] uiProducts = uiProject.getProducts();
        IUIProduct uiProduct = null;
        for (IUIProduct uiProd : uiProducts) {
            if (uiProd.getResource().getFullPath().isPrefixOf(resource.getFullPath())) {
                uiProduct = uiProd;
                break;
            }
        }
        if (uiProduct == null) {
            return false;
        }
        IUIWork[] uiWorks = uiProduct.getWorks();
        for (IUIWork uiWork : uiWorks) {
            if (uiWork.getResource().equals(resource)) {
                return isValidWork(uiWork);
            }
        }

        return false;
    }


    private static boolean isValidWork(IUIWork uiWork) {
        /* Memo 今は構文解析結果だけ有効 */
        IClaimWorkData work = uiWork.getObject();
        switch (work.getClaimWorkDataType()) {
        case RESLUT_PATTERN:
            return true;
        case CORRECTION_MISTAKES:
        case MORPHOLOGICAL:
        case DEPENDENCY_STRUCTURE:
        case CORRECTION_FLUC:
        case CORRECTION_SYNONYM:
        case FREQUENTLY_APPERING:
        default:
            return false;
        }
    }

}
