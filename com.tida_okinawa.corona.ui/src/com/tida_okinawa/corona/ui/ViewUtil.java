/**
 * @version $Id: ViewUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/24 10:02:25
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.progress.UIJob;

import com.tida_okinawa.corona.ui.views.db.DataBaseView;

/**
 * Viewに対して操作するためのユーティリティクラス。UIスレッドからのアクセスを前提としている。
 * 
 * @author kousuke-morishima
 */
public final class ViewUtil {

    /**
     * ProjectExplorerをリフレッシュする。
     * UIスレッド以外からのアクセスの場合、何もしない。
     * 
     * @param delay
     */
    public static void refreshProjectExplorer(long delay) {
        final IWorkbenchWindow window = getWindowWeak();
        if (window == null) {
            return;
        }
        run(new Runnable() {
            @Override
            public void run() {
                ProjectExplorer expl = (ProjectExplorer) window.getActivePage().findView(IPageLayout.ID_PROJECT_EXPLORER);
                if (expl != null) {
                    expl.getCommonViewer().refresh();
                }
            }
        }, "Refresh ProjectExplorer", delay);
    }


    /**
     * DataBaseViewをリフレッシュする。
     * UIスレッド以外からのアクセスの場合、何もしない。
     * 
     * @param delay
     */
    public static void refreshDatabaseView(long delay) {
        final IWorkbenchWindow window = getWindowWeak();
        if (window == null) {
            return;
        }
        run(new Runnable() {
            @Override
            public void run() {
                DataBaseView dbv = (DataBaseView) window.getActivePage().findView(DataBaseView.VIEW_ID);
                if (dbv != null) {
                    dbv.refreshView();
                }
            }
        }, "Refresh DatabaseView", delay);
    }


    /**
     * convenient method
     * 
     * @param viewId
     * @return
     */
    public static IViewPart findView(String viewId) {
        IWorkbenchWindow window = getWindowWeak();
        if (window == null) {
            return null;
        }
        return window.getActivePage().findView(viewId);
    }


    /**
     * @return このメソッドを呼び出したスレッドが、Displayを持つUIスレッドならtrue
     */
    public static boolean isUIThread() {
        return (Display.findDisplay(Thread.currentThread()) != null);
    }


    /* ****************************************
     * package
     */
    /**
     * @return 現在アクティブなWindowのShell。UIスレッド以外からの呼び出しの場合はnull。
     */
    static Display getDisplay() {
        Shell shell = getShell();
        if (shell == null) {
            return null;
        }
        return shell.getDisplay();
    }


    /**
     * @return 現在のWindowのShell。UIスレッド以外からの呼び出しの場合はnull。
     */
    static Shell getShell() {
        IWorkbenchWindow window = getWindowWeak();
        if (window == null) {
            return null;
        }
        return window.getShell();
    }


    /**
     * このメソッドは、UIスレッド以外からのアクセスでもActiveWorkbenchWindowを返すことができる。
     * 
     * @return may be null。
     */
    static IWorkbenchWindow getWindow() {
        final IWorkbenchWindow[] win = new IWorkbenchWindow[1];
        win[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (win[0] == null) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    win[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                }
            });
        }
        return win[0];
    }


    /* ****************************************
     * private
     */
    /**
     * このメソッドは、UIスレッド以外からのアクセスではWindowを返せない。
     * 
     * @return may be null
     */
    private static IWorkbenchWindow getWindowWeak() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }


    /**
     * UIスレッドで、delayを持たせて処理を実行させる
     * 
     * @param run
     *            実行する処理
     * @param jobName
     * @param delay
     */
    private static void run(final Runnable run, String jobName, long delay) {
        Job uiJob = new UIJob(jobName) {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                run.run();
                return Status.OK_STATUS;
            }
        };
        uiJob.setUser(false);
        uiJob.setSystem(true);
        uiJob.schedule(delay);
    }

}
