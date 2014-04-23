/**
 * @version $Id: CoronaFolder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/22 15:36:27
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.internal.ui.views.model.ICoronaFolder;

/**
 * Coronaプロジェクトのフォルダ系表示モデルのベース
 * 
 * @author kousuke-morishima
 */
public class CoronaFolder implements ICoronaFolder {
    private String name;


    /* public */CoronaFolder(String name) {
        this.name = name;
    }


    @Override
    public String getName() {
        return name;
    }


    /*
     * プロパティビュー対応でICoronaDicにIPropertySourceをextendsしたので、
     * 空のメソッド群を追加
     */
    @Override
    public Object getEditableValue() {
        return null;
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[0];
    }


    @Override
    public Object getPropertyValue(Object id) {
        return null;
    }


    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }


    @Override
    public void resetPropertyValue(Object id) {
    }


    @Override
    public void setPropertyValue(Object id, Object value) {
    }
}
