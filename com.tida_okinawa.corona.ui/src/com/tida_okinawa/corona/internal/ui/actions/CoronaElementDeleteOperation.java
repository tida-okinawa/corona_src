/**
 * @version $Id: CoronaElementDeleteOperation.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/30 10:20:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.internal.ui.views.model.IUIClaim;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibrary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaDics;
import com.tida_okinawa.corona.io.model.ICoronaObject;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.ViewUtil;
import com.tida_okinawa.corona.ui.editors.AbstractDicEditor;
import com.tida_okinawa.corona.ui.editors.LabelDicEditor;
import com.tida_okinawa.corona.ui.editors.user.UserDicEditor;

/**
 * @author kousuke-morishima
 */
public class CoronaElementDeleteOperation extends AbstractOperation {

    private IUIElement[] deleteObjects;


    /**
     * @param deleteObjects
     * @param label
     *            名前？
     */
    public CoronaElementDeleteOperation(IUIElement[] deleteObjects, String label) {
        super(label);
        this.deleteObjects = deleteObjects;
        /* 子があるユーザ辞書の消し漏れをなくすため、子になりうるものでソートする */
        Arrays.sort(deleteObjects, new Comparator<IUIElement>() {
            int category(IUIElement o) {
                if (o instanceof IUIProduct) {
                    return 10;
                } else if (o instanceof IUIClaim) {
                    return 10;
                } else if (o instanceof IUIProject) {
                    return 0;
                } else if (o instanceof IUIDictionary) {
                    ICoronaDic dic = ((IUIDictionary) o).getObject();
                    if (dic instanceof IUserDic) {
                        return 25;
                    }
                    return 20;
                } else {
                    return 99;
                }
            }


            @Override
            public int compare(IUIElement o1, IUIElement o2) {
                int cat = category(o1) - category(o2);
                if (cat == 0) {
                    /*
                     * 同じ辞書が複数のターゲットに登録されているとき、ひとつ消したらほかのターゲット下の辞書も確実に消さないといけない。
                     * 同じ辞書を一塊として処理しやすくするため、同じ辞書を指しているUIElementは連続させておく。
                     */
                    if ((o1 instanceof IUIDictionary) && (o2 instanceof IUIDictionary)) {
                        ICoronaDic d1 = ((IUIDictionary) o1).getObject();
                        ICoronaDic d2 = ((IUIDictionary) o2).getObject();
                        if (d1.getId() != d2.getId()) {
                            return d1.getName().compareTo(d2.getName());
                        }
                    }
                    return 0;
                }
                return cat;
            }
        });
    }


    @Override
    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask(getLabel(), deleteObjects.length);

        if (!monitor.isCanceled()) {
            if (deleteObjects.length > 0) {
                monitor.subTask(deleteObjects[0].getResource().getName());

                ICoronaObject prevObject = deleteObjects[0].getObject();
                for (IUIElement object : deleteObjects) {
                    /*
                     * 同じICoronaObjectを指すUIElementの削除中はキャンセルしたくないので、
                     * 処理対象のICoronaObjectが切り替わったタイミングでキャンセルされたか見に行く
                     */
                    ICoronaObject curObject = object.getObject();
                    if ((prevObject == null) || !prevObject.equals(curObject)) {
                        if (monitor.isCanceled()) {
                            break;
                        }
                        prevObject = curObject;
                        monitor.subTask(object.getResource().getName());
                    }

                    delete(object);
                    monitor.worked(1);
                }
            }
        }
        monitor.done();
        return stat;
    }

    private Set<IStatus> uniqueCheck = new HashSet<IStatus>();
    private MultiStatus stat = new MultiStatus(UIActivator.PLUGIN_ID, 332, "", null);


    private void addResult(IStatus result) {
        if (uniqueCheck.add(result)) {
            stat.add(result);
        }
    }

    /**
     * 削除アイテムと一緒に消えるべきアイテムのリソース
     */
    List<IResource> deleteResources;


    private void delete(final IUIElement uiElem) {
        /*
         * 削除順番
         * １．ICoronaObject
         * - IResourceを消した結果、子がいなくなるような場合にアイテムが消えずに残るため、先に消す
         * ２．IResource
         * ３．Adapter
         * - IResourceを消すとき、Adapterの情報を使用するので、後で消す。
         */

        deleteResources = new ArrayList<IResource>();
        /* ICoronaObject */
        IStatus result = Status.OK_STATUS;
        if (uiElem instanceof IUIDictionary) {
            result = delete((IUIDictionary) uiElem);
        } else if (uiElem instanceof IUIProject) {
            result = delete((IUIProject) uiElem);
        } else if (uiElem instanceof IUIProduct) {
            result = delete((IUIProduct) uiElem);
        } else if (uiElem instanceof IUIWork) {
            result = delete((IUIWork) uiElem);
        } else if (uiElem instanceof IUIClaim) {
            result = delete((IUIClaim) uiElem);
        }

        if (!result.isOK()) {
            addResult(result);
            return;
        }

        final IStatus status = new MultiStatus(UIActivator.PLUGIN_ID, 4100, "辞書削除", null);
        /* 処理後のStatusの状態を見るので、syncExec */
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                /* ProjectExplorerが更新されるため、UIスレッドで処理する */
                for (IResource refRes : deleteResources) {
                    try {
                        if (refRes instanceof IProject) {
                            /* 閉じているとisAccessibleがfalseになるので開く */
                            if (!((IProject) refRes).isOpen()) {
                                ((IProject) refRes).open(null);
                            }
                        }
                        if (isDeleteAvailable(refRes)) {
                            refRes.delete(true, new NullProgressMonitor());
                        }
                    } catch (CoreException e) {
                        ((MultiStatus) status).add(new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, refRes.getFullPath() + "の削除に失敗しました。", e));
                    }
                }

                // Adapter
                /*
                 * ProjectExplorerの更新前に消えると困るので、こちらもUIスレッドで処理する
                 * IResourceの削除に失敗しても、オブジェクトの関連は消えているのでキャッシュも消す。
                 */
                CoronaModel.INSTANCE.remove(uiElem);
                for (IResource refRes : deleteResources) {
                    CoronaModel.INSTANCE.remove(CoronaModel.INSTANCE.adapter(refRes, false));
                }
            }
        });
        if (!status.isOK()) {
            CoronaActivator.log(status, false);
            /* 削除に失敗した場合、UIアイテムがCoronaObjectのままになってしまうのでリフレッシュする */
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    ViewUtil.refreshProjectExplorer(0);
                }
            });
        }
    }


    boolean isDeleteAvailable(final IResource resource) {
        if (resource == null)
            return false;
        if (!resource.exists())
            return false;
        if (!resource.isAccessible())
            return false;
        return true;
    }


    /* ****************************************
     * 辞書削除のときの処理
     */
    /**
     * 辞書と、ほかに消さなきゃいけないものがある場合はそれも消す。
     * 
     * @param uiDic
     */
    private IStatus delete(IUIDictionary uiDic) {
        IUILibrary uiLib = CoronaModel.INSTANCE.getLibrary(uiDic);
        final ICoronaDic dic = uiDic.getObject();

        if (dic instanceof IUserDic) {
            ICoronaDics dics = uiLib.getObject();
            List<ICoronaDic> childDics = new ArrayList<ICoronaDic>();

            childDics.addAll(AbstractDicEditor.searchChildDictionaries((IUserDic) dic, dics.getDictionarys(ILabelDic.class)));
            childDics.addAll(AbstractDicEditor.searchChildDictionaries((IUserDic) dic, dics.getDictionarys(IFlucDic.class)));
            childDics.addAll(AbstractDicEditor.searchChildDictionaries((IUserDic) dic, dics.getDictionarys(ISynonymDic.class)));

            if (!childDics.isEmpty()) {
                StringBuilder buf = new StringBuilder(128).append(dic.getName()).append("は削除できません。以下の辞書から参照されています");
                for (ICoronaDic child : childDics) {
                    buf.append("\n").append(child.getName());
                }
                /* いろんなところにメッセージを配信 */
                return new DicDeleteStatus(IStatus.WARNING, buf.toString(), uiDic);
            }
        } else if (dic instanceof ILabelDic) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        /* 辞書が削除されるので、編集リスナから外す */
                        for (IEditorReference editorRef : LabelDicEditor.getRelatedUserDicEditor((ILabelDic) dic)) {
                            UserDicEditor editor = (UserDicEditor) editorRef.getEditor(false);
                            editor.removeRelated((ILabelDic) dic);
                        }
                    } catch (PartInitException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        /* 辞書を消す */
        _delete(uiLib, uiDic);
        deleteResources.add(uiDic.getResource());

        return Status.OK_STATUS;
    }


    /**
     * 指定された辞書をただ消すだけ
     * 
     * @param lib
     * @param uiDic
     */
    private static void _delete(IUILibrary lib, IUIDictionary uiDic) {
        if (lib instanceof IUIProject) {
            ICoronaDics dics = lib.getObject();
            if (dics != null) {
                dics.removeDictionary(uiDic.getId());
            }
        } else if (lib instanceof IUIProduct) {
            ICoronaDics dics = lib.getObject();
            if (dics != null) {
                dics.removeDictionary(uiDic.getId());
            }
        }
        IUIContainer parent = uiDic.getParent();
        if (parent != null) {
            parent.modifiedChildren(); /* 子が変わったことを通知 */
        }
    }


    /* ****************************************
     * プロジェクト削除のときの処理
     */
    private IStatus delete(IUIProject uiProject) {
        deleteResources.add(uiProject.getResource());
        return Status.OK_STATUS;
    }


    /* ****************************************
     * ターゲット削除のときの処理
     */
    /**
     * ターゲットと、ほかに消さなきゃいけないものがある場合はそれも消す
     * 
     * @param uiProduct
     */
    private IStatus delete(IUIProduct uiProduct) {
        IUIProject uiProject = CoronaModel.INSTANCE.getProject(uiProduct);
        ICoronaProject project = uiProject.getObject();

        /* ターゲット、および問合せデータを削除する */
        _delete(uiProject, uiProduct);
        deleteResources.add(uiProduct.getResource());

        /* どのターゲットからも参照されていない問合せデータを探す */
        List<IClaimData> original = project.getClaimDatas();
        List<IClaimData> deleteClaims = new ArrayList<IClaimData>(original);
        List<ICoronaProduct> products = project.getProducts();
        for (ICoronaProduct p : products) {
            deleteClaims.removeAll(p.getClaimDatas());
        }
        for (IClaimData claim : deleteClaims) {
            IUIClaim uiClaim = CoronaModel.INSTANCE.getClaim(uiProject, claim);
            _delete(uiProject, uiClaim);
            deleteResources.add(uiClaim.getResource());
        }

        return Status.OK_STATUS;
    }


    /**
     * 指定されたターゲットをただ消すだけ
     * 
     * @param uiProject
     * @param uiProduct
     */
    private static void _delete(IUIProject uiProject, IUIProduct uiProduct) {
        ICoronaProject project = uiProject.getObject();
        ICoronaProduct product = uiProduct.getObject();
        if ((project != null) && (product != null)) {
            project.removeProduct(product);
        }
        uiProject.modifiedChildren();
    }


    /* ****************************************
     * 問合せデータ
     */
    private IStatus delete(IUIClaim uiClaim) {
        IUIProject uiProject = CoronaModel.INSTANCE.getProject(uiClaim.getParent());
        _delete(uiProject, uiClaim);
        deleteResources.add(uiClaim.getResource());
        return Status.OK_STATUS;
    }


    private static void _delete(IUIProject uiProject, IUIClaim uiClaim) {
        ICoronaProject project = uiProject.getObject();
        if (project != null) {
            project.removeClaimData(uiClaim.getId());
        }
        /* 未作成なら通知しなくていいのでfalse */
        IUIContainer parent = uiClaim.getParent();
        if (parent != null) {
            parent.modifiedChildren();
        }
    }


    /* ****************************************
     * 処理結果
     */
    private IStatus delete(IUIWork uiWork) {
        IUIProduct uiProduct = (IUIProduct) CoronaModel.INSTANCE.getUIContainer(IUIProduct.class, uiWork.getParent());
        if (uiProduct != null) {
            IClaimWorkData work = uiWork.getObject();
            if (work != null) {
                uiProduct.getObject().removeClaimWorkData(work);
                deleteResources.add(uiWork.getResource());
                IUIContainer parent = uiWork.getParent();
                if (parent != null) {
                    parent.modifiedChildren();
                }
            }
        }
        return Status.OK_STATUS;
    }

    /* ****************************************
     * 辞書削除結果用IStatus
     */
    private static class DicDeleteStatus extends Status {
        private IUIDictionary uiDic;


        /**
         * @param severity
         * @param pluginId
         * @param message
         * @param targetDic
         *            削除対象の辞書
         */
        public DicDeleteStatus(int severity, String message, IUIDictionary targetDic) {
            super(severity, UIActivator.PLUGIN_ID, message);
            this.uiDic = targetDic;
        }


        @Override
        public int hashCode() {
            return uiDic.hashCode();
        }


        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                return true;
            }

            if (!(obj instanceof DicDeleteStatus)) {
                return false;
            }

            DicDeleteStatus s2 = (DicDeleteStatus) obj;
            if (getSeverity() != s2.getSeverity()) {
                return false;
            }

            ICoronaDic dic = uiDic.getObject();
            if (dic != null) {
                return dic.equals(s2.uiDic.getObject());
            }
            /* 本体が取れないならfalseでいいよ */
            return false;
        }
    }


    /* ****************************************
     * Undo&Redo
     */
    @Override
    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        // TODO しばらくは実装しなくていいと思う
        return null;
    }


    @Override
    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        // TODO しばらくは実装しなくていいと思う
        return null;
    }

}
