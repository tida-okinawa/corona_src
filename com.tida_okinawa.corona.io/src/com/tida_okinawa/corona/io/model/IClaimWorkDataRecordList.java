/**
 * @version $Id: IClaimWorkDataRecordList.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/28 11:34:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.List;

import com.tida_okinawa.corona.io.model.table.ITextRecord;

/**
 * 問い合わせ中間データレコードリストのインターフェース<br>
 * 問い合わせ中間データレコードを取り扱う
 * 
 * @author imai-yoshikazu
 */
public interface IClaimWorkDataRecordList extends List<ITextRecord> {
    /**
     * レコード追加
     * 
     * @param recordId
     *            レコードID
     * @param data
     */
    void add(int recordId, String data);


    /**
     * 問い合わせ中間データレコード取得
     * 
     * @param recordId
     *            レコードID
     * @return 問い合わせ中間データレコード
     */
    ITextRecord getRecord(int recordId);


    /**
     * コミット
     */
    void commit();


    /**
     * 問い合わせ中間データレコード取得
     * 
     * @param recordId
     *            レコードID
     * @param formerHisotryId
     *            クレンジング元履歴ID
     * @return 問い合わせ中間データレコード
     */
    ITextRecord getRecord(int recordId, int formerHisotryId);
}
