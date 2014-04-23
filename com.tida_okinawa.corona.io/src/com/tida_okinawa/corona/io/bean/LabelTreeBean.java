/**
 * @file Label_tree.java
 * @version $Id$
 * 
 * 2013/10/28 9:42:33
 * @author hajime-uchihara
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * ラベル親子関係
 * 
 * @author hajime-uchihara
 * 
 */
@Entity
@Table(name = "label_tree")
public class LabelTreeBean implements Serializable {

    /**
     * シリアルID
     */
    private static final long serialVersionUID = -6309868594508259737L;

    // 複合キー
    private LabelTreePKBean primaryKey;


    /**
     * コンストラクタ
     */
    public LabelTreeBean() {
    }


    /**
     * @return 複合キー
     */
    @EmbeddedId
    public LabelTreePKBean getPrimaryKeyBean() {
        return primaryKey;
    }


    /**
     * @param primaryKey
     *            複合キー
     */
    public void setPrimaryKeyBean(LabelTreePKBean primaryKey) {
        this.primaryKey = primaryKey;
    }


}
