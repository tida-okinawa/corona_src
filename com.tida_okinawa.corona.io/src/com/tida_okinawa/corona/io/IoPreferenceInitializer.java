/**
 * @version $Id: IoPreferenceInitializer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/25 14:15:39
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author kousuke-morishima
 */
public class IoPreferenceInitializer extends AbstractPreferenceInitializer {

    /**
     * コンストラクター
     */
    public IoPreferenceInitializer() {
    }

    /**
     * Juman, KNP の文字コードの指定
     */
    public static final String PREF_MORPHEME_ENCODING = "PREF_MORPHEME_ENCODING"; //$NON-NLS-1$


    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = IoActivator.getDefault().getPreferenceStore();
        store.setDefault(PREF_MORPHEME_ENCODING, "UTF-8"); //$NON-NLS-1$
    }
}
