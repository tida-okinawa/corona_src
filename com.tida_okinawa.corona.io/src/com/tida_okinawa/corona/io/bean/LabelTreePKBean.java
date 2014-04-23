/**
 * @version $Id$
 * 
 * 2013/10/29 20:07:37
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Index;

/**
 * ラベル親子関係(複合キー)
 * 
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class LabelTreePKBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8523822991782618609L;

    private int parentId; // 親ラベルID
    private int childId; // 子ラベルID


    /**
     * コンストラクタ
     */
    public LabelTreePKBean() {
    }


    /**
     * コンストラクタ
     * 
     * @param parentId
     *            親ラベルID
     * @param childId
     *            子ラベルID
     */
    public LabelTreePKBean(int parentId, int childId) {
        this.parentId = parentId;
        this.childId = childId;
    }


    /**
     * @return 親ラベルID
     */
    @Column(name = "PARENT_ID", nullable = false)
    public int getParentId() {
        return parentId;
    }


    /**
     * @param parentId
     *            親ラベルID
     */
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }


    /**
     * @return 子ラベルID
     */
    @Column(name = "CHILD_ID", nullable = false)
    @Index(name = "CHILD_ID", columnNames = { "CHILD_ID" })
    public int getChildId() {
        return childId;
    }


    /**
     * @param childId
     *            子ラベルID
     */
    public void setChildId(int childId) {
        this.childId = childId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + parentId;
        result = prime * result + childId;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof LabelTreePKBean)) {
            return false;
        }

        LabelTreePKBean labelTreePK = (LabelTreePKBean) obj;
        if (parentId != labelTreePK.getParentId() || childId != labelTreePK.getChildId()) {
            return false;
        }
        return true;
    }


}
