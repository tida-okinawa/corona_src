/**
 * @version $Id$
 * 
 * 2013/10/24 16:33:14
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;


/**
 * 辞書管理Bean
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "dic_table")
public class DicTableBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3245011011994845777L;

    private int dicId; // 辞書ID
    private String parentId; // 親辞書ID
    private String dicName; // 辞書名
    private String dicFileName; // 辞書ファイル名
    private int dicType; // 辞書種別
    private Integer categoryId; // 分野ID
    private Date date; // 更新日付
    private boolean inactive; // INACTIVE
    private Date creationTime; // 作成日付

    // リレーション
    private Set<RelPrjDicBean> relPrjDicBean; // プロジェクト-辞書リレーション
    private Set<RelProductDicBean> relProductDicBean; // ターゲット-辞書リレーション
    private Set<DicLabelBean> dicLabelBean; // ラベル辞書
    private Set<FlucTblBean> flucTblBean; // ゆらぎテーブル
    private Set<SynonymTblBean> synonymTblBean; // 同義語テーブル
    private Set<DicFlucBean> dicFlucBean; // ゆらぎ辞書
    private Set<DicSynonymBean> dicSynonymBean; // 同義語辞書
    private Set<RelCommonLabelBean> relCommonLabelBean; // ラベルリレーション
    private Set<DicPatternBean> dicPatternBean; // パターン辞書
    private Set<DicCommonBean> dicCommonBean; // 用語


    /**
     * @return 辞書ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "DIC_ID", nullable = false)
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
     * @return 親辞書ID
     */
    @Column(name = "PARENT_ID", columnDefinition = "varchar(256)")
    public String getParentId() {
        return parentId;
    }


    /**
     * @param parentId
     *            親辞書ID
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }


    /**
     * @return 辞書名
     */
    @Column(name = "DIC_NAME", nullable = false, columnDefinition = "varchar(255)")
    public String getDicName() {
        return dicName;
    }


    /**
     * @param dicName
     *            辞書名
     */
    public void setDicName(String dicName) {
        this.dicName = dicName;
    }


    /**
     * @return 辞書ファイル名
     */
    @Column(name = "DIC_FILE_NAME", nullable = false, columnDefinition = "varchar(255)")
    public String getDicFileName() {
        return dicFileName;
    }


    /**
     * @param dicFileName
     *            辞書ファイル名
     */
    public void setDicFileName(String dicFileName) {
        this.dicFileName = dicFileName;
    }


    /**
     * @return 辞書種別
     */
    @Column(name = "DIC_TYPE", nullable = false)
    public int getDicType() {
        return dicType;
    }


    /**
     * @param dicType
     *            辞書種別
     */
    public void setDicType(int dicType) {
        this.dicType = dicType;
    }


    /**
     * @return 分野ID
     */
    @Column(name = "CATEGORY_ID")
    @Index(name = "CATEGORY_ID", columnNames = { "CATEGORY_ID" })
    public Integer getCategoryId() {
        return categoryId;
    }


    /**
     * @param categoryId
     *            分野ID
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }


    /**
     * @return 更新日付
     */
    @Version
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE")
    public Date getDate() {
        return date;
    }


    /**
     * @param date
     *            更新日付
     */
    public void setDate(Date date) {
        this.date = date;
    }


    /**
     * @return INACTIVE
     */
    @Column(name = "INACTIVE")
    public boolean isInactive() {
        return inactive;
    }


    /**
     * @param inactive
     *            INACTIVE
     */
    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }


    /**
     * @return 作成日時
     */
    @Version
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATION_TIME")
    public Date getCreationTime() {
        return creationTime;
    }


    /**
     * @param creationTime
     *            作成日付
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }


    /**
     * @return プロジェクト-辞書リレーション
     */
    @OneToMany(targetEntity = RelPrjDicBean.class)
    @ForeignKey(name = "REL_PRJ_DIC_IBFK_1")
    @JoinColumn(name = "DIC_ID")
    public Set<RelPrjDicBean> getRelPrjDicBean() {
        return relPrjDicBean;
    }


    /**
     * @param relPrjDicBean
     *            プロジェクト-辞書リレーション
     */
    public void setRelPrjDicBean(Set<RelPrjDicBean> relPrjDicBean) {
        this.relPrjDicBean = relPrjDicBean;
    }


    /**
     * @return ラベル辞書
     */
    @OneToMany(targetEntity = DicLabelBean.class)
    @ForeignKey(name = "DIC_LABEL_IBFK_1")
    @JoinColumn(name = "DIC_ID")
    public Set<DicLabelBean> getDicLabelBean() {
        return dicLabelBean;
    }


    /**
     * @param dicLabelBean
     *            ラベル辞書
     */
    public void setDicLabelBean(Set<DicLabelBean> dicLabelBean) {
        this.dicLabelBean = dicLabelBean;
    }


    /**
     * @return ゆらぎテーブル
     */
    @OneToMany(targetEntity = FlucTblBean.class)
    @ForeignKey(name = "FLUC_TBL_IBFK_2")
    @JoinColumn(name = "DIC_ID")
    public Set<FlucTblBean> getFlucTblBean() {
        return flucTblBean;
    }


    /**
     * @param flucTblBean
     *            ゆらぎテーブル
     */
    public void setFlucTblBean(Set<FlucTblBean> flucTblBean) {
        this.flucTblBean = flucTblBean;
    }


    /**
     * @return 同義語テーブル
     */
    @OneToMany(targetEntity = SynonymTblBean.class)
    @ForeignKey(name = "SYNONYM_TBL_IBFK_2")
    @JoinColumn(name = "DIC_ID")
    public Set<SynonymTblBean> getSynonymTblBean() {
        return synonymTblBean;
    }


    /**
     * @param synonymTblBean
     *            同義語テーブル
     */
    public void setSynonymTblBean(Set<SynonymTblBean> synonymTblBean) {
        this.synonymTblBean = synonymTblBean;
    }


    /**
     * @return ゆらぎ辞書
     */
    @OneToMany(targetEntity = DicFlucBean.class)
    @ForeignKey(name = "DIC_FLUC_IBFK_2")
    @JoinColumn(name = "DIC_ID")
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
    @ForeignKey(name = "DIC_SYNONYM_IBFK_2")
    @JoinColumn(name = "DIC_ID")
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
    @ForeignKey(name = "REL_COMMON_LABEL_IBFK_3")
    @JoinColumn(name = "DIC_ID")
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
     * @return 用語
     */
    @OneToMany(targetEntity = DicCommonBean.class)
    @ForeignKey(name = "DIC_COMMON_IBFK_1")
    @JoinColumn(name = "DIC_ID")
    public Set<DicCommonBean> getDicCommonBean() {
        return dicCommonBean;
    }


    /**
     * @param dicCommonBean
     *            用語
     */
    public void setDicCommonBean(Set<DicCommonBean> dicCommonBean) {
        this.dicCommonBean = dicCommonBean;
    }


    /**
     * @return パターン辞書
     */
    @OneToMany(targetEntity = DicPatternBean.class)
    @ForeignKey(name = "DIC_PATTERN_IBFK_1")
    @JoinColumn(name = "DIC_ID")
    public Set<DicPatternBean> getDicPatternBean() {
        return dicPatternBean;
    }


    /**
     * @param dicPatternBean
     *            パターン辞書
     */
    public void setDicPatternBean(Set<DicPatternBean> dicPatternBean) {
        this.dicPatternBean = dicPatternBean;
    }


    /**
     * @return ターゲット-辞書リレーション
     */
    @OneToMany(targetEntity = RelProductDicBean.class)
    @ForeignKey(name = "DIC_ID")
    @JoinColumn(name = "REL_PRODUCT_DIC_IBFK_1")
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
