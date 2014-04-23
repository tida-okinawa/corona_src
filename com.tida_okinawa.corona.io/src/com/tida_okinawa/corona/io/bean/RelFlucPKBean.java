/**
 * @version $Id$
 * 
 * 2013/10/28 15:49:17
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
 * ゆらぎリレーション(複合キー)
 * 
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class RelFlucPKBean implements Serializable {


    /**
     * シリアライズID
     */
    private static final long serialVersionUID = 9093480680366161714L;

    private int id; // ID
    private int flucId; // ゆらぎID


    /**
     * コンストラクタ
     */
    public RelFlucPKBean() {

    }


    /**
     * コンストラクタ
     * 
     * @param id
     *            ID
     * @param flucId
     *            ゆらぎID
     */
    public RelFlucPKBean(int id, int flucId) {
        this.id = id;
        this.flucId = flucId;
    }


    /**
     * @return ID
     */
    @Column(name = "ID", nullable = false)
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
     * @return ゆらぎID
     */
    @Column(name = "FLUC_ID", nullable = false)
    @Index(name = "FLUC_ID", columnNames = { "FLUC_ID" })
    public int getFlucId() {
        return flucId;
    }


    /**
     * @param flucId
     *            ゆらぎID
     */
    public void setFlucId(int flucId) {
        this.flucId = flucId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + flucId;
        result = prime * result + id;
        return result;
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof RelFlucPKBean)) {
            return false;
        }

        RelFlucPKBean relFlucPK = (RelFlucPKBean) obj;
        if (id != relFlucPK.getId() || flucId != relFlucPK.getFlucId()) {
            return false;
        }

        return true;
    }

}
