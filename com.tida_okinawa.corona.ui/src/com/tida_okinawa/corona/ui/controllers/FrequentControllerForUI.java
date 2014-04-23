/**
 * @version $Id: FrequentControllerForUI.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2012/10/04 19:30:04
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.correction.frequent.FrequentRecord;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkFAData;
import com.tida_okinawa.corona.ui.data.ClaimWorkDataRecord;

/**
 * @author kousuke-morishima
 */
public class FrequentControllerForUI extends FrequentController {
    final IUIProduct uiProduct;


    /**
     * @param uiProduct
     *            解析対象のターゲット
     * @param works
     *            問い合わせデータリスト
     */
    public FrequentControllerForUI(IUIProduct uiProduct, List<IClaimWorkData> works) {
        super(uiProduct.getObject(), works);
        this.uiProduct = uiProduct;
    }


    /**
     * @param uiProduct
     *            解析対象のターゲット
     * @param uiWork
     *            問い合わせデータ
     */
    public FrequentControllerForUI(IUIProduct uiProduct, IUIWork uiWork) {
        super(uiProduct.getObject(), uiWork.getObject());
        this.uiProduct = uiProduct;
    }


    @Override
    IListener<ClaimWorkDataRecord> createCommitter() {

        return new ClaimWorkDataRecordCommitterForUI<ClaimWorkDataRecord>(uiProduct, typeR) {
            @Override
            public void end(IProgressMonitor monitor) {
                Collection<FrequentRecord> records = frequent_process.getRecords();
                int total = records.size() + 1;
                monitor.beginTask("Commit", total); //$NON-NLS-1$

                /* 前回データをクリアする */
                monitor.subTask("Clear"); //$NON-NLS-1$
                ((IClaimWorkFAData) claimWorkData).clear();
                monitor.worked(1);

                /* 今回データをコミット */
                int recId = 1; /* レコード番号 (クレームのレコードIDではない) */
                for (FrequentRecord record : records) {
                    //                    if (monitor.isCanceled()) {
                    //                        break;
                    //                    }
                    monitor.subTask(getMessage(recId + 1, total));
                    claimWorkData.addClaimWorkData(recId, record.toString());
                    recId++;
                    monitor.worked(1);
                }

                super.end(monitor);
                monitor.done();
            }


            private String getMessage(int current, int total) {
                return new StringBuffer(50).append(current).append("/").append(total).toString(); //$NON-NLS-1$
            }
        };
    }
}
