/**
 * @version $Id: WebEntryException.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/01 16:26:42
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry;

/**
 * WebEntryプラグインを外部から利用する場合に発生する例外
 * 
 * @author yukihiro-kinjo
 * 
 */
public class WebEntryException extends Exception {

    /**
     * 指定された詳細メッセージを使用して、新規例外を構築します。
     * 
     * @param message
     *            詳細メッセージ詳細。メッセージは、あとで {@link Throwable#getMessage()}
     *            メソッドで取得するために保存される
     */
    public WebEntryException(String message) {
        super(message);
    }

    /**
     * シリアライズID
     */
    private static final long serialVersionUID = -8805080761430761109L;

}
