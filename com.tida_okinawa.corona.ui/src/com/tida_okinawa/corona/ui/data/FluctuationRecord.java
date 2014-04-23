/**
 * @version $Id: FluctuationRecord.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/10/05 14:39:49
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.data;

/**
 * ゆらぎ補正
 * 
 * @author takayuki-matsumoto
 */
public class FluctuationRecord extends ClaimWorkDataRecord {

    /**
     * ゆらぎ補正対象レコード？
     * 
     * @param claimID
     * @param fieldID
     * @param recordID
     * @param result
     */
    public FluctuationRecord(int claimID, int fieldID, int recordID, String result) {
        super(claimID, fieldID, recordID, result);
    }
}
