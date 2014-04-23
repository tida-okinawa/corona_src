/**
 * @version $Id: NewCategoryDictionaryCreationPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/14 18:15:32
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.component.ElementListSelectionDialog1;
import com.tida_okinawa.corona.internal.ui.component.TextWithBrowseButtonGroup;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.service.IIoService;

/**
 * @author kousuke-morishima
 */
public class NewCategoryDictionaryCreationPage extends NewDictionaryCreationPage {

    public NewCategoryDictionaryCreationPage(String title, DicType dicType, IStructuredSelection selection) {
        super(title, dicType, selection);
    }


    @Override
    protected boolean dictionarySettings(IUIDictionary newUIDictionary) {
        if (newUIDictionary != null) {
            TextItem category = null;
            if (selectedCategory == null) {
                /* 入力された分野名があるか確認 */
                String categoryName = domainField.getText().trim();
                IIoService service = IoActivator.getService();
                List<TextItem> categories = service.getCategorys();
                for (TextItem item : categories) {
                    if (item.getText().equals(categoryName)) {
                        category = item;
                        break;
                    }
                }
                if (category == null) {
                    /* ないので作る */
                    category = service.createCategory(categoryName);
                }
            } else {
                category = selectedCategory;
            }
            ((IUserDic) newUIDictionary.getObject()).setDicCategory(category);
        }
        return true;
    }


    /* ****************************************
     * フィールドチェック
     */
    @Override
    protected boolean validatePage() {
        if (super.validatePage()) {
            if (domainField != null) {
                String categoryName = domainField.getText().trim();

                if (categoryName.length() == 0) {
                    setErrorMessage("カテゴリを指定してください");
                    return false;
                }

                /* DB定義が 40 文字まで */
                if (categoryName.length() > 40) {
                    setErrorMessage("分野名は40文字以内で入力してください。");
                    return false;
                }

                if (categoryName.indexOf("'") != -1) {
                    setErrorMessage("シングルクォートは分野名に入力できません。");
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    /* ****************************************
     * UI構築
     */
    TextWithBrowseButtonGroup domainField = null;
    TextItem selectedCategory;


    @Override
    protected void createAdvancedControls(Composite parent) {
        Composite dictTypeGroup = CompositeUtil.defaultComposite(parent, 1);
        ((GridData) dictTypeGroup.getLayoutData()).grabExcessVerticalSpace = false;

        /* 分野名入力フィールドを作成 */
        domainField = new TextWithBrowseButtonGroup(dictTypeGroup, SWT.BORDER | SWT.SINGLE, "分野名", "登録済み分野名...");
        domainField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                setPageComplete(validatePage());
                if ((selectedCategory != null) && !domainField.getText().equals(selectedCategory.getText())) {
                    selectedCategory = null;
                }
            }
        });
        domainField.addButtonSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ElementListSelectionDialog1 dialog = new ElementListSelectionDialog1(getShell(), new LabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((TextItem) element).getText();
                    }
                });
                dialog.setTitle("分野選択");
                dialog.setMessage("作成する辞書の分野を選択してください。");
                dialog.setMultipleSelection(false);
                dialog.setImeMode(SWT.NATIVE);
                dialog.setEmptyListMessage("分野名を選択するか、ダイアログを閉じて、新しい分野名を手入力してください。");
                dialog.setEmptySelectionMessage("分野名を選択するか、ダイアログを閉じて、新しい分野名を手入力してください。");

                IContainer selectedContainer = getSelectedContainer();
                if (selectedContainer != null) {
                    dialog.setElements(IoActivator.getService().getCategorys().toArray());
                }
                if (dialog.open() == Dialog.OK) {
                    selectedCategory = (TextItem) dialog.getFirstResult();
                    domainField.setText(selectedCategory.getText());
                }
            }
        });
    }
}
