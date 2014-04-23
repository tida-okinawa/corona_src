/**
 * @version $Id: IPagingDataProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/21 17:36:44
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.List;

/**
 * @author kousuke-morishima
 */
public interface IPagingDataProvider {
    /**
     * ページサイズの指定。すべてのレコードを表示するときに指定する。
     */
    public static final int SHOW_ALL = 0;


    /**
     * 現在保持しているデータを返す。
     * <p>
     * refreshがtrueならば、データの表示件数を {@link #getPagingSize()}に調整する<br />
     * 削除して表示件数が減っていれば、後ろページからデータを引っ張ってくる。
     * </p>
     * 
     * @param refresh
     * @return 現在保持しているデータ（直前に {@link #next()}, {@link #prev()}で返したデータ）
     */
    List<Object> current(boolean refresh);


    /**
     * @return 次のデータ。{@link #getPagingSize()}より少ない場合は実データの数だけ返す
     */
    List<Object> next();


    /**
     * @return 前のデータ。{@link #getPagingSize()}より少ない場合は実データの数だけ返す
     */
    List<Object> prev();


    /**
     * @return 先頭から {@link #getPagingSize()}件のデータを返す
     */
    List<Object> first();


    /**
     * @return 末尾から {@link #getPagingSize()}件のデータを返す
     */
    List<Object> last();


    /**
     * @return 次のデータが1件以上あればtrue
     */
    boolean hasNext();


    /**
     * @return 前にデータが1件以上あればtrue
     */
    boolean hasPrev();


    /**
     * @return 現在のページサイズを取得する。全レコードを取得・表示するなら {@link #SHOW_ALL}。
     */
    int getPagingSize();


    /**
     * @return 現在の条件で取得できるデータの総件数を返す。
     */
    int totalCount();


    /**
     * @return {@link #current(boolean)}の先頭データが全体の何番目か(0 base)
     */
    int currentIndex();


    /**
     * @param data
     *            追加するデータ
     * @return 追加したデータ。追加されなかった場合はnull
     */
    Object addData(Object data);


    /**
     * @param data
     *            削除するデータ（現在表示されているデータに限る）
     */
    boolean removeData(Object data);
}
