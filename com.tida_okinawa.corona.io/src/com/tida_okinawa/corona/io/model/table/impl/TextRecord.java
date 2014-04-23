/**
 * @version $Id: TextRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/26 17:01:15
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.table.impl;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.table.ITextRecord;

/**
 * @author shingo-takahashi
 */
public class TextRecord extends TextItem implements ITextRecord {

    protected String dispId;


    /**
     * @param recordId
     * @param data
     */
    public TextRecord(int recordId, String dispId, String data) {
        super(recordId, data);
        this.dispId = dispId;
    }


    @Override
    public String getDispId() {
        return dispId;
    }


    @Override
    public void setDispId(String id) {
        this.dispId = id;
        setDirty(true);
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
            IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("id", "ID"), new TextPropertyDescriptor("text", "テキスト") };
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
