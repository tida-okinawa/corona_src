/**
 * @file recent_dic_pri.java
 * @version $Id$
 * 
 * 2013/10/28 11:51:13
 * @author hajime-uchihara
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
 * @author hajime-uchihara
 * 
 */
@Entity
@Table(name = "RECENT_DIC_PRI")
public class RecentDicPriBean implements Serializable {

    /**
     * シリアルID
     */
    private static final long serialVersionUID = -6887999372418323583L;

    // 複合キー
    private RecentDicPriPKBean primaryKey;

    /**
     * 優先度
     */
    private int priority;
    /**
     * INACTIVE
     */
    private boolean inactive;


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public RecentDicPriPKBean getPrimaryKey() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKey(RecentDicPriPKBean primaryKey) {
        this.primaryKey = primaryKey;
    }


    /**
     * @return 優先度
     */
    @Column(name = "PRIORITY")
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
    @Column(name = "INACTIVE")
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
