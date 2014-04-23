/**
 * @version $Id: Concatenate.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/02/17
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.util;


/**
 * 文字列連結のクラス
 * 
 * @author KMorishima
 * 
 */
public class Concatenate {
    /**
     * 文字列連結クラスをインスタンス化する。連結文字は {@value #DefaultSeparator}。
     * 
     * @param capacity
     *            このクラスで連結する要素数。後からの変更はできない
     */
    public Concatenate(int capacity) {
        elements = new Object[capacity];
    }


    /**
     * 使用する区切り文字列を指定してインスタンス化する
     * 
     * @param capacity
     *            このクラスで連結する要素数。後からの変更はできない
     * @param separator
     */
    public Concatenate(int capacity, String separator) {
        this(capacity);
        this.separator = separator;
    }

    private int index;
    private Object[] elements;


    /**
     * 連結対象の要素を追加する
     * 
     * @param element
     */
    public void add(Object element) {
        if (index < elements.length) {
            elements[index++] = element;
        }
    }


    /**
     * 追加された要素を{@link #getSeparator()}で区切って連結して返す
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (elements.length > 0) {
            String sepa = separator;
            StringBuilder ret = new StringBuilder(elements[0].toString());
            for (int i = 1; i < index; i++) {
                ret.append(sepa + elements[i]);
            }
            return ret.toString();
        }
        return "";
    }

    /**
     * {@link #setSeparator(String)}を呼び出さなかった場合の区切り文字列<br />
     * デフォルトでは、{@value #DefaultSeparator}が指定されている
     */
    public static final String DefaultSeparator = ", ";
    private String separator = DefaultSeparator;


    /**
     * @param separator
     *            要素を連結するときに、使用する区切り文字列
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }


    /**
     * @return 現在設定されている区切り文字列
     */
    public String getSeparator() {
        return separator;
    }


    /**
     * 渡された配列の各要素を、{@link #DefaultSeparator}で区切って連結して返す
     * 
     * @param ary
     * @return
     */
    public static String toString(Object[] ary) {
        return toString(ary, DefaultSeparator);
    }


    /**
     * 渡された配列の各要素を、指定されたseparatorで区切って連結して返す
     * 
     * @param ary
     * @param separator
     * @return
     */
    public static String toString(Object[] ary, String separator) {
        if (ary.length > 0) {
            String sepa = separator;
            StringBuilder ret = new StringBuilder(ary[0].toString());
            for (int i = 1; i < ary.length; i++) {
                ret.append(sepa + ary[i]);
            }
            return ret.toString();
        }
        return "";
    }
}
