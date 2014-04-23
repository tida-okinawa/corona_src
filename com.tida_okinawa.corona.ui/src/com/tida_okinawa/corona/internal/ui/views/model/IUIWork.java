/**
 * @version $Id: IUIWork.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 17:26:46
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model;

import org.eclipse.core.resources.IFile;

import com.tida_okinawa.corona.io.model.IClaimWorkData;

/**
 * @author kousuke-morishima
 */
public interface IUIWork extends IUIElement {

    @Override
    public IClaimWorkData getObject();


    @Override
    public IFile getResource();
}
