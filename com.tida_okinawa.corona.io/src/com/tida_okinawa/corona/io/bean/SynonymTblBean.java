/**
 * @version $Id$
 * 
 * 2013/10/24 17:30:29
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
 * 同義語テーブル
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "synonym_tbl")
public class SynonymTblBean implements Serializable {

    /**
     * シリアルID
     */
    private static final long serialVersionUID = -7590342405534505829L;
    /**
     * ID
     */
    private int id;
    /**
     * 辞書ID
     */
    private int dicId;
    /**
     * 用語ID
     */
    private int itemId;
    /**
     * INACTIVE
     */
    private boolean inactive;


    // リレーション
    private Set<RelSynonymBean> relSynonymBean;


    /**
     * @return ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
        return inactive;
    }


    /**
     * @param inactive
     *            INACTIVE
     */
    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }


    /**
     * @return 同義語リレーション
     */
    @OneToMany(targetEntity = RelSynonymBean.class)
    @ForeignKey(name = "REL_SYNONYM_IBFK_2")
    @JoinColumn(name = "ID")
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
