/**
 * @version $Id: Pattern.java 967 2014-03-24 09:25:36Z yukihiro-kinjyo $
 * 
 * 2014/03/24 09:25:36
 * @author yukihiro-kinjyo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.tida_okinawa.corona.correction.common.table.Row;
import com.tida_okinawa.corona.correction.common.table.Table;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.ui.data.MorphemeRecord;
import com.tida_okinawa.corona.ui.data.PatternMatcherRecord;

/**
 * @author yukihiro-kinjyo
 * 
 */
public class ExportCSV extends Cleansing2<MorphemeRecord, PatternMatcherRecord> {
    /**
     * @param args
     *            前から順に、データベース接続URL、データベースユーザ名、データベースパスワード、
     *            対象プロジェクト名、対象ターゲット名、対象列番号、該当なしを含めるか、
     *            エクスポートファイルパス
     */
    public static void main(String[] args) {
        try {
            new ExportCSV(args).run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(Cleansing.ERROR_CODE_ANY_EXCEPTION);
        }
        System.exit(0);
    }


    /**
     * 自動実行用
     * 
     * @param args
     *            引数
     */
    public ExportCSV(String[] args) {
        super(args);
    }

    private static final int URL = 0;
    private static final int USER = 1;
    private static final int PASS = 2;
    private static final int PROJECT = 3;
    private static final int TARGET = 4;
    private static final int COLUMN = 5;
    private static final int RESULT_NOMATCH = 6;
    private static final int EXPORT_PATH = 7;

    /** 引数の数がおかしいときのエラーコード */
    public static final int ERROR_CODE_ARGS = 7;
    /** 列番号のエラーコード */
    public static final int ERROR_CODE_INVALID_COLUMN = 8;
    /** 該当なしを含めるかのエラーコード */
    public static final int ERROR_CODE_INVALID_NOMATCH = 9;
    /** エクスポートファイルパスのエラーコード */
    public static final int ERROR_CODE_INVALID_PATH = 10;
    /** CSVファイル書き込みのエラーコード */
    public static final int ERROR_CODE_NOT_WRITABLE = 11;


    private ICoronaProject project;
    private ICoronaProduct product;
    private int column;

    /** 該当なしフラグ */
    private boolean resultNomatch;
    /** エクスポートパス(ファイル名込み) */
    private String exportPath;


    @Override
    int check(String[] args) {
        if (args.length != 8) {
            return ERROR_CODE_ARGS;
        }

        service = createService(args[URL], args[USER], args[PASS]);
        if (service == null) {
            return ERROR_CODE_NO_DATABASE_CONNECTION;
        }

        project = searchProject(args[PROJECT]);
        if (project == null) {
            return ERROR_CODE_NO_PROJECT;
        }

        product = searchProduct(project, args[TARGET]);
        if (product == null) {
            return ERROR_CODE_NO_TARGET;
        }

        try {
            column = Integer.parseInt(args[COLUMN]);
        } catch (NumberFormatException e) {
            return ERROR_CODE_INVALID_COLUMN;
        }
        if (column < 1) {
            return ERROR_CODE_INVALID_COLUMN;
        }

        String resultNomatchString = args[RESULT_NOMATCH];
        if (resultNomatchString == null) {
            return ERROR_CODE_INVALID_NOMATCH;
        }
        if (resultNomatchString.equalsIgnoreCase("true")) { //$NON-NLS-1$
            resultNomatch = true;
        } else if (resultNomatchString.equalsIgnoreCase("false")) { //$NON-NLS-1$
            resultNomatch = false;
        } else {
            return ERROR_CODE_INVALID_NOMATCH;
        }

        exportPath = args[EXPORT_PATH];
        if (exportPath == null || exportPath.isEmpty() || exportPath.length() < 3) {
            return ERROR_CODE_INVALID_PATH;
        }

        return CODE_OK;
    }


    @Override
    String getErrorMessage(int errorCode) {
        switch (errorCode) {
        case ERROR_CODE_ARGS:
            return Messages.bind(Messages.ErrorMessage_ArgsNum, new Object[] { Messages.Export_CSV, 8, Messages.Export_ARGS });
        case ERROR_CODE_INVALID_NOMATCH:
            return Messages.ExportCSV_NO_MATCH_ARG;
        case ERROR_CODE_INVALID_COLUMN:
            return Messages.ExportCSV_INVALID_CLUMN;
        case ERROR_CODE_NOT_WRITABLE:
            return Messages.ExportCSV_NOT_WRITABLE;
        default:
            return super.getErrorMessage(errorCode);
        }
    }


    void run() {
        /* 処理対象の選定 */
        List<IClaimWorkData> works = new ArrayList<>();
        for (IClaimWorkData work : product.getClaimWorkDatas()) {
            if (ClaimWorkDataType.RESLUT_PATTERN.equals(work.getClaimWorkDataType())) {
                if (work.getFieldId() == column) {
                    works.add(work);
                }
            }
        }
        if (works.size() == 0) {
            errorExit(ERROR_CODE_INVALID_COLUMN, getErrorMessage(ERROR_CODE_INVALID_COLUMN));
        }
        for (IClaimWorkData workdata : works) {
            // ラベルなしでの構文解析結果エクスポート
            ResultPatternImitationEditor editor = new ResultPatternImitationEditor((IClaimWorkPattern) workdata, resultNomatch);
            Table table = generate(editor);
            try {
                // デバイス名などの予約語、ディレクトリ名など
                // ファイルとして保存できないパスが指定されてる場合は例外を投げる
                // 総パス長が OS の制限にかかる場合は何も対策していない。
                File file = new File(exportPath);
                table.write(file, new NullProgressMonitor());
            } catch (Exception e) {
                errorExit(ERROR_CODE_NOT_WRITABLE, getErrorMessage(ERROR_CODE_NOT_WRITABLE));
            } finally {
            }
            break;
        }
    }


    /**
     * 構文解析結果を CSV 出力用テーブルに変換して返す。
     * 
     * <pre>
     * フォーマット
     * 
     * 区分,パターン,テキスト,Claim#,Field#,Record#
     * </pre>
     */
    protected Table generate(ResultPatternImitationEditor editor) {

        IClaimData claim = IoActivator.getService().getClaimData(editor.getClaimWorkPattern().getClaimId());
        List<IFieldHeader> headers = claim.getFieldInformations();
        List<String> label = new ArrayList<>();

        label.add(com.tida_okinawa.corona.internal.ui.actions.Messages.ResultPatternEditorActionDelegate_div);
        label.add(com.tida_okinawa.corona.internal.ui.actions.Messages.ResultPatternEditorActionDelegate_pattern);
        label.add(com.tida_okinawa.corona.internal.ui.actions.Messages.ResultPatternEditorActionDelegate_text);
        label.add(com.tida_okinawa.corona.internal.ui.actions.Messages.ResultPatternEditorActionDelegate_claim);
        label.add(com.tida_okinawa.corona.internal.ui.actions.Messages.ResultPatternEditorActionDelegate_field);
        label.add(com.tida_okinawa.corona.internal.ui.actions.Messages.ResultPatternEditorActionDelegate_record);

        /* ヘッダ情報の取得 */
        for (IFieldHeader header : headers) {
            if (header.getId() != claim.getDispIdField()) {
                label.add(header.getDispName());
            }
        }

        Table table = new Table(new Row(label));

        Object[] patternTypes = editor.getElements();

        for (Object type : patternTypes) {
            PatternType patternType = (PatternType) type;
            Object[] patterns = editor.getChildren(patternType);

            for (Object pattern : patterns) {
                Object[] results = editor.getChildren((IPattern) pattern);

                for (Object result : results) {
                    IRecord rec = claim.getRecord(((IResultCoronaPattern) result).getRecordId());
                    String recid = (String) rec.getField(claim.getDispIdField()).getValue();
                    List<IField> fields = rec.getFields();
                    List<String> rows = new ArrayList<>(256);

                    rows.add(patternType.getPatternName());
                    rows.add(((IPattern) pattern).getLabel());
                    rows.add(((IResultCoronaPattern) result).getText());
                    rows.add(Integer.toString(claim.getId()));
                    rows.add(Integer.toString(editor.getClaimWorkPattern().getFieldId()));
                    rows.add(recid);

                    for (IField field : fields) {
                        if (field.getId() != claim.getDispIdField()) {
                            /* データをダブルクォートで囲ってセット(改行防止) */
                            String mBody = ((String) field.getValue());
                            mBody = "\"" + mBody + "\""; //$NON-NLS-1$ //$NON-NLS-2$
                            rows.add(mBody);
                        }
                    }
                    table.addRow(new Row(rows));
                }
            }
        }
        return table;
    }
}