/**
 * @version $Id: LabelDicEditDeleteHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/08 19:07:20
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
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.ui.editors.LabelDicEditor;

/**
 * 
 * @author kyohei-miyazato
 */
public class LabelDicEditDeleteHandler extends AbstractHandler {
    private IStructuredSelection selection;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        /*
         * 削除処理
         * 右クリックで選択したlabelを取得 → remove()にドン！
         * 複数ラベル削除対応！！
         */
        selection = (IStructuredSelection) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
        IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
        if (part instanceof LabelDicEditor) {
            if (selection.getFirstElement() instanceof ILabel) {
                for (Object o : selection.toList()) {
                    ((LabelDicEditor) part).remove((ILabel) o);
                }
            }
        }

        return null;
    }


    @Override
    public boolean isEnabled() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
            return false;
        }
        selection = (IStructuredSelection) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
        if (selection != null && selection.getFirstElement() != null) {
            return true;
        }

        return false;
    }
}
