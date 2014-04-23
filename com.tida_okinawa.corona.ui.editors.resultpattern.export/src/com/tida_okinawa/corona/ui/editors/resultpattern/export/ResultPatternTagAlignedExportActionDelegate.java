/**
 * @version $Id: ResultPatternTagAlignedExportActionDelegate.java 1842 2014-04-21 04:50:18Z yukihiro-kinjyo $
 * 
 * 2012/02/02 17:01:09
 * @author Shingo-Takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.resultpattern.export;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.correction.common.table.Row;
import com.tida_okinawa.corona.correction.common.table.Table;
import com.tida_okinawa.corona.correction.common.table.TextCell;
import com.tida_okinawa.corona.internal.ui.actions.AbstractResultPatternExportActionDelegate;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.ui.PreferenceInitializer;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.editors.ResultPatternEditorInput;


/**
 * 整列ラベル付での構文解析結果エクスポート
 * 
 * @author Shingo-Takahashi
 * 
 */
public class ResultPatternTagAlignedExportActionDelegate extends AbstractResultPatternExportActionDelegate {

    /** スプリッタ */
    private static final String SPLITTER = " "; //$NON-NLS-1$


    @Override
    protected Table generate(ResultPatternEditorInput input, IProgressMonitor monitor) {

        /** 該当なしフラグ */
        boolean resultNomatch = UIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceInitializer.PREF_RESULT_NOMATCH);
        /** 複数フラグ */
        boolean resultMulti = true;

        IClaimData claim = IoActivator.getService().getClaimData(input.getClaimWorkPattern().getClaimId());
        Table table = new Table(new Row(Messages.ResultPatternTagAlignedExportActionDelegate_text, Messages.ResultPatternTagAlignedExportActionDelegate_claim,
                Messages.ResultPatternTagAlignedExportActionDelegate_field, Messages.ResultPatternTagAlignedExportActionDelegate_record)); /* クレーム分類のみ固定 */

        /* パターン分類の取得 */
        PatternType[] ptnType = input.getClaimWorkPattern().getPatternTypes();
        /* パターン分類をヘッダに割り当て */
        for (int i = 0; i < ptnType.length; i++) {
            String tag = ptnType[i].getPatternName();
            table.get(0).add(new TextCell(Messages.ResultPatternTagAlignedExportActionDelegate_classification + tag));
        }

        int initTableLength = table.getColumnSize();

        Object[] patternTypes = input.getElements(); /* PatternType */
        monitor.beginTask(Messages.ResultPatternTagAlignedExportActionDelegate_createTable, patternTypes.length);
        int productId = input.getClaimWorkPattern().getProductId();
        String product = IoActivator.getService().getProductName(productId);
        for (IRecord recItem : claim.getRecords()) {
            /* 対象ターゲットのみ */
            if (!(recItem.getField(claim.getProductField()).getValue().equals(product))) {
                continue;
            }
            String[] labels = {};
            try {
                IResultCoronaPattern result = input.getClaimWorkPattern().getClaimWorkPattern(recItem.getRecordId());

                /* パターンマッチがないものはスキップ */
                if (resultNomatch == false && result == null) {
                    continue;
                }
                if (result != null) {
                    labels = result.getLabels();
                }
            } catch (IndexOutOfBoundsException e) { /* 範囲外もあり */
                if (resultNomatch == false) {
                    continue;
                }
            }

            /* 原文 */
            String originalWord = (String) recItem.getField(input.getClaimWorkPattern().getFieldId()).getValue();
            /* 半角カンマを置換 */
            originalWord = originalWord.replace(',', '、');

            /* 表示用ID */
            String recid = (String) recItem.getField(claim.getDispIdField()).getValue();

            Row row = new Row("\"" + originalWord + "\"", Integer.toString(input.getClaimWorkPattern().getClaimId()), Integer.toString(input //$NON-NLS-1$ //$NON-NLS-2$
                    .getClaimWorkPattern().getFieldId()), recid);

            /* ラベル */
            for (int i1 = 1; i1 < labels.length; i1 = i1 + 4) {
                boolean bHit = false;
                String type = labels[i1];
                String pattern = labels[i1 + 1];
                String label;
                if (labels.length > (i1 + 2)) {
                    label = labels[i1 + 2];
                } else {
                    label = ""; //$NON-NLS-1$
                }
                String word;
                if (labels.length > (i1 + 3)) {
                    word = labels[i1 + 3];
                } else {
                    word = ""; //$NON-NLS-1$
                }
                for (int i = 0; i < ptnType.length; i++) {
                    String tag = ptnType[i].getPatternName();
                    if (type.equals(tag)) {
                        do {
                            row.add(new TextCell("")); //$NON-NLS-1$
                            if (row.getColumnSize() >= initTableLength) {
                                break;
                            }
                        } while (true);
                        int idx = initTableLength - ptnType.length + i;
                        TextCell cell = (TextCell) row.get(idx);
                        if ((cell != null) && !(cell.getText().isEmpty()) && (resultMulti == true)) {
                            boolean bHitPattern = false;
                            String[] texts = cell.getText().split(SPLITTER);
                            for (String s : texts) {
                                if (s.isEmpty()) {
                                    continue;
                                }
                                if (pattern.equals(s)) {
                                    bHitPattern = true;
                                    break;
                                }
                            }
                            if (!(bHitPattern)) {
                                cell.setText(cell.getText() + SPLITTER + pattern);
                            }
                        } else {
                            row.set(idx, new TextCell(pattern));
                        }
                    }
                }
                /* ラベル名から、ヘッダに付与されているかチェック */
                for (int j = initTableLength; j < table.get(0).size(); j++) {
                    String target = table.get(0).get(j).toString();
                    if (label.equals(target)) {
                        if (row.getColumnSize() < j + 1) {
                            do {
                                row.add(new TextCell("")); //$NON-NLS-1$
                                if (row.getColumnSize() >= j + 1) {
                                    break;
                                }
                            } while (true);

                        }
                        TextCell cell = (TextCell) row.get(j);
                        if ((cell != null) && !(cell.getText().isEmpty()) && (resultMulti == true)) {
                            String[] texts = cell.getText().split(SPLITTER);
                            boolean bHitPattern = false;
                            for (String s : texts) {
                                if (s.isEmpty()) {
                                    continue;
                                }
                                if (word.equals(s)) {
                                    bHitPattern = true;
                                    break;
                                }
                            }
                            if (!(bHitPattern)) {
                                cell.setText(cell.getText() + SPLITTER + word);
                            }
                        } else {
                            row.set(j, new TextCell(word));
                        }

                        bHit = true;
                    }
                }
                /* ヘッダに無い場合 */
                if (!(bHit)) {
                    if (label.equals("")) { //$NON-NLS-1$
                        continue;
                    }
                    /* ヘッダに付与 */
                    table.get(0).add(new TextCell(label));
                    if (row.getColumnSize() < table.get(0).size()) {
                        do {
                            row.add(new TextCell("")); //$NON-NLS-1$
                            if (row.getColumnSize() >= table.get(0).size()) {
                                break;
                            }
                        } while (true);
                    }
                    row.set(table.get(0).size() - 1, new TextCell(word));
                }
            }
            table.addRow(row);
        }

        /* ユーザ入力項目をセット */
        int tableRow = table.getRowSize();
        int tableCol = table.get(0).getColumnSize();
        String[][] recordId = table.expand();

        for (int i = 1; i < tableRow; i++) {

            String strId = recordId[i][3];
            IRecord recrdItem = claim.getRecord(Integer.parseInt(strId));
            List<IField> fields = recrdItem.getFields();

            /* 空白セルをスペースで埋める */
            if (table.get(i).getColumnSize() < tableCol) {
                for (int j = (table.get(i).getColumnSize()); j < tableCol; j++) {
                    table.get(i).add(new TextCell("")); //$NON-NLS-1$
                }
            }

            /* 最終列にラベル情報がある場合、余分なセルが追加されているので除去 */
            if (table.get(i).getColumnSize() > tableCol) {
                int delCol = table.get(i).getColumnSize() - tableCol;
                int getCol = table.get(i).getColumnSize();
                for (int k = 1; delCol >= k; k++) {
                    table.get(i).remove(getCol - k);
                }
            }

            /* 項目をセット */
            for (IField field : fields) {
                if (field.getId() != claim.getDispIdField()) {
                    table.get(i).add(new TextCell("\"" + (String) field.getValue() + "\"")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }

        List<IFieldHeader> headers = claim.getFieldInformations();
        List<String> label = new ArrayList<String>();

        /* ヘッダ情報の取得 */
        for (IFieldHeader header : headers) {
            if (header.getId() != claim.getDispIdField()) {
                label.add(header.getDispName());
            }
        }

        /* CSV出力ヘッダ追加項目セット */
        table.get(0).add(new Row(label));

        monitor.worked(1);
        return table;
    }
}
