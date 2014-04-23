/**
 * @version $Id: ClaimWorkDataRecordProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/16 09:27:04
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.table.ITextRecord;
import com.tida_okinawa.corona.ui.data.ClaimWorkDataRecord;

/**
 * DBから中間データを取り出す
 * 
 * @author imai
 * @param <T>
 *            データ
 * 
 */
abstract class ClaimWorkDataRecordProvider<T extends ClaimWorkDataRecord> extends QueueDataProvider<T> {
    final IClaimWorkData claimWorkData;


    ClaimWorkDataRecordProvider(ICoronaProduct product, int claimId, int fieldId, ClaimWorkDataType type) {
        this(product.getClaimWorkData(claimId, type, fieldId));
    }


    ClaimWorkDataRecordProvider(IClaimWorkData claimWorkData) {
        //	this(claimWorkData, Runtime.getRuntime().availableProcessors());
        this(claimWorkData, 1024); // サーバーに対応したのでスレッド数より増やす
    }


    ClaimWorkDataRecordProvider(IClaimWorkData claimWorkData, int capacity) {
        super(capacity);
        this.claimWorkData = claimWorkData;
        if (claimWorkData == null) {
            throw new IllegalArgumentException();
        }
    }


    @Override
    public int total() {
        return claimWorkData.getClaimWorkDatas().size();
    }


    @Override
    public void run(IProgressMonitor monitor) throws InterruptedException {
        for (ITextRecord record : claimWorkData.getClaimWorkDatas()) {
            if (monitor.isCanceled())
                return;
            int recordId = record.getId();
            String text = record.getText();
            put(createRecord(claimWorkData.getClaimId(), claimWorkData.getFieldId(), recordId, text));
        }
        end();
    }


    abstract T createRecord(int claimId, int filedId, int recordId, String text);
}
