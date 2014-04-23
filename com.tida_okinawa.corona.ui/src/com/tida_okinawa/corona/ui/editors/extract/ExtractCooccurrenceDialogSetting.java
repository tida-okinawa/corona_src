/**
 * @version $Id: ExtractCooccurrenceDialogSetting.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/22 13:41:36
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

/**
 * @author s.takuro
 *         #173 パターン自動生成（共起抽出）
 */
public class ExtractCooccurrenceDialogSetting extends Dialog {

    private String lowestHitNum = Messages.EXTRACT_COOCCURRENCE_MIN_NUMBER;
    private boolean isOrderCooccurrence = false;


    /**
     * 共起条件UI（ダイアログ）
     * 
     * @param parentShell
     *            親シェル
     */
    protected ExtractCooccurrenceDialogSetting(Shell parentShell) {
        super(parentShell);
    }


    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.EXTRACT_COOCCURRENCE_SETTING_TITLE);
    }


    /**
     * 共起語数の取得
     * 
     * @return 共起語数
     */
    public int getExtractCount() {
        return Integer.parseInt(lowestHitNum);
    }


    /**
     * 共起順を考慮するかどうか
     * 
     * @return true:考慮する、false:考慮しない
     */
    public boolean getOrderCooccurrence() {
        return isOrderCooccurrence;
    }


    /* ****************************************
     * UI
     */
    Spinner countSpinner = null;
    Button checkButton = null;


    @Override
    protected Control createDialogArea(Composite parent) {
        parent.setLayout(new GridLayout());
        /* 1行目（概要） */
        Label outlineLabel = new Label(parent, SWT.NONE);
        outlineLabel.setText(Messages.EXTRACT_COOCCURRENCE_SETTING_CONDITION);
        GridData outlineGrid = new GridData(SWT.FILL, SWT.FILL, true, true);
        outlineGrid.horizontalSpan = 3;
        outlineLabel.setLayoutData(outlineGrid);
        outlineLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        /* 2行目（共起数の選択） */
        Composite compCount = new Composite(parent, SWT.NONE);
        compCount.setLayout(new GridLayout(3, false));

        Label countLabel = new Label(compCount, SWT.NONE);
        countLabel.setText(Messages.EXTRACT_COOCCURRENCE_SETTING_COMBO_NUMBER);
        countLabel.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));

        countSpinner = new Spinner(compCount, SWT.BORDER);
        countSpinner.setMaximum(5);
        countSpinner.setMinimum(2);
        countSpinner.setSelection(2);
        countSpinner.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));

        Label conditionLabel = new Label(compCount, SWT.NONE);
        conditionLabel.setText(Messages.EXTRACT_COOCCURRENCE_SETTING_COMBO_SCOPE);
        conditionLabel.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));

        /* 3行目（チェックボックス） */
        Composite compCheck = new Composite(parent, SWT.NONE);
        compCheck.setLayout(new GridLayout());
        checkButton = new Button(compCheck, SWT.CHECK);
        checkButton.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
        checkButton.setText(Messages.EXTRACT_COOCCURRENCE_SETTING_CHECK);
        checkButton.setSelection(true);

        return parent;
    }


    @Override
    protected void okPressed() {
        lowestHitNum = countSpinner.getText();
        isOrderCooccurrence = checkButton.getSelection();
        super.okPressed();
    }
}
