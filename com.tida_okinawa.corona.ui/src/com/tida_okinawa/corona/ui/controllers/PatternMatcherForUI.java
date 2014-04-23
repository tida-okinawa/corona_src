/**
 * @version $Id: PatternMatcherForUI.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2012/10/04 19:30:04
 * @author kousuke-morishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;


import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.ui.data.PatternMatcherRecord;

/**
 * 構文解析のコントローラクラス（UI用）
 * 
 * @author kousuke-morishima
 */
public class PatternMatcherForUI extends PatternMatcher {
    final IUIProduct uiProduct;


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
    public PatternMatcherForUI(IUIProduct product, List<IClaimWorkData> works, IListener<PatternMatcherRecord> listener, List<ICoronaDic> dics, boolean hitFlag) {
        super(product.getObject(), works, listener, dics, hitFlag);
        this.uiProduct = product;
    }


    /**
     * 複数フィールドに対して構文解析する場合のコンストラクタ
     * 
     * @param product
     *            ターゲット名
     * @param uiWork
     *            解析元データ情報
     * @param listener
     *            処理結果を受け取るリスナー
     * @param dics
     *            解析に使用する辞書一覧
     * @param hitFlag
     *            複数解析を行う場合true
     */
    public PatternMatcherForUI(IUIProduct product, IUIWork uiWork, IListener<PatternMatcherRecord> listener, List<ICoronaDic> dics, boolean hitFlag) {
        super(product.getObject(), uiWork.getObject(), listener, dics, hitFlag);
        this.uiProduct = product;
    }


    @Override
    IListener<PatternMatcherRecord> createCommitter() {
        return new ClaimWorkPatternCommitterForUI(uiProduct, typeR, dics);
    }

    static class ClaimWorkPatternCommitterForUI extends ClaimWorkPatternCommitter {
        final IUIProduct uiProduct;
        IUIWork uiWork;


        ClaimWorkPatternCommitterForUI(IUIProduct uiProduct, ClaimWorkDataType typeR, List<ICoronaDic> dics) {
            super(uiProduct.getObject(), typeR, dics);
            this.uiProduct = uiProduct;
        }


        @Override
        public void inputChanged(IClaimWorkData newWorkS) {
            super.inputChanged(newWorkS);
            /* UI表示アイテムも作る */
            uiWork = CoronaModel.INSTANCE.getWork(uiProduct, claimWorkData);
        }


        @Override
        public void end(IProgressMonitor monitor) {
            super.end(monitor);
            /* ファイル内容を更新 */
            uiWork.update(monitor);
        }
    }

}
