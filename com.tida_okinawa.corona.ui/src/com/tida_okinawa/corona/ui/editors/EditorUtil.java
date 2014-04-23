/**
 * @version $Id: EditorUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/16 13:34:39
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.FileEditorInput;

import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.ui.editors.user.TabTraverseListener;

/**
 * @author kousuke-morishima
 */
public class EditorUtil {

    /**
     * IFileがさすファイルが辞書の場合、DicEditorInputを返す
     * 
     * @param input
     * @return {@link DicEditorInput}
     */
    public static DicEditorInput convertFrom(FileEditorInput input) {
        IFile iFile = input.getFile();
        IUIElement element = CoronaModel.INSTANCE.adapter(iFile, false);
        if (!(element instanceof IUIDictionary)) {
            return null;
        }

        IUIDictionary dic = ((IUIDictionary) element);
        return new DicEditorInput(dic);
    }


    /**
     * Tabキーで編集セルのフォーカス移動を付与する
     * 
     * @param editors
     *            TableColumnごとにひとつのエディタを作って（編集させないならnullを入れて）配列にして渡す。
     * @param viewer
     * @param columns
     *            タブでフォーカスを移動させる列を指定する
     */
    public static void setFocusMoveListener(CellEditor[] editors, TableViewer viewer, final int... columns) {
        if (columns.length == 0) {
            return;
        }
        int startColumn = columns[0];
        int endColumn = columns[columns.length - 1];
        for (int i = 0; i < columns.length; i++) {
            int nextColumn = (columns[i] < endColumn) ? columns[i + 1] : startColumn;
            int prevColumn = (startColumn < columns[i]) ? columns[i - 1] : endColumn;
            boolean isFirst = (columns[i] == startColumn);
            boolean isLast = (columns[i] == endColumn);
            editors[columns[i]].getControl().addTraverseListener(new TabTraverseListener(viewer, nextColumn, prevColumn, isFirst, isLast));
        }
    }


    /**
     * 現在選択している行の指定列を編集状態にする。
     * 
     * @param viewer
     * @param targetColumn
     */
    public static void editMode(TableViewer viewer, int targetColumn) {
        /* 選択中の行番号を取得 */
        Table tbl = viewer.getTable();
        int cellRow = tbl.getSelectionIndex();
        if (cellRow != -1) {
            /* テーブル中のcellRow行目のアイテムを取得 */
            TableItem item = tbl.getItem(cellRow);
            /* 取得したアイテムの指定セルを編集モードへ */
            viewer.editElement(item.getData(), targetColumn);
        }
    }
}
