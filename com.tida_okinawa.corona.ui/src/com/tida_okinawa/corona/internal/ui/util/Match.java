/**
 * @version $Id: Match.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/01/05
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正規表現のマッチを行うための便利クラス
 * 
 * @author KMorishima
 * 
 */
public class Match {
    /**
     * 整数値にマッチする
     */
    public static final String Integer_ptn = "^-?[0-9]+$";

    private Pattern pattern;
    private Matcher matcher;
    private String target;


    public Match(String regex) {
        pattern = Pattern.compile(regex);
    }


    /**
     * @param regex
     *            パターン
     * @param target
     *            検索対象の文字列
     */
    public Match(String regex, String target) {
        this.target = target;
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(target);
    }


    /**
     * 検索文字列を指定する
     * 
     * @param target
     */
    public void setTarget(String target) {
        this.target = target;
        matcher = pattern.matcher(target);
    }


    public String replaceAll(String replacement) {
        return matcher.replaceAll(replacement);
    }


    /**
     * @return 検索対象文字列にパターンが含まれていればtrue
     */
    public boolean find() {
        if (matcher != null) {
            return matcher.find();
        }
        return false;
    }


    /**
     * @param group
     * @return 指定されたgroupのマッチ文字列。パターンがマッチしないか、検索を実行していない場合null
     */
    public String group(int group) {
        if (matcher != null) {
            return matcher.group(group);
        }
        return null;
    }


    /**
     * 便利メソッド。<br />
     * {@link #find()}?{@link #group(int)}:null;
     * 
     * @return マッチしていれば、i番目のマッチ結果
     */
    public String match(int i) {
        if (find()) {
            return group(i);
        }
        return null;
    }


    public Matcher getMatcher() {
        return matcher;
    }


    @Override
    public String toString() {
        return "ptn=" + pattern.pattern() + ", tgt=" + target;
    }

    public static final String URL_REGEX = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%\\+&=]*)?";

}
