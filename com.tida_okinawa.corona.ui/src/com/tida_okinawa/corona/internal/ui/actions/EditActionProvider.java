/**
 * @version $Id: EditActionProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/29 15:59:28
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * @author kousuke-morishima
 */
public class EditActionProvider extends CommonActionProvider {

    private EditActionGroup editActionGroup;

    private boolean inViewPart = false;


    /** コンストラクター */
    public EditActionProvider() {
    }


    @Override
    public void init(ICommonActionExtensionSite site) {
        super.init(site);

        ICommonViewerWorkbenchSite workbenchSite = null;
        if (site.getViewSite() instanceof ICommonViewerWorkbenchSite)
            workbenchSite = (ICommonViewerWorkbenchSite) site.getViewSite();

        if (workbenchSite != null) {
            if (workbenchSite.getPart() instanceof IViewPart) {
                editActionGroup = new EditActionGroup(workbenchSite.getSite(), workbenchSite.getSelectionProvider());
                inViewPart = true;
            }
        }
    }


    @Override
    public void fillContextMenu(IMenuManager menu) {
        if (inViewPart) {
            editActionGroup.fillContextMenu(menu);
        }
    }


    @Override
    public void fillActionBars(IActionBars actionBars) {
        if (inViewPart) {
            editActionGroup.fillActionBars(actionBars);
        }
    }
}
