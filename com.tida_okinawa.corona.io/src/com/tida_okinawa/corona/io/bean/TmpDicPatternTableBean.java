/**
 * @version $Id$
 * 
 * 2013/11/06 19:43:57
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author kaori-jiroku
 * 
 */
@Entity
public class TmpDicPatternTableBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -744920827056994578L;

    private int id; // ID
    private int dicId; // 辞書ID
    private String name; // パターン名
    private String pattern; // パターン情報
    private int typeId; // パターン分類ID
    private boolean parts; // 参照可否フラグ
    private boolean inactive; // INACTIVE


    /**
     * @return ID
     */
    @Id
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
     * @return 辞書ID
     */
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
     * @return パターン名
     */
    @Column(name = "NAME", columnDefinition = "varchar(80)")
    public String getName() {
        return name;
    }


    /**
     * @param name
     *            パターン名
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return パターン情報
     */
    @Column(name = "PATTERN", columnDefinition = "text", nullable = false)
    public String getPattern() {
        return pattern;
    }


    /**
     * @param pattern
     *            パターン情報
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }


    /**
     * @return パターン分類ID
     */
    @Column(name = "TYPE_ID", nullable = false)
    public int getTypeId() {
        return typeId;
    }


    /**
     * @param typeId
     *            パターン分類ID
     */
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }


    /**
     * @return 参照可否フラグ
     */
    @Column(name = "PARTS")
    public boolean isParts() {
        return parts;
    }


    /**
     * @param parts
     *            参照可否フラグ
     */
    public void setParts(boolean parts) {
        this.parts = parts;
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


}
