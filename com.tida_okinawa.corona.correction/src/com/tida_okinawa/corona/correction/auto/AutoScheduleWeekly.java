/**
 * @version $Id: AutoScheduleWeekly.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/15 18:04:04
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.auto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 毎週スケジュールの場合の予定を記憶するクラス.
 * 
 * @author kousuke-morishima
 */
public class AutoScheduleWeekly extends AutoSchedule {
    private static final long serialVersionUID = -8493726077957331354L;

    private List<Integer> runningWeeks;


    /**
     * コンストラクタ
     */
    public AutoScheduleWeekly() {
        super();
        runningWeeks = new ArrayList<Integer>(0);
    }


    /**
     * コンストラクタ
     * 実行する曜日を初期設定する
     * 
     * @param runningWeeks
     *            実行する曜日
     */
    public AutoScheduleWeekly(List<Integer> runningWeeks) {
        super();
        setRunWeeks(runningWeeks);
    }


    /**
     * 実行する曜日を設定する.
     * List内は昇順にソートされていること
     * 
     * @param runningWeeks
     *            実行する曜日
     * @throws NullPointerException
     *             if runningWeeks is null
     */
    public void setRunWeeks(List<Integer> runningWeeks) {
        if (runningWeeks == null) {
            throw new NullPointerException();
        }
        if (!runningWeeks.equals(this.runningWeeks)) {
            this.runningWeeks = runningWeeks;
            updateNextRunDate();
            fireScheduleChanged();
        }
    }


    /**
     * このスケジュールを実行する曜日を返す
     * 
     * @return 実行する曜日
     */
    public List<Integer> getRunWeeks() {
        return runningWeeks;
    }


    /**
     * 処理を実行する曜日を追加する
     * 
     * @param weekday
     *            実行する曜日
     * @see Calendar#SUNDAY
     * @see Calendar#MONDAY
     * @see Calendar#TUESDAY
     * @see Calendar#WEDNESDAY
     * @see Calendar#THURSDAY
     * @see Calendar#FRIDAY
     * @see Calendar#SATURDAY
     */
    public void addRunWeek(int weekday) {
        if (!runningWeeks.contains(weekday)) {
            runningWeeks.add(weekday);
            updateNextRunDate();
            fireScheduleChanged();
        }
    }


    /**
     * 実行する曜日から、指定の曜日を除去する
     * 
     * @param weekday
     *            実行しない曜日
     */
    public void removeRunWeek(int weekday) {
        runningWeeks.remove(Integer.valueOf(weekday));
    }


    /**
     * @throws IllegalStateException
     *             if runningWeeks is not set
     */
    @Override
    public Calendar updateNextRunDate() {
        if (runningWeeks == null || runningWeeks.isEmpty()) {
            throw new IllegalStateException();
        }

        super.updateNextRunDate();
        int nextWeekday = nextRunDate.get(Calendar.DAY_OF_WEEK);
        boolean nextIsBefore = true;
        for (int week : runningWeeks) {
            if (nextWeekday <= week) {
                nextIsBefore = false;
                int add = week - nextWeekday;
                nextRunDate.add(Calendar.DATE, add);
                break;
            }
        }
        if (nextIsBefore) {
            int add = runningWeeks.get(0) - nextWeekday + 7;
            nextRunDate.add(Calendar.DATE, add);
        }
        return (Calendar) nextRunDate.clone();
    }
}
