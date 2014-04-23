/**
 * @version $Id: DicImportFinishProgress.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/12/18 18:41:20
 * @author shingo-kuniyoshi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import static com.tida_okinawa.corona.common.CleansingNameVariable.MISTAKE_CORRECT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.correction.erratum.Erratum;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.XlsToArray;
import com.tida_okinawa.corona.io.command.DicIEConstants;
import com.tida_okinawa.corona.io.command.DicImport;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * 辞書インポート起動クラス
 * 
 * @author shingo-kuniyoshi
 * 
 */
public class DicImportFinishProgress implements IRunnableWithProgress {
    private final String strFileExt;
    private final String categoryName;
    private final String dicName;
    private final String path;
    private final Boolean ignoreLabel;
    private Erratum er = new Erratum();


    /**
     * 辞書種類に対応する編集処理を行い、インポート処理を実行する。
     * 
     * @param categoryName
     *            カテゴリ名
     * @param dicName
     *            辞書名
     * @param path
     *            ファイルパス
     * @param strFileExt
     *            辞書拡張子
     * @param ignoreLabel
     *            ラベル情報インポート有無フラグ
     */
    public DicImportFinishProgress(String categoryName, String dicName, String path, String strFileExt, Boolean ignoreLabel) {
        this.strFileExt = strFileExt;
        this.categoryName = categoryName;
        this.dicName = dicName;
        this.path = path;
        this.ignoreLabel = ignoreLabel;
    }


    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        // パターン辞書のみ補正対象外
        if (DicType.PATTERN.getExtension().equals(strFileExt)) {
            /* インポート処理実行 */
            DicImport imp = new DicImport();
            try {
                imp.import0(path, dicName, null, monitor, ignoreLabel);
            } catch (UnsupportedOperationException | IOException | SQLException e) {
                e.printStackTrace();
            }
            monitor.done();
            return;
        }

        List<String> errs = new ArrayList<String>();
        Exception exception = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        String correctFilePath = ""; //$NON-NLS-1$
        try {
            /* 読み込んだファイルのPathを取得 */
            File inputFile = new File(path);
            /* 拡張子を取得 */
            String ext = path.substring(path.indexOf("."), path.length()); //$NON-NLS-1$

            if (ext.contains(".xls")) {
                // xlsxをCSVに変換
                XlsToArray xArray = new XlsToArray(path);
                ArrayList<String> csvArray = xArray.getData();

                try {
                    // CSVファイル作成
                    File csvFile = File.createTempFile("temp", ext); //$NON-NLS-1$
                    csvFile.deleteOnExit();
                    BufferedWriter writer = null;
                    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile)));
                    for (String str : csvArray) {
                        writer.write(str);
                        writer.newLine();
                    }
                    writer.close();

                    // xlsxファイルの場合、CSV変換後のxlsxファイルパスを指定
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile.getPath())));
                } catch (Exception e) {
                    e.printStackTrace();
                    exception = e;
                }
            } else {

                // CSVファイルの場合、入力ファイルのファイルパスを指定
                br = new BufferedReader(new InputStreamReader(new FileInputStream(path), Encoding.MS932.toString()));

            }

            /* 書き込み用の一時ファイル作成 */
            File workFile = File.createTempFile("Corona", ext); //$NON-NLS-1$
            workFile.deleteOnExit();
            correctFilePath = workFile.getPath();
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(workFile)));

            /***** CSVから読み込んだレコードを誤記補正に掛けた後、DBへ渡す処理 *****/
            monitor.beginTask(Messages.DicImportFinishProgress_monitorPrepare, (int) inputFile.length());
            monitor.subTask(MISTAKE_CORRECT);

            String line = ""; //$NON-NLS-1$
            int count = 0;
            while ((line = br.readLine()) != null) {
                count++;
                /* 空行を読み飛ばす */
                if (line.length() == 0) {
                    continue;
                }
                /* カンマ区切りで分割 */
                String[] wkData = line.split(",", -1); //$NON-NLS-1$

                StringBuilder workLine = new StringBuilder(100);
                /* 見出し語のみ全角へ変換 */
                try {
                    if (DicType.FLUC.getExtension().equals(strFileExt) || DicType.SYNONYM.getExtension().equals(strFileExt)) {
                        wkData[DicIEConstants.DEPEND_HEADER] = er.convert(wkData[DicIEConstants.DEPEND_HEADER]);
                        wkData[DicIEConstants.SYNONYM] = er.convert(wkData[DicIEConstants.SYNONYM]);
                    } else {
                        wkData[DicIEConstants.HEADER] = er.convert(wkData[DicIEConstants.HEADER]);
                    }
                } catch (IndexOutOfBoundsException e) {
                    errs.add((Messages.bind(Messages.DicImportFinishProgress_errLogResult, new String[] { Integer.toString(count) })));
                    CoronaIoUtils.setErrorLogs(IStatus.ERROR, Messages.DicImportFinishProgress_errLogTitle, errs, null);
                    e.printStackTrace();
                }
                for (String wk : wkData) {
                    workLine.append(",").append(wk); //$NON-NLS-1$
                }
                /* 書き込み */
                bw.write(workLine.substring(1));
                bw.newLine();

                monitor.worked(line.length());
                if (monitor.isCanceled()) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                exception = e;
            }

            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                exception = e;
            }
        }

        if (exception != null) {
            throw new InvocationTargetException(exception, Messages.DicImportFinishProgress_errLogPrepareFail);
        }
        if (monitor.isCanceled()) {
            throw new InterruptedException(Messages.DicImportFinishProgress_errLogCancel);
        }

        TextItem category = null;
        if (DicType.CATEGORY.getExtension().equals(strFileExt)) {
            /* 専門辞書の場合 */
            List<TextItem> list = IoActivator.getService().getCategorys();
            for (TextItem item : list) {
                if (item.getText().equals(categoryName)) {
                    category = item;
                    break;
                }
            }
            if (category == null) {
                /* カテゴリを作成 */
                category = IoActivator.getService().createCategory(categoryName);
                list.add(category);
            }
        }

        /* インポート処理実行 */
        DicImport imp = new DicImport();
        try {
            imp.import0(correctFilePath, dicName, category, monitor, ignoreLabel);
        } catch (UnsupportedOperationException | IOException | SQLException e) {
            e.printStackTrace();
        }
        monitor.done();
    }
}
