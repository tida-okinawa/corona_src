/**
 * @version $Id: CleansingMorphemeHandler.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/08/09 16:02:33
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.CleansingNameVariable;
import com.tida_okinawa.corona.common.ILogger;
import com.tida_okinawa.corona.correction.common.ExternalProgramExitException;
import com.tida_okinawa.corona.correction.common.FileUtil;
import com.tida_okinawa.corona.correction.morphem.compile.DicCompileExecution;
import com.tida_okinawa.corona.correction.morphem.compile.JumanDicTransfer;
import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;
import com.tida_okinawa.corona.internal.ui.util.IFileUtil;
import com.tida_okinawa.corona.internal.ui.util.Kernel;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
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
import com.tida_okinawa.corona.ui.controllers.MorphemeController;
import com.tida_okinawa.corona.ui.controllers.MorphemeControllerForUI;
import com.tida_okinawa.corona.ui.wizards.CleansingWizard;

/**
 * @author takayuki-matsumoto, imai
 */
public class CleansingMorphemeHandler extends AbstractCleansingHandler {
    boolean compileSucceed = false;
    List<ICoronaDic> checkedDics;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        super.execute(event);
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
                        IClaimWorkData srcWork = null;
                        for (IClaimWorkData work : srcWorkList) {
                            if ((work.getClaimId() == claimId) && (work.getFieldId() == fieldId)) {
                                srcWork = work;
                                break;
                            }
                        }
                        if (!(srcWork != null && product.getId() == srcWork.getProductId())) {
                            continue;
                        }

                        /* クレンジング対象のワークデータがあった場合 */
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
                            // 辞書を取得
                            checkedDics = getDictionary(wizard.getFieldDicPriorityList(dstWork));
                        } else {
                            /* 共通の場合 */
                            checkedDics = getDictionary(wizard.getDicPriorityList());
                        }

                        try {
                            step1(uiProduct); // 辞書コンパイル
                            if (compileSucceed) {
                                step2Fields(uiProduct, srcWork); // 形態素・係り受け解析実行
                            }
                        } catch (InterruptedException e1) {
                            IClaimWorkData cwd = product.getClaimWorkData(claimId, EXEC_TYPE, fieldId);
                            if (cwd != null) {
                                /*
                                 * 失敗したので処理日時が入っていない。日時を入れるためにupdate。
                                 * Memo ただし、処理前に前回結果を破棄していないので、前回の結果と混ざる恐れあり。
                                 */
                                cwd.update();
                            }
                        }
                        if (dstWork != null && dstWork.getWorkdataId() == 0 && dstWork.getLasted() != null) {
                            // 処理結果後のクレームワークデータでマップを置き換える
                            // Memo dstWorkを作成した時、ターゲットに追加してしまえば置き換えずに済むのでは？
                            IClaimWorkData cwd = product.getClaimWorkData(claimId, EXEC_TYPE, fieldId);
                            wizard.updateDicPriMap(dstWork, cwd);
                        }
                    }
                }

                /*
                 * 保存処理
                 * IClaimWorkDataがDBに保存されてからでないと、workId==0により、優先度を保存できないことがあるため、
                 * 処理が終わってから保存
                 */
                wizard.saveDicPriList();

                // TODO dstWorkListじゃなくて、実際に処理したIClaimWorkDataの一覧から取得する
                if (dstWorkList.size() > 0) {
                    IClaimWorkData targetWork = dstWorkList.get(dstWorkList.size() - 1);
                    IClaimWorkData claimWorkData = getClaimWorkData(uiProduct, EXEC_TYPE, targetWork.getFieldId(), targetWork.getClaimId());
                    if (claimWorkData != null && claimWorkData.getLasted() != null) {
                        /* ダイアログメッセージ用に処理した列名を取得 */
                        StringBuilder dialogMessage = new StringBuilder();
                        dialogMessage.append(CleansingNameVariable.MORPH_DEPEND);
                        dialogMessage.append("が終了しました。\n\n対象列:\n");
                        for (int ch = 0; ch < (dstWorkList.size()); ch++) {
                            IClaimData claimData = IoActivator.getService().getClaimData(claimWorkData.getClaimId());
                            IFieldHeader header = claimData.getFieldInformation(dstWorkList.get(ch).getFieldId());
                            dialogMessage.append("　 [");
                            dialogMessage.append(header.getName());
                            dialogMessage.append("]\n");
                        }
                        /* 処理結果ダイアログを表示 */
                        MessageDialog.openInformation(shell, CleansingNameVariable.MORPH_DEPEND + "終了", dialogMessage.toString());
                    }
                }
            }
        } catch (InvocationTargetException | PartInitException e) {
            e.printStackTrace(System.err);
            throw new ExecutionException("形態素解析に失敗しました。", e);
        } finally {
            Kernel.stopSuppressSleep();
        }

        ViewUtil.refreshProjectExplorer(0);
        return null;
    }

    private static final ClaimWorkDataType EXEC_TYPE = ClaimWorkDataType.DEPENDENCY_STRUCTURE;


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


    // Step1: 辞書コンパイル
    private void step1(final IUIProduct uiProduct) throws InvocationTargetException, InterruptedException {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();
        ProgressMonitorDialog dialog1 = new ProgressMonitorDialog(shell);
        dialog1.run(true, false, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                compileSucceed = compileDic(uiProduct, monitor);
                ILogger logger = CoronaActivator.getDefault().getLogger();

                /* サーバーモード -> 辞書転送（辞書コンパイル・ターゲット切り替え時）kobayshi */
                JumanDicTransfer dt = new JumanDicTransfer();
                try {
                    dt.dicTrancefer(logger);
                } catch (IOException e1) {
                    e1.printStackTrace(logger.getErrStream());
                }
            }
        });
    }


    private static void step2Fields(IUIProduct uiProduct, IClaimWorkData cwd) throws InvocationTargetException, InterruptedException, PartInitException {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        IUIWork uiWork = CoronaModel.INSTANCE.getWork(uiProduct, cwd);
        MorphemeController controller = new MorphemeControllerForUI(uiProduct, uiWork, null);

        // TODO NewProgressに入れる意味ないよね。一つ入れては一回実行してるし。
        NewProgress newProgress = new NewProgress();
        newProgress.addRunnable(controller);

        Shell shell = window.getShell();
        ProgressMonitorDialog dialog2 = new ProgressMonitorDialog(shell);
        long s = System.currentTimeMillis();
        // 解析実行
        dialog2.run(true, true, newProgress);
        CoronaActivator.debugLog("形態素解析時間：" + (System.currentTimeMillis() - s) + "mSec");

    }


    // Step1: 辞書のエクスポート, コンパイル
    private boolean compileDic(IUIProduct uiProduct, IProgressMonitor monitor) {
        Assert.isLegal(uiProduct.getObject() != null);
        monitor.beginTask("辞書更新エクスポート", 2);

        ICoronaProduct product = uiProduct.getObject();
        ICoronaProject project = IoActivator.getService().getProject(product.getProjectId());

        /* juman 辞書の作成先 */
        File dicDir = MorphemePreference.getJumanDicDir();

        /* 辞書のリスト */
        File[] dicFiles = getDicFiles(dicDir);

        /* プロジェクトの辞書エクスポート */
        String encoding = MorphemePreference.convSJIS() ? "MS932" : "UTF-8";
        project.exportDictionarys(dicDir.getAbsolutePath(), encoding);
        monitor.worked(1);

        /* ターゲットの辞書エクスポート */
        product.exportDictionarys(dicDir.getAbsolutePath(), encoding);
        monitor.worked(1);

        /* product_folder: 辞書ファイルのワークスペース内の退避先 */
        IFolder product_folder = getProductFolder(uiProduct);
        boolean force = !product_folder.getFile("jumandic.pat").exists();
        // note: ワークスペースに退避したjumandic.patがなければ、dic の更新がなくても作成する

        /* 辞書更新 */
        ILogger logger = CoronaActivator.getDefault().getLogger();
        DicCompileExecution ce = new DicCompileExecution(logger, monitor);
        try {
            boolean isUpdated = ce.compile(dicFiles, force);

            /* Jumanが参照する辞書ファイル */
            File[] juman_dic_files = ce.getJumanDicFiles();

            if (isUpdated) {
                // 辞書を更新した -> juman/dic と ワークスペースに退避
                IFileUtil.copy(product_folder, juman_dic_files);
            } else {
                // 辞書の更新なし -> ワークスペースに退避してあるものを juman/dicに写す
                assert (!force);
                for (int i = 0; i < juman_dic_files.length; i++) {
                    File to = juman_dic_files[i];
                    IFile from = product_folder.getFile(to.getName());

                    // ハッシュをチェックして同じファイルならコピー不要
                    byte[] toMD5 = new byte[0];
                    if (to.exists()) {
                        toMD5 = FileUtil.calcMD5(to);
                    }
                    byte[] fromMD5 = FileUtil.calcMD5(from.getContents());

                    if (!Arrays.equals(fromMD5, toMD5)) {
                        InputStream is = from.getContents();
                        try {
                            FileUtil.copy(to, is);
                        } finally {
                            is.close();
                        }
                    }
                }
            }
            return true;
        } catch (ExternalProgramExitException e) {
            logger.getErrStream().println("辞書の作成に失敗しました:" + e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.getErrStream().println("ファイルが見つかりません:" + e);
            e.printStackTrace();
        } catch (InterruptedException | IOException | CoreException e) {
            e.printStackTrace();
        }
        return false;
    }


    File[] getDicFiles(File dicDir) {
        File[] dicFiles = new File[checkedDics.size()];
        for (int i = 0; i < checkedDics.size(); i++) {
            ICoronaDic dic = checkedDics.get(i);
            File ddic = new File(dicDir, dic.getName());
            dicFiles[i] = FileUtil.transPathExtension(ddic, "dic");
        }
        return dicFiles;
    }


    /**
     * ワークスペース内の「ターゲット」フォルダーを取得
     * 
     * @param product
     * @return
     */
    IFolder getProductFolder(IUIProduct uiProduct) {
        IFolder folder = uiProduct.getResource();
        try {
            folder.refreshLocal(IResource.DEPTH_ONE, null);
        } catch (CoreException e) {
            //
        }
        return folder;
    }


    /**
     * {@link IFile} to {@link File}
     * 
     * @param file
     * @return
     */
    File toFile(IFile file) {
        IPath path = file.getLocation();
        String ospath = path.toOSString();
        return new File(ospath);
    }


    @Override
    public boolean isEnabled() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }
        /* WorkData以外 */
        IStructuredSelection ss = (IStructuredSelection) window.getActivePage().getSelection();
        IUIElement target = null;
        if (ss != null) {
            for (Object element : ss.toArray()) {
                /* 複数フィールド選択時は、親オブジェクトが共通以外、またはWorkDataTypeが異なる場合は拒否 */
                if (target != null) {
                    if (element instanceof IUIElement) {
                        if (((IUIElement) element).getParent() != target.getParent()) {
                            return false;
                        } else if (element instanceof IUIWork) {
                            return super.isEnabled();
                        }
                    }
                } else {
                    if (element instanceof IUIElement) {
                        target = (IUIElement) element;
                    } else {
                        return false;
                    }
                }
            }
        }

        return super.isEnabled();
    }


    @Override
    protected boolean isEnabled(ClaimWorkDataType type) {
        switch (type) {
        case CORRECTION_MISTAKES:
            return true;
        }
        return false;
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