/**
 * @version $Id: ITextRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/26 16:39:56
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.table;

import com.tida_okinawa.corona.io.model.ITextItem;

/**
 * @author shingo-takahashi
 */
public interface ITextRecord extends ITextItem {
    /**
     * 問い合わせデータ固有のレコードIDを取得
     * 
     * @return dispId
     */
    public String getDispId();


    /**
     * 問い合わせデータ固有のレコードIDを設定
     * 
     * @param id
     */
    public void setDispId(String id);
}
