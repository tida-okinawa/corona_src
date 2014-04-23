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
 * javax.persistence.Tableと重複するので命名変更
 * 
 * @author satmin
 * 
 */
@Entity
@Table(name = "tables")
public class TablesBean implements Serializable {

    private static final long serialVersionUID = 5388895689388131591L;

    private int id; // ID
    private String name; // 名称
    private String dbname; // DB名
    private int type; // 種別
    private String comment; // コメント
    private Date lasted; // 更新日時

    // リレーション関連
    private Set<FieldsBean> fieldsBean; // FIELDS
    private Set<ClaimsBean> claimsBean; // CLAIMS
    private Set<RelPrjClmBean> relPrjClmBean; // REL_PRJ_CLM


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
     * @return 名称
     */
    @Column(name = "NAME", columnDefinition = "varchar(255)", nullable = false)
    public String getName() {
        return name;
    }


    /**
     * @param name
     *            名称
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return DB名
     */
    @Column(name = "DBNAME", columnDefinition = "varchar(255)", nullable = false)
    public String getDbname() {
        return dbname;
    }


    /**
     * @param dbname
     *            DB名
     */
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }


    /**
     * @return 種別
     */
    @Column(name = "TYPE", nullable = false)
    public int getType() {
        return type;
    }


    /**
     * @param type
     *            種別
     */
    public void setType(int type) {
        this.type = type;
    }


    /**
     * @return コメント
     */
    @Column(name = "COMMENT", columnDefinition = "varchar(255)")
    public String getComment() {
        return comment;
    }


    /**
     * @param comment
     *            コメント
     */
    public void setComment(String comment) {
        this.comment = comment;
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
     * @return FIELDS
     */
    @OneToMany(targetEntity = FieldsBean.class)
    @ForeignKey(name = "FIELDS_IBFK_1")
    @JoinColumn(name = "TBL_ID", referencedColumnName = "ID")
    public Set<FieldsBean> getFields() {
        return fieldsBean;
    }


    /**
     * @param fields
     *            FIELDS
     */
    public void setFields(Set<FieldsBean> fields) {
        this.fieldsBean = fields;
    }


    /**
     * @return CLAIMS
     */
    @OneToMany(targetEntity = ClaimsBean.class)
    @ForeignKey(name = "CLAIMS_IBFK_1")
    @JoinColumn(name = "ID")
    public Set<ClaimsBean> getClaimsBean() {
        return claimsBean;
    }


    /**
     * @param claimsBean
     *            CLAIMS
     */
    public void setClaimsBean(Set<ClaimsBean> claimsBean) {
        this.claimsBean = claimsBean;
    }


    /**
     * @return REL_PRJ_CLM
     */
    @OneToMany(targetEntity = RelPrjClmBean.class)
    @ForeignKey(name = "REL_PRJ_CLM_IBFK_2")
    @JoinColumn(name = "TBL_ID", referencedColumnName = "ID")
    public Set<RelPrjClmBean> getRelPrjClmBean() {
        return relPrjClmBean;
    }


    /**
     * @param relPrjClmBean
     *            REL_PRJ_CLM
     */
    public void setRelPrjClmBean(Set<RelPrjClmBean> relPrjClmBean) {
        this.relPrjClmBean = relPrjClmBean;
    }


}
