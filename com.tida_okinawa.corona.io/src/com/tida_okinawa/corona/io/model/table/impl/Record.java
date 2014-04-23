/**
 * @version $Id: Record.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.table.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.abstraction.CoronaObject;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IRecord;


/**
 * @author shingo-takahashi
 * 
 */
public class Record extends CoronaObject implements IRecord {

    private int recordId;

    private List<IField> fields = null;


    /**
     * @param recordId
     */
    public Record(int recordId) {
        super();
        this.recordId = recordId;
    }


    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }


    @Override
    public int getRecordId() {
        return recordId;
    }


    public void setFields(List<IField> fields) {
        this.fields = fields;
    }


    @Override
    public List<IField> getFields() {
        return fields;
    }


    @Override
    public IField getField(int fieldId) {

        for (IField fld : fields) {
            if (fld.getId() == fieldId) {
                return fld;
            }
        }
        return null;
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
            List<IPropertyDescriptor> descriptor = new ArrayList<IPropertyDescriptor>();
            for (IField fld : fields) {
                descriptor.add(new TextPropertyDescriptor(fld.getHeader().getName(), fld.getHeader().getDispName()));
            }
            return descriptor.toArray(new IPropertyDescriptor[descriptor.size()]);
        }


        @Override
        public Object getPropertyValue(Object id) {
            for (IField fld : fields) {
                if (fld.getHeader().getName().equals(id)) {
                    return fld.getValue();
                }
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
