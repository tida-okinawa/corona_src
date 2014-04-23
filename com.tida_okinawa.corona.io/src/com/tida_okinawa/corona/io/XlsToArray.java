package com.tida_okinawa.corona.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tida_okinawa.corona.io.command.DicIEConstants;
import com.tida_okinawa.corona.io.model.dic.DicType;


/**
 * xls構造をArrayListへ変換するクラス
 * 
 * @author shingo-kuniyoshi
 * 
 */
public class XlsToArray {

    private String path = ""; //$NON-NLS-1$


    public XlsToArray(String path) {
        this.path = path;
    }


    /**
     * セルタイプを判断してStringを返却する。
     * 
     * @param cell
     * @return
     *         String
     */
    private static String getValue(Cell cell) {

        if (cell == null) {
            return ""; //$NON-NLS-1$
        }

        switch (cell.getCellType()) {

        case Cell.CELL_TYPE_BOOLEAN:
            return Boolean.toString(cell.getBooleanCellValue());
        case Cell.CELL_TYPE_FORMULA:
            return cell.getCellFormula();
        case Cell.CELL_TYPE_NUMERIC:
            return Integer.toString((int) cell.getNumericCellValue());
        case Cell.CELL_TYPE_STRING:
            return cell.getStringCellValue();
        default:
            return ""; //$NON-NLS-1$
        }
    }


    /**
     * ArrayListを返却する。
     * 
     * @return
     *         ArrayList
     */
    public ArrayList<String> getData() {

        File excelFile = new File(path);

        /* 拡張子でどの辞書なのか判断 */
        int extLastIndex = path.lastIndexOf("."); //$NON-NLS-1$
        int extIndex = path.indexOf(".") + 1; //$NON-NLS-1$
        String extention = ""; //$NON-NLS-1$
        if (extIndex != extLastIndex + 1) {
            extention = path.substring(extIndex, extLastIndex);
        } else {
            extention = path.substring(extIndex, path.length());
        }
        int columnSize = 0;
        if (extention.equals(DicType.FLUC.getExtension()) || extention.equals(DicType.SYNONYM.getExtension())) {
            columnSize = DicIEConstants.DEPEND_DIC_COLUMNS;
        } else if (extention.equals(DicType.COMMON.getExtension()) || extention.equals(DicType.SPECIAL.getExtension())
                || extention.equals(DicType.CATEGORY.getExtension())) {
            columnSize = DicIEConstants.USER_DIC_COLUMNS;
        } else {
            columnSize = 0;
        }
        ArrayList<String> list = new ArrayList<String>();
        try {
            FileInputStream fi = new FileInputStream(excelFile);
            XSSFWorkbook book = new XSSFWorkbook(fi);
            fi.close();

            for (Sheet sheet : book) { // 全シートをなめる

                int rowCount = 0;

                for (Row row : sheet) { // 全行をなめる
                    StringBuilder str = new StringBuilder();

                    /* 拡張子がゆらぎ辞書、ユーザー辞書以外の場合はカンマ付与をしない */
                    if (columnSize == 0) {

                        if (row.getRowNum() == 0) {

                            for (Cell cell : row) {

                                str.append("\"" + getValue(cell) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                                str.append(","); //$NON-NLS-1$

                                /* 先頭行（表題）の項目数をカウントする */
                                rowCount = rowCount + 1;
                            }
                        } else {

                            for (int i = 0; i < rowCount; i++) {
                                Cell setCell = row.getCell(i);

                                String converted = getValue(setCell);

                                if (!(converted.equals(""))) { //$NON-NLS-1$
                                    /* 半角カンマとダブルクォーテーションと円記号を安全な文字に置き換える */
                                    converted = converted.replace(',', '，').replace('\"', '”').replace('\\', '￥');
                                }
                                str.append("\"" + converted + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                                str.append(","); //$NON-NLS-1$
                            }
                        }

                    } else {
                        /* ユーザー辞書、ゆらぎ辞書の場合はカンマ付与を行う */
                        for (int i = 0; i < columnSize; i++) {
                            Cell cell = row.getCell(i);
                            // TODO 20131218
                            // str.append(getValue(cell));
                            /* インポートファイルの項目数が増加する前のバージョンへの対応 */
                            String val = getValue(cell);
                            if (val.indexOf("\n") >= 0) { //$NON-NLS-1$
                                str.append(val.substring(0, val.indexOf("\n"))); //$NON-NLS-1$
                                //str.append(","); //$NON-NLS-1$
                                for (int j = i + 1; j < columnSize; j++) {
                                    str.append(","); //$NON-NLS-1$
                                }
                                break;
                            }
                            str.append(getValue(cell));
                            str.append(","); //$NON-NLS-1$
                        }

                    }
                    // 行末からカンマを削除した値をリストへ追加
                    list.add(str.deleteCharAt(str.toString().length() - 1).toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return list;
    }
}
