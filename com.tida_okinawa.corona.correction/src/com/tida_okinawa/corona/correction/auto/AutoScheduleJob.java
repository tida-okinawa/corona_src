/**
 * @version $Id: AutoScheduleJob.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/19 13:02:10
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.auto;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.RepeatJob;
import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.correction.CorrectionActivator;

/**
 * @author kousuke-morishima
 * 
 */
public class AutoScheduleJob extends RepeatJob {
    /** バッチファイル拡張子 */
    public static final String BAT = "bat"; //$NON-NLS-1$
    /** Jython拡張子 */
    public static final String JYTHON = ""; //$NON-NLS-1$
    private AutoSchedule schedule;


    /**
     * @param schedule
     *            実行するスケジュール
     */
    public AutoScheduleJob(AutoSchedule schedule) {
        super((schedule.getTask() != null) ? schedule.getTask().getName() : "", 0); //$NON-NLS-1$
        this.schedule = schedule;
    }


    @Override
    protected IStatus doRun(IProgressMonitor monitor) {
        IStatus result = null;
        /* 次回実行日時を求めるために、実行日時を更新しておく */
        if (schedule.isDoRun()) {
            if (schedule.getTask() != null) {
                final IFile flowFile = schedule.getTask();
                if (flowFile.exists()) {
                    if (BAT.equalsIgnoreCase(flowFile.getFileExtension())) {
                        result = doBatTask(flowFile);
                    } else if (JYTHON.equalsIgnoreCase(flowFile.getFileExtension())) {
                        ;// TODO jython
                    }
                } else {
                    result = new Status(IStatus.WARNING, CorrectionActivator.PLUGIN_ID, flowFile.getFullPath().toString() + Messages.AutoScheduleJob_errNoExist);
                }
            }
        }
        schedule.updateNextRunDate();
        if (!result.isOK()) {
            CoronaActivator.getDefault().getLogger().getErrStream().println(result.getMessage());
        }
        return result;
    }


    /**
     * バッチファイルで定義された解析フローを実行する
     * 
     * @param flowFile
     *            解析フローファイル（*.bat）
     * @return 成功ならOK_STATUS
     */
    private static IStatus doBatTask(final IFile flowFile) {
        ProcessBuilder pb = new ProcessBuilder(flowFile.getLocation().toOSString());

        try {
            final Process p = pb.start();
            /* 標準出力をCoronaコンソールへ */
            Thread outThread = redirect(p.getInputStream(), Encoding.Shift_JIS);
            /* エラー出力をCoronaコンソールへ */
            Thread errThread = redirect(p.getErrorStream(), Encoding.Shift_JIS);
            outThread.start();
            errThread.start();
            try {
                outThread.join();
            } catch (InterruptedException e) {
            }
            try {
                errThread.join();
            } catch (InterruptedException e) {
            }
            //Display.getDefault().asyncExec(new Runnable() {
            //    @Override
            //    public void run() {
            //        IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(IPageLayout.ID_PROJECT_EXPLORER);
            //        if (part instanceof ProjectExplorer) {
            //            ((ProjectExplorer) part).getCommonViewer().refresh();
            //        }
            //    }
            //});
            if (p.exitValue() > 0) {
                final String message = String.valueOf(p.exitValue()) + Messages.AutoScheduleJob_errCancelJob;
                return new Status(IStatus.ERROR, CorrectionActivator.PLUGIN_ID, message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Status(IStatus.ERROR, CorrectionActivator.PLUGIN_ID, e.getLocalizedMessage(), e);
        }
        return Status.OK_STATUS;
    }


    private static Thread redirect(final InputStream in, final Encoding charset) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                /* Memo FindBugs無視。bw を閉じてしまうと、Coronaコンソールが閉じてしまう */
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(CoronaActivator.getDefault().getLogger().getOutStream()));
                BufferedInputStream is = new BufferedInputStream(in);
                byte[] buf = new byte[512];
                try {
                    int n;
                    while ((n = is.read(buf)) != -1) {
                        if (n > 0) {
                            String message = new String(buf, 0, n, charset.toString());
                            bw.write(message);
                        }
                    }
                    is.close();
                    /* ここでクローズすると、Coronaコンソールが閉じてしまうので、閉じずに flush だけする */
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public long getInterval() {
        return computeInterval(schedule);
    }


    /**
     * 指定されたスケジュールを実行するまでの待ち時間を返す。
     * 
     * @param schedule
     *            待ち時間を算出したいスケジュール
     * @return スケジュールを実行するまでの待ち時間
     */
    public static final long computeInterval(AutoSchedule schedule) {
        Calendar now = Calendar.getInstance(Locale.JAPAN);
        Calendar next = schedule.getNextRunDate();
        if ((next.getTimeInMillis() - now.getTimeInMillis()) < 0) {
            next = schedule.updateNextRunDate();
        }
        return next.getTimeInMillis() - now.getTimeInMillis();
    }
}
