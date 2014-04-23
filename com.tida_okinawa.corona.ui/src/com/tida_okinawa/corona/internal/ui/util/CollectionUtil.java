/**
 * @version $Id: CollectionUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/29 11:56:52
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author kousuke-morishima
 */
public class CollectionUtil {

    /**
     * 一意な値のみで構成されたin1, in2の間で、追加・削除されたものをadded, deletedに格納する。
     * 
     * @param in1
     * @param in2
     * @param added
     *            out(in1になくて、in2にあるもの)
     * @param deleted
     *            out(in1にあって、in2にないもの)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? extends T>> void diff(Collection<T> in1, Collection<T> in2, Collection<T> added, Collection<T> deleted) {
        /* 追加分、削除分を求める */
        deleted.addAll(in1);
        added.addAll(in2);
        if (!deleted.isEmpty() && !added.isEmpty()) {
            for (Iterator<T> itr = deleted.iterator(); itr.hasNext();) {
                T o1 = itr.next();
                assert o1 != null;
                for (Iterator<T> itr2 = added.iterator(); itr2.hasNext();) {
                    T o2 = itr2.next();
                    if (((Comparable<T>) o1).compareTo(o2) == 0) {
                        itr.remove();
                        itr2.remove();
                        break;
                    }
                }
            }
        }
    }
}
