/**
 * @file Rel_clm_product.java
 * @version $Id$
 * 
 * 2013/10/28 12:08:30
 * @author hajime-uchihara
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
 * 製品-問い合わせリレーション
 * 
 * @author hajime-uchihara
 * 
 */
@Entity
@Table(name = "rel_clm_product")
public class RelClmProductBean implements Serializable {

    /**
     * シリアルID
     */
    private static final long serialVersionUID = -7449874215538432926L;

    // 複合キー
    private RelClmProductPKBean primaryKey;

    /**
     * 作業テーブルID
     */
    private Integer workTableId;
    /**
     * 関連テーブルID
     */
    private Integer relTableId;
    /**
     * ターゲットフィールド
     */
    private String targetFields;


    /**
     * コンストラクタ
     */
    public RelClmProductBean() {
    }


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public RelClmProductPKBean getPrimaryKey() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKey(RelClmProductPKBean primaryKey) {
        this.primaryKey = primaryKey;
    }


    /**
     * @return 作業テーブルID
     */
    @Column(name = "WORK_TBL_ID")
    public Integer getWorkTableId() {
        return workTableId;
    }


    /**
     * @param workTableId
     *            作業テーブルID
     */
    public void setWorkTableId(Integer workTableId) {
        this.workTableId = workTableId;
    }


    /**
     * @return 関連テーブルID
     */
    @Column(name = "REL_TBL_ID")
    public Integer getRelTableId() {
        return relTableId;
    }


    /**
     * @param relTableId
     *            関連テーブルID
     */
    public void setRelTableId(Integer relTableId) {
        this.relTableId = relTableId;
    }


    /**
     * @return ターゲットフィールド
     */
    @Column(name = "TGT_FLDS", columnDefinition = "text")
    public String getTargetFields() {
        return targetFields;
    }


    /**
     * @param targetFields
     *            ターゲットフィールド
     */
    public void setTargetFields(String targetFields) {
        this.targetFields = targetFields;
    }


}
