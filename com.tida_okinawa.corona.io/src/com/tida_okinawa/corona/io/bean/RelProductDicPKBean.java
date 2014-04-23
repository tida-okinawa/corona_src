/**
 * @version $Id$
 * 
 * 2013/10/28 18:00:54
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Index;

/**
 * 
 * ターゲット-辞書リレーション(複合キー)
 * 
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class RelProductDicPKBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = -1750687372787270191L;


    /**
     * コンストラクタ
     */
    public RelProductDicPKBean() {
    }


    /**
     * コンストラクタ
     * 
     * @param dicId
     *            辞書ID
     * @param productId
     *            ターゲットID
     */
    public RelProductDicPKBean(int dicId, int productId) {
        this.dicId = dicId;
        this.productId = productId;
    }

    private int dicId; // 辞書ID
    private int productId; // ターゲットID


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
     * @return ターゲットID
     */
    @Column(name = "PRODUCT_ID", nullable = false)
    public int getProductId() {
        return productId;
    }


    /**
     * @param productId
     *            ターゲットID
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dicId;
        result = prime * result + productId;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof RelProductDicPKBean)) {
            return false;
        }

        RelProductDicPKBean relProductDic = (RelProductDicPKBean) obj;
        if (dicId != relProductDic.getDicId() || productId != relProductDic.getProductId()) {
            return false;
        }
        return true;
    }

}
