/**
 * @version $Id: Activator.java 1842 2014-04-21 04:50:18Z yukihiro-kinjyo $
 * 
 * 2012/02/02 17:01:09
 * @author Shingo-Takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.resultpattern.export;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * 構文解析結果を、ヒットしたラベルごとに整形して出力する機能を提供するプラグインのアクティベータ
 * 
 * @author Shingo-Takahashi
 */
public class Activator extends AbstractUIPlugin implements IStartup {

    /** プラグインID */
    public static final String PLUGIN_ID = "com.tida_okinawa.corona.ui.editors.resultpatterneditor.export.tagAligned";
    private static Activator plugin;


    /** 何もしないコンストラクタ */
    public Activator() {
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
    public static Activator getDefault() {
        return plugin;
    }


    /**
     * プラグインIDを取得
     * 
     * @return プラグインID
     */
    public static String getPluginId() {
        return PLUGIN_ID;
    }


    @Override
    public void earlyStartup() {
        /* 何もしない */

    }
}