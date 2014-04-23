/**
 * @version $Id: IObjectSyncListener.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/15 18:56:04
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;


/**
 * @author kousuke-morishima
 */
public interface IObjectSyncListener {

    /**
     * 監視対象のオブジェクトが変更されたことを通知する
     * 
     * @param event
     */
    void modified(ObjectSyncEvent event);

}
