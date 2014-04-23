/**
 * @version $Id$
 * 
 * 2013/10/25 16:27:24
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

/**
 * プロジェクト-問い合わせデータリレーション
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "rel_prj_clm")
public class RelPrjClmBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = -8972141330958517403L;

    // 複合キー
    private RelPrjClmPKBean primaryKey;

    // リレーション
    private Set<RelClmProductBean> relClmProductBean;


    /**
     * コンストラクタ
     */
    public RelPrjClmBean() {
    }


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public RelPrjClmPKBean getPrimaryKey() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKey(RelPrjClmPKBean primaryKey) {
        this.primaryKey = primaryKey;
    }


    /**
     * @return ターゲット-問い合わせリレーション
     */
    @OneToMany(targetEntity = RelClmProductBean.class)
    @ForeignKey(name = "REL_CLM_PRODUCT_IBFK_2")
    @JoinColumns({ @JoinColumn(name = "PRJ_ID"), @JoinColumn(name = "TBL_ID") })
    public Set<RelClmProductBean> getRelClmProductBean() {
        return relClmProductBean;
    }


    /**
     * @param relClmProductBean
     *            ターゲット-問い合わせリレーション
     */
    public void setRelClmProductBean(Set<RelClmProductBean> relClmProductBean) {
        this.relClmProductBean = relClmProductBean;
    }


}
