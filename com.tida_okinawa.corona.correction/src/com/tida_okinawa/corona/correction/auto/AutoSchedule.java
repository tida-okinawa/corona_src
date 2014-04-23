/**
 * @version $Id: AutoSchedule.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/15 17:49:24
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.auto;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ListenerList;


/**
 * 自動実行のスケジュールを表すモデル.
 * 
 * @author kousuke-morishima
 */
public class AutoSchedule implements Serializable, Cloneable {

    private static final long serialVersionUID = 5241031190990865358L;
    /**
     * 自動実行スケジュールをファイル保存した時の拡張子
     */
    static final String extension = "schedule"; //$NON-NLS-1$

    protected transient IFile task;
    protected String taskFilePath;
    protected boolean doRun;
    protected Calendar time;
    protected Calendar nextRunDate;


    /**
     * コンストラクタ
     */
    public AutoSchedule() {
        doRun = true;
    }


    /**
     * デシリアライズした場合、listenersがnullになって困るので、デシリアライズ後はこのメソッドを呼んで回避する
     */
    void initTransientField() {
        listeners = new ListenerList();
        modified = false;
    }


    /**
     * 解析フローを定義したファイルのインスタンスを返す。実行する解析フローが設定されていない場合、<code>null</code>を返す。
     * 
     * @return 設定されている解析フロー
     */
    public IFile getTask() {
        return task;
    }


    /**
     * 実行する解析フローを設定する。<code>null</code>を指定すると、設定解除になる。
     * 
     * @param task
     *            解析フロー
     */
    public void setTask(IFile task) {
        if (task == null) {
            if (this.task == null) {
                return;
            }
        } else if (this.task == null) {
        } else if (this.task.equals(task)) {
            return;
        }
        this.task = task;
        if (task == null) {
            taskFilePath = null;
        } else {
            taskFilePath = task.getFullPath().toOSString();
        }
        fireScheduleChanged();
    }


    /**
     * 指定された実行日時が来たら実行するかどうか。
     * 
     * @return 実行するならtrue
     */
    public boolean isDoRun() {
        return doRun;
    }


    /**
     * @param doRun
     *            実行するならtrue
     */
    public void setDoRun(boolean doRun) {
        if (this.doRun != doRun) {
            this.doRun = doRun;
            fireScheduleChanged();
        }
    }


    private transient boolean modified = true;


    /**
     * 実行時間を設定する。
     * 
     * @param hours
     *            時(24時間表記)
     * @param minutes
     *            分
     */
    public void setTime(int hours, int minutes) {
        if (time == null) {
            time = Calendar.getInstance(Locale.JAPAN);
            time.set(Calendar.SECOND, 0);
            modified = true;
        }
        if ((time.get(Calendar.HOUR_OF_DAY) != hours) || (time.get(Calendar.MINUTE) != minutes)) {
            time.set(Calendar.HOUR_OF_DAY, hours);
            time.set(Calendar.MINUTE, minutes);
            modified = true;
        }
        getNextRunDate();
    }


    final Calendar getRunDate() {
        Calendar calendar = Calendar.getInstance(Locale.JAPAN);
        /* 時間を設定する */
        if (time != null) {
            calendar.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, time.get(Calendar.SECOND));
        }
        return calendar;
    }


    /**
     * 次に実行すべき日時を返す。
     * 
     * @return 次に実行すべき日時を返す
     */
    public Calendar getNextRunDate() {
        if (modified) {
            updateNextRunDate();
            fireScheduleChanged();
        }
        return (Calendar) nextRunDate.clone();
    }


    /**
     * 次回実行日時を最新に更新する
     * 
     * @return 次回実行日時
     */
    public Calendar updateNextRunDate() {
        modified = false;
        nextRunDate = getRunDate();
        /* 時間が過ぎていたら進める */
        Calendar now = Calendar.getInstance(Locale.JAPAN);
        if (!now.before(nextRunDate)) {
            nextRunDate.set(Calendar.YEAR, now.get(Calendar.YEAR));
            nextRunDate.set(Calendar.MONTH, now.get(Calendar.MONTH));
            nextRunDate.set(Calendar.DATE, now.get(Calendar.DATE) + 1);
        }
        return (Calendar) nextRunDate.clone();
    }


    /**
     * 実行できるかどうかを返す。すべてのパラメータが正しく設定されていない場合、falseを返す。
     * 
     * @return 正しくパラメータが設定されていて、実行できる場合はtrue。そうでなければfalse
     */
    boolean isRunnable() {
        return task != null;
    }


    private transient ListenerList listeners = new ListenerList();


    void addScheduleChangedListener(ScheduleChangedListener listener) {
        listeners.add(listener);
    }


    void removeScheduleChangedListener(ScheduleChangedListener listener) {
        listeners.remove(listener);
    }


    protected void fireScheduleChanged() {
        for (Object l : listeners.getListeners()) {
            ((ScheduleChangedListener) l).scheduleChanged(this);
        }
    }

    interface ScheduleChangedListener {
        /**
         * @param schedule
         *            変更されたスケジュール
         */
        void scheduleChanged(AutoSchedule schedule);
    }


    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(128);
        string.append("Task:"); //$NON-NLS-1$
        string.append((task == null) ? "none" : task.getName()).append(", "); //$NON-NLS-1$ //$NON-NLS-2$
        Calendar c = getNextRunDate();
        string.append("Next:").append(c.get(Calendar.YEAR)).append("/").append(c.get(Calendar.MONTH) + 1).append("/").append(c.get(Calendar.DATE)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        string.append(" ").append(c.get(Calendar.HOUR_OF_DAY)).append(":").append(c.get(Calendar.MINUTE)).append(", "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        string.append("Run:").append(doRun); //$NON-NLS-1$
        return string.toString();
    }


    @Override
    public AutoSchedule clone() {
        try {
            return (AutoSchedule) super.clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
