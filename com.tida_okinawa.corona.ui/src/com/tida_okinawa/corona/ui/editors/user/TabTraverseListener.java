/**
 * @version $Id: TabTraverseListener.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/22 2:06:43
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author kousuke-morishima
 */
public class TabTraverseListener implements TraverseListener {
    private TableViewer viewer;
    private Table table;
    private int nextColumn;
    private int prevColumn;
    private boolean isFirst;
    private boolean isLast;


    public TabTraverseListener(TableViewer viewer, int nextColumn, int prevColumn, boolean isFirst, boolean isLast) {
        this.viewer = viewer;
        this.table = viewer.getTable();
        this.nextColumn = nextColumn;
        this.prevColumn = prevColumn;
        this.isFirst = isFirst;
        this.isLast = isLast;
    }


    @Override
    public void keyTraversed(TraverseEvent e) {
        // 編集中のセルの行番号を取得
        int cellRow = table.getSelectionIndex();
        // テーブル中のcellRow行目のアイテムを取得
        TableItem item = table.getItem(cellRow);

        int moveColumn = -1;
        switch (e.detail) {
        case SWT.TRAVERSE_TAB_NEXT: // Tabキー押下
            // Eclipseでデフォルトで働いている「フォーカスマネージャ」によるタブ移動を無効化
            e.doit = false;

            moveColumn = nextColumn;
            if (isLast) {
                if (table.getItemCount() - 1 > cellRow) {
                    item = table.getItem(cellRow + 1);
                } else {
                    // 最下最右のセルにフォーカスがある場合、フォーカス移動しない
                }
            }
            break;
        case SWT.TRAVERSE_TAB_PREVIOUS: // Shift + Tabキー押下
            e.doit = false;

            moveColumn = prevColumn;
            if (isFirst) {
                if (0 < cellRow) {
                    item = table.getItem(cellRow - 1);
                } else {
                    // 最上最左のセルにフォーカスがある場合、フォーカス移動しない
                }
            }
            break;
        default:
            break;
        }
        if (moveColumn != -1) {
            viewer.editElement(item.getData(), moveColumn);
        }
    }

}
