/**
 * @version $Id$
 * 
 * 2013/10/29 21:40:13
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
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class RelSynonymPKBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1643065270568006407L;

    private int id; // ID
    private int synonymId; // 同義語ID


    /**
     * コンストラクタ
     */
    public RelSynonymPKBean() {
    }


    /**
     * コンストラクタ
     * 
     * @param id
     *            ID
     * @param synonymId
     *            同義語ID
     */
    public RelSynonymPKBean(int id, int synonymId) {
        this.id = id;
        this.synonymId = synonymId;
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
     * @return 同義語ID
     */
    @Column(name = "SYNONYM_ID", nullable = false)
    @Index(name = "SYNONYM_ID", columnNames = { "SYNONYM_ID" })
    public int getSynonymId() {
        return synonymId;
    }


    /**
     * @param synonymId
     *            同義語ID
     */
    public void setSynonymId(int synonymId) {
        this.synonymId = synonymId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + synonymId;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof RelSynonymPKBean)) {
            return false;
        }

        RelSynonymPKBean relSynonymPK = (RelSynonymPKBean) obj;
        if (id != relSynonymPK.getId() || synonymId != relSynonymPK.getSynonymId()) {
            return false;
        }
        return true;
    }


}
