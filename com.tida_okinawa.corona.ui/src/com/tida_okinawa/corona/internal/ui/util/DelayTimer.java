/**
 * @version $Id: DelayTimer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/01 12:56:17
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author kousuke-morishima
 */
public class DelayTimer {

    private Timer timer;
    private TimerTask task;
    Runnable run;


    public DelayTimer(String name, final Runnable run) {
        timer = new Timer(name);

        this.run = run;
    }


    /**
     * delay後に処理実行する。 {@link #cancel()}後は動かない。
     * 
     * @param delay
     */
    public void run(long delay) {
        if (timer != null) {
            if (task != null) {
                task.cancel();
            }
            timer.schedule(newTask(), delay);
        }
    }


    private TimerTask newTask() {
        task = new TimerTask() {
            @Override
            public void run() {
                run.run();
            }
        };
        return task;
    }


    /**
     * このTimerをキャンセルする。もう動かない。
     */
    public void cancel() {
        task.cancel();
        timer.cancel();
        timer = null;
        task = null;
        run = null;
    }
}
