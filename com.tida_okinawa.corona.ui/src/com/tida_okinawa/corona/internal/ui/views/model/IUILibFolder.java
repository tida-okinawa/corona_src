/**
 * @version $Id: IUILibFolder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 17:02:58
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model;

/**
 * @author kousuke-morishima
 */
public interface IUILibFolder extends IUIContainer {
    @Override
    public IUILibrary getParent();
}
