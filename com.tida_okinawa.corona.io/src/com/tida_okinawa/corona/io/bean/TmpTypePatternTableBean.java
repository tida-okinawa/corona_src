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
public class TmpTypePatternTableBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -744920827056994578L;

    private int id; // ID
    private String name; // パターン名


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
     * @return パターン名
     */
    @Column(name = "NAME", columnDefinition = "varchar(20)")
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
}
