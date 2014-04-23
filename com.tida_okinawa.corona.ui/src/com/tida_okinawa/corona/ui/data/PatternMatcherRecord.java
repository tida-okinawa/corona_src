/**
 * @version $Id: PatternMatcherRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/16 09:27:04
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.data;

import com.tida_okinawa.corona.io.model.IResultCoronaPattern;

/**
 * 構文パターン解析
 * 
 * @author kyohei-miyazato
 */
public class PatternMatcherRecord extends ClaimWorkDataRecord {
    final IResultCoronaPattern resultPattern;


    /**
     * 構文パターン解析対象レコード
     * 
     * @param claimId
     * @param fieldId
     * @param recordId
     * @param resultPattern
     *            textに合致したパターン
     */
    public PatternMatcherRecord(int claimId, int fieldId, int recordId, IResultCoronaPattern resultPattern) {
        super(claimId, fieldId, recordId, "");
        this.resultPattern = resultPattern;
    }


    /**
     * 構文パターン解析対象レコード取得
     * 
     * @return resultPattern
     *         合致したパターン
     */
    public IResultCoronaPattern getResultPattern() {
        return resultPattern;
    }
}
