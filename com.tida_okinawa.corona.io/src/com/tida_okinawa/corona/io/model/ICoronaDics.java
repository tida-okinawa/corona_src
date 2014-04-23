/**
 * @version $Id: ICoronaDics.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.List;

import com.tida_okinawa.corona.io.model.dic.ICoronaDic;


/**
 * @author shingo-takahashi
 * 
 */
public interface ICoronaDics extends ICoronaComponent {

    /**
     * 辞書リスト取得
     * 毎回異なるListインスタンスを返す
     * 
     * @param cls
     * @return 辞書リスト
     */
    public abstract List<ICoronaDic> getDictionarys(Class<?> cls);


    /**
     * 辞書取得
     * 
     * @param id
     * @return 辞書情報
     */
    public abstract ICoronaDic getDictionary(int id);


    /**
     * 辞書追加<br>
     * 
     * @param dic
     *            辞書インターフェース
     * @return 辞書を新規追加したらtrue。すでに登録されているなど、追加しなかったときはfalse。
     */
    public abstract boolean addDictionary(ICoronaDic dic);


    /**
     * 辞書破棄<br>
     * 自身のオブジェクトと辞書のリンク関係を破棄する
     * 
     * @param id
     *            辞書ID
     */
    public abstract void removeDictionary(int id);


    /**
     * 辞書のエクスポート
     * 
     * @param outputPath
     *            出力先
     * @param encoding
     *            辞書の文字コード
     * @return 成否
     */
    public abstract boolean exportDictionarys(String outputPath, String encoding);
}
