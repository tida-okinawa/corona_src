/**
 * @version $Id: ITextItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/17 13:45:11
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import org.eclipse.core.runtime.IAdaptable;


/**
 * IDと文字列だけの要素のインタフェース.
 * 
 * @author shingo-takahashi
 */
public interface ITextItem extends IAdaptable {

    /**
     * このアイテムのIDを返す
     * 
     * @return このアイテムのID
     */
    public abstract int getId();


    /**
     * テキストデータを設定する
     * 
     * @param text
     *            テキストデータ
     */
    public abstract void setText(String text);


    /**
     * @return not null
     */
    public abstract String getText();

}