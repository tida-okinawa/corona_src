/**
 * @version $Id: AbstractCoronaProjectWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/02 17:01:09
 * @author miyaguni
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.tida_okinawa.corona.internal.ui.CoronaConstants;
import com.tida_okinawa.corona.internal.ui.PersistentPropertyKeys;
import com.tida_okinawa.corona.internal.ui.util.PreferenceUtils;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.data.ConnectionParameter;

/**
 * Corona Project 作成ウィザードの Abstract クラス
 * 
 * @author miyaguni
 */
public abstract class AbstractCoronaProjectWizard extends Wizard implements IExecutableExtension {

    /**
     * パースペクティブを切り替える処理に必要
     */
    private IConfigurationElement config;


    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        this.config = config;
    }


    /**
     * プロジェクトを作成
     * 
     * @param name
     *            作成するプロジェクト名
     * @param locationURI
     *            プロジェクトのロケーション
     * @param monitor
     *            進捗表示モニター(Nullの場合は非表示)
     * @throws CoreException
     *             例外処理
     */
    protected void createProject(String name, URI locationURI, IProgressMonitor monitor) throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);

        if (!project.exists()) {
            /* ローカルファイルシステムにプロジェクト作成 */
            IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
            if ((locationURI != null) && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(locationURI)) {
                locationURI = null;
            }
            desc.setLocationURI(locationURI);
            project.create(desc, monitor);
            if (!project.isOpen()) {
                project.open(monitor);
            }

            /* CoronaNatureを付加（CoronaProject作成前に付加する） */
            CoronaModel.INSTANCE.setNature(project, monitor);
            /* DBで付与されるIDを保持する（ローカルファイルシステム上に作成してからセットする） */
            IPreferenceStore pStore = UIActivator.getDefault().getPreferenceStore(project);
            ConnectionParameter parameter = PreferenceUtils.getCurrentConnectionParameter();
            if (parameter != null) {
                pStore.setValue(PersistentPropertyKeys.DB_CONNECT_NAME.toString(), parameter.name);
                pStore.setValue(PersistentPropertyKeys.DB_CONNECT_URL.toString(), parameter.url);
            }

            /* プロジェクト作成 & DBへの登録 */
            IUIProject uiProject = (IUIProject) CoronaModel.INSTANCE.create(project);
            pStore.setValue(PersistentPropertyKeys.PROJECT_ID.toString(), uiProject.getId());

            /* パースペクティブ切り替え */
            BasicNewProjectResourceWizard.updatePerspective(config);

            /* ローカルファイル作成 */
            ControlLocalFolderAndFileUtil coronaLocal = new ControlLocalFolderAndFileUtil();
            coronaLocal.createFolder(project, CoronaConstants.COMMON_LIBRARY_NAME, monitor);
            coronaLocal.createFolder(project, CoronaConstants.CLAIM_FOLDER_NAME, monitor);
            coronaLocal.createDicAndWorkData(uiProject, monitor);
            coronaLocal.createCommonDicFile(uiProject, monitor);
            coronaLocal.createClaimDataFile(uiProject, monitor);
        }
    }
}
