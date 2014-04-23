/**
 * @version $Id: IClaimWorkComponent.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/02 20:59:44
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.Date;
import java.util.List;

import com.tida_okinawa.corona.io.exception.CoronaError;

/**
 * @author shingo-takahashi
 */
public interface IClaimWorkComponent extends ICoronaComponent {

    /**
     * フィールドIDを取得
     * 
     * @return フィールドID
     */
    public abstract int getFieldId();


    /**
     * クレームIDを取得
     * 
     * @return クレームID
     */
    public abstract int getClaimId();


    /**
     * 更新日を取得
     * 
     * @return 更新日
     */
    public abstract Date getLasted();


    /**
     * 問い合わせ中間データタイプ取得
     * 
     * @return　問い合わせ中間データタイプ
     */
    public abstract ClaimWorkDataType getClaimWorkDataType();


    /**
     * Memo トランザクション開始の処理が実装された場合に戻り値に変更があるかもしれない。
     * トランザクション開始
     * 
     * @return トランザクション開始に成功したら、true
     */
    public abstract boolean beginTransaction();


    /**
     * Memo トランザクション処理処理が実装された場合に戻り値に変更があるかもしれない。
     * トランザクション完了
     * 
     * @return トランザクション終了に成功したら、true
     */
    public abstract boolean commitTransaction();


    /**
     * Memo トランザクションロールバックの処理が実装された場合に戻り値に変更があるかもしれない。
     * トランザクションロールバック
     * 
     * @return トランザクションロールバック処理に成功したら、true
     */
    public abstract boolean rollbackTransaction();


    /**
     * エラーリストを取得
     * 
     * @return エラーリスト
     */
    public abstract List<CoronaError> getErrors();


    /**
     * プロジェクトID取得
     * 
     * @return プロジェクトID
     */
    public abstract int getProjectId();


    /**
     * ターゲットID取得
     * 
     * @return ターゲットID
     */
    public abstract int getProductId();


    /**
     * 辞書の優先度
     * 
     * @param dicId
     * @return 辞書の優先度オブジェクト
     */
    public abstract ICoronaDicPri createDicPriority(int dicId);


    /**
     * 辞書プライオリティリスト取得
     * 
     * プライオリティ順で辞書リストを取得する
     * 
     * @return 辞書プライオリティリスト
     */
    public abstract List<ICoronaDicPri> getDicPrioritys();


    /**
     * 辞書プライオリティリスト設定
     * 
     * @param list
     */
    public abstract void setDicPrioritys(List<ICoronaDicPri> list);


    /**
     * 辞書プライオリティ削除
     * 
     * @param dicId
     * @return 削除成功ならTrue、失敗ならFalse
     */
    public abstract boolean deleteDicPriority(int dicId);


    /**
     * 辞書プライオリティ追加
     * 
     * @param dicId
     * @return 辞書優先度
     */
    public abstract boolean addDicPriority(int dicId);


    /**
     * フィールド共通の辞書プライオリティリスト取得
     * 
     * プライオリティ順で辞書リストを取得する
     * ClaimWorkDataTypeが同じなら、どのフィールドのIClaimWorkDataで実行しても、同じ結果が返る。（
     * ターゲットが変われば変わる）
     * 
     * @return フィールド共通の辞書プライオリティリスト
     */
    public abstract List<ICoronaDicPri> getDicPrioritysCom();


    /**
     * 共通辞書プライオリティリスト設定
     * 
     * @param list
     */
    public abstract void setDicPrioritysCom(List<ICoronaDicPri> list);


    /**
     * 共通辞書プライオリティ削除
     * 
     * @param dicId
     * @return 削除成功ならTrue、失敗ならFalse
     */
    public abstract boolean deleteDicPriorityCom(int dicId);


    /**
     * 共通辞書プライオリティ追加
     * 
     * @param dicId
     * @return 追加成功ならTrue、失敗ならFalse
     */
    public abstract boolean addDicPriorityCom(int dicId);
}
