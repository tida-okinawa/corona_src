/**
 * @version $Id: IRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.table;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

/**
 * @author shingo-takahashi
 * 
 */
public interface IRecord extends IAdaptable {
    /**
     * レコードIDを取得
     * 
     * @return レコードID
     */
    public abstract int getRecordId();


    /**
     * カラムリストを取得
     * 
     * @return カラムリスト
     */
    public abstract List<IField> getFields();


    /**
     * カラムを取得
     * 
     * @param fieldId
     * @return カラム
     */
    public abstract IField getField(int fieldId);
}
