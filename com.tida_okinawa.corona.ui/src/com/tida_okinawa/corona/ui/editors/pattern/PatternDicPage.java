/**
 * @version $Id: PatternDicPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:29:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;

/**
 * @author kousuke-morishima
 */
public class PatternDicPage extends FormPage {
    public static final String PAGE_ID = "PatternDicEditorPage.UniqueIdentifier";
    private IPatternDic ptnDic;


    /**
     * パターン辞書ページ
     * 
     * @param editor
     * @param ptnDic
     */
    public PatternDicPage(FormEditor editor, IPatternDic ptnDic) {
        super(editor, PAGE_ID, "構文パターン");
        this.ptnDic = ptnDic;
    }

    PatternDicEditorMasterBlock master;


    @Override
    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();

        FormEditor editor = getEditor();

        form.setText("構文パターン");
        form.getBody().setLayout(new GridLayout());
        master = new PatternDicEditorMasterBlock();
        master.createContent(managedForm);
        master.registerContextMenu(editor.getSite());
        master.setInput(new PatternRecords(ptnDic));
    }


    @Override
    public boolean isDirty() {
        return ptnDic.isDirty();
    }


    @Override
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);
        master.doSave();
        ptnDic.commit(monitor);
    }


    /**
     * 更新
     */
    public void update() {
        master.update();
    }


    public void doCreate(PatternRecord pattern) {
        /* Memo PasteActionから呼びたかったのでpublicにしてしまった。Actionをこのパッケージに移そうかな */
        master.add(pattern);
    }


    public List<Pattern> doDelete() {
        /* Memo CutActionから呼びたかったのでpublicにしてしまった。Actionをこのパッケージに移そうかな */
        return master.remove();
    }


    public void setGlobalActions() {
        master.setGlobalActions();
    }


    /**
     * @param element
     *            not null
     */
    public void reveal(Object element) {
        master.reveal(element);
    }


    @Override
    public void setFocus() {
        master.setFocus();
    }
}
