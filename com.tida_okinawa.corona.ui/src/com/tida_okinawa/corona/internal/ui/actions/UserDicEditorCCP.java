/**
 * @version $Id: UserDicEditorCCP.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/08 20:13:11
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.ui.editors.user.UserDicEditor;


public class UserDicEditorCCP extends AbstractCCP {
    UserDicEditor editor;


    public UserDicEditorCCP(UserDicEditor editor) {
        this.editor = editor;
    }


    @Override
    protected AbstractCoronaCCPAction createCopy(String name) {
        return new AbstractCoronaCCPAction(name) {
            @Override
            public void run() {
                // TODO label copy
                // 空行を除去
                Object[] elements = selection.toArray();

                int length = elements.length;
                if (elements[length - 1] instanceof ITerm) {
                    if (editor.isLastRow((ITerm) elements[selection.size() - 1])) {
                        length--;
                    }
                }
                for (int i = 0; i < length; i++) {
                    elements[i] = ((ITerm) elements[i]).clone();
                }
                Object[] dst = null;
                if (length != elements.length) {
                    dst = new Object[length];
                    System.arraycopy(elements, 0, dst, 0, length);
                } else {
                    dst = elements;
                }

                setContents(new StructuredSelection(dst));

                /* #471(コピーしてから、選択を変更しないと貼り付けられない)への強引な対応 */
                AbstractCoronaCCPAction action = (AbstractCoronaCCPAction) getPasteAction();
                if (action != null) {
                    action.setEnabled(true);
                }
            }
        };
    }


    @Override
    protected AbstractCoronaCCPAction createCut(String name) {
        return null;
    }


    @Override
    protected AbstractCoronaCCPAction createPaste(String name) {
        return new AbstractCoronaCCPAction(name) {
            @Override
            public void run() {
                ISelection clip = getContents();
                if (clip instanceof IStructuredSelection) {
                    IStructuredSelection clipItem = (IStructuredSelection) clip;

                    /* ユーザ辞書への貼り付け */
                    Object[] src = clipItem.toArray();
                    List<ITerm> dst = new ArrayList<ITerm>(src.length);
                    for (Object o : src) {
                        if (o instanceof ITerm) {
                            ITerm term = (ITerm) ((ITerm) o).clone();
                            term.setDirty(true);
                            dst.add(term);
                        }
                    }
                    editor.add(dst);
                }
            }


            @Override
            protected boolean updateSelection(IStructuredSelection selection) {
                ISelection clip = getContents();
                if (clip == null) {
                    return false;
                }
                if (clip instanceof IStructuredSelection) {
                    IStructuredSelection clipItem = (IStructuredSelection) clip;
                    Object[] src = clipItem.toArray();
                    for (Object o : src) {
                        if (!(o instanceof ITerm)) {
                            return false;
                        }
                    }
                }
                return true;
            }

        };
    }

}
