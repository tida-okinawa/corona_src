/**
 * @version $Id: ResultPatternEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/09/02 21:49:11
 * @author imai
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * 構文解析（パターンマッチング）結果ビュー
 * 
 * @author imai
 */
public class ResultPatternEditor extends FormEditor {

    /** エディターID */
    public static final String EDITOR_ID = "com.tida_okinawa.corona.ui.editor.resultpatterneditor";

    ResultPatternPage page1;
    private ResultPatternPage2 page2;
    private Set<ISelectionChangedListener> selChangeListener = new HashSet<ISelectionChangedListener>();


    @Override
    protected void addPages() {

    }


    @Override
    protected void createPages() {
        ResultPatternEditorInput input = (ResultPatternEditorInput) getEditorInput();
        page1 = new ResultPatternPage(this);
        page2 = new ResultPatternPage2(this, new ResultPatternEditorInput2(input.getName(), input.claimWorkPattern));

        try {
            addPage(page1);
            addPage(page2);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
        super.createPages();
        getSite().setSelectionProvider(new ISelectionProvider() {
            @Override
            public void addSelectionChangedListener(ISelectionChangedListener listener) {
                int index = getActivePage();
                if (index > -1) {
                    if (pages.get(index) instanceof ResultPatternPage) {
                        ResultPatternPage activePage = (ResultPatternPage) pages.get(index);
                        if (activePage.resultTreeViewer != null) {
                            activePage.resultTreeViewer.addSelectionChangedListener(listener);
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
                if (page instanceof ResultPatternPage2) {
                    result = ((ResultPatternPage2) page).resultTreeViewer.getSelection();
                } else if (page instanceof ResultPatternPage) {
                    result = ((ResultPatternPage) page).resultTreeViewer.getSelection();
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
                if (event.getSelectedPage() instanceof ResultPatternPage) {
                    for (ISelectionChangedListener l : selChangeListener) {
                        ((ResultPatternPage) event.getSelectedPage()).resultTreeViewer.removeSelectionChangedListener(l);
                        ((ResultPatternPage) event.getSelectedPage()).resultTreeViewer.addSelectionChangedListener(l);
                    }
                }

            }

        });

    }


    /* ****************************************
     * 保存
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
    }


    @Override
    public void doSaveAs() {
    }


    @Override
    public boolean isDirty() {
        return false;
    }


    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }


    @Override
    public String getPartName() {
        return getEditorInput().getName();
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySheetPage.class)) {
            return new PropertySheetPage();
        }
        return super.getAdapter(adapter);
    }
}
