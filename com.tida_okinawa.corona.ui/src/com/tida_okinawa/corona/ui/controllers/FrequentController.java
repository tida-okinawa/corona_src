/**
 * @version $Id: FrequentController.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/05 14:37:09
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import static com.tida_okinawa.corona.common.CleansingNameVariable.FREQUENT;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.correction.frequent.Frequent;
import com.tida_okinawa.corona.correction.frequent.FrequentRecord;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkFAData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.ui.data.ClaimWorkDataRecord;
import com.tida_okinawa.corona.ui.data.FluctuationRecord;

/**
 * @author takayuki-matsumoto
 */
public class FrequentController extends ClaimWorkDataController<FluctuationRecord, ClaimWorkDataRecord> {

    /**
     * @param product
     *            解析対象のターゲット
     * @param works
     *            問い合わせデータリスト
     */
    public FrequentController(ICoronaProduct product, List<IClaimWorkData> works) {
        super(FREQUENT, product, works, ClaimWorkDataType.FREQUENTLY_APPERING, null);
    }


    /**
     * @param product
     *            解析対象のターゲット
     * @param work
     *            問い合わせデータ
     */
    public FrequentController(ICoronaProduct product, IClaimWorkData work) {
        super(FREQUENT, product, work, ClaimWorkDataType.FREQUENTLY_APPERING, null);
    }

    Frequent frequent_process = new Frequent();


    @Override
    ClaimWorkDataRecord execImpl(FluctuationRecord record) {
        String text = record.getResult();
        frequent_process.count(text);
        /* クレームレコードごとに処理結果はないので、ダミー */
        return record;
    }


    @Override
    FluctuationRecord createRecordImpl(int claimId, int fieldId, int recordId, String text) {
        return new FluctuationRecord(claimId, fieldId, recordId, text);
    }


    @Override
    IListener<ClaimWorkDataRecord> createCommitter() {

        return new ClaimWorkDataRecordCommitter<ClaimWorkDataRecord>(product, ClaimWorkDataType.FREQUENTLY_APPERING) {
            @Override
            synchronized public void receiveResult(ClaimWorkDataRecord record) {
                /* なにもしない */
            }


            @Override
            public void end(IProgressMonitor monitor) {
                Collection<FrequentRecord> records = frequent_process.getRecords();
                int total = records.size() + 1;
                monitor.beginTask("Commit", total);

                /* 前回データをクリアする */
                monitor.subTask("Clear");
                ((IClaimWorkFAData) claimWorkData).clear();
                monitor.worked(1);

                /* 今回データをコミット */
                int recId = 1; /* レコード番号 (クレームのレコードIDではない) */
                for (FrequentRecord record : records) {
                    if (monitor.isCanceled()) {
                        break;
                    }
                    monitor.subTask(getMessage(recId + 1, total));
                    claimWorkData.addClaimWorkData(recId, record.toString());
                    recId++;
                    monitor.worked(1);
                }

                super.end(monitor);
                monitor.done();
            }


            private String getMessage(int current, int total) {
                return new StringBuffer(50).append(current).append("/").append(total).toString();
            }
        };
    }
}
