/**
 * @version $Id: Pair.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/01/31
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.util;

/**
 * ペアを扱うクラス
 * 
 * @author KMorishima
 * 
 * @param <T1>
 *            value1
 * @param <T2>
 *            value2
 */
public class Pair<T1, T2> {
    /**
     * 初期値を与えてペアを作る
     * 
     * @param value1
     * @param value2
     */
    public Pair(T1 value1, T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }


    @Override
    public int hashCode() {
        return value1.hashCode() - value2.hashCode();
    }

    private T1 value1;


    /**
     * @return 1番目の値
     */
    public T1 getValue1() {
        return value1;
    }

    private T2 value2;


    /**
     * @return 2番目の値
     */
    public T2 getValue2() {
        return value2;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        Pair<?, ?> map2 = (Pair<?, ?>) obj;

        if (value1 != null) {
            if (!value1.equals(map2.value1)) {
                return false;
            }
        } else {
            if (map2.value1 != null) {
                return false;
            }
        }

        if (value2 != null) {
            if (!value2.equals(map2.value2)) {
                return false;
            }
        } else {
            if (map2.value2 != null) {
                return false;
            }
        }

        return true;
    }
}
