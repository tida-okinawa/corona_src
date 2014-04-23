/**
 * @version $Id: TextWithBrowseButtonGroup.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2010/11/18
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.component;

import java.io.File;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * ラベル、テキストフィールド、参照ボタンがひとつになったコンポーネント
 * 
 * @author KMorishima
 * 
 */
public class TextWithBrowseButtonGroup {
    /**
     * @param parent
     * @param labelText
     *            ラベル文字列
     * @param buttonText
     *            参照ボタンの文字列
     */
    public TextWithBrowseButtonGroup(Composite parent, String labelText, String buttonText) {
        this(parent, SWT.SINGLE | SWT.BORDER, labelText, buttonText);
    }


    /**
     * @param parent
     * @param style
     *            Textに指定できるスタイル
     * @param labeltext
     * @param buttonText
     * @see SWT#READ_ONLY
     * @see SWT#SINGLE
     * @see SWT#MULTI
     */
    public TextWithBrowseButtonGroup(Composite parent, int style, String labelText, String buttonText) {
        createContents(parent, style, labelText, buttonText);
    }

    private Composite rootComposite;
    private Label label;
    private Text textField;
    private Button browseButton;


    private void createContents(Composite parent, int style, String labelText, String buttonText) {
        rootComposite = new Composite(parent, SWT.NONE);
        rootComposite.setLayout(new GridLayout(3, false));
        rootComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        rootComposite.setFont(parent.getFont());

        label = new Label(rootComposite, SWT.NONE);
        label.setText(labelText + " : ");
        label.setFont(parent.getFont());

        GridData textData = new GridData(SWT.FILL, SWT.NONE, true, false);
        textData.widthHint = 200;
        textField = new Text(rootComposite, style);
        textField.setLayoutData(textData);
        textField.setFont(parent.getFont());

        GridData buttonData = new GridData();
        browseButton = new Button(rootComposite, SWT.PUSH);
        browseButton.setText(buttonText);
        browseButton.setLayoutData(buttonData);
        browseButton.setFont(parent.getFont());
        browseButton.pack();

        setEnabled(true);
    }


    /**
     * 指定された文字列を、テキストフィールドに追加する
     * 
     * @param newText
     */
    public void setText(String newText) {
        if (newText.length() > 0) {
            textField.setText(newText);
        }
    }


    /**
     * @return テキストフィールドの文字列。not null
     */
    public String getText() {
        return textField.getText();
    }


    /**
     * テキストフィールドの文字列をクリアする
     */
    public void clearText() {
        textField.setText("");
    }


    /**
     * テキストフィールドにlistenerを追加する
     * 
     * @param listener
     */
    public void addModifyListener(ModifyListener listener) {
        textField.addModifyListener(listener);
    }


    /**
     * テキストフィールドからlistenerを削除する
     * 
     * @param listener
     */
    public void removeModifyListener(ModifyListener listener) {
        textField.removeModifyListener(listener);
    }


    /**
     * テキストフィールドにlistenerを追加する
     * 
     * @param listener
     */
    public void addVerifyListener(VerifyListener listener) {
        textField.addVerifyListener(listener);
    }


    /**
     * テキストフィールドからlistenerを削除する
     * 
     * @param listener
     */
    public void removeVerifyListener(VerifyListener listener) {
        textField.removeVerifyListener(listener);
    }


    /**
     * ブラウズボタンにlistenerを追加する
     * 
     * @param listener
     */
    public void addButtonSelectionListener(SelectionListener listener) {
        browseButton.addSelectionListener(listener);
    }


    /**
     * ブラウズボタンからlistenerを削除する
     * 
     * @param listener
     */
    public void removeButtonSelectionListener(SelectionListener listener) {
        browseButton.removeSelectionListener(listener);
    }


    /**
     * テキストフィールドにフォーカスをあわせる
     */
    public void setFocus() {
        textField.setFocus();
    }

    /**
     * このコントロールが有効かどうかを示す
     */
    private boolean isEnabled;


    /**
     * @return
     */
    public boolean getEnabled() {
        return isEnabled;
    }


    /**
     * @return テキストフィールドが編集できるならtrue
     */
    public boolean isEditable() {
        return textField.getEditable();
    }


    /**
     * テキストフィールドを編集できるようにするか設定する
     */
    public void setEditable(boolean editable) {
        textField.setEditable(editable);
    }


    /**
     * すべてのアイテムの有効/無効を切り替える
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        label.setEnabled(enabled);
        textField.setEnabled(enabled);
        browseButton.setEnabled(enabled);
    }


    /**
     * @return このアイテムのルートコンポジット
     */
    public Composite getComposite() {
        return rootComposite;
    }


    public void dispose() {
        label.dispose();
        textField.dispose();
        browseButton.dispose();
    }


    /* ****************************************
     * Layout
     */
    /**
     * ラベルにlayoutDataを適用する
     * 
     * @param layoutData
     */
    public void setLabelLayout(Object layoutData) {
        label.setLayoutData(layoutData);
    }


    /**
     * テキストフィールドにlayoutDataを適用する
     * 
     * @param layoutData
     */
    public void setTextLayout(Object layoutData) {
        textField.setLayoutData(layoutData);
    }


    /**
     * 参照ボタンにlayoutDataを適用する
     * 
     * @param layoutData
     */
    public void setButtonLayout(Object layoutData) {
        browseButton.setLayoutData(layoutData);
    }


    /* ****************************************
     */
    /**
     * ファイル（フォルダ）パスを受け取り、適切な初期ディレクトリを返す
     * 
     * @param path
     * @return 存在するフォルダならpathそのもの。<br />
     *         存在するファイルならその親フォルダ。<br />
     *         存在しないフォルダならその親をたどり、初めて存在するフォルダ。<br />
     *         存在しないファイルならその親をたどり、初めて存在するフォルダ。<br />
     *         pathがnullならから文字。
     */
    public static String getFilterPath(String path) {
        String ret = "";
        if (path == null || path.equals("")) {
            // ワークスペース
            try {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                if (workspace != null) {
                    return workspace.getRoot().getLocation().toOSString();
                }
            } catch (IllegalStateException e) {
                // ワークスペースがない状態でアクセス(mainからとか）すると発生
            }
        } else {
            File checkFile = new File(path);
            if (checkFile.exists()) {
                if (checkFile.isDirectory()) {
                    return path;
                } else if (checkFile.isFile()) {
                    return checkFile.getParent();
                }
            }
            return getFilterPath(checkFile.getParent());
        }
        return ret;
    }
}