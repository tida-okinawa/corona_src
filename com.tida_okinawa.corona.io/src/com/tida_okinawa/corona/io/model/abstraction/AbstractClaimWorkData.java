/**
 * @version $Id: AbstractClaimWorkData.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/16 16:31:25
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.abstraction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tida_okinawa.corona.io.dam.hibernate.ClaimWorkDataRecordList;
import com.tida_okinawa.corona.io.exception.CoronaError;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.CorrectionMistakesType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.table.ITextRecord;


/**
 * @author shingo-takahashi
 */
public abstract class AbstractClaimWorkData extends CoronaObject implements IClaimWorkData, Comparable<IClaimWorkData> {

    protected int _claimId;

    protected int _fieldId;

    protected int _projectId;

    protected int _productId;

    protected Date lasted;

    // TODO 20131121 rev1592で変更されていた箇所を戻す。
    protected ClaimWorkDataRecordList _records;

    protected boolean internal;

    protected boolean external;

    protected ClaimWorkDataType _workDataType = ClaimWorkDataType.NONE;

    protected CorrectionMistakesType _correctionMistakesType = CorrectionMistakesType.NONE;

    protected List<CoronaError> errors = new ArrayList<CoronaError>();

    private String note = "";


    /**
     * @param claimId
     *            問い合わせデータID
     * @param fieldId
     *            フィールドID
     * @param workDataType
     *            種別
     * @param projectId
     *            誤記補正の場合はプロダクト単位ではないので、0を設定する。
     * @param productId
     *            ターゲットID
     */
    public AbstractClaimWorkData(int claimId, int fieldId, ClaimWorkDataType workDataType, int projectId, int productId) {
        super();
        this._workDataType = workDataType;
        this._claimId = claimId;
        this._fieldId = fieldId;
        this._projectId = projectId;
        this._productId = productId;
    }


    @Override
    public ClaimWorkDataType getClaimWorkDataType() {
        return _workDataType;
    }


    @Override
    public boolean isExternalCorrectionMistakes() {
        return external;
    }


    @Override
    public boolean isInternalCorrectionMistakes() {
        return internal;
    }


    @Override
    public String getClaimWorkData(int recordId) {
        ITextRecord rec = _records.getRecord(recordId);
        if (rec == null) {
            return null;
        }
        return rec.getText();
    }


    @Override
    public boolean addClaimWorkData(int recordId, String data) {
        // TODO:表示用IDを取得
        // String dispId = getDispIdDam(this.getClaimId(), recordId);
        _records.add(recordId, data);
        return true;
    }


    @Override
    public int getFieldId() {
        return _fieldId;
    }


    /**
     * @return _claimId
     */
    @Override
    public int getClaimId() {
        return _claimId;
    }


    @Override
    public List<ITextRecord> getClaimWorkDatas() {
        return _records;
    }


    @Override
    public Date getLasted() {
        if (lasted != null) {
            return (Date) lasted.clone();
        }
        return null;
    }


    /**
     * @param date
     */
    protected void setLasted(Date date) {
        if (date != null) {
            lasted = (Date) date.clone();
        } else {
            lasted = null;
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (!(obj instanceof IClaimWorkData)) {
            return false;
        }

        IClaimWorkData item = (IClaimWorkData) obj;
        if (this.getClaimId() == item.getClaimId() && this.getFieldId() == item.getFieldId() && this.getClaimWorkDataType() == item.getClaimWorkDataType()) {
            return true;
        }
        return false;
    }


    @Override
    public int compareTo(IClaimWorkData o) {
        if (o == null)
            return -1;
        if (this.getClaimId() != o.getClaimId()) {
            return this.getClaimId() - o.getClaimId();
        }
        if (this.getFieldId() != o.getFieldId()) {
            return this.getFieldId() - o.getFieldId();
        }
        return this.getClaimWorkDataType().compareTo(o.getClaimWorkDataType());
    }


    @Override
    public String toString() {
        return "[" + this.getClaimId() + "-" + this.getFieldId() + "-" + this.getClaimWorkDataType().getName() + "(" + this._projectId + "-" + this._productId
                + "):" + this.getLasted() + "]";
    }


    /**
     * @return _projectId
     */
    @Override
    public int getProjectId() {
        return _projectId;
    }


    /**
     * @param _projectId
     *            セットする _projectId
     */
    public void setProjectId(int _projectId) {
        this._projectId = _projectId;
    }


    /**
     * @return _productId
     */
    @Override
    public int getProductId() {
        return _productId;
    }


    /**
     * @param _productId
     *            セットする _productId
     */
    public void setProductId(int _productId) {
        this._productId = _productId;
    }


    @Override
    public List<CoronaError> getErrors() {
        return errors;
    }


    /**
     * @param claimId
     *            問い合わせデータID
     * @param recId
     *            レコードID
     * @return may be null if system error occurred
     */
    abstract protected String getDispIdDam(int claimId, int recId);


    @Override
    public void setNote(String str) {
        note = str;
    }


    @Override
    public String getNote() {
        return note;
    }
}
