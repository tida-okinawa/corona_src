/**
 * @version $Id$
 * 
 * 2013/10/28 17:35:39
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
 * プロジェクト-辞書リレーション(複合キー)
 * 
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class RelPrjDicPKBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = 221978292144984956L;


    /**
     * コンストラクタ
     */
    public RelPrjDicPKBean() {

    }


    /**
     * コンストラクタ
     * 
     * @param projectId
     *            プロジェクトID
     * @param dicId
     *            辞書ID
     */
    public RelPrjDicPKBean(int projectId, int dicId) {
        this.projectId = projectId;
        this.dicId = dicId;
    }

    private int projectId; // プロジェクトID
    private int dicId; // 辞書ID


    /**
     * @return プロジェクトID
     */
    @Column(name = "PROJECT_ID", nullable = false)
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


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dicId;
        result = prime * result + projectId;
        return result;
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof RelPrjDicPKBean)) {
            return false;
        }

        RelPrjDicPKBean relPrjDicPK = (RelPrjDicPKBean) obj;
        if (projectId != relPrjDicPK.getProjectId() || dicId != relPrjDicPK.getDicId()) {
            return false;
        }

        return true;
    }


}
