/**
 * @version $Id$
 * 
 * 2013/10/24 17:17:01
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
 * ゆらぎテーブル
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "fluc_tbl")
public class FlucTblBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2963286209534395793L;


    private int id; // ID
    private int dicId; // 辞書ID
    private int itemId; // 用語ID
    private boolean inactive; // INACTIVE

    // リレーション
    private Set<RelFlucBean> relFlucBean;


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
     * @return ゆらぎリレーション
     */
    @OneToMany(targetEntity = RelFlucBean.class)
    @ForeignKey(name = "REL_FLUC_IBFK_2")
    @JoinColumn(name = "ID")
    public Set<RelFlucBean> getRelFlucBean() {
        return relFlucBean;
    }


    /**
     * @param relFlucBean
     *            ゆらぎリレーション
     */
    public void setRelFlucBean(Set<RelFlucBean> relFlucBean) {
        this.relFlucBean = relFlucBean;
    }

}
