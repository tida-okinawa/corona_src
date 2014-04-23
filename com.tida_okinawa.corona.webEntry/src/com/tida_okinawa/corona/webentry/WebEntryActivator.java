/**
 * @version $Id: WebEntryActivator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/18 18:07:00
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the Web Entry plug-in life cycle
 */
public class WebEntryActivator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "com.tida_okinawa.corona.webEntry"; //$NON-NLS-1$

    // The shared instance
    private static WebEntryActivator plugin;


    /**
     * The constructor
     */
    public WebEntryActivator() {
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
    public static WebEntryActivator getDefault() {
        return plugin;
    }

}
