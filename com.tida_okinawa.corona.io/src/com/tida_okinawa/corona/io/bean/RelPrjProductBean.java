/**
 * @version $Id$
 * 
 * 2013/10/28 18:47:43
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * プロジェクト情報-ターゲットリレーション
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "rel_prj_product")
public class RelPrjProductBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = -8296034725655756658L;


    // 複合キー
    RelPrjProductPKBean primaryKey;


    /**
     * コンストラクタ
     */
    public RelPrjProductBean() {
    }


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public RelPrjProductPKBean getPrimaryKey() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKey(RelPrjProductPKBean primaryKey) {
        this.primaryKey = primaryKey;
    }

}
