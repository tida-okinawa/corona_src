/**
 * @version $Id: DaoConnecter.java 33 2012-07-31 06:55:35Z kousuke-morishima $
 * 
 * 2011/10/28 11:34:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate.connector;

import java.util.Set;


/**
 * 
 * @author imai
 * 
 * @param <K>
 *            キー
 * @param <T>
 *            レコードデータ
 */
public interface DaoConnecter<K, T> {

    /**
     * recordIdを指定して取得する
     * 
     * @param key
     * @return may be null
     */
    T get(K key);


    /**
     * レコード数を取得する
     * 
     * @return レコード数
     */
    int size();


    /**
     * DBへ登録する
     * 
     * @param key
     *            キー
     * @param value
     *            レコードデータ
     */
    void commit(K key, T value);


    /**
     * キーの一覧
     * 
     * @return may be null
     */
    Set<K> getKeys();


    /**
     * データのクリア
     * 
     */
    void clear();


    /**
     * 後始末
     */
    void close();
}