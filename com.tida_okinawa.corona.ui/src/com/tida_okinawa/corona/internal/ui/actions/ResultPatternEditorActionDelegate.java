/**
 * @version $Id: ResultPatternEditorActionDelegate.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/09 00:25:11
 * @author yoshikazu-imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.correction.common.table.Row;
import com.tida_okinawa.corona.correction.common.table.Table;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.ui.editors.ResultPatternEditorInput;

/**
 * 構文解析結果をラベル無しCSV形式でエクスポートするアクション
 * 
 * @author yoshikazu-imai
 * 
 */
public class ResultPatternEditorActionDelegate extends AbstractResultPatternExportActionDelegate {

    /**
     * 構文解析結果を ラベル無しCSV 出力用テーブルに変換して返す。
     * 
     * <pre>
     * フォーマット
     * 
     * 区分,パターン,テキスト,Claim#,Field#,Record#
     * </pre>
     */
    @Override
    protected Table generate(ResultPatternEditorInput input, IProgressMonitor monitor) {

        IClaimData claim = IoActivator.getService().getClaimData(input.getClaimWorkPattern().getClaimId());
        List<IFieldHeader> headers = claim.getFieldInformations();
        List<String> label = new ArrayList<String>();

        label.add(Messages.ResultPatternEditorActionDelegate_div);
        label.add(Messages.ResultPatternEditorActionDelegate_pattern);
        label.add(Messages.ResultPatternEditorActionDelegate_text);
        label.add(Messages.ResultPatternEditorActionDelegate_claim);
        label.add(Messages.ResultPatternEditorActionDelegate_field);
        label.add(Messages.ResultPatternEditorActionDelegate_record);

        /* ヘッダ情報の取得 */
        for (IFieldHeader header : headers) {
            if (header.getId() != claim.getDispIdField()) {
                label.add(header.getDispName());
            }
        }

        Table table = new Table(new Row(label));

        Object[] patternTypes = input.getElements();

        monitor.beginTask(Messages.ResultPatternEditorActionDelegate_createTable, patternTypes.length);
        monitor.setTaskName(Messages.ResultPatternEditorActionDelegate_createTable);

        for (Object type : patternTypes) {
            PatternType patternType = (PatternType) type;
            Object[] patterns = input.getChildren(patternType);

            for (Object pattern : patterns) {
                Object[] results = input.getChildren((IPattern) pattern);

                for (Object result : results) {
                    IRecord rec = claim.getRecord(((IResultCoronaPattern) result).getRecordId());
                    String recid = (String) rec.getField(claim.getDispIdField()).getValue();
                    List<IField> fields = rec.getFields();
                    List<String> rows = new ArrayList<String>();

                    rows.add(patternType.getPatternName());
                    rows.add(((IPattern) pattern).getLabel());
                    rows.add(((IResultCoronaPattern) result).getText());
                    rows.add(Integer.toString(claim.getId()));
                    rows.add(Integer.toString(input.getClaimWorkPattern().getFieldId()));
                    rows.add(recid);

                    for (IField field : fields) {
                        if (field.getId() != claim.getDispIdField()) {

                            /* データをダブルクォーテーションで囲ってセット(改行防止) */
                            String mBody = ((String) field.getValue());
                            mBody = "\"" + mBody + "\""; //$NON-NLS-1$ //$NON-NLS-2$
                            rows.add(mBody);
                        }
                    }

                    table.addRow(new Row(rows));
                }
            }
            monitor.worked(1);
        }
        return table;
    }
}
