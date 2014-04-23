/**
 * @version $Id: FieldHeader.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.table.impl;

import com.tida_okinawa.corona.io.model.abstraction.CoronaObject;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;

/**
 * @author shingo-takahashi
 * 
 */
public class FieldHeader extends CoronaObject implements IFieldHeader {

    private int id;

    private String name;

    private String dispName;

    private String type;


    public FieldHeader(String name, String dispName, String type) {
        this.setName(name);
        this.setDispName(dispName);
        this.setType(type);
    }


    public FieldHeader(String name, String dispName, String type, int id) {
        this.setId(id);
        this.setName(name);
        this.setDispName(dispName);
        this.setType(type);
    }


    @Override
    public void setId(int id) {
        this.id = id;
    }


    @Override
    public int getId() {
        return id;
    }


    @Override
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public void setDispName(String dispName) {
        this.dispName = dispName;
    }


    @Override
    public String getDispName() {
        return dispName;
    }


    @Override
    public void setType(String type) {
        this.type = type;
    }


    @Override
    public String getType() {
        return type;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FieldHeader)) {
            return false;
        }

        FieldHeader f2 = (FieldHeader) obj;
        return (id == f2.id) && name.equals(f2.name) && dispName.equals(f2.dispName) && type.equals(f2.type);
    }

}
