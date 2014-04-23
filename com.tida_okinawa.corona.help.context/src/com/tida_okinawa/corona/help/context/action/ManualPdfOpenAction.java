/**
 * @version $Id: ManualPdfOpenAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/08/22 16:48:17
 * @author yukihiro-kinjyo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.help.context.action;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.tida_okinawa.corona.help.context.CoronaHelpActivator;
import com.tida_okinawa.corona.help.context.Messages;

/**
 * CoronaのマニュアルPDFファイルを開くアクション
 * 
 * @author yukihiro-kinjo
 * 
 */
public class ManualPdfOpenAction implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;


    /**
     * コンストラクター
     */
    public ManualPdfOpenAction() {
    }


    /**
     * 実行
     */
    @Override
    public void run(IAction action) {

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    /* プラグインのパスからマニュアルのパスを取得 */
                    URL entry = CoronaHelpActivator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
                    String manualPath = FileLocator.resolve(entry).getPath();
                    manualPath = manualPath.replace("file:/", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    manualPath = manualPath.replaceAll("/plugins/com\\.tida_okinawa\\.corona\\.help\\.context_.*\\.jar!", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    manualPath = manualPath + "docs/Coronaマニュアル.pdf"; //$NON-NLS-1$
                    /* マニュアルを開く */
                    File pdfFile = new File(manualPath);
                    if (!(pdfFile.exists())) {
                        MessageDialog.openError(window.getShell(), Messages.ManualPdfOpenAction_ERROR, Messages.ManualPdfOpenAction_NOT_FOUND_MANUAL
                                + manualPath);
                    }
                    desktop.open(pdfFile);
                } catch (IOException e) {
                    MessageDialog.openError(window.getShell(), Messages.ManualPdfOpenAction_ERROR, Messages.ManualPdfOpenAction_OPEN_FAILED_MANUAL);
                }
            }
        }
    }


    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }


    @Override
    public void dispose() {
    }


    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
}