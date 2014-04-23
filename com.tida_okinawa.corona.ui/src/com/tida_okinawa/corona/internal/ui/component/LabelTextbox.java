/**
 * @version $Id: LabelTextbox.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2010/12/25
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * ラベル付きテキストボックス
 * 
 * @author KMorishima
 * 
 */
public class LabelTextbox extends AbstractWidget {
    private Composite rootComposite;
    private Text textbox;
    private Label label;


    /**
     * 与えられたlabelTextを持ったTextを作る
     * 
     * @param parent
     * @param labelText
     */
    public LabelTextbox(Composite parent, String labelText) {
        rootComposite = new Composite(parent, SWT.NONE);
        rootComposite.setLayout(new GridLayout(2, false));
        rootComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        rootComposite.setFont(parent.getFont());

        label = new Label(rootComposite, SWT.NONE);
        label.setLayoutData(new GridData());
        label.setFont(parent.getFont());
        label.setText(labelText);

        textbox = new Text(rootComposite, SWT.BORDER);
        textbox.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        textbox.setFont(parent.getFont());
    }


    /**
     * @return テキストボックス。リスナーをつけたりするためのgetter
     */
    public Text getTextbox() {
        return textbox;
    }


    /**
     * @return 現在入力されている文字列
     */
    public String getText() {
        return textbox.getText();
    }


    /**
     * textを現在の文字列として設定する
     * 
     * @param text
     *            任意の文字列
     */
    public void setText(String text) {
        if (text == null) {
            text = "";
        }
        textbox.setText(text);
    }


    /* ****************************************
     * layout
     */
    /**
     * 全体にかかるLayoutDataをセットする
     * 
     * @param layoutData
     */
    public void setLayoutData(Object layoutData) {
        rootComposite.setLayoutData(layoutData);
    }


    /**
     * ラベルの幅を設定する
     * 
     * @param width
     *            最小幅
     * @param hint
     *            初期幅
     */
    public void setLabelWidth(int width, int hint) {
        GridData layoutData = new GridData();
        layoutData.minimumWidth = width;
        layoutData.widthHint = hint;
        label.setLayoutData(layoutData);
    }


    /* ****************************************
     * その他
     */
    /**
     * 有効無効を切り替える
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        label.setEnabled(enabled);
        textbox.setEnabled(enabled);
    }


    /**
     * @return このコンポーネントの直親
     */
    public Composite getComposite() {
        return rootComposite;
    }


    @Override
    public void setFocus() {
        textbox.setFocus();
    }


    @Override
    public void dispose() {
    }


    /**
     * なんのエラーチェックもしない
     * 
     * @see com.tida_okinawa.corona.internal.ui.component.zipc.atg.ui.util.widgets.ErrorHandler#handling()
     */
    @Override
    protected int handling() {
        return NO_ERROR;
    }
}
