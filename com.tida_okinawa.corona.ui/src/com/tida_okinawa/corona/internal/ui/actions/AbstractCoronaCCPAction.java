/**
 * @version $Id: AbstractCoronaCCPAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/08 20:13:11
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public abstract class AbstractCoronaCCPAction extends BaseSelectionListenerAction {

    protected AbstractCoronaCCPAction(String text) {
        super(text);
    }


    @Override
    public abstract void run();

    protected IStructuredSelection selection = null;


    /**
     * 引数のSelectionのとき、このアクションが有効かどうかを返す
     * 
     * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    protected boolean updateSelection(IStructuredSelection selection) {
        this.selection = selection;
        return (selection != null) && !selection.isEmpty();
    }
}
