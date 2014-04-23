/**
 * @version $Id: ExtractCooccurrenceTableSorter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/18 16:31:27
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author s.takuro
 * 
 */
public class ExtractCooccurrenceTableSorter extends ViewerSorter {
    private int sortOrder = 1;
    private int pos = -1;


    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (pos == -1) {
            return 0;
        }
        /* パターンの種類のカラム選択時 */
        else if (pos == 0) {
            String value1 = ((ExtractCooccurrenceElement) e1).getPatternType();
            String value2 = ((ExtractCooccurrenceElement) e2).getPatternType();
            return value1.compareTo(value2) * sortOrder;
        }
        /* 出現回数のカラム選択時 */
        else if (pos == 1) {
            int integer1 = Integer.parseInt(((ExtractCooccurrenceElement) e1).getTerm(pos - 1));
            int integer2 = Integer.parseInt(((ExtractCooccurrenceElement) e2).getTerm(pos - 1));
            return (integer1 - integer2) * sortOrder;
        }
        /* 単語のカラム選択時 */
        else {
            String value1 = ((ExtractCooccurrenceElement) e1).getTerm(pos - 1);
            String value2 = ((ExtractCooccurrenceElement) e2).getTerm(pos - 1);
            return value1.compareTo(value2) * sortOrder;
        }
    }


    /**
     * ソート順を逆転する
     */
    public void changeSortMode() {
        this.sortOrder *= -1;
    }


    /**
     * カラムの位置を設定する
     * 
     * @param pos
     *            カラムの位置
     */
    public void setPos(int pos) {
        this.pos = pos;
    }
}
