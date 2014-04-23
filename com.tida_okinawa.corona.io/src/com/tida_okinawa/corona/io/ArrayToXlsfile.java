package com.tida_okinawa.corona.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ArrayToXlsfile {
    /**
     * ArrayListをxls構造へ変換しExcelファイルを作成するクラス
     * 
     * @author shingo-kuniyoshi
     * 
     */
    /**
     * Excel出力時の最大行数　6万行を設定
     */
    public static final int EXCELLINEMAX = 60000;

    // poi objects
    private XSSFWorkbook wb;
    private String path = "";


    public ArrayToXlsfile(String path) {
        this.path = path;
    }


    /**
     * ArrayListをxls構造へ変換しExcelファイルを作成　ＭＡＩＮ
     * 
     * @param meisai
     * @throws IOException
     */
    public void createFile(String xlsheader, ArrayList<String> meisai) throws IOException {
        wb = new XSSFWorkbook();
        write(xlsheader, meisai);
        FileOutputStream outxlsx = new FileOutputStream(path); // ファイル名を指定する
        wb.write(outxlsx);
        outxlsx.close();
    }


    /**
     * ArrayListをExcelに転記処理
     * 
     * @param xlsheader
     * @param meisai
     * @throws IOException
     */
    private void write(String xlsheader, ArrayList<String> meisai) throws IOException {

        /* ArrayListの件数取得 */
        int totalcnt = meisai.size();
        /* EXCELLINEMAX行毎にシート分割を実施 */
        int maxsheet = meisai.size() / EXCELLINEMAX;
        int amari = meisai.size() % EXCELLINEMAX;

        if (maxsheet > 0) {
            if (amari == 0) {
                maxsheet = maxsheet - 1;
            }
        }
        for (int shtCount = 0; shtCount <= maxsheet; shtCount++) {

            /* シートの作成　シート名　"sheet_" + シートカウント */
            XSSFSheet sheet = wb.createSheet("sheet_" + shtCount);

            XSSFRow row = sheet.createRow(0);

            /* 入力した内容を『,』にて分割し各セルに設定する。 */
            String[] splitstrAry = xlsheader.split(",");

            for (int j = 0; j < splitstrAry.length; j++) {
                XSSFCell cell = row.createCell(j);
                cell.setCellValue(splitstrAry[j]);
            }
        }

        /* 上記で算出されたシートカウント分繰り返す */
        for (int shtCount = 0; shtCount <= maxsheet; shtCount++) {

            /* シート選択　シート名　"sheet_" + シートカウント */
            XSSFSheet sheet = wb.getSheet("sheet_" + shtCount);

            /* 対象シートに設定すべきArrayListの開始終了位置を算出 */
            int lpsrtcnt = shtCount * EXCELLINEMAX + 0;
            int lpendcnt = shtCount * EXCELLINEMAX + EXCELLINEMAX;

            if (lpendcnt >= totalcnt) {
                lpendcnt = totalcnt;
            }

            /* 上記算出された開始終了位置を基にArrayListを繰り返す */
            for (int lopCount = lpsrtcnt; lopCount < lpendcnt; lopCount++) {
                /* 設定する行を指定 */
                int setRow = lopCount - lpsrtcnt + 1;
                XSSFRow row = sheet.createRow(setRow);

                /* 入力した内容を『,』にて分割し各セルに設定する。 */
                String[] splitstrAry = meisai.get(lopCount).split(",");

                for (int j = 0; j < splitstrAry.length; j++) {
                    XSSFCell cell = row.createCell(j);
                    cell.setCellValue(splitstrAry[j]);
                }
            }
        }
    }
}
