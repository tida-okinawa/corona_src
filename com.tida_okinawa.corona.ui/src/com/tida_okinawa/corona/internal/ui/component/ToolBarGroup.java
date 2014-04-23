/**
 * @version $Id: ToolBarGroup.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/15 17:44:22
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.component;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;


/**
 * @author kousuke-morishima
 */
public abstract class ToolBarGroup {

    /**
     * ツールバーグループを作る
     * 
     * @param parent
     */
    public ToolBarGroup(Composite parent) {
        createContent(parent);
    }

    private Composite root;


    /**
     * @return このツールバーのコンポジット
     */
    public Composite getControl() {
        return root;
    }

    private ToolBar bar;


    protected void createContent(Composite parent) {
        root = CompositeUtil.defaultComposite(parent, 1);
        ((GridLayout) root.getLayout()).marginHeight = 0;
        ((GridData) root.getLayoutData()).grabExcessVerticalSpace = false;

        ToolBarManager manager = new ToolBarManager();
        bar = manager.createControl(root);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        bar.setLayout(layout);
        GridData barLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
        barLayoutData.horizontalAlignment = SWT.RIGHT;
        bar.setLayoutData(barLayoutData);

        addButtons();
    }


    /**
     * アクションボタンを追加する
     * 
     * @param text
     *            ボタンの表示文字。not null
     * @param tooltip
     *            マウスホバー時に表示する文字列。
     * @param icon
     *            不要ならnull
     * @param listener
     *            ボタンが押されたときの処理を行うリスナー
     * @return
     */
    public ToolItem createToolItem(String text, String tooltip, Image icon, SelectionListener listener) {
        return CompositeUtil.createToolItem(bar, text, tooltip, icon, listener);
    }

    Map<ToolItem, Integer> buttons = new HashMap<ToolItem, Integer>();


    /**
     * @see CompositeUtil#createToolItem(ToolBar, int, String, String, Image,
     *      SelectionListener)
     */
    protected ToolItem createToolItem(int style, String text, String tooltip, Image icon, SelectionListener listener) {
        ToolItem item = CompositeUtil.createToolItem(bar, style, text, tooltip, icon, listener);
        item.addSelectionListener(selectionListener);
        buttons.put(item, buttons.size());
        return item;
    }


    /**
     * {@link #createToolItem(String, String, Image, SelectionListener)}
     * を使って、ボタンを作る
     */
    abstract protected void addButtons();

    /* ****************************************
     * Listener
     */
    private SelectionListener selectionListener = new SelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            buttonSelection(e);
        }


        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    };
    private ListenerList listeners = new ListenerList();


    public void addListener(SelectionListener listener) {
        listeners.add(listener);
    }


    public void removeListener(SelectionListener listener) {
        listeners.remove(listener);
    }


    /**
     * @param item
     *            押されたボタン
     */
    protected void buttonSelection(SelectionEvent event) {
        for (Object l : listeners.getListeners()) {
            ((SelectionListener) l).widgetSelected(event);
        }
    }
}
