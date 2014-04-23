/**
 * @version $Id: ErratumController.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/08/31 18:31:32
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.correction.data.ErratumCheckRecord;
import com.tida_okinawa.corona.correction.data.ErratumCorrectionRecord;
import com.tida_okinawa.corona.correction.erratum.Erratum;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.io.model.table.ITextRecord;

/**
 * 
 * @author kyohei-miyazato
 */
public class ErratumController implements IRunnableWithProgress {
    // Memo UIからほぼ丸コピしてきたクラス
    // TODO いずれこちらで置き換える
    final IClaimData claimData;
    final IListener<ErratumCorrectionRecord> listener;
    final List<IFieldHeader> fields;


    /**
     * @param claimData
     *            誤記補正対象の問合せデータ
     * @param fields
     *            誤記補正対象のフィールド
     * @param listener
     *            処理結果を受け取るリスナー
     */
    public ErratumController(IClaimData claimData, List<IFieldHeader> fields, IListener<ErratumCorrectionRecord> listener) {
        this.claimData = claimData;
        this.listener = listener;
        this.fields = fields;
    }


    @Override
    public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
        long s = System.currentTimeMillis();
        int claimId = claimData.getId();
        for (IFieldHeader field : fields) {
            claimData.addCorrectionMistakesField(field.getId());
            AsyncController<ErratumCheckRecord, ErratumCorrectionRecord> controller = new Controller(claimId, field.getId());
            controller.run(monitor);
        }
        CoronaActivator.debugLog("誤記補正時間： " + (System.currentTimeMillis() - s) + "mSec"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    class Controller extends AsyncController<ErratumCheckRecord, ErratumCorrectionRecord> {
        final int fieldId;


        public Controller(final int claimId, final int fieldId) {
            super(ClaimWorkDataType.CORRECTION_MISTAKES + " claim#:" + claimId + ", field#:" + fieldId); //$NON-NLS-1$ //$NON-NLS-2$
            this.fieldId = fieldId;

            /* DBからクレームデータを取り出す */
            setProvider(new DataProvider(getThreadNum()));

            /* ユーザ指定のリスナを追加 */
            if (listener != null) {
                addListener(listener);
            }

            /* 誤記補正したデータをDBに保存するインスタンスを生成 */
            IClaimWorkData workData = claimData.getCorrectionMistakes(fieldId);
            if (workData == null) {
                workData = IoActivator.getModelFactory().createClaimWorkData(claimId, fieldId, ClaimWorkDataType.CORRECTION_MISTAKES, 0, 0);
                claimData.addCorrectionMistakes(workData);
            }
            addListener(new Committer(workData));
        }

        /* ********************
         * DataProvider
         */
        class DataProvider extends QueueDataProvider<ErratumCheckRecord> {
            DataProvider(int capacity) {
                super(capacity);
            }


            @Override
            public int total() {
                return claimData.getRecords().size();
            }


            @Override
            public void run(IProgressMonitor monitor) throws InterruptedException {
                /* 誤記補正対象データを取得 */
                /* 1件ずつ処理するように */
                List<IRecord> datas = claimData.getRecords();
                int claimId = claimData.getId();

                claimData.addCorrectionMistakesField(fieldId);
                for (IRecord records : datas) {
                    if (monitor.isCanceled()) { /* キャンセル押下処理 */
                        break;
                    }

                    ITextRecord text = claimData.getTextRecord(fieldId, records.getRecordId());
                    ErratumCheckRecord cwdRecord = new ErratumCheckRecord(claimId, fieldId, records.getRecordId(), text.getText());
                    put(cwdRecord);
                }
                end();
            }
        }


        /* ********************
         * Committer
         */
        class Committer implements IListener<ErratumCorrectionRecord> {
            private List<ErratumCorrectionRecord> illegalResultList = new ArrayList<ErratumCorrectionRecord>();
            private IClaimWorkData workData;


            public Committer(IClaimWorkData workData) {
                this.workData = workData;
            }


            @Override
            public void receiveResult(ErratumCorrectionRecord result) {
                if (result.getIllegalWordList().size() == 0) {
                    workData.addClaimWorkData(result.getRecordId(), result.getResult());
                } else {
                    illegalResultList.add(result);
                }
            }


            @Override
            public void end(IProgressMonitor monitor) {
                /* 補正対象が存在する場合 */
                if (illegalResultList.size() != 0) {
                    /* 手動で誤記補正 */
                    List<ErratumCorrectionRecord> commitResultList = manualIllegalCorrection(illegalResultList);
                    for (ErratumCorrectionRecord commitResult : commitResultList) {
                        workData.addClaimWorkData(commitResult.getRecordId(), commitResult.getResult());
                    }
                }
                workData.setNote(ClaimWorkDataType.CORRECTION_MISTAKES.getName());
                workData.commit(monitor);
            }


            @Override
            public void inputChanged(IClaimWorkData newWorkS) {
                /* 毎回 new するので、使用しない */
            }
        };


        /**
         * @param illegalResultList
         *            自動補正できなかった語の一覧
         * @return 手動誤記補正してもらった結果
         */
        List<ErratumCorrectionRecord> manualIllegalCorrection(final List<ErratumCorrectionRecord> illegalResultList) {
            ///* 手動誤記補正結果のリターン用 */
            //final List<ErratumCorrectionRecord> correctResultList = new ArrayList<ErratumCorrectionRecord>();
            ///* 非UIスレッドからUIに関わる操作を行う場合は、実行しているスレッドからインナスレッドを呼び出す必要がある。SWTの制約。 */
            //Display.getDefault().syncExec(new Runnable() {
            //    @Override
            //    public void run() {
            //        /*
            //         * Memo ShellをProgressMonitorDialogからもらってきている。もし動かなくなった時のためのメモ
            //         */
            //        ErratumManualDialog dialog = new ErratumManualDialog(shell, illegalResultList);
            //        if (dialog.open() == Dialog.OK) {
            //            /* ダイアログでの補正結果を受け取る */
            //            correctResultList.addAll(dialog.getCorrectResultList());
            //        } else {
            //            /* キャンセルボタンが押されたら、誤記補正前のレコードをそのまま返す */
            //            /*
            //             * Memo Dialog内で変更されない保証がないので、dialog#
            //             * getOriginalIllegalLisとかしたほうが安心。
            //             */
            //            correctResultList.addAll(illegalResultList);
            //        }
            //    }
            //});
            //return correctResultList;
            return illegalResultList;
        }


        @Override
        ErratumCorrectionRecord exec(ErratumCheckRecord data) {
            int claimID = data.getClaimId();
            int fieldID = data.getFieldId();
            int recordID = data.getRecordId();
            String text = data.getResult();

            ErratumCorrectionRecord resRec;

            /* 補正対象がnullだと処理されないので、やらない。 */
            if (text != null) {
                Erratum erratum = new Erratum(); /* マルチスレッドで呼ばれるのでここで作っている */
                String correctedText = erratum.convert(text);
                resRec = new ErratumCorrectionRecord(claimID, fieldID, recordID, correctedText, erratum.getIllegalWordList());
            } else {
                System.err.println("text is null. claim:" + claimID + ", field:" + fieldID + ", record:" + recordID); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                resRec = new ErratumCorrectionRecord(claimID, fieldID, recordID, ""); //$NON-NLS-1$
            }
            return resRec;
        }


        @Override
        protected void start(IProgressMonitor monitor) throws InterruptedException {
            /* 何もしない */
        }


        @Override
        protected void end(IProgressMonitor monitor) {
            /* 何もしない */
        }
    }
}
