/**
 * @version $Id: DeleteHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/30 17:31:51
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.tida_okinawa.corona.correction.parsing.model.ModificationElement;

/**
 * @author kousuke-morishima
 */
public class DeleteHandler extends AbstractHandler {
    PatternDicEditor editor = null;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        editor.performAction(ActionFactory.DELETE.getId());
        return null;
    }


    @Override
    public boolean isEnabled() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
            return false;
        }

        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart editor = page.getActiveEditor();
        if (editor instanceof PatternDicEditor) {
            this.editor = (PatternDicEditor) editor;
            IStructuredSelection ss = (IStructuredSelection) page.getSelection();
            return isEnabled(ss);
        }
        return false;
    }


    /**
     * @param selection
     * @return 削除できる要素のみ含まれていればtrue
     */
    public static boolean isEnabled(IStructuredSelection selection) {
        if (selection.size() > 0) {
            for (Object o : selection.toArray()) {
                if (o instanceof ModificationElement) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
