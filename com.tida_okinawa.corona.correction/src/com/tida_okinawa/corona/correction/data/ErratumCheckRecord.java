/**
 * @version $Id: ErratumCheckRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/02 14:00:02
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.data;


/**
 * 誤記補正
 * 
 * @author kyohei-miyazato
 */
public class ErratumCheckRecord extends ClaimWorkDataRecord {
    // TODO テスト用にUIから丸コピしてきたクラス（Javadocはこっちのほうがしっかりしている）
    /**
     * 誤記補正対象レコード
     * 
     * @param claimID
     *            クレームID
     * @param fieldID
     *            フィールドID
     * @param recordID
     *            レコードID
     * @param text
     *            補正対象テキスト
     */
    public ErratumCheckRecord(int claimID, int fieldID, int recordID, String text) {
        super(claimID, fieldID, recordID, text);
    }
}
