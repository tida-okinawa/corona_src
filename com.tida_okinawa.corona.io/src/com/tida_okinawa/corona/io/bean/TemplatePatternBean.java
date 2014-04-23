/**
 * @version $Id$
 * 
 * 2013/10/24 19:27:39
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

/**
 * ひな型管理
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "template_pattern")
public class TemplatePatternBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -9213885128126391039L;

    private int id; // ID
    private int templateId; // ひな型ID
    private String name; // ひな型名
    private String template; // ひな型情報
    private int typeId; // ひな型分類ID
    private boolean parts; // 参照可否フラグ
    private boolean inactive; // INACTIVE


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
     * @return ひな型ID
     */
    @Column(name = "TEMPLATE_ID", nullable = false)
    public int getTemplateId() {
        return templateId;
    }


    /**
     * @param templateId
     *            辞書ID
     */
    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }


    /**
     * @return ひな型名
     */
    @Column(name = "NAME", columnDefinition = "varchar(80)")
    public String getName() {
        return name;
    }


    /**
     * @param name
     *            ひな型名
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return ひな型情報
     */
    @Column(name = "TEMPLATE", columnDefinition = "text")
    public String getTemplate() {
        return template;
    }


    /**
     * @param template
     *            ひな型情報
     */
    public void setTemplate(String template) {
        this.template = template;
    }


    /**
     * @return ひな型分類ID
     */
    @Column(name = "TYPE_ID", nullable = false)
    public int getTypeId() {
        return typeId;
    }


    /**
     * @param typeId
     *            ひな型分類ID
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
