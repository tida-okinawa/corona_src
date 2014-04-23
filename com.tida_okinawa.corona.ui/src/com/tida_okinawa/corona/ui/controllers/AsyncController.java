/**
 * @version $Id: AsyncController.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 21:30:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.ui.PreferenceInitializer;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * UI-処理のインタフェース
 * 
 * 大量のデータを１件ずつ処理して、処理結果をUIに通知する
 * 
 * @author imai
 * 
 * @param <TS>
 *            入力データ
 * @param <TR>
 *            処理結果
 * 
 *            TD は io, TL は view に実装
 */
abstract public class AsyncController<TS, TR> implements IRunnableWithProgress {

    final String name;
    IDataProvider<TS> provider;
    List<IListener<TR>> listeners = new ArrayList<IListener<TR>>();

    /**
     * 処理スレッドの数
     */
    final int nProcThread;


    /**
     * @param name
     *            処理名
     * @param nProcThread
     *            処理スレッド数
     */
    AsyncController(String name, int nProcThread) {
        this.name = name;
        this.nProcThread = nProcThread;
    }


    /**
     * @param name
     *            処理名
     */
    AsyncController(String name) {
        this.name = name;
        if (UIActivator.getDefault() != null) {
            this.nProcThread = UIActivator.getDefault().getPreferenceStore().getInt(PreferenceInitializer.PREF_NUM_THREADS);
        } else {
            this.nProcThread = Runtime.getRuntime().availableProcessors();
        }
    }


    /**
     * 
     * @param name
     *            タスク名
     * @param nProcThread
     *            処理スレッド数
     * @param provider
     *            問い合わせ情報など
     */
    AsyncController(String name, int nProcThread, final IDataProvider<TS> provider) {
        this(name, nProcThread);
        setProvider(provider);
    }


    void setProvider(final IDataProvider<TS> provider) {
        this.provider = provider;
    }


    void addListener(final IListener<TR> listener) {
        this.listeners.add(listener);
    }


    /**
     * 処理スレッドの数を取得
     * 
     * @return　処理スレッドの数
     */
    public int getThreadNum() {
        return nProcThread;
    }

    int worked = 0;


    /**
     * 処理を実行する
     * 
     * @param progressMonitor
     *            プログレスモニター
     */
    @Override
    public void run(final IProgressMonitor progressMonitor) throws InterruptedException, InvocationTargetException {
        System.out.println("# create " + getThreadNum() + " threads."); //$NON-NLS-1$ //$NON-NLS-2$

        final IProgressMonitor monitor;
        if (progressMonitor == null) {
            monitor = new NullProgressMonitor();
        } else {
            monitor = progressMonitor;
        }

        /* タスクの量 = 入力データの量 */
        final int total = provider.total();

        monitor.beginTask(name, total + 1);

        start(new SubProgressMonitor(monitor, 1));
        if (monitor.isCanceled()) {
            throw new InterruptedException(""); //$NON-NLS-1$
        }
        monitor.setTaskName(name);

        /* 処理スレッドを準備 */
        Thread[] threads = new Thread[getThreadNum()];

        /* 処理スレッドの起動 */
        for (int i = 0; i < getThreadNum(); i++) {
            threads[i] = new RunThread(monitor, total, i);
            /* プライオリティを設定 */
            threads[i].setPriority(Thread.MIN_PRIORITY);
            threads[i].start();
        }

        /* 入力データの供給を開始 */
        provider.run(monitor);

        /* すべての処理を待つ */
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /* 終わりを通知 */
        for (IListener<TR> listener : listeners) {
            listener.end(monitor);
        }

        monitor.done();

        end(monitor);
    }


    /**
     * @return Controller内のスレッド番号を取得
     */
    @SuppressWarnings("static-method")
    protected final int getThreadId() {
        @SuppressWarnings("unchecked")
        RunThread t = (RunThread) Thread.currentThread();
        return t.threadId;
    }

    private final class RunThread extends Thread {
        private final IProgressMonitor monitor;
        private final int total;
        final int threadId;

        /**
         * 処理時間
         */
        private int totalTime = 0;
        /**
         * 処理レコード数
         */
        private int nRecs = 0;


        RunThread(IProgressMonitor monitor, int total, int threadId) {
            this.monitor = monitor;
            this.total = total;
            this.threadId = threadId;
        }


        @Override
        public void run() {
            TS data;
            while ((data = next()) != null && !monitor.isCanceled()) {
                try {
                    long t0 = System.currentTimeMillis();
                    TR result = exec(data);
                    long t1 = System.currentTimeMillis();
                    sendResult(result);
                    totalTime += (t1 - t0);
                    nRecs++;
                } catch (Exception e) {
                    e.printStackTrace();
                    IStatus status = null;
                    if (CoronaActivator.isDebugMode()) {
                        status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, "Uncaught exception occurred.", e); //$NON-NLS-1$
                    } else {
                        status = new Status(IStatus.WARNING, UIActivator.PLUGIN_ID, "Uncaught exception occurred.", e); //$NON-NLS-1$
                    }
                    CoronaActivator.log(status, false);
                }
                monitor.worked(1);
                worked++;
                monitor.subTask(worked + "/" + total); //$NON-NLS-1$
            }
            provider.endQueue();
        }


        private TS next() {
            synchronized (provider) {
                TS data = provider.next();
                return data;
            }
        }


        private void sendResult(TR result) {
            for (IListener<TR> listener : listeners) {
                synchronized (listener) {
                    listener.receiveResult(result);
                }
            }
        }
    }


    /**
     * 1データ分の処理
     * スレッドセーフにすること
     * 
     * @param data
     *            入力データ
     * @return 解析結果
     */
    abstract TR exec(TS data);


    /**
     * 開始時の処理
     * 
     * @param monitor
     *            プログレスモニター
     * @throws InterruptedException
     *             何らかの割り込みが発生
     */
    abstract protected void start(IProgressMonitor monitor) throws InterruptedException;


    /**
     * 終了時の処理
     * 
     * @param monitor
     *            進捗ダイアログ
     */
    abstract protected void end(IProgressMonitor monitor);
}
