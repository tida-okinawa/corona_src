/**
 * @version $Id$
 * 
 * 2013/10/24 18:30:13
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
 * プロジェクト-辞書リレーション
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "rel_prj_dic")
public class RelPrjDicBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = -8686499845815917542L;


    // 複合キー
    RelPrjDicPKBean primaryKey;


    /**
     * コンストラクタ
     */
    public RelPrjDicBean() {
    }


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public RelPrjDicPKBean getPrimaryKey() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKey(RelPrjDicPKBean primaryKey) {
        this.primaryKey = primaryKey;
    }

}
