/**
 * @version $Id: CorrectionActivator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/21 13:24:00
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.tida_okinawa.corona.correction.auto.AutoScheduler;

/**
 * The activator class controls the plug-in life cycle
 */
public class CorrectionActivator extends AbstractUIPlugin {

    /** プラグインID */
    public static final String PLUGIN_ID = "com.tida_okinawa.corona.correction"; //$NON-NLS-1$

    // The shared instance
    private static CorrectionActivator plugin;


    /**
     * コンストラクター
     */
    public CorrectionActivator() {
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
    public static CorrectionActivator getDefault() {
        return plugin;
    }


    /**
     * 自動実行スケジュールを起動する
     */
    public static void initAutoSchedules() {
        AutoScheduler.Instance.load();
    }


    /* ****************************************
     * flag
     */
    /**
     * デバッグ出力モードであるか。
     * 現在は常時falseを返却する。
     * 
     * @return デバッグ出力モードならtrue
     */
    public static final boolean isDebugMode() {
        return false;
    }
}
