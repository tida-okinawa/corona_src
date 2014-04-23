/**
 * @version $Id: Encoding.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/23 11:11:11
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.common;

/**
 * 日本語テキストファイルの文字コード
 * 
 */
public enum Encoding {
    /**
     * UTF-8
     * UTF-8 は定義名にできないので、toString()をオーバーライドしている
     */
    UTF_8 {
        @Override
        public String toString() {
            return "UTF-8";
        }
    },

    /**
     * Shift_JIS
     */
    Shift_JIS,

    /**
     * MS932
     */
    MS932,
}
