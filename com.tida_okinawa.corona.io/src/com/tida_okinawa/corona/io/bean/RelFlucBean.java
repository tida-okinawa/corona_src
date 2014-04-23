/**
 * @version $Id$
 * 
 * 2013/10/28 12:03:15
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
 * ゆらぎリレーション
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "rel_fluc")
public class RelFlucBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = -2840514832263006397L;


    private Integer status; // 状態


    // 複合キー
    private RelFlucPKBean primaryKey;


    /**
     * コンストラクタ
     */
    public RelFlucBean() {
    }


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public RelFlucPKBean getPrimaryKey() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKey(RelFlucPKBean primaryKey) {
        this.primaryKey = primaryKey;
    }


    /**
     * @return 状態
     */
    @Column(name = "STATUS", nullable = true)
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
