/**
 * @version $Id: IDicItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/19 13:19:47
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import com.tida_okinawa.corona.io.model.ICoronaObject;


/**
 * 辞書アイテムオブジェクトのインタフェース
 * 
 * @author shingo-takahashi
 */
public interface IDicItem extends ICoronaObject {

    /**
     * DBに保存される前につける辞書アイテムID
     */
    int UNSAVED_ID = 0;


    /**
     * DB上で管理される辞書アイテムIDを取得する
     * 
     * @return 辞書アイテムid。DB内で一意に識別される
     */
    public abstract int getId();


    /**
     * DB上で管理される辞書アイテムIDを設定する
     * 
     * @param id
     *            辞書アイテムID
     */
    public abstract void setId(int id);


    /**
     * この辞書アイテムを内包している辞書の辞書IDを取得する
     * 
     * @return 辞書ID
     */
    int getComprehensionDicId();


    /**
     * ダーティ(変更有フラグ)の取得
     * 
     * @return 自身に変更があればtrue
     */
    public abstract boolean isDirty();


    /**
     * ダーティ(変更有フラグ)の設定
     * 
     * @param dirty
     *            true:変更有 false:変更なし
     */
    public abstract void setDirty(boolean dirty);


    /**
     * この辞書アイテムが削除されているかのフラグを取得
     * 
     * @return true:削除 false:有効
     */
    public boolean isInActive();


}