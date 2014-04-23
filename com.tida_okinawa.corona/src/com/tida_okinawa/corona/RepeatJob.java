/**
 * @version $Id: RepeatJob.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/25 14:30:09
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * 繰り返し処理を行うときに使用するJob.
 * 実行する処理と、繰り返し間隔を指定できる。処理が終了してから、指定した間隔だけ時間をおいて次の処理が実行される。
 * 
 * @author kousuke-morishima
 */
public abstract class RepeatJob extends Job {
    private long delay;
    private boolean shouldSchedule = true;


    /**
     * インターバルをデフォルトで設定せずにインスタンス化する。別途インターバルを設定しないと実行できない。
     * 
     * @param name
     *            処理名
     */
    public RepeatJob(String name) {
        super(name);
        setInterval(-1);
    }


    /**
     * インターバルを設定してインスタンス化する
     * 
     * @param name
     *            処理名
     * @param interval
     *            デフォルトのインターバル。非負。
     */
    public RepeatJob(String name, long interval) {
        super(name);
        setInterval(interval);
    }


    @Override
    protected IStatus run(IProgressMonitor monitor) {
        IStatus status = doRun(monitor);
        Assert.isLegal(getInterval() >= 0); // Assert
        schedule(getInterval());
        return status;
    }


    abstract protected IStatus doRun(IProgressMonitor monitor);


    /**
     * @return 処理の実行間隔
     */
    public long getInterval() {
        return delay;
    }


    /**
     * @param interval
     *            処理間隔(ms)
     */
    public void setInterval(long interval) {
        this.delay = interval;
    }


    @Override
    public boolean shouldSchedule() {
        return shouldSchedule;
    }


    /**
     * 繰り返し処理を止める
     */
    public void stop() {
        shouldSchedule = false;
    }

}
