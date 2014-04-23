/**
 * @version $Id: DataBaseView.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/01 13:57:56
 * @author Miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */

package com.tida_okinawa.corona.ui.views.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.tida_okinawa.corona.internal.ui.actions.CoronaElementRenameAction;
import com.tida_okinawa.corona.internal.ui.util.DictionaryNameValidator;
import com.tida_okinawa.corona.internal.ui.util.ProjectNameValidator;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaObject;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.ui.TIDA;
import com.tida_okinawa.corona.ui.handlers.DbViewOpenHandler;
import com.tida_okinawa.corona.ui.views.RenameInputDialog;
import com.tida_okinawa.corona.ui.views.db.action.DeleteClaimDataAction;
import com.tida_okinawa.corona.ui.views.db.action.DeleteDicAction;
import com.tida_okinawa.corona.ui.views.db.action.DeleteDicCategoryAction;
import com.tida_okinawa.corona.ui.views.db.action.DeletePatternTypeAction;
import com.tida_okinawa.corona.ui.views.db.action.DeleteProjectAction;
import com.tida_okinawa.corona.uicomponent.IRefreshable;

/**
 * データベースビュー
 * 
 * @author Miyazato
 * 
 */
public class DataBaseView extends ViewPart implements IRefreshable {

    /** ビューID */
    public static final String VIEW_ID = "com.tida_okinawa.corona.ui.DataBaseView"; //$NON-NLS-1$
    /** 問い合わせデータ削除時メッセージ */
    public static final String deleteMessage = Messages.DataBaseView_messageDeleteDatabase;

    TreeViewer viewer;


    /**
     * コンストラクター
     */
    public DataBaseView() {
    }


    /* ****************************************
     * UI
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.FULL_SELECTION); /* 行選択複数可 */
        viewer.setContentProvider(new DataBaseViewContentProvider());
        viewer.setLabelProvider(new DataBaseViewLabelProvider());
        viewer.setInput(IoActivator.getService());
        addListener(viewer);

        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
        getSite().registerContextMenu(menuMgr, viewer);
        getSite().setSelectionProvider(viewer);
        viewer.setSorter(DataBaseViewContentProvider.getSorter());

        createActions();
    }


    private static void addListener(final TreeViewer viewer) {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                /* Enter押下時もここにくるため、選択したものすべてを開くようにする */
                /* ただし、開けないものを含んでいるときは何もしない */
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                IStructuredSelection selection = (IStructuredSelection) window.getActivePage().getSelection();

                if ((DbViewOpenHandler.isEnabled(selection)) && (selection != null)) {
                    IWorkbenchPage page = window.getActivePage();
                    if (page == null) {
                        return;
                    }
                    for (Object item : selection.toArray()) {
                        if (item instanceof ICoronaObject) {
                            try {
                                TIDA.openEditor(page, (ICoronaObject) item);
                            } catch (PartInitException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }


    private void createActions() {
        // 更新(F5)
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), new Action() {
            @Override
            public void run() {
                viewer.refresh();
            }
        });

        // 削除(Delete)
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), new Action() {
            @Override
            public void run() {
                if (viewer.getSelection() == null) {
                    return;
                }
                if (viewer.getSelection().isEmpty()) {
                    return;
                }

                for (Object item : ((IStructuredSelection) viewer.getSelection()).toArray()) {
                    if (item instanceof String) {
                        return;
                    } else if (item instanceof ICoronaDic) {
                        if (item instanceof IUserDic) {
                            if (DicType.JUMAN.equals(((IUserDic) item).getDicType())) {
                                return;
                            }
                        }
                    } else if (item instanceof ICoronaProject) {
                    } else if (item instanceof IClaimData) {
                    } else if (item instanceof PatternType) {
                    } else if (item instanceof TextItem) {
                    } else {
                        return;
                    }
                }
                if (MessageDialog.openConfirm(getSite().getShell(), Messages.DataBaseView_messageDeleteData, deleteMessage)) {
                    remove(((IStructuredSelection) viewer.getSelection()).toArray());
                }
            }
        });

        // 名前変更(F2)
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.RENAME.getId(), new Action() {
            @Override
            public void run() {
                if (viewer.getSelection() == null) {
                    return;
                }
                if (viewer.getSelection().isEmpty()) {
                    return;
                }
                for (Object o : ((IStructuredSelection) viewer.getSelection()).toArray()) {
                    if (o instanceof ICoronaDic) {
                    } else if (o instanceof ICoronaProject) {
                    } else {
                        /* 辞書、プロジェクトファイル以外が含まれていたら何もしないで抜ける */
                        return;
                    }
                }
                rename((IStructuredSelection) viewer.getSelection());
            }
        });
    }


    /* ****************************************
     * 編集
     */
    /**
     * @param delItems
     * @return ICoronaProjectだけ抜き出す
     */
    /**
     * @param delItems
     */
    public void remove(Object[] delItems) {
        /* 振り分け */
        List<ICoronaProject> delProjs = new ArrayList<ICoronaProject>();
        List<ICoronaDic> delDics = new ArrayList<ICoronaDic>();
        List<IClaimData> delClaims = new ArrayList<IClaimData>();
        List<PatternType> delPTypes = new ArrayList<PatternType>();
        List<TextItem> delCategories = new ArrayList<TextItem>();
        for (Object o : delItems) {
            if (o instanceof ICoronaDic) {
                delDics.add((ICoronaDic) o);
            } else if (o instanceof ICoronaProject) {
                delProjs.add((ICoronaProject) o);
            } else if (o instanceof IClaimData) {
                delClaims.add((IClaimData) o);
            } else if (o instanceof PatternType) {
                delPTypes.add((PatternType) o);
            } else if (o instanceof TextItem) {
                delCategories.add((TextItem) o);
            }
        }


        /* プロジェクトを削除 */
        if (!delProjs.isEmpty()) {
            DeleteProjectAction action = new DeleteProjectAction();
            for (ICoronaProject proj : delProjs) {
                action.setProject(proj);
                action.run();
            }
        }

        /* 辞書削除 */
        if (!delDics.isEmpty()) {
            /* ユーザ辞書より先に子辞書を消さないといけないのでソート */
            sort(delDics);
            DeleteDicAction action = new DeleteDicAction();
            List<ICoronaDic> failList = new ArrayList<ICoronaDic>();
            for (ICoronaDic dic : delDics) {
                action.setDictionay(dic);
                action.run();
                if (action.getResult().getSeverity() == IStatus.ERROR) {
                    failList.add(dic);
                }
            }
            if (failList.size() > 0) {
                String message = Messages.DataBaseView_messageFailDeleteDic;
                StringBuilder dicNames = new StringBuilder();
                for (ICoronaDic dic : failList) {
                    dicNames.append(", ").append(dic.getName()); //$NON-NLS-1$
                }
                if (dicNames.length() > 0) {
                    dicNames.delete(0, 2);
                }
                dicNames.insert(0, message);
                MessageDialog.openWarning(getSite().getShell(), Messages.DataBaseView_messageFailDelete, dicNames.toString());
            }
        }

        /* 問い合わせデータ削除 */
        if (!delClaims.isEmpty()) {
            List<String> exists = new ArrayList<String>();
            DeleteClaimDataAction action = new DeleteClaimDataAction();
            for (IClaimData claim : delClaims) {
                if (!IoActivator.getService().chkRelPrjClm(claim.getId())) {
                    action.setClaimData(claim);
                    action.run();
                } else {
                    exists.add(claim.getName() + Messages.DataBaseView_leftParenthesis + claim.getFileName() + Messages.DataBaseView_rightParenthesis);
                }
            }
            if (exists.size() > 0) {
                // ダイアログ表示
                StringBuilder buf = new StringBuilder(Messages.DataBaseView_8);
                for (String str : exists) {
                    buf.append(" "); //$NON-NLS-1$
                    buf.append(str);
                    buf.append("\n"); //$NON-NLS-1$
                }
                String message = buf.toString();
                MessageDialog.openWarning(getSite().getShell(), Messages.DataBaseView_messageWarning, message);
            }
        }

        /* パターン分類削除 */
        if (!delPTypes.isEmpty()) {
            DeletePatternTypeAction action = new DeletePatternTypeAction(delPTypes);
            if (action.isEnabled()) {
                action.preview(getSite().getShell());
            }
        }

        /* 分類名削除 */
        if (!delCategories.isEmpty()) {
            DeleteDicCategoryAction action = new DeleteDicCategoryAction(delCategories);
            if (action.isEnabled()) {
                action.preview(getSite().getShell());
            }
        }

        viewer.refresh();
    }


    private void sort(List<ICoronaDic> list) {
        Collections.sort(list, new Comparator<Object>() {
            private int category(Object o) {
                if (o instanceof ICoronaDic) {
                    if (o instanceof IUserDic) {
                        return 1;
                    }
                    return 0;
                } else if (o instanceof ICoronaProject) {
                    return 2;
                } else if (o instanceof IClaimData) {
                    return 3;
                } else {
                    return 99;
                }
            }


            @Override
            public int compare(Object o1, Object o2) {
                int cat1 = category(o1);
                int cat2 = category(o2);
                return cat1 - cat2;
            }
        });
    }


    /**
     * プロジェクト・辞書名変更処理
     * 
     * @param selection
     */
    public void rename(IStructuredSelection selection) {

        CoronaElementRenameAction action = new CoronaElementRenameAction(getSite());
        action.selectionChanged(selection);
        if (action.isEnabled()) {
            action.run(selection);
            return;
        }

        String name = null;
        Object o = selection.getFirstElement();
        if (o instanceof ICoronaDic) {
            String ext = ""; //$NON-NLS-1$
            ICoronaDic dic = (ICoronaDic) o;
            if (dic instanceof IUserDic) {
                if (DicType.JUMAN.equals(((IUserDic) dic).getDicType())) {
                    return;
                }
            }
            name = dic.getName();
            int i = name.lastIndexOf("."); //$NON-NLS-1$
            if (i > 0) {
                ext = name.substring(i);
            }
            InputDialog d = new RenameInputDialog(null, Messages.DataBaseView_inputDialogChangeName, Messages.DataBaseView_inputDialogNewName, name,
                    getValidator(o), ext);
            if (d.open() == Dialog.OK) {
                String newName = d.getValue();
                if (!newName.endsWith(ext)) {
                    newName = newName + ext;
                }
                dic.setName(newName);
                dic.commit(false, new NullProgressMonitor());
            }

        } else if (o instanceof ICoronaProject) {
            ICoronaProject prj = (ICoronaProject) o;
            name = prj.getName();
            InputDialog d = new RenameInputDialog(null, Messages.DataBaseView_inputDialogChangeName, Messages.DataBaseView_inputDialogNewName, name,
                    getValidator(o), ""); //$NON-NLS-1$ 
            if (d.open() == Dialog.OK) {
                String newName = d.getValue();
                prj.setName(newName);
                prj.commit(false, new NullProgressMonitor());
            }
        }

        /* 裸で更新処理を実行しても表示が変わらないことがあるので、ディレイを与える */
        final IStructuredSelection finalSelection = selection;
        Job refreshJob = new UIJob("") { //$NON-NLS-1$
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                viewer.update(finalSelection.getFirstElement(), null);
                return Status.OK_STATUS;
            }
        };
        refreshJob.setUser(false);
        refreshJob.setSystem(true);
        refreshJob.schedule(25);
    }


    private static IInputValidator getValidator(Object checkTarget) {
        if (checkTarget instanceof ICoronaDic) {
            return DictionaryNameValidator.getDictionaryNameValidator(null);
        } else if (checkTarget instanceof ICoronaProject) {
            return ProjectNameValidator.getProjectNameValidator();
        }
        return null;
    }


    /* ****************************************
     * other
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }


    /**
     * データベースビューの表示を更新する
     */
    @Override
    public void refreshView() {
        viewer.refresh();
    }

}
