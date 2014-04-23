/**
 * @version $Id: IDepend.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/18 9:53:10
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.List;
import java.util.Map;

/**
 * 主従関係を持つ辞書の辞書アイテムインタフェース
 * 
 * @author shingo-takahashi
 */
public interface IDepend extends IDicItem {

    /**
     * 代表語 用語アイテムを取得
     * 
     * @return 代表語 用語アイテムオブジェクト
     */
    public abstract ITerm getMain();


    /**
     * 代表語 用語アイテムを設定する
     * 
     * @param main
     *            代表語 用語アイテムオブジェクト
     */
    public abstract void setMain(ITerm main);


    /**
     * 従属語 用語アイテムを取得する
     * 
     * @return 従属語 用語アイテムオブジェクトのList
     */
    public abstract List<ITerm> getSub();


    /**
     * 従属語 用語アイテムを設定する
     * 
     * @param sub
     *            従属語 用語アイテムのList
     */
    public abstract void setSub(List<ITerm> sub);


    /**
     * 従属語 用語アイテムを追加する
     * 
     * @param sub
     *            従属語 用語アイテムのオブジェクト
     */
    public abstract void addSub(ITerm sub);


    /**
     * 従属語 用語アイテムを複数取得する
     * 
     * @return 従属語 用語アイテムを格納したMap
     */
    public abstract Map<Integer, IDependSub> getSubs();


    /**
     * 従属語 用語アイテムを複数削除する
     * 
     * @param sub
     *            従属語 用語アイテムを格納したMap
     * @return 処理結果。成功した場合true
     */
    public abstract boolean removeSub(ITerm sub);
}
