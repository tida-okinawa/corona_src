/**
 * @version $Id: ILogger.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/31 18:30:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.common;

import java.io.PrintStream;

/**
 * ログをコンソールなどに出力するためのインタフェース
 * 
 * @author imai
 * 
 */
public interface ILogger {
    /**
     * Coronaコンソールへの標準出力を取得する.
     * ここで得たストリームを閉じると、Coronaコンソールへの標準出力が閉じてしまうので、
     * <code>{@link PrintStream#close()}</code>しないこと。
     * 
     * @return 標準出力
     */
    PrintStream getOutStream();


    /**
     * Coronaコンソールへのエラー出力を取得する.
     * ここで得たストリームを閉じると、Coronaコンソールへのエラー出力が閉じてしまうので、
     * <code>{@link PrintStream#close()}</code>しないこと。
     * 
     * @return エラー出力
     */
    PrintStream getErrStream();
}
