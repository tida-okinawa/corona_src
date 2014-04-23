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
 * フィールド情報
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "fields")
public class FieldsBean implements Serializable {

    private static final long serialVersionUID = -5058836939882331538L;
    private int id; // ID
    private int tableId; // テーブルID
    private Integer fieldId; // フィールドID
    private String name; // フィールド名
    private String dbName; // DB名


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
     * @return テーブルID
     */
    @Column(name = "TBL_ID", nullable = false)
    @Index(name = "TBL_ID", columnNames = { "TBL_ID" })
    public int getTableId() {
        return tableId;
    }


    /**
     * @param tableId
     *            テーブルID
     */
    public void setTableId(int tableId) {
        this.tableId = tableId;
    }


    /**
     * @return フィールドID
     */
    @Column(name = "FLD_ID")
    public Integer getFieldId() {
        return fieldId;
    }


    /**
     * @param fieldId
     *            フィールドID
     */
    public void setFieldId(Integer fieldId) {
        this.fieldId = fieldId;
    }


    /**
     * @return フィールド名
     */
    @Column(name = "NAME", columnDefinition = "varchar(256)")
    public String getName() {
        return name;
    }


    /**
     * @param name
     *            フィールド名
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return DB名
     */
    @Column(name = "DBNAME", columnDefinition = "varchar(128)")
    public String getDbName() {
        return dbName;
    }


    /**
     * @param dbName
     *            DB名
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }


}
