/**
 * @version $Id: IllegalWordRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/06 11:11:09
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.erratum;

/**
 * 誤記文字格納用のフィールド
 * 
 * @author wataru-higa
 * @author kyohei-miyazato
 */
public class IllegalWordRecord {
    /**
     * 誤記単語の開始位置
     */
    int startId;


    /**
     * @return startId
     */
    public int getStartId() {
        return startId;
    }


    /**
     * @return illegalWord
     */
    public String getIllegalWord() {
        return illegalWord;
    }

    /**
     * 誤記文字列
     */
    String illegalWord;


}
