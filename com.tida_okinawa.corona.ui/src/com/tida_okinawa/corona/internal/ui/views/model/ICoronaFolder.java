/**
 * @version $Id: ICoronaFolder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/22 15:27:43
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model;

import com.tida_okinawa.corona.io.model.ICoronaObject;

/**
 * @author kousuke-morishima
 */
public interface ICoronaFolder extends ICoronaObject {

    /**
     * @return フォルダ名
     */
    public String getName();

}
