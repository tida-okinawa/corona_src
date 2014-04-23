/**
 * @version $Id: CheckExternalPluginBudleId.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/12/12 11:26:04
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.auto;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.correction.external.ExternalActivator;

/**
 * juman/KNPのバンドルIDを知るためのコマンドクラス
 * 
 * @author kousuke-morishima
 */
public class CheckExternalPluginBudleId implements IEditorActionDelegate {
    @Override
    public void run(IAction action) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        long bundleId = ExternalActivator.getDefault().getBundle().getBundleId();

        MessageDialog.openInformation(shell, Messages.CheckExternalPluginBudleId_labelCheckBundleID,
                Messages.bind(Messages.CheckExternalPluginBudleId_labelBundleID, new Object[] { bundleId }));
    }


    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }


    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    }

}
