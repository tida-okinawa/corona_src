/**
 * @version $Id: OpenActionProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/30 20:42:43
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import com.tida_okinawa.corona.internal.ui.views.model.IUIClaim;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.ui.TIDA;

/**
 * @author kousuke-morishima
 */
public class OpenActionProvider extends CommonActionProvider {

    /** コンストラクター */
    public OpenActionProvider() {
    }

    private MyOpenAction openAction;


    @Override
    public void init(ICommonActionExtensionSite aSite) {
        /* WorkbenchPartの中で実行されているか確認する */
        ICommonViewerSite site = aSite.getViewSite();
        if (site instanceof ICommonViewerWorkbenchSite) {
            ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) site;
            openAction = new MyOpenAction(workbenchSite.getSite());

            workbenchSite.getSelectionProvider().addSelectionChangedListener(openAction);
        }
        super.init(aSite);
    }


    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        if (openAction.isEnabled()) {
            menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, openAction);
        }
    }


    @Override
    public void fillActionBars(IActionBars actionBars) {
        if (openAction.isEnabled()) {
            actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
        }
    }

    private static class MyOpenAction extends SelectionDispatchAction {

        protected MyOpenAction(IWorkbenchSite site) {
            super(site);
            setText("開く(&O)");
        }


        @Override
        public void run() {
            IStructuredSelection selection = (IStructuredSelection) getSelection();
            if ((selection != null) && !selection.isEmpty()) {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                for (Iterator<?> itr = selection.iterator(); itr.hasNext();) {
                    Object element = itr.next();
                    if (element instanceof IUIElement) {
                        try {
                            TIDA.openEditor(page, (IUIElement) element, false);
                        } catch (PartInitException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


        @Override
        public String getToolTipText() {
            return "";
        }


        @Override
        public void selectionChanged(IStructuredSelection selection) {
            boolean enabled = true;
            if (selection.isEmpty()) {
                enabled = false;
            }
            for (Iterator<?> itr = selection.iterator(); itr.hasNext();) {
                Object element = itr.next();
                if (element instanceof IUIDictionary) {
                } else if (element instanceof IUIWork) {
                } else if (element instanceof IUIClaim) {
                } else {
                    enabled = false;
                    break;
                }
            }
            setEnabled(enabled);
        }

    }
}
