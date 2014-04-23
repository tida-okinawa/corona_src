/**
 * @version $Id: RefactorActionProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/06 16:13:22
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
public class RefactorActionProvider extends CommonActionProvider {

    private RefactorActionGroup refactorActionGroup;
    private boolean inViewPart = false;


    /** コンストラクター */
    public RefactorActionProvider() {
    }


    @Override
    public void init(ICommonActionExtensionSite site) {
        super.init(site);

        if (site.getViewSite() instanceof ICommonViewerWorkbenchSite) {
            ICommonViewerWorkbenchSite workSite = (ICommonViewerWorkbenchSite) site.getViewSite();
            if (workSite != null) {
                refactorActionGroup = new RefactorActionGroup(workSite.getSite(), workSite.getSelectionProvider());
                inViewPart = (workSite.getPart() instanceof IViewPart);
            }
        }
    }


    @Override
    public void fillContextMenu(IMenuManager menu) {
        if (inViewPart) {
            refactorActionGroup.fillContextMenu(menu);
        }
    }


    @Override
    public void fillActionBars(IActionBars actionBars) {
        if (inViewPart) {
            refactorActionGroup.fillActionBars(actionBars);
        }
    }
}
