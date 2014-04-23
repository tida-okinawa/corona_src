/**
 * @version $Id$
 * 
 * 2013/11/06 19:22:28
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * @author kaori-jiroku
 * 
 */
@Entity
public class UsrWorkTableBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6370027537220040731L;


    /**
     * コンストラクタ
     */
    public UsrWorkTableBean() {
    }

    // 複合キー
    private UsrWorkTablePKBean primaryKey;

    private String data; // データ


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public UsrWorkTablePKBean getPrimaryKey() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKey(UsrWorkTablePKBean primaryKey) {
        this.primaryKey = primaryKey;
    }


    /**
     * @return ヒット位置情報
     */
    @Column(name = "DATA", columnDefinition = "text")
    public String getData() {
        return data;
    }


    /**
     * @param data
     *            DATA
     *            ヒット位置情報
     */
    public void setData(String data) {
        this.data = data;
    }

}
