/**
 * @version $Id: LabelDicEditMakeParentHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/13 13:54:17
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.ui.editors.LabelDicEditor;
import com.tida_okinawa.corona.ui.editors.LabelNameInputValidator;

/**
 * 
 * @author kyohei-miyazato
 */
public class LabelDicEditMakeParentHandler extends AbstractHandler {
    private String name;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
        Shell shell = HandlerUtil.getActiveShell(event);
        /* 名前の設定をダイアログで行う */
        InputDialog dialog = new InputDialog(shell, "新規ラベル名入力", "ラベル名を入力してください。", "", new LabelNameInputValidator());
        /* ダイアログオープン */
        if (dialog.open() == Window.CANCEL) {/* CANCELボタンが押されたら何もしないで返る */
            return null;
        }

        name = dialog.getValue();

        ((LabelDicEditor) part).add(name, null, true);

        return null;
    }


    @Override
    public boolean isEnabled() {
        return true;
    }
}
