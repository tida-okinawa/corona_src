/**
 * @version $Id: IListener.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 21:30:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.controller;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.io.model.IClaimWorkData;

/**
 * 解析結果を受け取った時の処理と、解析が終わった時の処理を定義するためのインタフェース.
 * 処理結果を１件ずつ受け取る
 * 
 * @author imai
 * 
 * @param <T>
 *            処理結果
 */
public interface IListener<T> {
    // Memo UIから丸コピしてきたクラス
    // TODO いずれこちらに置き換える
    /**
     * 解析対象の {@link IClaimWorkData}が変更されるときに呼ばれる。
     * 
     * @param newWorkS
     *            変更後の入力データ
     */
    void inputChanged(IClaimWorkData newWorkS);


    /**
     * 処理結果通知（処理結果が１件ごと）に呼ばれる
     * 
     * @param result
     *            処理結果
     */
    void receiveResult(T result);


    /**
     * 処理終了（すべての処理結果を通知後）に呼ばれる
     * 
     * @param monitor
     *            進捗ダイアログ
     */
    void end(IProgressMonitor monitor);
}
