/**
 * @version $Id: RefactorActionGroup.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/06 16:26:40
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
public class RefactorActionGroup extends ActionGroup {
    private ISelectionProvider provider;
    private SelectionDispatchAction renameAction;


    public RefactorActionGroup(IWorkbenchSite site, ISelectionProvider provider) {
        this.provider = provider;

        renameAction = new RenameAction(site);

        initialize();
    }


    private void initialize() {
        ISelection selection = provider.getSelection();
        renameAction.update(selection);

        provider.addSelectionChangedListener(renameAction);
    }


    @Override
    public void fillContextMenu(IMenuManager menu) {
        menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, renameAction);
    }


    @Override
    public void fillActionBars(IActionBars actionBars) {
        actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), renameAction);
    }
}
