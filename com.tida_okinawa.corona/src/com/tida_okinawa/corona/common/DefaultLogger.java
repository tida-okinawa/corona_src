/**
 * @version $Id: DefaultLogger.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/31 18:30:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.common;

import java.io.PrintStream;


/**
 * ログ用のストリーム
 * 
 */
public class DefaultLogger implements ILogger {

    @Override
    public PrintStream getOutStream() {
        return System.out;
    }


    @Override
    public PrintStream getErrStream() {
        return System.err;
    }
}
