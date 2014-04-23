/**
 * @version $Id: ColumnSelectWizardPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/27 14:39:52
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.component.ElementListSelectionDialog1;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;

/**
 * @author kousuke-morishima
 */
public class ColumnSelectWizardPage extends WizardPageBase {
    private Label idFieldLabel;
    private Label productFieldLabel;
    private Button idClearButton;
    private Button productClearButton;
    private Button idFieldButton;
    private Button productFieldButton;
    private IClaimData claim;
    private List<IFieldHeader> fieldHeaders;

    int headerId;
    int headerProduct;


    protected ColumnSelectWizardPage(String pageName) {
        super(pageName);
        setMessage(Messages.ColumnSelectWizardPage_DefaultMessage);
    }


    @Override
    public void createControl(Composite parent) {
        Composite composite = CompositeUtil.defaultComposite(parent, 1);
        GridLayout gridLayout = new GridLayout(4, false);
        gridLayout.marginWidth = 10;
        gridLayout.marginHeight = 5;
        composite.setLayout(gridLayout);
        createIdFieldGroup(composite);
        createProductFieldGroup(composite);
        setControl(composite);
        fieldValidate();
    }


    /**
     * 変更されたカラムをDBへ登録
     */
    public void finished() {
        claim.setDispIdField(headerId);
        claim.setProductField(headerProduct);
        claim.commit();
    }


    private void createIdFieldGroup(Composite parent) {
        Composite composite = parent;
        /* データファイルのセット(ラベル、テキストボックス、ボタン) */
        /* ラベル */
        Label dataLabel = new Label(composite, SWT.NONE);
        dataLabel.setText(Messages.ColumnSelectWizardPage_Label_ID);
        if (claim != null) {
            headerId = claim.getDispIdField();
            IFieldHeader field = claim.getFieldInformation(claim.getDispIdField());
            if (field != null) {
                /* すでにIDカラムが指定されている場合は、初期値として表示する。 */
                idFieldLabel = createClumnLabel(composite, field.getDispName());
                fieldHeaders = claim.getFieldInformations();
            } else {
                idFieldLabel = createClumnLabel(composite, ""); //$NON-NLS-1$
            }
        } else {
            /* 新規登録の場合は初期値なし。 */
            idFieldLabel = createClumnLabel(composite, Messages.ColumnSelectWizardPage_DefaultColumnValue_ID);
            headerId = 1;
        }
        /* ボタン */
        idFieldButton = new Button(composite, SWT.PUSH);
        idFieldButton.setText(Messages.ColumnSelectWizardPage_ButtonLabel_Refer);
        /* (データファイルの)「参照」ボタンを押したときの処理 */
        idFieldButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent columnChengeDialog) {
                Shell shell = getShell();
                ElementListSelectionDialog1 dialog = new ElementListSelectionDialog1(shell, new LabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((IFieldHeader) element).getDispName();
                    }
                });
                dialog.setTitle(Messages.ColumnSelectWizardPage_DialogTitle_Select_ID);
                dialog.setMessage(Messages.ColumnSelectWizardPage_DialogMessage_Select_ID);
                dialog.setElements(setToObject());
                dialog.setMultipleSelection(false);
                if (dialog.open() == Dialog.OK) {
                    IFieldHeader result = (IFieldHeader) dialog.getFirstResult();
                    headerId = result.getId();
                    idFieldLabel.setText(result.getName());
                }
                fieldValidate();
            }
        });

        idClearButton = new Button(composite, SWT.PUSH);
        idClearButton.setText(Messages.ColumnSelectWizardPage_ButtonLabel_Initialize);
        /* IDカラムの「Clear」ボタンを押したときの処理 */
        idClearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                idFieldLabel.setText(Messages.ColumnSelectWizardPage_DefaultColumnValue_ID);
                headerId = 1;
                fieldValidate();
            }
        });
    }


    /**
     * IDカラムの位置を返却
     * 
     * @return
     *         IDカラムの位置
     */
    public int getIdFieldIndex() {
        return headerId;
    }


    private void createProductFieldGroup(Composite parent) {
        Composite composite = parent;
        /* データファイルのセット(ラベル、テキストボックス、ボタン) */
        /* ラベル */
        Label dataLabel = new Label(composite, SWT.NONE);
        dataLabel.setText(Messages.ColumnSelectWizardPage_Label_Target);
        if (claim != null) {
            headerProduct = claim.getProductField();

            IFieldHeader field = claim.getFieldInformation(claim.getProductField());
            if (field != null) {
                /* すでにIDカラムが指定されている場合は、初期値として表示する。 */
                productFieldLabel = createClumnLabel(composite, field.getDispName());
            } else {
                productFieldLabel = createClumnLabel(composite, ""); //$NON-NLS-1$
            }
        } else {
            /* 新規登録の場合は初期値なし。 */
            productFieldLabel = createClumnLabel(composite, ""); //$NON-NLS-1$
        }
        /* ボタン */
        productFieldButton = new Button(composite, SWT.PUSH);
        productFieldButton.setText(Messages.ColumnSelectWizardPage_ButtonLabel_Refer);
        /* (データファイルの)「参照」ボタンを押したときの処理 */
        productFieldButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent columnChengeDialog) {
                Shell shell = getShell();
                ElementListSelectionDialog1 dialog = new ElementListSelectionDialog1(shell, new LabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((IFieldHeader) element).getDispName();
                    }
                });
                dialog.setTitle(Messages.ColumnSelectWizardPage_DialogTitle_Select_Target);
                dialog.setMessage(Messages.ColumnSelectWizardPage_DialogMessage_Select_Target);
                dialog.setElements(setToObject());
                dialog.setMultipleSelection(false);
                if (dialog.open() == Dialog.OK) {
                    IFieldHeader result = (IFieldHeader) dialog.getFirstResult();
                    headerProduct = result.getId();
                    productFieldLabel.setText(result.getName());
                }
                fieldValidate();
            }
        });

        productClearButton = new Button(composite, SWT.PUSH);
        productClearButton.setText(Messages.ColumnSelectWizardPage_ButtonLabel_Initialize);
        /* ターゲットカラムの「Clear」ボタンを押したときの処理 */
        productClearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                productFieldLabel.setText(""); //$NON-NLS-1$
                headerProduct = 0;
                fieldValidate();
            }
        });
    }


    /**
     * ターゲットカラムの位置を返却
     * 
     * @return
     *         ターゲットカラムの位置
     */
    public int getProductFieldIndex() {
        return headerProduct;
    }


    private static Label createClumnLabel(Composite parent, String text) {
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        Label retLabel = new Label(parent, SWT.SINGLE | SWT.BORDER);
        retLabel.setText(text);
        retLabel.setLayoutData(gridData);
        return retLabel;
    }


    /**
     * 問い合わせデータのDB情報を設定
     * 
     * @param claim
     *            問い合わせデータのDB情報
     */
    public void setClaimData(IClaimData claim) {
        this.claim = claim;
    }


    /**
     * フィールド選択ウィザードのカラム情報を返却
     * 
     * @return
     *         問い合わせデータのカラム情報
     */
    public Object[] setToObject() {
        Object[] s = fieldHeaders.toArray(new Object[fieldHeaders.size()]);
        return s;
    }


    /**
     * カラム情報をセットする
     * 
     * @param fieldHeaders
     *            問い合わせデータのカラム情報
     */
    public void setFieldHeaders(List<IFieldHeader> fieldHeaders) {
        this.fieldHeaders = fieldHeaders;
    }


    void fieldValidate() {
        if (productFieldLabel.getText().length() != 0 && idFieldLabel.getText().length() != 0) {
            setPageComplete(true);
        } else {
            setPageComplete(false);
        }
    }


    @Override
    public void setFocus() {
        idFieldButton.setFocus();
    }
}
