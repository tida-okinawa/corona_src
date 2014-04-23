/**
 * @version $Id: ClaimWorkDataRecordCommitterForUI.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/04 19:30:04
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.ui.data.ClaimWorkDataRecord;

/**
 * 中間データをDBに格納する
 * 
 * @author imai
 * @param <TR>
 *            出力データ
 * 
 */
public class ClaimWorkDataRecordCommitterForUI<TR extends ClaimWorkDataRecord> extends ClaimWorkDataRecordCommitter<TR> implements IListener<TR> {
    final IUIProduct uiProduct;
    IUIWork uiWork;


    /**
     * @param uiProduct
     *            処理対象のターゲット
     * @param typeR
     *            出力データのタイプ
     */
    ClaimWorkDataRecordCommitterForUI(IUIProduct uiProduct, ClaimWorkDataType typeR) {
        super(uiProduct.getObject(), typeR);
        this.uiProduct = uiProduct;
    }


    @Override
    public void inputChanged(IClaimWorkData workS) {
        super.inputChanged(workS);

        /* UI表示アイテムも作る */
        uiWork = CoronaModel.INSTANCE.getWork(uiProduct, claimWorkData);
    }


    @Override
    public void end(IProgressMonitor monitor) {
        super.end(monitor);
        uiWork.update(monitor);
    }
}
