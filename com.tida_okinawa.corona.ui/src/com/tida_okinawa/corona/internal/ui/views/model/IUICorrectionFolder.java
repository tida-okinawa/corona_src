/**
 * @version $Id: IUICorrectionFolder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 17:08:44
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model;

/**
 * @author kousuke-morishima
 */
public interface IUICorrectionFolder extends IUIContainer {
    /**
     * @return 処理結果
     */
    public IUIWork[] getWorks();
}
