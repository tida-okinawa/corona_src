/**
 * @version $Id: IUILibrary.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/13 9:29:03
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model;

import com.tida_okinawa.corona.io.model.ICoronaDics;


/**
 * @author kousuke-morishima
 */
public interface IUILibrary extends IUIContainer {

    @Override
    ICoronaDics getObject();
}
