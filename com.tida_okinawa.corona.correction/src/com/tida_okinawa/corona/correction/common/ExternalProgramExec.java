/**
 * @version $Id: ExternalProgramExec.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/17 12:00:11
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.tida_okinawa.corona.correction.CorrectionActivator;

/**
 * 外部プログラムを実行する
 * 
 */
public class ExternalProgramExec {

    /**
     * 実行中のプロセス
     */
    Process process;

    private long processTimeout = 0;
    private long outputTimeout = 30000;
    private long errorTimeout = 1000;


    /**
     * 外部プログラムの処理の最大待ち時間(ms)を設定する<br/>
     * 初期値は<code>0</code>（無制限）
     * 
     * @param timeout
     *            最大待ち時間(ms)。0にすると制限なしで待つ
     */
    public void setProcessTimeout(long timeout) {
        this.outputTimeout = timeout;
    }


    /**
     * 外部プログラムの標準出力の最大待ち時間(ms)を設定する<br/>
     * 初期値は<code>30000</code>
     * 
     * @param timeout
     *            最大待ち時間(ms)。0にすると制限なしで待つ
     */
    public void setOutputTimeout(long timeout) {
        this.outputTimeout = timeout;
    }


    /**
     * 外部プログラムの標準エラー出力の最大待ち時間(ms)を設定する<br/>
     * 初期値は<code>1000</code>
     * 
     * @param timeout
     *            最大待ち時間(ms)。0にすると制限なしで待つ
     */
    public void setErrorTimeout(long timeout) {
        this.errorTimeout = timeout;
    }


    /**
     * 外部プログラムを実行する
     * 
     * @param args
     *            コマンド文字列
     * @param directory
     *            実行ディレクトリ
     * @param in
     *            外部コマンドへの入力
     * @param out
     *            外部コマンドの出力を受け取るストリーム
     * @param err
     *            外部コマンドのエラーを受け取るストリーム
     * @throws IOException
     *             何らかのIOエラー
     * @throws InterruptedException
     *             処理待ち中の割り込み
     * @throws ExternalProgramExitException
     *             異常終了
     */
    public void exec(String[] args, File directory, InputStream in, OutputStream out, OutputStream err) throws IOException, InterruptedException,
            ExternalProgramExitException {
        List<String> argList = Arrays.asList(args);
        ProcessBuilder pb = new ProcessBuilder(argList);

        /* ワークディレクトリが指定されている場合 */
        if (directory != null) {
            pb.directory(directory);
        }

        /* 外部プログラムを実行 */
        if (CorrectionActivator.isDebugMode()) {
            StringBuilder buf = new StringBuilder(100);
            for (String arg : args)
                buf.append(arg).append(" "); //$NON-NLS-1$
            System.out.println(buf.toString());
        }

        process = pb.start();
        Thread outThr = null;
        Thread errThr = null;
        Thread inThr = null;
        if (out != null) {
            outThr = print("out", out, process.getInputStream()); //$NON-NLS-1$
        }
        if (err != null) {
            errThr = print("err", err, process.getErrorStream()); //$NON-NLS-1$
        }
        if (in != null) {
            inThr = input("in", process.getOutputStream(), in); //$NON-NLS-1$
        }

        /* 終了まで待ち */
        if (inThr != null) {
            inThr.join(processTimeout);
        }

        if (outThr != null) {
            outThr.join(outputTimeout);
        }
        if (errThr != null) {
            errThr.join(errorTimeout);
        }

        /* 終了コードをチェック */
        int exitValue = process.exitValue();
        if (exitValue != 0) {
            System.err.println(exitValue);
            throw new ExternalProgramExitException(args, process);
        }
    }


    /* 入力(in)のスレッド */
    Thread input(final String name, final OutputStream os, final InputStream is) {
        return redirect(name, os, is, true);
    }


    /* 出力(out, err)のスレッド */
    Thread print(final String name, final OutputStream os, final InputStream is) {
        return redirect(name, os, is, false);
    }

    private static final int RedirectBufSize = 1024 * 64;


    /**
     * リダイレクト
     * 文字コードをそのまま通すようにバイナリで行っている
     * 
     * @param name
     *            名前 （デバッグ用）
     * @param os
     *            出力先
     * @param is
     *            入力
     * @param doClose
     *            出力をcloseするか
     * @return リダイレクト処理を行うスレッド
     */
    Thread redirect(final String name, final OutputStream os, final InputStream is, final boolean doClose) {
        final BufferedInputStream bis = new BufferedInputStream(is);
        final BufferedOutputStream bos = new BufferedOutputStream(os);
        Thread thread = new Thread("redirect-" + name) { //$NON-NLS-1$
            @Override
            public void run() {
                try {
                    int n;
                    byte[] buf = new byte[RedirectBufSize];
                    while ((n = bis.read(buf)) != -1) {
                        bos.write(buf, 0, n);
                    }
                    bis.close();
                    if (doClose) {
                        bos.close();
                        // note: ここで bos をclose()しないとprocessが終わらない
                    } else {
                        bos.flush();
                        // note: ここで bos をclose()すると、コンソールが終わってしまう。
                        // processの入力は別にclose()する
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        };

        thread.start();

        return thread;
    }
}
