/**
 * @version $Id: IClaimWorkData.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/16 16:34:47
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.List;

import com.tida_okinawa.corona.io.model.table.ITextRecord;


/**
 * 問い合わせ中間データインターフェース<br>
 * 一意の中間データ毎で管理する。<br>
 * 元問い合わせデータ、マイニング対象フィールドID、中間クレームデータタイプ毎でそれぞれのデータを持つ。
 * 
 * @author shingo-takahashi
 */
public interface IClaimWorkData extends IClaimWorkComponent {
    /**
     * 問い合わせ中間データ追加
     * 
     * @param recordId
     *            レコードID
     * @param data
     *            データ
     * @return 追加成功ならTrue、失敗ならFalse
     */
    public abstract boolean addClaimWorkData(int recordId, String data);


    /**
     * 問い合わせ中間データ取得
     * 
     * @param recordId
     *            レコードID
     * @return レコードを取得できない場合、null。
     */
    public abstract String getClaimWorkData(int recordId);


    /**
     * 問い合わせ中間データ取得
     * 
     * @return 問い合わせ中間データ
     */
    public abstract List<ITextRecord> getClaimWorkDatas();


    /**
     * 問い合わせ中間データ取得
     * 
     * @param productName
     *            ターゲット名
     * @return　問い合わせ中間データ
     */
    public abstract List<ITextRecord> getClaimWorkDatas(String productName);


    /**
     * 問い合わせ中間データ取得
     * 
     * @param productName
     *            ターゲット名
     * @param page
     * @return 問い合わせ中間データ
     */
    public abstract List<ITextRecord> getClaimWorkDatas(String productName, int page);


    /**
     * 外部で誤記補正したデータを取り込んだかどうかを返す
     * 
     * @return 成否判定
     */
    public abstract boolean isExternalCorrectionMistakes();


    /**
     * 内部で誤記補正処理を行ったかどうかを返す
     * 
     * @return 成否判定
     */
    public abstract boolean isInternalCorrectionMistakes();


    /**
     * TODO 使わないメソッド
     * 誤記補正タイプをセット
     * 
     * @param type
     *            種別
     * @return 成功ならTrue、失敗ならFalse
     */
    public abstract boolean setCorrectionMistakesType(CorrectionMistakesType type);


    /**
     * 問い合わせデータ固有のテキスト情報を保持する
     * 
     * @param str
     *            WORKDATASのLINKに設定する文字列
     */
    public void setNote(String str);


    /**
     * 問い合わせデータ固有のテキスト情報を取得する
     * 
     * @return テキストデータ
     */
    public String getNote();


    /**
     * 問い合わせ中間データID取得
     * 
     * @return 問い合わせ中間データID
     */
    public abstract int getWorkdataId();


    /**
     * 問い合わせ中間データ取得
     * 
     * @param id
     *            作業ID
     * @param formerHistoryId
     *            クレンジング元履歴ID
     * @return レコードを取得できない場合、null。
     */
    public abstract String getClaimWorkData(int id, int formerHistoryId);


    /**
     * クレンジング元履歴IDを取得する
     * 誤記補正の場合、0が返る。
     * 誤記補正では、履歴IDの管理をしないので使用しない
     * 
     * @return クレンジング元履歴ID
     */
    public abstract int getFormerHistoryId();


    /**
     * メモリ上で保持している履歴IDを更新する
     * メモリ上の履歴IDをインクリメントすることで、最新の履歴と同期をとる
     */
    public abstract void upgreadHistoryId();


    /**
     * クレンジングの実行履歴を取得する
     * 
     * @return クレンジング実行履歴ID
     */
    public int getCleansingHistoryId();


    // testH25 20130801 
    /**
     * 古い結果を削除する
     */
    public void clearWorkData();

}