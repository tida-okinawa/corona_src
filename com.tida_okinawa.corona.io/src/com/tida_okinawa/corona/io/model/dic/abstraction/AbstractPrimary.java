package com.tida_okinawa.corona.io.model.dic.abstraction;

import java.util.UUID;

/**
 * 一意なオブジェクトを返す。
 * 
 * @author Shingo-Kuniyoshi(Glovalex)
 * 
 */
public class AbstractPrimary {

    private int _primaryKey;
    private Object _obj;


    public AbstractPrimary(Object obj) {
        _primaryKey = UUID.randomUUID().hashCode();
        this._obj = obj;
    }


    @Override
    public int hashCode() {
        return _primaryKey;
    }


    public Object getItem() {
        return this._obj;
    }
}
