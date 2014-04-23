/**
 * @version $Id: ResultPatternPage2.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/09/02 21:49:11
 * @author imai
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.ui.TIDA;

/**
 * 構文解析（パターンマッチング）結果ビュー
 * 
 * @author imai
 */
public class ResultPatternPage2 extends ResultPatternPage {
    private IEditorInput input;


    public ResultPatternPage2(FormEditor editor) {
        this(editor, "ResultPatternPage.UniqueIdentifier", "レコード別");
    }


    public ResultPatternPage2(FormEditor editor, IEditorInput input) {
        this(editor, "ResultPatternPage.UniqueIdentifier", "レコード別");
        this.input = input;
        this.formTitle = "構文解析フォーム（レコード別）";
    }


    /**
     * @param editor
     * @param id
     * @param title
     */
    public ResultPatternPage2(FormEditor editor, String id, String title) {
        super(editor, id, title);
        this.formTitle = "構文解析フォーム（レコード別）";
    }


    @Override
    protected void createPatternViewer(Composite parent) {
        Tree resultTree = new Tree(parent, SWT.FULL_SELECTION | SWT.VIRTUAL);
        resultTree.setHeaderVisible(true);
        resultTree.setLinesVisible(true);
        resultTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        CompositeUtil.createColumn(resultTree, "Rec#", 150);
        CompositeUtil.createColumn(resultTree, "解析対象テキスト", 350);
        CompositeUtil.createColumn(resultTree, "パターン数", 80);
        // TODO These are show in PropertyView
        // CompositeUtil.createColumn(resultTree, "Claim#", 60);
        // CompositeUtil.createColumn(resultTree, "Field#", 60);

        configureTree(resultTree);

        resultTreeViewer = new TreeViewer(resultTree);
        resultTreeViewer.setContentProvider(new ITreeContentProvider() {
            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }


            @Override
            public void dispose() {
            }


            @Override
            public boolean hasChildren(Object element) {
                return (element instanceof IResultCoronaPattern) || (element instanceof IPattern);
            }


            @Override
            public Object getParent(Object element) {
                ResultPatternEditorInput2 input = (ResultPatternEditorInput2) getEditorInput();
                if (element instanceof IPattern) {
                    return input.getParent((IPattern) element);
                }
                return null;
            }


            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof ResultPatternEditorInput2) {
                    return ((ResultPatternEditorInput2) inputElement).getElements();
                }
                return new Object[0];
            }


            @Override
            public Object[] getChildren(Object parentElement) {
                ResultPatternEditorInput2 input = (ResultPatternEditorInput2) getEditorInput();
                if (parentElement instanceof IResultCoronaPattern) {
                    return input.getChildren((IResultCoronaPattern) parentElement);
                }
                /* TODO プロパティで見られるし、３階層目にパターン分類を出す意味ってないよね */
                if (parentElement instanceof IPattern) {
                    return input.getChildren((IPattern) parentElement);
                }
                return new Object[0];
            }
        });

        resultTreeViewer.setLabelProvider(new ColorTableLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                ResultPatternEditorInput2 input = (ResultPatternEditorInput2) getEditorInput();

                if (element instanceof PatternType) {
                    PatternType patternType = (PatternType) element;
                    if (columnIndex == 0) {
                        return patternType.getPatternName();
                    }

                } else if (element instanceof IPattern) {
                    IPattern pattern = (IPattern) element;
                    if (columnIndex == 0) {
                        return pattern.getLabel();
                    }

                } else if (element instanceof IResultCoronaPattern) {
                    IResultCoronaPattern result = (IResultCoronaPattern) element;
                    switch (columnIndex) {
                    case 0:
                        return String.valueOf(result.getRecordId());
                    case 1: // text
                        return (result.getText());
                    case 2: // 件数
                        // TODO 要テスト。#817
                        return String.valueOf(result.getHitPositions(0).size()) + " 種";
                    default:
                        break;
                    }
                } else if (element instanceof IRecord) {
                    IRecord rec = (IRecord) element;
                    switch (columnIndex) {
                    case 0:
                        return String.valueOf(rec.getRecordId());
                    case 1: // text
                        return (String) (rec.getField(input.fieldID).getValue());
                    case 2: // 件数
                        return " -";
                    default:
                        break;
                    }

                }
                return "";
            }

            private final Color URL_COLOR = new Color(null, 0, 5, 255);


            @Override
            public Color getForeground(Object element, int columnIndex) {
                String value = getColumnText(element, columnIndex);
                if (value.matches(TIDA.URL_REGEX)) {
                    return URL_COLOR;
                }
                return super.getForeground(element, columnIndex);
            }
        });

        resultTreeViewer.setInput(getEditorInput());
        if (resultTreeViewer.getTree().getItemCount() > 0) {
            resultTreeViewer.expandToLevel(resultTreeViewer.getTree().getTopItem().getData(), 1);
        }
        configureToolTip(resultTreeViewer.getTree());
    }


    @Override
    protected void configureTree(final Tree tree) {
        final int fieldID;
        final IEditorInput editorInput = getEditorInput();
        if (editorInput instanceof IResultPatternEditorInput) {
            fieldID = ((IResultPatternEditorInput) editorInput).getFieldId();
        } else {
            return;
        }
        tree.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem item = (TreeItem) e.item;

                if (item.getData() instanceof IResultCoronaPattern) {
                    SyntaxStructure importText = new SyntaxStructure(getInputText((IResultCoronaPattern) item.getData()));
                    /* テキストエリアに表示する解析元テキストを作る。 */
                    StringBuilder token = new StringBuilder(200);
                    for (MorphemeElement me : importText.getMorphemeElemsnts()) {
                        token.append(me.getHyouki());
                    }
                    /* 表示テキスト(パターン解析対象テキスト)をセット */
                    text.setText(token.toString());

                } else if (item.getData() instanceof IRecord) {
                    IRecord record = (IRecord) item.getData();
                    IField field = record.getField(fieldID);
                    if ((field != null) && (field.getValue() != null)) {
                        text.setText(field.getValue().toString());
                    } else {
                        text.setText("");
                    }
                } else {
                    text.setText("");
                }
            }
        });

        tree.setCursor(new Cursor(null, SWT.CURSOR_ARROW));
        tree.addMouseMoveListener(new MouseMoveListener() {
            private Cursor defaultCursor = new Cursor(null, SWT.CURSOR_ARROW);
            private Cursor handCursor = new Cursor(null, SWT.CURSOR_HAND);


            @Override
            public void mouseMove(MouseEvent e) {
                ViewerCell cell = resultTreeViewer.getCell(new Point(e.x, e.y));
                if (cell != null) {
                    String value = cell.getText();
                    if (value.matches(TIDA.URL_REGEX)) {
                        tree.setCursor(handCursor);
                    } else {
                        tree.setCursor(defaultCursor);
                    }
                } else {
                    tree.setCursor(defaultCursor);
                }
            }
        });
    }


    @Override
    public IEditorInput getEditorInput() {
        return input;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySheetPage.class)) {
            return new PropertySheetPage();
        }
        return super.getAdapter(adapter);
    }
}
