/**
 * @version $Id: ErratumCorrectionRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/06 9:45:34
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.data;

import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.correction.erratum.IllegalWordRecord;


/**
 * 誤記補正
 * 
 * @author kyohei-miyazato, imai
 */
public class ErratumCorrectionRecord extends ClaimWorkDataRecord {
    /**
     * 誤記訂正箇所
     */
    List<IllegalWordRecord> illegalWordList;

    /**
     * 誤記訂正箇所なし
     */
    final static List<IllegalWordRecord> NO_ILLEGAL = new ArrayList<IllegalWordRecord>();


    /**
     * 誤記訂正箇所なし
     * 
     * @param claimID
     * @param fieldID
     * @param recordID
     * @param result
     */
    public ErratumCorrectionRecord(int claimID, int fieldID, int recordID, String result) {
        super(claimID, fieldID, recordID, result);
        this.illegalWordList = NO_ILLEGAL;
    }


    /**
     * 誤記補正箇所あり
     * 
     * @param claimID
     * @param fieldID
     * @param recordID
     * @param result
     * @param illegalWordList
     */
    public ErratumCorrectionRecord(int claimID, int fieldID, int recordID, String result, List<IllegalWordRecord> illegalWordList) {
        super(claimID, fieldID, recordID, result);
        this.illegalWordList = illegalWordList;
    }


    /**
     * 誤記補正箇所取得
     * 
     * @return illegalWordList 誤記補正箇所
     */
    public List<IllegalWordRecord> getIllegalWordList() {
        return illegalWordList;
    }


    /**
     * @param correctText
     */
    public void setResult(String correctText) {
        result = correctText;
    }
}
