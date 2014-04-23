/**
 * @version $Id: ITermCount.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/27 17:37:29
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.List;

/**
 * 頻出用語抽出 用語アイテムインタフェース
 * 
 * @author shingo-takahashi
 */
public interface ITermCount extends ITerm {

    /**
     * 用語の登場回数を取得する
     * 
     * @return 用語の登場回数
     */
    public int getCount();


    /**
     * 用語の登場回数を設定する
     * 
     * @param cnt
     *            用語の登場回数
     */
    public void setCount(int cnt);


    /**
     * 用語が登録されている辞書IDのListを取得する
     * 
     * @return この用語が登録されている辞書のIDのList
     */
    public List<Integer> getParentDicIds();


    /**
     * 用語が登録されている辞書IDのListを設定する
     * 
     * @param parents
     *            この用語が登録されている辞書のIDのList
     */
    public void setParentDicIds(List<Integer> parents);


    /**
     * @deprecated if call this method throw
     *             {@link UnsupportedOperationException}
     * @return このアイテムに変更があるかどうか。 変更有:true 変更なし:false
     */
    @Deprecated
    @Override
    public abstract boolean isDirty();

}
