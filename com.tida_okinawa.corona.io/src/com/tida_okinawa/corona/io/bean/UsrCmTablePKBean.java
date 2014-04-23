/**
 * @version $Id$
 * 
 * 2013/11/11 20:56:55
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class UsrCmTablePKBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5859607237488995323L;

    private int workId; // 作業ID
    private int fieldId; // フィールドID
    private int recId; // レコードID


    /**
     * コンストラクタ
     */
    public UsrCmTablePKBean() {
    }


    /**
     * コンストラクタ
     * 
     * @param workId
     *            作業ID
     * @param fieldId
     *            フィールドID
     * @param recId
     *            // レコードID
     */
    public UsrCmTablePKBean(int workId, int fieldId, int recId) {
        this.workId = workId;
        this.fieldId = fieldId;
        this.recId = recId;
    }


    /**
     * @return 作業ID
     */
    @Column(name = "WORK_ID")
    public int getWorkId() {
        return workId;
    }


    /**
     * @param workId
     *            作業ID
     */
    public void setWorkId(int workId) {
        this.workId = workId;
    }


    /**
     * @return フィールドID
     */
    @Column(name = "FLD_ID")
    public int getFieldId() {
        return fieldId;
    }


    /**
     * @param fieldId
     *            フィールドID
     */
    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }


    /**
     * @return レコードID
     */
    @Column(name = "REC_ID")
    public int getRecId() {
        return recId;
    }


    /**
     * @param recId
     *            レコードID
     */
    public void setRecId(int recId) {
        this.recId = recId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + workId;
        result = prime * result + fieldId;
        result = prime * result + recId;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof UsrCmTablePKBean)) {
            return false;
        }

        UsrCmTablePKBean usrCmTablePK = (UsrCmTablePKBean) obj;
        if (workId != usrCmTablePK.getWorkId() || fieldId != usrCmTablePK.getFieldId() || recId != usrCmTablePK.getRecId()) {
            return false;
        }
        return true;
    }

}
