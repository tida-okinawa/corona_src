/**
 * @version $Id: DbViewOpenHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 19:19:15
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.ui.TIDA;

/**
 * データベースビューで「開く」を選択した場合のハンドラー
 * 
 * @author kyohei-miyazato
 */
public class DbViewOpenHandler extends AbstractHandler {
    private IWorkbenchWindow window = null;
    private IStructuredSelection selection = null;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        /* 開く処理 */
        for (Iterator<?> itr = selection.iterator(); itr.hasNext();) {
            Object item = itr.next();
            if (window == null) {
                continue;
            }
            try {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    if (item instanceof IClaimData) {
                        /* 問い合わせデータを開く */
                        TIDA.openEditor(page, (IClaimData) item);
                    } else if (item instanceof ICoronaDic) {
                        /* 辞書を開く */
                        TIDA.openEditor(page, (ICoronaDic) item);
                    }
                }
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    @Override
    public boolean isEnabled() {
        window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        selection = (IStructuredSelection) window.getActivePage().getSelection();
        return isEnabled(selection);
    }


    /**
     * 項目の選択状況の確認
     * 
     * @param selection
     * @return 何も選択されていなければfalse
     *         単/複数選択時、辞書、問い合わせデータ以外を含んで選択している場合はfalse
     *         辞書、問い合わせデータを選択している場合true
     */
    public static boolean isEnabled(IStructuredSelection selection) {
        if (selection == null) {
            return false;
        }
        if (selection.size() == 0) {
            return false;
        }
        for (Object item : selection.toArray()) {
            if (!(item instanceof IClaimData) && !(item instanceof ICoronaDic)) {
                return false;
            }
        }
        return true;
    }
}
