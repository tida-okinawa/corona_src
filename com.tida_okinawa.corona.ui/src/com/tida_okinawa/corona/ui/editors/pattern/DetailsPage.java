/**
 * @version $Id: DetailsPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/02 11:47:49
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author kousuke-morishima
 */
public abstract class DetailsPage implements IDetailsPage {

    protected FormEditor editor;
    protected FormToolkit kit;


    /**
     * @param editor
     *            入力の変更を通知する先のエディター
     */
    public DetailsPage(FormEditor editor) {
        this.editor = editor;
    }


    @Override
    public void initialize(IManagedForm form) {
        this.kit = form.getToolkit();
    }


    /* ****************************************
     * 値保存系
     */
    @Override
    public void commit(boolean onSave) {
        if (onSave) {
            doSave();
            setDirty(false);
        } else {
            commit();
        }
    }


    /**
     * 保存するときに呼ばれる
     */
    protected abstract void doSave();


    /**
     * 一時的な値保持を依頼しているときに呼ばれる
     */
    protected abstract void commit();


    protected void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;

            editor.editorDirtyStateChanged();
        }
    }

    private boolean dirty;


    @Override
    public boolean isDirty() {
        return dirty;
    }


    /* ****************************************
     * 何してるのかよくわかってないメソッド類
     */
    @Override
    public void dispose() {
    }


    @Override
    public boolean setFormInput(Object input) {
        return false;
    }


    @Override
    public void setFocus() {
    }


    @Override
    public boolean isStale() {
        return false;
    }


    @Override
    public void refresh() {
    }
}
