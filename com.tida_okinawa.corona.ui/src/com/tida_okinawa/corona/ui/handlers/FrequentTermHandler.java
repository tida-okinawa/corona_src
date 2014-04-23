/**
 * @version $Id: FrequentTermHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/29 14:38:34
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.CleansingNameVariable;
import com.tida_okinawa.corona.internal.ui.util.Kernel;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.ui.ViewUtil;
import com.tida_okinawa.corona.ui.controllers.FrequentController;
import com.tida_okinawa.corona.ui.controllers.FrequentControllerForUI;

/**
 * @author takayuki-matsumoto
 */
public class FrequentTermHandler extends AbstractCleansingHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        super.execute(event);

        Kernel.startSuppressSleep();

        try {
            for (IUIProduct uiProduct : getProducts()) {
                ICoronaProduct product = uiProduct.getObject();
                List<IClaimData> claims = product.getClaimDatas();
                IClaimWorkData workData = null;

                /* ダイアログメッセージ用のStringBuffer */
                StringBuilder message = new StringBuilder();
                message.append(CleansingNameVariable.FREQUENT);
                message.append("が終了しました。\n\n対象列:\n");

                for (IClaimData claim : claims) {
                    int claimId = claim.getId();
                    Collection<Integer> fieldIds = product.getMiningFields(claimId);
                    for (Integer fieldId : fieldIds) {
                        IClaimWorkData srcWork = null;
                        for (IClaimWorkData work : srcWorkList) {
                            if ((work.getClaimId() == claimId) && (work.getFieldId() == fieldId)) {
                                srcWork = work;
                                break;
                            }
                        }
                        if (srcWork != null) {
                            IUIWork uiWork = CoronaModel.INSTANCE.getWork(uiProduct, srcWork);
                            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                            FrequentController controller = new FrequentControllerForUI(uiProduct, uiWork);
                            Shell shell = window.getShell();
                            ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
                            long s = System.currentTimeMillis();
                            dialog.run(true, true, controller);
                            CoronaActivator.debugLog("頻出用語抽出時間：    " + (System.currentTimeMillis() - s) + "mSec");
                            /* 最終出力用のクレームデータ保持 */
                            workData = getClaimWorkData(uiProduct, ClaimWorkDataType.FREQUENTLY_APPERING, uiWork.getObject().getFieldId(), uiWork.getObject()
                                    .getClaimId());

                            /* 処理した列名をダイアログ用StringBufferに追加する */
                            IClaimData claimData = IoActivator.getService().getClaimData(workData.getClaimId());
                            IFieldHeader header = claimData.getFieldInformation(fieldId);
                            message.append("　 [");
                            message.append(header.getName());
                            message.append("]\n");
                        }
                    }
                }
                if (workData != null) {
                    /* 処理結果ダイアログを開く */
                    MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CleansingNameVariable.FREQUENT + "終了",
                            message.toString());
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Kernel.stopSuppressSleep();
        }
        ViewUtil.refreshProjectExplorer(0);
        return null;
    }


    @Override
    protected boolean isEnabled(ClaimWorkDataType type) {
        switch (type) {
        case DEPENDENCY_STRUCTURE:
        case CORRECTION_FLUC:
        case CORRECTION_SYNONYM:
            return true;
        default:
            return false;
        }
    }


    @Override
    ClaimWorkDataType getExecType() {
        return ClaimWorkDataType.FREQUENTLY_APPERING;
    }
}
