/**
 * @version $Id: CleansingSynonymHandler.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/08/09 16:22:54
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
import org.eclipse.ui.IWorkbenchWindow;
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
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.ui.ViewUtil;
import com.tida_okinawa.corona.ui.controllers.SynonymController;
import com.tida_okinawa.corona.ui.controllers.SynonymControllerForUI;
import com.tida_okinawa.corona.ui.wizards.CleansingWizard;

/**
 * 同義語補正実行・結果表示
 * 
 * @author takayuki-matsumoto, imai
 */
public class CleansingSynonymHandler extends AbstractCleansingHandler {

    List<ICoronaDic> checkedDics;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        super.execute(event);
        /* ウィザードを表示　 */
        Shell shell = HandlerUtil.getActiveShell(event);

        IClaimWorkData data = getUIWork().getObject();
        if (data == null) {
            CoronaActivator.debugLog("ICoronaObjectをUIWorkから取得できませんでした");
            throw new IllegalStateException("ゆらぎ・同義語補正処理に必要な入力データが取得できませんでした");
        }

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
                List<IClaimData> claims = uiProduct.getObject().getClaimDatas();
                ICoronaProduct product = uiProduct.getObject();

                for (IClaimData claim : claims) {
                    int claimId = claim.getId();
                    Collection<Integer> fieldIds = product.getMiningFields(claimId);
                    for (Integer fieldId : fieldIds) {
                        IClaimWorkData srcWork = null;
                        /* クレームワークデータを探す */
                        for (IClaimWorkData work : srcWorkList) {
                            if (work.getClaimId() == claimId && work.getFieldId() == fieldId) {
                                srcWork = work;
                                break;
                            }
                        }
                        if (!(srcWork != null && product.getId() == srcWork.getProductId())) {
                            continue;
                        }

                        /* 処理対象のデータが存在する */
                        IClaimWorkData dstWork = null;
                        if (wizard.isFieldSelect()) {
                            /* フィールド毎の指定あり */
                            for (IClaimWorkData work : dstWorkList) {
                                if ((work.getClaimId() == claimId) && (work.getFieldId() == fieldId)) {
                                    dstWork = work;
                                    break;
                                }
                            }
                            if (dstWork == null) {
                                /* 対応する辞書リストが取得できない場合、後続の処理を行わない */
                                continue;
                            }
                            /* 辞書を取得 */
                            checkedDics = getDictionary(wizard.getFieldDicPriorityList(dstWork));
                        } else {
                            /* 共通の場合 */
                            checkedDics = getDictionary(wizard.getDicPriorityList());
                        }
                        try {
                            stepFields(uiProduct, srcWork); // 同義語補正実行
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                            IClaimWorkData cwd = product.getClaimWorkData(claimId, EXEC_TYPE, fieldId);
                            if (cwd != null) {
                                /*
                                 * 失敗したので日付情報が入っていない。日付を入れるためにupdate。
                                 * これで、途中経過までのデータが表示できる。
                                 * TODO ただし、処理前に前回結果を破棄しないので処理結果が混ざる可能性あり。
                                 * ウィザードで辞書が選択されていないか、辞書が空のときは実行できなくしたので、
                                 * 全部が前の結果にはならないはず
                                 */
                                cwd.update();
                            }
                        }
                        if (dstWork != null && dstWork.getWorkdataId() == 0 && dstWork.getLasted() != null) {
                            /* 処理結果後のクレームワークデータでマップを置き換える */
                            // Memo dstWorkを作成した時、ターゲットに追加してしまえば置き換えずに済むのでは？
                            IClaimWorkData cwd = product.getClaimWorkData(claimId, EXEC_TYPE, fieldId);
                            wizard.updateDicPriMap(dstWork, cwd);
                        }

                    }
                }
                /* 保存処理 */
                wizard.saveDicPriList();

                // TODO dstWorkListじゃなくて、実際に処理したIClaimWorkDataの一覧から取得する
                if (dstWorkList.size() > 0) {
                    IClaimWorkData targetWork = dstWorkList.get(dstWorkList.size() - 1);
                    IClaimWorkData work = getClaimWorkData(uiProduct, EXEC_TYPE, targetWork.getFieldId(), targetWork.getClaimId());
                    if (work != null && work.getLasted() != null) {
                        /* ダイアログメッセージ用に処理した列名を取得 */
                        StringBuilder dialogMessage = new StringBuilder();
                        dialogMessage.append(CleansingNameVariable.FLUC_SYNONYM);
                        dialogMessage.append("が終了しました。\n\n対象列:\n");
                        for (int ch = 0; ch < (dstWorkList.size()); ch++) {
                            IClaimData claimData = IoActivator.getService().getClaimData(work.getClaimId());
                            IFieldHeader header = claimData.getFieldInformation(dstWorkList.get(ch).getFieldId());
                            dialogMessage.append("　 [");
                            dialogMessage.append(header.getName());
                            dialogMessage.append("]\n");
                        }
                        /* 処理結果ダイアログを表示 */
                        MessageDialog.openInformation(shell, CleansingNameVariable.FLUC_SYNONYM + "終了", dialogMessage.toString());
                    } else {
                        CoronaActivator.debugLog("workdata is null");
                    }
                }
            }
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (PartInitException e) {
            e.printStackTrace();
        } finally {
            Kernel.stopSuppressSleep();
        }
        ViewUtil.refreshProjectExplorer(0);
        return null;
    }

    private static final ClaimWorkDataType EXEC_TYPE = ClaimWorkDataType.CORRECTION_SYNONYM;


    @Override
    ClaimWorkDataType getExecType() {
        return EXEC_TYPE;
    }


    private static List<ICoronaDic> getDictionary(List<ICoronaDicPri> priList) {
        List<ICoronaDic> list = new ArrayList<ICoronaDic>();
        /* 後に渡したものが優先されるため、辞書を逆から設定　 */
        for (int i = priList.size(); i > 0; i--) {
            ICoronaDicPri pri = priList.get(i - 1);
            if (!pri.isInActive()) {
                list.add(IoActivator.getService().getDictionary(pri.getDicId()));
            }
        }

        return list;
    }


    private void stepFields(IUIProduct uiProduct, IClaimWorkData cwd) throws InvocationTargetException, InterruptedException, PartInitException {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        IUIWork uiWork = CoronaModel.INSTANCE.getWork(uiProduct, cwd);
        SynonymController controller = new SynonymControllerForUI(uiProduct, uiWork, null, checkedDics);

        NewProgress newProgress = new NewProgress();
        newProgress.addRunnable(controller);

        Shell shell = window.getShell();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);

        /* 同義語補正実行 */
        long s = System.currentTimeMillis();
        dialog.run(true, true, newProgress);
        CoronaActivator.debugLog("ゆらぎ・同義語補正時間：    " + (System.currentTimeMillis() - s) + "mSec");
    }


    @Override
    protected boolean isEnabled(ClaimWorkDataType type) {
        switch (type) {
        case DEPENDENCY_STRUCTURE:
            return true;
        default:
            return false;
        }
    }


    List<ICoronaDic> getDics(IUIProduct uiProduct) {
        // 同義語辞書 ターゲット + プロジェクト共通
        ICoronaProduct product = uiProduct.getObject();
        List<ICoronaDic> dics = product.getDictionarys(ISynonymDic.class);
        dics.addAll(product.getDictionarys(IFlucDic.class));
        ICoronaProject project = IoActivator.getService().getProject(product.getProjectId());
        dics.addAll(project.getDictionarys(ISynonymDic.class));
        dics.addAll(project.getDictionarys(IFlucDic.class));

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
