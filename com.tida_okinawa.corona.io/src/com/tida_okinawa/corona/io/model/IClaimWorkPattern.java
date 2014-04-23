/**
 * @version $Id: IClaimWorkPattern.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/09/02 20:55:33
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.List;

import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;

/**
 * 構文解析結果用中間データインターフェース
 * 
 * @author shingo-takahashi
 * 
 */
public interface IClaimWorkPattern extends IClaimWorkData {
    /**
     * 構文解析処理結果をセットする
     * 
     * @param recordId
     *            レコードID
     * @param data
     * @return 処理成功ならTrue
     */
    public abstract boolean setClaimWorkPattern(int recordId, IResultCoronaPattern data);


    /**
     * 構文解析用中間データ取得
     * 
     * @return 問い合わせ中間データ
     */
    public abstract List<IResultCoronaPattern> getClaimWorkPatterns();


    /**
     * 構文解析用中間データの指定されたレコードを取得
     * 
     * @param recordId
     *            レコードID
     * @return 指定されたレコード
     */
    public abstract IResultCoronaPattern getClaimWorkPattern(int recordId);


    /**
     * 構文解析用中間データの指定されたレコードを取得
     * 
     * @param productName
     *            ターゲット名
     * @return 指定されたレコード
     */
    public abstract List<IResultCoronaPattern> getClaimWorkPatterns(String productName);


    /**
     * パターンにマッチしたレコードIDリスト
     * 
     * @param pattern
     *            パターン
     * @return パターンにマッチしたレコード
     */
    public abstract List<Integer> getRecord(IPattern pattern);


    /**
     * 古い結果を削除する
     */
    public void clearRelPattern();


    /**
     * 構文解析時にDBに存在していたパターン分類を返す。
     * 
     * @return 構文解析時にDBに存在していたパターン分類
     */
    public abstract PatternType[] getAllPatternTypes();


    /**
     * 構文解析時で使用した辞書が含んでいるパターン分類の一覧を返す
     * 
     * @return 構文解析時で使用した辞書が含んでいるパターン分類の一覧
     */
    public abstract PatternType[] getPatternTypes();


    /**
     * 構文解析時にDBに存在していたパターンを取得
     * 
     * @param id
     *            パターンID
     * @return 構文解析時にDBに存在していたパターン
     */
    public abstract IPattern getPattern(int id);


    /**
     * パターン辞書リストを取得
     * 
     * @return パターン辞書リスト
     */
    public abstract List<IPatternDic> getPatternDics();


    /**
     * パターン辞書追加
     * 
     * @param dics
     *            パターン辞書リスト
     */
    public abstract void addPatternDic(List<IPatternDic> dics);


}
