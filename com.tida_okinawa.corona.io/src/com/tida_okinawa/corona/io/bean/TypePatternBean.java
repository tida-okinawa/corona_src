/**
 * @version $Id$
 * 
 * 2013/10/25 15:10:50
 * @author wataru-higa
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
 * パターン分類
 * 
 * @author wataru-higa
 * 
 */
@Entity
@Table(name = "type_pattern")
public class TypePatternBean implements Serializable {

    /**
     * シリアルID
     */
    private static final long serialVersionUID = 6179502532184323966L;
    /**
     * ID
     */
    private int id;
    /**
     * パターン分類名
     */
    private String name;


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
     * @return パターン分類名
     */
    @Column(name = "NAME", columnDefinition = "varchar(20)", nullable = false)
    public String getName() {
        return name;
    }


    /**
     * @param name
     *            パターン分類名
     */
    public void setName(String name) {
        this.name = name;
    }

}
