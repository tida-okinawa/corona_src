/**
 * @version $Id: ICoronaDicPri.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/13 11:09:07
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;


/**
 * @author shingo-takahashi
 */
public interface ICoronaDicPri {

    /**
     * @return 辞書ID
     */
    public abstract int getDicId();


    /**
     * @return 解析に使用しない場合はtrue
     */
    public abstract boolean isInActive();


    /**
     * @param inactive
     *            解析に使用しない場合はtrue
     */
    public abstract void setInActive(boolean inactive);


    /**
     * @return プライオリティ値
     */
    public abstract int getDicPri();


    /**
     * @param dicId
     *            辞書ID
     */
    public abstract void setDicId(int dicId);
}
