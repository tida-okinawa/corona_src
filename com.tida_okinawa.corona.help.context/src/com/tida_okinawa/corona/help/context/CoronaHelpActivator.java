/**
 * @version $Id: CoronaHelpActivator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/03/01 17:00:00
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.help.context;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CoronaHelpActivator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "com.tida_okinawa.corona.help.context"; //$NON-NLS-1$

    // The shared instance
    private static CoronaHelpActivator plugin;


    /**
     * The constructor
     */
    public CoronaHelpActivator() {
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
    public static CoronaHelpActivator getDefault() {
        return plugin;
    }

}
