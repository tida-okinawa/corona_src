/**
 * @version $Id: AutoScheduler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/18 20:07:52
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.auto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.tida_okinawa.corona.correction.CorrectionActivator;
import com.tida_okinawa.corona.correction.auto.AutoSchedule.ScheduleChangedListener;

/**
 * 自動実行スケジュールを管理、実行するクラス
 * 
 * @author kousuke-morishima
 * 
 */
public class AutoScheduler implements ScheduleChangedListener {

    /**
     * シングルトン
     */
    public static final AutoScheduler Instance = new AutoScheduler();


    private AutoScheduler() {
        scheduledJobs = new HashMap<AutoSchedule, AutoScheduleJob>();
    }


    private List<AutoSchedule> schedules;
    private Map<AutoSchedule, AutoScheduleJob> scheduledJobs;


    /**
     * 現在設定されているスケジュールを返す
     * 
     * @return 設定されているスケジュールの一覧
     */
    public List<AutoSchedule> getSchedules() {
        if (schedules == null) {
            load();
        }
        return new ArrayList<AutoSchedule>(schedules);
    }


    /**
     * スケジュールを追加する
     * 
     * @param schedule
     *            追加するスケジュール
     */
    public void add(AutoSchedule schedule) {
        if (schedule != null) {
            if (schedules == null) {
                load();
            }
            if (!schedules.contains(schedule)) {
                schedule.addScheduleChangedListener(this);
                schedules.add(schedule);
                setJob(schedule);
            }
        }
    }


    private void setJob(AutoSchedule schedule) {
        /* 実行する処理を設定する */
        AutoScheduleJob job = new AutoScheduleJob(schedule);
        job.schedule(AutoScheduleJob.computeInterval(schedule));
        scheduledJobs.put(schedule, job);
    }


    @Override
    public void scheduleChanged(AutoSchedule schedule) {
        AutoScheduleJob job = scheduledJobs.remove(schedule);
        if (job != null) {
            job.cancel();
        }
        if (schedule.isDoRun()) {
            setJob(schedule);
        }
    }


    /**
     * @param schedule
     *            削除するスケジュール
     * @return 指定されたスケジュールを削除したらtrue
     */
    public boolean remove(AutoSchedule schedule) {
        if (schedules == null) {
            return false;
        }

        if (schedule != null) {
            schedule.removeScheduleChangedListener(this);
            AutoScheduleJob job = scheduledJobs.remove(schedule);
            if (job != null) {
                job.cancel();
            }
            return schedules.remove(schedule);
        }
        return false;
    }


    /**
     * 既存のスケジュールを別のスケジュールに置き換える。このメソッドは、管理されている順番を維持したまま入れ替えを行いたいときに使用する
     * 
     * @param newSchedule
     *            置き換える新しいスケジュール
     * @param oldSchedule
     *            置き換えられる既存のスケジュール
     * @return 置き換えに成功したらtrue
     */
    public boolean replace(AutoSchedule newSchedule, AutoSchedule oldSchedule) {
        if (schedules == null) {
            return false;
        }

        int oldIndex = schedules.indexOf(oldSchedule);
        if (oldIndex != -1) {
            schedules.set(oldIndex, newSchedule);
            return true;
        }
        return false;
    }


    /**
     * 現在設定されている自動実行スケジュールを保存する.
     */
    public final void save() {
        if (schedules == null) {
            return;
        }

        /* 保存先を取得する */
        IPath location = getScheduleSavePath();
        /* 保存前に、ファイルを消す */
        File dir = new File(location.toOSString());
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(AutoSchedule.extension)) {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
        }

        for (int i = 0; i < schedules.size(); i++) {
            IPath fileLocation = location.append(String.format("%04d", i)).addFileExtension(AutoSchedule.extension); //$NON-NLS-1$
            save(schedules.get(i), fileLocation.toOSString());
        }
    }


    /**
     * @param schedule
     *            保存するスケジュール
     * @param filePath
     *            保存先のファイルパス
     */
    static final void save(AutoSchedule schedule, String filePath) {
        ObjectOutputStream output = null;
        try {
            /* ファイルを作る */
            output = new ObjectOutputStream(new FileOutputStream(new File(filePath)));
            output.writeObject(schedule);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     * 保存場所に保存してあるすべての実行スケジュールを読み込んで返す。
     * 
     * @return 保存されているすべてのスケジュール
     */
    public final AutoSchedule[] load() {
        for (Iterator<Entry<AutoSchedule, AutoScheduleJob>> itr = scheduledJobs.entrySet().iterator(); itr.hasNext();) {
            Entry<AutoSchedule, AutoScheduleJob> e = itr.next();
            e.getKey().removeScheduleChangedListener(this);
            e.getValue().cancel();
            itr.remove();
        }
        schedules = new ArrayList<AutoSchedule>();
        IPath location = getScheduleSavePath();
        File dir = new File(location.toOSString());
        for (File file : dir.listFiles()) {
            AutoSchedule schedule = load(file);
            if (schedule != null) {
                add(schedule);
            }
        }
        return schedules.toArray(new AutoSchedule[schedules.size()]);
    }


    /**
     * 指定された実行スケジュールを読み込む
     * 
     * @param file
     *            読みこむ実行スケジュール
     * @return 読み込んだ実行スケジュール。読み込めなかったらnull
     */
    public static final AutoSchedule load(File file) {
        if (file.isFile() && file.getName().endsWith(AutoSchedule.extension)) {
            ObjectInputStream input = null;
            try {
                input = new ObjectInputStream(new FileInputStream(file));
                AutoSchedule schedule = (AutoSchedule) input.readObject();
                schedule.initTransientField();
                if (schedule.taskFilePath != null) {
                    schedule.setTask(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(schedule.taskFilePath)));
                }
                schedule.updateNextRunDate();
                return schedule;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return null;
    }


    /**
     * @return スケジュールの保存先フォルダのパス
     */
    private static final IPath getScheduleSavePath() {
        IPath location = ResourcesPlugin.getPlugin().getStateLocation();
        location = location.append(CorrectionActivator.PLUGIN_ID).append(".autoSchedule"); //$NON-NLS-1$
        File saveDirectory = new File(location.toOSString());
        if (!saveDirectory.exists()) {
            if (!saveDirectory.mkdirs()) {
                return null;
            }
        }
        return location;
    }


}
