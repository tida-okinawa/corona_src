/**
 * @version $Id: AbstractClaimWorkDataExportHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/16 14:48:10
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.internal.ui.actions.AbstractResultPatternExportActionDelegate;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.ui.editors.ResultPatternEditorInput;


/**
 * @author shingo-takahashi
 */
public abstract class AbstractClaimWorkDataExportHandler extends AbstractHandler {
    private IStructuredSelection selection;
    private IWorkbenchWindow window;
    protected Shell shell;


    @Override
    public Object execute(ExecutionEvent event) {
        IClaimWorkData work = getClaimWorkData();
        shell = HandlerUtil.getActiveShell(event);
        final File file = FileChooser.selectFile(shell, "出力先を選択", "result.csv");

        if (file == null)
            return false;

        if (ClaimWorkDataType.RESLUT_PATTERN.equals(work.getClaimWorkDataType())) {
            AbstractResultPatternExportActionDelegate action = getResultPatternExportAction();
            ResultPatternEditorInput input = new ResultPatternEditorInput("", (IClaimWorkPattern) work);
            action.run(input, file, null);
        }

        return true;
    }


    /**
     * 現在は構文解析結果だけエクスポートメニューを表示している
     */
    @Override
    public boolean isEnabled() {
        IClaimWorkData work = getClaimWorkData();

        if (work == null)
            return false;

        if (ClaimWorkDataType.RESLUT_PATTERN.equals(work.getClaimWorkDataType())) {
            return true;
        }

        return false;
    }


    /**
     * 選択しているクレンジング結果を返す。<br/>
     * もし「何も選択していない 」or「クレンジング結果以外を選択している」場合は null を返す。
     * 
     * @return クレンジング結果 or null
     */
    final protected IClaimWorkData getClaimWorkData() {
        window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        selection = (IStructuredSelection) window.getActivePage().getSelection();
        if (selection == null || selection.size() != 1) {
            return null;
        }

        Object element = selection.getFirstElement();
        if (element != null && element instanceof IUIWork) {
            return ((IUIWork) element).getObject();
        }

        return null;
    }


    /**
     * 構文解析結果を出力するアクションクラスを取得する。<br/>
     * 継承先で出力したいフォーマットに合わせてアクションクラスを変更する
     * 
     * @return 構文解析結果出力アクションクラス
     */
    protected abstract AbstractResultPatternExportActionDelegate getResultPatternExportAction();

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
                        return;
                    }
                    file = new File(fileName);
                    if (file.isFile()) {
                        if (!MessageDialog.openConfirm(shell, "上書き確認", fileName + "はすでに存在します。上書きしますか")) {
                            file = null;
                            return;
                        }
                    }

                }
            });
            return file;
        }
    }
}
