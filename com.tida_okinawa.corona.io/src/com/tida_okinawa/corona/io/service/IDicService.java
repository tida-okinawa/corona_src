/**
 * @version $Id: IDicService.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/23 17:56:32
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.service;

import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IDicName;

/**
 * @author kousuke-morishima
 */
public interface IDicService {

    /**
     * @param dicId
     * @return 辞書タイプ。辞書が特定できなかったらnull
     */
    public DicType getDicType(int dicId);


    /**
     * 指定されたIDを持つアイテムを探してくる
     * 
     * @param itemId
     *            アイテムID
     * @param type
     *            辞書種別
     * @return 指定されたIDを持つアイテム。なければnull
     */
    public IDicItem getItem(int itemId, DicType type);


    /**
     * 辞書名を取得
     * 
     * @param type
     *            辞書種別
     * @return 辞書名一覧
     */
    public IDicName[] getDicName(DicType type);
}
