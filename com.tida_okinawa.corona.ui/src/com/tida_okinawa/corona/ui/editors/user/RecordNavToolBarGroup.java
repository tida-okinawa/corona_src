/**
 * @version $Id: RecordNavToolBarGroup.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/24 10:55:40
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.editors.IPagingDataProvider;

/**
 * @author kousuke-morishima
 */
public class RecordNavToolBarGroup {
    /*
     * データ件数ラベルを表示する
     * ボタンが押されたとき、表示するデータを変える
     */

    IPagingDataProvider dataProvider;


    public RecordNavToolBarGroup(Composite parent, IPagingDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        createContent(parent);
    }

    Viewer viewer;


    public void setViewer(Viewer viewer) {
        this.viewer = viewer;
    }

    private Composite root;


    public Composite getControl() {
        return root;
    }

    private Label dataNumLabel;


    private void createContent(Composite parent) {
        root = CompositeUtil.defaultComposite(parent, 2);
        ((GridLayout) root.getLayout()).marginHeight = 0;
        ((GridData) root.getLayoutData()).grabExcessVerticalSpace = false;

        dataNumLabel = CompositeUtil.createLabel(root, "", -1);
        ((GridData) dataNumLabel.getLayoutData()).grabExcessVerticalSpace = false;
        updateDataNumLabel();

        ToolBarManager manager = new ToolBarManager(SWT.RIGHT);
        ToolBar bar = manager.createControl(root);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        bar.setLayout(layout);
        GridData barLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
        barLayoutData.horizontalAlignment = SWT.RIGHT;
        bar.setLayoutData(barLayoutData);

        CompositeUtil.createToolItem(bar, "", "最初へ", Icons.INSTANCE.get(Icons.IMG_TOOL_FIRST), new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dataProvider.hasPrev()) {
                    viewer.setInput(dataProvider.first());
                    updateDataNumLabel();
                    firePageMoved(FIRST_BTN);
                }
            }
        });

        CompositeUtil.createToolItem(bar, "", "前へ", Icons.INSTANCE.get(Icons.IMG_TOOL_PREV), new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dataProvider.hasPrev()) {
                    viewer.setInput(dataProvider.prev());
                    updateDataNumLabel();
                    firePageMoved(PREV_BTN);
                }
            }
        });

        CompositeUtil.createToolItem(bar, "", "次へ", Icons.INSTANCE.get(Icons.IMG_TOOL_NEXT), new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dataProvider.hasNext()) {
                    viewer.setInput(dataProvider.next());
                    updateDataNumLabel();
                    firePageMoved(NEXT_BTN);
                }
            }
        });

        CompositeUtil.createToolItem(bar, "", "最後へ", Icons.INSTANCE.get(Icons.IMG_TOOL_LAST), new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dataProvider.hasNext()) {
                    viewer.setInput(dataProvider.last());
                    updateDataNumLabel();
                    firePageMoved(LAST_BTN);
                }
            }
        });
    }


    /**
     * 表示しているデータの件数ラベルを更新する
     */
    public void updateDataNumLabel() {
        StringBuilder label = new StringBuilder("(");
        int current = dataProvider.currentIndex() + dataProvider.current(false).size();
        label.append(current).append("/").append(dataProvider.totalCount()).append(")");
        dataNumLabel.setText(label.toString());
    }


    /* ****************************************
     * Listener
     */
    public static final int FIRST_BTN = 0;
    public static final int PREV_BTN = 1;
    public static final int NEXT_BTN = 2;
    public static final int LAST_BTN = 3;

    private ListenerList listeners = new ListenerList();


    /**
     * 表示されるページが変わったときに呼び出されるリスナーを登録する。
     * 渡されるEventは {@link Event#data}に押されたボタンが入る
     * 
     * @param listener
     */
    public void addPageMovedListener(Listener listener) {
        listeners.add(listener);
    }


    void firePageMoved(int btnType) {
        Event e = new Event();
        e.data = btnType;
        for (Object listener : listeners.getListeners()) {
            if (listener instanceof Listener) {
                ((Listener) listener).handleEvent(e);
            }
        }
    }

}
