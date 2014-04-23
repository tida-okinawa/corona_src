/**
 * @version $Id: PatternMatcher.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/29 21:30:11
 * @author imai
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.CleansingNameVariable;
import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.correction.parsing.ICoronaPatternParser;
import com.tida_okinawa.corona.correction.parsing.PatternParserFactory;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.data.MorphemeRecord;
import com.tida_okinawa.corona.ui.data.PatternMatcherRecord;

/**
 * 構文解析のコントローラクラス
 * 
 * @author imai
 */
public class PatternMatcher extends ClaimWorkDataController<MorphemeRecord, PatternMatcherRecord> {
    /**
     * 構文パターンの解析器
     */
    final ICoronaPatternParser parser;
    final List<ICoronaDic> dics;


    /**
     * ひとつのフィールドに対して構文解析する場合のコンストラクタ
     * 
     * @param product
     *            解析対象のターゲット
     * @param works
     *            問い合わせデータリスト
     * @param listener
     *            処理結果を受け取るリスナー
     * @param dics
     *            辞書リスト
     * @param hitFlag
     *            複数解析を行う場合true
     */
    public PatternMatcher(ICoronaProduct product, List<IClaimWorkData> works, IListener<PatternMatcherRecord> listener, List<ICoronaDic> dics, boolean hitFlag) {
        super(CleansingNameVariable.PATTERN_PARSING, product, works, ClaimWorkDataType.RESLUT_PATTERN, listener);
        ICoronaProject project = IoActivator.getService().getProject(product.getProjectId());
        parser = PatternParserFactory.getInstance().createPatternParser(project, product);
        parser.setMaltiHit(hitFlag);
        this.dics = dics;
    }


    /**
     * 複数フィールドに対して構文解析する場合のコンストラクタ
     * 
     * @param product
     *            ターゲット名
     * @param work
     *            解析元データ情報
     * @param listener
     *            処理結果を受け取るリスナー
     * @param dics
     *            解析に使用する辞書一覧
     * @param hitFlag
     *            複数解析を行う場合true
     */
    public PatternMatcher(ICoronaProduct product, IClaimWorkData work, IListener<PatternMatcherRecord> listener, List<ICoronaDic> dics, boolean hitFlag) {
        super(CleansingNameVariable.PATTERN_PARSING, product, work, ClaimWorkDataType.RESLUT_PATTERN, listener);
        ICoronaProject project = IoActivator.getService().getProject(product.getProjectId());
        parser = PatternParserFactory.getInstance().createPatternParser(project, product);
        parser.setMaltiHit(hitFlag);
        this.dics = dics;
    }


    @Override
    IListener<PatternMatcherRecord> createCommitter() {
        return new ClaimWorkPatternCommitter(product, typeR, dics);
    }

    static class ClaimWorkPatternCommitter extends ClaimWorkDataRecordCommitter<PatternMatcherRecord> {
        final List<ICoronaDic> dics;


        /**
         * @param product
         *            処理対象のターゲット
         * @param typeR
         *            出力データのタイプ
         * @param dics
         *            解析に使用する辞書一覧
         */
        ClaimWorkPatternCommitter(ICoronaProduct product, ClaimWorkDataType typeR, List<ICoronaDic> dics) {
            super(product, typeR);
            this.dics = dics;
        }


        @Override
        public void inputChanged(IClaimWorkData newWorkS) {
            int claimId = newWorkS.getClaimId();
            int fieldId = newWorkS.getFieldId();

            claimWorkData = product.getClaimWorkData(claimId, typeR, fieldId);
            if (claimWorkData == null) {
                /* 新しく作る */
                claimWorkData = IoActivator.getModelFactory().createClaimWorkPattern(claimId, fieldId, typeR, product.getProjectId(), product.getId());
                try {
                    product.addClaimWorkData(claimWorkData);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            /* メモリ上の履歴IDを更新 */
            claimWorkData.upgreadHistoryId();
            /* end でNoteを更新するために、値を入れておく */
            claimWorkData.setNote(newWorkS.getNote());

            /* 旧データのクリア */
            ((IClaimWorkPattern) claimWorkData).clearRelPattern();
            /* パターン辞書の更新 */
            List<IPatternDic> pdics = new ArrayList<IPatternDic>();
            for (ICoronaDic dic : dics) {
                if (dic instanceof IPatternDic) {
                    pdics.add((IPatternDic) dic);
                }
            }
            ((IClaimWorkPattern) claimWorkData).addPatternDic(pdics);
        }


        @Override
        public void receiveResult(PatternMatcherRecord result) {
            ((IClaimWorkPattern) claimWorkData).setClaimWorkPattern(result.getRecordId(), result.getResultPattern());
        }

    }


    @Override
    MorphemeRecord createRecordImpl(int claimId, int fieldId, int recordId, String text) {
        return new MorphemeRecord(claimId, fieldId, recordId, "", text);
    }


    @Override
    void startImpl(IProgressMonitor monitor) throws InterruptedException {
        String message = "";
        if (dics.size() > 0) {
            monitor.beginTask("準備", dics.size());
            monitor.setTaskName("辞書データ準備");
            for (ICoronaDic dic : dics) {
                monitor.subTask(dic.getName());
                parser.addDic(dic);
                monitor.worked(1);
            }

            monitor.done();
            if (parser.getPatternMapSize() == 0) {
                monitor.setCanceled(true);
                message = "解析するパターンがありません。";
                CoronaActivator.log(new Status(IStatus.INFO, UIActivator.PLUGIN_ID, message, null), true);
            }
        } else {
            monitor.setCanceled(true);
            message = "パターン辞書がありません";
            CoronaActivator.log(new Status(IStatus.INFO, UIActivator.PLUGIN_ID, message, null), true);
        }

        if (monitor.isCanceled()) {
            throw new InterruptedException(message);
        }
    };


    @Override
    PatternMatcherRecord execImpl(MorphemeRecord record) {
        IResultCoronaPattern result = parser.parsing(record.getResult());
        return new PatternMatcherRecord(record.getClaimId(), record.getFieldId(), record.getRecordId(), result);
    }
}
