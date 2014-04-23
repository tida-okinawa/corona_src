/**
 * @version $Id: Cell.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/02/21
 * @author uehara
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.correction.common.table;

/**
 * CSV出力用テーブルのセルを表すインタフェースです。
 * 
 * @author uehara
 * 
 */
public interface Cell {
    /**
     * CSVにしたときの列数
     * 
     * @return 列数
     */
    int getColumnSize();


    /**
     * CSVにしたときの行数
     * 
     * @return 行数
     */
    int getRowSize();


    /**
     * CSV用にデータを格子状に展開
     * [行][列]
     * 
     * @return 展開した行列データ
     */
    String[][] expand();
}
