/**
 * @version $Id: IUIProduct.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 16:46:38
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model;

import org.eclipse.core.resources.IFolder;

import com.tida_okinawa.corona.io.model.ICoronaProduct;

/**
 * @author kousuke-morishima
 */
public interface IUIProduct extends IUILibrary {

    @Override
    public ICoronaProduct getObject();


    @Override
    public IFolder getResource();


    /**
     * @return このターゲットが持っているすべての処理結果
     */
    public IUIWork[] getWorks();
}
