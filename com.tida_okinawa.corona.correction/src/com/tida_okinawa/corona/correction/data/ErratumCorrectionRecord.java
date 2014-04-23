/**
 * @version $Id: ErratumCorrectionRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/06 9:45:34
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.data;

import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.correction.erratum.IllegalWordRecord;


/**
 * 誤記補正
 * 
 * @author kyohei-miyazato, imai
 */
public class ErratumCorrectionRecord extends ClaimWorkDataRecord {
    // TODO テスト用にUIから丸コピしてきたクラス（Javadocはこっちのほうがしっかりしている）
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
     * @param claimId
     *            誤記補正した問い合わせデータID
     * @param fieldId
     *            誤記補正したフィールドID
     * @param recordId
     *            誤記補正したレコードID
     * @param result
     *            誤記補正結果
     */
    public ErratumCorrectionRecord(int claimId, int fieldId, int recordId, String result) {
        super(claimId, fieldId, recordId, result);
        this.illegalWordList = NO_ILLEGAL;
    }


    /**
     * 誤記補正箇所あり
     * 
     * @param claimId
     *            誤記補正した問い合わせデータID
     * @param fieldId
     *            誤記補正したフィールドID
     * @param recordId
     *            誤記補正したレコードID
     * @param result
     *            誤記補正結果
     * @param illegalWordList
     *            修正しきれなかった誤記
     */
    public ErratumCorrectionRecord(int claimId, int fieldId, int recordId, String result, List<IllegalWordRecord> illegalWordList) {
        super(claimId, fieldId, recordId, result);
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
     * 誤記補正結果のテキストをセットする。
     * 手動で補正された結果をセットするために使う。
     * 
     * @param correctText
     *            正しいテキスト
     */
    public void setResult(String correctText) {
        result = correctText;
    }
}
