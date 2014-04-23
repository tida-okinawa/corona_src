/**
 * @version $Id: IClaimWorkPatternRecordMap.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/31 19:32:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.Map;

import com.tida_okinawa.corona.io.model.dic.IPattern;

/**
 * パターンマッチング結果インターフェース
 * 
 * @author kaori-jiroku
 * 
 */
public interface IClaimWorkPatternRecordMap extends Map<Integer, IResultCoronaPattern> {
    /**
     * 構文解析結果を追加
     * 
     * @param recordId
     *            レコードID
     * @param result
     *            構文解析結果
     */
    void add(int recordId, IResultCoronaPattern result);


    /**
     * マッチング結果を取得
     * 
     * @param recordId
     *            レコードID
     * @return マッチした構文パターン結果
     */
    IResultCoronaPattern getRecord(int recordId);


    /**
     * 後処理
     */
    void commit();


    /**
     * パターンの一覧
     * 
     * @return パターン一覧
     */
    public IPattern[] getPatterns();

}
