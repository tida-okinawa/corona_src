/**
 * @version $Id$
 * 
 * 2013/11/06 19:22:28
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

/**
 * @author kaori-jiroku
 * 
 */
@Entity
public class UsrRelptnTableBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6370027537220040731L;

    private int id; // ID
    private int workId; // 作業ID
    private int fieldId; // フィールドID
    private int history; // 履歴
    private int recId; // レコードID
    private int patternId; // パターンID
    private String hitInfo; // ヒット位置情報


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
     * @return 作業ID
     */
    @Column(name = "WORK_ID", nullable = false)
    public int getWorkId() {
        return workId;
    }


    /**
     * @param workId
     *            作業ID
     */
    public void setWorkId(int workId) {
        this.workId = workId;
    }


    /**
     * @return フィールドID
     */
    @Column(name = "FLD_ID", nullable = false)
    public int getFieldId() {
        return fieldId;
    }


    /**
     * @param fieldId
     *            フィールドID
     */
    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }


    /**
     * @return 履歴
     */
    @Column(name = "HISTORY", nullable = false)
    public int getHistory() {
        return history;
    }


    /**
     * @param history
     *            履歴
     */
    public void setHistory(int history) {
        this.history = history;
    }


    /**
     * @return レコードID
     */
    @Column(name = "REC_ID", nullable = false)
    public int getRecId() {
        return recId;
    }


    /**
     * @param recId
     *            レコードID
     */
    public void setRecId(int recId) {
        this.recId = recId;
    }


    /**
     * @return パターンID
     */
    @Column(name = "PATTERN_ID", nullable = false)
    public int getPatternId() {
        return patternId;
    }


    /**
     * @param patternId
     *            パターンID
     */
    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }


    /**
     * @return ヒット位置情報
     */
    @Column(name = "HIT_INFO")
    public String getHitInfo() {
        return hitInfo;
    }


    /**
     * @param hitInfo
     *            ヒット位置情報
     */
    public void setHitInfo(String hitInfo) {
        this.hitInfo = hitInfo;
    }


}
