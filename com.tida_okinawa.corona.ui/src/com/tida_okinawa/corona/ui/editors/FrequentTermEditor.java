/**
 * @version $Id: FrequentTermEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/29 13:30:39
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

import com.tida_okinawa.corona.correction.frequent.FrequentRecord;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.ui.PreferenceInitializer;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author takayuki-matsumoto, imai
 */
public class FrequentTermEditor extends FormEditor {

    /** エディターID */
    public static final String EDITOR_ID = "com.tida_okinawa.corona.ui.editor.frequenttermeditor"; //$NON-NLS-1$

    FrequentTermPage page1;
    private FrequentTermUndefPage page2;
    public IPreferenceStore store;
    private Set<ISelectionChangedListener> selChangeListener = new HashSet<ISelectionChangedListener>();


    /** コンストラクター */
    public FrequentTermEditor() {
    }

    private DicEditorDisposer disposer;
    private IPartListener jobDisposer = new IPartListener() {
        @Override
        public void partOpened(IWorkbenchPart part) {
        }


        @Override
        public void partDeactivated(IWorkbenchPart part) {
            if (FrequentTermEditor.this.equals(part)) {
                page1.cancelJob();
            }
        }


        @Override
        public void partClosed(IWorkbenchPart part) {
            if (FrequentTermEditor.this.equals(part)) {
                page1.cancelJob();
            }
        }


        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
        }


        @Override
        public void partActivated(IWorkbenchPart part) {
            if (FrequentTermEditor.this.equals(part)) {
                if (page1.equals(getActivePageInstance())) {
                    page1.startJob();
                }
            }
        }
    };


    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        assert input instanceof FrequentTermEditorInput;
        disposer = new DicEditorDisposer(this, new ArrayList<ICoronaDic>(((FrequentTermEditorInput) input).getDics()));
        site.getPage().addPartListener(disposer);
        site.getPage().addPartListener(jobDisposer);
    }


    @Override
    public void dispose() {
        super.dispose();
        getSite().getPage().removePartListener(disposer);
        getSite().getPage().removePartListener(jobDisposer);
    }


    @Override
    protected void addPages() {

    }


    @Override
    protected void createPages() {
        page1 = new FrequentTermPage(this);
        page2 = new FrequentTermUndefPage(this);
        try {
            addPage(page2);
            addPage(page1);

        } catch (PartInitException e) {
            e.printStackTrace();
        }
        super.createPages();
        getSite().setSelectionProvider(new ISelectionProvider() {
            @Override
            public void addSelectionChangedListener(ISelectionChangedListener listener) {
                int index = getActivePage();
                if (index > -1) {
                    if (pages.get(index) instanceof FrequentTermPage) {
                        FrequentTermPage activePage = (FrequentTermPage) pages.get(index);
                        if (activePage.viewer != null) {
                            activePage.viewer.addSelectionChangedListener(listener);
                        }
                    }

                }
                selChangeListener.add(listener);
            }


            @Override
            public ISelection getSelection() {
                ISelection result = null;
                int index = getActivePage();
                if (index < 0)
                    return null;
                IFormPage page = getActivePageInstance();
                if (page instanceof FrequentTermUndefPage) {
                    result = ((FrequentTermUndefPage) page).viewer.getSelection();
                } else if (page instanceof FrequentTermPage) {
                    result = ((FrequentTermPage) page).viewer.getSelection();
                }
                return result;
            }


            @Override
            public void removeSelectionChangedListener(ISelectionChangedListener listener) {

            }


            @Override
            public void setSelection(ISelection selection) {
            }
        });
        this.addPageChangedListener(new IPageChangedListener() {

            @Override
            public void pageChanged(PageChangedEvent event) {
                if (event.getSelectedPage() instanceof FrequentTermPage) {
                    for (ISelectionChangedListener l : selChangeListener) {
                        ((FrequentTermPage) event.getSelectedPage()).viewer.removeSelectionChangedListener(l);
                        ((FrequentTermPage) event.getSelectedPage()).viewer.addSelectionChangedListener(l);
                    }
                }

            }

        });
    }

    protected MessageCheckDialog dialog;


    /**
     * ダイアログオープン
     */
    public void openMessageCheckDialog() {
        /*
         * TODO FindBugs
         * ・Editorごとにダイアログを表示する判定がされるので、staticで持つ必要はないと思う
         * ・毎回生成する必要もnullチェックする必要もないのでは？
         */
        if (dialog == null) {
            dialog = new MessageCheckDialog(getSite().getShell(), Messages.FrequentTermEditor_messageConfirm);
            dialog.open();
            dialog = null;
        }

    }


    @Override
    public void doSave(IProgressMonitor monitor) {
        page1.doSave(monitor);
        page2.doSave(monitor);

        StringBuilder message = new StringBuilder(200);

        if (page1.errorItems.size() > 0 || page2.errorItems.size() > 0) {
            message.append(Messages.FrequentTermEditor_messageErrorInput);
            if (page1.errorItems.size() > 0) {
                message.append(Messages.FrequentTermEditor_messageSetedTerm);
                for (FrequentRecord r : page1.errorItems) {
                    message.append(r.getGenkei()).append(", "); //$NON-NLS-1$
                }
                message.append("\n\n"); //$NON-NLS-1$
            }
            if (page2.errorItems.size() > 0) {
                message.append(Messages.FrequentTermEditor_messageUndefinedTerm);
                for (FrequentRecord r : page2.errorItems) {
                    message.append(r.getGenkei()).append(", "); //$NON-NLS-1$
                }
            }
            /* メッセージ表示 */
            MessageDialog.openInformation(getSite().getShell(), Messages.FrequentTermEditor_messageError, message.toString());
        }

        store = UIActivator.getDefault().getPreferenceStore();
        if (!store.getBoolean(PreferenceInitializer.PREF_DISP_FREDLG)) {
            /* プリファレンスにてダイアログ表示しないにチェックが入っていない場合 */
            if (page1.doSaveFlg || page2.doSaveFlg) {
                /* page1またはpage2のいずれかで保存が行われた場合、ダイアログ表示 */
                openMessageCheckDialog();
            }
        }

    }


    @Override
    public void doSaveAs() {
        // nothing to do
    }


    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }


    @Override
    public boolean isDirty() {
        boolean flg = (page1.isDirty() || page2.isDirty());
        return flg;
    }


    @Override
    public String getPartName() {
        return getEditorInput().getName();
    }


    @Override
    protected void pageChange(int newPageIndex) {
        if (!page1.equals(pages.get(newPageIndex))) {
            page1.cancelJob();
        }
        super.pageChange(newPageIndex);
        if (page1.equals(pages.get(newPageIndex))) {
            page1.startJob();
        }
    }

    class MessageCheckDialog extends Dialog {

        private String title;
        private Button dialogShow = null;


        public MessageCheckDialog(Shell shell, String title) {
            super(shell);
            this.title = title;
        }


        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            /* 「OK」ボタンを生成 */
            createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        }


        @Override
        protected Control createDialogArea(Composite parent) {
            Composite root = (Composite) super.createDialogArea(parent);
            /* メッセージラベル表示 */
            CompositeUtil.createLabel(root, Messages.FrequentTermEditor_labelWarning_1, -1);
            CompositeUtil.createLabel(root, Messages.FrequentTermEditor_labelWarning_2, -1);
            /* チェックボックスを表示 */
            CompositeUtil.createLabel(parent, "", 1); //$NON-NLS-1$
            dialogShow = new Button(root, SWT.CHECK);
            dialogShow.setText(Messages.FrequentTermEditor_messageNotDisplayNext);

            return root;

        }


        @Override
        protected void okPressed() {
            /* チェックボックスの情報をプリファレンスに保持 */
            store.setValue(PreferenceInitializer.PREF_DISP_FREDLG, dialogShow.getSelection());
            /* ダイアログを閉じる */
            super.okPressed();
        }


        /**
         * タイトルを設定
         */
        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(title);
        }
    }
}
