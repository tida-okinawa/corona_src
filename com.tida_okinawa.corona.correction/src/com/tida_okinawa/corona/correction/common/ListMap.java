/**
 * @version $Id: ListMap.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/29 19:30:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * V -> List<K>
 * 
 * @author imai
 * 
 * @param <K>
 *            キー
 * @param <V>
 *            値
 */
public class ListMap<K, V> extends HashMap<K, List<V>> {
    private static final long serialVersionUID = -2853924813108945584L;


    /**
     * 要素を追加する
     * 
     * @param key
     *            キー
     * @param value
     *            値
     */
    public synchronized void add(K key, V value) {
        List<V> values = get(key);
        if (values == null) {
            values = new ArrayList<V>();
            put(key, values);
        }
        values.add(value);
    }


    /**
     * values() のListの全要素を返す
     * 
     * @return 全キーの全値
     */
    public Collection<V> valuesExpanded() {
        List<V> valuesExpanded = new ArrayList<V>();
        for (List<V> values : values()) {
            valuesExpanded.addAll(values);
        }
        return valuesExpanded;
    }
}
