/**
 * @version $Id: CoronaActivator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/01 12:59:00
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.tida_okinawa.corona.common.DefaultLogger;
import com.tida_okinawa.corona.common.ILogger;

/**
 * The activator class controls the plug-in life cycle
 */
public class CoronaActivator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "com.tida_okinawa.corona"; //$NON-NLS-1$

    // The shared instance
    private static CoronaActivator plugin;


    /**
     * The constructor
     */
    public CoronaActivator() {
    }


    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }


    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }


    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static CoronaActivator getDefault() {
        return plugin;
    }


    ILogger logger = new DefaultLogger();


    /**
     * @return ログ出力
     */
    public ILogger getLogger() {
        return logger;
    }


    /**
     * @param logger
     *            ログ出力
     */
    public void setLogger(ILogger logger) {
        if (logger == null) {
            return;
        }
        this.logger = logger;
    }


    /**
     * ログビューにログを出すための便利メソッド。
     * 
     * @param status
     *            出力するログ。
     * @param activate
     *            出力後、ログビューをアクティブにするならtrue。
     */
    public static void log(IStatus status, boolean activate) {
        if (getDefault() == null) {
            return;
        }

        getDefault().getLog().log(status);
        if (!(activate)) {
            return;
        }

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        if (window != null) {
                            window.getActivePage().showView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
                        }
                    } catch (PartInitException e) {
                        // nothing to do
                    }
                }
            });
        } else {
            try {
                window.getActivePage().showView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
            } catch (PartInitException e) {
                // nothing to do
            }
        }
    }


    /**
     * デバッグ出力モードであるか。
     * 現在は常時trueを返却する。
     * 
     * @return デバッグ出力モードならtrue
     */
    public static boolean isDebugMode() {
        return true;
    }


    /**
     * デバッグ出力を行う。 {@link #isDebugMode()}がtrueのときだけ出力される
     * 
     * @param message
     *            出力するメッセージ
     */
    public static void debugLog(String message) {
        if (isDebugMode()) {
            if (getDefault() != null) {
                getDefault().getLogger().getOutStream().println(message);
            }
            System.out.println(message);
        }
    }
}
