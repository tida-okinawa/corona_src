/**
 * @version $Id: PatternDicEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:28:42
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.FileEditorInput;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.ui.editors.DicEditorDisposer;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;
import com.tida_okinawa.corona.ui.editors.EditorUtil;

/**
 * @author kousuke-morishima
 */
public class PatternDicEditor extends FormEditor {
    public static final String EDITOR_ID = "com.tida_okinawa.corona.ui.editor.patterndic";


    public PatternDicEditor() {
    }

    private PatternDicPage page1 = null;


    @Override
    protected void addPages() {
        page1 = new PatternDicPage(this, (IPatternDic) coronaDic);
        try {
            addPage(page1);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

    private ICoronaDic coronaDic;


    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (input instanceof DicEditorInput) {
        } else if (input instanceof FileEditorInput) {
            input = EditorUtil.convertFrom((FileEditorInput) input);
            if (input == null) {
                throw new PartInitException("パターン辞書ではないため、開けません");
            }
        } else {
            throw new PartInitException("インプットが辞書ではありません");
        }
        DicEditorInput dicInput = (DicEditorInput) input;
        coronaDic = dicInput.getDictionary();
        if (!(coronaDic instanceof IPatternDic)) {
            throw new PartInitException("パターン辞書ではないため、開けません");
        }

        super.init(site, input);

        List<ICoronaDic> dics = new ArrayList<ICoronaDic>(1);
        dics.add(coronaDic);
        partListener = new DicEditorDisposer(this, dics);
        getSite().getPage().addPartListener(partListener);
    }


    @Override
    public void dispose() {
        super.dispose();
        getSite().getPage().removePartListener(partListener);
    }


    /* ****************************************
     * 編集インタフェース
     */
    /**
     * @param id
     *            {@link ActionFactory#DELETE}とか {@link ActionFactory#CUT}
     *            とかそんなんのID(DELETE以外は未実装)
     * @return
     */
    protected boolean performAction(String id) {
        if (ActionFactory.DELETE.getId().equals(id)) {
            ((PatternDicPage) getActivePageInstance()).doDelete();
            return true;
        }
        return false;
    }


    /* ****************************************
     * 保存的な
     */
    @Override
    public boolean isDirty() {
        return page1.isDirty();
    }


    @Override
    public void doSave(IProgressMonitor monitor) {
        page1.doSave(monitor);
        firePropertyChange(PROP_DIRTY);
    }


    @Override
    public void doSaveAs() {
    }


    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /* ****************************************
     * その他
     */
    private IPartListener partListener;


    @Override
    public String getPartName() {
        return getEditorInput().getName();
    }


    @Override
    public String getTitleToolTip() {
        return getEditorInput().getToolTipText();
    }


    @Override
    public void setFocus() {
        IFormPage activePage = getActivePageInstance();
        if (activePage != null) {
            activePage.setFocus();
        }
    }


    /**
     * @param element
     *            not null
     */
    public void reveal(Object element) {
        page1.reveal(element);
    }


    /* ****************************************
     * パターン分類を取得する
     * PatternRecordDetailsPageから使用されている
     */
    /**
     * @return 現在DBに登録されているすべてのパターン分類
     */
    PatternType[] getPatternTypes() {
        PatternType[] types = IoActivator.getService().getPatternTypes();
        Arrays.sort(types, new Comparator<PatternType>() {
            @Override
            public int compare(PatternType o1, PatternType o2) {
                /* idがマイナスのものは、常に後ろに持っていく。そうでなければ名前順 */
                int id1 = o1.getId();
                int id2 = o2.getId();
                if ((id1 >= 0) && (id2 >= 0)) {
                    return o1.getPatternName().compareTo(o2.getPatternName());
                }
                if (id2 < 0) {
                    return -1;
                }
                if (id1 < 0) {
                    return 1;
                }
                return 0;
            }
        });
        return types;
    }


    /**
     * @param name
     *            作成する分類の名前
     * @return 作成した分類。すでに存在すればnull
     */
    PatternType createPatternType(String name) {
        for (PatternType type : getPatternTypes()) {
            if (type.getPatternName().equals(name)) {
                return null;
            }
        }
        PatternType type = IoActivator.getService().addPatternType(name);
        return type;
    }


}
