/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/15 16:34:08
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.collocation;

import org.eclipse.osgi.util.NLS;

/**
 * @author wataru-higa
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.collocation.messages"; //$NON-NLS-1$
    /**
     * .Net Framework 4.0以上がインストールされていない可能性
     */
    public static String COLLOCATION_ErrorMessage_TMT_NotInstalled_DotNetFramework4;
    /**
     * TMT実行中にOutOfMemoryが起きた
     */
    public static String COLLOCATION_ErrorMessage_TMT_OutOfMemory;
    /**
     * プログレスモニタタイトル
     */
    public static String COLLOCATION_PROGRES_BEGINTASK_COLLOCATION;
    /**
     * プログレスモニタTMT実行中表示内容
     */
    public static String COLLOCATION_PROGRESS_SUBTASK_TMT;
    /**
     * プログレスモニタJuman実行中表示内容
     */
    public static String COLLOCATION_PROGRESS_SUBTASK_JUMAN;
    /**
     * プログレスモニタ連語抽出実行中内容
     */
    public static String COLLOCATION_PROGRESS_SUBTASK_COLLOCATION;
    /**
     * TMT実行エラーメッセージ内容
     */
    public static String COLLOCATION_MESSAGE_BOX_EXCEPTION_ERROR_TEXT;
    /**
     * TMT実行エラータイトル
     */
    public static String COLLOCATION_MESSAGE_BOX_EXCEPTION_ERROR_TITLE;
    /**
     * 抽出結果0件時のメッセージ内容
     */
    public static String COLLOCATION_MESSAGE_BOX_EMPTY_ERROR_TEXT_1;
    /**
     * 抽出結果0件時のタイトル
     */
    public static String COLLOCATION_MESSAGE_BOX_EMPTY_ERROR_TITLE;
    /**
     * TMTコマンド
     */
    public static String COLLOCATION_TMT_EXECUTE_COMMAND;
    /**
     * 区切り文字定義ファイル
     */
    public static String COLLOCATION_TMT_EXECUTE_KEY_FILE;
    /**
     * TMTコマンドオプション{-w}
     */
    public static String COLLOCATION_TMT_EXECUTE_OPTION_1;
    /**
     * TMTコマンドオプション{-n=}
     */
    public static String COLLOCATION_TMT_EXECUTE_OPTION_2;
    /**
     * TMTコマンドオプション{-k=}
     */
    public static String COLLOCATION_TMT_EXECUTE_OPTION_3;
    /**
     * TMTコマンドオプション{-n=}<br/>
     * 出現数/共起数がこの値に満たないものは除外されます。
     */
    public static String COLLOCATION_TMT_EXECUTE_OPTION_N;
    /**
     * TMTコマンドオプション{-r=}<br/>
     * 出力形式を共起抽出にします。
     */
    public static String COLLOCATION_TMT_EXECUTE_OPTION_R;
    /**
     * TMTコマンドオプション{-m=}<br/>
     * 共起語の最大数を2～5まで指定できます。(省略した場合は2)
     */
    public static String COLLOCATION_TMT_EXECUTE_OPTION_M;
    /**
     * TMTコマンドオプション{-s=}<br/>
     * 共起抽出で出現順を考慮しません。
     */
    public static String COLLOCATION_TMT_EXECUTE_OPTION_S;
    /**
     * TMT実行エラーメッセージ内容（共起抽出結果が空）
     */
    public static String COLLOCATION_MESSAGE_BOX_EMPTY_ERROR_TEXT;
    /**
     * TMT実行エラータイトル（共起抽出）
     */
    public static String COLLOCATION_MESSAGE_BOX_TITLE;
    /**
     * TMT実行エラーメッセージ内容（共起抽出失敗）
     */
    public static String COLLOCATION_MESSAGE_BOX_TMT_ERROR_TEXT;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
