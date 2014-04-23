/**
 * @version $Id: AbstractClaimData.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.abstraction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.io.PropertyUtil;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.io.model.table.ITextRecord;
import com.tida_okinawa.corona.io.model.table.impl.TextRecord;


public abstract class AbstractClaimData extends CoronaObject implements IClaimData, Comparable<IClaimData> {

    protected int _id;

    protected String _name;

    protected String _dbName;

    protected Set<Integer> _miningFields;

    protected int _productField = -1;

    protected int _recordIdField = -1;

    protected int _baseId;

    protected List<IFieldHeader> _headers = new ArrayList<IFieldHeader>();

    protected List<IRecord> _records = new ArrayList<IRecord>();

    protected List<IClaimWorkData> _correctionMistakes = new ArrayList<IClaimWorkData>();

    private Date lasted = null;


    /**
     * @param name
     * @param dbName
     * @param id
     */
    public AbstractClaimData(String name, String dbName, int id) {
        this._name = name;
        this._dbName = dbName;
        this._id = id;
        this._miningFields = new HashSet<Integer>();
    }


    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (!(obj instanceof IClaimData)) {
            return false;
        }

        IClaimData item = (IClaimData) obj;
        if (this.getId() == item.getId()) {
            return true;
        }
        return false;
    }


    @Override
    public int compareTo(IClaimData o) {
        if (o == null)
            return -1;
        return o.getId() - getId();
    }


    @Override
    public String toString() {
        return "[" + this.getId() + ":" + this.getFileName() + "(" + this.getTableName() + ")]";
    }


    @Override
    public void setProductField(int fieldNo) {
        this._productField = fieldNo;
    }


    @Override
    public int getProductField() {
        return _productField;
    }


    @Override
    public void removeProductField() {
        this._productField = -1;
    }


    @Override
    public List<String> getProducts() {
        return getProductsDam();
    }


    @Override
    public void addCorrectionMistakesField(int fieldNo) {
        if (!_miningFields.contains(fieldNo)) {
            _miningFields.add(fieldNo);
        }
    }


    @Override
    public void removeCorrectionMistakesField(int fieldNo) {
        if (_miningFields.contains(fieldNo)) {
            _miningFields.remove(fieldNo);
        }
    }


    @Override
    public Set<Integer> getCorrectionMistakesFields() {
        return _miningFields;
    }


    @Override
    public IClaimWorkData getCorrectionMistakes(int fieldNo) {
        return getCorrectionMistakesDam(fieldNo);
    }


    @Override
    public IFieldHeader getFieldInformation(int fieldId) {
        for (IFieldHeader field : _headers) {
            if (field.getId() == fieldId) {
                return field;
            }
        }
        return null;
    }


    @Override
    public List<IFieldHeader> getFieldInformations() {
        return _headers;
    }


    @Override
    public List<IRecord> getRecords() {
        return _records;
    }


    @Override
    public IRecord getRecord(int recordId) {
        for (IRecord rec : _records) {
            if (rec.getRecordId() == recordId) {
                return rec;
            }
        }
        return null;
    }


    @Override
    public List<ITextRecord> getTextRecords(int fieldNo) {
        List<ITextRecord> list = new ArrayList<ITextRecord>();

        for (IRecord rec : _records) {
            String strDispId;
            if (getDispIdField() > 0) {
                strDispId = (String) rec.getField(getDispIdField()).getValue();
            } else {
                strDispId = String.valueOf(rec.getRecordId());
            }
            IField fld = rec.getField(fieldNo);
            list.add(new TextRecord(rec.getRecordId(), strDispId, (String) fld.getValue()));
        }
        return list;
    }


    @Override
    public ITextRecord getTextRecord(int fieldNo, int recordId) {
        for (IRecord rec : _records) {
            if (recordId == rec.getRecordId()) {
                String strDispId;
                if (getDispIdField() > 0) {
                    strDispId = (String) rec.getField(getDispIdField()).getValue();
                } else {
                    strDispId = String.valueOf(rec.getRecordId());
                }
                IField fld = rec.getField(fieldNo);
                return new TextRecord(fld.getId(), strDispId, (String) fld.getValue());
            }
        }
        return null;
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
    public void setFileName(String fileName) {
        this._name = fileName;
    }


    @Override
    public String getFileName() {
        return _name;
    }


    @Override
    public void setTableName(String tableName) {
        this._dbName = tableName;
    }


    @Override
    public String getTableName() {
        return _dbName;
    }


    @Override
    public void setDispIdField(int fieldId) {
        _recordIdField = fieldId;
    }


    @Override
    public int getDispIdField() {
        return _recordIdField;
    }


    @Override
    public Date getLasted() {
        return (Date) lasted.clone();
    }


    /**
     * 更新日時をセット
     * 
     * @param lasted
     */
    public void setLasted(Date lasted) {
        if (lasted != null) {
            this.lasted = (Date) lasted.clone();
        } else {
            this.lasted = null;
        }
    }


    abstract protected IClaimWorkData getCorrectionMistakesDam(int fieldNo);


    abstract protected List<String> getProductsDam();


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        PropertyUtil prop = new PropertyUtil();
        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { prop.getDescriptor(PropertyItem.PROP_IMPORTDATE),
                prop.getDescriptor(PropertyItem.PROP_NAME), prop.getDescriptor(PropertyItem.PROP_COLUMN_NAME), prop.getDescriptor(PropertyItem.PROP_RECORDS), };
        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {
        if (PropertyItem.PROP_IMPORTDATE.getKey().equals(id)) {
            Date date = getLasted();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            return sdf.format(date);
        } else if (PropertyItem.PROP_NAME.getKey().equals(id)) {
            return getName();
        } else if (PropertyItem.PROP_COLUMN_NAME.getKey().equals(id)) {
            StringBuilder fieldNameConcatBuffer = new StringBuilder();
            String fieldName = "";
            for (IFieldHeader header : getFieldInformations()) {
                fieldNameConcatBuffer.append(header.getName());
                fieldNameConcatBuffer.append(",");
            }
            fieldName = fieldNameConcatBuffer.toString();
            if (!("".equals(fieldName))) {
                fieldName = fieldName.substring(0, fieldName.lastIndexOf(","));
            }
            return fieldName;
        } else if (PropertyItem.PROP_RECORDS.getKey().equals(id)) {
            return Integer.toString(this.getRecords().size());
        }
        return PropertyUtil.DEFAULT_VALUE;
    }


    @Override
    public String getName() {
        return getTableName().substring("USR_CLAIM_".length());
    }

}
