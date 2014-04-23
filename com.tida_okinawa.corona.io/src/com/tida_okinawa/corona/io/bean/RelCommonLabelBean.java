/**
 * @version $Id$
 * 
 * 2013/10/24 17:31:04
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;


/**
 * ラベルリレーション
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "rel_common_label")
public class RelCommonLabelBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = -5654813213012666392L;


    private int relLabelId; // リレーションラベルID
    private int labelId; // ラベルID
    private int dicId; // 辞書ID
    private int itemId; // 用語ID
    private Integer value; // 値
    private boolean mathFlag; // MATHフラグ


    /**
     * @return リレーションラベルID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "REL_LABEL_ID", nullable = true)
    public int getRelLabelId() {
        return relLabelId;
    }


    /**
     * @param relLabelId
     *            リレーションラベルID
     */

    public void setRelLabelId(int relLabelId) {
        this.relLabelId = relLabelId;
    }


    /**
     * @return ラベルID
     */
    @Column(name = "LABEL_ID", nullable = true)
    @Index(name = "LABEL_ID", columnNames = { "LABEL_ID" })
    public int getLabelId() {
        return labelId;
    }


    /**
     * @param labelId
     *            ラベルID
     */
    public void setLabelId(int labelId) {
        this.labelId = labelId;
    }


    /**
     * @return 辞書ID
     */
    @Column(name = "DIC_ID", nullable = true)
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
     * @return 用語ID
     */
    @Column(name = "ITEM_ID", nullable = true)
    @Index(name = "ITEM_ID", columnNames = { "ITEM_ID" })
    public int getItemId() {
        return itemId;
    }


    /**
     * @param itemId
     *            用語ID
     */
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }


    /**
     * @return 値
     */
    @Column(name = "VALUE")
    public Integer getValue() {
        return value;
    }


    /**
     * @param value
     *            値
     */
    public void setValue(Integer value) {
        this.value = value;
    }


    /**
     * @return MATHフラグ
     */
    @Column(name = "MATH_FLG")
    public boolean isMathFlag() {
        return mathFlag;
    }


    /**
     * @param mathFlag
     *            MATHフラグ
     */
    public void setMathFlag(boolean mathFlag) {
        this.mathFlag = mathFlag;
    }


}
