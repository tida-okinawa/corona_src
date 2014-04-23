/**
 * @version $Id$
 * 
 * 2013/10/28 16:20:01
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
 * プロジェクト-問い合わせデータリレーション(複合キー)
 * 
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class RelPrjClmPKBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = -2835686381202568463L;

    private int projectId; // プロジェクトID
    private int tableId; // テーブルID


    /**
     * コンストラクタ
     */
    public RelPrjClmPKBean() {
    }


    /**
     * コンストラクタ
     * 
     * @param projectId
     *            プロジェクトID
     * @param tableId
     *            テーブルID
     */
    public RelPrjClmPKBean(int projectId, int tableId) {
        this.projectId = projectId;
        this.tableId = tableId;
    }


    /**
     * @return プロジェクトID
     */
    @Column(name = "PRJ_ID", nullable = false)
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
     * @return テーブルID
     */
    @Column(name = "TBL_ID", nullable = false)
    @Index(name = "TBL_ID", columnNames = { "TBL_ID" })
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
        result = prime * result + tableId;
        return result;
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof RelPrjClmPKBean)) {
            return false;
        }

        RelPrjClmPKBean relPrjClmPK = (RelPrjClmPKBean) obj;
        if (projectId != relPrjClmPK.getProjectId() || tableId != relPrjClmPK.getTableId()) {
            return false;
        }

        return true;
    }

}
