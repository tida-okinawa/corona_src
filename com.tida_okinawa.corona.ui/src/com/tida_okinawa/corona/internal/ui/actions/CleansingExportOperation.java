/**
 * @version $Id: CleansingExportOperation.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/29 14:45:14
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.component.EditableTable;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.ui.editors.ResultPatternEditorInput;

/**
 * @author kousuke-morishima
 */
public class CleansingExportOperation extends AbstractOperation {

    private IUIWork[] subjects = null;
    private File exportDir;

    boolean canceled;


    /**
     * コンストラクタ
     */
    public CleansingExportOperation() {
        super("クレンジング処理エクスポート");
    }


    /**
     * @param subjects
     *            処理対象
     * @param exportDir
     *            エクスポートディレクトリ
     * @param monitor
     *            進捗ダイアログ
     * @return 処理結果
     * @throws ExecutionException
     *             処理中に発生した、予期せぬException
     */
    public IStatus execute(IUIWork[] subjects, File exportDir, IProgressMonitor monitor) throws ExecutionException {
        this.subjects = subjects;
        this.exportDir = exportDir;
        return execute(monitor, null);
    }


    /**
     * こちらではなく、{@link #execute(IUIWork[], File, IProgressMonitor)}を呼び出してください。
     */
    @Override
    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        IStatus result;
        Assert.isTrue((subjects != null) && (exportDir != null)); // Assert
        MultiStatus statusList = new MultiStatus(IoActivator.PLUGIN_ID, Status.ERROR, "エクスポートに失敗しました", new Exception("各ファイルの失敗理由:"));

        monitor.beginTask(getLabel(), subjects.length + 1);
        monitor.subTask("ファイルチェック");

        final Map<File, IUIWork> exportFiles = createExportFiles(subjects, exportDir);
        final List<File> exists = checkExistsFile(exportFiles);
        if (!exists.isEmpty()) {
            Display display = Display.getCurrent();
            display = (display == null) ? Display.getDefault() : display;
            if (display.getThread() == Thread.currentThread()) {
                renameExistsFile(exportFiles, exists);
            } else {
                display.syncExec(new Runnable() {
                    @Override
                    public void run() {
                        renameExistsFile(exportFiles, exists);
                    }
                });
            }
        }
        if (canceled) {
            monitor.setCanceled(true);
            return Status.CANCEL_STATUS;
        }
        monitor.worked(1);

        for (Entry<File, IUIWork> e : exportFiles.entrySet()) {
            IClaimWorkData work = e.getValue().getObject();
            if (ClaimWorkDataType.RESLUT_PATTERN.equals(work.getClaimWorkDataType())) {
                ResultPatternEditorActionDelegate action = new ResultPatternEditorActionDelegate();
                ResultPatternEditorInput input = new ResultPatternEditorInput("", (IClaimWorkPattern) work);
                result = action.run(input, e.getKey(), new SubProgressMonitor(monitor, 1));
                if (result.getSeverity() == Status.ERROR) {
                    statusList.add(result);
                }
            } else {
                // TODO まだほかのエクスポート処理はできない
                monitor.worked(1);
            }
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
        }

        monitor.done();

        return statusList;
    }


    /**
     * エクスポート先ファイル名の一覧
     * 
     * @param uiWorks
     *            エクスポート対象の処理結果
     * @param exportDir
     *            出力先ディレクトリ
     * @return 処理結果と出力先ファイルのマップ
     */
    private static Map<File, IUIWork> createExportFiles(IUIWork[] uiWorks, File exportDir) {
        Map<File, IUIWork> newFiles = new HashMap<File, IUIWork>(uiWorks.length);
        for (IUIWork uiWork : uiWorks) {
            StringBuilder fileName = new StringBuilder(32);
            fileName.append(uiWork.getResource().getProject().getName()).append("_");
            fileName.append(CoronaModel.INSTANCE.getUIContainer(IUIProduct.class, uiWork.getParent()).getResource().getName()).append("_");
            fileName.append(uiWork.getResource().getName());
            fileName.append(".csv");
            newFiles.put(new File(exportDir, fileName.toString()), uiWork);
        }
        return newFiles;
    }


    /**
     * すでに存在する出力先ファイルを返す
     * 
     * @param files
     *            出力先ファイルと処理結果のマップ
     * @return 既存ファイルと重複するファイルの一覧
     */
    private static List<File> checkExistsFile(Map<File, IUIWork> files) {
        final List<File> exists = new ArrayList<File>();
        for (Entry<File, IUIWork> e : files.entrySet()) {
            if (e.getKey().exists()) {
                exists.add(e.getKey());
            }
        }
        return exists;
    }


    /* ********************
     * 出力先変更
     */
    void renameExistsFile(Map<File, IUIWork> original, List<File> existFiles) {
        List<File> files = openFilenameEditDialog(existFiles);
        if (!files.isEmpty()) {
            for (int i = 0; i < existFiles.size(); i++) {
                IUIWork uiWork = original.remove(existFiles.get(i));
                original.put(files.get(i), uiWork);
            }
        }
    }


    /**
     * 渡されたファイル一覧のリネーム結果を返す。
     * 戻り値と引数のファイルは、添え字で対応している
     * 
     * @param existsFiles
     *            リネームしたいファイルの一覧
     * @return 修正後のファイル一覧。キャンセルの場合は空リスト。
     */
    List<File> openFilenameEditDialog(List<File> existsFiles) {
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


    static class EditDialog extends Dialog {
        protected EditDialog(Shell parentShell) {
            super(parentShell);
        }


        public void setElement(List<File> input) {
            this.input = input;
            modifyMap = new HashMap<File, String>(input.size());
            for (File file : input) {
                modifyMap.put(file, null);
            }
        }

        List<File> input = null;
        List<File> results = null;


        @Override
        protected void okPressed() {
            results = new ArrayList<File>(modifyMap.size());
            for (File file : input) {
                if (modifyMap.get(file) != null) {
                    results.add(new File(file.getParentFile(), modifyMap.get(file)));
                } else {
                    results.add(file);
                }
            }
            super.okPressed();
        };


        /**
         * @return ファイル名を書き換えたあとのファイル一覧。変更されなかったものも含まれる。並び順はsetElementの引数と同じ。
         */
        public List<File> getResults() {
            return results;
        }

        /* ****************************************
         * UI
         */
        EditableTable editTable = null;
        Map<File, String> modifyMap = new HashMap<File, String>();


        @Override
        protected Control createDialogArea(Composite parent) {
            String[] props = new String[] { CellModifier.PROP_NAME, CellModifier.PROP_PATH };
            int[] widths = new int[] { 300, 450 };
            parent = CompositeUtil.defaultComposite(parent, 1);
            editTable = new EditableTable(parent, props, widths, "同名のファイルが存在します。\n上書きしない場合はファイル名を修正してください。");
            editTable.setEditable(new boolean[] { true, false });
            editTable.setLabelProvider(new TableLabelProvider());
            editTable.setCellModifier(new CellModifier());
            editTable.setInput(modifyMap.keySet());
            editTable.setFocus();
            editTable.setSelection(0);

            return parent;
        }


        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            /* 名称変更でEnterのイベントを取りたいので、デフォルトボタンを無効化 */
            createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        }


        @Override
        protected boolean isResizable() {
            return true;
        }


        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText("ファイル名確認");
        }

        class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }


            @Override
            public String getColumnText(Object element, int columnIndex) {
                File file = (File) element;
                switch (columnIndex) {
                case 0:
                    if (modifyMap.get(file) != null) {
                        return modifyMap.get(file);
                    }
                    return file.getName();
                case 1:
                    return file.getParent();
                }
                return "";
            }
        }

        class CellModifier implements ICellModifier {
            static final String PROP_NAME = "ファイル名";
            static final String PROP_PATH = "ファイルパス";


            @Override
            public void modify(Object element, String property, Object value) {
                File file = (File) ((TableItem) element).getData();
                if (PROP_NAME.equals(property)) {
                    try {
                        // Check filename
                        new File(file.getParent(), property).getCanonicalPath();
                        modifyMap.put(file, (String) value);
                    } catch (IOException e) {
                        System.out.println("invalid file name");
                        e.printStackTrace();
                    }
                }
                editTable.update(file);
            }


            @Override
            public Object getValue(Object element, String property) {
                File file = (File) element;
                if (PROP_NAME.equals(property)) {
                    if (modifyMap.get(file) == null) {
                        return file.getName();
                    } else {
                        return modifyMap.get(file);
                    }
                }
                return "";
            }


            @Override
            public boolean canModify(Object element, String property) {
                return editTable.canModify(element, property);
            }
        }
    }


    /* ****************************************
     * Undo/Redo
     */
    @Override
    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        return null;
    }


    @Override
    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        return null;
    }

}
