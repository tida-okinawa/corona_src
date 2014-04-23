/**
 * @version $Id: ClaimDataRecordList.java 33 2012-07-31 06:55:35Z kousuke-morishima $
 * 
 * 2011/10/28 11:43:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.dam.hibernate.connector.DaoConnecter;
import com.tida_okinawa.corona.io.dam.hibernate.connector.impl.ClaimDataDaoConnector;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimDataRecordList;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IRecord;

/**
 * {@link IClaimData#getRecords()} の実装
 * 
 * クレームのテキスト部分は保持せずに、必要なときにDBから読む
 * 
 * @author imai
 * 
 */
public class ClaimDataRecordList implements IClaimDataRecordList {

    /**
     * DB処理
     */
    final DaoConnecter<Integer, List<IField>> connector;

    Map<Integer, IRecord> records;


    /**
     * クレームデータレコードリスト
     * 
     * @param connector
     */

    @SuppressWarnings({ "javadoc" })
    public ClaimDataRecordList(ClaimDataDaoConnector connector) {
        this.connector = connector;
    }


    void init() {
        Set<Integer> keys = connector.getKeys();
        Integer[] recordIds = keys.toArray(new Integer[keys.size()]);
        records = new TreeMap<Integer, IRecord>();
        for (int recordId : recordIds) {
            records.put(recordId, new Record(recordId));
        }
    }

    class Record implements IRecord {
        int recordId;


        Record(int recordId) {
            this.recordId = recordId;
        }


        @Override
        public int getRecordId() {
            return recordId;
        }


        @Override
        public List<IField> getFields() {
            List<IField> list = connector.get(recordId);
            if (list != null) {
                return list;
            }
            return new ArrayList<IField>(0);
        }


        @Override
        public IField getField(int fieldId) {
            List<IField> fields = getFields();
            if (fieldId - 1 < fields.size()) {
                return fields.get(fieldId - 1);
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
                for (IField fld : getFields()) {
                    descriptor.add(new TextPropertyDescriptor(fld.getId(), fld.getHeader().getDispName()));
                }
                IPropertyDescriptor[] out = descriptor.toArray(new IPropertyDescriptor[descriptor.size()]);
                return out;
            }


            @Override
            public Object getPropertyValue(Object id) {
                for (IField fld : getFields()) {
                    if (((Integer) fld.getId()).equals(id)) {
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


    @Deprecated
    @Override
    public boolean add(IRecord e) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public void add(int index, IRecord element) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public boolean addAll(Collection<? extends IRecord> c) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public boolean addAll(int index, Collection<? extends IRecord> c) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            if (records == null) {
                init();
            }
            return records.keySet().contains(o);
        }
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (contains(e)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public IRecord get(int index) {
        if (records == null) {
            init();
        }
        return records.get(index);
    }


    @Deprecated
    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isEmpty() {
        if (records == null) {
            init();
        }
        return records.isEmpty();
    }


    @Override
    public Iterator<IRecord> iterator() {
        if (records == null) {
            init();
        }
        return new RecordsIterator();
    }

    class RecordsIterator implements Iterator<IRecord> {
        Iterator<Integer> keyIterator = records.keySet().iterator();


        @Override
        public boolean hasNext() {
            return keyIterator.hasNext();
        }


        @Override
        public IRecord next() {
            Integer recordId = keyIterator.next();
            return records.get(recordId);
        }


        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }


    @Deprecated
    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public ListIterator<IRecord> listIterator() {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public ListIterator<IRecord> listIterator(int index) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public IRecord remove(int index) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public IRecord set(int index, IRecord element) {
        throw new UnsupportedOperationException();
        // add() を使う
    }


    @Override
    public int size() {
        if (records == null) {
            init();
        }
        return records.size();
    }


    @Deprecated
    @Override
    public List<IRecord> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object[] toArray() {
        if (records == null) {
            init();
        }
        return records.values().toArray();
    }


    @Deprecated
    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }


    @Override
    public IRecord getRecord(int recordId) {
        if (records == null) {
            init();
        }
        return records.get(recordId);
    }
}
