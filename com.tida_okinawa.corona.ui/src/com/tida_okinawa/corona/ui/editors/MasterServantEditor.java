/**
 * @version $Id: MasterServantEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/04 15:18:30
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;

/**
 * @author kousuke-morishima
 */
public abstract class MasterServantEditor extends AbstractDicEditor implements ISelectionProvider {
    public static final String MASTER = "master";
    public static final String SERVANT = "servant";

    private int[] weights;
    private int masterStyle;
    private int servantStyle;


    /**
     * 幅３：７で作成
     */
    public MasterServantEditor() {
        this(new int[] { 3, 7 }, SWT.MULTI, SWT.MULTI);
    }


    public MasterServantEditor(int[] weights) {
        this(weights, SWT.MULTI, SWT.MULTI);
    }


    /**
     * @param weights
     *            長さ２の配列。０番目がマスターの領域。１がサブの領域。
     * @param masterStyle
     *            TableViewerに指定できるスタイル
     * @param servantStyle
     *            TableViewerに指定できるスタイル
     */
    public MasterServantEditor(int[] weights, int masterStyle, int servantStyle) {
        this.weights = weights;

        this.masterStyle = masterStyle;
        this.servantStyle = servantStyle;
    }


    @Override
    public void createPartControl(Composite parent) {
        SashForm root = CompositeUtil.defaultSashForm(parent, SWT.HORIZONTAL);
        createMasterArea(root);
        activeViewer = masterViewer;
        createServantArea(root);
        root.setWeights(weights);

        super.createPartControl(parent);

        getSite().setSelectionProvider(new ISelectionProvider() {
            @Override
            public void addSelectionChangedListener(ISelectionChangedListener listener) {
                masterViewer.addSelectionChangedListener(listener);
                servantViewer.addSelectionChangedListener(listener);
            }


            @Override
            public ISelection getSelection() {
                if (activeViewer == null)
                    return null;
                return activeViewer.getSelection();
            }


            @Override
            public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            }


            @Override
            public void setSelection(ISelection selection) {
            }
        });
    }


    /* ****************************************
     * マスター
     */
    TableViewer masterViewer;


    protected TableViewer getMasterViewer() {
        return masterViewer;
    }


    /**
     * マスターの領域を作成する
     * 
     * @param parent
     * @return
     */
    protected Control createMasterArea(Composite parent) {
        masterViewer = new TableViewer(parent, masterStyle);
        masterViewer.setLabelProvider(getMasterLabelProvider());
        masterViewer.setContentProvider(getMasterContentProvider());
        masterViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            final Object OBJ = new Object();
            Object currentSelection = OBJ;


            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                /* マスターで選択されたものによって、サブの内容を切り替える */
                Object input = null;
                if (masterViewer.getTable().getSelectionCount() > 1) {
                    /* 複数選択時は、何も表示しない */
                    currentSelection = OBJ;
                } else {
                    Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
                    if (selection != null) {
                        if (!currentSelection.equals(selection)) {
                            currentSelection = selection;
                        }
                        input = currentSelection;
                    } else {
                        currentSelection = OBJ;
                    }
                }
                servantViewer.setInput(input);
                if (servantViewer.getTable().getItemCount() > 0) {
                    servantViewer.getTable().select(0);
                }
            }
        });
        masterViewer.getControl().addFocusListener(focusListener);

        masterViewer.setInput(getMasterInput());

        return masterViewer.getControl();
    }


    /**
     * マスター領域に表示する要素のContentProviderを返す。
     * デフォルトの実装は、ArrayContentProviderを返す。
     * 
     * @return must not null
     */
    protected IStructuredContentProvider getMasterContentProvider() {
        return new ArrayContentProvider();
    }


    protected ILabelProvider getMasterLabelProvider() {
        return new LabelProvider();
    }


    /**
     * @return マスター領域に表示するアイテム
     */
    abstract protected Object getMasterInput();

    /* ****************************************
     * サブ
     */
    TableViewer servantViewer;


    protected TableViewer getServantViewer() {
        return servantViewer;
    }


    /**
     * @param parent
     * @return
     */
    protected Control createServantArea(Composite parent) {
        servantViewer = new TableViewer(parent, servantStyle);
        servantViewer.setLabelProvider(getServantLabelProvider());
        servantViewer.setContentProvider(getServantContentProvider());
        servantViewer.getControl().addFocusListener(focusListener);
        return servantViewer.getControl();
    }


    /**
     * サブ領域に表示する要素のContentProviderを返す。
     * マスター領域に表示しているアイテムがgetElementの引数にわたってくる
     * 
     * @return must not null
     */
    abstract protected IStructuredContentProvider getServantContentProvider();


    protected ILabelProvider getServantLabelProvider() {
        return new LabelProvider();
    }


    /* ****************************************
     * その他
     */
    protected void addSelectionChangedListener(String addDest, ISelectionChangedListener listener) {
        if (MASTER.equals(addDest)) {
            masterViewer.addSelectionChangedListener(listener);
        } else {
            servantViewer.addSelectionChangedListener(listener);
        }
    }


    protected void removeSelectionChangedListener(String removeSrc, ISelectionChangedListener listener) {
        if (MASTER.equals(removeSrc)) {
            masterViewer.removeSelectionChangedListener(listener);
        } else {
            servantViewer.removeSelectionChangedListener(listener);
        }
    }


    @Override
    public void setFocus() {
        if (activeViewer != null) {
            activeViewer.getControl().setFocus();
            IStructuredSelection selection = (IStructuredSelection) activeViewer.getSelection();
            Object element = selection.getFirstElement();
            if ((element == null) && (activeViewer.getTable().getItemCount() > 0)) {
                element = activeViewer.getTable().getItem(0).getData();
                activeViewer.setSelection(new StructuredSelection(element), true);
            } else {
                activeViewer.setSelection(selection, true);
            }
        }
    }

    private FocusListener focusListener = new FocusListener() {
        @Override
        public void focusLost(FocusEvent e) {
        }


        @Override
        public void focusGained(FocusEvent e) {
            if (masterViewer.getControl().equals(e.getSource())) {
                activeViewer = masterViewer;
            } else {
                activeViewer = servantViewer;
            }
        }
    };

    TableViewer activeViewer;


    public Viewer getActiveViewer() {
        return activeViewer;
    }


    /**
     * @deprecated use
     *             {@link #addSelectionChangedListener(String, ISelectionChangedListener)}
     */
    @Deprecated
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    };


    /**
     * @deprecated use
     *             {@link #removeSelectionChangedListener(String, ISelectionChangedListener)}
     */
    @Deprecated
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    }


    @Override
    public ISelection getSelection() {
        return activeViewer.getSelection();
    }


    /**
     * @deprecated always throw {@link UnsupportedOperationException}
     */
    @Deprecated
    @Override
    public void setSelection(ISelection selection) {
        throw new UnsupportedOperationException();
    }
}
