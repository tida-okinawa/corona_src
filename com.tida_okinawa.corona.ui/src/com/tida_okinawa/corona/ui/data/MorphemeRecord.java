/**
 * @version $Id: MorphemeRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/31 17:15:13
 * @author shingo_wakamatsu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.data;

import java.util.List;

import com.tida_okinawa.corona.correction.common.StringUtil;
import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;


/**
 * 中間データ: 形態素・係り受け解析の処理結果
 * 
 * @author shingo_wakamatsu
 */
public class MorphemeRecord extends ClaimWorkDataRecord {
    /**
     * 解析対象のテキスト（入力テキスト）
     */
    String text;


    /**
     * 形態素解析結果
     * 
     * @param claimID
     * @param fieldID
     * @param recordID
     * @param result
     *            解析結果のテキスト (DBに入った状態。複数行を\\nでつなげたもの）
     */
    public MorphemeRecord(int claimID, int fieldID, int recordID, String result) {
        /* text は解析結果から作る（ダミー） */
        super(claimID, fieldID, recordID, result);
        SyntaxStructure ss = new SyntaxStructure(result);
        this.text = ss.getText();
    }


    /**
     * 形態素解析結果
     * 
     * @param claimID
     * @param fieldID
     * @param recordID
     * @param text
     *            解析対象のテキスト（入力テキスト）
     * @param result
     *            解析結果のテキスト (DBに入った状態。複数行を\\nでつなげたもの）
     */
    public MorphemeRecord(int claimID, int fieldID, int recordID, String text, String result) {
        super(claimID, fieldID, recordID, result);
        this.text = text;
    }


    /**
     * 形態素解析結果
     * 
     * @param claimID
     * @param fieldID
     * @param recordID
     * @param text
     *            解析対象のテキスト（入力テキスト）
     * @param results
     *            解析結果のテキスト
     */
    public MorphemeRecord(int claimID, int fieldID, int recordID, String text, List<String> results) {
        super(claimID, fieldID, recordID, StringUtil.mergeStrings(results));
        this.text = text;
    }


    /**
     * 解析した文を取得
     * 
     * @return 解析した文
     */
    public String getText() {
        return text;
    }
}
