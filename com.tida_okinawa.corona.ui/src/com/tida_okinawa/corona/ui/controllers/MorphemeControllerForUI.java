/**
 * @version $Id: MorphemeControllerForUI.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
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
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.ui.ProjectPreference;
import com.tida_okinawa.corona.ui.data.MorphemeRecord;

/**
 * UI->形態素・係り受け解析処理
 * 
 * @author imai
 * 
 */
public class MorphemeControllerForUI extends MorphemeController {
    /**
     * プロダクト
     */
    final IUIProduct uiProduct;


    /**
     * ターゲットに紐づいている問合せデータ（誤記補正済み）に対して、形態素・係り受け解析を実行するインスタンスのコンストラクタ。
     * 
     * @param uiProduct
     *            形態素・係り受け解析に使用する辞書の最終更新日
     * @param listener
     *            処理結果を受け取るリスナー (EditorInput)
     */
    public MorphemeControllerForUI(IUIProduct uiProduct, IListener<MorphemeRecord> listener) {
        super(uiProduct.getObject(), doKnp(uiProduct), listener);
        this.uiProduct = uiProduct;
    }


    /**
     * @param uiProduct
     *            解析対象のターゲット
     * @param uiWork
     *            入力データ
     * @param listener
     *            処理結果を受け取るリスナー (EditorInput)
     */
    public MorphemeControllerForUI(IUIProduct uiProduct, IUIWork uiWork, IListener<MorphemeRecord> listener) {
        super(uiProduct.getObject(), uiWork.getObject(), doKnp(uiProduct), listener);
        this.uiProduct = uiProduct;
    }


    /**
     * @param uiProduct
     *            解析対象のターゲット
     * @param works
     *            入力データ
     * @param listener
     *            処理結果を受け取るリスナー (EditorInput)
     */
    public MorphemeControllerForUI(IUIProduct uiProduct, List<IClaimWorkData> works, IListener<MorphemeRecord> listener) {
        super(uiProduct.getObject(), works, doKnp(uiProduct), listener);
        this.uiProduct = uiProduct;
    }


    /**
     * 指定したターゲットを含むプロジェクトが、KNPを行う設定になっているかどうかを返す
     * 
     * @param uiProduct
     *            対象ターゲット
     * @return KNPを行うならtrue
     */
    private static final boolean doKnp(IUIProduct uiProduct) {
        return ProjectPreference.isDoKnp(uiProduct.getObject().getProjectId());
    }


    @Override
    IListener<MorphemeRecord> createCommitter() {
        //        return new ClaimWorkDataRecordCommitterForUI<MorphemeRecord>(uiProduct, typeR);
        return new ClaimWorkDataRecordCommitterForUI<MorphemeRecord>(uiProduct, typeR) {
            @Override
            public void end(IProgressMonitor monitor) {
                super.end(monitor);
                StringBuffer note = new StringBuffer(claimWorkData.getNote());
                note.append(":").append(doKnp); //$NON-NLS-1$

                /* DBでの文字数制限 */
                if (note.length() > 255) {
                    note.delete(0, note.length() - 255);
                }
                claimWorkData.setNote(note.toString());

                claimWorkData.commit(monitor);
            }
        };
    }
}
