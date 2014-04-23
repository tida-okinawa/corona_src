/**
 * @version $Id: CorrectionStringList.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/05 13:30:08
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.collocation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.correction.common.ExternalProgramExitException;
import com.tida_okinawa.corona.correction.morphem.MorphemeRelationProcessor;
import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;

/**
 * @author s.takuro
 * 
 */
public class CorrectionStringList {

    List<String> jumanResult = new ArrayList<String>();


    /**
     * 文字列にJuman実行する
     * 
     * @param stringList
     *            文字列リスト
     * @return Juman処理結果
     * @throws InvocationTargetException
     *             例外
     * @throws InterruptedException
     *             例外
     */
    public List<String> exec(final List<String> stringList) throws InvocationTargetException, InterruptedException {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final Shell shell = window.getShell();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        dialog.run(true, false, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask(Messages.COLLOCATION_PROGRES_BEGINTASK_COLLOCATION, 3);
                monitor.subTask(Messages.COLLOCATION_PROGRESS_SUBTASK_JUMAN);
                jumanResult = jumanExec(stringList, monitor);
                monitor.done();
            }
        });
        return jumanResult;
    }


    /**
     * Juman実行処理
     * 
     * @param stringList
     *            文字列リスト
     * @param monitor
     *            プログレス・モニタ
     * @return 実行結果
     */
    public List<String> jumanExec(List<String> stringList, IProgressMonitor monitor) {

        byte[] inputBytes;
        List<String> jumanResultList = new ArrayList<String>();
        boolean convSJIS = MorphemePreference.convSJIS();
        MorphemeRelationProcessor morphRelationProcesser = new MorphemeRelationProcessor(0);
        StringBuilder tmtResults = new StringBuilder(100);

        for (String str : stringList) {
            tmtResults.append(str).append("\n"); //$NON-NLS-1$
        }
        monitor.worked(1);

        if (convSJIS) {
            try {
                inputBytes = tmtResults.toString().getBytes(Encoding.MS932.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return jumanResultList;
            }
        } else {
            inputBytes = tmtResults.toString().getBytes();
        }
        monitor.worked(1);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BufferedReader br = null;
        try {
            morphRelationProcesser.exec(input, output, err, false);
            if (convSJIS) {
                br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.toByteArray()), Encoding.MS932.toString()));
            } else {
                br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.toByteArray())));
            }
            String jumanResult;
            String jumanResults = null;
            while ((jumanResult = br.readLine()) != null) {
                if (jumanResult.equals("EOS")) { //$NON-NLS-1$
                    jumanResultList.add(jumanResults);
                    jumanResults = null;
                } else {
                    if (jumanResults == null) {
                        jumanResults = jumanResult;
                    } else {
                        StringBuilder strBuf = new StringBuilder(100);
                        strBuf.append(jumanResults).append(",").append(jumanResult); //$NON-NLS-1$
                        jumanResults = strBuf.toString();
                    }
                }
            }
        } catch (IOException | InterruptedException | ExternalProgramExitException e) {
            e.printStackTrace();
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
        monitor.worked(1);
        return jumanResultList;
    }
}
