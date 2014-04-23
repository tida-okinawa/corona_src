/**
 * @version $Id: DicEditorInput.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/31 11:57:19
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertySource;

import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * @author kousuke-morishima
 */
public class DicEditorInput implements IEditorInput {

    private IUIDictionary uiDic;
    private ICoronaDic dic;


    public DicEditorInput(IUIDictionary dic) {
        this.uiDic = dic;
        this.dic = uiDic.getObject();
    }


    public DicEditorInput(ICoronaDic dic) {
        this.dic = dic;
    }


    /**
     * @return may be null
     */
    public IUIDictionary getUIDictionary() {
        return uiDic;
    }


    /**
     * @return not null
     */
    public ICoronaDic getDictionary() {
        return dic;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        // ここでIResourceにAdapterできるようになってると、右クリックメニューにSVNのメニューが出てきたりする
        // if (IResource.class.equals(adapter)) {
        // return CoronaModel.INSTANCE.adapter(dic);
        // }
        if (adapter.equals(IPropertySource.class)) {
            return uiDic.getEditableValue();
        }
        return null;
    }


    @Override
    public boolean exists() {
        return false;
    }


    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }


    @Override
    public String getName() {
        if (uiDic != null) {
            return uiDic.toString();
        }
        return dic.getName();
    }


    @Override
    public IPersistableElement getPersistable() {
        // Memo DicEditorInputの状態の保存に使うオブジェクトを返す。状態を保存？なにするんだ？
        return null;
    }


    @Override
    public String getToolTipText() {
        if (uiDic != null) {
            IResource res = uiDic.getResource();
            return res.getProject().getName() + "/" + res.getProjectRelativePath().toString();
        }
        return dic.getName();
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DicEditorInput)) {
            if (obj instanceof FileEditorInput) {
                return equals(EditorUtil.convertFrom((FileEditorInput) obj));
            }
            return false;
        }

        DicEditorInput i2 = (DicEditorInput) obj;
        if (uiDic == null) {
            if (i2.uiDic == null) {
                return dic.equals(i2.dic);
            }
            return false;
        }
        if (i2.uiDic == null) {
            return false;
        }
        return dic.equals(i2.dic);
    }


    @Override
    public int hashCode() {
        if (uiDic != null) {
            return uiDic.hashCode();
        }
        return dic.hashCode();
    }


    @Override
    public String toString() {
        if (uiDic != null) {
            return uiDic.toString();
        }
        return dic.toString();
    }
}
