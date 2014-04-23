/**
 * @version $Id$
 * 
 * 2013/10/29 20:58:46
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 優先度管理(複合キー)
 * 
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class RecentDicPriPKBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1567167358892370714L;

    private int id; // ID
    private int fieldId; // フィールドID
    private int dicId; // 辞書ID


    /**
     * コンストラクタ
     */
    public RecentDicPriPKBean() {
    }


    /**
     * コンストラクタ
     * 
     * @param id
     *            ID
     * @param fieldId
     *            フィールドID
     * @param dicId
     *            辞書ID
     */
    public RecentDicPriPKBean(int id, int fieldId, int dicId) {
        this.id = id;
        this.fieldId = fieldId;
        this.dicId = dicId;
    }


    /**
     * @return ID
     */
    @Column(name = "ID")
    public int getId() {
        return id;
    }


    /**
     * @param id
     *            ID
     */
    public void setId(int id) {
        this.id = id;
    }


    /**
     * @return fldId
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
     * @return 辞書ID
     */
    @Column(name = "DIC_ID")
    public int getDicId() {
        return dicId;
    }


    /**
     * @param dicId
     *            辞書ID
     */
    public void setDicId(int dicId) {
        this.dicId = dicId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + fieldId;
        result = prime * result + dicId;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof RecentDicPriPKBean)) {
            return false;
        }

        RecentDicPriPKBean recentDicPriPK = (RecentDicPriPKBean) obj;
        if (id != recentDicPriPK.getId() || fieldId != recentDicPriPK.getFieldId() || dicId != recentDicPriPK.getDicId()) {
            return false;
        }
        return true;
    }

}
