/**
 * @version $Id$
 * 
 * 2013/10/24 17:30:42
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
 * ゆらぎ辞書
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "dic_fluc")
public class DicFlucBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -728542039199436805L;

    private int flucId; // ゆらぎID
    private int dicId; // 辞書ID
    private int itemId; // 用語ID
    private boolean inactive; // INACTIVE

    // リレーション
    private Set<RelFlucBean> relFlucBean; // ゆらぎリレーション


    /**
     * @return ゆらぎID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "FLUC_ID", nullable = false)
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
    @ForeignKey(name = "REL_FLUC_IBFK_1")
    @JoinColumn(name = "FLUC_ID")
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
