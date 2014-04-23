/**
 * @version $Id: LicenseActivator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/03/15 11:26:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.license;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LicenseActivator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "com.tida_okinawa.corona.license"; //$NON-NLS-1$

    // The shared instance
    private static LicenseActivator plugin;


    /**
     * The constructor
     */
    public LicenseActivator() {
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
    public static LicenseActivator getDefault() {
        return plugin;
    }


    // testH25 20130806 互換性テスト
    /**
     * ライセンスで登録されたDBバージョンを取得する。
     * 
     * @return ライセンスで登録されたDBバージョン
     */
    public String getDbVersion() {
        // 
        return "1.0.0"; //$NON-NLS-1$
    }

}
