/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2013/02/19
 * @author shingo-kuniyoshi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.correction.auto;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.auto.messages"; //$NON-NLS-1$

    /**
     * エラー表示（キャンセルされた処理）
     */
    public static String AutoScheduleJob_errCancelJob;

    /**
     * エラー表示（対象が存在しない）
     */
    public static String AutoScheduleJob_errNoExist;

    /**
     * バンドルID表示領域
     */
    public static String CheckExternalPluginBudleId_labelBundleID;

    /**
     * バンドルID表示確認ダイアログ文言
     */
    public static String CheckExternalPluginBudleId_labelCheckBundleID;

    /**
     * 毎日実行することを表す
     */
    public static String Duration_everyDay;

    /**
     * 毎月実行することを表す
     */
    public static String Duration_everyMonth;

    /**
     * 毎週実行することを表す
     */
    public static String Duration_everyWeek;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
