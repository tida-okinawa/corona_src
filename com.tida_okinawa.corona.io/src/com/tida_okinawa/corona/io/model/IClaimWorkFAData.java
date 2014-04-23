/**
 * @version $Id: IClaimWorkFAData.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/06 10:28:48
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;


/**
 * 頻出用語用中間データインターフェース
 * 
 * @author shingo-takahashi
 */
public interface IClaimWorkFAData extends IClaimWorkData {

    /**
     * リストの初期化
     */
    public abstract void clear();
}
