/**
 * @version $Id: ListDataProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 21:30:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * リスト → {@link IDataProvider} スタブ用
 * 
 * @author imai
 * 
 * @param <T>
 */
@Deprecated
abstract public class ListDataProvider<T> implements IDataProvider<T> {
    final private List<T> list;
    Iterator<T> iterator;


    ListDataProvider() {
        list = new ArrayList<T>();
        iterator = null;
    }


    protected void put(T data) {
        list.add(data);
    }


    protected void end() {
        list.add(null);
    }


    @Override
    public T next() {
        if (iterator == null)
            iterator = list.iterator();
        return iterator.next();
    }
}
