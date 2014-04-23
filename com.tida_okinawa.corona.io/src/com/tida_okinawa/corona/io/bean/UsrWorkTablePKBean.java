/**
 * @version $Id$
 * 
 * 2013/11/06 19:33:48
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
public class UsrWorkTablePKBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4508034485329188533L;


    /**
     * コンストラクタ
     */
    public UsrWorkTablePKBean() {
    }


    /**
     * コンストラクタ
     * 
     * @param workId
     *            作業ID
     * @param fieldId
     *            フィールドID
     * @param recId
     *            レコードID
     * @param historyId
     *            履歴ID
     */
    public UsrWorkTablePKBean(int workId, int fieldId, int recId, int historyId) {
        this.workId = workId;
        this.fieldId = fieldId;
        this.recId = recId;
        this.historyId = historyId;
    }


    private int workId; // 作業ID
    private int fieldId; // フィールドID
    private int recId; // レコードID
    private int historyId; // 履歴ID


    /**
     * @return 作業ID
     */
    @Column(name = "WORK_ID", nullable = false)
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
    @Column(name = "FLD_ID", nullable = false)
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
     * @return 履歴ID
     */
    @Column(name = "HISTORY_ID", nullable = false)
    public int getHistoryId() {
        return historyId;
    }


    /**
     * @param historyId
     *            履歴
     */
    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }


    /**
     * @return レコードID
     */
    @Column(name = "REC_ID", nullable = false)
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
        result = prime * result + fieldId;
        result = prime * result + historyId;
        result = prime * result + recId;
        result = prime * result + workId;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof UsrWorkTablePKBean)) {
            return false;
        }

        UsrWorkTablePKBean usrWorkTablePK = (UsrWorkTablePKBean) obj;

        if (fieldId != usrWorkTablePK.getFieldId() || historyId != usrWorkTablePK.getHistoryId() || recId != usrWorkTablePK.getRecId()
                || workId != usrWorkTablePK.getWorkId()) {
            return false;
        }
        return true;
    }

}
