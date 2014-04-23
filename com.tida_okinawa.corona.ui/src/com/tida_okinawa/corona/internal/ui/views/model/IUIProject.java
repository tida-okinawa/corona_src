/**
 * @version $Id: IUIProject.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 16:31:32
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model;

import org.eclipse.core.resources.IProject;

import com.tida_okinawa.corona.io.model.ICoronaProject;

/**
 * @author kousuke-morishima
 */
public interface IUIProject extends IUILibrary {

    @Override
    public ICoronaProject getObject();


    @Override
    public IProject getResource();


    public IUIProduct[] getProducts();
}
