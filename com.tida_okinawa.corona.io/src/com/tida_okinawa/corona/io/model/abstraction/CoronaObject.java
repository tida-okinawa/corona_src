/**
 * @version $Id: CoronaObject.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/16 10:16:10
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.abstraction;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.io.model.ICoronaObject;

/**
 * @author shingo-takahashi
 */
public abstract class CoronaObject implements ICoronaObject {

    @Override
    public Object getEditableValue() {
        // 処理なし
        return "";
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        // 処理なし
        return (new IPropertyDescriptor[0]);
    }


    @Override
    public Object getPropertyValue(Object id) {
        // 処理なし
        return "";
    }


    @Override
    public boolean isPropertySet(Object id) {
        // 処理なし
        return false;
    }


    @Override
    public void resetPropertyValue(Object id) {
        // 処理なし
    }


    @Override
    public void setPropertyValue(Object id, Object value) {
        // 処理なし
    }
}
