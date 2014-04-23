/**
 * @version $Id: IPatternDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/19 14:44:25
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;


/**
 * 構文パターン辞書インタフェース
 * 
 * @author shingo-takahashi
 */
public interface IPatternDic extends ICoronaDic {


    /**
     * 指定したアイテムIDの辞書アイテムの名前を取得する
     * 
     * @param id
     *            辞書アイテムID
     * @return 辞書アイテムの名前
     */
    public abstract String getItemName(int id);
}
