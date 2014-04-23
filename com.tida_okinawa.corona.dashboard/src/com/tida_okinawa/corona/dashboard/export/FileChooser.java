/**
 * @version $Id: FileChooser.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/22 17:56:32
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.dashboard.export;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * ファイルダイアログで、出力ファイルを取得
 * 
 * @author kousuke-morishima
 */
class FileChooser {
    /**
     * 保存するファイルをダイアログで選択する
     * 
     * @param shell
     *            parent shell
     * @param title
     *            file select dialog title
     * @param defaultFileName
     *            default save file name
     * @param extensions
     *            選択できる拡張子の一覧
     * @return 選択をキャンセルしたらnull
     */
    public static File getSaveFile(Shell shell, String title, final String defaultFileName, String... extensions) {
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        if (extensions.length > 0) {
            dialog.setFilterExtensions(extensions);
        }
        dialog.setOverwrite(true);
        dialog.setFileName(defaultFileName);
        dialog.setText(title);
        String filePath = dialog.open();
        if (filePath == null) {
            return null;
        }

        return new File(filePath);
    }


    /**
     * 保存するファイルをダイアログで選択する
     * 
     * @param shell
     *            parent shell
     * @param title
     *            file select dialog title
     * @param defaultFileName
     *            default save file name
     * @return 選択をキャンセルしたらnull
     */
    public static File getSaveFile(Shell shell, String title, final String defaultFileName) {
        return getSaveFile(shell, title, defaultFileName, "*.*"); //$NON-NLS-1$
    }


    /**
     * 開くファイルをダイアログで選択する
     * 
     * @param shell
     *            parent shell
     * @param title
     *            file select dialog title
     * @param defaultFileName
     *            default save file name
     * @param extensions
     *            選択できる拡張子の一覧
     * @return キャンセルしたり、存在しないファイルを選んだりしたらnull
     */
    public static File getOpenFile(Shell shell, String title, final String defaultFileName, String... extensions) {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFileName(defaultFileName);
        if (extensions.length > 0) {
            dialog.setFilterExtensions(extensions);
        }
        dialog.setText(title);
        String fileName = dialog.open();
        if (fileName == null) {
            return null;
        }

        File file = new File(fileName);
        if (!file.isFile()) {
            return null;
        }
        return file;
    }


    /**
     * 開くファイルをダイアログで選択する
     * 
     * @param shell
     *            parent shell
     * @param title
     *            file select dialog title
     * @param defaultFileName
     *            default save file name
     * @return キャンセルしたり、存在しないファイルを選んだりしたらnull
     */
    public static File getOpenFile(Shell shell, String title, final String defaultFileName) {
        return getOpenFile(shell, title, defaultFileName, "*.*"); //$NON-NLS-1$
    }
}