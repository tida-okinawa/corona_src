/**
 * @version $Id: AbstractCCP.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/02 13:46:58
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;

/**
 * @author kousuke-morishima
 */
public abstract class AbstractCCP extends CCPUtil implements ISelectionChangedListener {

    protected AbstractCoronaCCPAction copyAction;
    protected AbstractCoronaCCPAction cutAction;
    protected AbstractCoronaCCPAction pasteAction;


    /**
     * @param name
     *            アクションラベル名
     * @return コピーアクションを作る。サポートしない場合はnullを返す。
     */
    protected abstract AbstractCoronaCCPAction createCopy(String name);


    /**
     * @param name
     *            アクションラベル名
     * @return カットアクションを作る。サポートしない場合はnullを返す。
     */
    protected abstract AbstractCoronaCCPAction createCut(String name);


    /**
     * @param name
     *            アクションラベル名
     * @return ペーストアクションを作る。サポートしない場合はnullを返す。
     */
    protected abstract AbstractCoronaCCPAction createPaste(String name);


    /**
     * @return コピーアクション。サポートしていない場合null
     */
    public Action getCopyAction() {
        if (copyAction == null) {
            copyAction = createCopy("コピー");
            if (copyAction != null) {
                initAction(copyAction, SWT.MOD1 | SWT.KEYCODE_BIT | 'C', null);
            }
        }
        return copyAction;
    }


    /**
     * @return カットアクション。サポートしていない場合null
     */
    public Action getCutAction() {
        if (cutAction == null) {
            cutAction = createCut("切り取り");
            if (cutAction != null) {
                initAction(cutAction, SWT.MOD1 | SWT.KEYCODE_BIT | 'X', null);
            }
        }
        return cutAction;
    }


    /**
     * @return ペーストアクション。サポートしていない場合null
     */
    public Action getPasteAction() {
        if (pasteAction == null) {
            pasteAction = createPaste("貼り付け");
            if (pasteAction != null) {
                initAction(pasteAction, SWT.MOD1 | SWT.KEYCODE_BIT | 'V', null);
            }
        }
        return pasteAction;
    }


    private static void initAction(AbstractCoronaCCPAction action, int keycode, ImageDescriptor image) {
        action.setAccelerator(keycode);
        action.setImageDescriptor(image);
        action.selectionChanged(new StructuredSelection());
    }


    public AbstractCCP() {
    }


    /* ****************************************
     * 選択変更
     */
    /**
     * 選択しているものが変わったことをCut, Copy, Pasteの各アクションに通知する。
     * 
     * @param selection
     */
    public void selectionChanged(IStructuredSelection selection) {
        if (copyAction != null) {
            copyAction.selectionChanged(selection);
        }
        if (cutAction != null) {
            cutAction.selectionChanged(selection);
        }
        if (pasteAction != null) {
            pasteAction.selectionChanged(selection);
        }
    }


    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        if (event.getSelection() instanceof IStructuredSelection) {
            selectionChanged((IStructuredSelection) event.getSelection());
        }
    }
}
