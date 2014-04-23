/**
 * @version $Id$
 * 
 * 2013/10/25 15:09:38
 * @author wataru-higa
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
 * 同義語リレーション
 * 
 * @author wataru-higa
 * 
 */
@Entity
@Table(name = "rel_synonym")
public class RelSynonymBean implements Serializable {

    /**
     * シリアルID
     */
    private static final long serialVersionUID = -6811241083377665496L;

    // 複合キー
    private RelSynonymPKBean primaryKey;

    /**
     * STATUS
     */
    private Integer status;


    /**
     * コンストラクタ
     */
    public RelSynonymBean() {
    }


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public RelSynonymPKBean getPrimaryKey() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKey(RelSynonymPKBean primaryKey) {
        this.primaryKey = primaryKey;
    }


    /**
     * @return 状態
     */
    @Column(name = "STATUS")
    public Integer getStatus() {
        return status;
    }


    /**
     * @param status
     *            状態
     */
    public void setStatus(Integer status) {
        this.status = status;
    }


}
