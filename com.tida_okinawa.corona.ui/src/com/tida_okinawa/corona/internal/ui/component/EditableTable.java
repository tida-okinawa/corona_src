/**
 * @version $Id: EditableTable.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2010/12/08
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.component;

import java.util.Arrays;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.tida_okinawa.corona.ui.editors.EditorUtil;

/**
 * 編集可能なTableViewerを提供するコンポーネント
 * 
 * @author KMorishima
 * 
 */
public class EditableTable {
    private String[] columns;


    /**
     * @param parent
     * @param columns
     *            テーブルのカラムラベル
     * @param widths
     *            各カラムの幅
     * @param tableLabel
     *            テーブルにつける説明ラベル。nullならつけない
     */
    public EditableTable(Composite parent, String[] columns, int[] widths, String tableLabel) {
        this(parent, SWT.BORDER | SWT.SINGLE, columns, widths, tableLabel);
    }


    /**
     * @param parent
     * @param style
     *            TableViewerに使えるスタイル
     * @param columns
     *            テーブルのカラム
     * @param widths
     *            カラムの幅
     * @param tableLabel
     *            テーブルにつける説明ラベル。nullならつけない
     */
    public EditableTable(Composite parent, int style, String[] columns, int[] widths, String tableLabel) {
        this.columns = Arrays.copyOf(columns, columns.length);

        createTable(parent, style, widths, tableLabel);

        // 編集可能なカラムの初期化
        editable = new boolean[columns.length];
        setEditable(true);
    }


    /* ****************************************
     * 便利口
     */
    /**
     * TableViewerを再描画する<br />
     * TableViewerにセットしたモデルを更新してから呼ばないと、表示は変わらない
     */
    public void refresh() {
        tableViewer.refresh();
    }


    public void update(Object element) {
        tableViewer.update(element, null);
    }


    /**
     * @param index
     * @return TableViewerのindex番目のアイテム
     */
    public Object getElementAt(int index) {
        return tableViewer.getElementAt(index);
    }


    /**
     * @return テーブルセルの編集エディタ
     */
    public TextCellEditor getDefaultEditor() {
        return new TextCellEditor(tableViewer.getTable());
    }


    /**
     * @return TableViewerのInput
     */
    public Object getInput() {
        return tableViewer.getInput();
    }


    /**
     * @param data
     *            TableViewerに表示するデータモデル
     */
    public void setInput(Object data) {
        tableViewer.setInput(data);
    }


    /**
     * @return TableViewerに表示されている全アイテム
     */
    public TableItem[] getItems() {
        return tableViewer.getTable().getItems();
    }


    /* ****************************************
     * 操作口
     */
    public void addSelectionListener(SelectionListener listener) {
        tableViewer.getTable().addSelectionListener(listener);
    }


    public void removeSelectionListener(SelectionListener listener) {
        tableViewer.getTable().removeSelectionListener(listener);
    }


    public void addFocusListener(FocusListener listener) {
        tableViewer.getTable().addFocusListener(listener);
    }


    public void removeFocusListener(FocusListener listener) {
        tableViewer.getTable().removeFocusListener(listener);
    }


    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        tableViewer.addSelectionChangedListener(listener);
    }


    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        tableViewer.removeSelectionChangedListener(listener);
    }


    /**
     * TableViewerのContentProviderをセットする。<br />
     * 呼ばれない場合、デフォルトでArrayContentProviderがセットされている
     * 
     * @param provider
     */
    public void setContentProvider(IContentProvider contentProvider) {
        tableViewer.setContentProvider(contentProvider);
    }


    /**
     * {@link TableViewer#setLabelProvider(IBaseLabelProvider)}がデフォルト
     * 
     * @param labelProvider
     */
    public void setLabelProvider(ITableLabelProvider labelProvider) {
        tableViewer.setLabelProvider(labelProvider);
    }


    /**
     * @return TableViewerが選択しているアイテム
     */
    public IStructuredSelection getSelection() {
        return (IStructuredSelection) tableViewer.getSelection();
    }


    /**
     * アイテムを選択する
     * 
     * @param index
     */
    public void setSelection(int index) {
        tableViewer.getTable().setSelection(index);
    }


    /* ****************************************
     * 編集機能
     */
    /**
     * セル編集機能をつける
     * 
     * @param cellModifier
     */
    public void setCellModifier(ICellModifier cellModifier) {
        tableViewer.setCellModifier(cellModifier);
    }


    /**
     * セルエディタをつける<br />
     * 呼ばれなければ、TextCellEditorがつく
     * 
     * @param editors
     */
    public void setCellEditors(CellEditor[] editors) {
        tableViewer.setCellEditors(editors);
    }


    /**
     * カラムプロパティを指定する<br />
     * 呼ばれなければ、コンストラクタに渡された<code>String[] columns</code>が設定されている
     * 
     * @param columnProperties
     */
    public void setColumnProperties(String[] columnProperties) {
        tableViewer.setColumnProperties(columnProperties);
    }

    /* ****************************************
     * 編集可能なカラムを管理
     */
    private boolean[] editable;


    /**
     * index番目の列が編集可能か
     * 
     * @return
     */
    public boolean getEditable(int index) {
        return editable[index];
    }


    /**
     * @return 全列の編集可否を格納した配列
     */
    public boolean[] getEditable() {
        return Arrays.copyOf(editable, editable.length);
    }


    /**
     * index番目の列の編集可否を変更する
     * 
     * @param editable
     * @param index
     */
    public void setEditable(boolean editable, int index) {
        if ((index >= 0) && (index < this.editable.length)) {
            this.editable[index] = editable;
        }
    }


    /**
     * 各カラムを、指定されたeditableに変更する
     * 
     * @param editable
     */
    public void setEditable(boolean[] editable) {
        if (this.editable.length == editable.length) {
            this.editable = editable.clone();
        } else if (this.editable.length < editable.length) {
            // 今より多い分は無視
            for (int i = 0; i < this.editable.length; i++) {
                this.editable[i] = editable[i];
            }
        } else {
            // 足りない分は変更しない
            for (int i = 0; i < editable.length; i++) {
                this.editable[i] = editable[i];
            }
        }
    }


    /**
     * 全カラムの編集可否をeditableに変更する。デフォルト設定では、すべてtrueになっている
     * 
     * @param editable
     */
    public void setEditable(boolean editable) {
        for (int i = 0; i < this.editable.length; i++) {
            this.editable[i] = editable;
        }
    }

    /* ****************************************
     * private create method
     */
    private TableViewer tableViewer;


    private void createTable(Composite parent, int style, int[] widths, String label) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        composite.setFont(parent.getFont());

        if (label != null) {
            Label tableLabel = new Label(composite, SWT.NONE);
            tableLabel.setText(label);
        }

        tableViewer = new TableViewer(composite, style);
        tableViewer.setContentProvider(new ArrayContentProvider());

        Table table = tableViewer.getTable();
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.heightHint = 120;
        table.setLayoutData(layoutData);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        for (int i = 0; i < columns.length; i++) {
            createColumn(table, columns[i], widths[i]);
        }
        table.pack();

        // 編集の初期設定
        tableViewer.setColumnProperties(columns);
        TextCellEditor[] editors = new TextCellEditor[columns.length];
        for (int i = 0; i < editors.length; i++) {
            editors[i] = new TextCellEditor(tableViewer.getTable());
        }
        tableViewer.setCellEditors(editors);
        tableViewer.setCellModifier(new CellModifier()); // しかし、使い物にならないはず

        setEditKeyListener(tableViewer, 0);
    }


    private static void setEditKeyListener(final TableViewer viewer, final int targetColumn) {
        viewer.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
            }


            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.character) {
                case SWT.CR:
                    EditorUtil.editMode(viewer, targetColumn);
                    break;
                default:
                    break;
                }
            }
        });
    }


    private static TableColumn createColumn(Table parent, String title, int width) {
        TableColumn column = new TableColumn(parent, SWT.NONE);
        column.setText(title);
        if (width != -1) {
            column.setWidth(width);
        } else {
            column.pack();
        }
        return column;
    }


    /* ****************************************
     * implements default CellModifier
     * 引数elementは使用されていないが、
     * このクラスを継承しメソッドをオーバーライドした際に利用される
     */
    public boolean canModify(@SuppressWarnings("unused") Object element, String property) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(property)) {
                return editable[i];
            }
        }
        return false;
    }

    class CellModifier implements ICellModifier {
        @Override
        public boolean canModify(Object element, String property) {
            return EditableTable.this.canModify(element, property);
        }


        @Override
        public Object getValue(Object element, String property) {
            return element.toString();
        }


        @Override
        public void modify(Object element, String property, Object value) {
            ((TableItem) element).setData(value);
            ((TableItem) element).setText(String.valueOf(value));
            update(value);
        }
    }


    /* ****************************************
     */
    public void dispose() {
        tableViewer.getControl().dispose();
    }


    public void setFocus() {
        tableViewer.getTable().setFocus();
    }


    public void setEnabled(boolean enabled) {
        tableViewer.getControl().setEnabled(enabled);
    }
}
