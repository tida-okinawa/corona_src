/**
 * @version $Id$
 * 
 * 2013/10/29 17:02:26
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 辞書プライオリティ(複合キー)
 * 
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class DicPriorityPKBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4339572886880096904L;

    private int id; // ID
    private int fieldId; // フィールドID
    private int dicId; // 辞書ID


    /**
     * コンストラクタ
     */
    public DicPriorityPKBean() {
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
    public DicPriorityPKBean(int id, int fieldId, int dicId) {
        this.id = id;
        this.fieldId = fieldId;
        this.dicId = dicId;
    }


    /**
     * @return ID
     */
    @Column(name = "ID", nullable = true)
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
     * @return フィールドID
     */
    @Column(name = "FLD_ID", nullable = true)
    public int getFieldId() {
        return fieldId;
    }


    /**
     * @param fldId
     *            フィールドID
     */
    public void setFieldId(int fldId) {
        this.fieldId = fldId;
    }


    /**
     * @return 辞書ID
     */
    @Column(name = "DIC_ID", nullable = true)
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
        if (obj == null || !(obj instanceof DicPriorityPKBean)) {
            return false;
        }

        DicPriorityPKBean dicPriorityPk = (DicPriorityPKBean) obj;
        if (id != dicPriorityPk.getId() || fieldId != dicPriorityPk.getFieldId() || dicId != dicPriorityPk.dicId) {
            return false;
        }
        return true;
    }


}
