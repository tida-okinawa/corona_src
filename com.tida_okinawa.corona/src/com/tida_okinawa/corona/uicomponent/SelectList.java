/**
 * @version $Id: SelectList.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/19 13:58:36
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.uicomponent;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

/**
 * 二つのリスト間で、アイテムを移動させるUI
 * 
 * @author kousuke-morishima
 */
public class SelectList {
    /**
     * デフォルトサイズでコンポーネントを作成する
     * 
     * @param parent
     *            親コンポーネント
     */
    public SelectList(Composite parent) {
        this(parent, 200, 100);
    }

    private int width;
    private int height;


    /**
     * 指定サイズでコンポーネントを作成する
     * 
     * @param parent
     *            親コンポーネント
     * @param width
     *            リスト１つの幅
     * @param height
     *            リスト１つの高さ
     */
    public SelectList(Composite parent, int width, int height) {
        this.width = width;
        this.height = height;
        createControl(parent);
    }


    /**
     * @return 選択したアイテムの一覧
     */
    public Object[] getSelected() {
        return selected.toArray();
    }


    /* ****************************************
     * UI構築
     */
    private void createControl(Composite parent) {
        parent = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        parent.setLayout(layout);
        parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSelectedViewer(parent);
        createButtonGroup(parent);
        createSourceViewer(parent);
    }

    /* ********************
     * Viewer
     */
    TableViewer sourceViewer;
    List<Object> source;


    private void createSourceViewer(Composite parent) {
        sourceViewer = createBaseViewer(parent);
    }

    TableViewer selectedViewer;
    List<Object> selected;


    private void createSelectedViewer(Composite parent) {
        selectedViewer = createBaseViewer(parent);
    }


    private TableViewer createBaseViewer(Composite parent) {
        TableViewer base = new TableViewer(parent, SWT.MULTI | SWT.BORDER);
        base.setContentProvider(new ArrayContentProvider());

        base.getTable().addMouseListener(doubleClickListener);

        Table t = base.getTable();
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        t.setLayout(layout);
        GridData layoutData = new GridData(SWT.NONE, SWT.FILL, false, true);
        layoutData.widthHint = width;
        layoutData.heightHint = height;
        t.setLayoutData(layoutData);
        return base;
    }

    MouseListener doubleClickListener = new MouseAdapter() {
        @Override
        public void mouseDoubleClick(MouseEvent e) {
            if (isMoveByDoubleClick) {
                if (sourceViewer.getTable().equals(e.getSource())) {
                    IStructuredSelection ss = (IStructuredSelection) sourceViewer.getSelection();
                    Object[] array = ss.toArray();
                    doAdd(array);
                    fireEvent(getEvent(ADDED, array));
                } else if (selectedViewer.getTable().equals(e.getSource())) {
                    IStructuredSelection ss = (IStructuredSelection) selectedViewer.getSelection();
                    Object[] array = ss.toArray();
                    doRemove(array);
                    fireEvent(getEvent(REMOVED, array));
                }
            }
            fireDoubleClickEvent(e);
        }
    };

    boolean isMoveByDoubleClick = true;


    /**
     * @return アイテムをダブルクリックしてリスト間移動できるかどうか
     */
    public boolean canMoveByDoubleClick() {
        return isMoveByDoubleClick;
    }


    /**
     * @param enabled
     *            アイテムをダブルクリックしてリスト間を移動できるようにするならtrue
     */
    public void setMovableByDoubleClick(boolean enabled) {
        this.isMoveByDoubleClick = enabled;
    }


    /* ********************
     * Button
     */
    /* Memo 現状では使用する箇所がないので、コメントアウト */
    /* Button addButton; */
    /* Button removeButton; */


    private void createButtonGroup(Composite parent) {
        parent = new Composite(parent, SWT.NONE);
        parent.setLayout(new GridLayout());
        parent.setLayoutData(new GridData());
        /* addButton = */createButton(parent, "< 追加(&A)", addListener);
        /* removeButton = */createButton(parent, "除去(&R) >", removeListener);
    }


    private static Button createButton(Composite parent, String label, SelectionListener listener) {
        Button b = new Button(parent, SWT.PUSH);
        b.setText(label);
        b.addSelectionListener(listener);
        b.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
        return b;
    }

    /* ********************
     * Button Listeners
     */
    SelectionListener addListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            int firstIndex = 0;
            IStructuredSelection ss = (IStructuredSelection) sourceViewer.getSelection();
            if (ss.size() > 0) {
                Object[] array = ss.toArray();
                firstIndex = source.indexOf(array[0]);
                doAdd(array);

                fireEvent(getEvent(ADDED, array));
            }
            defaultSelection(sourceViewer, source, firstIndex);
        }
    };
    SelectionListener removeListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            int firstIndex = 0;
            IStructuredSelection ss = (IStructuredSelection) selectedViewer.getSelection();
            if (ss.size() > 0) {
                Object[] array = ss.toArray();
                firstIndex = selected.indexOf(array[0]);
                doRemove(array);

                fireEvent(getEvent(REMOVED, array));
            }
            defaultSelection(selectedViewer, selected, firstIndex);
        }
    };


    static Event getEvent(int detail, Object[] items) {
        Event event = new Event();
        event.detail = detail;
        event.data = items;
        return event;
    }


    void doAdd(Object[] array) {
        for (Object o : array) {
            if (source.remove(o)) {
                selected.add(o);
                sourceViewer.remove(o);
                selectedViewer.add(o);
            }
        }
    }


    void doRemove(Object[] array) {
        for (Object o : array) {
            if (selected.remove(o)) {
                source.add(o);
                selectedViewer.remove(o);
                sourceViewer.add(o);
            }
        }
    }


    void defaultSelection(TableViewer viewer, List<Object> list, int oldFirstIndex) {
        int index = (oldFirstIndex < 0) ? 0 : oldFirstIndex;
        int size = list.size();

        if (size > 0) {
            index = (size <= oldFirstIndex) ? size - 1 : index;
            viewer.setSelection(new StructuredSelection(list.get(index)));
        }
    }

    /* ********************
     * Event Provider
     */
    ListenerList listeners = new ListenerList();

    /**
     * アイテムが追加されたことを示す値
     */
    public static final int ADDED = 1;
    /**
     * アイテムが除去されたことを示す値
     */
    public static final int REMOVED = 2;


    /**
     * 選択されているアイテムの一覧に変更があった時に通知を受けるリスナーを追加する。 {@link Event#detail}の値は
     * {@link #ADDED} or {@link #REMOVED}<br />
     * 対象のアイテムが {@link Event#data}に入っている
     * 
     * @param listener
     *            選択しているアイテムが変更されたときに通知を受けるリスナー
     */
    public void addListChangedListener(Listener listener) {
        listeners.add(listener);
    }


    /**
     * @param listener
     *            除去するリスナー
     */
    public void removeListChangedLitener(Listener listener) {
        listeners.remove(listener);
    }


    void fireEvent(Event event) {
        for (Object l : listeners.getListeners()) {
            ((Listener) l).handleEvent(event);
        }
    }

    ListenerList mouseListeners = new ListenerList();


    /**
     * リストアイテムがダブルクリックされたときに通知を受けるリスナーを追加する
     * 
     * @param listener
     *            ダブルクリック通知を受けるリスナー
     */
    public void addDoubleClickListener(Listener listener) {
        mouseListeners.add(listener);
    }


    /**
     * リスナーを除去する
     * 
     * @param listener
     *            ダブルクリック通知を受けるリスナー
     */
    public void removeDoubleClickListener(Listener listener) {
        mouseListeners.remove(listener);
    }


    void fireDoubleClickEvent(MouseEvent e) {
        Event event = new Event();
        event.button = e.button;
        event.count = e.count;
        event.data = e.data;
        event.display = e.display;
        event.stateMask = e.stateMask;
        event.time = e.time;
        event.widget = e.widget;
        event.x = e.x;
        event.y = e.y;
        for (Object l : mouseListeners.getListeners()) {
            ((Listener) l).handleEvent(event);
        }
    }


    /* ********************
     * UIのためのsetter
     */
    /**
     * リストアイテムのラベルプロバイダーを設定する
     * 
     * @param labelProvider
     *            リストに表示するアイテムのラベルプロバイダー
     */
    public void setLabelProvider(LabelProvider labelProvider) {
        sourceViewer.setLabelProvider(labelProvider);
        selectedViewer.setLabelProvider(labelProvider);
    }


    /**
     * リストアイテムのコンテントプロバイダーを設定する
     * 
     * @param provider
     *            リストアイテムのコンテントプロバイダー
     */
    public void setContentProvider(IStructuredContentProvider provider) {
        sourceViewer.setContentProvider(provider);
        selectedViewer.setContentProvider(provider);
    }


    /**
     * 初期状態で選択しておくアイテムを指定する。 {@link #setSourceInput(List)}
     * で指定したアイテムに含まれないアイテムは無視される。
     * 
     * @param initialValues
     *            初期値の一覧
     */
    public void setInitialValues(Object[] initialValues) {
        doAdd(initialValues);
    }


    /**
     * 初期状態で選択しておくアイテムを指定する。 {@link #setSourceInput(List)}
     * で指定したアイテムに含まれないアイテムは無視される。
     * 
     * @param initialValues
     *            初期値の一覧
     */
    public void setInitialValues(List<Object> initialValues) {
        setInitialValues(initialValues.toArray());
    }


    /**
     * 選択元リストの表示項目をセットする。選択したものリストはクリアされる。
     * 
     * @param input
     *            選択元リストの表示項目
     */
    public void setSourceInput(List<Object> input) {
        this.source = input;
        this.selected = new ArrayList<Object>(input.size());
        setInput(sourceViewer, this.source);
        setInput(selectedViewer, this.selected);
    }


    private static void setInput(TableViewer viewer, List<Object> input) {
        viewer.setInput(input);
        if ((input != null) && (input.size() > 0)) {
            viewer.getTable().select(0);
        }
        viewer.getTable().layout();
    }


    /**
     * リストの表示を変更する
     * 
     * @param layoutData
     *            リストのレイアウトデータ
     */
    public void setLayoutData(GridData layoutData) {
        sourceViewer.getControl().setLayoutData(layoutData);
        selectedViewer.getControl().setLayoutData(layoutData);
    }

}
