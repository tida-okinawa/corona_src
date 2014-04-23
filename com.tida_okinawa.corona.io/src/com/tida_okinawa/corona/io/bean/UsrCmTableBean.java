/**
 * @version $Id$
 * 
 * 2013/11/05 21:18:44
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
public class UsrCmTableBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6365839816105931504L;


    /**
     * コンストラクタ
     */
    public UsrCmTableBean() {
    }

    // 複合キー
    private UsrWorkTablePKBean primaryKey;

    private String data; // 誤記補正対象データ


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
     * @return 誤記補正対象データ
     */
    @Column(name = "DATA", columnDefinition = "text")
    public String getData() {
        return data;
    }


    /**
     * @param data
     *            誤記補正対象データ
     */
    public void setData(String data) {
        this.data = data;
    }


}
