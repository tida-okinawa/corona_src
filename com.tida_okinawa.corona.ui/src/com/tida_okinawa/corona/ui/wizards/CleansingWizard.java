/**
 * @version $Id: CleansingWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/01 16:06:20
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.wizard.Wizard;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaDicPri;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author takayuki-matsumoto
 */
public class CleansingWizard extends Wizard {
    private static final String PageName1 = "com.tida_okinawa.corona.ui.wizards.ClensingWizardPage";

    private CleansingWizardPage page1;
    private List<IClaimWorkData> claimWorkDataList;
    private ClaimWorkDataType claimWorkDataType;
    private IUIProduct uiProduct;
    private HashMap<IClaimWorkData, List<ICoronaDicPri>> priMap;


    /**
     * @param cwdList
     *            処理結果の格納先(処理するフィールド分)
     * @param type
     *            これから行う処理種別
     * @param uiProduct
     *            処理対象のターゲット
     */
    public CleansingWizard(List<IClaimWorkData> cwdList, ClaimWorkDataType type, IUIProduct uiProduct) {
        Assert.isLegal(cwdList.size() > 0); // Assert
        this.claimWorkDataList = cwdList;
        this.claimWorkDataType = type;
        this.uiProduct = uiProduct;
        setWindowTitle("クレンジング実行<" + uiProduct.getObject().getName() + ">");
        setDialogSettings(UIActivator.getDefault().getDialogSettings());
    }


    @Override
    public void addPages() {
        page1 = new CleansingWizardPage(PageName1, this.claimWorkDataType);
        page1.initializeDicPriorityListCommmon(this.uiProduct, this.claimWorkDataType, this.claimWorkDataList);
        page1.setClaimWorkData(this.claimWorkDataList);
        addPage(page1);
    }


    @Override
    public boolean performFinish() {
        this.priMap = page1.getDicPriorityMap();
        page1.finished();
        return true;
    }


    /* ****************************************
     * 辞書優先度リストgetter
     */
    /**
     * @return 共通の辞書優先度リスト
     */
    public List<ICoronaDicPri> getDicPriorityList() {
        return page1.getCommonDicPriorityList();
    }


    /**
     * フィールドごとの優先度リストの取得
     * 
     * @param cwd
     *            問い合わせ中間データ
     * @return フィールドごとの辞書優先度リスト
     */
    public List<ICoronaDicPri> getFieldDicPriorityList(IClaimWorkData cwd) {
        return priMap.get(cwd);
    }


    /**
     * @return フィールドごとに辞書優先度を設定しているならtrue
     */
    public boolean isFieldSelect() {
        if (page1 == null) {
            throw new NullPointerException();
        }
        return page1.isFieldSelected();
    }


    /**
     * メンテナンス用メソッド。辞書優先度と処理結果の紐づけを行う。<br/>
     * コンストラクタで受け取ったIClaimWorkDataは保存には使えないことがあるので、
     * 処理結果が入ったIClaimWorkDataで置き換える必要がある。
     * 
     * @param bfrCwd
     *            コンストラクタで渡したIClaimWorkDataのうちのひとつ
     * @param aftCwd
     *            bfrCwdを置き換える、処理結果が入ったIClaimWorkData
     */
    public void updateDicPriMap(IClaimWorkData bfrCwd, IClaimWorkData aftCwd) {
        // 最新のクレームワークデータで置き換える
        List<ICoronaDicPri> list = priMap.get(bfrCwd);
        priMap.remove(bfrCwd);
        priMap.put(aftCwd, list);
    }


    /**
     * 辞書優先度のリストを保存する。
     */
    public void saveDicPriList() {
        if (page1 == null) {
            throw new NullPointerException();
        }

        if (page1.isFieldSelected()) {
            // mapをまわして保存
            for (Entry<IClaimWorkData, List<ICoronaDicPri>> e : priMap.entrySet()) {
                /* 　プライオリティリスト設定　 */
                IClaimWorkData cwd = e.getKey();
                if (cwd != null) {
                    if (cwd.getWorkdataId() == 0) {
                        int claimId = cwd.getClaimId();
                        int fieldId = cwd.getFieldId();
                        cwd = uiProduct.getObject().getClaimWorkData(claimId, cwd.getClaimWorkDataType(), fieldId);
                        if (cwd.getLasted() == null) {
                            continue;
                        }
                    }
                    cwd.setDicPrioritys(e.getValue());
                } else {
                    CoronaActivator.debugLog("saveDicPriList() : IClaimWorkData is null.");
                }
            }
        } else {
            IClaimWorkData cwd = claimWorkDataList.get(0);
            if (cwd.getWorkdataId() == 0) {
                // ワークIDが0のときは処理済みのクレームワークデータを取得
                // Memo dstWorkを作成した時、ターゲットに追加してしまえば置き換えずに済むのでは？
                int claimId = cwd.getClaimId();
                int fieldId = cwd.getFieldId();
                cwd = uiProduct.getObject().getClaimWorkData(claimId, cwd.getClaimWorkDataType(), fieldId);
                if ((cwd == null) || cwd.getLasted() == null) {
                    return;
                }
            }
            /* 　プライオリティリスト設定　 */
            cwd.setDicPrioritysCom(page1.getCommonDicPriorityList());
        }
    }


    /**
     * test method
     * 
     * @param cwd
     *            問い合わせ中間データ
     * @param list
     *            辞書リスト
     */
    public static final void saveDictionaryTest(IClaimWorkData cwd, List<ICoronaDic> list) {
        List<ICoronaDicPri> pris = new ArrayList<ICoronaDicPri>();
        for (ICoronaDic dic : list) {
            ICoronaDicPri pri = cwd.createDicPriority(dic.getId());
            pri.setInActive(true);
            pris.add(pri);
        }
        // テスト用に辞書登録
        cwd.setDicPrioritys(pris);
        cwd.setDicPrioritysCom(pris);
    }


    /**
     * 複数Hitを行うかどうか
     * 
     * @return 複数Hitを行うならtrue
     */
    public boolean getHitSelectStatus() {
        return page1.isHitSelectStatus();
    }
}
