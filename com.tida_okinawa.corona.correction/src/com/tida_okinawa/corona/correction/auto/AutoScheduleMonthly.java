/**
 * @version $Id: AutoScheduleMonthly.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/15 18:42:40
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.auto;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 自動実行を毎月実行するスケジュール
 * 
 * @author kousuke-morishima
 */
public class AutoScheduleMonthly extends AutoSchedule {
    private static final long serialVersionUID = 6920688162832272089L;

    private List<Integer> runningDays;


    /**
     * コンストラクタ
     */
    public AutoScheduleMonthly() {
        super();
    }


    /**
     * コンストラクタ.
     * 実行する日を初期設定する
     * 
     * @param runningDays
     *            実行する日
     */
    public AutoScheduleMonthly(List<Integer> runningDays) {
        super();
        setRunDays(runningDays);
    }


    /**
     * 実行する日を設定する
     * 
     * @param runningDays
     *            実行する日
     * @throws NullPointerException
     *             if runningWeeks is null
     */
    public void setRunDays(List<Integer> runningDays) {
        if (runningDays == null) {
            throw new NullPointerException();
        }
        if (!runningDays.equals(this.runningDays)) {
            this.runningDays = runningDays;
            updateNextRunDate();
            fireScheduleChanged();
        }
    }


    /**
     * このスケジュールを実行する日を返す
     * 
     * @return 実行する日
     */
    public List<Integer> getRunDays() {
        return runningDays;
    }


    /**
     * 処理を実行する日を追加する
     * 
     * @param day
     *            実行する日
     * @see #START_OF_MONTH
     * @see #MIDDLE_OF_MONTH
     * @see #END_OF_MONTH
     */
    public void addRunDay(int day) {
        if (!runningDays.contains(day)) {
            runningDays.add(day);
            updateNextRunDate();
            fireScheduleChanged();
        }
    }

    /** 月初めに実行することを表す定数 */
    public static final int START_OF_MONTH = 1;
    /** 15日に実行することを表す定数 */
    public static final int MIDDLE_OF_MONTH = 15;
    /** 月末に実行することを表す定数 */
    public static final int END_OF_MONTH = -1;


    /**
     * @throws IllegalStateException
     *             if runningDays is not set
     */
    @Override
    public Calendar updateNextRunDate() {
        super.updateNextRunDate();
        int nextDate = nextRunDate.get(Calendar.DATE);
        boolean nextIsBefore = true;
        for (Integer day : runningDays) {
            if (day == END_OF_MONTH) {
                /* nextRunDate が含まれる月の最終日を求める */
                Calendar c = Calendar.getInstance(Locale.JAPAN);
                c.set(Calendar.MONTH, nextRunDate.get(Calendar.MONTH) + 1);
                c.set(Calendar.DATE, 1);
                c.add(Calendar.DATE, -1);
                int lastDay = c.get(Calendar.DATE);
                int add = lastDay - nextDate;
                nextRunDate.add(Calendar.DATE, add);
                return nextRunDate;
            } else {
                if (nextDate <= day) {
                    nextIsBefore = false;
                    int add = day - nextDate;
                    nextRunDate.add(Calendar.DATE, add);
                    break;
                }
            }
        }
        if (nextIsBefore) {
            nextRunDate.add(Calendar.MONTH, 1);
            nextRunDate.set(Calendar.DATE, runningDays.get(0));
        }
        return (Calendar) nextRunDate.clone();
    }
}
