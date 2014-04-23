/**
 * @version $Id: ExportDictionaryWizardPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/29 14:36:45
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.tida_okinawa.corona.internal.ui.actions.DictionaryExportOperation;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.internal.ui.views.model.impl.UILibFolder;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDependDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * 辞書エクスポートウィザードページ
 * 
 * @author kousuke-morishima
 */
public class ExportDictionaryWizardPage extends ExportWizardPage {

    public ExportDictionaryWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, CoronaModel.toResources(selection));
        setTitle("辞書　エクスポート");
    }


    @Override
    public boolean finish() {
        final DictionaryExportOperation op = new DictionaryExportOperation();
        List<?> selectedResources = getSelectedResources();
        final ICoronaDic[] subjects = new ICoronaDic[selectedResources.size()];
        Iterator<?> itr = selectedResources.iterator();
        final Map<ICoronaDic, Set<ILabelDic>> map = new HashMap<ICoronaDic, Set<ILabelDic>>();
        for (int i = 0; i < subjects.length; i++) {
            IResource resource = (IResource) itr.next();
            subjects[i] = (ICoronaDic) model.adapter(resource, false).getObject();

            // ユーザー辞書の場合、ラベル辞書マップを作成
            if (subjects[i] instanceof IUserDic) {
                IProject project = resource.getProject();
                IUIProject uiProject = (IUIProject) model.adapter(project, false);
                List<ICoronaDic> ldics = uiProject.getObject().getDictionarys(ILabelDic.class);
                for (ICoronaDic dic : ldics) {
                    Set<Integer> id = dic.getParentIds();
                    if (id.contains(subjects[i].getId())) {
                        Set<ILabelDic> list = map.get(subjects[i]);
                        if (list == null) {
                            list = new HashSet<ILabelDic>();
                            map.put(subjects[i], list);
                        }
                        list.add((ILabelDic) dic);
                    }
                }

                IUIProduct[] uiProducts = uiProject.getProducts();
                for (IUIProduct uiProd : uiProducts) {
                    List<ICoronaDic> ldics2 = uiProd.getObject().getDictionarys(ILabelDic.class);
                    for (ICoronaDic dic : ldics2) {
                        Set<Integer> id = dic.getParentIds();
                        if (id.contains(subjects[i].getId())) {
                            Set<ILabelDic> list = map.get(subjects[i]);
                            if (list == null) {
                                list = new HashSet<ILabelDic>();
                                map.put(subjects[i], list);
                            }
                            list.add((ILabelDic) dic);
                        }
                    }
                }

            }
        }
        final File exportDir = new File(destinationField.getText());

        Job exportJob = new Job("エクスポート") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                IStatus status = null;
                try {
                    List<ICoronaDic> list = new ArrayList<ICoronaDic>();
                    for (ICoronaDic dic : subjects) {
                        list.add(dic);
                    }
                    status = op.execute(list.toArray(new ICoronaDic[list.size()]), exportDir, map, monitor);
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
            if (!isDictionary((IResource) o)) {
                setErrorMessage("エクスポート可能な辞書以外のファイルが含まれています。");
                return false;
            }
        }
        return true;
    }

    CoronaModel model = CoronaModel.INSTANCE;


    private boolean isDictionary(IResource resource) {
        if (!(resource instanceof IFile)) {
            return false;
        }

        IUIElement element = model.adapter(resource, false);
        if (element instanceof IUIDictionary) {
            /* IUIDictionaryが選択されている */
            return isDictionary(resource, element);
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
        IUIElement[] eles = uiProject.getChildren();
        for (IUIElement uiEle : eles) {
            if (isDictionary(resource, uiEle)) {
                return true;
            }
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
        IUIElement[] eles2 = uiProduct.getChildren();
        for (IUIElement uiEle : eles2) {
            if (isDictionary(resource, uiEle)) {
                return true;
            }
        }

        return false;
    }


    private boolean isDictionary(IResource resource, IUIElement element) {
        if (element instanceof IUIDictionary) {
            // ユーザー辞書、ゆらぎ辞書、同義語辞書、パターン辞書のみエクスポート可能。
            ICoronaDic dic = ((IUIDictionary) element).getObject();
            if (dic instanceof IUserDic || dic instanceof IDependDic || dic instanceof IPatternDic) {
                return element.getResource().equals(resource);
            }
            return false;
        }

        if (element instanceof UILibFolder) {
            UILibFolder uiLib = (UILibFolder) element;
            for (IUIElement uiEle : uiLib.getChildren()) {
                if (isDictionary(resource, uiEle)) {
                    return true;
                }
            }
        }
        return false;
    }

}
