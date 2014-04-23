package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 分野(Category)
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "category")
public class CategoryBean implements Serializable {

    private static final long serialVersionUID = 6011242102078543268L;

    private int id; // ID
    private String name; // 分野名


    /**
     * @return ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
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
     * @return 分野名
     */
    @Column(name = "NAME", columnDefinition = "varchar(40)", nullable = false)
    public String getName() {
        return name;
    }


    /**
     * @param name
     *            分野名
     */
    public void setName(String name) {
        this.name = name;
    }


}
