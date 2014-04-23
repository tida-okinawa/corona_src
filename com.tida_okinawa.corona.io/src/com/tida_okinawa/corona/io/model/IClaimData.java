/**
 * @version $Id: IClaimData.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.io.model.table.ITextRecord;


/**
 * クレームデータインターフェース<br>
 * 問い合わせデータ関連のデータの取り扱いを行う
 * 
 * @author shingo-takahashi
 * 
 */
public interface IClaimData extends ICoronaObject {

    /**
     * ID設定
     * 
     * @param id
     *            クレームデータID
     */
    public abstract void setId(int id);


    /**
     * ID取得
     * 
     * @return クレームデータID
     */
    public abstract int getId();


    /**
     * ファイル名設定
     * 
     * @param name
     *            ファイル名
     */
    public abstract void setFileName(String name);


    /**
     * ファイル名取得
     * 
     * @return ファイル名
     */
    public abstract String getFileName();


    /**
     * テーブル名設定
     * 
     * @param dbName
     *            テーブル名
     */
    public abstract void setTableName(String dbName);


    /**
     * テーブル名取得
     * 
     * @return テーブル名
     */
    public abstract String getTableName();


    /**
     * 名前取得
     * 
     * @return クレームデータ名
     */
    public abstract String getName();


    /**
     * IDフィールド設定
     * 
     * @param fieldId
     *            ID値
     */
    public abstract void setDispIdField(int fieldId);


    /**
     * IDフィールド取得
     * 
     * @return ID値
     */
    public abstract int getDispIdField();


    /**
     * ターゲットフィールド設定<br>
     * 問い合わせデータ中のフィールドで、対象ターゲットが定義されているフィールドを指定する。
     * 
     * @see #getProducts()
     * @param fieldNo
     *            フィールド番号
     */
    public abstract void setProductField(int fieldNo);


    /**
     * ターゲットフィールド取得
     * 
     * @return フィールド番号
     */
    public abstract int getProductField();


    /**
     * ターゲットフィールド破棄<br>
     * 問い合わせデータ中のターゲットフィールドの指定を破棄する。
     * この操作を行った場合、ターゲットとの対応関係が崩れるため、
     * 注意が必要。
     */
    public abstract void removeProductField();


    /**
     * ターゲットリスト取得<br>
     * 問い合わせデータ中のターゲットリストを取得する。<br>
     * 誤ったフィールドが指定されていた場合、正しい結果が取得されないため、<br>
     * 注意が必要。
     * 
     * @see #setProductField(int)
     * @return ターゲットリスト
     */
    public abstract List<String> getProducts();


    /**
     * 誤記補正フィールド追加<br>
     * 問い合わせデータ中の誤記補正対象のフィールドを設定する。<br>
     * 誤記補正対象のフィールドのみが、テキストマイニング対象となる。
     * 
     * @see com.tida_okinawa.corona.io.model.ICoronaProduct#addMiningField(int,
     *      int)
     * @param fieldNo
     *            フィールド番号
     */
    public abstract void addCorrectionMistakesField(int fieldNo);


    /**
     * 誤記補正フィールド破棄
     * 
     * @param fieldNo
     *            フィールド番号
     */
    public abstract void removeCorrectionMistakesField(int fieldNo);


    /**
     * 誤記補正フィールドリスト取得<br>
     * 誤記補正対象のフィールドリストを取得する。
     * 
     * @return 誤記補正フィールドリスト
     */
    public abstract Set<Integer> getCorrectionMistakesFields();


    /**
     * 誤記補正データ取得
     * 
     * @param fieldNo
     * @return 誤記補正データ取得成功でTrue
     */
    public abstract IClaimWorkData getCorrectionMistakes(int fieldNo);


    /**
     * 誤記補正データ追加
     * 
     * @param data
     * @return 処理完了でTrue、処理中断でFalse
     */
    public abstract boolean addCorrectionMistakes(IClaimWorkData data);


    /**
     * 誤記補正データ削除
     * 
     * @param data
     * @return 処理完了でTrue、処理中断でFalse
     */
    public abstract boolean removeCorrectionMistakes(IClaimWorkData data);


    /**
     * 問合せ情報の、指定されたフィールド情報を取得する
     * 
     * @param fieldId
     * @return フィールド情報
     */
    public abstract IFieldHeader getFieldInformation(int fieldId);


    /**
     * フィールド情報取得<br>
     * 問い合わせデータのフィールド情報を取得する。
     * 
     * @return　フィールドヘッダリスト
     */
    public abstract List<IFieldHeader> getFieldInformations();


    /**
     * レコードリスト取得<br>
     * 問い合わせ情報の原文を取得する
     * 
     * @return レコードリスト
     */
    public abstract List<IRecord> getRecords();


    /**
     * 
     * 指定されたレコードリストを取得<br>
     * 問い合わせ情報の原文を取得する
     * 
     * @param page
     * @return null
     */
    public abstract List<IRecord> getRecords(int page);


    /**
     * レコード取得<br>
     * 指定レコードを取得する
     * 
     * @param recordId
     *            レコードID
     * @return レコード
     */
    public abstract IRecord getRecord(int recordId);


    /**
     * 指定されたテキストレコードリスト取得
     * 
     * @param fieldNo
     * @return テキストレコードリスト
     */
    public abstract List<ITextRecord> getTextRecords(int fieldNo);


    /**
     * 
     * 指定されたテキストレコードリスト取得
     * 
     * @param fieldNo
     * @param page
     * @return null
     */
    public abstract List<ITextRecord> getTextRecords(int fieldNo, int page);


    /**
     * テキストレコードを取得
     * 
     * @param fieldNo
     * @param recordId
     * @return テキストレコード
     */
    public abstract ITextRecord getTextRecord(int fieldNo, int recordId);


    /**
     * 更新
     * 
     * @return 処理完了でTrue
     */
    public abstract boolean update();


    /**
     * コミット
     * 
     * @return 処理完了でTrue
     */
    public abstract boolean commit();


    /**
     * 更新日時取得
     * 
     * @return 最終更新日時
     */
    public abstract Date getLasted();
}
