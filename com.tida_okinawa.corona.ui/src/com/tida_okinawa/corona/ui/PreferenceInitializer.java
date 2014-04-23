/**
 * @version $Id: PreferenceInitializer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 15:38:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.tida_okinawa.corona.ui.views.CoronaElementViewerSorter;

public class PreferenceInitializer extends AbstractPreferenceInitializer {
    /**
     * 処理スレッド数
     */
    final static public String PREF_NUM_THREADS = "NUM_THREADS";

    /**
     * ユーザ辞書エディタのレコードを一度に表示する件数
     */
    public static final String PREF_NUM_VIEW_USERDIC_RECORD = "number of record in userdic viewer";

    public static final String PREF_DIC_SORT_TYPE = "dic sort type";

    public static final String PREF_RESULT_NOMATCH = "no match pattern";
    /**
     * DB接続パラメータ
     */
    public static final String PREF_DB_INDEX = "DB_INDEX";
    public static final String PREF_DB_NAME = "DB NAME";
    public static final String PREF_DB_CONNECTER = "DB_CONNECTER";
    public static final String PREF_DB_USER = "DB_USER";
    public static final String PREF_DB_PW = "DB_PW";

    /**
     * 頻出用語警告メッセージ
     */
    public static final String PREF_DISP_FREDLG = "DISP_FREDLG";


    @Override
    public void initializeDefaultPreferences() {
        final int num_proccessors = Runtime.getRuntime().availableProcessors();
        IPreferenceStore store = UIActivator.getDefault().getPreferenceStore();
        store.setDefault(PREF_NUM_THREADS, num_proccessors);
        store.setDefault(PREF_NUM_VIEW_USERDIC_RECORD, 500);
        store.setDefault(PREF_DIC_SORT_TYPE, CoronaElementViewerSorter.TYPE);
        store.setDefault(PREF_RESULT_NOMATCH, false);
        store.setDefault(PREF_DB_INDEX, 0);
        store.setDefault(PREF_DB_NAME, "TIDA");
        store.setDefault(PREF_DB_CONNECTER, "jdbc:h2:tidadb");
        store.setDefault(PREF_DB_USER, "root");
        store.setDefault(PREF_DB_PW, "root");
        store.setDefault(PREF_DISP_FREDLG, false);
    }
}
