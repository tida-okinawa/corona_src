/**
 * @version $Id: AbstractResultPatternExportGraphActionDelegate.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/22 18:10:09
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.dashboard.export;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.tida_okinawa.corona.correction.common.table.Table;
import com.tida_okinawa.corona.correction.common.table.TextCell;
import com.tida_okinawa.corona.dashboard.Messages;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.ICoronaDicPri;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.ui.editors.ResultPatternEditorInput;

/**
 * 構文解析結果を グラフ出力するクラス
 * 
 * @author kousuke-morishima
 */
public abstract class AbstractResultPatternExportGraphActionDelegate implements IEditorActionDelegate {
    private IEditorPart activeEditorPart = null;
    // TODO store in PreferenceStore
    private static final String TemplateFile = "templates" //$NON-NLS-1$
            + "/" + Messages.AbstractResultPatternExportGraphActionDelegate_templateName; //$NON-NLS-1$


    @Override
    public void run(final IAction action) {
        if (!(activeEditorPart.getEditorInput() instanceof ResultPatternEditorInput)) {
            return;
        }

        Shell shell = activeEditorPart.getEditorSite().getShell();
        final File templateFile = new File(TemplateFile);
        if (templateFile.exists()) {
            final File outputFile = FileChooser.getSaveFile(shell, Messages.ResultPatternExportGraph_DialogTitle_OutputFile,
                    "", Messages.AbstractResultPatternExportGraphActionDelegate_extension); //$NON-NLS-1$
            if (outputFile != null) {
                final ResultPatternEditorInput input = (ResultPatternEditorInput) activeEditorPart.getEditorInput();
                Job job = new Job(Messages.ResultPatternExportGraph_JobName_OutputResultPattern) {
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        IStatus result = AbstractResultPatternExportGraphActionDelegate.this.run(input, templateFile, outputFile, monitor);
                        if (result.isOK()) {
                            try {
                                Desktop.getDesktop().open(outputFile);
                            } catch (IOException e) {
                                MessageDialog.openWarning(new Shell(), Messages.ResultPatternExportGraph_DialogTitle_FailedOpenFile, e.getLocalizedMessage());
                                e.printStackTrace();
                            }
                        }
                        return result;
                    }
                };
                job.setUser(true);
                job.setSystem(false);
                job.schedule();
            }
        } else {
            MessageDialog.openError(shell, Messages.ResultPatternExportGraph_DialogTitle_FailedGetTemplateFile,
                    Messages.bind(Messages.ResultPatternExportGraph_ErrorMessage_FileNotFound, templateFile.getAbsolutePath()));
        }
    }


    /**
     * エクスポート処理を行い、その間進捗ダイアログを表示する。<br />
     * プログレスモニタが起動していない場合、Job ダイアログで進捗を表示する。
     * 
     * @param input
     *            エクスポートする構文解析結果
     * @param templateFile
     *            グラフテンプレートファイル
     * @param outputFile
     *            エクスポートファイル名
     * @param monitor
     *            プログレスモニターのインスタンス
     * @return 処理結果
     */
    public IStatus run(ResultPatternEditorInput input, File templateFile, File outputFile, IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        return doExport(input, templateFile, outputFile, monitor);
    }


    IStatus doExport(ResultPatternEditorInput input, File templateFile, File outputFile, IProgressMonitor monitor) {
        monitor.beginTask(Messages.ResultPatternExportGraph_TaskName_ExportGraph, 2);

        Table table = generate(input, new SubProgressMonitor(monitor, 1, 0));

        /* ユーザ入力項目をセット */
        IClaimData claim = IoActivator.getService().getClaimData(input.getClaimWorkPattern().getClaimId());

        if (table != null) {

            int tableRow = table.getRowSize();
            String[][] recordId = table.expand();

            for (int i = 1; i < tableRow; i++) {

                String strId = recordId[i][3];
                IRecord recrdItem = claim.getRecord(Integer.parseInt(strId));
                List<IField> fields = recrdItem.getFields();

                /* 項目をセット */
                for (IField field : fields) {
                    if (field.getId() != claim.getDispIdField()) {
                        table.get(i).add(new TextCell((String) field.getValue()));
                    }
                }
            }

            List<IFieldHeader> headers = claim.getFieldInformations();

            /* ヘッダ情報の取得後、追加項目セット */
            for (IFieldHeader header : headers) {
                if (header.getId() != claim.getDispIdField()) {
                    table.get(0).add(new TextCell(header.getDispName()));
                }
            }
        }
        try {

            if (table != null) {
                /* 解析結果の出力 */
                table.writeXlsx(templateFile, outputFile, new SubProgressMonitor(monitor, 1, 0));
                /* 追加情報の出力 */
                IClaimWorkPattern work = input.getClaimWorkPattern();
                ICoronaProject project = IoActivator.getService().getProject(work.getProjectId());
                String projectName = ""; //$NON-NLS-1$
                String targetName = ""; //$NON-NLS-1$
                if (project != null) {
                    projectName = project.getName();
                    for (ICoronaProduct product : project.getProducts()) {
                        if (product.getId() == work.getProductId()) {
                            targetName = product.getName();
                            break;
                        }
                    }
                }
                work.getProjectId();
                List<ICoronaDicPri> dics = work.getDicPrioritysCom();
                StringBuilder userDics = new StringBuilder(128);
                StringBuilder labelDics = new StringBuilder(128);
                StringBuilder patternDics = new StringBuilder(128);
                for (ICoronaDicPri dic : dics) {
                    if (!dic.isInActive()) {
                        DicType type = IoActivator.getDicUtil().getDicType(dic.getDicId());
                        switch (type) {
                        case CATEGORY:
                        case COMMON:
                        case SPECIAL:
                            userDics.append(getDicName(dic.getDicId())).append(Messages.ResultPatternExportGraph_Delimiter);
                            break;
                        case LABEL:
                            labelDics.append(getDicName(dic.getDicId())).append(Messages.ResultPatternExportGraph_Delimiter);
                            break;
                        case PATTERN:
                            patternDics.append(getDicName(dic.getDicId())).append(Messages.ResultPatternExportGraph_Delimiter);
                            break;
                        default:
                            break;
                        }
                    }
                }
                if (userDics.length() > 0) {
                    userDics.deleteCharAt(userDics.length() - 1);
                }
                if (patternDics.length() > 0) {
                    patternDics.deleteCharAt(patternDics.length() - 1);
                }
                if (labelDics.length() > 0) {
                    labelDics.deleteCharAt(labelDics.length() - 1);
                }

                table.writeSummary(outputFile, projectName, targetName, work.getLasted(), work.getClaimWorkDatas().size(), work.getClaimWorkPatterns().size(),
                        userDics.toString(), patternDics.toString(), labelDics.toString(), monitor);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, Messages.bind(Messages.ResultPatternExportGraph_ErrorMessage_FailedExportGraph,
                    outputFile.getAbsolutePath()), e);
        } finally {
            monitor.done();
        }

        return Status.OK_STATUS;
    }


    private static final String getDicName(int dicId) {
        ICoronaDic dic = IoActivator.getService().getDictionary(dicId);
        if (dic != null) {
            return dic.getName();
        }
        return ""; //$NON-NLS-1$
    }


    /**
     * 構文解析結果をグラフ出力用テーブルに変換する</br>
     * 継承先でフォーマットを決定する
     * 
     * @param input
     *            エクスポートする構文解析結果
     * @param monitor
     *            進捗ダイアログ
     * @return input を変換したグラフ出力用テーブル
     */
    protected abstract Table generate(ResultPatternEditorInput input, IProgressMonitor monitor);


    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }


    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        activeEditorPart = targetEditor;
    }

}
