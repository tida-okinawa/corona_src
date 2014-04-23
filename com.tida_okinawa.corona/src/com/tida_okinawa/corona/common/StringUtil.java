/**
 * @version $Id: StringUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/01 13:01:16
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.common;


/**
 * 文字列操作クラス
 * 
 * @author kousuke-morishima
 */
public class StringUtil {

    /**
     * target中のsearchWordの出現回数を求める<br/>
     * 探索文字列を削除した文字列と、元の文字列との差を使って求める<br />
     * <p>
     * 例．target "あいうえおあい"、searchWord　"あい"　->　(7 - 3) / 2 = 2
     * </p>
     * 
     * @param target
     *            検索対象の文字列
     * @param searchWord
     *            探す文字列
     * @return searchWordの出現回数
     */
    public static final int countStringInString(String target, String searchWord) {
        return (target.length() - target.replaceAll(searchWord, "").length()) / searchWord.length();
    }


    /**
     * nameから、ファイル名に使えない文字(/:\<>*?"|)を全角に置き換えて返す
     * 
     * @param name
     *            must not null
     * @return ファイル名に使えない文字を全角に置き換えた文字列
     */
    public static final String convertValidFileName(String name) {
        name = name.replace('/', '／').replace(':', '：').replace('\\', '￥').replace('<', '＜').replace('>', '＞');
        name = name.replace('*', '＊').replace('?', '？').replace('"', '”').replace('|', '｜');
        return name;
    }


    /**
     * validNameの「／：￥＜＞＊？”｜」を半角に戻す
     * 
     * @param validName
     *            must not null
     * @return 半角に戻った文字列
     */
    public static final String deconvertValidFaliName(String validName) {
        validName = validName.replace('／', '/').replace('：', ':').replace('￥', '\\').replace('＜', '<').replace('＞', '>');
        validName = validName.replace('＊', '*').replace('？', '?').replace('”', '"').replace('｜', '|');
        return validName;
    }
}
