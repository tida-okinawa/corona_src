package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Index;

/**
 * プロジェクト-ターゲットリレーション(複合キー)
 * 
 * @author kaori-jiroku
 * 
 */
@Embeddable
public class RelPrjProductPKBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = 7044832349986469746L;


    /**
     * コンストラクタ
     */
    public RelPrjProductPKBean() {

    }


    /**
     * コンストラクタ
     * 
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     */
    public RelPrjProductPKBean(int projectId, int productId) {
        this.projectId = projectId;
        this.productId = productId;
    }

    private int projectId; // プロジェクトID
    private int productId; // ターゲットID


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
    @Column(name = "PRODUCT_ID", nullable = false)
    @Index(name = "PRODUCT_ID", columnNames = { "PRODUCT_ID" })
    public int getProductId() {
        return productId;
    }


    /**
     * @param productId
     *            プロダクトID
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + productId;
        result = prime * result + projectId;
        return result;
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof RelPrjProductPKBean)) {
            return false;
        }

        RelPrjProductPKBean relPrjProductPK = (RelPrjProductPKBean) obj;
        if (projectId != relPrjProductPK.getProjectId() || projectId != relPrjProductPK.getProductId()) {
            return false;
        }

        return true;
    }


}
