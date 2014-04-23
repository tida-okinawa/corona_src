/**
 * @versiogn $Id: IErrorListener.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/02/02
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.component;

/**
 * IErrorHandlerからの通知を受け取るリスナーインタフェース
 * 
 * @author KMorishima
 * 
 */
public interface IErrorListener {
    /**
     * エラーなし、または各種エラーが起きたとき、起きたエラー種別を知らさせる
     * Memo errorTypeをErrorEventに置き換える
     * 
     * @param errorType
     */
    public void errorOccurs(int errorType);
}
