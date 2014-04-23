/**
 * @version $Id: EditActionGroup.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/29 16:16:28
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * @author kousuke-morishima
 */
public class EditActionGroup extends ActionGroup {

    private SelectionDispatchAction deleteAction;


    public EditActionGroup(IWorkbenchSite site, ISelectionProvider provider) {

        deleteAction = new DeleteAction(site);

        ISelection selection = provider.getSelection();
        deleteAction.update(selection);

        /*
         * 選択が変わった時、Enableを切り替えるための登録
         */
        provider.addSelectionChangedListener(deleteAction);
    }


    @Override
    public void fillContextMenu(IMenuManager menu) {
        menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, deleteAction);
    }


    @Override
    public void fillActionBars(IActionBars actionBars) {
        actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
    }

}
