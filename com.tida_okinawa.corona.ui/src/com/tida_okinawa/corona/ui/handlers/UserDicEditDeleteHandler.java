/**
 * @version $Id: UserDicEditDeleteHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/12 14:54:54
 * @author takayuki-matsumoto
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

import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;
import com.tida_okinawa.corona.ui.editors.user.UserDicEditor;

/**
 * @author takayuki-matsumoto
 */
public class UserDicEditDeleteHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();

        if (part instanceof UserDicEditor) {
            UserDicEditor editor = (UserDicEditor) part;
            editor.remove();
        }
        return null;
    }

    private IStructuredSelection selection;


    @Override
    public boolean isEnabled() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        selection = (IStructuredSelection) window.getActivePage().getSelection();
        if ((selection == null) || (selection.size() == 0)) {
            return false;
        }

        IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
        if (part instanceof UserDicEditor) {
            UserDicEditor editor = (UserDicEditor) part;
            ICoronaDic dic = ((DicEditorInput) editor.getEditorInput()).getDictionary();
            if (dic instanceof IUserDic) {
                if (DicType.JUMAN.equals(((IUserDic) dic).getDicType())) {
                    /* Juman辞書は消さない */
                    return false;
                }
            }

            /* 最終行１つだけ選択していたらメニューは出さないけど、複数選択だったらメニューを出す（Editor側で最終行を消さないようにする） */
            if ((selection.size() == 1) && editor.isLastRow((ITerm) selection.getFirstElement())) {
                return false;
            }

            return true;
        }

        return false;
    }
}
