/**
 * @version $Id: CollocationExtractSequence.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/28 11:00:14
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.collocation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.correction.CorrectionActivator;
import com.tida_okinawa.corona.correction.common.ExternalProgramExec;
import com.tida_okinawa.corona.correction.common.ExternalProgramExitException;
import com.tida_okinawa.corona.correction.external.ExternalActivator;

/**
 * @author s.takuro
 */
public class CollocationExtractSequence {
    final static int HIT_NUM = 2;
    final static int ERROR_STATE_EXCEPTION = 0;
    final static int ERROR_STATE_EMPTY = 1;
    final static int ERROR_STATE_FAIL = 2;
    final static int ERROR_STATE_TMT_SYSTEM = 3;
    final static int ERROR_STATE_TMT_OOM = 4;
    int errorState;
    List<String> extractResult = new ArrayList<String>();


    /**
     * 誤記補正結果より連語の抽出処理を行う
     * 
     * @param misstakesList
     *            誤記補正結果レコードリスト
     * @param cooccurrenceNum
     *            共起語の最大数
     * @param isOrderCooccurrence
     *            共起順を考慮するかどうか
     * @return TMT処理結果 List<String>
     * @throws InterruptedException
     *             例外
     * @throws InvocationTargetException
     *             例外
     */
    public List<String> exec(final List<String> misstakesList, final int cooccurrenceNum, final boolean isOrderCooccurrence) throws InvocationTargetException,
            InterruptedException {
        errorState = -1;

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final Shell shell = window.getShell();
        ProgressMonitorDialog dialog1 = new ProgressMonitorDialog(shell);
        dialog1.run(true, false, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                // TMT実行コマンドを取得
                monitor.beginTask(Messages.COLLOCATION_PROGRES_BEGINTASK_COLLOCATION, 3);
                monitor.subTask(Messages.COLLOCATION_PROGRESS_SUBTASK_TMT);
                String[] tmtCmd = getTmtCmdLine(cooccurrenceNum, isOrderCooccurrence);
                monitor.worked(1);

                // すべてのレコードを1つにまとめる
                // TODO 区切り基準をユーザ指定できるようにしたい
                final StringBuffer records = new StringBuffer(100);
                for (String record : misstakesList) {
                    //records.append(record).append("#EOB"); //$NON-NLS-1$
                    record = record.replace("。", "。#EOB").replace("？", "？#EOB"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    records.append(record);
                    if (!record.endsWith("#EOB")) { //$NON-NLS-1$
                        records.append("#EOB"); //$NON-NLS-1$
                    }
                }
                monitor.worked(1);

                // TMTを実行する
                try {
                    extractResult = tmtExec(records, tmtCmd, monitor);
                    if ((errorState == -1) && (extractResult.size() == 0)) {
                        errorState = ERROR_STATE_EMPTY;
                    }
                } catch (ExternalProgramExitException e) {
                    // エラーが起きて結果が空のときに、errorStateが上書きされるのを防ぐためにcatch
                    e.printStackTrace();
                }
                monitor.done();
            }
        });

        switch (errorState) {
        case ERROR_STATE_EXCEPTION:
            dispMessage(shell, Messages.COLLOCATION_MESSAGE_BOX_TITLE, Messages.COLLOCATION_MESSAGE_BOX_EXCEPTION_ERROR_TEXT);
            break;
        case ERROR_STATE_FAIL:
            dispMessage(shell, Messages.COLLOCATION_MESSAGE_BOX_TITLE, Messages.COLLOCATION_MESSAGE_BOX_TMT_ERROR_TEXT);
            break;
        case ERROR_STATE_EMPTY:
            dispMessage(shell, Messages.COLLOCATION_MESSAGE_BOX_TITLE, Messages.COLLOCATION_MESSAGE_BOX_EMPTY_ERROR_TEXT);
            break;
        case ERROR_STATE_TMT_OOM:
            dispMessage(shell, Messages.COLLOCATION_MESSAGE_BOX_TITLE, Messages.COLLOCATION_ErrorMessage_TMT_OutOfMemory);
            break;
        case ERROR_STATE_TMT_SYSTEM:
            dispMessage(shell, Messages.COLLOCATION_MESSAGE_BOX_TITLE, Messages.COLLOCATION_ErrorMessage_TMT_NotInstalled_DotNetFramework4);
            break;
        default:
            break;
        }
        return extractResult;
    }


    /**
     * TMT実行コマンドを取得するメソッド
     * 
     * @param cooccurrenceNum
     *            共起語の最大数
     * @param isOrderCooccurrence
     *            共起順を考慮するかどうか
     * @return TMTコマンドを返却String[]、格納先がない場合null
     */
    static String[] getTmtCmdLine(int cooccurrenceNum, boolean isOrderCooccurrence) {
        Plugin plugin = ExternalActivator.getDefault();
        if (plugin == null) {
            return new String[] {};
        }
        String filePath = null;
        try {
            // Coronaツール格納先を取得
            URL url = FileLocator.toFileURL(plugin.getBundle().getEntry("")); //$NON-NLS-1$
            File file = new File(url.toURI());
            filePath = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        // TMTのexeをファイルパスへ追加
        String tmtPaht = filePath + Messages.COLLOCATION_TMT_EXECUTE_COMMAND;
        String tmtKeyPath = filePath + Messages.COLLOCATION_TMT_EXECUTE_KEY_FILE;
        // 外部コマンドを作成
        String[] tmtCmd;
        if (isOrderCooccurrence == true) {
            tmtCmd = new String[] { tmtPaht, Messages.COLLOCATION_TMT_EXECUTE_OPTION_R, Messages.COLLOCATION_TMT_EXECUTE_OPTION_M + cooccurrenceNum,
                    Messages.COLLOCATION_TMT_EXECUTE_OPTION_N + HIT_NUM, Messages.COLLOCATION_TMT_EXECUTE_OPTION_3 + tmtKeyPath };
        } else {
            tmtCmd = new String[] { tmtPaht, Messages.COLLOCATION_TMT_EXECUTE_OPTION_R, Messages.COLLOCATION_TMT_EXECUTE_OPTION_M + cooccurrenceNum,
                    Messages.COLLOCATION_TMT_EXECUTE_OPTION_N + HIT_NUM, Messages.COLLOCATION_TMT_EXECUTE_OPTION_3 + tmtKeyPath,
                    Messages.COLLOCATION_TMT_EXECUTE_OPTION_S };
        }
        return tmtCmd;

    }


    /*
     * TMT実行処理
     */
    List<String> tmtExec(StringBuffer records, String[] tmtCmd, IProgressMonitor monitor) throws ExternalProgramExitException {
        List<String> tmtResultList = new ArrayList<String>();
        ExternalProgramExec externalProgramExec = new ExternalProgramExec();
        byte[] inputBytes;
        try {
            inputBytes = records.toString().getBytes(Encoding.MS932.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return tmtResultList;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BufferedReader br = null;
        try {
            externalProgramExec.setOutputTimeout(0);
            externalProgramExec.setErrorTimeout(0);
            externalProgramExec.exec(tmtCmd, null, input, output, err);
            br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.toByteArray()), Encoding.MS932.toString()));

            String tmtResult;
            while ((tmtResult = br.readLine()) != null) {
                tmtResultList.add(tmtResult);
            }
            monitor.worked(1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            errorState = ERROR_STATE_EXCEPTION;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExternalProgramExitException e) {
            try {
                String message = new String(err.toByteArray(), Encoding.Shift_JIS.toString());
                if (message.startsWith("[E902]")) { //$NON-NLS-1$
                    message = Messages.COLLOCATION_ErrorMessage_TMT_OutOfMemory;
                    errorState = ERROR_STATE_TMT_OOM;
                } else if (message.startsWith("[E901]")) { //$NON-NLS-1$
                    errorState = ERROR_STATE_TMT_SYSTEM;
                    message = new StringBuilder(message.length() + 64).append(Messages.COLLOCATION_ErrorMessage_TMT_NotInstalled_DotNetFramework4).append('\n')
                            .append(message).toString();
                }
                CoronaActivator.log(new Status(IStatus.ERROR, CorrectionActivator.PLUGIN_ID, message), false);
            } catch (UnsupportedEncodingException e1) {
            }
            throw e;
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
            errorState = ERROR_STATE_FAIL;
        } finally {
            try {
                output.close();
                input.close();
                err.close();
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tmtResultList;
    }


    /*
     * エラーメッセージを表示させるメソッド
     */
    private static void dispMessage(Shell shell, String title, String text) {
        MessageBox message = new MessageBox(shell);
        message.setText(title);
        message.setMessage(text);
        message.open();
    }
}
