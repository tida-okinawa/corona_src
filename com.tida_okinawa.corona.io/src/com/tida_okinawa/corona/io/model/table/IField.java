/**
 * @version $Id: IField.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/08 18:44:51
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.table;

/**
 * @author shingo-takahashi
 */
public interface IField {

    /**
     * ヘッダ情報を取得
     * 
     * @return ヘッダ情報
     */
    public abstract IFieldHeader getHeader();


    /**
     * ヘッダのIDを取得
     * 
     * @return ヘッダのID
     */
    public abstract int getId();


    /**
     * データを取得
     * 
     * @return データ
     */
    public abstract Object getValue();
}
