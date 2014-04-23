/**
 * @version $Id: PreferenceInitializer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 15:38:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Preferenceの初期値を設定するクラス
 * 
 * @author kousuke-morishima
 * 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
    /**
     * データベース接続が切断されたとき、再接続を試行する最大回数
     */
    public static final String PREF_DB_RETRY_CNT = "try reconnect count"; //$NON-NLS-1$


    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = CoronaActivator.getDefault().getPreferenceStore();
        store.setDefault(PREF_DB_RETRY_CNT, 5);
    }
}
