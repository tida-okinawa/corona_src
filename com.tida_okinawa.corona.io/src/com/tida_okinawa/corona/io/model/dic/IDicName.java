/**
 * @version $Id: IDicName.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/09 16:47:04
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

/**
 * @author s.takuro
 *         #177 パターン自動化（係り受け抽出）
 */
public interface IDicName {

    /**
     * 辞書IDを取得
     * 
     * @return 辞書ID
     */
    public int getDicId();


    /**
     * 辞書名の取得
     * 
     * @return 辞書名
     */
    public String getDicName();


    /**
     * この辞書アイテムが削除されているかのフラグを取得
     * 
     * @return true:削除 false:有効
     */
    public boolean isInActive();
}
