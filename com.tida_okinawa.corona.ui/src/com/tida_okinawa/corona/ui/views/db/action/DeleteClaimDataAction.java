/**
 * @version $Id: DeleteClaimDataAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/12 17:44:46
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
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author kousuke-morishima
 */
public class DeleteClaimDataAction extends Action {

    public DeleteClaimDataAction() {
    }

    private IClaimData claim = null;


    public void setClaimData(IClaimData claim) {
        this.claim = claim;
    }


    @Override
    public void run() {
        List<IUIElement> uiElements = CoronaModel.INSTANCE.adapter(claim);
        /* ローカルファイルシステムから削除 */
        CoronaElementDeleteOperation op = new CoronaElementDeleteOperation(uiElements.toArray(new IUIElement[uiElements.size()]), "");
        try {
            op.execute(null, null);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /* DBから問い合わせデータを削除 */
        if (IoActivator.getService().removeClaimData(claim.getId())) {
            result = Status.OK_STATUS;
        } else {
            result = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, claim.getTableName() + "の削除に失敗しました");
        }
    }

    private IStatus result;


    public IStatus getResult() {
        return result;
    }

}
