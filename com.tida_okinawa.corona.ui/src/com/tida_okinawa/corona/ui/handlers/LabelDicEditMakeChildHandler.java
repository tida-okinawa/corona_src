/**
 * @version $Id: LabelDicEditMakeChildHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/08 19:01:40
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.ui.editors.LabelDicEditor;
import com.tida_okinawa.corona.ui.editors.LabelNameInputValidator;

/**
 * 
 * @author kyohei-miyazato
 */
public class LabelDicEditMakeChildHandler extends AbstractHandler {
    private String name;
    private IStructuredSelection selection;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        selection = (IStructuredSelection) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
        IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
        Shell shell = HandlerUtil.getActiveShell(event);
        /* 名前の設定をダイアログで行う */
        InputDialog dialog = new InputDialog(shell, "新規ラベル名入力", "ラベル名を入力してください。", "", new LabelNameInputValidator());
        /* ダイアログオープン */
        if (dialog.open() == Window.CANCEL) {/* CANCELボタンが押されたら何もしないで返る */
            return null;
        }

        name = dialog.getValue();

        if (part instanceof LabelDicEditor) {
            if (selection.getFirstElement() instanceof ILabel) {
                ((LabelDicEditor) part).add(name, (ILabel) selection.getFirstElement(), false);
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
