/**
 * @version $Id: ICoronaProduct.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * ターゲットインターフェース
 * 
 * @author shingo-takahashi
 * 
 */
public interface ICoronaProduct extends ICoronaDics {

    /**
     * プロジェクトIDを取得
     * 
     * @return プロジェクトID
     */
    public abstract int getProjectId();


    /**
     * ターゲットIDを取得
     * 
     * @return ターゲットID
     */
    public abstract int getId();


    /**
     * ターゲット名を取得
     * 
     * @return ターゲット名
     */
    public abstract String getName();


    /**
     * このターゲットと関連する問い合わせデータ一覧を返す。
     * 
     * @return 問い合わせデータ一覧
     */
    public abstract List<IClaimData> getClaimDatas();


    /**
     * このターゲットと関連する問い合わせ中間データ一覧を返す
     * 
     * @return 問い合わせ中間データ一覧
     */
    public abstract Set<IClaimWorkData> getClaimWorkDatas();


    /**
     * このターゲットと関連する問い合わせ中間データを返す
     * 
     * @param claimId
     * @param type
     * @param fieldNo
     * @return 問い合わせ中間データ
     */
    public abstract IClaimWorkData getClaimWorkData(int claimId, ClaimWorkDataType type, int fieldNo);


    /**
     * このターゲットに関連する問い合わせ中間データを追加する
     * 
     * @param data
     * @return 追加成功ならTrue、失敗ならFalse
     * @throws SQLException
     */
    public abstract boolean addClaimWorkData(IClaimWorkData data) throws SQLException;


    /**
     * 指定された中間データを削除する。DBからも、メモリからも削除される。<br/>
     * ただし、 {@link ClaimWorkDataType#CORRECTION_MISTAKES}の場合、DBからは削除しない
     * 
     * @param work
     * @return 正常に削除されればtrue
     */
    public abstract boolean removeClaimWorkData(IClaimWorkData work);


    /**
     * マイニングフィールドリスト取得<br>
     * 指定した問い合わせデータに対するマイニングフィールドリストを取得する
     * 
     * @param claimId
     * @return マイニングフィールド
     */
    public abstract Set<Integer> getMiningFields(int claimId);


    /**
     * マイニングフィールドリスト追加
     * 
     * @param claimId
     * @param fieldId
     */
    public abstract void addMiningField(int claimId, int fieldId);


    /**
     * 指定したフィールドを、マイニングフィールドから破棄する
     * 
     * @param claimId
     * @param fieldId
     */
    public abstract void removeMiningFeild(int claimId, int fieldId);


    /**
     * Memo 実装先でreturn nullになってる
     * クレームの日時取得？
     * 
     * @param claimId
     * @param type
     * @return null
     */
    public abstract Date getClaimLasted(int claimId, ClaimWorkDataType type);


    /**
     * 指定した辞書のリレーションを取得する
     * 
     * @param id
     * @param dicId
     * @return 取得完了でTrue
     */
    public abstract boolean getRelation(int id, int dicId);


    /**
     * プライオリティ削除
     * 
     * @param strIds
     * @param dicId
     * @return プライオリティ削除成功ならTure
     */
    public abstract boolean delDicPriority(String strIds, int dicId); /* プライオリティ削除 */


    /**
     * ターゲットに紐づくプロジェクトを取得
     * 
     * @param id
     * @return ターゲットに紐づくプロジェクト
     */
    public abstract Map<Integer, String> getWorkData(int id); /* ターゲットに紐づくプロジェクトを取得する */


    /**
     * プライオリティ存在チェック
     * 
     * @param id
     * @param dicId
     * @return 存在していればTrue
     */
    public abstract boolean getDicPriority(int id, int dicId); /* プライオリティ存在チェック */


    /**
     * プライオリティ追加
     * 
     * @param id
     * @param dicId
     */
    public abstract void addDicPriority(int id, int dicId);
}
