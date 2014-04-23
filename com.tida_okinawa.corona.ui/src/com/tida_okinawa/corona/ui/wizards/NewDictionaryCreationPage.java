/**
 * @version $Id: NewDictionaryCreationPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/02 11:23:51
 * @author KMorishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;


import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.tida_okinawa.corona.common.StringUtil;
import com.tida_okinawa.corona.internal.ui.util.DictionaryNameValidator;
import com.tida_okinawa.corona.internal.ui.util.DictionaryPriorityUtil;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibrary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.internal.ui.views.model.impl.FileContent;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * 
 * @author KMorishima
 */
public class NewDictionaryCreationPage extends WizardNewFileCreationPage {
    private static final CoronaModel model = CoronaModel.INSTANCE;


    /**
     * @param title
     *            ページタイトル
     * @param dicType
     *            作成する辞書の種類
     * @param selection
     *            Newを実行した時に選択していたアイテム。ダイアログで、デフォルトで選択する親フォルダ
     * @see DicType
     */
    public NewDictionaryCreationPage(String title, DicType dicType, IStructuredSelection selection) {
        super("NewDictionaryCreationPage", CoronaModel.toContainer(selection)); //$NON-NLS-1$
        setFileExtension(dicType.getExtension()); /* 自動で付けてくれる拡張子を設定する */

        setTitle(title);
        setDescription(Messages.NewDictionaryCreationPage_descriptionCreateDic);
    }

    private IUIDictionary newUIDictionary;


    /**
     * {@link #createNewFile()}で作成した辞書を返す。 {@link #createNewFile()}を呼び出すまではnull
     * 
     * @return 作成した辞書
     */
    public IUIDictionary getDictionary() {
        return newUIDictionary;
    }


    protected boolean dictionarySettings(IUIDictionary newDictionary) {
        return (newDictionary != null);
    }


    @Override
    public IFile createNewFile() {
        IFile newIFile = createFileHandle(getContainerFullPath().append(getFileName()));

        /* 辞書種別に応じた辞書(ICoronaDic)を作る */
        newUIDictionary = (IUIDictionary) model.create(newIFile);
        ICoronaDic newDictionary = newUIDictionary.getObject();
        if (dictionarySettings(newUIDictionary)) {
            /* 同じターゲットがツリー上にあれば、辞書を作る */
            if (model.getLibrary(newUIDictionary) instanceof IUIProduct) {
                List<IUIElement> uiElements = model.adapter(((IUIProduct) model.getLibrary(newUIDictionary)).getObject());
                for (IUIElement uiElement : uiElements) {
                    IUIDictionary uiDic = model.getDic((IUILibrary) uiElement, newDictionary);
                    if (!newUIDictionary.equals(uiDic)) {
                        uiDic.update(null);
                        uiDic.getParent().modifiedChildren();
                    }
                }
            }
            newDictionary.commit(false, new NullProgressMonitor()); // プロパティを反映させるため
            DictionaryPriorityUtil.addDicPriority(newUIDictionary);
            /* IFileを作る */
            try {
                if (!newIFile.exists()) {
                    newIFile.create(FileContent.toStream(newUIDictionary), false, null);
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
            return newIFile;
        }

        /* エラーが発生したら、全部消す */
        CoronaModel.INSTANCE.remove(newUIDictionary);
        IoActivator.getService().removeDictionary(newDictionary.getId());
        newUIDictionary = null;
        return null;
    }


    /**
     * このページの設定を記憶する
     * 
     * @param settings
     */
    public void saveDefault(DialogSettings settings) {
    }


    @Override
    public void handleEvent(Event event) {
        super.handleEvent(event);
        setPageComplete(validatePage());
    }


    /**
     * @return 現在選択されているフォルダ。何も選択されていないときはnull。テキストボックスの値が優先される。
     */
    public IContainer getSelectedContainer() {
        IPath path = getContainerFullPath();
        /* ファイルを作るときは、パスのルートに何かないといけないので、セグメント数チェック */
        if ((path != null) && (path.segmentCount() > 0)) {
            return createFileHandle(path.append("dummy")).getParent(); //$NON-NLS-1$
        }
        return null;
    }


    /* ****************************************
     * ページチェック
     */
    @Override
    protected boolean validatePage() {
        if (!super.validatePage()) {
            return false;
        }

        IContainer selectedContainer = getSelectedContainer();
        if (selectedContainer == null) {
            setErrorMessage(Messages.NewDictionaryCreationPage_errorMessageNotParentFolder);
            return false;
        }

        if (!selectedContainer.exists()) {
            setErrorMessage(Messages.NewDictionaryCreationPage_errorMessageDisableParentFolder);
            return false;
        }

        if (!isLibrary(selectedContainer)) {
            setErrorMessage(Messages.NewDictionaryCreationPage_errorMessageSelectDic);
            return false;
        }

        /*
         * 文字数制限
         * DBで255文字に制限されている
         */
        String fileName = getFileName();
        if (!fileName.endsWith("." + getFileExtension())) { //$NON-NLS-1$
            // non reached
            fileName += "." + getFileExtension(); //$NON-NLS-1$
        }

        String message = DictionaryNameValidator.isValid(selectedContainer, fileName);
        if (message != null) {
            if (message.contains(Messages.NewDictionaryCreationPage_messageDatabase)) { /*
                                                                                         * メッセージを分けるために無理やり感が
                                                                                         * …
                                                                                         */
                setErrorMessage(message + Messages.NewDictionaryCreationPage_errorMessageUseImport);
            } else {
                setErrorMessage(message);
            }
            return false;
        }

        setErrorMessage(null);
        return true;
    }


    static final boolean isLibrary(IContainer container) {
        if (container == null) {
            return false;
        }

        /*
         * プロジェクトツリーを未展開だと、adapterがうまく働かなくて、ILibraryがとってこれず
         * 判定が成功しなかった
         */
        IUIElement element = model.adapter(container, false);
        if (element instanceof IUILibFolder) {
            /* ILibraryが選択されている */
            return true;
        }

        if (element != null) {
            /* その他のCoronaObjectが選択されている */
            return false;
        }

        /* 選択したIResourceは、Adapterに登録されていない */
        IContainer parentContainer = container.getParent();
        IUIElement parentElement = model.adapter(parentContainer, false);
        if ((parentElement instanceof IUILibrary) && (container instanceof IFolder)) {
            IUIElement libElement = model.create(container);
            if (libElement instanceof IUILibFolder) {
                return true;
            }
        }

        /* ターゲット下のフォルダの場合、まだ取れない可能性がある */
        IProject project = container.getProject();
        IUIProject uiProject = (IUIProject) model.adapter(project, false);
        if (uiProject != null) {
            /* nullの場合、CoronaProjectではない */
            ICoronaProject coronaProject = uiProject.getObject();
            List<ICoronaProduct> products = coronaProject.getProducts();
            for (ICoronaProduct product : products) {
                if (StringUtil.convertValidFileName(product.getName()).equals(parentContainer.getName())) {
                    IUIElement libElement = model.create(container);
                    if (libElement instanceof IUILibFolder) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    /* ****************************************
     * ページUI作成
     */
    /* superの拡張領域を消したので、色々無効にする */
    @Override
    protected void createAdvancedControls(Composite parent) {
    }


    @Override
    protected IStatus validateLinkedResource() {
        return new Status(IStatus.OK, UIActivator.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
    }


    @Override
    protected void createLinkTarget() {
    }
}
