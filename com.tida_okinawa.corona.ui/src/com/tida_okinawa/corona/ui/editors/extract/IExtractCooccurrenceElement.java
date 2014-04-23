/**
 * @version $Id: IExtractCooccurrenceElement.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/22 18:00:59
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.util.List;

/**
 * @author s.takuro
 */
public interface IExtractCooccurrenceElement {
    /**
     * 単語の取得
     * 
     * @return 単語一覧
     */
    List<String> getTerms();


    /**
     * 単語の取得
     * 
     * @param pos
     *            取得する位置
     * @return 単語
     */
    String getTerm(int pos);


    /**
     * 共起語の出現頻度の取得
     * 
     * @return 出現数
     */
    String getCount();


    /**
     * 共起語のサイズ（文字数）
     * 
     * @return サイズ
     */
    int getSize();


    /**
     * パターンの種類を取得
     * 
     * @return パターンの種類
     */
    String getPatternType();


    /**
     * パターンの種類を設定
     * 
     * @param patternType
     *            パターンの種類
     */
    void setPatternType(String patternType);


    /**
     * パターン辞書に登録済みかどうかを取得する
     * 
     * @return true:登録済み、false:未登録
     */
    boolean getCompletion();


    /**
     * パターン辞書に登録済みかどうかを設定する
     * 
     * @param completion
     *            true:登録済み、false:未登録
     */
    void setCompletion(boolean completion);
}
