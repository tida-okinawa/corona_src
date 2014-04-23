/**
 * @version $Id: IHitMorphemeHolder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/06 11:52:17
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * @author kousuke-morishima
 */
public interface IHitMorphemeHolder {

    /**
     * 構文解析でヒットしたとき、解析を行った文の先頭位置を記憶しておく。
     * Order, Sequence探索中の前方OR再探索で使用する。
     * 
     * @param morphemeElement
     *            構文解析でヒットした形態素解析要素の先頭
     */
    public void setTopMorpheme(MorphemeElement morphemeElement);


    /**
     * 構文解析でヒットした文の先頭要素を返す。
     * nullが返るときは、まだ検索を実行していないか、ヒットしていないことを示す。
     * 再探索が必要なとき、前回と同じ位置から検索を始めるために使用する。
     * 
     * @return 構文解析でヒットした形態素解析要素の先頭
     */
    public MorphemeElement getTopMorpheme();

}
