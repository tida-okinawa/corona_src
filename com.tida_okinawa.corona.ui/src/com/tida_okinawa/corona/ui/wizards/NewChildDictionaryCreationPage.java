/**
 * @version $Id: NewChildDictionaryCreationPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/14 15:30:20
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibFolder;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.uicomponent.SelectList;

/**
 * @author kousuke-morishima
 */
public abstract class NewChildDictionaryCreationPage extends NewDictionaryCreationPage {

    public NewChildDictionaryCreationPage(String title, DicType dicType, IStructuredSelection selection) {
        super(title, dicType, selection);
    }

    /* ****************************************
     * 参照辞書の切り替え
     */
    private IContainer oldSelectedLibFolder;


    @Override
    public void handleEvent(Event event) {
        super.handleEvent(event);

        /* 選択されたプロジェクトの辞書一覧を取得する */
        IContainer selectedContainer = getSelectedContainer(); /* 現在選択されているフォルダを取得 */
        if (selectedContainer != null) {
            /* 異なるLibFolderを選択した時だけ、コンボのアイテムを変更する */
            if ((oldSelectedLibFolder == null) || !oldSelectedLibFolder.equals(selectedContainer)) {
                IUIElement folderElement = CoronaModel.INSTANCE.adapter(selectedContainer, false);
                refDictionaries.clear();
                if (folderElement instanceof IUILibFolder) {
                    IUILibFolder library = (IUILibFolder) folderElement;
                    /*
                     * コンボにセットする辞書名を取得
                     * 選択しているフォルダの辞書のみを表示
                     */
                    IUIElement[] children = library.getChildren();
                    for (IUIElement child : children) {
                        if (child instanceof IUIDictionary) {
                            Class<? extends ICoronaDic> cls = ((IUIDictionary) child).getObject().getClass();
                            for (Class<?> dicClass : refDics) {
                                if (dicClass.isAssignableFrom(cls)) {
                                    refDictionaries.add(((IUIDictionary) child));
                                    break;
                                }
                            }
                        }
                    }
                }
                setDictionaryComboItems(refDictionaries);
                oldSelectedLibFolder = selectedContainer;
            }
        } else {
            oldSelectedLibFolder = null;
            // 存在しないフォルダのときは、辞書一覧は空にしない。
        }
    }

    private List<IUIDictionary> comboItems = new ArrayList<IUIDictionary>();


    private void setDictionaryComboItems(List<IUIDictionary> items) {
        if (list == null) {
            comboItems = items;
        } else {
            list.setSourceInput(new ArrayList<Object>(items));
        }
        validatePage();
    }

    private Class<?>[] refDics = new Class<?>[0];


    /**
     * @param refDics
     *            must be not null<br />
     *            参照辞書に表示する辞書種別
     */
    public void setReferenceDictionary(Class<?>[] refDics) {
        this.refDics = refDics;
    }


    /* ****************************************
     * フィールドチェック
     */
    @Override
    protected boolean validatePage() {
        if (!super.validatePage()) {
            return false;
        }
        if (list != null) {
            Object[] selected = list.getSelected();
            if (selected.length == 0) {
                setErrorMessage("参照元辞書を選択してください");
                return false;
            }
        }
        setErrorMessage(null);
        return true;
    }

    /* ****************************************
     * UIの拡張領域構築
     */
    private SelectList list;
    private List<IUIDictionary> refDictionaries = new ArrayList<IUIDictionary>();


    @Override
    protected void createAdvancedControls(Composite parent) {
        Composite dictTypeGroup = CompositeUtil.defaultComposite(parent, 1);
        ((GridData) dictTypeGroup.getLayoutData()).grabExcessVerticalSpace = false;

        /* 参照元選択フィールドを作成 */
        Composite refDictGroup = CompositeUtil.defaultComposite(dictTypeGroup, 2);
        refDictGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        CompositeUtil.createLabel(refDictGroup, "参照元辞書", 70);

        list = new SelectList(dictTypeGroup, 250, 120);
        list.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof IUIDictionary) {
                    return ((IUIDictionary) element).getObject().getName();
                }
                return super.getText(element);
            }
        });
        list.setSourceInput(new ArrayList<Object>(comboItems));
        list.addListChangedListener(new Listener() {
            @Override
            public void handleEvent(Event event) {
                setPageComplete(validatePage());
            }
        });
    }


    /**
     * @return 参照元辞書に指定された辞書を取得する
     */
    public List<IUIDictionary> getReferenceDictionary() {
        List<IUIDictionary> ret = new ArrayList<IUIDictionary>();
        if (list != null) {
            for (Object o : list.getSelected()) {
                ret.add((IUIDictionary) o);
            }
        }
        return ret;
    }
}
