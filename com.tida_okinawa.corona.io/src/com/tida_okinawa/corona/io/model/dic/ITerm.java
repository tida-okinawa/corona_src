/**
 * @version $Id: ITerm.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

/**
 * 用語アイテムオブジェクトのインタフェース
 * 
 * @author shingo-takahashi
 * 
 */
public interface ITerm extends IDicItem, Cloneable {

    /**
     * @return DB内でこの用語を識別するためのID
     */
    @Override
    public abstract int getId();


    /**
     * 見出し語を取得する
     * 
     * @return 見出し語文字列　非null(空文字)。
     */
    public abstract String getValue();


    /**
     * 見出し語を設定する
     * 
     * @param value
     *            見出し語文字列
     */
    public abstract void setValue(String value);


    /**
     * 品詞区分を取得する
     * 
     * @return 品詞区分列挙型定数。非null({@link TermPart#NONE})
     */
    public abstract TermPart getTermPart();


    /**
     * 品詞区分を設定する
     * 
     * @param termPart
     *            品詞区分列挙型定数
     */
    public abstract void setTermPart(TermPart termPart);


    /**
     * 品詞詳細区分を取得する
     * 
     * @return 品詞詳細区分列挙型定数。非null({@link TermClass#NONE})
     */
    public abstract TermClass getTermClass();


    /**
     * 品詞詳細区分を設定する
     * 
     * @param termClass
     *            品詞詳細区分列挙型定数
     */
    public abstract void setTermClass(TermClass termClass);


    /**
     * よみを取得する
     * 
     * @return よみ(ひらがな)文字列。非null(空文字)
     */
    public abstract String getReading();


    /**
     * よみを設定する
     * 
     * @param reading
     *            よみ文字列
     */
    public abstract void setReading(String reading);


    /**
     * 活用形を取得する
     * 
     * @return 活用形列挙型定数。非null({@link TermCForm#NONE})
     */
    public abstract TermCForm getCform();


    /**
     * 活用形を設定する
     * 
     * @param cform
     *            活用形列挙型定数
     */
    public abstract void setCform(TermCForm cform);


    @Override
    public abstract boolean isDirty();


    /**
     * この用語をJuman辞書形式にした文字列を取得する
     * 
     * @return この用語をJuman辞書形式にした文字列。非null(空文字)
     */
    public abstract String getJumanBase();


    /**
     * この用語をJuman辞書形式にした文字列を設定する
     * 
     * @param jumanBase
     *            用語をJuman辞書形式にした文字列
     */
    public abstract void setJumanBase(String jumanBase);


    /**
     * @return この用語を保持している辞書のID
     */
    @Override
    public abstract int getComprehensionDicId();


    /**
     * @return この用語を一意に識別するための文字列。見出し語、よみ、品詞をカンマで連結した文字列
     */
    String getKeyword();


    /**
     * この用語が不正な値を含んでいるか判定する。
     * 
     * @return 必要な情報がすべて正しく設定されていればfalse
     */
    public boolean isError();


    /**
     * このオブジェクトのコピーを作成する
     * 
     * @return コピーされたオブジェクト
     */
    public Object clone();
}
