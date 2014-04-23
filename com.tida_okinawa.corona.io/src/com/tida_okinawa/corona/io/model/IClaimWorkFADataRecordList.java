/**
 * @version $Id: IClaimWorkFADataRecordList.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/01 11:37:06
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.List;

import com.tida_okinawa.corona.io.model.dic.ITermCount;

/**
 * 頻出用語用中間データレコードインターフェース
 * 
 * @author shingo-takahashi
 */
public interface IClaimWorkFADataRecordList extends List<ITermCount> {


    @Override
    boolean add(ITermCount tcount);


    /**
     * コミット
     */
    void commit();


    /**
     * パターンの一覧を取得
     * 
     * @return パターン一覧
     */
    public List<ITermCount> getCounts();
}
