/**
 * @version $Id: ClaimDataEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/29 17:22:05
 * @author shingo_wakamatsu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.ui.Icons;


/**
 * @author shingo_wakamatsu
 */
public class ClaimDataEditor extends EditorPart {

    /** エディターID */
    public static final String EDITOR_ID = "com.tida_okinawa.corona.ui.editors.ClaimDataEditor";


    /** コンストラクター */
    public ClaimDataEditor() {
    }

    private IClaimData claim;
    private ClaimDataEditorInput editorInput;


    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        // 必須処理
        setSite(site);
        setInput(input);

        assert input instanceof ClaimDataEditorInput;
        editorInput = (ClaimDataEditorInput) input;
        claim = editorInput.getClaim();
    }


    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, false));
        createUI(parent);
        setTraverse();

        viewer.setInput(claim.getRecords());
    }

    private TableViewer viewer;
    Text text;


    private void createUI(Composite parent) {
        // テキスト表示領域とテーブルとの境界線
        SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // テキスト表示領域
        text = new Text(sashForm, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        text.setEditable(false);
        GridData gdText = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
        gdText.heightHint = 15; // 縦幅の下限
        text.setLayoutData(gdText);

        viewer = new TableViewer(sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.VIRTUAL);
        viewer.setContentProvider(new ClaimDataContentProvider(claim.getProductField(), editorInput.getProductName(), editorInput.getUIWork()));
        if (editorInput.getUIWork() != null) {
            viewer.setLabelProvider(new ClaimDataLabelProvider(editorInput.getUIWork()));
        } else {
            viewer.setLabelProvider(new ClaimDataLabelProvider(editorInput.getClaim(), true));
        }

        // テーブル作成
        Table tbl = viewer.getTable();
        tbl.setToolTipText(""); // cancel tool tip
        tbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tbl.setHeaderVisible(true);

        // テーブルカラムにテキストを設定
        {
            /* ID列は狭くていい */
            TableColumn column = new TableColumn(tbl, SWT.LEFT);
            column.setText("ID");
            column.setWidth(60);
        }
        Object[] fields = claim.getFieldInformations().toArray();
        for (int i = 1; i < fields.length; i++) {
            TableColumn column = new TableColumn(tbl, SWT.LEFT);
            column.setText(((IFieldHeader) fields[i]).getName());
            column.setWidth(150);
        }

        // 境界線の位置を調整　上部20％　下部80％
        // テキスト表示領域、テーブルの諸設定をした後のこの位置で指定しないとエラー
        sashForm.setWeights(new int[] { 2, 8 });

        getSite().setSelectionProvider(viewer);
    }


    private void setTraverse() {
        // セル（アイテム）単位で選択可能に設定
        final Table tbl = viewer.getTable();
        final TableCursor cursor = new TableCursor(tbl, SWT.NULL);

        // セルが選択状態になった場合
        cursor.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem item = cursor.getRow();
                int column = cursor.getColumn();
                text.setText(item.getText(column));

                int row = tbl.indexOf(item);
                // セルの選択カーソルの移動に合わせて、行の選択カーソルも移動
                tbl.select(row);
            }
        });

        // 矢印キーやEnterキーなど、カーソルの移動に関するイベントが発生した場合
        cursor.addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event e) {
                TableItem item = cursor.getRow();
                if (item == null) {
                    return;
                }

                int column = cursor.getColumn();
                int row = tbl.indexOf(item);

                switch (e.detail) {
                case SWT.TRAVERSE_TAB_NEXT:
                    // Tabキー押下
                    // Eclipseでデフォルトで働いている「フォーカスマネージャ」によるタブ移動を無効化
                    e.doit = false;
                    if (tbl.getColumnCount() - 1 > column) {
                        // 右のセルにカーソル移動
                        cursor.setSelection(row, column + 1);
                        text.setText(item.getText(column + 1));
                    } else {
                        if (tbl.getItemCount() - 1 > row) {
                            // 1行下最左のセルにカーソル移動
                            cursor.setSelection(row + 1, 0);
                            // 行（アイテム）データ取り直し
                            item = cursor.getRow();
                            text.setText(item.getText(0));
                            // セルの選択カーソルの移動に合わせて、行の選択カーソルも移動
                            tbl.select(row + 1);
                        } else {
                            // テーブルの最下最右のセルにフォーカスがある場合、セルのフォーカス移動しない
                        }
                    }
                    break;

                case SWT.TRAVERSE_TAB_PREVIOUS:
                    // Shift+Tabキー押下
                    // Eclipseでデフォルトで働いている「フォーカスマネージャ」によるタブ移動を無効化
                    e.doit = false;
                    if (column > 0) {
                        // 左のセルにカーソル移動
                        cursor.setSelection(row, column - 1);
                        text.setText(item.getText(column - 1));
                    } else {
                        if (0 < row) {
                            // 1行上最右のセルにカーソル移動
                            cursor.setSelection(row - 1, tbl.getColumnCount() - 1);
                            // 行（アイテム）データ取り直し
                            // item = cursor.getRow();
                            text.setText(item.getText(0));
                            // セルの選択カーソルの移動に合わせて、行の選択カーソルも移動
                            tbl.select(row - 1);
                        } else {
                            // テーブルの最下最左のセルにフォーカスがある場合、セルのフォーカス移動しない
                        }
                    }
                    break;

                default:
                    break;
                }
            }
        });
    }


    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }


    @Override
    public void doSave(IProgressMonitor monitor) {
        // nothing to do
    }


    @Override
    public void doSaveAs() {
        // nothing to do
    }


    @Override
    public boolean isDirty() {
        // nothing to do
        return false;
    }


    @Override
    public boolean isSaveAsAllowed() {
        // nothing to do
        return false;
    }


    @Override
    public String getPartName() {
        return getEditorInput().getName();
    }


    @Override
    public Image getTitleImage() {
        IUIWork uiWork = ((ClaimDataEditorInput) getEditorInput()).getUIWork();
        if (uiWork != null) {
            return Icons.INSTANCE.get(Icons.IMG_RESLUT_CORRECTION_MISTAKES);
        }
        return super.getTitleImage();
    }

    private static class ClaimDataContentProvider extends ArrayContentProvider {
        private int productField;
        private String productName;
        private IClaimWorkData work;


        /**
         * @param productField
         *            ターゲットフィールドID
         * @param productName
         *            ターゲット名
         * @param uiWork
         *            誤記補正結果。元の問合せデータを表示したいときはnullでいい。
         */
        public ClaimDataContentProvider(int productField, String productName, IUIWork uiWork) {
            this.productField = productField;
            this.productName = productName;
            if (uiWork != null) {
                this.work = uiWork.getObject();
            }
        }


        @Override
        public Object[] getElements(Object input) {
            Object[] elements = super.getElements(input);
            if (productName != null) {
                List<IRecord> ret = new ArrayList<IRecord>();
                for (Object o : elements) {
                    Object value = ((IRecord) o).getField(productField).getValue();
                    if ((value != null) && (productName.equals(value.toString()))) {
                        if ((work != null) && work.getClaimWorkData(((IRecord) o).getRecordId()) != null) {
                            ret.add((IRecord) o);
                        }
                    }
                }
                return ret.toArray();
            }
            return elements;
        }
    }

    private static class ClaimDataLabelProvider extends LabelProvider implements ITableLabelProvider {
        private Set<Integer> mistakesFieldIds = new HashSet<Integer>();


        /**
         * @param claim
         *            表示する問合せデータ
         * @param original
         *            誤記補正したフィールドの値でも、元データのまま表示する場合はtrue。falseにすると、
         *            誤記補正されたフィールドはすべて誤記補正後の値で表示される。
         */
        public ClaimDataLabelProvider(IClaimData claim, boolean original) {
            if (original) {
                Set<Integer> fields = claim.getCorrectionMistakesFields();
                for (Integer id : fields) {
                    this.mistakesFieldIds.add(id);
                }
            }
        }

        private IUIWork uiWork;


        public ClaimDataLabelProvider(IUIWork uiWork) {
            this.uiWork = uiWork;
            this.mistakesFieldIds.add(uiWork.getObject().getFieldId());
        }


        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }


        @Override
        public String getColumnText(Object element, int columnIndex) {
            IRecord record = (IRecord) element;
            if (mistakesFieldIds.contains(columnIndex + 1)) {
                if (uiWork != null) {
                    return uiWork.getObject().getClaimWorkData(record.getRecordId());
                }
            }

            Object value = record.getField(columnIndex + 1).getValue();
            return (value == null) ? "" : (String) value;
        }
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySheetPage.class)) {
            return new PropertySheetPage();
        }
        return super.getAdapter(adapter);
    }
}
