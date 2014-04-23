/**
 * @version $Id: IDependSub.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/18 15:10:26
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

/**
 * 主従関係を持つ辞書の従属語アイテムインタフェース
 * 
 * @author shingo-takahashi
 */
public interface IDependSub extends IDicItem {

    /**
     * 自身が属している代表語用語アイテムを取得する
     * 
     * @return 自身が属している代表語用語アイテム
     */
    public IDepend getParent();


    /**
     * 自身が属している代表語辞書アイテムを設定する
     * 
     * @param parent
     *            代表語用語アイテム
     */
    public void setParent(IDepend parent);


    /**
     * 従属語を取得する
     * 
     * @return 従属語用語アイテム
     */
    public abstract ITerm getTerm();


    /**
     * 従属語を設定する
     * 
     * @param term
     *            従属語用語アイテム
     */
    public abstract void setTerm(ITerm term);


    /**
     * 自動で補正をするかどうかを取得(今は使われていない)
     * 
     * @return 自動で補正するかどうか
     */
    public abstract int getLevel();


    /**
     * 自動で補正をするかどうかを設定
     * 
     * @param level
     *            設定値
     */
    public abstract void setLevel(int level);


    @Override
    public abstract String toString();
}
