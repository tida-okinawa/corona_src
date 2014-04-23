/**
 * @version $Id: ClaimWorkDataRecord.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/08/31 17:15:13
 * @author shingo_wakamatsu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.data;

/**
 * 中間データの処理結果
 * 
 * @author shingo_wakamatsu
 */
public class ClaimWorkDataRecord implements Comparable<ClaimWorkDataRecord> {
    // TODO テスト用にUIから丸コピしてきたクラス（Javadocはこっちのほうがしっかりしている）
    int claimID;
    int fieldID;
    int recordID;
    String result;


    /**
     * 中間データの処理結果取得
     * 
     * @param claimID
     *            クレームID
     * @param fieldID
     *            フィールドID
     * @param recordID
     *            レコードID
     * @param result
     *            処理結果テキスト
     */
    public ClaimWorkDataRecord(int claimID, int fieldID, int recordID, String result) {
        this.claimID = claimID;
        this.fieldID = fieldID;
        this.recordID = recordID;
        this.result = result;
    }


    /**
     * @return クレームID
     */
    public int getClaimId() {
        return claimID;
    }


    /**
     * @return フィールドID
     */
    public int getFieldId() {
        return fieldID;
    }


    /**
     * @return レコードID
     */
    public int getRecordId() {
        return recordID;
    }


    /**
     * @return　結果
     */
    public String getResult() {
        return result;
    }


    @Override
    public int compareTo(ClaimWorkDataRecord o) {
        if (o == null) {
            return -1;
        }
        if (claimID != o.claimID) {
            return claimID - o.claimID;
        }
        if (fieldID != o.fieldID) {
            return fieldID - o.fieldID;
        }
        return recordID - o.recordID;
    }
}
