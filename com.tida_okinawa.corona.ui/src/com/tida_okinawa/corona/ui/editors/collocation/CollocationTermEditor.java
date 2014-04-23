/**
 * @version $Id: CollocationTermEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/06 10:34:50
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.collocation;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

import com.tida_okinawa.corona.correction.frequent.FrequentRecord;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author wataru-higa
 * 
 */
public class CollocationTermEditor extends FormEditor {

    /** エディターID */
    public static final String EDITOR_ID = "com.tida_okinawa.corona.ui.editor.collocationtermeditor"; //$NON-NLS-1$

    CollocationTermPage collocationTermPage;
    /**
     * UIActivaterのIPreferenceStore(DBの辞書情報)
     */
    public IPreferenceStore store;
    private Set<ISelectionChangedListener> selChangeListener = new HashSet<ISelectionChangedListener>();


    /** コンストラクター */
    public CollocationTermEditor() {
    }


    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
    }


    @Override
    protected void addPages() {
        try {
            addPage(collocationTermPage);

        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void createPages() {
        collocationTermPage = new CollocationTermPage(this);
        super.createPages();
        getSite().setSelectionProvider(new ISelectionProvider() {
            @Override
            public void addSelectionChangedListener(ISelectionChangedListener listener) {
                int index = getActivePage();
                if (index > -1) {
                    if (pages.get(index) instanceof CollocationTermPage) {
                        CollocationTermPage activePage = (CollocationTermPage) pages.get(index);
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
                result = ((CollocationTermPage) page).viewer.getSelection();
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

            }

        });
    }


    @Override
    public void doSave(IProgressMonitor monitor) {
        collocationTermPage.doSave(monitor);

        StringBuilder message = new StringBuilder(200);

        if (collocationTermPage.errorItems.size() > 0) {
            message.append("以下のアイテムは、入力に誤りがあるため保存できません\n\n"); //$NON-NLS-1$
            if (collocationTermPage.errorItems.size() > 0) {
                message.append("[連語]\n  "); //$NON-NLS-1$
                for (FrequentRecord r : collocationTermPage.errorItems) {
                    message.append(r.getGenkei()).append(", "); //$NON-NLS-1$
                }
                message.append("\n\n"); //$NON-NLS-1$
            }

            /* メッセージ表示 */
            MessageDialog.openInformation(getSite().getShell(), "error", message.toString()); //$NON-NLS-1$
        }
        store = UIActivator.getDefault().getPreferenceStore();
    }


    @Override
    public void doSaveAs() {
    }


    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

}
