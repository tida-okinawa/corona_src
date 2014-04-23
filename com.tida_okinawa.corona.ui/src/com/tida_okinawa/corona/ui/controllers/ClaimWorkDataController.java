/**
 * @version $Id: ClaimWorkDataController.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/16 09:27:04
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.ui.PreferenceInitializer;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.data.ClaimWorkDataRecord;


/**
 * 中間データの処理
 * 
 * @param <TS>
 *            入力データ
 * @param <TR>
 *            出力データ
 */
abstract public class ClaimWorkDataController<TS extends ClaimWorkDataRecord, TR extends ClaimWorkDataRecord> implements IRunnableWithProgress {
    /**
     * 解析対象のターゲット
     */
    final ICoronaProduct product;
    /**
     * 処理対象のデータ
     */
    final Set<IClaimWorkData> inputWorks = new HashSet<IClaimWorkData>();

    /**
     * 結果のリスナー (EditorInput)
     */
    final IListener<TR> listener;

    final ClaimWorkDataType typeR;

    final String name;


    private ClaimWorkDataController(String name, ICoronaProduct product, ClaimWorkDataType typeR, IListener<TR> listener) {
        Assert.isLegal(product != null); // Assert
        this.name = name;
        this.product = product;
        this.typeR = typeR;
        this.listener = listener;
    }


    /**
     * @param name
     *            処理名
     * @param product
     *            解析対象のターゲット
     * @param typeS
     *            入力データのタイプ
     * @param typeR
     *            出力データのタイプ
     * @param listener
     *            処理結果を受け取るリスナー
     */
    public ClaimWorkDataController(String name, ICoronaProduct product, ClaimWorkDataType typeS, ClaimWorkDataType typeR, IListener<TR> listener) {
        this(name, product, typeR, listener);
        for (IClaimWorkData claimWorkData : product.getClaimWorkDatas()) {
            if (claimWorkData.getClaimWorkDataType() == typeS) {
                inputWorks.add(claimWorkData);
            }
        }
        if (inputWorks.isEmpty()) {
            CoronaActivator.log(new Status(IStatus.WARNING, UIActivator.PLUGIN_ID, "入力元データがないため、処理を行いません"), false);
        }
    }


    /**
     * 
     * @param name
     *            処理名
     * @param product
     *            解析対象のターゲット
     * @param works
     *            入力データ
     * @param typeR
     *            出力データのタイプ
     * @param listener
     *            処理結果を受け取るリスナー
     */
    public ClaimWorkDataController(String name, ICoronaProduct product, Collection<IClaimWorkData> works, ClaimWorkDataType typeR, IListener<TR> listener) {
        this(name, product, typeR, listener);
        if (works.isEmpty()) {
            CoronaActivator.log(new Status(IStatus.WARNING, UIActivator.PLUGIN_ID, "入力元データがないため、処理を行いません"), false);
        }
        inputWorks.addAll(works);
    }


    /**
     * 
     * @param name
     *            処理名
     * @param product
     *            解析対象のターゲット
     * @param workS
     *            入力データ
     * @param typeR
     *            出力データのタイプ
     * @param listener
     *            処理結果を受け取るリスナー
     */
    public ClaimWorkDataController(String name, ICoronaProduct product, IClaimWorkData workS, ClaimWorkDataType typeR, IListener<TR> listener) {
        this(name, product, typeR, listener);

        if (workS == null) {
            CoronaActivator.log(new Status(IStatus.WARNING, UIActivator.PLUGIN_ID, "入力元データがないため、処理を行いません"), false);
        }
        this.inputWorks.add(workS);
    }


    @Override
    public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
        IListener<TR> committer = createCommitter();
        for (IClaimWorkData claimWorkData : inputWorks) {
            run(claimWorkData, committer, monitor);
        }
    }


    /**
     * claimWorkData 1つ分(1 claim, 1 field) の処理を実行する
     * 
     * @param workS
     *            入力データ
     * @param committer
     *            解析結果をDBへ保存するためのリスナー
     * @param monitor
     *            プログレスモニター
     * @throws InterruptedException
     *             何らかの割り込みが発生した
     * @throws InvocationTargetException
     *             何らかのエラーが発生した
     */
    void run(IClaimWorkData workS, IListener<TR> committer, IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
        String title = name + " claim#:" + workS.getClaimId() + ", field#:" + workS.getFieldId();
        controller = createController(title);
        controller.setProvider(createProvider(workS));
        if (listener != null) {
            listener.inputChanged(workS);
            controller.addListener(listener);
        }
        committer.inputChanged(workS);
        controller.addListener(committer);
        controller.run(monitor);
    }


    protected Controller createController(String title) {
        if (UIActivator.getDefault() != null) {
            int nThread = UIActivator.getDefault().getPreferenceStore().getInt(PreferenceInitializer.PREF_NUM_THREADS);
            return new Controller(title, nThread);
        } else {
            return new Controller(title, Runtime.getRuntime().availableProcessors());
        }
    }

    protected Controller controller;


    /**
     * 入力データをDBから取り出す処理を作成
     * 
     * @param claimWorkData
     *            入力データ
     * @return 入力データのプロバイダ
     */
    IDataProvider<TS> createProvider(IClaimWorkData claimWorkData) {
        return new ClaimWorkDataRecordProvider<TS>(claimWorkData) {
            @Override
            TS createRecord(int claimId, int fieldId, int recordId, String text) {
                return createRecordImpl(claimId, fieldId, recordId, text);
            }
        };
    }


    /**
     * 出力データをDBへ格納する処理を作成
     * 
     * @return 処理結果を格納する機能を持ったクラス
     */
    IListener<TR> createCommitter() {
        return new ClaimWorkDataRecordCommitter<TR>(product, typeR);
    }


    class Controller extends AsyncController<TS, TR> {

        Controller(String title, int nThread) {
            super(title, nThread);
        }


        @Override
        protected void start(IProgressMonitor monitor) throws InterruptedException {
            startImpl(monitor);
        }


        @Override
        final TR exec(TS record) {
            TR result = execImpl(record);
            return result;
        }


        @Override
        protected void end(IProgressMonitor monitor) {
            endImpl(monitor);
        }
    }


    /**
     * DBの情報から入力データ execimpl()の引数 を作る
     * 
     * @param claimId
     *            解析対象の問い合わせデータID
     * @param fieldId
     *            解析対象のフィールドID
     * @param recordId
     *            解析対象のレコードID
     * @param text
     *            解析対象のテキスト
     * @return 処理に渡せる形式に変換された、解析処理の入力データ
     */
    abstract TS createRecordImpl(int claimId, int fieldId, int recordId, String text);


    /**
     * 前処理
     * 
     * @param monitor
     *            進捗ダイアログ
     * @throws InterruptedException
     *             何らかの割り込みが発生した
     */
    void startImpl(IProgressMonitor monitor) throws InterruptedException {
    }


    /**
     * 入力データを処理して出力データを作る
     * 
     * @param record
     *            １件の入力データ
     * @return 処理結果
     */
    abstract TR execImpl(TS record);


    /**
     * 後処理
     * 
     * @param monitor
     *            進捗ダイアログ
     */
    void endImpl(IProgressMonitor monitor) {
    }

}
