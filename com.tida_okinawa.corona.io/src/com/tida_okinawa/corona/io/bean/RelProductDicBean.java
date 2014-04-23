/**
 * @version $Id$
 * 
 * 2013/10/28 17:45:59
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
 * ターゲット-辞書リレーション
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "rel_product_dic")
public class RelProductDicBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = 4820754325612071672L;

    // 複合キー
    private RelProductDicPKBean primaryKey;


    /**
     * コンストラクタ
     */
    public RelProductDicBean() {
    }


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public RelProductDicPKBean getPrimaryKey() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKey(RelProductDicPKBean primaryKey) {
        this.primaryKey = primaryKey;
    }


}
