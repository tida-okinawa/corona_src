/**
 * @version $Id$
 * 
 * 2013/10/24 17:30:53
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

/**
 * 同義語辞書
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "dic_synonym")
public class DicSynonymBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = 7205719677183728520L;

    private int synonymId; // 同義語ID
    private int dicId; // 辞書ID
    private int itemId; // 用語ID
    private boolean isInactive; // INACTIVE

    // リレーション
    private Set<RelSynonymBean> relSynonymBean;


    /**
     * @return 同義語ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "SYNONYM_ID", nullable = false)
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


    /**
     * @return 辞書ID
     */
    @Column(name = "DIC_ID", nullable = false)
    @Index(name = "DIC_ID", columnNames = { "DIC_ID" })
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


    /**
     * @return 用語ID
     */
    @Column(name = "ITEM_ID", nullable = false)
    @Index(name = "ITEM_ID", columnNames = { "ITEM_ID" })
    public int getItemId() {
        return itemId;
    }


    /**
     * @param itemId
     *            用語ID
     */
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }


    /**
     * @return INACTIVE
     */
    @Column(name = "INACTIVE")
    public boolean isInactive() {
        return isInactive;
    }


    /**
     * @param isInactive
     *            INACTIVE
     */
    public void setInactive(boolean isInactive) {
        this.isInactive = isInactive;
    }


    /**
     * @return 同義語リレーション
     */
    @OneToMany(targetEntity = RelSynonymBean.class)
    @ForeignKey(name = "REL_SYNONYM_IBFK_1")
    @JoinColumn(name = "SYNONYM_ID")
    public Set<RelSynonymBean> getRelSynonymBean() {
        return relSynonymBean;
    }


    /**
     * @param relSynonymBean
     *            同義語リレーション
     */
    public void setRelSynonymBean(Set<RelSynonymBean> relSynonymBean) {
        this.relSynonymBean = relSynonymBean;
    }

}
