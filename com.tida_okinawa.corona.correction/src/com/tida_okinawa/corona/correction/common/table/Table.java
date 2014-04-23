/**
 * @version $Id: Table.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/02/21
 * @author uehara
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.correction.common.table;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.tida_okinawa.corona.common.Encoding;

/**
 * CSV出力用テーブルです。
 * 
 * @author uehara, imai
 * 
 */
public class Table extends ArrayList<Row> implements Cell {
    private static final long serialVersionUID = 8845931279705597933L;


    /**
     * 空のテーブルオブジェクトを生成
     */
    public Table() {
        // nothing
    }


    /**
     * 指定した文字列からなるテーブルを作る
     * 
     * @param texts
     *            構成要素
     */
    public Table(String[][] texts) {
        for (String[] rowText : texts) {
            Row row = new Row(rowText);
            addRow(row);
        }
    }


    /**
     * 指定した行からなるテーブルを作る
     * 
     * @param rows
     *            構成要素の行
     */
    public Table(Row... rows) {
        for (Row row : rows) {
            addRow(row);
        }
    }


    /**
     * @return 全行
     */
    public List<Row> getRows() {
        return this;
    }


    @Override
    public int getColumnSize() {
        int sz = 0;
        for (Row row : this) {
            int rcsz = row.getColumnSize();
            if (rcsz > sz)
                sz = rcsz;
        }
        return sz;
    }


    @Override
    public int getRowSize() {
        int sz = 0;
        for (Row row : this) {
            sz += row.getRowSize();
        }
        return sz;
    }


    /**
     * 新規行を末尾に追加します。
     * 
     * @return 追加した行オブジェクト
     */
    public Row newRow() {
        Row row = new Row(0);
        addRow(row);
        return row;
    }


    /**
     * 末尾に行を追加します。
     * 追加する行の列数と、すでにある行の列数が同じことが前提
     * 
     * @param newRow
     *            追加する行
     */
    public void addRow(Row newRow) {
        this.add(newRow);
    }


    /**
     * tableの全行をこのインスタンスに追加する。
     * 
     * @param table
     *            追加する行を持っているテーブル
     */
    public void addRows(Table table) {
        for (Row row : table) {
            addRow(row);
        }
    }


    @Override
    public String[][] expand() {
        if (size() == 0) {
            final String[][] empty = { { "" } }; //$NON-NLS-1$
            return empty;
        }
        int rowSz = getRowSize();
        int colSz = getColumnSize();
        String[][] expanded = new String[rowSz][colSz];

        int iRow = 0;
        for (Row row : this) {
            String[][] rowExpanded = row.expand();
            for (int i = 0; i < rowExpanded.length; i++) {
                for (int j = 0; j < rowExpanded[i].length; j++) {
                    expanded[i + iRow][j] = rowExpanded[i][j];
                }
                // パディング
                for (int j = rowExpanded[i].length; j < colSz; j++) {
                    expanded[i + iRow][j] = ""; //$NON-NLS-1$
                }
            }
            iRow += rowExpanded.length;
        }
        return expanded;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Row row : this) {
            sb.append(row.toString());
        }

        return sb.toString();
    }


    /**
     * ファイルに出力する
     * 
     * @param bw
     *            出力先
     * @throws IOException
     *             書き込み時のIOエラー
     */
    public void write(BufferedWriter bw) throws IOException {
        write(bw, new NullProgressMonitor());
    }


    /**
     * ファイルに出力する
     * 
     * @param bw
     *            出力先
     * @param monitor
     *            進捗管理用モニター
     * @throws IOException
     *             書き込み時のIOエラー
     */
    public void write(BufferedWriter bw, IProgressMonitor monitor) throws IOException {
        // bw.write(toString());
        for (Row row : this) {
            bw.write(row.toString());
            monitor.worked(1);
            if (monitor.isCanceled()) {
                break;
            }
        }
    }

    // 出力CSVファイルの文字コード
    private static final String CHARSET = Encoding.MS932.toString();


    /**
     * CSVエクスポートします。
     * 
     * @param file
     *            出力ファイル
     * @throws IOException
     *             書き込み時のIOエラー
     */
    public void write(File file) throws IOException {
        write(file, new NullProgressMonitor());
    }


    /**
     * CSVエクスポートします。
     * 
     * @param file
     *            出力ファイル
     * @param monitor
     *            進捗管理用モニター
     * @throws IOException
     *             書き込み時のIOエラー
     */
    public void write(File file, IProgressMonitor monitor) throws IOException {
        monitor.beginTask(Messages.Table_TaskName_Exporting, size());
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, CHARSET);
            bw = new BufferedWriter(osw);
            write(bw, monitor);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (osw != null) {
                    osw.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                System.err.println(e);
                e.printStackTrace(System.err);
            }
        }
    }


    /**
     * テンプレートの先頭シートにデータを出力し、出力ファイルを作成する。
     * 
     * @param templateFile
     *            グラフテンプレート（*.xlsx）
     * @param outputFile
     *            出力先ファイル
     * @param monitor
     *            進捗ダイアログ
     * @throws IOException
     *             テンプレートファイルや出力ファイルがないなど、IOエラー
     */
    public void writeXlsx(File templateFile, File outputFile, IProgressMonitor monitor) throws IOException {
        monitor.beginTask(Messages.Table_TaskName_Exporting, size());

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        XSSFWorkbook book = null;
        try {
            fos = new FileOutputStream(outputFile);
            bos = new BufferedOutputStream(fos);
            book = new XSSFWorkbook(templateFile.getAbsolutePath());
            XSSFSheet sheet = book.getSheetAt(0);
            writeXlsx(sheet, monitor);
            book.write(bos);
        } finally {
            if (book != null) {
                /* テンプレートの変更をなかったことにする。ただ閉じるだけだと、出力先と同じ値になってしまう */
                book.getPackage().revert();
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
            }
        }
    }


    /**
     * 指定されたシートに、データを書き出す
     * 
     * @param sheet
     *            データを書き出すシート
     * @param monitor
     *            進捗ダイアログ
     */
    public void writeXlsx(XSSFSheet sheet, IProgressMonitor monitor) {
        for (int rowIndex = 0; rowIndex < this.size(); rowIndex++) {
            XSSFRow row = getRow(sheet, rowIndex);
            Row thisRow = get(rowIndex);
            for (int columnIndex = 0; columnIndex < thisRow.size(); columnIndex++) {
                XSSFCell cell = getCell(row, columnIndex);
                Cell thisCell = thisRow.get(columnIndex);
                String value = thisCell.toString();
                cell.setCellValue(thisCell.toString());
                if (value.isEmpty()) {
                    /* 空文字種別にしないと、空白扱いにならない。また、文字設定の後にやらないといけない */
                    cell.setCellType(XSSFCell.CELL_TYPE_BLANK);
                }
            }
            monitor.worked(1);
            if (monitor.isCanceled()) {
                break;
            }
        }
    }


    private static final XSSFRow getRow(XSSFSheet sheet, int index) {
        XSSFRow row = sheet.getRow(index);
        if (row == null) {
            row = sheet.createRow(index);
        }
        return row;
    }


    private static final XSSFCell getCell(XSSFRow row, int index) {
        XSSFCell cell = row.getCell(index);
        if (cell == null) {
            cell = row.createCell(index);
        }
        return cell;
    }


    /**
     * TODO キーワードに指定された場所へ、各種情報を出力する <br/>
     * 固定の情報を、特定の場所に書き出す。
     * 
     * @param outputFile
     *            出力先
     * @param project
     *            プロジェクト名
     * @param target
     *            ターゲット名
     * @param cleansingDate
     *            解析日時
     * @param dataNum
     *            解析レコード総件数
     * @param hitNum
     *            ヒットしたレコード件数
     * @param userDics
     *            使用したユーザ辞書一覧
     * @param patternDics
     *            使用したパターン辞書一覧
     * @param labelDics
     *            使用したラベル辞書一覧
     * @param monitor
     *            進捗ダイアログ
     * @throws IOException
     *             テンプレートファイルや出力ファイルがないなど、IOエラー
     */
    public void writeSummary(File outputFile, String project, String target, Date cleansingDate, int dataNum, int hitNum, String userDics, String patternDics,
            String labelDics, IProgressMonitor monitor) throws IOException {
        monitor.beginTask(Messages.Table_TaskName_Exporting, size());

        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        XSSFWorkbook book = null;
        try {
            /* 上書き保存の場合、FileInputStreamを使う */
            fis = new FileInputStream(outputFile);
            book = new XSSFWorkbook(fis);
            XSSFSheet sheet = book.getSheetAt(1);
            writeSummary(sheet, project, target, cleansingDate, dataNum, hitNum, userDics, patternDics, labelDics, monitor);
            fos = new FileOutputStream(outputFile);
            bos = new BufferedOutputStream(fos);
            book.write(bos);
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (book != null) {
                book.getPackage().close();
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
            }
        }
    }


    /**
     * TODO キーワードに指定された場所へ、各種情報を出力する <br/>
     * 固定の情報を、特定の場所に書き出す。
     * 
     * @param sheet
     *            出力先シート
     * @param project
     *            プロジェクト名
     * @param target
     *            ターゲット名
     * @param cleansingDate
     *            解析日時
     * @param dataNum
     *            解析レコード総件数
     * @param hitNum
     *            ヒットしたレコード件数
     * @param userDics
     *            使用したユーザ辞書一覧
     * @param patternDics
     *            使用したパターン辞書一覧
     * @param labelDics
     *            使用したラベル辞書一覧
     * @param monitor
     *            進捗ダイアログ
     */
    void writeSummary(XSSFSheet sheet, String project, String target, Date cleansingDate, int dataNum, int hitNum, String userDics, String patternDics,
            String labelDics, IProgressMonitor monitor) {
        /* T2:出力日 */
        XSSFRow row = getRow(sheet, 3);
        XSSFCell cell = getCell(row, 6);
        cell.setCellValue(new Date(System.currentTimeMillis()));

        final int ROW = 4; // +1行から出力
        final int COL = 6; // G列から出力
        /* プロジェクト名 */
        row = getRow(sheet, ROW);
        cell = getCell(row, COL);
        cell.setCellValue(project);

        /* ターゲット名 */
        row = getRow(sheet, ROW + 1);
        cell = getCell(row, COL);
        cell.setCellValue(target);

        /* 解析日 */
        row = getRow(sheet, ROW + 2);
        cell = getCell(row, COL);
        cell.setCellValue(cleansingDate);

        /* 総件数 */
        row = getRow(sheet, ROW + 3);
        cell = getCell(row, COL);
        cell.setCellValue(dataNum);

        /* ヒット件数 */
        row = getRow(sheet, ROW + 4);
        cell = getCell(row, COL);
        cell.setCellValue(hitNum);

        /* ユーザ辞書 */
        row = getRow(sheet, ROW + 5);
        cell = getCell(row, COL);
        cell.setCellValue(userDics);

        /* パターン辞書 */
        row = getRow(sheet, ROW + 6);
        cell = getCell(row, COL);
        cell.setCellValue(patternDics);

        /* ラベル辞書 */
        row = getRow(sheet, ROW + 7);
        cell = getCell(row, COL);
        cell.setCellValue(labelDics);
    }
}
