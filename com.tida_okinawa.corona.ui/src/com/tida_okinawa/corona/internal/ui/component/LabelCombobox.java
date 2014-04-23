/**
 * @version $Id: LabelCombobox.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/02/15
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.component;


import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * ラベル付きコンボボックスコンポーネント
 * 
 * @author KMorishima
 * 
 */
public class LabelCombobox extends AbstractWidget implements ModifyListener {
    /**
     * 入力項目が必須であることを示す
     */
    public static final int REQUIRED = 1 << 0;


    public LabelCombobox(Composite parent, int style, String labelText) {
        createContents(parent, style, labelText);
    }

    /* ****************************************
     * create UI
     */
    private Composite rootComposite;
    private Label label;
    private Label required;
    private ComboViewer text;


    protected void createContents(Composite parent, int style, String labelText) {
        rootComposite = new Composite(parent, SWT.NONE);
        rootComposite.setLayout(new GridLayout(3, false));
        rootComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        GridData requiredData = new GridData();
        requiredData.verticalAlignment = SWT.TOP;
        required = new Label(rootComposite, SWT.NONE);
        required.setLayoutData(requiredData);

        GridData labelData = new GridData(SWT.FILL, SWT.NONE, false, false);
        labelData.widthHint = 140;
        label = new Label(rootComposite, SWT.NONE);
        label.setText(labelText);
        label.setFont(parent.getFont());
        label.setLayoutData(labelData);

        GridData gridData = new GridData();
        gridData.widthHint = 350;
        label.setLayoutData(labelData);
        Combo combo = new Combo(rootComposite, style);
        combo.setLayoutData(gridData);
        combo.addModifyListener(this);
        text = new ComboViewer(combo);
        text.setContentProvider(new ArrayContentProvider());

        setEnabled(true);
    }


    public void setLabelProvider(LabelProvider labelProvider) {
        text.setLabelProvider(labelProvider);
    }

    /* ****************************************
     * 操作口
     */

    /* ********************
     */
    private boolean isRequired = false;


    /**
     * @return この入力項目が必須ならtrue
     */
    public boolean getRequired() {
        return isRequired;
    }


    /**
     * @param required
     *            この入力項目が必須かどうか指定する
     */
    public void setRequired(boolean required) {
        isRequired = required;
        this.required.setText((required) ? "*" : "");
    }


    /* ********************
     * 値入力
     */
    /**
     * @return テキストフィールドの文字列
     */
    public String getText() {
        return text.getCombo().getText();
    }


    /**
     * 指定された文字列を、テキストフィールドと履歴に追加する
     * 
     * @param newText
     *            空文字の場合、無視される
     */
    public void setText(String newText) {
        if (newText.length() > 0) {
            String[] currentItems = text.getCombo().getItems();
            int selectionIndex = -1;
            for (int i = 0; i < currentItems.length; i++) {
                if (currentItems[i].equals(newText)) {
                    selectionIndex = i;
                    break;
                }
            }
            if (selectionIndex < 0) {
                int oldLength = currentItems.length;
                String[] newItems = new String[oldLength + 1];
                System.arraycopy(currentItems, 0, newItems, 0, oldLength);
                newItems[oldLength] = newText;
                text.getCombo().setItems(newItems);
            }
        }
        text.getCombo().setText(newText);
    }


    /**
     * テキストを空にします。SWT.READ_ONLYがセットされている場合、何も選択していない状態にします。
     */
    public void clearText() {
        text.getCombo().deselectAll();
    }


    /* ********************
     * 履歴
     */
    /**
     * @return テキストフィールドのドロップダウンの履歴
     */
    public String[] getItems() {
        return text.getCombo().getItems();
    }


    /**
     * @return 現在選択しているアイテム。may be null
     */
    public Object getSelection() {
        return ((IStructuredSelection) text.getSelection()).getFirstElement();
    }


    /**
     * テキストフィールドのドロップダウンに文字列を設定する
     * 
     * @param items
     */
    public void setItems(String[] items) {
        text.getCombo().setItems(items);
    }


    public void setInput(Object[] input) {
        text.setInput(input);
    }


    public void setInput(List<?> input) {
        text.setInput(input);
    }


    /* ********************
     * 有効無効切り替え
     */
    /**
     * @return このコンポーネントが有効ならtrue
     */
    public boolean getEnabled() {
        return label.getEnabled();
    }


    /**
     * すべてのアイテムの有効/無効を切り替える
     * 
     * @param enabled
     *            有効ならtrue
     */
    public void setEnabled(boolean enabled) {
        label.setEnabled(enabled);
        text.getCombo().setEnabled(enabled);
    }


    /* ****************************************
     * 取得口
     */

    /**
     * @return 入力領域のコンボボックス
     */
    public Combo getTextbox() {
        return text.getCombo();
    }


    /**
     * @return このアイテムのルートコンポジット
     */
    public Composite getComposite() {
        return rootComposite;
    }


    /* ****************************************
     * set listeners
     */
    /**
     * テキストフィールドにlistenerを追加する
     * 
     * @param listener
     */
    public void addModifyListener(ModifyListener listener) {
        text.getCombo().addModifyListener(listener);
    }


    /**
     * テキストフィールドからlistenerを削除する
     * 
     * @param listener
     */
    public void removeModifyListener(ModifyListener listener) {
        text.getCombo().removeModifyListener(listener);
    }


    /**
     * テキストフィールドにlistenerを追加する
     * 
     * @param listener
     */
    public void addVerifyListener(VerifyListener listener) {
        text.getCombo().addVerifyListener(listener);
    }


    /**
     * テキストフィールドからlistenerを削除する
     * 
     * @param listener
     */
    public void removeVerifyListener(VerifyListener listener) {
        text.getCombo().removeVerifyListener(listener);
    }


    /**
     * コンボボックスにlistenerを追加する
     * 
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        text.getCombo().addSelectionListener(listener);
    }


    /**
     * コンボボックスからlistenerを削除する
     * 
     * @param listener
     */
    public void removeSelectionListener(SelectionListener listener) {
        text.getCombo().removeSelectionListener(listener);
    }


    /**
     * テキストフィールドにフォーカスをあわせる
     */
    @Override
    public void setFocus() {
        text.getCombo().setFocus();
    }


    /* ****************************************
     * validate
     */
    @Override
    protected int handling() {
        // 必須属性が付いているとき、値が入力されていなければエラーを保持する

        int error = NO_ERROR;

        if (!isRequired) {
            return error;
        }

        String input = text.getCombo().getText();
        if (input.trim().length() == 0) {
            error |= REQUIRED;
        }
        return error;
    }


    /**
     * @see #setRequired(boolean)
     */
    @Override
    public void setListeningError(int errorTypes) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void addListeningError(int errorTypes) {
        throw new UnsupportedOperationException();
    }


    /* ****************************************
     * implements Listener
     */
    /* ********************
     * modify
     */
    @Override
    public void modifyText(ModifyEvent e) {
        check();
    }


    /* ****************************************
     * Layout
     */
    public void setLabelLayout(Object layoutData) {
        label.setLayoutData(layoutData);
    }


    public void setTextLayout(Object layoutData) {
        text.getCombo().setLayoutData(layoutData);
    }


    /* ****************************************
     * other
     */
    @Override
    public void dispose() {
        label.dispose();
        text.getCombo().dispose();
    }

}
