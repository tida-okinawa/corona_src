/**
 * @version $Id: IUIClaim.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 17:25:42
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model;

import org.eclipse.core.resources.IFile;

import com.tida_okinawa.corona.io.model.IClaimData;

/**
 * @author kousuke-morishima
 */
public interface IUIClaim extends IUIElement {

    @Override
    public IClaimData getObject();


    @Override
    public IFile getResource();
}
