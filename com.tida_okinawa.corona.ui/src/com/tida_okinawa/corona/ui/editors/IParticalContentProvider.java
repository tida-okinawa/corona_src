/**
 * @version $Id: IParticalContentProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/21 15:22:05
 * @author yoshikazu-imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

public interface IParticalContentProvider {
    /**
     * 指定した範囲の要素を取得
     * ex. from=0, to=1000 -> [0]～[999]
     * 
     * @param from
     * @param to
     * @return 指定した範囲の要素を配列で返す
     */
    Object[] getElements(int from, int to);


    /**
     * 要素数を取得
     * 
     * @return 要素数
     */
    int getElementNumber();
}
