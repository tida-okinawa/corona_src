/**
 * @version $Id: TemplateAddHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/21 11:45:00
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.template;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.ui.editors.pattern.PatternDicEditor;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateAddHandler extends AbstractHandler {
    private PatternRecord selectedPattern = null;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IStructuredSelection ss = (IStructuredSelection) page.getSelection();
        Object object = ss.getFirstElement();
        if (object instanceof PatternRecord) {
            /* ダイアログ表示 */
            TemplateModifyDialog dialog = new TemplateModifyDialog(shell);
            dialog.setExist(false);
            dialog.setTreeContentProvider(new TemplateTreeContentProvider());
            dialog.setTreeLabelProvider(new TemplateTreeLabelProvider());
            dialog.setInput(selectedPattern);
            if (dialog.open() == Dialog.OK) {
                return null;
            }
        }
        return null;
    }


    @Override
    public boolean isEnabled() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
            return false;
        }
        /* パターンの先頭を選択している場合のみ有効（親を持たなければtrue） */
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart editor = page.getActiveEditor();
        if (editor instanceof PatternDicEditor) {
            IStructuredSelection ss = (IStructuredSelection) page.getSelection();
            Object object = ss.getFirstElement();
            if (object instanceof PatternRecord) {
                this.selectedPattern = (PatternRecord) object;
                return true;
            }
        }
        return false;
    }
}
