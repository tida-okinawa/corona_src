/**
 * @version $Id$
 * 
 * 2013/10/24 15:37:19
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
 * 用語
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "dic_common")
public class DicCommonBean implements Serializable {

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = -2172998602548557998L;

    private int itemId; // 用語ID
    private String name; // 用語
    private String reading; // よみ
    private int partId; // 品詞ID
    private Integer classId; // 品詞詳細ID
    private Integer cformId; // 活用形ID
    private int dicId; // 辞書ID
    private boolean inactive; // INACTIVE
    private String jumanBase; // JUMAN_BASE

    // リレーション
    private Set<FlucTblBean> flucTableBean; // ゆらぎテーブル
    private Set<DicFlucBean> dicFlucBean; // ゆらぎ辞書
    private Set<SynonymTblBean> synonymTblBean; // 同義語テーブル
    private Set<DicSynonymBean> dicSynonymBean; // 同義語辞書
    private Set<RelCommonLabelBean> relCommonLabelBean; // ラベルリレーション


    /**
     * @return 用語ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ITEM_ID", nullable = false)
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
     * @return 用語
     */
    @Column(name = "NAME", nullable = false, columnDefinition = "varchar(255)")
    public String getName() {
        return name;
    }


    /**
     * @param name
     *            用語
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return よみ
     */
    @Column(name = "READING", nullable = false, columnDefinition = "varchar(255)")
    public String getReading() {
        return reading;
    }


    /**
     * @param reading
     *            よみ
     */
    public void setReading(String reading) {
        this.reading = reading;
    }


    /**
     * @return 品詞ID
     */
    @Column(name = "PART_ID", nullable = false)
    public int getPartId() {
        return partId;
    }


    /**
     * @param partId
     *            品詞ID
     */
    public void setPartId(int partId) {
        this.partId = partId;
    }


    /**
     * @return 品詞詳細ID
     */
    @Column(name = "CLASS_ID", nullable = true)
    public Integer getClassId() {
        return classId;
    }


    /**
     * @param classId
     *            品詞詳細ID
     */
    public void setClassId(Integer classId) {
        this.classId = classId;
    }


    /**
     * @return 活用形ID
     */
    @Column(name = "CFORM_ID", nullable = true)
    public Integer getCformId() {
        return cformId;
    }


    /**
     * @param cformId
     *            活用形ID
     */
    public void setCformId(Integer cformId) {
        this.cformId = cformId;
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
     * @return INACVIBE
     */
    @Column(name = "INACTIVE")
    public boolean isInactive() {
        return inactive;
    }


    /**
     * @param inactive
     *            INACVIBE
     */
    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }


    /**
     * @return JUMAN_BASE
     */
    @Column(name = "JUMAN_BASE", nullable = true, columnDefinition = "varchar(4096)")
    public String getJumanBase() {
        return jumanBase;
    }


    /**
     * @param jumanBase
     *            JUMANベース
     */
    public void setJumanBase(String jumanBase) {
        this.jumanBase = jumanBase;
    }


    /**
     * @return ゆらぎテーブル
     */
    @OneToMany(targetEntity = FlucTblBean.class)
    @ForeignKey(name = "FLUC_TBL_IBFK_1")
    @JoinColumn(name = "ITEM_ID")
    public Set<FlucTblBean> getFlucTableBean() {
        return flucTableBean;
    }


    /**
     * @param flucTableBean
     *            ゆらぎテーブル
     */
    public void setFlucTableBean(Set<FlucTblBean> flucTableBean) {
        this.flucTableBean = flucTableBean;
    }


    /**
     * @return 同義語テーブル
     */
    @OneToMany(targetEntity = SynonymTblBean.class)
    @ForeignKey(name = "SYNONYM_TBL_IBFK_1")
    @JoinColumn(name = "ITEM_ID")
    public Set<SynonymTblBean> getSynonymTableBean() {
        return synonymTblBean;
    }


    /**
     * @param synonymTableBean
     *            同義語テーブル
     */
    public void setSynonymTableBean(Set<SynonymTblBean> synonymTableBean) {
        this.synonymTblBean = synonymTableBean;
    }


    /**
     * @return ゆらぎ辞書
     */
    @OneToMany(targetEntity = DicFlucBean.class)
    @ForeignKey(name = "DIC_FLUC_IBFK_1")
    @JoinColumn(name = "ITEM_ID")
    public Set<DicFlucBean> getDicFlucBean() {
        return dicFlucBean;
    }


    /**
     * @param dicFlucBean
     *            ゆらぎ辞書
     */
    public void setDicFlucBean(Set<DicFlucBean> dicFlucBean) {
        this.dicFlucBean = dicFlucBean;
    }


    /**
     * @return 同義語辞書
     */
    @OneToMany(targetEntity = DicSynonymBean.class)
    @ForeignKey(name = "DIC_SYONOYM_IBFK_1")
    @JoinColumn(name = "ITEM_ID")
    public Set<DicSynonymBean> getDicSynonymBean() {
        return dicSynonymBean;
    }


    /**
     * @param dicSynonymBean
     *            同義語辞書
     */
    public void setDicSynonymBean(Set<DicSynonymBean> dicSynonymBean) {
        this.dicSynonymBean = dicSynonymBean;
    }


    /**
     * @return ラベルリレーション
     */
    @OneToMany(targetEntity = RelCommonLabelBean.class)
    @ForeignKey(name = "REL_COMMON_LABEL_IBFK_1")
    @JoinColumn(name = "ITEM_ID")
    public Set<RelCommonLabelBean> getRelCommonLabelBean() {
        return relCommonLabelBean;
    }


    /**
     * @param relCommonLabel
     *            ラベルリレーション
     */
    public void setRelCommonLabelBean(Set<RelCommonLabelBean> relCommonLabel) {
        this.relCommonLabelBean = relCommonLabel;
    }


}
