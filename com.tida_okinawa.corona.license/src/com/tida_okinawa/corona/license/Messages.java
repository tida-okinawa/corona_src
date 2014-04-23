package com.tida_okinawa.corona.license;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.license.messages"; //$NON-NLS-1$
    public static String LicenseActivator_DialogMessage;
    public static String LicenseActivator_DialogTitle;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
