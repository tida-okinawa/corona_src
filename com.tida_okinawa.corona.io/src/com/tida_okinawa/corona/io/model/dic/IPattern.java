/**
 * @version $Id: IPattern.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/19 11:22:11
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;


/**
 * 構文パターン辞書アイテムのインタフェース
 * 
 * @author shingo-takahashi
 */
public interface IPattern extends IDicItem {

    /**
     * 構文パターンIDを取得
     * 
     * @return 構文パターン分類を取得
     */
    public abstract int getPatternType();


    /**
     * 構文パターンIDを設定
     * 
     * @param patternType
     *            {@link PatternType}のid
     */
    public abstract void setPatternType(int patternType);


    /**
     * 構文パターン名を取得
     * 
     * @return 構文パターン名
     */
    public abstract String getLabel();


    /**
     * 構文パターン名を設定
     * 
     * @param text
     *            構文パターン名
     */
    public abstract void setLabel(String text);


    /**
     * 構文パターンテキストを取得
     * 
     * @return 構文パターンテキスト
     */
    public abstract String getText();


    /**
     * 構文パターンテキストを設定
     * 
     * @param text
     *            構文パターンテキスト
     */
    public abstract void setText(String text);


    /**
     * この構文パターンアイテムが部品であるかを取得
     * 
     * @return 部品である場合true
     */
    public abstract boolean isParts();


    /**
     * この構文パターンアイテムが部品であるかを設定
     * 
     * @param parts
     *            true:部品 false:部品でない
     */
    public abstract void setParts(boolean parts);
}