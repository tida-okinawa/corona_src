/**
 * @version $Id: Kernel.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/23 16:56:27
 * @author miyaguni
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import com.tida_okinawa.corona.CoronaActivator;

/**
 * @author miyaguni
 */
public final class Kernel {
    /**
     * SetThreadExecutionState() が失敗した時の返り値
     */
    private static final int ES_NULL = 0x00000000;

    /**
     * システムアイドルタイマをリセットしてシステムスリープを防ぐ用フラグ
     * 
     * @see "http://msdn.microsoft.com/ja-jp/library/cc429178.aspx"
     */
    private static final int ES_SYSTEM_REQUIRED = 0x00000001;

    /**
     * システムアイドルタイマをリセットしてディスプレイスリープを防ぐ用フラグ
     * 
     * @see "http://msdn.microsoft.com/ja-jp/library/cc429178.aspx"
     */
    private static final int ES_DISPLAY_REQUIRED = 0x00000002;

    /**
     * 上記二つのフラグとセットで使うと、ES_CONTINUOUS 単体で使う時まで設定を維持してくれる。
     * 
     * <pre>
     * // ディスプレイスリープ抑止開始
     * SetThreadExecutionState(ES_DISPLAY_REQUIRED | ES_CONTINUOUS);
     * 
     * // なんらかの処理
     * 
     * // ディスプレイスリープ抑止終了
     * SetThreadExecutionState(ES_CONTINUOUS);
     * </pre>
     * 
     * @see "http://msdn.microsoft.com/ja-jp/library/cc429178.aspx"
     */
    private static final int ES_CONTINUOUS = 0x80000000;

    private interface Kernel32 extends StdCallLibrary {
        public static final Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);


        /**
         * @see "http://msdn.microsoft.com/ja-jp/library/cc429178.aspx"
         * @param esFLAGS
         * @return
         */
        public int SetThreadExecutionState(int esFLAGS);
    }


    /**
     * システムスリープを一時的に抑制する。
     * この効果は {@link #stopSuppressSleep()} が呼び出されるまで続く
     */
    public static void startSuppressSystemSleep() {
        int state = Kernel32.INSTANCE.SetThreadExecutionState(ES_SYSTEM_REQUIRED | ES_CONTINUOUS);
        if (state == ES_NULL) {
            CoronaActivator.debugLog("システムスリープの抑止に失敗しました");
        }
    }


    /**
     * ディスプレイスリープを一時的に抑制する。
     * この効果は {@link #stopSuppressSleep()} が呼び出されるまで続く
     */
    public static void startSuppressDisplaySleep() {
        int state = Kernel32.INSTANCE.SetThreadExecutionState(ES_DISPLAY_REQUIRED | ES_CONTINUOUS);
        if (state == ES_NULL) {
            CoronaActivator.debugLog("ディスプレイスリープの抑止に失敗しました");
        }
    }


    /**
     * ディスプレイ及びシステムのスリープを一時的に抑制する。
     * この効果は {@link #stopSuppressSleep()} が呼び出されるまで続く
     */
    public static void startSuppressSleep() {
        int state = Kernel32.INSTANCE.SetThreadExecutionState(ES_SYSTEM_REQUIRED | ES_DISPLAY_REQUIRED | ES_CONTINUOUS);
        if (state == ES_NULL) {
            CoronaActivator.debugLog("スリープの抑止に失敗しました");
        }
    }


    /**
     * {@link #startSuppressDisplaySleep()} や
     * {@link #startSuppressSystemSleep()}、{@link #startSuppressSleep()}
     * の効果を解除する
     */
    public static void stopSuppressSleep() {
        Kernel32.INSTANCE.SetThreadExecutionState(ES_CONTINUOUS);
    }
}