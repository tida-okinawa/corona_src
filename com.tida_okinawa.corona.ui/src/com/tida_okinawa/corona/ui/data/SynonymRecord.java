/**
 * @version $Id: SynonymRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/12 01:36:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.data;

/**
 * 同義語補正結果のレコード
 * 
 * @author imai
 * 
 */
public class SynonymRecord extends MorphemeRecord {
    /**
     * @param claimID
     * @param fieldID
     * @param recordID
     * @param text
     *            解析対象のテキスト（入力テキスト）
     * @param result
     *            解析結果(DBに格納されているテキスト）
     */
    public SynonymRecord(int claimID, int fieldID, int recordID, String text, String result) {
        super(claimID, fieldID, recordID, text, result);
    }
}
