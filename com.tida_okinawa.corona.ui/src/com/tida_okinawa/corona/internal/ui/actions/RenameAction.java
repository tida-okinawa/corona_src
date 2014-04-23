/**
 * @version $Id: RenameAction.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/10/06 16:15:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchSite;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.internal.ui.util.DictionaryNameValidator;
import com.tida_okinawa.corona.internal.ui.util.ProjectNameValidator;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.model.ICoronaObject;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.ViewUtil;
import com.tida_okinawa.corona.ui.views.RenameInputDialog;

/**
 * @author kousuke-morishima
 */
public class RenameAction extends SelectionDispatchAction {
    /*
     * Memo 名称変更できるアイテムの追加方法
     * １．selectionChangedメソッドに追加。
     * 名称変更したいアイテムのとき、trueを返す。
     * ２．runメソッドに追加。
     * １でtrueを返したオブジェクトの名称変更処理を書く。
     * ３．rollbackメソッドに追加。
     * ４．（必要ならば）getValidatorメソッドに追加。
     */

    public RenameAction(IWorkbenchSite site) {
        super(site);
        setText(Messages.RenameAction_renameAndKey);
        setAccelerator(SWT.F2);
    }


    /**
     * 新規名称ダイアログを表示する。selectionの先頭要素を入力された名称にリネームする。
     * 先頭要素がさしているICoronaObjectを指しているUIElementがほかにあれば、それもリネームする
     */
    @Override
    public void run(IStructuredSelection selection) {
        try {
            String newName = decideNewName(selection.getFirstElement());
            runWithProgress(selection, newName);
        } catch (CancellationException e1) {
            e1.printStackTrace();
        } catch (CoreException e1) {
            e1.printStackTrace();
        }
    }


    /**
     * selectionのすべてのアイテムを指定された名前に変更する
     * 
     * @param selection
     * @param newName
     */
    public void run(IStructuredSelection selection, String newName) {
        runWithProgress(selection, newName);
    }


    private void runWithProgress(final IStructuredSelection selection, final String newName) {
        Job renameJob = new Job(Messages.RenameAction_rename) {
            @Override
            public IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(Messages.RenameAction_rename, selection.size());

                for (Object o : selection.toArray()) {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    IResource oldResource = getResource(o);
                    monitor.subTask(oldResource.getFullPath().toString());

                    ICoronaObject key = CoronaModel.INSTANCE.adapter(oldResource, false).getObject();
                    List<IUIElement> uiElements = CoronaModel.INSTANCE.adapter(key);
                    /* IStatus stat = */doRename(uiElements, newName);
                    // if(!Status.OK_STATUS.equals(stat)){
                    // return stat;
                    // }
                    monitor.worked(1);
                }
                monitor.done();

                /* Memo DBViewの表示も更新(無理やり感があっていや。モデルの変更をビューが検出できるようにしたい) */
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtil.refreshDatabaseView(0);
                    }
                });
                return Status.OK_STATUS;
            }
        };
        renameJob.setSystem(false);
        renameJob.setUser(true);
        renameJob.schedule();
    }


    /**
     * 指定されたIResourceおよび同じICoronaObjectを指しているIResourceをリネームする
     * 複数のIResourceがリネームされる場合、ひとつでも失敗したらすべての変更を元に戻す。
     * 
     * @param oldResource
     * @param newName
     * @return
     */
    IStatus doRename(List<IUIElement> uiElements, String newName) {
        /*
         * 編集順番
         * 1. CoronaObjectの編集＆DBに反映
         * 2. 新しいAdapterの作成
         * 3. Adapterの張り替え
         * 4. IResourceの名称変更
         */
        Map<IUIElement, IResource> corrected = new HashMap<IUIElement, IResource>(); // newUIElement,
                                                                                     // oldIResource

        try {
            for (IUIElement element : uiElements) {
                IResource oldResource = element.getResource();
                IResource newResource = null;

                if (!isRenameAvailable(oldResource)) {
                    throwCoreException(Messages.RenameAction_errorNotFile_1);
                }

                if (element instanceof IUIDictionary) {
                    // TODO ここ冗長。forの外でいい
                    IUIDictionary uiDic = (IUIDictionary) element;
                    /* 1. ICoronaDicの変更をDBに反映する */
                    ICoronaDic dic = uiDic.getObject();
                    if (dic == null) {
                        continue;
                    }
                    dic.setName(newName);
                    dic.commit(false, new NullProgressMonitor());

                    /* 2. 新しいIResourceを作る */
                    newResource = oldResource.getParent().getFile(new Path(newName));
                } else if (element instanceof IUIProject) {
                    IUIProject uiProject = (IUIProject) element;
                    /* 1. ICoronaProjectの変更をDBに反映する */
                    ICoronaProject project = uiProject.getObject();
                    project.setName(newName);
                    project.commit(false, new NullProgressMonitor());

                    /* 2. 新しいIResourceを作る */
                    newResource = oldResource.getWorkspace().getRoot().getProject(newName);
                }

                /* 3. Adaperを張り替え */
                CoronaModel.INSTANCE.changeAdapterKey(oldResource, newResource);
                corrected.put(element, oldResource);

                /* 4. IResourceの名称変更 */
                oldResource.move(newPath(oldResource.getFullPath(), newName), IResource.SHALLOW, null);
            }
        } catch (CoreException e) {
            /* IResource#moveが失敗したら、変更を戻す */
            e.printStackTrace();
            CoronaActivator.getDefault().getLogger().getOutStream().println(e.getLocalizedMessage());
            for (Entry<IUIElement, IResource> entry : corrected.entrySet()) {
                rollback(entry.getKey(), entry.getValue(), entry.getKey().getResource());
            }
            return new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, e.getLocalizedMessage());
        }
        return Status.OK_STATUS;
    }


    /**
     * リネームされたアイテムを元に戻す
     * 
     * @param element
     * @param oldResource
     * @param newResource
     */
    private static void rollback(IUIElement element, IResource oldResource, IResource newResource) {
        if (element instanceof IUIDictionary) {
            ICoronaDic dic = ((IUIDictionary) element).getObject();
            dic.setName(oldResource.getName());
            dic.commit(false, new NullProgressMonitor());
        } else if (element instanceof IUIProject) {
            ICoronaProject project = ((IUIProject) element).getObject();
            project.setName(oldResource.getName());
            project.commit(false, new NullProgressMonitor());
        }

        CoronaModel.INSTANCE.changeAdapterKey(newResource, oldResource);
    }


    private static IPath newPath(IPath oldPath, String newName) {
        return oldPath.removeLastSegments(1).append(newName);
    }


    /* ****************************************
     * dialog
     */
    /**
     * 新しい名前を入力するダイアログを開く
     * oldがIResource
     * 
     * @param old
     *            元のアイテム。（IResourceかIUIElement）
     * @return
     * @throws CoreException
     *             <ul>
     *             <li>IResourceが存在しない</li>
     *             <li>oldがIResourceでもIUIElementでもない</li>
     *             <li>IResourceにアクセスできない</li>
     *             </ul>
     * @throws CancellationException
     *             名称入力ダイアログがキャンセルされた
     */
    private String decideNewName(Object old) throws CoreException, CancellationException {
        IResource oldResource = getResource(old);
        if (isRenameAvailable(oldResource)) {
            String oldName = oldResource.getName();
            IUIElement element = getElement(old);
            if (element != null) {
                int i = oldName.lastIndexOf("."); //$NON-NLS-1$
                String ext = ""; //$NON-NLS-1$
                if (i > 0) {
                    ext = oldName.substring(i);
                }
                InputDialog d = new RenameInputDialog(getShell(), Messages.RenameAction_rename, Messages.RenameAction_newName, oldName, getValidator(element),
                        ext);
                if (d.open() == Dialog.OK) {
                    if (!d.getValue().endsWith(ext)) {
                        return d.getValue() + ext;
                    }
                    return d.getValue();
                } else {
                    throw new CancellationException(Messages.RenameAction_errorInputCancel);
                }
            } else {
                throwCoreException(old + Messages.RenameAction_errorFindItem);
            }
        } else {
            throwCoreException(old + Messages.RenameAction_errorNotFile_2);
        }
        return null; // non reached code
    }


    private static IInputValidator getValidator(Object checkTarget) {
        if (checkTarget instanceof IUIDictionary) {
            IResource res = ((IUIDictionary) checkTarget).getResource();
            return DictionaryNameValidator.getDictionaryNameValidator(res.getParent());
        } else if (checkTarget instanceof IUIProject) {
            return ProjectNameValidator.getProjectNameValidator();
        }
        return null;
    }


    IResource getResource(Object element) {
        if (element instanceof IUIElement) {
            return ((IUIElement) element).getResource();
        } else if (element instanceof IProject) { 
            // Memo なんでIProject？IResourceで悪い理由は？
            return (IResource) element;
        }
        return null;
    }


    IUIElement getElement(Object element) {
        if (element instanceof IUIElement) {
            return (IUIElement) element;
        } else if (element instanceof IResource) {
            return CoronaModel.INSTANCE.adapter((IResource) element, false);
        }
        return null;
    }


    @Override
    public void selectionChanged(IStructuredSelection selection) {
        /*
         * Juman以外の辞書か、
         */
        boolean enabled = true;
        if (selection.size() == 1) {
            Object o = selection.getFirstElement();
            if (o instanceof IUIDictionary) {
                ICoronaDic dic = ((IUIDictionary) o).getObject();
                if (dic == null) {
                    enabled = false;
                } else if (dic instanceof IUserDic) {
                    if (DicType.JUMAN.equals(((IUserDic) dic).getDicType())) {
                        enabled = false;
                    }
                }
            } else if ((o instanceof IUIProject) || (o instanceof IProject)) {
                /* 何もしない */
            } else {
                enabled = false;
            }
        } else {
            enabled = false;
        }
        setEnabled(enabled);
    }


    boolean isRenameAvailable(final IResource resource) {
        if (resource == null) {
            return false;
        }
        if (!resource.exists()) {
            return false;
        }
        if (!resource.isAccessible()) {
            return false;
        }
        return true;
    }


    private static void throwCoreException(String message) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, message));
    }

}
