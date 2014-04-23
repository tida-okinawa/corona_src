/**
 * @version $Id: ResultMorphemeEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/22 11:54:31
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import com.tida_okinawa.corona.correction.morphem.ISyntaxStructureElement;
import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.internal.ui.actions.AbstractCCP;
import com.tida_okinawa.corona.internal.ui.actions.ResultMorphemeEditorCCP;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.table.ITextRecord;
import com.tida_okinawa.corona.ui.Icons;

/**
 * @author takayuki-matsumoto
 */
public class ResultMorphemeEditor extends EditorPart {

    /** エディターID */
    public static final String EDITOR_ID = "com.tida_okinawa.corona.ui.editor.morphemeeditor";
    protected Viewer activeViewer;

    /**
     * 置換後文字列を表示
     */
    protected StyledText resultText;

    /**
     * 置換後データの一覧
     */
    protected TableViewer textTableViewer;

    /**
     * 解析結果
     */
    protected TreeViewer morphemeTreeViewer;


    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(2, false));

        SashForm rootSash = CompositeUtil.defaultSashForm(parent, SWT.VERTICAL);
        createTextArea(rootSash);

        SashForm sashForm = CompositeUtil.defaultSashForm(rootSash, SWT.HORIZONTAL);
        createTextViewer(sashForm);
        createMorphemeViewer(sashForm);

        rootSash.setWeights(new int[] { 2, 8 });
        sashForm.setWeights(new int[] { 1, 1 });

        createActions();
        hookContextMenu();

        getSite().setSelectionProvider(new ISelectionProvider() {
            @Override
            public void addSelectionChangedListener(ISelectionChangedListener listener) {
                textTableViewer.addSelectionChangedListener(listener);
                morphemeTreeViewer.addSelectionChangedListener(listener);
            }


            @Override
            public ISelection getSelection() {
                if (activeViewer == null)
                    return null;
                return activeViewer.getSelection();
            }


            @Override
            public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            }


            @Override
            public void setSelection(ISelection selection) {
            }
        });
    }


    private void createTextArea(Composite parent) {
        // 置換後文字列
        resultText = new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        resultText.setEditable(false);
        GridData gdText = new GridData(SWT.FILL, SWT.FILL, false, false);
        gdText.heightHint = 100;
        resultText.setLayoutData(gdText);
        resultText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                Action copyAction = new Action("コピー") {
                    @Override
                    public void run() {
                        resultText.copy();
                    }


                    @Override
                    public boolean isEnabled() {
                        return resultText.getSelectionCount() > 0;
                    }
                };
                manager.add(copyAction);
            }
        });
        resultText.setMenu(manager.createContextMenu(resultText));
    }


    private void createTextViewer(Composite parent) {
        // 原文の一覧
        textTableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL);
        Table textTable = textTableViewer.getTable();
        textTable.setToolTipText(""); // cancel tool tip
        GridData gdTextList = new GridData(GridData.FILL, SWT.FILL, true, true);
        textTable.setHeaderVisible(true);
        textTable.setLinesVisible(true);
        gdTextList.widthHint = 400;
        textTable.setLayoutData(gdTextList);

        CompositeUtil.createColumn(textTable, "Claim#", 55).setAlignment(SWT.RIGHT);
        CompositeUtil.createColumn(textTable, "Field#", 55).setAlignment(SWT.RIGHT);
        CompositeUtil.createColumn(textTable, "Record#", 60).setAlignment(SWT.RIGHT);
        CompositeUtil.createColumn(textTable, "テキスト", 230);

        textTableViewer.setContentProvider(ArrayContentProvider.getInstance());
        ClaimWorkDataEditorInput input = (ClaimWorkDataEditorInput) getEditorInput();
        IClaimWorkData claimWorkData = input.getClaimWorkData();
        textTableViewer.setInput(claimWorkData.getClaimWorkDatas());
        textTableViewer.setLabelProvider(new TableLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof ITextRecord) {
                    ITextRecord record = (ITextRecord) element;
                    IClaimWorkData claimWorkData = ((ClaimWorkDataEditorInput) getEditorInput()).getClaimWorkData();

                    switch (columnIndex) {
                    case 0:
                        return String.valueOf(claimWorkData.getClaimId());
                    case 1:
                        return String.valueOf(claimWorkData.getFieldId());
                    case 2:
                        return String.valueOf(record.getId());
                    case 3:
                        String text = record.getText();
                        SyntaxStructure ss = new SyntaxStructure(text);
                        return ss.getText();
                    }
                }
                return "";
            }
        });
        textTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            IStructuredSelection prevSelection = new StructuredSelection();


            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection) {
                    if (!prevSelection.equals(event.getSelection())) {
                        Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
                        if (o instanceof ITextRecord) {
                            ITextRecord selectedRecord = (ITextRecord) o;
                            String text = selectedRecord.getText();
                            SyntaxStructure ss = new SyntaxStructure(text);
                            setResultText(ss);
                            morphemeTreeViewer.setInput(selectedRecord);
                            if (morphemeTreeViewer.getTree().getItemCount() > 0) {
                                Object topItem = morphemeTreeViewer.getTree().getTopItem().getData();
                                morphemeTreeViewer.expandToLevel(topItem, 1);
                            }
                        }
                        prevSelection = (IStructuredSelection) event.getSelection();
                        if (activeViewer != textTableViewer) {
                            activeViewer = textTableViewer;
                        }
                    }
                }
            }
        });
    }


    private void createMorphemeViewer(Composite parent) {
        // 解析結果
        morphemeTreeViewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
        GridData gdMorphemeList = new GridData(GridData.FILL, SWT.FILL, true, true);
        Tree morphemeTree = morphemeTreeViewer.getTree();
        morphemeTree.setToolTipText(""); // cancel tool tip
        morphemeTree.setLayoutData(gdMorphemeList);
        morphemeTree.setHeaderVisible(true);
        morphemeTree.setLinesVisible(true);

        CompositeUtil.createColumn(morphemeTreeViewer, "ID", 50).getColumn().setAlignment(SWT.RIGHT);
        CompositeUtil.createColumn(morphemeTreeViewer, "文節", 200);
        CompositeUtil.createColumn(morphemeTreeViewer, "係り先", 50);
        CompositeUtil.createColumn(morphemeTreeViewer, "係り元", 100);
        // 形態素の情報
        CompositeUtil.createColumn(morphemeTreeViewer, "原形", 80);
        CompositeUtil.createColumn(morphemeTreeViewer, "品詞", 80);
        CompositeUtil.createColumn(morphemeTreeViewer, "細分類", 100);

        morphemeTreeViewer.setContentProvider(new ITreeContentProvider() {
            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }


            @Override
            public void dispose() {
            }


            @Override
            public boolean hasChildren(Object element) {
                return (element instanceof ISyntaxStructureElement);
            }


            @Override
            public Object getParent(Object element) {
                // TODO: あとで
                return null;
            }


            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof ITextRecord) {
                    ITextRecord record = (ITextRecord) inputElement;
                    SyntaxStructure ss = new SyntaxStructure(record.getText());
                    return ss.toArray();
                }
                return new Object[0];
            }


            @Override
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof ISyntaxStructureElement) {
                    ISyntaxStructureElement sse = (ISyntaxStructureElement) parentElement;
                    return sse.getMorphemes().toArray();
                }
                return new Object[0];
            }

        });

        morphemeTreeViewer.setLabelProvider(new TableLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof ISyntaxStructureElement) {
                    ISyntaxStructureElement sse = (ISyntaxStructureElement) element;
                    switch (columnIndex) {
                    case 0: // ID
                        return String.valueOf(sse.getIndex());
                    case 1: // 表記
                        return sse.getHyouki();
                    case 2: // 係り先
                        ISyntaxStructureElement dst = sse.getDependDestination();
                        return (dst != null) ? String.valueOf(dst.getIndex()) : "";
                    case 3: // 係り元
                        if (sse.getDependSources() == null || sse.getDependSources().isEmpty())
                            return "";
                        StringBuilder buf = new StringBuilder();
                        for (ISyntaxStructureElement src : sse.getDependSources()) {
                            buf.append("," + src.getIndex());
                        }
                        return buf.toString().substring(1);
                    }
                    return "";
                } else if (element instanceof MorphemeElement) {
                    MorphemeElement morphme = (MorphemeElement) element;
                    switch (columnIndex) {
                    case 1: /* 表記 */
                        return morphme.getHyouki();
                    case 4: /* 原形 */
                        return morphme.getGenkei();
                    case 5: /* 品詞 */
                        return morphme.getHinshi();
                    case 6: /* 品詞細分類 */
                        return morphme.getHinshiSaibunrui();
                    }
                    return "";
                }
                return "";
            }
        });
    }

    AbstractCCP ccp = null;


    protected void createActions() {
        ccp = new ResultMorphemeEditorCCP() {

            @Override
            public void selectionChanged(IStructuredSelection selection) {
                if (activeViewer != morphemeTreeViewer) {
                    activeViewer = morphemeTreeViewer;
                }
                super.selectionChanged(selection);
            }

        };
        morphemeTreeViewer.addSelectionChangedListener(ccp);
    }


    protected void setGlobalActions() {
        IActionBars bar = getEditorSite().getActionBars();
        bar.setGlobalActionHandler(ActionFactory.COPY.getId(), ccp.getCopyAction());
    }


    protected void hookContextMenu() {
        MenuManager menuManager = new MenuManager("#MorphemeTreeViewerPopup");
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(ccp.getCopyAction());
                manager.add(new Action("展開する") {
                    @Override
                    public void run() {
                        morphemeTreeViewer.expandAll();
                    }


                    @Override
                    public ImageDescriptor getImageDescriptor() {
                        return Icons.INSTANCE.getDescriptor(Icons.IMG_TOOL_EXPAND_ALL);
                    }


                    @Override
                    public boolean isEnabled() {
                        return morphemeTreeViewer.getTree().getItemCount() > 0;
                    }
                });
                manager.add(new Action("畳む") {
                    @Override
                    public void run() {
                        morphemeTreeViewer.collapseAll();
                    }


                    @Override
                    public ImageDescriptor getImageDescriptor() {
                        return Icons.INSTANCE.getDescriptor(Icons.IMG_TOOL_COLLAPSE_ALL);
                    }


                    @Override
                    public boolean isEnabled() {
                        return morphemeTreeViewer.getTree().getItemCount() > 0;
                    }
                });
            }
        });
        getSite().registerContextMenu(menuManager, morphemeTreeViewer);
        morphemeTreeViewer.getTree().setMenu(menuManager.createContextMenu(morphemeTreeViewer.getTree()));
    }


    /**
     * @param text
     *            処理結果
     */
    void setResultText(SyntaxStructure ss) {
        this.resultText.setText(ss.getText());
    }


    /* ****************************************
     * 保存
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        // nothing to do now
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
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }


    @Override
    public void setFocus() {
        textTableViewer.getControl().setFocus();
    }


    @Override
    public String getPartName() {
        return getEditorInput().getName();
    }


    @Override
    public String getTitleToolTip() {
        return getEditorInput().getToolTipText();
    }

    /**
     * コード量削減のためのスーパークラス
     * 
     * @author kousuke-morishima
     */
    static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public TableLabelProvider() {
        }


        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }


        @Override
        public String getColumnText(Object element, int columnIndex) {
            return "";
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
