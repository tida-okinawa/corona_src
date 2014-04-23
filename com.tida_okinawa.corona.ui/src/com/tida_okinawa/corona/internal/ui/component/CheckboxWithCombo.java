/**
 * @version $Id: CheckboxWithCombo.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/28 12:01:56
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.component;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author kousuke-morishima
 */
public class CheckboxWithCombo extends CheckboxWith {
    private Combo combo;
    private String[] items;


    /**
     * デフォルトスタイル（{@link SWT#DROP_DOWN} | {@link SWT#READ_ONLY}）のコンボボックス
     * 
     * @param parent
     * @param label
     */
    public CheckboxWithCombo(Composite parent, String label) {
        this(parent, SWT.DROP_DOWN | SWT.READ_ONLY, label, new String[0]);
    }


    public CheckboxWithCombo(Composite parent, int style, String label) {
        this(parent, style, label, new String[0]);
    }


    /**
     * デフォルトスタイル（{@link SWT#DROP_DOWN} | {@link SWT#READ_ONLY}）のコンボボックス
     * 
     * @param parent
     * @param label
     * @param items
     *            not null
     */
    public CheckboxWithCombo(Composite parent, String label, String[] items) {
        this(parent, SWT.DROP_DOWN | SWT.READ_ONLY, label, items);
    }


    /**
     * @param parent
     * @param style
     * @param label
     * @param items
     *            not null
     */
    public CheckboxWithCombo(Composite parent, int style, String label, String[] items) {
        super(parent, label, style);
        setItems(items);
    }


    /**
     * @param items
     *            not null
     */
    public void setItems(String[] items) {
        combo.setItems(items);
        this.items = Arrays.copyOf(items, items.length);
    }


    @Override
    public String getValue() {
        int index = combo.getSelectionIndex();
        if (index != -1) {
            return items[combo.getSelectionIndex()];
        }
        return null;
    }


    /**
     * 値を選択する。リストにvalueがない場合は、なにもしない。
     * 
     * @param value
     */
    public void setValue(String value) {
        if (value != null) {
            for (int i = 0; i < items.length; i++) {
                if (value.equals(items[i])) {
                    combo.select(i);
                    setChecked(true);
                    return;
                }
            }
        }
    }


    public int getValueIndex() {
        return combo.getSelectionIndex();
    }


    /**
     * 指定されたindexを選択する。indexが範囲外のときはなにもしない。
     * 
     * @param index
     */
    public void setValueIndex(int index) {
        if ((index >= 0) && (index < items.length)) {
            combo.select(index);
            setChecked(true);
        }
    }


    @Override
    protected Control createWithControl(Composite parent, int style) {
        combo = new Combo(parent, style);
        combo.setLayoutData(CompositeUtil.gridData(true, false, 1, 1));
        return combo;
    }


    /* ****************************************
     * Listeners
     */
    public void addSelectionListener(SelectionListener listener) {
        combo.addSelectionListener(listener);
    }


    public void removeSelectionListener(SelectionListener listener) {
        combo.removeSelectionListener(listener);
    }
}
