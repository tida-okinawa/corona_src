/**
 * @version $Id: TextCell.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/02/21
 * @author uehara
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.correction.common.table;

/**
 * テキスト文字列を保持するセルです。
 * 
 * @author uehara
 * 
 */
public class TextCell implements Cell {

    private String text = ""; //$NON-NLS-1$


    /**
     * 空のテキストセル
     */
    public TextCell() {
        super();
    }


    /**
     * 
     * @param text
     *            セルに保持させるテキスト
     */
    public TextCell(String text) {
        super();
        setText(text);
    }


    /**
     * 
     * @param i
     *            セルに保持させる数値（文字列に変換されて保持されます）
     */
    public TextCell(int i) {
        super();
        setText(String.valueOf(i));
    }


    /**
     * セルのテキストを取得します。
     * 
     * @return セルのテキスト
     */
    public String getText() {
        return text;
    }


    /**
     * セルに保持させるテキストを設定します。
     * 
     * @param text
     *            セルに保持させるテキスト
     */
    public void setText(String text) {
        if (text == null) {
            text = ""; //$NON-NLS-1$
        } else {
            this.text = text;
        }
    }


    @Override
    public String toString() {
        if (text.indexOf(",") != -1) { //$NON-NLS-1$
            return "\"" + text + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return text;
    }


    @Override
    final public int getRowSize() {
        return 1;
    }


    @Override
    final public int getColumnSize() {
        return 1;
    }


    @Override
    final public String[][] expand() {
        String[][] result = { { toString() } };
        return result;
    }


    @Override
    public int hashCode() {
        return text.hashCode();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TextCell)) {
            return false;
        }
        return getText().equals(((TextCell) o).getText());
    }
}
