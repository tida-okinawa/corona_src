/**
 * @version $Id: CoronaElementRenameAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/19 14:53:22
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.util.List;

import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * DataBaseViewでICoronaObjectをリネームするためのクラス
 * 
 * @author kousuke-morishima
 */
public class CoronaElementRenameAction extends SelectionDispatchAction {

    private RenameAction renameAction;


    /**
     * コンストラクター
     * 
     * @param site
     */
    public CoronaElementRenameAction(IWorkbenchSite site) {
        super(site);
        renameAction = new RenameAction(site);
    }


    @Override
    public void run(IStructuredSelection selection) {
        renameAction.run(getValidSelection(selection));
    }


    @Override
    public void selectionChanged(IStructuredSelection selection) {
        renameAction.selectionChanged(getValidSelection(selection));
    }


    private static IStructuredSelection getValidSelection(IStructuredSelection selection) {
        List<IUIElement> uiElements = null;
        Object element = selection.getFirstElement();
        if (element instanceof ICoronaProject) {
            uiElements = CoronaModel.INSTANCE.adapter((ICoronaProject) element);
        } else if (element instanceof ICoronaDic) {
            uiElements = CoronaModel.INSTANCE.adapter((ICoronaDic) element);
        } else {
        }

        if ((uiElements != null) && (uiElements.size() > 0)) {
            selection = new StructuredSelection(uiElements.get(0));
        } else {
            selection = new StructuredSelection();
        }
        return selection;
    }


    @Override
    public boolean isEnabled() {
        return renameAction.isEnabled();
    }
}
