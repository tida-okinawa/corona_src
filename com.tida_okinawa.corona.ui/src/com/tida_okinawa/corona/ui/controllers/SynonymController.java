/**
 * @version $Id: SynonymController.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/12 01:36:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.CleansingNameVariable;
import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.correction.synonym.SynonymCorrector;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.data.MorphemeRecord;
import com.tida_okinawa.corona.ui.data.SynonymRecord;

/**
 * ゆらぎ・同義語補正のコントローラクラス
 * 
 * @author kyohei-miyazato
 */
public class SynonymController extends ClaimWorkDataController<MorphemeRecord, SynonymRecord> {

    final SynonymCorrector corrector;
    List<ICoronaDic> dics;


    /**
     * @param product
     *            解析対象のターゲット
     * @param works
     *            解析対象データ
     * @param listener
     *            処理結果を受け取るリスナー
     * @param dics
     *            辞書一覧
     */
    public SynonymController(ICoronaProduct product, List<IClaimWorkData> works, IListener<SynonymRecord> listener, List<ICoronaDic> dics) {
        super(CleansingNameVariable.FLUC_SYNONYM, product, works, ClaimWorkDataType.CORRECTION_SYNONYM, listener);
        this.dics = dics;
        corrector = new SynonymCorrector(null);
    }


    /**
     * @param product
     *            解析対象のターゲット
     * @param work
     *            解析対象データ
     * @param listener
     *            処理結果を受け取るリスナー
     * @param dics
     *            辞書一覧
     */
    public SynonymController(ICoronaProduct product, IClaimWorkData work, IListener<SynonymRecord> listener, List<ICoronaDic> dics) {
        super(CleansingNameVariable.FLUC_SYNONYM, product, work, ClaimWorkDataType.CORRECTION_SYNONYM, listener);
        this.dics = dics;
        corrector = new SynonymCorrector(null);
    }


    @Override
    void startImpl(IProgressMonitor monitor) throws InterruptedException {
        if (dics.size() > 0) {
            corrector.init(dics, monitor);
            if (monitor.isCanceled()) {
                throwException("辞書に単語が登録されていないため、処理を中断します。");
            }
        } else {
            throwException("ゆらぎ・同義語辞書がないため、処理を中断します。");
        }
    }


    private static void throwException(String message) throws InterruptedException {
        CoronaActivator.log(new Status(IStatus.INFO, UIActivator.PLUGIN_ID, message, null), true);
        throw new InterruptedException(message);
    }


    @Override
    MorphemeRecord createRecordImpl(int claimId, int fieldId, int recordId, String text) {
        return new MorphemeRecord(claimId, fieldId, recordId, text);
    }


    @Override
    SynonymRecord execImpl(MorphemeRecord record) {
        SyntaxStructure ss = new SyntaxStructure(record.getResult());
        corrector.process(ss);
        return new SynonymRecord(record.getClaimId(), record.getFieldId(), record.getRecordId(), ss.getText(), ss.toString());
    }
}
