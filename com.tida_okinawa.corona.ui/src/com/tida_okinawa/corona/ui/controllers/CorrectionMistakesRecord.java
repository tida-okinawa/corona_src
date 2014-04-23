/**
 * @version $Id: CorrectionMistakesRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/01 20:21:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import com.tida_okinawa.corona.ui.data.ClaimWorkDataRecord;

/**
 * 誤記補正済みデータ
 * 
 * @author imai
 * 
 */
public class CorrectionMistakesRecord extends ClaimWorkDataRecord {
    CorrectionMistakesRecord(int claimID, int fieldID, int recordID, String text) {
        super(claimID, fieldID, recordID, text);
    }
}
