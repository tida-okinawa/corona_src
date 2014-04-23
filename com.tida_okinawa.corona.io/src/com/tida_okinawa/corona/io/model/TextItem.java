/**
 * @version $Id: TextItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.dic.abstraction.DicItem;


/**
 * IDと文字列だけの要素を管理する.
 * 
 * @author shingo-takahashi
 */
public class TextItem extends DicItem implements ITextItem {
    /*
     * TODO com.tida_okinawa.corona.io.model.dic.implに移動させたい。
     * TextItemを外部に公開したくない。
     * AbstractUserDic#getDicCategory とか、戻り値にはITextItem を指定する。
     */

    private String _text;


    /**
     * コンストラクタ
     * 
     * @param recordId
     *            レコードID
     * @param data
     *            文字列データ
     */
    public TextItem(int recordId, String data) {
        setId(recordId);
        setText(data);
    }


    @Override
    public void setId(int id) {
        this._id = id;
    }


    @Override
    public int getId() {
        return _id;
    }


    @Override
    public void setText(String text) {
        this._text = (text == null) ? "" : text;
        setDirty(true);
    }


    @Override
    public String getText() {
        return _text;
    }


    @Override
    public String toString() {
        return _text;
    }


    @Override
    public int hashCode() {
        if (_id == UNSAVED_ID) {
            return 47;
        }
        return _id;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TextItem)) {
            return false;
        }

        TextItem t2 = (TextItem) obj;
        if (_id == t2._id) {
            return true;
        }
        return false;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySource.class)) {
            return source;
        }
        return null;
    }

    private IPropertySource source = new IPropertySource() {
        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("id", "ID"), new TextPropertyDescriptor("text", "TEXT"), };
            return descriptor;
        }


        @Override
        public Object getPropertyValue(Object id) {
            if (id.equals("id")) {
                return String.valueOf(getId());
            }
            if (id.equals("text")) {
                return getText();
            }
            return null;
        }


        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }


        @Override
        public void resetPropertyValue(Object id) {
        }


        @Override
        public void setPropertyValue(Object id, Object value) {
        }


        @Override
        public Object getEditableValue() {
            return null;
        }

    };
}
