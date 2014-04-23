/**
 * @version $Id: Field.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.table.impl;

import com.tida_okinawa.corona.io.model.abstraction.CoronaObject;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;


public class Field extends CoronaObject implements IField {

    private IFieldHeader header;


    /**
     * @param iFieldHeader
     */
    public Field(IFieldHeader header) {
        this.header = header;
    }

    private Object value;


    public void setValue(Object fieldValue) {
        this.value = fieldValue;
    }


    @Override
    public Object getValue() {
        return value;
    }


    public void setHeader(IFieldHeader header) {
        this.header = header;
    }


    @Override
    public IFieldHeader getHeader() {
        return header;
    }


    @Override
    public int getId() {
        return header.getId();
    }

}
