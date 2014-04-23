/**
 * @version $Id: DicImportWizardPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/03 15:03:26
 * @author kenta-uechi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.component.ElementListSelectionDialog1;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * 辞書インポート ウィザードページクラス
 * 
 * @author kenta-uechi
 */
public class DicImportWizardPage extends WizardPage {

    private List<ICoronaDic> listDics = new ArrayList<ICoronaDic>();
    protected FileFieldEditor editor;

    /** インポートファイルの拡張子格納用 */
    private String strFileExt = ""; //$NON-NLS-1$
    /** 登録辞書名設定テキストボックス */
    private Text dicNameText;
    /** ラベル情報インポート用チェックボックス */
    private Button boxItem;
    /** 分野名設定テキストボックス */
    private Text categoryText;
    /** 選択された分野名の情報を格納 */
    private TextItem selectedCategory;
    /** 登録済分野名一覧ボタン */
    private Button categoryButton;
    /** 既存辞書か新規辞書か判別するフラグ */
    private boolean bNewDic;


    /**
     * 辞書インポート ウィザードページクラスのコンストラクタ
     * 
     * @param pageName
     *            ページ名
     */
    public DicImportWizardPage(String pageName) {
        super(pageName);
        setTitle(pageName);
    }


    /**
     * ユーザインターフェース作成処理
     */
    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(fileSelectionData);
        composite.moveAbove(null);
        composite.setLayout(new GridLayout());
        setControl(composite);

        Composite field1 = CompositeUtil.defaultComposite(composite, 3);
        editor = new FileFieldEditor("fileSelect", Messages.DicImportWizardPage_labelFile, field1); //$NON-NLS-1$
        String[] extensions = { "*.xlsx;*.csv;*.xml", "*.*" };// 対象辞書拡張子のみ //$NON-NLS-1$ //$NON-NLS-2$
        editor.setFileExtensions(extensions);
        editor.setChangeButtonText(Messages.DicImportWizardPage_buttonBrowse);
        /* テキストボックスを入力不可にしてみた */
        // editor.getTextControl(composite).setEnabled(false);
        /********* 参照パスのテキストチェック *********/
        editor.getTextControl(field1).addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                setPageComplete(fieldValidate());
                setDispayFieldEnable();
                createDicTextName();
            }
        });
        Label label = new Label(field1, SWT.LEFT);
        label.setText(Messages.DicImportWizardPage_labelDic);
        dicNameText = new Text(field1, SWT.BORDER | SWT.SINGLE);
        dicNameText.setLayoutData(fileSelectionData);
        dicNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                setPageComplete(fieldValidate());
            };
        });
        dicNameText.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent check) {
                setPageComplete(fieldValidate());
            }
        });
        Label labelDummy = new Label(field1, SWT.LEFT);
        labelDummy.setText(""); //$NON-NLS-1$

        /* ラベル情報インポートチェックボックス */
        Label labelDummy2 = new Label(field1, SWT.LEFT);
        labelDummy2.setText(""); //$NON-NLS-1$
        boxItem = new Button(field1, SWT.CHECK);
        boxItem.setEnabled(false);
        boxItem.setText(Messages.DicImportWizardPage_labelLabelImport);
        /* チェックボックスの動作 */
        boxItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent check) {
                fieldValidate();
            }
        });
        Label labelDummy3 = new Label(field1, SWT.LEFT);
        labelDummy3.setText(""); //$NON-NLS-1$

        /* 分野情報 */
        Label labelCategory = new Label(field1, SWT.LEFT);
        labelCategory.setText(Messages.DicImportWizardPage_labelCategory);
        categoryText = new Text(field1, SWT.BORDER | SWT.SINGLE);
        categoryText.setEnabled(false);
        categoryText.setLayoutData(fileSelectionData);
        categoryText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                setPageComplete(fieldValidate());
                if ((selectedCategory != null) && !categoryText.getText().equals(selectedCategory.getText())) {
                    selectedCategory = null;
                }
            };
        });
        GridData buttonData = new GridData();
        categoryButton = new Button(field1, SWT.PUSH);
        categoryButton.setEnabled(false);
        categoryButton.setText(Messages.DicImportWizardPage_buttonExistCategory);
        categoryButton.setLayoutData(buttonData);
        categoryButton.pack();
        categoryButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ElementListSelectionDialog1 dialog = new ElementListSelectionDialog1(getShell(), new LabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((TextItem) element).getText();
                    }
                });
                dialog.setTitle(Messages.DicImportWizardPage_dialogSelCategory);
                dialog.setMessage(Messages.DicImportWizardPage_labelSelCategory);
                dialog.setMultipleSelection(false);
                dialog.setImeMode(SWT.NATIVE);
                dialog.setEmptyListMessage(Messages.DicImportWizardPage_labelNewCategory);

                /* カテゴリを取得 */
                List<TextItem> list = IoActivator.getService().getCategorys();
                if (list != null) {
                    dialog.setElements(list.toArray());
                }
                if (dialog.open() == Dialog.OK) {
                    selectedCategory = (TextItem) dialog.getFirstResult();
                    categoryText.setText(selectedCategory.getText());
                }
            }
        });
        setPageComplete(fieldValidate());
    }


    /**
     * 画面活性・非活性制御
     */
    protected void setDispayFieldEnable() {
        if (DicType.CATEGORY.getExtension().equals(strFileExt)) {
            categoryText.setEnabled(true);
            categoryButton.setEnabled(true);
        } else {
            categoryText.setText(""); //$NON-NLS-1$
            categoryText.setEnabled(false);
            categoryButton.setEnabled(false);
        }
        /* ユーザー辞書の場合のみラベルインポート有無を問う */
        if (DicType.COMMON.getExtension().equals(strFileExt) || DicType.CATEGORY.getExtension().equals(strFileExt)
                || DicType.SPECIAL.getExtension().equals(strFileExt)) {
            boxItem.setEnabled(true);
        } else {
            boxItem.setEnabled(false);
        }
    }


    /** 辞書名をセット */
    private void createDicTextName() {
        String fn = editor.getStringValue();
        if (fn.length() > 0) {
            int nameIndex = fn.lastIndexOf("\\"); //$NON-NLS-1$
            int extIndex = fn.indexOf("."); //$NON-NLS-1$
            if (nameIndex < extIndex) {
                /* 辞書名をセット */
                /* [xlsx][xml][csv]が拡張子として付与されるためそれらを除去 */
                dicNameText.setText(fn.substring(nameIndex + 1, extIndex));
            }
        }
    }


    protected void createNewFile() {


        /* ファイルのパスを取得 */
        final String path = editor.getStringValue();
        /* 作成する辞書名を取得 */
        final String dicName = dicNameText.getText();
        /* カテゴリ名を取得 */
        final String categoryName = categoryText.getText();
        /* ラベルインポート有無を取得 */
        final Boolean ignoreLabel = boxItem.getSelection();

        DicImportFinishProgress finishProgress = new DicImportFinishProgress(categoryName, dicName, path, strFileExt, ignoreLabel);
        IWizardContainer container = getContainer();
        try {
            container.run(true, true, finishProgress);
        } catch (InvocationTargetException | InterruptedException e1) {
            e1.printStackTrace();
        }
    }


    /**
     * ページ入力内容チェック
     */
    private boolean fieldValidate() {
        if (editor.getStringValue() != null && editor.getStringValue().length() == 0) {
            setErrorMessage(Messages.DicImportWizardPage_errSelFile);
            return false;
        }
        if (!checkImportFile()) {
            setErrorMessage(Messages.DicImportWizardPage_errUnsupport);
            return false;
        }

        if (dicNameText != null) {
            if (dicNameText.getText().trim().length() == 0) {
                setErrorMessage(Messages.DicImportWizardPage_errInputDicName);
                return false;
            }

            if (!stringCheck(dicNameText.getText())) {
                setErrorMessage(Messages.DicImportWizardPage_errInputCharacter);
                return false;
            }
        }

        if ((categoryText != null) && (categoryText.getEnabled()) && categoryText.getText().trim().length() == 0) {
            setErrorMessage(Messages.DicImportWizardPage_errSelCategory);
            return false;
        }

        if (!checkOverlapDicName()) {
            setErrorMessage(Messages.DicImportWizardPage_errImpCategory);
            return false;
        }
        /* 既存辞書と新規でメッセージを変える */
        if (bNewDic) {
            setDescription(Messages.DicImportWizardPage_labelNewDic);
        } else {
            setDescription(Messages.DicImportWizardPage_labelExistDic);
        }

        setErrorMessage(null);
        return true;
    }


    /**
     * 辞書名の重複チェック
     * 
     * @return boolean
     *         重複していないならtrue、重複してるならfalse
     */
    private boolean checkOverlapDicName() {
        listDics.clear();
        /* 全辞書の取得 */
        listDics.addAll(IoActivator.getService().getDictionarys(ICoronaDic.class));
        String name = dicNameText.getText() + "." + strFileExt; //$NON-NLS-1$
        bNewDic = true; /* 辞書フラグ */
        for (ICoronaDic wk : listDics) {
            if (wk.getName().equals(name)) {
                /* 分野辞書の場合、カテゴリIDチェック */
                if (strFileExt.equals(DicType.CATEGORY.getExtension())) {
                    String str = ((IUserDic) wk).getDicCategory().getText();
                    if (!categoryText.getText().equals(str)) {
                        return false;
                    }
                }
                bNewDic = false;
            }
        }
        return true;
    }


    /**
     * インポートファイルの存在チェック
     * 一般辞書、分野辞書、同義語辞書、ゆらぎ辞書、専門辞書のみOKとする
     * 
     * @return boolean
     *         拡張子が一致ならtrue、不一致ならfalse
     */
    private boolean checkImportFile() {
        String filePath = editor.getStringValue();
        if (filePath.indexOf('.') == -1) {
            return false;
        }
        /* [xlsx][xml][csv]が拡張子として付与されるためそれらを除去 */
        strFileExt = filePath.substring(filePath.indexOf(".") + 1, filePath.lastIndexOf(".")); /* 拡張子取得 *///$NON-NLS-1$ //$NON-NLS-2$
        File readFile = new File(filePath);
        if (readFile.exists()) {
            if (DicType.COMMON.getExtension().equals(strFileExt) || DicType.CATEGORY.getExtension().equals(strFileExt)
                    || DicType.SPECIAL.getExtension().equals(strFileExt) || DicType.FLUC.getExtension().equals(strFileExt)
                    || DicType.SYNONYM.getExtension().equals(strFileExt) || DicType.PATTERN.getExtension().equals(strFileExt)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 入力文字(辞書名)のエラーチェック
     * 
     * @param str
     *            チェック文字列
     * @return boolean
     *         入力文字が正常ならtrue、不正ならfalse
     */
    private static boolean stringCheck(String str) {
        if (!(str.isEmpty())) {
            if (str.matches("[[ぁ-ゖ][ァ-ヺ][ａ-ｚＡ-Ｚ][一-龻][0-9０-９][a-zA-Z][-_ー]]*")) { //$NON-NLS-1$
                return true;
            }
        }
        return false;
    }
}
