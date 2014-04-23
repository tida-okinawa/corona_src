/**
 * @version $Id: DashboardActivator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/22 17:09:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.dashboard;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DashboardActivator extends AbstractUIPlugin implements IStartup {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "com.tida_okinawa.corona.dashboard"; //$NON-NLS-1$

    // The shared instance
    private static DashboardActivator plugin;


    /**
     * The constructor
     */
    public DashboardActivator() {
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
    public static DashboardActivator getDefault() {
        return plugin;
    }


    @Override
    public void earlyStartup() {
        // nothing to do
    }

}
