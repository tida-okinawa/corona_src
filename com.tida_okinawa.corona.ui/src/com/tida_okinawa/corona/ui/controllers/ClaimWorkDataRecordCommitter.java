/**
 * @version $Id: ClaimWorkDataRecordCommitter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/16 09:27:04
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.ui.data.ClaimWorkDataRecord;

/**
 * 中間データをDBに格納する.
 * 
 * @author imai
 * @param <TR>
 *            出力データ
 * 
 */
public class ClaimWorkDataRecordCommitter<TR extends ClaimWorkDataRecord> implements IListener<TR> {
    final ICoronaProduct product;
    final ClaimWorkDataType typeR;
    IClaimWorkData claimWorkData;


    /**
     * @param product
     *            処理対象のターゲット
     * @param typeR
     *            出力データのタイプ
     */
    ClaimWorkDataRecordCommitter(ICoronaProduct product, ClaimWorkDataType typeR) {
        this.product = product;
        this.typeR = typeR;
    }


    @Override
    public void inputChanged(IClaimWorkData newWorkS) {
        int claimId = newWorkS.getClaimId();
        int fieldId = newWorkS.getFieldId();

        claimWorkData = product.getClaimWorkData(claimId, typeR, fieldId);
        if (claimWorkData == null) {
            /* 新しく作る */
            claimWorkData = IoActivator.getModelFactory().createClaimWorkData(claimId, fieldId, typeR, product.getProjectId(), product.getId());
            try {
                product.addClaimWorkData(claimWorkData);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        /* メモリ上の履歴IDを更新 */
        claimWorkData.upgreadHistoryId();

        // testH25 20130801 
        /* 旧データのクリア */
        claimWorkData.clearWorkData();

        /* end でNoteを更新するために、値を入れておく */
        claimWorkData.setNote(newWorkS.getNote());
    }


    @Override
    synchronized public void receiveResult(TR record) {
        claimWorkData.addClaimWorkData(record.getRecordId(), record.getResult());
    }


    @Override
    public void end(IProgressMonitor monitor) {
        StringBuilder note = new StringBuilder(claimWorkData.getNote());
        if (note.length() > 0) {
            note.append(",");
        }

        note.append(typeR.getName());

        /* DBでの文字数制限 */
        if (note.length() > 255) {
            note.delete(0, note.length() - 255);
        }
        claimWorkData.setNote(note.toString());

        claimWorkData.commit(monitor);
    }
}
