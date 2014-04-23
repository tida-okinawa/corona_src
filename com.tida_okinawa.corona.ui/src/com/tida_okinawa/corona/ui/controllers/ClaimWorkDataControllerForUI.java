/**
 * @version $Id: ClaimWorkDataControllerForUI.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/04 19:30:04
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.ui.data.ClaimWorkDataRecord;


/**
 * 中間データの処理.
 * UIの処理を行うために拡張したクラス
 * 
 * @param <TS>
 *            入力データ
 * @param <TR>
 *            出力データ
 */
abstract public class ClaimWorkDataControllerForUI<TS extends ClaimWorkDataRecord, TR extends ClaimWorkDataRecord> extends ClaimWorkDataController<TS, TR> {
    /**
     * プロダクト
     */
    final IUIProduct uiProduct;


    /**
     * 
     * @param name
     *            処理名
     * @param uiProduct
     *            解析対象のターゲット
     * @param typeS
     *            入力データのタイプ
     * @param typeR
     *            出力データのタイプ
     * @param listener
     *            処理結果を受け取るリスナー
     */
    public ClaimWorkDataControllerForUI(String name, IUIProduct uiProduct, ClaimWorkDataType typeS, ClaimWorkDataType typeR, IListener<TR> listener) {
        super(name, uiProduct.getObject(), typeS, typeR, listener);
        this.uiProduct = uiProduct;

        if (inputWorks.isEmpty()) {
            MessageDialog.openInformation(new Shell(), "クレンジング実行", "入力元データがないため、処理を行いません");
        }
    }


    /**
     * 
     * @param name
     *            処理名
     * @param uiProduct
     *            解析対象のターゲット
     * @param works
     *            入力データ
     * @param typeR
     *            出力データのタイプ
     * @param listener
     *            処理結果を受け取るリスナー
     */
    public ClaimWorkDataControllerForUI(String name, IUIProduct uiProduct, List<IClaimWorkData> works, ClaimWorkDataType typeR, IListener<TR> listener) {
        super(name, uiProduct.getObject(), works, typeR, listener);
        this.uiProduct = uiProduct;
        if (inputWorks.size() > 0) {
            MessageDialog.openInformation(new Shell(), "クレンジング実行", "入力元データがないため、処理を行いません");
        }
    }


    /**
     * 
     * @param name
     *            処理名
     * @param uiProduct
     *            解析対象のターゲット
     * @param uiWork
     *            入力データ
     * @param typeR
     *            出力データのタイプ
     * @param listener
     *            処理結果を受け取るリスナー
     */
    public ClaimWorkDataControllerForUI(String name, IUIProduct uiProduct, IUIWork uiWork, ClaimWorkDataType typeR, IListener<TR> listener) {
        super(name, uiProduct.getObject(), uiWork.getObject(), typeR, listener);
        if (inputWorks.size() > 0) {
            MessageDialog.openInformation(new Shell(), "クレンジング実行", "入力元データがないため、処理を行いません");
        }
        this.uiProduct = uiProduct;
    }


    @Override
    IListener<TR> createCommitter() {
        return new ClaimWorkDataRecordCommitterForUI<TR>(uiProduct, typeR);
    }


}
