/**
 * @version $Id: StringUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/02 20:34:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 文字列操作
 * 
 * @author kousuke-morishima
 * 
 */
public class StringUtil {
    static final char NL_CODE = '\n';


    /**
     * List&lt;String&gt; を一つのStringにする
     * 
     * @param list
     *            連結対象
     * @return 連結後の文字列
     */
    public static String mergeStrings(List<String> list) {
        StringBuilder merged = new StringBuilder(list.size() * 16);
        for (String s : list) {
            merged.append(s).append(NL_CODE);
        }
        if (merged.length() >= 1) {
            /* 最後の改行をとる */
            return merged.substring(0, merged.length() - 1);
        } else {
            /* 空だった場合 */
            return merged.toString();
        }
    }


    /**
     * 要素で区切る
     * 
     * {@link String#split(String)} は遅いので
     * 
     * @param text
     *            分割対象文字列
     * @param delim
     *            区切り文字
     * @return 分割した文字列のリスト
     * 
     */
    public static List<String> splitFast(String text, char delim) {
        List<String> result = new ArrayList<String>();
        String s = text;
        while (!s.isEmpty()) {
            int pos = s.indexOf(delim);
            if (pos == -1) {
                result.add(s);
                break;
            } else {
                result.add(s.substring(0, pos));
            }
            s = s.substring(pos + 1);
        }
        return result;
    }


    /**
     * {@link #mergeStrings(List)} で結合したStringをListに戻す
     * 
     * @param s
     *            分割対象文字列
     * @return 分割後のリスト
     */
    public static List<String> splitFast(String s) {
        if (s != null) {
            List<String> list = splitFast(s, NL_CODE);
            return list;
        } else {
            return new ArrayList<String>(0);
        }
    }
}
