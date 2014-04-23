/**
 * @version $Id: DbViewRenameHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/13 9:37:23
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.views.db.DataBaseView;

/**
 * 
 * @author kyohei-miyazato
 */
public class DbViewRenameHandler extends AbstractHandler {
    private IWorkbenchWindow window = null;
    private IStructuredSelection selection = null;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // TODO 名前変更処理
        IWorkbenchPart part = window.getActivePage().getActivePart();
        DataBaseView dbView = (DataBaseView) part;
        dbView.rename(selection);

        return null;
    }


    @Override
    public boolean isEnabled() {
        window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        /*
         * 何も選択されていなければfalse
         * フォルダを含んで選択している場合はfalse
         * 複数選択している場合はfalse
         * 辞書が選択されている場合
         * ・juman辞書が選択されている場合はfalse
         * ・その他辞書の場合はtrue
         * プロジェクトを選択している場合true
         * 問い合わせデータを選択している場合false
         */
        selection = (IStructuredSelection) window.getActivePage().getSelection();
        if ((selection != null) && (selection.size() == 1) && (selection.getFirstElement() != null)) {
            for (Object item : selection.toArray()) {
                if (item instanceof IClaimData) {
                    return false;
                } else if (item instanceof ICoronaDic) {
                    if (item instanceof IUserDic) {
                        if (DicType.JUMAN.equals(((IUserDic) item).getDicType())) {
                            return false;
                        }
                    }
                } else if (item instanceof ICoronaProject) {
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
