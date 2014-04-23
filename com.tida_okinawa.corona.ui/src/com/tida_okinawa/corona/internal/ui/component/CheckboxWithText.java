/**
 * @version $Id: CheckboxWithText.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/25 20:21:39
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * @author kousuke-morishima
 */
public class CheckboxWithText extends CheckboxWith {
    private Text text;


    /**
     * デフォルトスタイル（{@link SWT#BORDER} | {@link SWT#SINGLE}）のテキスト
     * 
     * @param parent
     * @param label
     */
    public CheckboxWithText(Composite parent, String label) {
        super(parent, label, SWT.BORDER | SWT.SINGLE);
    }


    /**
     * @param parent
     * @param style
     *            {@link Text}のスタイル
     * @param label
     */
    public CheckboxWithText(Composite parent, int style, String label) {
        super(parent, label, style);
    }


    @Override
    protected Control createWithControl(Composite parent, int style) {
        text = CompositeUtil.createText(parent, style, -1);
        return text;
    }


    @Override
    public String getValue() {
        assert text != null;
        return text.getText();
    }


    public void setValue(String value) {
        assert text != null;
        text.setText(value);
        setChecked(true);
    }
}
