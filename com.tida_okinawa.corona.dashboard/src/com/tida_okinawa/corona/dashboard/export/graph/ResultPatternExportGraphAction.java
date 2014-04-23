/**
 * @version $Id: ResultPatternExportGraphAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/22 17:09:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.dashboard.export.graph;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.correction.common.table.Row;
import com.tida_okinawa.corona.correction.common.table.Table;
import com.tida_okinawa.corona.dashboard.Messages;
import com.tida_okinawa.corona.dashboard.export.AbstractResultPatternExportGraphActionDelegate;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.ui.editors.ResultPatternEditorInput;

/**
 * @author kousuke-morishima
 * 
 */
public class ResultPatternExportGraphAction extends AbstractResultPatternExportGraphActionDelegate {
    private static final String Label_PatternType = Messages.ResultPatternExportGraphAction_ColumnName_PatternType;
    private static final String Label_PatternName = Messages.ResultPatternExportGraphAction_ColumnName_PatternName;
    private static final String Label_Text = Messages.ResultPatternExportGraphAction_ColumnName_Text;
    private static final String Label_RecordId = Messages.ResultPatternExportGraphAction_ColumnName_RecordId;


    /**
     * <pre>
     * フォーマット
     * 
     * 区分,パターン,テキスト,レコードID
     * </pre>
     */
    @Override
    protected Table generate(ResultPatternEditorInput input, IProgressMonitor monitor) {
        Table table = new Table(new Row(Label_PatternType, Label_PatternName, Label_Text, Label_RecordId));
        PatternType[] patternTypes = input.getElements();

        monitor.beginTask(Messages.ResultPatternExportGraphAction_TaskName_DataLayout, patternTypes.length);
        monitor.setTaskName(Messages.ResultPatternExportGraphAction_TaskName_DataLayout);

        IClaimWorkPattern work = input.getClaimWorkPattern();
        IClaimData claim = IoActivator.getService().getClaimData(work.getClaimId());

        for (PatternType patternType : patternTypes) {
            String typeName = patternType.getPatternName();
            for (IPattern pattern : input.getChildren(patternType)) {
                String patternName = pattern.getLabel();
                for (IResultCoronaPattern result : input.getChildren(pattern)) {
                    IRecord rec = claim.getRecord(result.getRecordId());
                    String recid = (String) rec.getField(claim.getDispIdField()).getValue();
                    table.addRow(new Row(typeName, patternName, result.getText(), recid));
                }
            }
            monitor.worked(1);
        }
        return table;
    }
}
