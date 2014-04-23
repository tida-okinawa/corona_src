/**
 * @version $Id: LabelDicEditRenameHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/08 19:05:40
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.ui.editors.LabelDicEditor;

/**
 * 
 * @author kyohei-miyazato
 */
public class LabelDicEditRenameHandler extends AbstractHandler {
    private IStructuredSelection selection = null;
    private LabelDicEditor editor = null;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        editor.rename();
        return null;
    }


    @Override
    public boolean isEnabled() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
            return false;
        }
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page.getActivePart() instanceof LabelDicEditor) {
            editor = (LabelDicEditor) page.getActivePart();
            selection = (IStructuredSelection) page.getSelection();
            if (selection != null && selection.getFirstElement() != null) {
                return (selection.getFirstElement() instanceof ILabel);
            }
        }
        return false;
    }
}
