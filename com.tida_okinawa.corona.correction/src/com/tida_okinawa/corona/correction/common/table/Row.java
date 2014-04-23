/**
 * @version $Id: Row.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/02/21
 * @author uehara
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.correction.common.table;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * CSV出力用テーブルの行
 * 
 * @author uehara, imai
 * 
 */
public class Row extends ArrayList<Cell> implements Cell {
    private static final long serialVersionUID = 6894252739048812676L;


    /**
     * 空の行オブジェクトを生成
     */
    public Row() {
        super();
    }


    /**
     * 空のセルからなる行を作る
     * 
     * @param sz
     *            列数
     */
    public Row(int sz) {
        for (int i = 0; i < sz; i++) {
            addCell(new TextCell());
        }
    }


    /**
     * 指定した文字列からなる行を作る
     * 
     * @param texts
     *            セル数分のテキスト
     */
    public Row(String... texts) {
        for (String text : texts) {
            addCell(new TextCell(text));
        }
    }


    /**
     * 指定したセルからなる行を作る
     * 
     * @param cells
     *            行の構成要素となるセルオブジェクト一覧
     */
    public Row(Cell... cells) {
        for (Cell cell : cells) {
            addCell(cell);
        }
    }


    /**
     * 指定した要素からなる行を作る
     * 
     * @param cells
     *            行の構成要素となるオブジェクト一覧
     */
    public Row(Object... cells) {
        for (Object cell : cells) {
            if (cell instanceof Cell) {
                addCell((Cell) cell);
            } else if (cell instanceof String) {
                addCell(new TextCell((String) cell));
            } else {
                throw new IllegalArgumentException(cell.getClass().getName());
            }
        }
    }


    /**
     * 指定したデータからなる行を作る
     * 
     * @param datas
     *            行の構成要素となるオブジェクト一覧
     */
    public Row(Collection<?> datas) {
        for (Object data : datas) {
            addCell(new TextCell(data.toString()));
        }
    }


    /**
     * @return この行を構成するセルオブジェクト一覧
     */
    public List<Cell> getCells() {
        return this;
    }


    @Override
    public int getColumnSize() {
        int sz = 0;
        for (Cell cell : this) {
            sz += cell.getColumnSize();
        }
        return sz;
    }


    @Override
    public int getRowSize() {
        int sz = 0;
        for (Cell cell : this) {
            int csz = cell.getRowSize();
            if (csz > sz)
                sz = csz;
        }
        return sz;
    }


    /**
     * レコードにセルを追加します。Cellがテーブルの場合は、既存の行列を適切に拡張します。
     * 
     * @param cell
     *            追加するセル
     */
    public void addCell(Cell cell) {
        super.add(cell);
    }


    @Override
    public String[][] expand() {
        if (size() == 0) {
            String[][] empty = { { "" } }; //$NON-NLS-1$
            return empty;
        }
        int rowSz = getRowSize();
        int colSz = getColumnSize();

        String[][] expanded = new String[rowSz][colSz];

        int jCol = 0;
        for (Cell cell : this) {
            String[][] cellExpanded = cell.expand();
            assert (cellExpanded.length > 0 && cellExpanded[0].length > 0);
            for (int i = 0; i < cellExpanded.length; i++) {
                for (int j = 0; j < cellExpanded[i].length; j++) {
                    expanded[i][jCol + j] = cellExpanded[i][j];
                }
            }
            // パディング
            for (int i = cellExpanded.length; i < expanded.length; i++) {
                for (int j = 0; j < cellExpanded[0].length; j++) {
                    expanded[i][jCol + j] = ""; //$NON-NLS-1$
                }
            }
            jCol += cellExpanded[0].length;
        }

        return expanded;
    }


    /**
     * ファイルに出力する
     * 
     * @param bw
     *            出力先
     * @throws IOException
     *             なんらかのIOエラー
     */
    public void write(BufferedWriter bw) throws IOException {
        bw.write(toString());
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String[][] expanded = expand();
        for (String[] row : expanded) {
            for (String cell : row) {
                sb.append(cell);
                sb.append(","); //$NON-NLS-1$
            }
            sb.append("\n"); //$NON-NLS-1$
        }

        return sb.toString();
    }
}
