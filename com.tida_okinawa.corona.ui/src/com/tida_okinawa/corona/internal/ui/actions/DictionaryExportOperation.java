/**
 * @version $Id: DictionaryExportOperation.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/29 14:45:14
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.internal.ui.actions.CleansingExportOperation.EditDialog;
import com.tida_okinawa.corona.internal.ui.util.StringUtil;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.command.DicExport;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDependDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * @author kousuke-morishima
 */
public class DictionaryExportOperation extends AbstractOperation {
    private ICoronaDic[] subjects;
    private File exportDir;
    private Map<ICoronaDic, Set<ILabelDic>> labelMap = null;

    boolean canceled;


    /**
     * コンストラクタ
     */
    public DictionaryExportOperation() {
        super(Messages.dialogLabel);
    }


    /**
     * 辞書をエクスポートする
     * 
     * @param subjects
     *            処理対象
     * @param exportDir
     *            エクスポートディレクトリ
     * @param map
     * @param monitor
     *            進捗ダイアログ
     * @return
     *         処理結果。辞書出力に失敗した場合、それぞれの失敗結果が格納されている
     * @throws ExecutionException
     *             処理中に発生した予期せぬException
     */
    public IStatus execute(ICoronaDic[] subjects, File exportDir, Map<ICoronaDic, Set<ILabelDic>> map, IProgressMonitor monitor) throws ExecutionException {
        this.subjects = subjects;
        this.exportDir = exportDir;
        this.labelMap = map;
        return execute(monitor, null);
    }


    /**
     * こちらではなく、{@link #execute(ICoronaDic[], File, Map, IProgressMonitor)}
     * を呼び出してください。
     * 
     * <p>
     * ファイル名が重複していれば上書き確認のダイアログを表示する
     * </p>
     */
    @Override
    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        DicExport exporter = new DicExport();
        MultiStatus statusList = new MultiStatus(IoActivator.PLUGIN_ID, Status.OK, Messages.errLogExportFail, new Exception(Messages.errLogReason));

        /* 文字コード指定 */
        exporter.setEncode("MS932"); //$NON-NLS-1$

        Assert.isTrue((subjects != null) && (exportDir != null)); // Assert

        monitor.beginTask(getLabel(), subjects.length + 1);
        monitor.subTask(Messages.monitorFileCheck);

        final Map<File, ICoronaDic> exportFiles = createExportFiles(subjects, exportDir);
        final List<File> exists = checkExistsFile(exportFiles);
        if (!exists.isEmpty()) {
            Display display = Display.getCurrent();
            display = (display == null) ? Display.getDefault() : display;
            if (display.getThread() == Thread.currentThread()) {
                existsFileRename(exportFiles, exists);
            } else {
                display.syncExec(new Runnable() {
                    @Override
                    public void run() {
                        existsFileRename(exportFiles, exists);
                    }
                });
            }
        }

        if (canceled) {
            monitor.setCanceled(true);
            return Status.CANCEL_STATUS;
        }

        monitor.worked(1);
        for (Entry<File, ICoronaDic> item : exportFiles.entrySet()) {
            IStatus resultStatus = Status.OK_STATUS;
            ICoronaDic dic = item.getValue();
            File exportFile = item.getKey();
            try {
                /**
                 * デバイス名などの予約語、ディレクトリ名など
                 * ファイルとして保存できないパスが指定されてる場合は例外を投げる
                 * 
                 * TODO 総パス長が OS の制限にかかる場合は何も対策していない。
                 */
                if (!StringUtil.isValidFileName(exportFile.getName())) {
                    throw new Exception(exportFile.getAbsolutePath() + Messages.labelUnsave);
                }
                if (dic instanceof IUserDic) {
                    /* ラベル辞書を指定する */
                    Set<ILabelDic> ldics = labelMap.get(item.getValue());
                    resultStatus = exporter.export(exportFile.getPath(), (IUserDic) dic, ldics, new SubProgressMonitor(monitor, 1));
                } else if (dic instanceof IDependDic) {
                    resultStatus = exporter.export(exportFile.getPath(), dic, new SubProgressMonitor(monitor, 1));
                } else if (dic instanceof IPatternDic) {
                    resultStatus = exporter.export(exportFile.getPath(), dic, new SubProgressMonitor(monitor, 1));
                } else {
                    monitor.worked(1);
                }
            } catch (Exception e) {
                /**
                 * DicImport#export 内で掴まえきれなかったエラーをここでキャッチして
                 * Error Status を作成する
                 */
                resultStatus = new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, Messages.errLogCsvExportFail, e);
            }
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            if (resultStatus.getSeverity() == Status.ERROR) {
                statusList.add(resultStatus);
            }
        }

        monitor.done();
        return statusList;
    }


    /**
     * エクスポート先ファイル名の一覧
     * 
     * @param dics
     * @param exportDir
     * @return エクスポートファイル文字列と辞書オブジェクトを返す
     */
    private static Map<File, ICoronaDic> createExportFiles(ICoronaDic[] dics, File exportDir) {
        Map<File, ICoronaDic> files = new HashMap<File, ICoronaDic>(dics.length);

        for (ICoronaDic dic : dics) {
            /* 辞書種類によって拡張子を変化 */
            int extIndex = dic.getName().lastIndexOf('.');
            String extention = dic.getName().substring(extIndex + 1, dic.getName().length());
            String exportType = null;
            DicType dicType = DicType.valueOfExt(extention);
            if (DicType.PATTERN.equals(dicType)) {
                exportType = ".xml"; //$NON-NLS-1$
            } else {
                exportType = ".xlsx"; //$NON-NLS-1$
            }
            files.put(new File(exportDir + File.separator + dic.getName() + exportType), dic);
        }
        return files;
    }


    /**
     * @param files
     * @return 既存ファイルと重複するファイルの一覧
     */
    private static List<File> checkExistsFile(Map<File, ICoronaDic> files) {
        final List<File> exists = new ArrayList<File>();
        for (Entry<File, ICoronaDic> e : files.entrySet()) {
            if (e.getKey().exists()) {
                exists.add(e.getKey());
            }
        }
        return exists;
    }


    /**
     * 出力先変更
     * 
     * @param original
     * @param existFiles
     */
    void existsFileRename(Map<File, ICoronaDic> original, List<File> existFiles) {
        List<File> files = openFileNameEditDialog(existFiles);
        if (!files.isEmpty()) {
            for (int i = 0; i < existFiles.size(); i++) {
                ICoronaDic dic = original.remove(existFiles.get(i));
                original.put(files.get(i), dic);
            }
        }
    }


    /**
     * @param existsFiles
     * @return 修正後のファイル一覧。キャンセルの場合は空リスト。
     */
    List<File> openFileNameEditDialog(List<File> existsFiles) {
        EditDialog dialog = new EditDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        dialog.setElement(existsFiles);
        int retCode = dialog.open();
        if (retCode == Dialog.CANCEL) {
            canceled = true;
        } else if (retCode == Dialog.OK) {
            return dialog.getResults();
        }
        return new ArrayList<File>(0);
    }


    @Override
    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        return null;
    }


    @Override
    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        return null;
    }

}
