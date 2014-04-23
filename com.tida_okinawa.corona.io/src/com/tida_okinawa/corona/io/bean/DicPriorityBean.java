/**
 * @version $Id$
 * 
 * 2013/10/25 15:18:23
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 辞書プライオリティ
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "DIC_PRI")
public class DicPriorityBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1966688875146685804L;

    // 複合キー
    private DicPriorityPKBean primaryKey;


    private int priority; // 優先度
    private boolean inactive; // INACTIVE


    /**
     * コンストラクタ
     */
    public DicPriorityBean() {
    }


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public DicPriorityPKBean getPrimaryKey() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKey(DicPriorityPKBean primaryKey) {
        this.primaryKey = primaryKey;
    }


    /**
     * @return 優先度
     */
    @Column(name = "PRIORITY", nullable = false)
    public int getPriority() {
        return priority;
    }


    /**
     * @param priority
     *            優先度
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }


    /**
     * @return INACTIVE
     */
    @Column(name = "INACTIVE", nullable = false)
    public boolean isInactive() {
        return inactive;
    }


    /**
     * @param inactive
     *            INACTIVE
     */
    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }


}
