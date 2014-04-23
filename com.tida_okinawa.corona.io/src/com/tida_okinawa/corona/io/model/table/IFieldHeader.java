/**
 * @version $Id: IFieldHeader.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 10:34:00
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.table;


/**
 * @author shingo-takahashi
 */
public interface IFieldHeader {

    /**
     * カラムIDを設定
     * 
     * @param id
     *            カラムID
     */
    public abstract void setId(int id);


    /**
     * カラムIDを取得
     * 
     * @return カラムID
     */
    public abstract int getId();


    /**
     * カラム名を設定
     * 
     * @param name
     *            カラム名
     */
    public abstract void setName(String name);


    /**
     * カラム名を取得
     * 
     * @return カラム名
     */
    public abstract String getName();


    /**
     * 表示する(初期値の)カラム名を設定
     * 
     * @param dispName
     *            表示する(初期値の)カラム名
     */
    public abstract void setDispName(String dispName);


    /**
     * 表示する(初期値の)カラム名を取得
     * 
     * @return 表示する(初期値の)カラム名
     */
    public abstract String getDispName();


    /**
     * カラムの型を設定
     * 
     * @param type
     *            カラムの型
     */
    public abstract void setType(String type);


    /**
     * カラムの型を取得
     * 
     * @return カラムの型
     */
    public abstract String getType();
}