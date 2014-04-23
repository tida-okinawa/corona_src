package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * CLAIMS
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "claims")
public class ClaimsBean implements Serializable {

    private static final long serialVersionUID = 3660409023249042839L;

    private int id; // ID
    private Integer workTableId; // 作業テーブルID
    private Integer keyFieldId; // キーフィールドID
    private Integer productFieldId; // ターゲットフィールドID
    private String targetFields; // ターゲットフィールド
    private boolean externalFlag; // EXTERNALフラグ
    private boolean internalFlag; // INTERNALフラグ


    /**
     * @return ID
     */
    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }


    /**
     * @param id
     *            ID
     */
    public void setId(int id) {
        this.id = id;
    }


    /**
     * @return 作業テーブルID
     */
    @Column(name = "WORK_TBL_ID")
    public Integer getWorkTableId() {
        return workTableId;
    }


    /**
     * @param workTableId
     *            作業テーブルID
     */
    public void setWorkTableId(Integer workTableId) {
        this.workTableId = workTableId;
    }


    /**
     * @return キーフィールドID
     */
    @Column(name = "KEY_FLD_ID")
    public Integer getKeyFieldId() {
        return keyFieldId;
    }


    /**
     * @param keyFieldId
     *            キーフィールドID
     */
    public void setKeyFieldId(Integer keyFieldId) {
        this.keyFieldId = keyFieldId;
    }


    /**
     * @return ターゲットフィールドID
     */
    @Column(name = "PRODUCT_FLD_ID")
    public Integer getProductFieldId() {
        return productFieldId;
    }


    /**
     * @param productFieldId
     *            ターゲットフィールドID
     */
    public void setProductFieldId(Integer productFieldId) {
        this.productFieldId = productFieldId;
    }


    /**
     * @return ターゲットフィールド
     */
    @Column(name = "TGT_FLDS", columnDefinition = "text")
    public String getTargetFields() {
        return targetFields;
    }


    /**
     * @param targetFields
     *            ターゲットフィールド
     */
    public void setTargetFields(String targetFields) {
        this.targetFields = targetFields;
    }


    /**
     * @return EXTERNAL_FLG
     */
    @Column(name = "EXTERNAL_FLG", nullable = false)
    public boolean isExternalFlag() {
        return externalFlag;
    }


    /**
     * @param externalFlag
     *            EXTERNAL_FLG
     */
    public void setExternalFlag(boolean externalFlag) {
        this.externalFlag = externalFlag;
    }


    /**
     * @return INTERNAL_FLG
     */
    @Column(name = "INTERNAL_FLG")
    public boolean isInternalFlag() {
        return internalFlag;
    }


    /**
     * @param internalFlag
     *            INTERNAL_FLG
     */
    public void setInternalFlag(boolean internalFlag) {
        this.internalFlag = internalFlag;
    }

}
