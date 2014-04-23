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

/**
 * ワークデータ
 * 
 * @author kaori-jiroku
 * 
 */
@Table(name = "workdatas")
@Entity
public class WorkdatasBean implements Serializable {


    /**
	 * 
	 */
    private static final long serialVersionUID = -7014091851697483148L;
    private int id; // ID
    private int projectId; // プロジェクトID
    private int productId; // ターゲットID
    private int inputTableId; // INPUT_TABLE_ID
    private Integer type; // 種別
    private Integer historyId; // 履歴ID
    private Integer formerHistoryId; // クレンジング元履歴ID
    private String link; // LINK
    private Date lasted; // 更新日付

    // リレーション関連用
    private Set<DicPriorityBean> dicPriorityBean; // 辞書プライオリティ


    /**
     * @return ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
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
     * @return ターゲットID
     */
    @Column(name = "PRODUCT_ID", nullable = false)
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
     * @return INPUT_TABLE_ID
     */
    @Column(name = "INPUT_TABLE_ID", nullable = false)
    public int getInputTableId() {
        return inputTableId;
    }


    /**
     * @param inputTableId
     *            INPUT_TABLE_ID
     */
    public void setInputTableId(int inputTableId) {
        this.inputTableId = inputTableId;
    }


    /**
     * @return 種別
     */
    @Column(name = "TYPE")
    public Integer getType() {
        return type;
    }


    /**
     * @param type
     *            種別
     */
    public void setType(Integer type) {
        this.type = type;
    }


    /**
     * @return LINK
     */
    @Column(name = "LINK", columnDefinition = "varchar(255)")
    public String getLink() {
        return link;
    }


    /**
     * @param link
     *            LINK
     */
    public void setLink(String link) {
        this.link = link;
    }


    /**
     * @return 更新日時
     */
    @Version
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LASTED")
    public Date getLasted() {
        return lasted;
    }


    /**
     * @param lasted
     *            更新日時
     */
    public void setLasted(Date lasted) {
        this.lasted = lasted;
    }


    /**
     * @return 履歴ID
     */
    @Column(name = "HISTORY_ID")
    public Integer getHistoryId() {
        return historyId;
    }


    /**
     * @param historyId
     *            履歴ID
     */
    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }


    /**
     * @return クレンジング元履歴ID
     */
    @Column(name = "FORMER_HISTORY_ID")
    public Integer getFormerHistoryId() {
        return formerHistoryId;
    }


    /**
     * @param formerHistoryId
     *            クレンジング元履歴ID
     */
    public void setFormerHistoryId(Integer formerHistoryId) {
        this.formerHistoryId = formerHistoryId;
    }


    /**
     * @return 辞書プライオリティ
     */
    @OneToMany(targetEntity = DicPriorityBean.class)
    @ForeignKey(name = "DIC_PRI_IBFK_1")
    @JoinColumn(name = "ID")
    public Set<DicPriorityBean> getDicPriBean() {
        return dicPriorityBean;
    }


    /**
     * @param dicPriBean
     *            辞書プライオリティ
     */
    public void setDicPriBean(Set<DicPriorityBean> dicPriBean) {
        this.dicPriorityBean = dicPriBean;
    }
}
