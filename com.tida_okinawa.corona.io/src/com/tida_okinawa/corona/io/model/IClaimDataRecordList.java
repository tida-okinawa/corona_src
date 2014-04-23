/**
 * @version $Id: IClaimDataRecordList.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/28 11:34:44
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.List;

import com.tida_okinawa.corona.io.model.table.IRecord;

/**
 * クレームデータレコードリスト インターフェース<br>
 * 問い合わせデータレコードを取り扱う
 * 
 * @author shingo-takahashi
 */
public interface IClaimDataRecordList extends List<IRecord> {
    /**
     * 指定されたレコードを取得
     * 
     * @param recordId
     * @return　レコードリスト
     */
    IRecord getRecord(int recordId);
}
