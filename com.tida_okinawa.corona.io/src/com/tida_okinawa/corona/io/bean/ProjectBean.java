/**
 * @file Project.java
 * @version $Id$
 * 
 * 2013/10/28 10:39:24
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
 * プロジェクト情報
 * 
 * @author hajime-uchihara
 * 
 */
@Entity
@Table(name = "project")
public class ProjectBean implements Serializable {

    /**
     * シリアルID
     */
    private static final long serialVersionUID = -455612023530240965L;
    /**
     * プロジェクトID
     */
    private int projectId;
    /**
     * プロジェクト名
     */
    private String projectName;
    /**
     * GUID
     */
    private String guId;
    /**
     * KNP実行設定フラグ
     */
    private int knpConfig;


    // リレーション
    private Set<RelPrjProductBean> relPrjProductBean; // プロジェクト-ターゲットリレーション
    private Set<RelPrjDicBean> relPrjDicBean; // プロジェクト-辞書リレーション
    private Set<RelPrjClmBean> relPrClmBean; // プロジェクト-問い合わせリレーション


    /**
     * @return プロジェクトID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
     * @return プロジェクト名
     */
    @Column(name = "PROJECT_NAME", columnDefinition = "varchar(80)", nullable = false)
    public String getProjectName() {
        return projectName;
    }


    /**
     * @param projectName
     *            プロジェクト名
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }


    /**
     * @return GID
     */
    @Column(name = "GUID", columnDefinition = "varchar(0)", nullable = true)
    public String getGuId() {
        return guId;
    }


    /**
     * @param guId
     *            GID
     */
    public void setGuId(String guId) {
        this.guId = guId;
    }


    /**
     * @return KNP実行設定フラグ
     */
    @Column(name = "KNP_CONFIG", nullable = false)
    public int getKnpConfig() {
        return knpConfig;
    }


    /**
     * @param knpConfig
     *            KNP実行設定フラグ
     */
    public void setKnpConfig(int knpConfig) {
        this.knpConfig = knpConfig;
    }


    /**
     * @return プロジェクト-辞書リレーション
     */
    @OneToMany(targetEntity = RelPrjDicBean.class)
    @ForeignKey(name = "REL_PRJ_DIC_IBFK_2")
    @JoinColumn(name = "PROJECT_ID")
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
     * @return プロジェクト-問い合わせリレーション
     */
    @OneToMany(targetEntity = RelPrjClmBean.class)
    @ForeignKey(name = "REL_PRJ_CLM_IBFK_1")
    @JoinColumn(name = "PRJ_ID", referencedColumnName = "PROJECT_ID")
    public Set<RelPrjClmBean> getRelPrClmBean() {
        return relPrClmBean;
    }


    /**
     * @param relPrClmBean
     *            プロジェクト-問い合わせリレーション
     */
    public void setRelPrClmBean(Set<RelPrjClmBean> relPrClmBean) {
        this.relPrClmBean = relPrClmBean;
    }


    /**
     * @return プロジェクト-ターゲットリレーション
     */
    @OneToMany(targetEntity = RelPrjProductBean.class)
    @ForeignKey(name = "REL_PRJ_PRODUCT_IBFK_2")
    @JoinColumn(name = "PROJECT_ID")
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


}
