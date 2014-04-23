/**
 * @version $Id: IFilteringDataProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/16 10:10:09
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.Map;

/**
 * @author kousuke-morishima
 */
public interface IFilteringDataProvider extends IPagingDataProvider {

    /**
     * データ取得のソート指定を行う
     * sortFieldをnullにすると、ソートしない。
     * 
     * @param sortField
     *            ソート対象フィールド. may be null
     * @param asc
     *            ソート順。昇順ならtrue。
     */
    void setOrder(Object sortField, boolean asc);


    /**
     * @return ソート対象フィールド. may be null
     */
    Object getOrder();


    /**
     * @return ソート順。昇順ならtrue
     */
    boolean isAscending();


    /**
     * ソートを解除する
     */
    void clearOrder();


    /**
     * データ取得のフィルタ指定を行う。<br/>
     * filterFieldに指定された列をmatchValuesの値によってフィルタする。matchValuesが含まれる値のみ取得する
     * 
     * @param filterField
     *            フィルタする列
     * @param matchValues
     *            表示する値
     */
    void addFilter(Object filterField, Object[] matchValues);


    /**
     * @return 現在のフィルタ
     */
    Map<Object, Object[]> getFilters();


    /**
     * フィルタの一括指定
     * 
     * @param filter
     */
    void setFilter(Map<Object, Object[]> filter);


    /**
     * フィルタを解除する
     */
    void clearFilter();
}
