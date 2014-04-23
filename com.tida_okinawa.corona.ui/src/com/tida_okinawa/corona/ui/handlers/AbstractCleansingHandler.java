/**
 * @version $Id: AbstractCleansingHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/11 20:02:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.internal.ui.CoronaConstants;
import com.tida_okinawa.corona.internal.ui.views.model.ICorrectionFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUICorrectionFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.ui.controllers.ClaimWorkDataController;

/**
 * クレンジング実行の共通クラス
 * 
 * @author imai
 * 
 */
abstract public class AbstractCleansingHandler extends AbstractHandler {
    protected List<IClaimWorkData> dstWorkList;
    protected List<IClaimWorkData> srcWorkList;
    protected ClaimWorkDataType targetType = ClaimWorkDataType.NONE;

    private static final String ProjectExplorerId = "org.eclipse.ui.navigator.ProjectExplorer"; //$NON-NLS-1$


    /**
     * 指定された種別の解析結果を取得する
     * 
     * @param product
     *            解析ターゲット
     * @param claimId
     *            解析問い合わせデータID
     * @param fieldId
     *            解析フィールドID
     * @param type
     *            取得する処理の種別
     * @return 指定されたワークデータを返す。（ターゲットのマイニング対象のみ））
     */
    final private static IClaimWorkData getWorkData(ICoronaProduct product, int claimId, int fieldId, ClaimWorkDataType type) {
        IClaimWorkData ret = product.getClaimWorkData(claimId, type, fieldId);
        if (ret == null) {
            ret = IoActivator.getModelFactory().createClaimWorkData(claimId, fieldId, type, product.getProjectId(), product.getId());
        }
        return ret;
    }


    /**
     * 指定された種別の処理結果をすべて取得する
     * 
     * @param product
     *            解析ターゲット
     * @param type
     *            取得する処理の種別
     * @return 指定されたタイプのワークデータを返す。（ターゲットのマイニング対象のみ）
     */
    final private static List<IClaimWorkData> getWorkList(ICoronaProduct product, ClaimWorkDataType type) {
        List<IClaimWorkData> ret = new ArrayList<IClaimWorkData>();
        for (IClaimData claim : product.getClaimDatas()) {
            int claimId = claim.getId();
            Collection<Integer> fields = product.getMiningFields(claimId);
            for (Integer fieldId : fields) {
                ret.add(getWorkData(product, claimId, fieldId, type));
            }
        }
        return ret;
    }


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        /* 処理の入力になるデータと、出力先の処理結果を取得 */
        ISelection sel = HandlerUtil.getActiveMenuSelection(event);
        ClaimWorkDataType dtype = ClaimWorkDataType.NONE;
        if (this instanceof CleansingMorphemeHandler) {
            dtype = ClaimWorkDataType.CORRECTION_MISTAKES;
        } else if (this instanceof CleansingSynonymHandler || this instanceof CleansingSyntaxHandler) {
            dtype = targetType;
        } else if (this instanceof FrequentTermHandler) {
            dtype = ClaimWorkDataType.LASTED_EXEC;
        }

        srcWorkList = new ArrayList<IClaimWorkData>();
        dstWorkList = new ArrayList<IClaimWorkData>();

        for (Object o : ((IStructuredSelection) sel).toArray()) {
            if (o instanceof IUICorrectionFolder) { // 処理結果フォルダ
                // 該当問い合わせデータのフィールド群
                IClaimWorkData last = getLastClaimWorkData((IUICorrectionFolder) o);
                ICoronaProduct product = getProduct(o).getObject();
                Set<Integer> flds = product.getMiningFields(last.getClaimId());
                for (Integer fld : flds) {
                    IClaimWorkData work = product.getClaimWorkData(last.getClaimId(), dtype, fld);
                    if (work != null) {
                        srcWorkList.add(work);
                        dstWorkList.add(getWorkData(product, last.getClaimId(), fld, getExecType()));
                    } else {
                        // エラーメッセージ
                        CoronaActivator
                                .getDefault()
                                .getLogger()
                                .getOutStream()
                                .println(
                                        Messages.bind(Messages.CleansingHandler_ErrorMessage_NoInputData_StartsWithFolder, last.getClaimId(), last.getFieldId()));
                    }
                }
            } else if (o instanceof IUIProduct) {
                ICoronaProduct product = ((IUIProduct) o).getObject();
                IClaimWorkData work = null;
                for (IClaimData clm : product.getClaimDatas()) {
                    int clmId = clm.getId();
                    for (Integer i : product.getMiningFields(clmId)) {
                        work = product.getClaimWorkData(clmId, dtype, i);
                        if (work == null) {
                            break;
                        }
                    }
                    if (work == null) {
                        break;
                    }
                }
                if (work != null) {
                    srcWorkList.addAll(getWorkList(product, dtype));
                    dstWorkList.addAll(getWorkList(product, getExecType()));
                } else {
                    // エラーメッセージ
                    CoronaActivator.getDefault().getLogger().getOutStream()
                            .println(Messages.bind(Messages.CleansingHandler_ErrorMessage_NoInputData_StartsWithTarget, product.getName()));
                }
            } else if (o instanceof IUIWork) {
                IUIWork work = (IUIWork) o;
                ICoronaProduct product = getProduct(o).getObject();
                srcWorkList.add(work.getObject());
                dstWorkList.add(getWorkData(product, work.getObject().getClaimId(), work.getObject().getFieldId(), getExecType()));
            }
        }
        return null;
    }


    abstract ClaimWorkDataType getExecType();


    /**
     * 対象のターゲット {@link IUIProduct} を取得
     * 
     * @param target
     *            解析開始時に、プロジェクトエクスプローラ上で選択していたアイテム
     * @return 解析するターゲット
     */
    final protected static IUIProduct getProduct(Object target) {
        // 選択対象を取得
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        ProjectExplorer projExpl = (ProjectExplorer) page.findView(ProjectExplorerId);
        if (projExpl == null) {
            return null;
        }
        if (target != null) {
            if (target instanceof IUICorrectionFolder) {
                // 処理結果フォルダを選択している場合
                return (IUIProduct) ((IUICorrectionFolder) target).getParent();

            } else if (target instanceof IUIProduct) {
                // ターゲットを選択している場合の場合
                return (IUIProduct) target;
            } else if (target instanceof IUIWork) {
                // 中間データを選択している場合
                // 親をたどってICoronaProductを探す
                IUIWork work = (IUIWork) target;
                IUIContainer parent = work.getParent();
                while (parent != null) {
                    if (parent instanceof IUIProduct) {
                        return (IUIProduct) parent;
                    }
                    parent = parent.getParent();
                }
            }
            return null;

        }
        // ターゲット・中間データを選択していない
        // isEnable() でチェックしているのでありえない
        return null;
    }


    final protected static Set<IUIProduct> getProducts() {
        Set<IUIProduct> list = new HashSet<IUIProduct>();

        // 選択対象を取得
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        ProjectExplorer projExpl = (ProjectExplorer) page.findView(ProjectExplorerId);
        if (projExpl == null) {
            return null;
        }
        ITreeSelection ss = (ITreeSelection) projExpl.getCommonViewer().getSelection();
        for (Object o : ss.toArray()) {
            if (o instanceof IUICorrectionFolder) {
                // 処理結果フォルダを選択している場合
                list.add((IUIProduct) ((IUICorrectionFolder) o).getParent());
            } else if (o instanceof IUIProduct) {
                // ターゲットを選択している場合の場合
                list.add((IUIProduct) o);
            } else if (o instanceof IUIWork) {
                // 中間データを選択している場合
                // 親をたどってICoronaProductを探す
                IUIWork work = (IUIWork) o;
                IUIContainer parent = work.getParent();
                while (parent != null) {
                    if (parent instanceof IUIProduct) {
                        list.add((IUIProduct) parent);
                        break;
                    }
                    parent = parent.getParent();
                }
            }
        }
        return list;
    }


    /**
     * 選択している中間データ(ターゲットを選択している場合は最新の中間データ）を取得
     * 
     * @return 解析する入力データ
     */
    final protected static IUIWork getUIWork() {
        // 選択対象を取得
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        ProjectExplorer projExpl = (ProjectExplorer) page.findView(ProjectExplorerId);
        if (projExpl == null) {
            return null;
        }
        ITreeSelection ss = (ITreeSelection) projExpl.getCommonViewer().getSelection();
        for (Object o : ss.toArray()) {
            if (o instanceof IUICorrectionFolder) {
                // 処理結果フォルダを選択している場合
                IClaimWorkData work = getLastClaimWorkData((IUICorrectionFolder) o);
                return CoronaModel.INSTANCE.getWork(getProduct(o), work);
            } else if (o instanceof IUIProduct) {
                // ターゲットを選択している場合
                ICoronaProduct product = getProduct(o).getObject();
                IClaimWorkData work = getLastClaimWorkData(product);
                return CoronaModel.INSTANCE.getWork(getProduct(o), work);
            } else if (o instanceof IUIWork) {
                return (IUIWork) o;
            }
        }
        // 例外
        return null;
    }


    private static IClaimWorkData getLastClaimWorkData(IUICorrectionFolder element) {
        if (element == null) {
            return null;
        }
        ICorrectionFolder folder = (ICorrectionFolder) element.getObject();
        List<IClaimData> claims = ((ICoronaProduct) element.getParent().getObject()).getClaimDatas();
        IClaimWorkData lasted = null;
        for (IClaimData claim : claims) {
            // 問い合わせデータ名称で比較
            if (folder.getName().equals(CoronaConstants.createCorrectionFolderName(claim.getName()))) {
                for (Integer fieldNo : claim.getCorrectionMistakesFields()) {
                    IClaimWorkData workData = ((ICoronaProduct) element.getParent().getObject()).getClaimWorkData(claim.getId(), ClaimWorkDataType.LASTED_EXEC,
                            fieldNo);
                    if (workData == null) {
                        continue;
                    }
                    if (lasted == null) {
                        lasted = workData;
                    } else {
                        if (lasted.getLasted().before(workData.getLasted())) {
                            lasted = workData;
                        }
                    }
                }
            }
        }
        return lasted;
    }


    /**
     * ターゲットのなかで最後に作成/更新した処理結果を取得
     * 
     * @param product
     *            処理結果を取得するターゲット
     * @return 最後に作成/更新した処理結果
     */
    private static IClaimWorkData getLastClaimWorkData(ICoronaProduct product) {
        if (product == null) {
            return null;
        }
        ICoronaProject coronaProject = IoActivator.getService().getProject(product.getProjectId());
        List<IClaimData> claims = coronaProject.getClaimDatas();
        IClaimWorkData lasted = null;
        for (IClaimData claim : claims) {
            int claimId = claim.getId();
            Set<Integer> fields = product.getMiningFields(claimId);
            for (Integer fieldNo : fields) {
                IClaimWorkData workData = product.getClaimWorkData(claimId, ClaimWorkDataType.LASTED_EXEC, fieldNo);
                if (lasted == null) {
                    lasted = workData;
                } else {
                    if (lasted.getLasted().before(workData.getLasted())) {
                        lasted = workData;
                    }
                }
            }
        }
        return lasted;
    }


    private static ClaimWorkDataType getWorkDataType() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return ClaimWorkDataType.NONE;
        }

        IStructuredSelection ss = (IStructuredSelection) window.getActivePage().getSelection();
        IUIElement target = null;
        IClaimWorkData lasted = null;
        for (Object element : ss.toArray()) {
            // 複数フィールド選択時は、親オブジェクトが共通以外、またはWorkDataTypeが異なる場合は拒否
            if (target != null) {
                if (element instanceof IUIElement) {
                    if (((IUIElement) element).getParent() != target.getParent()) {
                        return ClaimWorkDataType.NONE;
                    } else if (element instanceof IUIWork) {
                        if (((IUIWork) element).getObject().getClaimWorkDataType() != ((IUIWork) target).getObject().getClaimWorkDataType()) {
                            return ClaimWorkDataType.NONE;
                        }
                    }
                }
            } else {
                if (element instanceof IUIElement) {
                    target = (IUIElement) element;
                } else {
                    return ClaimWorkDataType.NONE;
                }
            }
            IClaimWorkData workData = null;
            if (element instanceof IUICorrectionFolder) { // 処理結果フォルダ
                workData = getLastClaimWorkData((IUICorrectionFolder) element);
                if (workData == null) {
                    return ClaimWorkDataType.NONE;
                }
            } else if (element instanceof IUIProduct) {
                /* 最後に実行した処理(Lasted)に応じて判断する */
                workData = getLastClaimWorkData(((IUIProduct) element).getObject());
                if (workData == null) {
                    return ClaimWorkDataType.NONE;
                }
            } else if (element instanceof IUIWork) {
                workData = ((IUIWork) element).getObject();
                if (workData == null) {
                    return ClaimWorkDataType.NONE;
                }
            }
            if (lasted == null) {
                lasted = workData;
            } else {
                // WorkDataTypeをローレベルに集約する
                if (lasted.getLasted().before(workData.getLasted())) {
                    lasted = workData;
                }
            }
        }
        return lasted.getClaimWorkDataType();
    }


    @Override
    public boolean isEnabled() {
        targetType = getWorkDataType();
        return isEnabled(targetType);
    }


    protected final static boolean isProductSelected() {
        ITreeSelection ss = (ITreeSelection) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
        return (ss.getFirstElement() instanceof IUIProduct);
    }


    /**
     * 解析処理の入力データにできるタイプかどうか判定する
     * 
     * @param type
     *            これから行う処理の種別
     * @return trueなら入力データにできる
     */
    protected boolean isEnabled(ClaimWorkDataType type) {
        return false;
    }


    /**
     * 出力する中間データを取得
     * {@link ClaimWorkDataController#run(org.eclipse.core.runtime.IProgressMonitor)}
     * 実行後に作ること
     * 
     * @param product
     *            解析ターゲット
     * @param type
     *            出力する解析結果の種別
     * @return 出力する解析結果の受け皿
     */
    protected static final IClaimWorkData getClaimWorkData(IUIProduct product, ClaimWorkDataType type) {
        if (product != null) {
            for (IClaimWorkData claimWorkData : product.getObject().getClaimWorkDatas()) {
                if (claimWorkData.getClaimWorkDataType() == type) {
                    return claimWorkData;
                }
            }
        }
        return null;
    }


    protected IClaimWorkData getClaimWorkData(IUIProduct product, ClaimWorkDataType type, int fieldId) {
        for (IClaimWorkData work : product.getObject().getClaimWorkDatas()) {
            if ((work.getClaimWorkDataType() == type) && (work.getFieldId() == fieldId)) {
                return work;
            }
        }
        return null;
    }


    protected IClaimWorkData getClaimWorkData(IUIProduct product, ClaimWorkDataType type, int fieldId, int claimId) {
        for (IClaimWorkData work : product.getObject().getClaimWorkDatas()) {
            if ((work.getClaimWorkDataType() == type) && (work.getFieldId() == fieldId) && (work.getClaimId() == claimId)) {
                return work;
            }
        }
        return null;
    }

}
