/**
 * @version $Id: SynonymControllerForUI.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/04 19:30:04
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import java.util.List;

import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.ui.data.SynonymRecord;

/**
 * ゆらぎ・同義語補正のコントローラクラス（UI用）
 * 
 * @author kousuke-morishima
 * 
 */
public class SynonymControllerForUI extends SynonymController {
    final IUIProduct uiProduct;


    /**
     * @param uiProduct
     *            解析対象のターゲット
     * @param works
     *            解析対象データ
     * @param listener
     *            処理結果を受け取るリスナー
     * @param dics
     *            辞書一覧
     */
    public SynonymControllerForUI(IUIProduct uiProduct, List<IClaimWorkData> works, IListener<SynonymRecord> listener, List<ICoronaDic> dics) {
        super(uiProduct.getObject(), works, listener, dics);
        this.uiProduct = uiProduct;
    }


    /**
     * @param uiProduct
     *            解析対象のターゲット
     * @param uiWork
     *            解析対象データ
     * @param listener
     *            処理結果を受け取るリスナー
     * @param dics
     *            辞書一覧
     */
    public SynonymControllerForUI(IUIProduct uiProduct, IUIWork uiWork, IListener<SynonymRecord> listener, List<ICoronaDic> dics) {
        super(uiProduct.getObject(), uiWork.getObject(), listener, dics);
        this.uiProduct = uiProduct;
    }


    @Override
    IListener<SynonymRecord> createCommitter() {
        return new ClaimWorkDataRecordCommitterForUI<SynonymRecord>(uiProduct, typeR);
    }
}
