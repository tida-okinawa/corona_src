/**
 * @version $Id: ExternalProgramExitException.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/17 12:00:11
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.common;

import java.util.Arrays;

/**
 * 外部プログラムが異常終了
 * 
 */
public class ExternalProgramExitException extends Exception {
    private static final long serialVersionUID = 8935125120569256236L;

    /**
     * 実行時のコマンド
     */
    final public String[] args;

    /**
     * プロセス
     */
    /* シリアライズできないので、transient修飾子を付加 */
    final public transient Process process;


    /**
     * @param args
     *            コマンド
     * @param process
     *            実行先
     */
    public ExternalProgramExitException(String[] args, Process process) {
        this.args = Arrays.copyOf(args, args.length);
        this.process = process;
    }


    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (String s : args) {
            buf.append(s).append(" "); //$NON-NLS-1$
        }
        return buf.toString();
    }
}
