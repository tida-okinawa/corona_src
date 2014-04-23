/**
 * @version $Id: CleansingCollocationHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/29 17:45:38
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.correction.CorrectionActivator;
import com.tida_okinawa.corona.correction.CorrectionPreferenceInitializer;
import com.tida_okinawa.corona.correction.collocation.CollocationExtract;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.table.ITextRecord;
import com.tida_okinawa.corona.ui.editors.collocation.CollocationTermEditor;
import com.tida_okinawa.corona.ui.editors.collocation.CollocationTermEditorInput;

/**
 * 連語抽出が実行された際に行う処理クラス
 * 
 * @author wataru-higa
 * 
 */
public class CleansingCollocationHandler extends AbstractCleansingHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        super.execute(event);
        ISelection selection = HandlerUtil.getActiveMenuSelection(event);
        List<String> misstakesList = new ArrayList<String>();

        // 選択されたターゲットを取得
        Object o = ((IStructuredSelection) selection).getFirstElement();
        IUIProduct product = getProduct(o);
        IClaimWorkData claimWorkData = getUIWork().getObject();

        // レコードをリストに追加
        List<ITextRecord> workDatas = claimWorkData.getClaimWorkDatas();
        for (ITextRecord workData : workDatas) {
            misstakesList.add(workData.getText());
        }
        IPreferenceStore store = CorrectionActivator.getDefault().getPreferenceStore();
        // プリファレンスより最低ヒット回数を取得
        String lowestHitNum = store.getString(CorrectionPreferenceInitializer.PREF_COLLOCATION_WORD);

        try {
            // TMT実行
            CollocationExtract collocationExtract = new CollocationExtract();
            List<String> tmtResult = collocationExtract.exec(misstakesList, lowestHitNum);
            if (tmtResult.size() > 0) {
                // Editorオープン
                CollocationTermEditorInput input = new CollocationTermEditorInput(product, "連語", claimWorkData, tmtResult); //$NON-NLS-1$
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                page.openEditor(input, CollocationTermEditor.EDITOR_ID);
            }
        } catch (PartInitException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected boolean isEnabled(ClaimWorkDataType type) {
        // 選択されたクレンジング結果が誤記補正以外はコンテキストメニューをグレーアウト
        switch (type) {
        case CORRECTION_MISTAKES:
            return true;
        default:
            return false;
        }
    }


    @Override
    ClaimWorkDataType getExecType() {
        return ClaimWorkDataType.COLLOCATION;
    }
}
