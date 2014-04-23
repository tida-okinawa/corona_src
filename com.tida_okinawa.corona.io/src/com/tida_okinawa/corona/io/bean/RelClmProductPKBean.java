/**
 * @version $Id$
 * 
 * 2013/10/29 21:06:27
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
 * ターゲット-問い合わせリレーション(複合キー)
 * 
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class RelClmProductPKBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4447303504407241029L;

    private int projectId; // プロジェクトID
    private int productId; // ターゲットID
    private int tableId; // テーブルID


    /**
     * コンストラクタ
     */
    public RelClmProductPKBean() {
    }


    /**
     * コンストラクタ
     * 
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     * @param tableId
     *            テーブルID
     */
    public RelClmProductPKBean(int projectId, int productId, int tableId) {
        this.projectId = projectId;
        this.productId = productId;
        this.tableId = tableId;
    }


    /**
     * @return プロジェクトID
     */
    @Column(name = "PRJ_ID", nullable = false)
    @Index(name = "PRJ_ID", columnNames = { "PRJ_ID", "TBL_ID" })
    public int getProjectId() {
        return projectId;
    }


    /**
     * @param projectId
     *            プロジェクトID
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }


    /**
     * @return ターゲットID
     */
    @Column(name = "PRODUCT_ID", nullable = false)
    @Index(name = "PRODUCT_ID", columnNames = { "PRODUCT_ID" })
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
     * @return テーブルID
     */
    @Column(name = "TBL_ID", nullable = false)
    public int getTableId() {
        return tableId;
    }


    /**
     * @param tableId
     *            テーブルID
     */
    public void setTableId(int tableId) {
        this.tableId = tableId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + projectId;
        result = prime * result + productId;
        result = prime * result + tableId;
        return result;
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof RelClmProductPKBean)) {
            return false;
        }

        RelClmProductPKBean relClmProductPK = (RelClmProductPKBean) obj;
        if (projectId != relClmProductPK.getProjectId() || productId != relClmProductPK.getProductId() || tableId != relClmProductPK.getTableId()) {
            return false;
        }
        return true;
    }

}
