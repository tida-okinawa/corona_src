/**
 * @version $Id: CleansingSyntaxHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 16:28:24
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

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
import com.tida_okinawa.corona.io.model.ICoronaDicPri;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.ui.ViewUtil;
import com.tida_okinawa.corona.ui.controllers.PatternMatcher;
import com.tida_okinawa.corona.ui.controllers.PatternMatcherForUI;
import com.tida_okinawa.corona.ui.wizards.CleansingWizard;

/**
 * @author takayuki-matsumoto, imai
 */
public class CleansingSyntaxHandler extends AbstractCleansingHandler {

    List<ICoronaDic> checkedDics;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        super.execute(event);
        /* ウィザードを表示　 */
        Shell shell = HandlerUtil.getActiveShell(event);

        Kernel.startSuppressSleep();

        try {
            List<CleansingWizard> wizards = new ArrayList<CleansingWizard>();
            for (IUIProduct uiProduct : getProducts()) {
                /* 辞書優先度情報は、出力先処理結果から取ってくる */
                CleansingWizard wizard = new CleansingWizard(dstWorkList, EXEC_TYPE, uiProduct);
                WizardDialog d = new WizardDialog(shell, wizard);
                if (d.open() != Dialog.OK) {
                    return false;
                }
                wizards.add(wizard);
            }
            int i = 0;
            for (IUIProduct uiProduct : getProducts()) {
                CleansingWizard wizard = wizards.get(i++);
                ICoronaProduct product = uiProduct.getObject();
                List<IClaimData> claims = product.getClaimDatas();
                for (IClaimData claim : claims) {
                    int claimId = claim.getId();
                    Collection<Integer> fieldIds = product.getMiningFields(claimId);
                    for (Integer fieldId : fieldIds) {
                        IClaimWorkData srcCwd = null;
                        // クレームワークデータを探す
                        for (IClaimWorkData cwd : srcWorkList) {
                            if (cwd.getClaimId() == claimId && cwd.getFieldId() == fieldId) {
                                srcCwd = cwd;
                                break;
                            }
                        }
                        if (!(srcCwd != null && product.getId() == srcCwd.getProductId())) {
                            continue;
                        }
                        IClaimWorkData dstCwd = null;
                        if (wizard.isFieldSelect()) {
                            for (IClaimWorkData cwd : dstWorkList) {
                                if (cwd.getClaimId() == claimId && cwd.getFieldId() == fieldId) {
                                    dstCwd = cwd;
                                    break;
                                }
                            }
                            if (dstCwd == null) {
                                /* 対応する辞書リストが取得できない場合、後続の処理を行わない */
                                continue;
                            }
                            // 辞書を取得
                            checkedDics = getDictionary(wizard.getFieldDicPriorityList(dstCwd));
                        } else {
                            checkedDics = getDictionary(wizard.getDicPriorityList());
                        }
                        try {
                            stepFields(uiProduct, srcCwd, wizard.getHitSelectStatus()); // 構文解析実行
                        } catch (InterruptedException e1) {
                            IClaimWorkData cwd = product.getClaimWorkData(claimId, EXEC_TYPE, fieldId);
                            if (cwd != null) {
                                /*
                                 * 失敗したので、レコード以外を復元。
                                 * 処理前に前回のデータは消えているので、前のデータに戻ることはない。
                                 */
                                cwd.update();
                            }
                        }

                        if (dstCwd != null && dstCwd.getWorkdataId() == 0 && dstCwd.getLasted() != null) {
                            IClaimWorkData cwd = product.getClaimWorkData(claimId, EXEC_TYPE, fieldId);
                            wizard.updateDicPriMap(dstCwd, cwd);
                        }

                    }
                }
                // 保存処理
                wizard.saveDicPriList();

                // TODO dstWorkListじゃなくて、実際に処理したIClaimWorkDataの一覧から取得する
                if (dstWorkList.size() > 0) {
                    IClaimWorkData targetWork = dstWorkList.get(dstWorkList.size() - 1);
                    IClaimWorkData claimWorkData = getClaimWorkData(uiProduct, EXEC_TYPE, targetWork.getFieldId(), targetWork.getClaimId());

                    if (claimWorkData != null && claimWorkData.getLasted() != null) {
                        /* ダイアログメッセージ用に処理した列名を取得 */
                        StringBuilder dialogMessage = new StringBuilder();
                        dialogMessage.append(CleansingNameVariable.PATTERN_PARSING);
                        dialogMessage.append("が終了しました。\n\n対象列:\n");
                        for (int ch = 0; ch < (dstWorkList.size()); ch++) {
                            IClaimData claimData = IoActivator.getService().getClaimData(claimWorkData.getClaimId());
                            IFieldHeader header = claimData.getFieldInformation(dstWorkList.get(ch).getFieldId());
                            dialogMessage.append("　 [");
                            dialogMessage.append(header.getName());
                            dialogMessage.append("]\n");
                        }
                        /* 処理結果ダイアログを表示 */
                        MessageDialog.openInformation(shell, CleansingNameVariable.PATTERN_PARSING + "終了", dialogMessage.toString());
                    }
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (PartInitException e) {
            e.printStackTrace();
        } finally {
            Kernel.stopSuppressSleep();
        }
        ViewUtil.refreshProjectExplorer(0);
        return null;
    }


    private static final ClaimWorkDataType EXEC_TYPE = ClaimWorkDataType.RESLUT_PATTERN;


    @Override
    ClaimWorkDataType getExecType() {
        return EXEC_TYPE;
    }


    private static List<ICoronaDic> getDictionary(List<ICoronaDicPri> priList) {
        List<ICoronaDic> list = new ArrayList<ICoronaDic>();
        for (ICoronaDicPri pri : priList) {
            /* 辞書を設定　 */
            if (!pri.isInActive()) {
                list.add(IoActivator.getService().getDictionary(pri.getDicId()));
            }
        }
        return list;
    }


    private void stepFields(IUIProduct uiProduct, IClaimWorkData cwd, boolean hitFlag) throws InvocationTargetException, InterruptedException,
            PartInitException {
        // 選択しているターゲットを取得
        IUIWork uiWork = CoronaModel.INSTANCE.getWork(uiProduct, cwd);

        // 結果を表示する画面のEditorInput
        PatternMatcher matcher;
        matcher = new PatternMatcherForUI(uiProduct, uiWork, null, checkedDics, hitFlag);

        NewProgress newProgress = new NewProgress();
        newProgress.addRunnable(matcher);

        // 構文パターン処理の実行
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        long s = System.currentTimeMillis();

        dialog.run(true, true, newProgress);

        CoronaActivator.debugLog("構文解析時間： " + (System.currentTimeMillis() - s) + "mSec");
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


    List<ICoronaDic> getDics(IUIProduct uiProduct) {
        ICoronaProduct product = uiProduct.getObject();
        List<ICoronaDic> dics = product.getDictionarys(ICoronaDic.class);

        ICoronaProject project = IoActivator.getService().getProject(product.getProjectId());
        dics.addAll(project.getDictionarys(ICoronaDic.class));

        return dics;
    }

    static class NewProgress implements IRunnableWithProgress {
        List<IRunnableWithProgress> runs = new ArrayList<IRunnableWithProgress>();


        public void addRunnable(IRunnableWithProgress runnable) {
            if (runnable != null) {
                runs.add(runnable);
            }
        }


        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            for (IRunnableWithProgress run : runs) {
                run.run(monitor);
            }
        }
    }
}
