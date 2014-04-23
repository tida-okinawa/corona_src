/**
 * @version $Id: DeleteProjectAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/12 18:31:20
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.db.action;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;

import com.tida_okinawa.corona.internal.ui.actions.CoronaElementDeleteOperation;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * DBとローカルファイルシステムから、指定されたプロジェクトを削除する
 * 
 * @author kousuke-morishima
 */
public class DeleteProjectAction extends Action {

    public DeleteProjectAction() {
    }

    private ICoronaProject project = null;


    public void setProject(ICoronaProject project) {
        this.project = project;
    }


    @Override
    public void run() {
        List<IUIElement> projs = CoronaModel.INSTANCE.adapter(project);
        /* ローカルファイルシステムから削除 */
        CoronaElementDeleteOperation op = new CoronaElementDeleteOperation(projs.toArray(new IUIElement[projs.size()]), "");
        try {
            result = op.execute(null, null);
            if (result.isOK()) {
                /* 全部正常に消せたら、DBから消す */
                if (IoActivator.getService().removeProject(project)) {
                    result = Status.OK_STATUS;
                } else {
                    result = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, "データベースから" + project.getName() + "を削除できませんでした");
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            result = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, "ファイル削除中にエラーが発生しました", e);
        }
    }

    private IStatus result;


    public IStatus getResult() {
        return result;
    }


    /**
     * @return 常にtrue
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
