/**
 * @version $Id: AbstractResultPatternExportActionDelegate.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/02 17:01:09
 * @author miyaguni
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.tida_okinawa.corona.correction.common.table.Table;
import com.tida_okinawa.corona.internal.ui.util.StringUtil;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.ui.editors.ResultPatternEditorInput;

/**
 * 構文解析結果を CSV ファイルに出力する editorAction クラス
 * 
 * @author miyaguni
 * 
 */
public abstract class AbstractResultPatternExportActionDelegate implements IEditorActionDelegate {
    private IEditorPart activeEditorPart = null;


    @Override
    public void run(final IAction action) {
        if (!(activeEditorPart.getEditorInput() instanceof ResultPatternEditorInput))
            return;

        Shell shell = activeEditorPart.getEditorSite().getShell();
        final File file = FileChooser.selectFile(shell, "出力先を選択", "result.csv");
        if (file != null) {
            final ResultPatternEditorInput input = (ResultPatternEditorInput) activeEditorPart.getEditorInput();
            run(input, file, null);
        }
    }


    /**
     * エクスポート処理を行い、その間進捗ダイアログを表示する。<br />
     * プログレスモニタが起動していない場合、Job ダイアログで進捗を表示する。
     * 
     * @param input
     *            エクスポートする構文解析結果
     * @param exportFile
     *            エクスポートファイル名
     * @param monitor
     *            プログレスモニターのインスタンス
     * @return
     */
    public IStatus run(final ResultPatternEditorInput input, final File exportFile, IProgressMonitor monitor) {
        if ((monitor == null) || (monitor instanceof NullProgressMonitor)) {
            Job writeJob = new Job("CSV出力") {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    return doExport(input, exportFile, monitor);
                }
            };
            writeJob.setUser(true);
            writeJob.schedule();
            try {
                writeJob.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return writeJob.getResult();
        } else {
            return doExport(input, exportFile, monitor);
        }
    }


    IStatus doExport(ResultPatternEditorInput input, File exportFile, IProgressMonitor monitor) {
        Table table = generate(input, monitor);
        monitor.worked(1);

        try {
            if (table != null) {
                /**
                 * デバイス名などの予約語、ディレクトリ名など
                 * ファイルとして保存できないパスが指定されてる場合は例外を投げる
                 * 
                 * TODO 総パス長が OS の制限にかかる場合は何も対策していない。
                 */
                if (!StringUtil.isValidFileName(exportFile.getName())) {
                    throw new Exception(exportFile.getAbsolutePath() + " このファイル名では保存できません。");
                }
                table.write(exportFile, monitor);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, "CSV出力に失敗しました", e);
        } finally {
            monitor.done();
        }

        return Status.OK_STATUS;
    }


    /**
     * 構文解析結果を CSV 出力用テーブルに変換して返す。<br/>
     * 継承先でフォーマットを決定する
     * 
     * @param input
     *            エクスポートする構文解析結果
     * @param monitor
     *            プログレスモニターのインスタンス
     * @return input を変換した CSV 出力用テーブル
     */
    abstract protected Table generate(ResultPatternEditorInput input, IProgressMonitor monitor);


    @Override
    public void selectionChanged(IAction action, ISelection selection) {

    }


    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        activeEditorPart = targetEditor;
    }

    /**
     * ファイルダイアログで、出力ファイルを取得
     * 
     * 他にも使うようなら、クラスとして独立
     */
    static class FileChooser {
        File file;


        /**
         * ダイアログでファイルを選択する
         * 
         * @param shell
         * @param title
         * @param defaultFileName
         * @return File null:キャンセルした
         */
        static public File selectFile(final Shell shell, final String title, final String defaultFileName) {
            FileChooser fc = new FileChooser();
            return fc.getFile(shell, title, defaultFileName);
        }


        File getFile(final Shell shell, final String title, final String defaultFileName) {
            Display display = shell.getDisplay();
            display.syncExec(new Runnable() {

                @Override
                public void run() {
                    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
                    dialog.setFileName(defaultFileName);
                    dialog.setText(title);
                    String fileName = dialog.open();
                    if (fileName == null) {
                        // キャンセル
                        file = null;
                        ;
                        return;
                    }
                    file = new File(fileName);
                    if (file.isFile()) {
                        if (!MessageDialog.openConfirm(shell, "上書き確認", fileName + "はすでに存在します。上書きしますか")) {
                            file = null;
                            ;
                            return;
                        }
                    }

                }
            });
            return file;
        }
    }
}
