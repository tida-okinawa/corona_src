/**
 * @file Product.java
 * @version $Id$
 * 
 * 2013/10/28 9:59:32
 * @author hajime-uchihara
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

/**
 * ターゲット
 * 
 * @author hajime-uchihara
 * 
 */
@Entity
@Table(name = "product")
public class ProductBean implements Serializable {

    /**
     * シリアルID
     */
    private static final long serialVersionUID = -6228429343770456823L;
    /**
     * ターゲットID
     */
    private int productId;
    /**
     * ターゲット名
     */
    private String productName;

    // リレーション関連
    private Set<RelPrjProductBean> relPrjProductBean; // プロジェクト-ターゲットリレーション
    private Set<RelClmProductBean> relClmProductBean; // ターゲット-問い合わせリレーション
    private Set<RelProductDicBean> relProductDicBean; // ターゲット-辞書リレーション


    /**
     * @return ターゲットID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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


    /**
     * @return ターゲット名
     */
    @Column(name = "PRODUCT_NAME", columnDefinition = "varchar(100)", nullable = false)
    public String getProductName() {
        return productName;
    }


    /**
     * @param productName
     *            ターゲット名
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }


    /**
     * @return プロジェクト-ターゲットリレーション
     */
    @OneToMany(targetEntity = RelPrjProductBean.class)
    @ForeignKey(name = "REL_PRJ_PRODUCT_IBFK_1")
    @JoinColumn(name = "PRODUCT_ID")
    public Set<RelPrjProductBean> getRelPrjProductBean() {
        return relPrjProductBean;
    }


    /**
     * @param relPrjProductBean
     *            プロジェクト-ターゲットリレーション
     */
    public void setRelPrjProductBean(Set<RelPrjProductBean> relPrjProductBean) {
        this.relPrjProductBean = relPrjProductBean;
    }


    /**
     * @return ターゲット-問い合わせリレーション
     */
    @OneToMany(targetEntity = RelClmProductBean.class)
    @ForeignKey(name = "REL_CLM_PRODUCT_IBFK_1")
    @JoinColumn(name = "PRODUCT_ID")
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


    /**
     * @return ターゲット-辞書リレーション
     */
    @OneToMany(targetEntity = RelProductDicBean.class)
    @ForeignKey(name = "REL_PRODUCT_DIC_IBFK_2")
    @JoinColumn(name = "PRODUCT_ID")
    public Set<RelProductDicBean> getRelProductDicBean() {
        return relProductDicBean;
    }


    /**
     * @param relProductDicBean
     *            ターゲット-辞書リレーション
     */
    public void setRelProductDicBean(Set<RelProductDicBean> relProductDicBean) {
        this.relProductDicBean = relProductDicBean;
    }

}
