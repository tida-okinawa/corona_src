/**
 * @version $Id: ResultPatternPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/09/02 21:49:11
 * @author imai
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import com.tida_okinawa.corona.common.Range;
import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternDecoder;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.cleansing.HitPosition;
import com.tida_okinawa.corona.io.model.cleansing.HitPositionConverter;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.io.service.IIoService;
import com.tida_okinawa.corona.ui.TIDA;
import com.tida_okinawa.corona.ui.editors.ResultMorphemeEditor.TableLabelProvider;
import com.tida_okinawa.corona.ui.editors.pattern.PatternDicContentProvider;
import com.tida_okinawa.corona.ui.editors.pattern.TmpPatternDicLabelProvider;

/**
 * 構文解析（パターンマッチング）結果ビュー
 * 
 * @author imai
 */
public class ResultPatternPage extends FormPage {
    protected String formTitle;


    /**
     * @param editor
     *            親エディター
     */
    public ResultPatternPage(FormEditor editor) {
        this(editor, "ResultPatternPage.UniqueIdentifier", "パターン別"); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * 
     * @param editor
     *            親エディター
     * @param id
     *            ページID
     * @param title
     *            ページタイトル
     */
    public ResultPatternPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        this.formTitle = "構文解析フォーム（パターン別）"; //$NON-NLS-1$
    }

    private IPartListener partListener = new IPartListener() {
        @Override
        public void partOpened(IWorkbenchPart part) {
        }


        @Override
        public void partDeactivated(IWorkbenchPart part) {
            if (tooltip != null) {
                /* 破棄 */
                tooltip.dispose();
                tooltip = null;
            }
        }


        @Override
        public void partClosed(IWorkbenchPart part) {
        }


        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
        }


        @Override
        public void partActivated(IWorkbenchPart part) {
        }
    };


    @Override
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);
        site.getPage().addPartListener(partListener);
    }


    @Override
    public void dispose() {
        super.dispose();
        getSite().getPage().removePartListener(partListener);
    }


    @Override
    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
        form.setText(formTitle);
        form.getBody().setLayout(new GridLayout());
        form.getBody().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite composite = CompositeUtil.defaultComposite(form.getBody(), 1);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // 原文
        createTextArea(sashForm);
        /* 構文解析結果 */
        SashForm resultSash = new SashForm(sashForm, SWT.HORIZONTAL);
        createPatternViewer(resultSash);
        createRecordViewer(resultSash);

        sashForm.setWeights(new int[] { 2, 8 });
        resultSash.setWeights(new int[] { 2, 1 });
    }

    /**
     * 原文を表示
     */
    StyledText text;


    protected void createTextArea(Composite parent) {
        text = new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        text.setEditable(false);
        GridData gdText = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
        gdText.heightHint = 30;
        text.setLayoutData(gdText);
    }

    /**
     * 結果表示部分
     */
    TreeViewer resultTreeViewer;


    protected void createPatternViewer(Composite parent) {
        Tree resultTree = new Tree(parent, SWT.FULL_SELECTION | SWT.VIRTUAL);
        resultTree.setHeaderVisible(true);
        resultTree.setLinesVisible(true);
        resultTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        CompositeUtil.createColumn(resultTree, "パターン", 150); //$NON-NLS-1$
        CompositeUtil.createColumn(resultTree, "解析対象テキスト", 260); //$NON-NLS-1$
        CompositeUtil.createColumn(resultTree, "Rec#", 50); //$NON-NLS-1$
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
                return (element instanceof IPattern) || (element instanceof PatternType);
            }


            @Override
            public Object getParent(Object element) {
                ResultPatternEditorInput input = (ResultPatternEditorInput) getEditorInput();
                if (element instanceof IPattern) {
                    return input.getParent((IPattern) element);
                }
                return null;
            }


            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof ResultPatternEditorInput) {
                    return ((ResultPatternEditorInput) inputElement).getElements();
                }
                return new Object[0];
            }


            @Override
            public Object[] getChildren(Object parentElement) {
                ResultPatternEditorInput input = (ResultPatternEditorInput) getEditorInput();
                if (parentElement instanceof PatternType) {
                    return input.getChildren((PatternType) parentElement);
                }
                if (parentElement instanceof IPattern) {
                    return input.getChildren((IPattern) parentElement);
                }
                return new Object[0];
            }
        });

        resultTreeViewer.setLabelProvider(new ColorTableLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                ITreeContentProvider cp = (ITreeContentProvider) resultTreeViewer.getContentProvider();
                ResultPatternEditorInput input = (ResultPatternEditorInput) getEditorInput();

                if (element instanceof PatternType) {
                    PatternType patternType = (PatternType) element;
                    switch (columnIndex) {
                    case 0:
                        return patternType.getPatternName();
                    case 1:
                        // 件数 = クレームの件数 - 該当なしの件数
                        // IClaimData claimData =
                        // Activator.getService().getClaimData(input.claimWorkPattern.getClaimId());
                        Set<Integer> counter = new HashSet<Integer>();
                        for (Object child : input.getChildren(patternType)) {
                            IPattern pattern = (IPattern) child;
                            if (!"該当なし".equals(pattern.getLabel())) { //$NON-NLS-1$
                                for (Object o : cp.getChildren(child)) {
                                    if (o instanceof IResultCoronaPattern) {
                                        IResultCoronaPattern result = (IResultCoronaPattern) o;
                                        counter.add(result.getRecordId());
                                    }
                                }
                            } else {
                            }
                        }
                        return (counter.size()) + " 件"; //$NON-NLS-1$
                    }
                } else if (element instanceof IPattern) {
                    IPattern pattern = (IPattern) element;
                    switch (columnIndex) {
                    case 0:
                        return pattern.getLabel();
                    case 1:
                        Object[] children = cp.getChildren(element);
                        int n = (children == null) ? 0 : children.length;
                        return n + " 件"; //$NON-NLS-1$
                    }
                } else if (element instanceof IResultCoronaPattern) {
                    IResultCoronaPattern result = (IResultCoronaPattern) element;
                    switch (columnIndex) {
                    case 0:
                        break;
                    case 1: // text
                        return (result.getText());
                    case 2: // record id
                        return String.valueOf(result.getRecordId());
                    default:
                        break;
                    }
                }
                return ""; //$NON-NLS-1$
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

    /**
     * Tableだと左のツリーと行幅がずれるのでTree
     */
    TreeViewer recordTreeViewer;


    protected void createRecordViewer(Composite parent) {
        final int claimID;
        final int currFieldId;
        final IEditorInput editorInput = getEditorInput();
        if (editorInput instanceof IResultPatternEditorInput) {
            claimID = ((IResultPatternEditorInput) editorInput).getClaimId();
            currFieldId = ((IResultPatternEditorInput) editorInput).getFieldId();
        } else {
            return;
        }

        final Tree recordTree = new Tree(parent, SWT.FULL_SELECTION);
        recordTree.setLinesVisible(true);
        recordTree.setHeaderVisible(true);
        CompositeUtil.createColumn(recordTree, "列名", 80); //$NON-NLS-1$
        CompositeUtil.createColumn(recordTree, "値", 200); //$NON-NLS-1$

        recordTreeViewer = new TreeViewer(recordTree);

        IIoService service = IoActivator.getService();
        final IClaimData claim = service.getClaimData(claimID);
        final List<IFieldHeader> fields = new ArrayList<IFieldHeader>(claim.getFieldInformations());

        int pF = claim.getProductField();
        for (Iterator<IFieldHeader> itr = fields.iterator(); itr.hasNext();) {
            IFieldHeader f = itr.next();
            int id = f.getId();
            /* 表示する必要のない列を除去 */
            if (!((id != 1) && (id != pF) && (id != currFieldId))) {
                itr.remove();
            }
        }
        recordTreeViewer.setContentProvider(new ITreeContentProvider() {
            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }


            @Override
            public void dispose() {
            }


            @Override
            public boolean hasChildren(Object element) {
                return false;
            }


            @Override
            public Object getParent(Object element) {
                return null;
            }


            @Override
            public Object[] getElements(Object input) {
                final int claimID;
                final IEditorInput editorInput = getEditorInput();
                if (editorInput instanceof IResultPatternEditorInput) {
                    claimID = ((IResultPatternEditorInput) editorInput).getClaimId();
                } else {
                    return new Object[0];
                }
                if (input instanceof IResultCoronaPattern) {
                    IResultCoronaPattern result = (IResultCoronaPattern) input;
                    IClaimData claim = IoActivator.getService().getClaimData(claimID);
                    int recordId = result.getRecordId();
                    IRecord rec = claim.getRecord(recordId);
                    if (rec != null) {
                        Object[] ret = new Object[fields.size()];
                        int i = 0;
                        for (Iterator<IFieldHeader> itr = fields.iterator(); itr.hasNext();) {
                            IField field = rec.getField(itr.next().getId());
                            ret[i++] = field;
                        }
                        return ret;
                    }
                } else if (input instanceof IRecord) {
                    IRecord rec = (IRecord) input;
                    Object[] ret = new Object[fields.size()];
                    int i = 0;
                    for (Iterator<IFieldHeader> itr = fields.iterator(); itr.hasNext();) {
                        IField field = rec.getField(itr.next().getId());
                        ret[i++] = field;
                    }
                    return ret;
                }
                return new Object[0];
            }


            @Override
            public Object[] getChildren(Object parent) {
                return new Object[0];
            }
        });
        recordTreeViewer.setLabelProvider(new ColorTableLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof IField) {
                    switch (columnIndex) {
                    case 0:
                        return ((IField) element).getHeader().getDispName();
                    case 1:
                        // TODO 20131224 null対策
                        /*
                         * シート名が問合せデータに欠落している場合に構文解析結果画面でエラー(nullが原因の例外エラー)
                         * が発生する件への対応
                         */
                        if (((IField) element).getValue() == null) {
                            return ""; //$NON-NLS-1$
                        }
                        return ((IField) element).getValue().toString();
                    }
                }
                return ""; //$NON-NLS-1$
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

        recordTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (((IStructuredSelection) event.getSelection()).getFirstElement() instanceof IField) {
                    IField item = (IField) ((IStructuredSelection) event.getSelection()).getFirstElement();
                    String value = item.getValue().toString();
                    text.setText(value);
                } else {
                    /* リスナーを後に追加したため、左ツリーのsetTextを上書きしてしまうことがある。 */
                    if (recordTreeViewer.getTree().isFocusControl()) {
                        text.setText(""); //$NON-NLS-1$
                    }
                }
            }
        });
        recordTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                TreeItem item = recordTree.getItem(new Point(e.x, e.y));
                if (item != null) {
                    IField field = (IField) item.getData();
                    openBrowser(field.getValue().toString());
                }
            }
        });
        recordTree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if ((e.item != null) && (e.item.getData() instanceof IField)) {
                    IField field = (IField) e.item.getData();
                    openBrowser(field.getValue().toString());
                }
            }
        });
        recordTree.addMouseMoveListener(new MouseMoveListener() {
            private Cursor defaultCursor = new Cursor(null, SWT.CURSOR_ARROW);
            private Cursor handCursor = new Cursor(null, SWT.CURSOR_HAND);


            @Override
            public void mouseMove(MouseEvent e) {
                ViewerCell cell = recordTreeViewer.getCell(new Point(e.x, e.y));
                if (cell != null) {
                    String value = cell.getText();
                    if (value.matches(TIDA.URL_REGEX)) {
                        recordTree.setCursor(handCursor);
                    } else {
                        recordTree.setCursor(defaultCursor);
                    }
                } else {
                    recordTree.setCursor(defaultCursor);
                }
            }
        });
        resultTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) resultTreeViewer.getSelection();
                recordTreeViewer.setInput(selection.getFirstElement());
            }
        });
    }


    /**
     * 選択した解析結果の解析元テキストを取得する
     * 
     * @param resultCoronaPattern
     *            パターンマッチ結果
     * @return 解析元テキスト
     */
    protected String getInputText(IResultCoronaPattern resultCoronaPattern) {
        String textData = ""; //$NON-NLS-1$

        IClaimWorkPattern workPattern = ((IResultPatternEditorInput) getEditorInput()).getClaimWorkPattern();
        List<IUIElement> uiElements = CoronaModel.INSTANCE.adapter(workPattern);
        if (uiElements.size() > 0) {
            IUIProduct uiProduct = (IUIProduct) uiElements.get(0).getParent().getParent();
            ICoronaProduct product = uiProduct.getObject();
            String dataType = ""; //$NON-NLS-1$

            /* ClaimWorkDataTypeを取得する */
            String[] types = workPattern.getNote().split(","); //$NON-NLS-1$
            /* 　入力データ種別と自分の種別の２つはあるはず　 */
            if (types.length > 1) {
                dataType = types[types.length - 2];
            }
            /* 　形態素係り受け時はKNPフラグが付加されているので分ける　 */
            if (dataType.contains(":")) { //$NON-NLS-1$
                dataType = dataType.substring(0, dataType.indexOf(":")); //$NON-NLS-1$
            }

            // クレンジング元履歴IDを取得する
            int formerHistoryId = workPattern.getFormerHistoryId();

            // クレンジング元履歴IDと問い合わせデータで取得したIDより解析元のワークデータを取得する
            IClaimWorkData workData = product.getClaimWorkData(workPattern.getClaimId(), ClaimWorkDataType.valueOfName(dataType), workPattern.getFieldId());
            textData = workData.getClaimWorkData(resultCoronaPattern.getRecordId(), formerHistoryId);
        }

        return textData;
    }


    protected void configureTree(final Tree tree) {
        final int fieldID;
        final IEditorInput editorInput = getEditorInput();
        if (editorInput instanceof IResultPatternEditorInput) {
            fieldID = ((IResultPatternEditorInput) editorInput).getFieldId();
        } else {
            return;
        }
        tree.addSelectionListener(new SelectionAdapter() {
            Color hitInfoFore = new Color(null, 255, 0, 255);

            TreeItem currentItem = null;


            @Override
            public void widgetSelected(SelectionEvent event) {

                TreeItem item = (TreeItem) event.item;
                /* 同じものを選択した時は処理を行わない。 */
                if (item.equals(currentItem)) {
                    return;
                }
                currentItem = item;
                if (item.getData() instanceof IResultCoronaPattern) {
                    IResultCoronaPattern resultPattern = (IResultCoronaPattern) item.getData();
                    /* Hit箇所に色付けを行う処理 */
                    List<StyleRange> styleRanges = new ArrayList<StyleRange>();
                    SyntaxStructure importText = new SyntaxStructure(getInputText(resultPattern));

                    Range r = new Range();
                    /* TODO ClaimWorkData#getHistoryId が IF に公開されていないため、取得できない */
                    // int history = ((ResultPatternEditorInput) getEditorInput()).getClaimWorkPattern().getHistoryId();
                    Map<IPattern, List<String>> hitPositions = resultPattern.getHitPositions(0);
                    for (Entry<IPattern, List<String>> e : hitPositions.entrySet()) {
                        if (((IPattern) item.getParentItem().getData()).equals(e.getKey())) {
                            for (String hitString : e.getValue()) {
                                HitPosition hitPosition = HitPositionConverter.convertHitPosition(hitString);
                                int[] hitRange = hitPosition.getRange();
                                hitRange[1] += hitRange[0] - 1;
                                r.add(hitRange);
                            }
                        }
                    }
                    r.marge();
                    for (int[] pos : r.getRanges()) {
                        pos = calcHighlightRange(importText, pos);
                        styleRanges.add(new StyleRange(pos[0], pos[1], hitInfoFore, null));
                    }

                    /* テキストエリアに表示する解析元テキストを作る。 */
                    StringBuilder token = new StringBuilder(200);
                    for (MorphemeElement me : importText.getMorphemeElemsnts()) {
                        token.append(me.getHyouki());
                    }
                    /* 表示テキスト(パターン解析対象テキスト)をセット */
                    text.setText(token.toString());
                    /* 色付け */
                    text.setStyleRanges(styleRanges.toArray(new StyleRange[styleRanges.size()]));

                } else if (item.getData() instanceof IRecord) {
                    IRecord record = (IRecord) item.getData();
                    IField field = record.getField(fieldID);
                    if ((field != null) && (field.getValue() != null)) {
                        text.setText(field.getValue().toString());
                    } else {
                        text.setText(""); //$NON-NLS-1$
                    }

                } else {
                    text.setText(""); //$NON-NLS-1$
                }
            }


            /**
             * ヒット位置（形態素）をハイライト位置（文字数）に変換する
             */
            int[] calcHighlightRange(SyntaxStructure ss, int[] matchPosition) {
                int matchPoint[] = new int[2];
                StringBuilder matchText = new StringBuilder(200);
                StringBuilder previousText = new StringBuilder(1000);

                int i = 0;
                for (; i < matchPosition[0]; i++) {
                    previousText.append(ss.getMorphemeElemsnts().get(i).getHyouki());
                }

                int matchPos = matchPosition[1];
                if (ss.getMorphemeElemsnts().size() <= matchPosition[1]) {
                    matchPos = ss.getMorphemeElemsnts().size() - 1;
                }

                for (; i <= matchPos; i++) {
                    matchText.append(ss.getMorphemeElemsnts().get(i).getHyouki());
                }

                matchPoint[0] = previousText.length();/* 開始位置 */
                matchPoint[1] = matchText.length();/* マッチ箇所の文字列長 */
                return matchPoint;
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


    void openBrowser(String url) {
        TIDA.openBrowser(url, "構文解析結果", "ブラウザ", null); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private Shell tooltip;


    protected void configureToolTip(final Tree tree) {
        tree.setToolTipText(""); // cancel default tool tip //$NON-NLS-1$

        final Listener listener = new Listener() {

            Label label;
            TreeViewer viewer;


            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Deactivate:
                case SWT.Dispose:
                case SWT.KeyDown:
                case SWT.MouseMove: {
                    if (tooltip == null) {
                        break;
                    }
                    /* 破棄 */
                    tooltip.dispose();
                    tooltip = null;
                    label = null;
                    viewer = null;
                    break;
                }
                case SWT.MouseHover:
                    final IEditorInput editorInput = getEditorInput();
                    final IClaimWorkPattern cwp;
                    if (editorInput instanceof IResultPatternEditorInput) {
                        cwp = ((IResultPatternEditorInput) editorInput).getClaimWorkPattern();
                    } else {
                        return;
                    }
                    /* ツールチップ作成 */
                    Point point = new Point(event.x, event.y);
                    TreeItem item = tree.getItem(point);
                    if (item != null) {
                        if ((item.getData() instanceof IPattern) && (item.getBounds(0).contains(point))) {
                            /* パターン名にカーソルを当てているときだけ表示する */
                            IPattern iPattern = (IPattern) item.getData();
                            // TODO:仮対応。一時データのパターンを表示する。本来はitemデータの設定時に下記データを設定する
                            iPattern = cwp.getPattern(iPattern.getId());

                            PatternDecoder decoder = new PatternDecoder();

                            if (iPattern == null || iPattern.getText() == null) {
                                return;
                            }

                            Pattern ptn = decoder.encode(iPattern.getText());
                            if (ptn != null) {
                                final Color background = tree.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
                                final Color foreground = tree.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);

                                tooltip = new Shell(tree.getShell(), SWT.ON_TOP | SWT.RESIZE | SWT.TOOL);
                                tooltip.setLayout(CompositeUtil.gridLayout(1, 2, 2, 0, 0));
                                tooltip.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                                tooltip.setBackground(background);

                                /* パターン名 */
                                label = new Label(tooltip, SWT.NONE);
                                label.setText(iPattern.getLabel());
                                label.setBackground(background);
                                label.setForeground(foreground);

                                /* パターンツリー */
                                viewer = new TreeViewer(tooltip, SWT.NONE);
                                viewer.setContentProvider(new PatternDicContentProvider());
                                viewer.setLabelProvider(new TmpPatternDicLabelProvider(cwp.getPatternDics().get(0)));
                                viewer.setInput(new PatternRecord(iPattern));
                                // viewer.setInput(new
                                // PatternRecords(cwp.getPatternDics().get(0)));
                                viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                                viewer.getTree().setBackground(background);
                                viewer.getTree().setForeground(foreground);

                                /* 位置、サイズ指定 */
                                Rectangle bound = item.getBounds(0);
                                Point dispPoint = item.getParent().toDisplay(bound.x, bound.y);
                                tooltip.setBounds(dispPoint.x, dispPoint.y, 300, 200);
                                tooltip.setVisible(true);
                            }
                            break;
                        }
                    }
                    break;
                default:
                    break;
                }
            }
        };
        tree.addListener(SWT.Dispose, listener);
        tree.addListener(SWT.KeyDown, listener);
        tree.addListener(SWT.MouseMove, listener);
        tree.addListener(SWT.MouseHover, listener);
    }

    protected static class ColorTableLabelProvider extends TableLabelProvider implements ITableColorProvider, IFontProvider {
        public ColorTableLabelProvider() {
        }


        @Override
        public Color getForeground(Object element, int columnIndex) {
            return null;
        }


        @Override
        public Color getBackground(Object element, int columnIndex) {
            return null;
        }


        @Override
        public Font getFont(Object element) {
            return null;
        }
    }


    /* ****************************************
     * 保存
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
    }


    @Override
    public void doSaveAs() {
    }


    @Override
    public boolean isDirty() {
        return false;
    }


    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }


    @Override
    public void setFocus() {
        resultTreeViewer.getControl().setFocus();
    }


    @Override
    public String getTitleToolTip() {
        return getEditorInput().getToolTipText();
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySheetPage.class)) {
            return new PropertySheetPage();
        }
        return super.getAdapter(adapter);
    }
}
