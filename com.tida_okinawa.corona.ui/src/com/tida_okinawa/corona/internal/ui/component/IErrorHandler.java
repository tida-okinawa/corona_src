/**
 * @version $Id: IErrorHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/02/02
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.component;

/**
 * ハンドリングされるエラー項目は、各実装ごとに定義される。エラータイプはbitwiseが要求される
 * 
 * @author KMorishima
 * 
 */
public interface IErrorHandler {
    /**
     * ハンドリングしたエラーを返す
     * 
     * @return ハンドリングしたエラー
     * 
     * @see #setListeningError(int)
     * @see #addListeningError(int)
     */
    public int getError();


    /**
     * {@link #handling()}で保持するエラーを返す
     * 
     * @return 保持するエラー
     * 
     * @see #setListeningError(int)
     * @see #addListeningError(int)
     */
    public int getListeningError();


    /**
     * ハンドリングするエラーを設定する。以前の値は上書きされる。
     * 
     * @param errorTypes
     */
    public void setListeningError(int errorTypes);


    /**
     * ハンドリングするエラーに追加する。
     * 
     * @param errorTypes
     */
    public void addListeningError(int errorTypes);


    /**
     * リスナーの登録受け付ける
     * 
     * @param listener
     */
    public void addErrorListener(IErrorListener listener);
}
