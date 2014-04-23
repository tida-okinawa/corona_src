/**
 * @version $Id: DeleteAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/30 9:30:54
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.internal.ui.util.DictionaryPriorityUtil;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibrary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author kousuke-morishima
 */
public class DeleteAction extends SelectionDispatchAction {
    /**
     * プロジェクトビューでの削除処理
     * 
     * @param site
     */
    public DeleteAction(IWorkbenchSite site) {
        super(site);
        setText(Messages.DeleteAction_labelDeleteCommond);
        setImageDescriptor(Icons.INSTANCE.getDescriptor(Icons.IMG_TOOL_DELETE));
    }


    /**
     * 選択している要素および、選択している要素がIUIDictionaryかつターゲット下の辞書であるとき、同じターゲット下にある辞書も一緒に消す
     * このメソッドは、ProjectExplorerでの削除処理でのみ使う
     */
    @Override
    public void run(final IStructuredSelection selection) {
        boolean confirmDelete = confirmDelete(selection);
        if (!confirmDelete) {
            return;
        }
        /*
         * TODO 削除がキャンセルされたときの処理を考える
         * モデルが変更されたときにUI側で通知を受ける仕組みにすれば、
         * CoronaElementDeleteOperationがそもそもICoronaObjectベースの処理にできる
         */
        for (Object obj : selection.toArray()) {
            if (obj instanceof IUIDictionary) {
                /** DBからプライオリティ関連を削除 */
                IUILibrary uiLib1 = CoronaModel.INSTANCE.getLibrary((IUIDictionary) obj);
                DictionaryPriorityUtil.deleteDicPriority(uiLib1, (IUIDictionary) obj);
            }
        }

        /*
         * TODO CoronaElementDeleteOperationをモデルドリブンに書き直す
         * CoronaElementDeleteOperationで、モデルの削除（プロジェクトとターゲットの紐づけを切ったりとか）
         * をしているがこれも微妙では
         * ？
         * モデルの変更をUIがListenerで受けて、それに合わせてファイルを消す方がいい。
         */
        final Set<IUIElement> deleteObjects = new HashSet<IUIElement>(selection.size());
        final Set<IProject> deleteResources = new HashSet<IProject>(selection.size());
        final Job deleteObjectSearchJob = new Job(Messages.DeleteAction_jobSearchDeleteItems) {
            @Override
            public IStatus run(IProgressMonitor monitor) {
                int cnt = 1;
                int total = selection.size();
                monitor.beginTask(Messages.DeleteAction_monitorSearchdeleteItems, total);
                for (Object o : selection.toArray()) {
                    monitor.subTask(new StringBuilder(32).append(cnt++).append("/").append(total).toString()); //$NON-NLS-1$
                    if (o instanceof IUIDictionary) {
                        IUILibrary uiLib = CoronaModel.INSTANCE.getLibrary((IUIDictionary) o);
                        if (uiLib instanceof IUIProduct) {
                            /* ターゲット下の辞書の場合、他の同じターゲットに登録してある辞書も消す */
                            List<IUIElement> tmp = CoronaModel.INSTANCE.adapter(((IUIDictionary) o).getObject());
                            for (Iterator<IUIElement> itr = tmp.iterator(); itr.hasNext();) {
                                IUILibrary uiLib2 = CoronaModel.INSTANCE.getLibrary((IUIDictionary) itr.next());
                                if (!(uiLib2 instanceof IUIProduct)) {
                                    itr.remove();
                                } else if (!uiLib.getResource().getName().equals(uiLib2.getResource().getName())) {
                                    itr.remove();
                                }
                            }
                            deleteObjects.addAll(tmp);
                        } else {
                            deleteObjects.add((IUIElement) o);
                        }
                    } else if (o instanceof IUIElement) {
                        deleteObjects.add((IUIElement) o);
                    } else if (o instanceof IProject) {
                        IUIProject uiProject = (IUIProject) CoronaModel.INSTANCE.adapter((IProject) o, false);
                        if (uiProject != null) {
                            deleteObjects.add(uiProject);
                        } else {
                            deleteResources.add((IProject) o);
                        }
                    }
                    monitor.worked(1);
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        deleteObjectSearchJob.setUser(true);
        deleteObjectSearchJob.schedule();

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /* UI ThreadでjoinするとUIスレッドが止まってしまい、進捗ダイアログが出ない。 */
                    deleteObjectSearchJob.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                /*
                 * プロジェクトを閉じている間にDBからプロジェクトを削すと、CoroEleDelOpeでは消せないのでここで消していたが対応した
                 * 。
                 * 消すのもあれなので一応残しておく。
                 */
                for (IProject project : deleteResources) {
                    try {
                        project.delete(true, null);
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
                runOnly(deleteObjects);
            }
        });
        th.start();
    }


    /**
     * 選択しているIUIElementのみ消す。
     * DBViewからの仕様を想定。
     * 
     * @param selection
     */
    public void runOnly(IStructuredSelection selection) {
        final List<IUIElement> deleteObjects = new ArrayList<IUIElement>(selection.size());
        for (Object o : selection.toArray()) {
            if (o instanceof IUIElement) {
                deleteObjects.add((IUIElement) o);
            }
        }
        runOnly(deleteObjects);
    }


    void runOnly(final Collection<IUIElement> deleteObjects) {
        final Job deleteJob = new Job(Messages.DeleteAction_jobDelete) {
            @Override
            public IStatus run(IProgressMonitor monitor) {
                CoronaElementDeleteOperation op = new CoronaElementDeleteOperation(deleteObjects.toArray(new IUIElement[deleteObjects.size()]),
                        Messages.DeleteAction_monitorDelete);
                try {
                    return PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(op, monitor, null);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    return new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, e.getMessage());
                }
            }
        };

        deleteJob.setUser(true);
        deleteJob.schedule();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /* UI ThreadでjoinするとUIスレッドが止まってしまい、進捗ダイアログが出ない。 */
                    deleteJob.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                /* エラーメッセージをダイアログで表示 */
                getShell().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        IStatus status = deleteJob.getResult();
                        if (!status.isOK()) {
                            StringBuilder message = new StringBuilder(512);
                            if (status.isMultiStatus()) {
                                for (IStatus s : status.getChildren()) {
                                    message.append(s.getMessage()).append("\n"); //$NON-NLS-1$
                                }
                            } else {
                                message.append(status.getMessage());
                            }
                            MessageDialog.openWarning(getShell(), Messages.DeleteAction_labelDelete, message.toString());
                        }
                    };
                });
            }
        }).start();
    }


    @Override
    public void selectionChanged(IStructuredSelection selection) {
        if (selection.size() == 0) {
            setEnabled(false);
            return;
        }
        Object target = null;
        for (Object o : selection.toArray()) {
            /* 異なるアイテムが選択されているとき、メニューを非表示にする */
            if (target == null) {
                target = o;
            } else if (!target.getClass().isAssignableFrom(o.getClass())) {
                setEnabled(false);
                return;
            }

            if (o instanceof IUIDictionary) {
            } else if (o instanceof IUIProduct) {
            } else if (o instanceof IUIProject) {
            } else if (o instanceof IProject) {
            } else {
                setEnabled(false);
                return;
            }
        }
        setEnabled(true);
    }


    private boolean confirmDelete(IStructuredSelection selection) {
        String message = ""; //$NON-NLS-1$
        if (selection.size() == 1) {
            Object o = selection.getFirstElement();
            if ((o instanceof IUIProject) || (o instanceof IProject)) {
                if (o instanceof IProject) {
                    IUIElement uiElem = CoronaModel.INSTANCE.adapter((IProject) o, false);
                    if (uiElem == null) {
                        message = Messages.bind(Messages.DeleteAction_labelDelNonDBProject, new String[] { ((IProject) o).getName() });
                    } else {
                        message = Messages.bind(Messages.DeleteAction_labelDelDBProject, new String[] { ((IUIProject) uiElem).getResource().getName() });
                    }
                } else {
                    message = Messages.bind(Messages.DeleteAction_labelDelDBProject, new String[] { ((IUIProject) o).getResource().getName() });
                }
            } else if (o instanceof IUIProduct) {
                message = Messages.bind(Messages.DeleteAction_labelDelProduct, new String[] { ((IUIProduct) o).getResource().getName() });
            } else if (o instanceof IUIDictionary) {
                if (((IUIDictionary) o).getObject() instanceof IPatternDic) {

                    /* TODO DbViewDeleteHandler.javaのパターン辞書削除処理と共通クラス化したい */
                    IPatternDic obj = (IPatternDic) ((IUIDictionary) o).getObject();
                    HashMap<String, String> regMap = new HashMap<String, String>();
                    HashMap<String, String> referDics = new HashMap<String, String>();
                    /* パターン辞書の場合他辞書から参照されていないか確認する */
                    IUIContainer container = ((IUIDictionary) o).getParent().getParent();
                    StringBuilder items = new StringBuilder();
                    List<ICoronaDic> patternDics = new ArrayList<ICoronaDic>();
                    /* 共通辞書を取得 */
                    IUIProject uiProject = CoronaModel.INSTANCE.getProject((IUIElement) o);
                    ICoronaProject project = uiProject.getObject();
                    /* 共通辞書をAdd */
                    patternDics.addAll(project.getDictionarys(IPatternDic.class));
                    if (container instanceof IUIProduct) {
                        /* ターゲット辞書を削除する場合、自ターゲット辞書を取得 */
                        patternDics.addAll(((IUIProduct) container).getObject().getDictionarys(IPatternDic.class));
                    } else {
                        /* 共通辞書を削除する場合、プロジェクト配下の全ターゲット辞書を取得 */
                        List<ICoronaProduct> products = ((IUIProject) container).getObject().getProducts();
                        for (ICoronaProduct product : products) {
                            patternDics.addAll(product.getDictionarys(IPatternDic.class));
                        }
                    }
                    patternDics.remove(obj);
                    /* パターン辞書のtext部分を結合 */
                    for (ICoronaDic patternDic : patternDics) {
                        for (IDicItem item : patternDic.getItems()) {
                            items.append(((IPattern) item).getText());
                        }
                        regMap.put(patternDic.getName(), items.toString());
                        /* StringBuilderの初期化 */
                        items.setLength(0);
                    }

                    /* 選択された辞書がほかの辞書から参照されていないか確認 */
                    List<IDicItem> selItems = obj.getItems();
                    String regular = ".*LINK ID="; //$NON-NLS-1$
                    for (IDicItem selItem : selItems) {
                        StringBuilder p = new StringBuilder();
                        p.append(regular).append("\"").append(selItem.getId()).append("\".*"); //$NON-NLS-1$ //$NON-NLS-2$
                        for (Map.Entry<String, String> e : regMap.entrySet()) {
                            if (java.util.regex.Pattern.matches(p.toString(), e.getValue())) {
                                referDics.put(e.getKey(), ((IPattern) selItem).getLabel());
                            }
                        }
                    }
                    if (!referDics.isEmpty()) {
                        /* 他辞書から参照されている場合 */
                        StringBuilder message2 = new StringBuilder();
                        message2.append(Messages.DeleteAction_labelDelRefPattern);
                        for (Map.Entry<String, String> e : referDics.entrySet()) {
                            message2.append(Messages.bind(Messages.DeleteAction_labelDelPatternDics, new String[] { e.getKey() }) + "\n"); //$NON-NLS-1$
                        }
                        MessageDialog.openError(getShell(), Messages.DeleteAction_titleRefPattern, message2.toString());
                        return false;
                    }
                }

                if (((IUIDictionary) o).getParent().getParent() instanceof IUIProject) {
                    message = Messages.DeleteAction_labelDelProjectDics;
                } else {
                    message = Messages.DeleteAction_labelDelProductDics;
                }
            }
        } else {
            /* すべて同じものを選択しているので、ひとつめだけ見る */
            Object o = selection.getFirstElement();
            if ((o instanceof IUIProject) || (o instanceof IProject)) {
                message = Messages.DeleteAction_labelDelProjects;
            } else if (o instanceof IUIProduct) {
                message = Messages.DeleteAction_labelDelProducts;
            } else if (o instanceof IUIDictionary) {
                message = Messages.DeleteAction_labelDelUIDics;
            }
        }
        return MessageDialog.openConfirm(getShell(), Messages.DeleteAction_labelDelete, message);
    }
}
