/**
 * @version $Id: IModelParser.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/05 10:03:49
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

/**
 * モデル(TS)とモデル(TD)を相互変換するインスタンスを提供する
 * 
 * @author kousuke-morishima
 */
public interface IModelParser<T1, T2> {

    public ModelEncoder<T1, T2> getEncoder();


    public ModelEncoder<T2, T1> getDecoder();

    public interface ModelEncoder<TS, TD> {
        /**
         * 与えられたelementを変換する
         * 
         * @param element
         * @return 変換する要素がない場合、null
         */
        public abstract TD encode(TS element);
    }

}
