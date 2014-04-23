/**
 * @version $Id$
 * 
 * 2013/10/24 18:30:24
 * @author kaori-jiroku
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
import org.hibernate.annotations.Index;

/**
 * ラベル辞書
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "dic_label")
public class DicLabelBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = -2000336731609781421L;


    private int labelId; // ラベルID
    private int dicId; // 辞書ID
    private String labelName; // ラベル名
    private boolean isInactive; // INACTIVE

    // リレーション
    private Set<RelCommonLabelBean> relCommonLabelBean; // ラベルリレーション
    private Set<LabelTreeBean> labelTreeParentBean; // ラベル親子関係(外部キー1)
    private Set<LabelTreeBean> labelTreeChildBean; // ラベル親子関係(外部キー2)


    /**
     * @return ラベルID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "LABEL_ID", nullable = false)
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
     * @return ラベル名
     */
    @Column(name = "LABEL_NAME", columnDefinition = "varchar(40)")
    public String getLabelName() {
        return labelName;
    }


    /**
     * @param labelName
     *            ラベル名
     */
    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }


    /**
     * @return INACTIVE
     */
    @Column(name = "INACTIVE")
    public boolean isInactive() {
        return isInactive;
    }


    /**
     * @param isInactive
     *            INACTIVE
     */
    public void setInactive(boolean isInactive) {
        this.isInactive = isInactive;
    }


    /* --- リレーション関連定義 --- */
    /**
     * @return ラベルリレーション
     */
    @OneToMany(targetEntity = RelCommonLabelBean.class)
    @ForeignKey(name = "REL_COMMON_LABEL_IBFK_2")
    @JoinColumn(name = "LABEL_ID")
    public Set<RelCommonLabelBean> getRelCommonLabelBean() {
        return relCommonLabelBean;
    }


    /**
     * @param relCommonLabelBean
     *            ラベルリレーション
     */
    public void setRelCommonLabelBean(Set<RelCommonLabelBean> relCommonLabelBean) {
        this.relCommonLabelBean = relCommonLabelBean;
    }


    /**
     * @return ラベル親子関係(外部キー1)
     */
    @OneToMany(targetEntity = LabelTreeBean.class)
    @ForeignKey(name = "LABEL_TREE_IBFK_2")
    @JoinColumn(name = "PARENT_ID", referencedColumnName = "LABEL_ID")
    public Set<LabelTreeBean> getLabelTreeParentBean() {
        return labelTreeParentBean;
    }


    /**
     * @param labelTreeParentBean
     *            ラベル親子関係(外部キー1)
     */
    public void setLabelTreeParentBean(Set<LabelTreeBean> labelTreeParentBean) {
        this.labelTreeParentBean = labelTreeParentBean;
    }


    /**
     * @return ラベル親子関係(外部キー2)
     */
    @OneToMany(targetEntity = LabelTreeBean.class)
    @ForeignKey(name = "LABEL_TREE_IBFK_1")
    @JoinColumn(name = "CHILD_ID", referencedColumnName = "LABEL_ID")
    public Set<LabelTreeBean> getLabelTreeChildBean() {
        return labelTreeChildBean;
    }


    /**
     * @param labelTreeChildBean
     *            ラベル親子関係(外部キー2)
     */
    public void setLabelTreeChildBean(Set<LabelTreeBean> labelTreeChildBean) {
        this.labelTreeChildBean = labelTreeChildBean;
    }

}
