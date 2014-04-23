/**
 * @version $Id: PatternDicEditorMasterBlock.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 18:45:43
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.tida_okinawa.corona.correction.parsing.PatternModelUtil;
import com.tida_okinawa.corona.correction.parsing.model.AndOperator;
import com.tida_okinawa.corona.correction.parsing.model.IPatternListener;
import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.correction.parsing.model.Modification;
import com.tida_okinawa.corona.correction.parsing.model.ModificationElement;
import com.tida_okinawa.corona.correction.parsing.model.NotOperator;
import com.tida_okinawa.corona.correction.parsing.model.OrOperator;
import com.tida_okinawa.corona.correction.parsing.model.Order;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;
import com.tida_okinawa.corona.correction.parsing.model.Sequence;
import com.tida_okinawa.corona.correction.parsing.model.Term;
import com.tida_okinawa.corona.internal.ui.actions.PartPatternModifyAction;
import com.tida_okinawa.corona.internal.ui.actions.PatternDicEditorCCP;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;
import com.tida_okinawa.corona.ui.editors.template.TemplateRecords;
import com.tida_okinawa.corona.ui.editors.template.TemplateTreeContentProvider;
import com.tida_okinawa.corona.ui.editors.template.TemplateTreeLabelProvider;
import com.tida_okinawa.corona.ui.editors.template.TemplateUseDialog;
import com.tida_okinawa.corona.ui.views.pattern.reference.AcquireReferenceRelation;
import com.tida_okinawa.corona.ui.views.pattern.reference.ReferenceRelationView;

/**
 * @author kousuke-morishima
 */
public class PatternDicEditorMasterBlock extends MasterDetailsBlock {

    TreeViewer viewer;
    private Button addButton;
    private Button removeButton;
    private Button templateButton;
    private FormEditor editor;


    @Override
    protected void createMasterPart(IManagedForm managedForm, final Composite parent) {
        editor = ((FormPage) managedForm.getContainer()).getEditor();

        /* マスター（左側）を作る */
        FormToolkit kit = managedForm.getToolkit();

        Section section = kit.createSection(parent, Section.TITLE_BAR | Section.DESCRIPTION);
        section.setText(Messages.PatternDicEditorMasterBlock_labelPatern);
        section.setDescription(""); //$NON-NLS-1$

        Composite root = kit.createComposite(section);
        root.setLayout(new GridLayout(2, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        /* Tree領域 */
        Composite treeClient = kit.createComposite(root);
        treeClient.setLayout(new GridLayout());
        GridData treeLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeLayout.heightHint = 300;
        treeClient.setLayoutData(treeLayout);
        Tree tree = kit.createTree(treeClient, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer = new TreeViewer(tree);
        viewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dragSource);
        viewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() }, new PatternDropTargetListener(viewer));
        viewer.setContentProvider(new PatternDicContentProvider());
        viewer.setLabelProvider(new PatternDicLabelProvider());
        if (input != null) {
            viewer.setInput(input);
        }
        GridData viewerData = new GridData(SWT.FILL, SWT.FILL, true, true);
        viewerData.widthHint = 200;
        viewer.getTree().setLayoutData(viewerData);

        /* Button領域 */
        Composite btnClient = kit.createComposite(root);
        btnClient.setLayout(new GridLayout());
        GridData layoutData = new GridData();
        layoutData.verticalAlignment = SWT.TOP;
        btnClient.setLayoutData(layoutData);
        addButton = kit.createButton(btnClient, Messages.PatternDicEditorMasterBlock_buttonAdd, SWT.PUSH);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        addButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                add(new PatternRecord(null));
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        removeButton = kit.createButton(btnClient, Messages.PatternDicEditorMasterBlock_buttonDelete, SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        removeButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                remove();
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        /**
         * @author s.takuro
         *         #187 構文パターン自動生成
         *         ひな型利用ダイアログ表示用ボタン
         */
        templateButton = kit.createButton(btnClient, Messages.PatternDicEditorMasterBlock_buttonTemplate, SWT.PUSH);
        templateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        templateButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TemplateRecords templateRecords = new TemplateRecords(IoActivator.getTemplateFactory());
                if (templateRecords.isTemplateTerm() >= 0) {
                    TemplateUseDialog dialog = new TemplateUseDialog(editor.getEditorSite().getShell());
                    dialog.setTreeContentProvider(new TemplateTreeContentProvider());
                    dialog.setTreeLabelProvider(new TemplateTreeLabelProvider());
                    dialog.setFormEditor(editor);
                    dialog.setPatternRecords(input);
                    dialog.open();
                    if (dialog.getPatternRecords() != null) {
                        input = dialog.getPatternRecords();
                        viewer.setInput(input);
                    }
                } else {
                    MessageBox messageError = new MessageBox(parent.getShell(), SWT.ICON_ERROR);
                    messageError.setText(Messages.PatternDicEditorMasterBlock_errLabel);
                    messageError.setMessage(Messages.PatternDicEditorMasterBlock_errText);
                    messageError.open();
                    return;
                }
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        initSelection(section, managedForm);

        section.setClient(root);
        createActions();

        /**
         * #458 パターンツリーを選択中と、それ以外の場合で Cut,Copy,Paste アクションを切り替える
         */
        viewer.getControl().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setGlobalActions();
            }


            @Override
            public void focusLost(FocusEvent e) {
                resetGlobalActions();
            }
        });
    }

    /* ****************************************
     * Selection系
     */
    IStructuredSelection currentSelection = new StructuredSelection();


    private void initSelection(Section parent, final IManagedForm managedForm) {
        /* 選択変更を通知する */
        final SectionPart sectionPart = new SectionPart(parent);
        managedForm.addPart(sectionPart);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection oldSelection = currentSelection;
                currentSelection = (IStructuredSelection) event.getSelection();
                managedForm.fireSelectionChanged(sectionPart, currentSelection);

                if (!oldSelection.isEmpty()) {
                    viewer.update(oldSelection.getFirstElement(), null);
                }

                updateButton(currentSelection);
            }
        });
    }


    /**
     * 選択しているパターンによって、削除ボタンの有効無効を切り替える
     * 
     * @param selection
     */
    void updateButton(IStructuredSelection selection) {
        boolean enabled = DeleteHandler.isEnabled(selection);
        removeButton.setEnabled(enabled);
    }

    /* ****************************************
     * パターンの管理
     */
    PatternRecords input;


    /**
     * 入力内容設定
     * 
     * @param input
     */
    public void setInput(PatternRecords input) {
        if (viewer != null) {
            viewer.setInput(input);
        }
        this.input = input;
        input.addPatternListener(new IPatternListener() {
            @Override
            public void patternRemoved(PatternEvent event) {
                dirtyChanged();
            }


            @Override
            public void patternChanged(PatternEvent event) {
                dirtyChanged();
            }


            @Override
            public void patternAdded(PatternEvent event) {
                dirtyChanged();
            }
        });
    }


    void add(PatternRecord data) {
        input.add(data);

        viewer.refresh();
        viewer.setSelection(new StructuredSelection(data), true);

        dirtyChanged();
    }


    /**
     * remove selection items without refresh
     * 
     * @return 削除されたパターン
     */
    List<Pattern> remove() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        List<Pattern> deletedItem = new ArrayList<Pattern>(selection.size());

        for (Object o : selection.toArray()) {
            if (o instanceof Pattern) {
                if (remove((Pattern) o)) {
                    deletedItem.add((Pattern) o);
                }
            }
        }

        dirtyChanged();
        return deletedItem;
    }

    /**
     * UIから削除されたPatternRecordを {@link #createPartPatterns(ICoronaDic)}
     * で表示しないために使う
     */
    List<IPattern> removedRecord = new ArrayList<IPattern>();


    /**
     * 指定したパターンを viewer から削除する
     * 
     * <pre>
     * 依存している参照パターンなど、削除できない場合もある(ダイアログで ok/cancel)
     * </pre>
     * 
     * @param pattern
     *            削除するパターン
     * @return pattern が削除されたら true
     */
    boolean remove(Pattern pattern) {
        if (pattern instanceof PatternRecord) {
            /* root なので、inputから消す */
            PatternRecord record = (PatternRecord) pattern;
            if (record.isPart()) {
                /* 参照可能パターンを消したとき、この参照しているパターンがあるか確認する */
                Set<PatternRecord> patterns = new HashSet<PatternRecord>();
                IUIElement element = ((DicEditorInput) editor.getEditorInput()).getUIDictionary();
                /* 他辞書からの参照を確認する */
                Set<ICoronaDic> dics = getUIDics(element);
                for (ICoronaDic dic : dics) {
                    PatternRecords recs = new PatternRecords((IPatternDic) dic);
                    patterns.addAll(recs.getPatternRecords());
                }
                patterns.addAll(input.getPatternRecords());

                PartPatternModifyAction action = new PartPatternModifyAction(new ArrayList<PatternRecord>(patterns));
                action.set(record);
                action.preview(editor.getEditorSite().getShell());
                if (Status.OK_STATUS.equals(action.getResult())) {
                    input.remove(record);
                    removedRecord.add(record.getIPattern());
                    return true;
                }
            } else {
                input.remove(record);
                removedRecord.add(record.getIPattern());
                return true;
            }
        } else {
            PatternContainer parent = pattern.getParent();
            if (parent != null) {
                parent.removeChild(pattern);
                viewer.setSelection(new StructuredSelection(parent));
                return true;
            } else {
                System.err.println("parent is null"); //$NON-NLS-1$
            }
        }

        return false;
    }


    /* ****************************************
     * 保存
     */
    /**
     * パターンの整合性を確認して、不備があれば修正する
     */
    public void doSave() {
        List<PatternRecord> records = input.getPatternRecords();
        for (PatternRecord record : records) {
            PatternModelUtil.scopeCheck(record, SearchScopeType.SEARCH_ALL);
            linkLabelCheck(record);
        }

        needUpdate = true;
        removedRecord.clear();
        dirtyChanged();
        viewer.refresh();
    }


    private void linkLabelCheck(Pattern rec) {
        for (Pattern child : ((PatternContainer) rec).getChildren()) {
            if (child instanceof Link) {
                /* 参照先を指定していないとき、nullなので対策 */
                IPattern linkPattern = ((Link) child).getLinkPattern();
                if (linkPattern != null) {
                    String label = linkPattern.getLabel();
                    if (!label.equals(((Link) child).getLabel())) {
                        ((Link) child).setLabel(label);
                    }
                }
            }
            if (child.hasChildren()) {
                linkLabelCheck(child);
            }
        }
    }


    void dirtyChanged() {
        editor.editorDirtyStateChanged();
    }

    /* ****************************************
     * アクション関連
     */
    PatternDicEditorCCP ccp;


    protected void createActions() {
        ccp = new PatternDicEditorCCP((PatternDicEditor) editor);
        viewer.addSelectionChangedListener(ccp);
    }


    /**
     * Copy, Cut, Paste アクションをセットする。 構文パターンエディタが選択される毎(
     * {@link PatternDicEditorActionContributor#setActiveEditor(org.eclipse.ui.IEditorPart)}
     * ) に呼ばれる。
     * 
     * <pre>
     * エディタが切り替わるとクリップボードの状態や各アクションの enable 状態が取れなくなるので
     * 切り替えの度に selectionChanged をコールしてチェックを行う
     * </pre>
     */
    void setGlobalActions() {
        IActionBars bar = editor.getEditorSite().getActionBars();
        bar.setGlobalActionHandler(ActionFactory.PASTE.getId(), ccp.getPasteAction());
        bar.setGlobalActionHandler(ActionFactory.COPY.getId(), ccp.getCopyAction());
        bar.setGlobalActionHandler(ActionFactory.CUT.getId(), ccp.getCutAction());
        ccp.selectionChanged((IStructuredSelection) viewer.getSelection());
    }


    /**
     * Copy, Cut, Paste アクションをリセットする
     */
    void resetGlobalActions() {
        IActionBars bar = editor.getEditorSite().getActionBars();
        bar.setGlobalActionHandler(ActionFactory.PASTE.getId(), null);
        bar.setGlobalActionHandler(ActionFactory.COPY.getId(), null);
        bar.setGlobalActionHandler(ActionFactory.CUT.getId(), null);
    }

    /**
     * 辞書グループGroupMaker設定値
     */
    public static final String PatternDicEditorEdit = "PatternDicEditorEditGroup"; //$NON-NLS-1$

    /**
     * 新規辞書GroupMaker設定値
     */
    public static final String PatternDicEditorNew = "PatternDicEditorNew"; //$NON-NLS-1$

    /**
     * ひな型追加GroupMaker設定値
     */
    public static final String TemplateAddDialog = "TemplateAddDialog"; //$NON-NLS-1$


    /**
     * コンテキストメニューへアイテムを追加
     * 
     * @param site
     */
    public void registerContextMenu(final IWorkbenchPartSite site) {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new GroupMarker(PatternDicEditorNew));
                manager.add(new Separator());
                manager.add(ccp.getCutAction());
                manager.add(ccp.getCopyAction());
                manager.add(ccp.getPasteAction());
                manager.add(new GroupMarker(PatternDicEditorEdit));
                manager.add(new Separator());
                manager.add(new Action(Messages.PatternDicEditorMasterBlock_labelTreeOpen) {
                    @Override
                    public void run() {
                        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
                        if (ss != null) {
                            for (Object o : ss.toArray()) {
                                viewer.expandToLevel(o, TreeViewer.ALL_LEVELS);
                            }
                        }
                    }


                    @Override
                    public ImageDescriptor getImageDescriptor() {
                        return Icons.INSTANCE.getDescriptor(Icons.IMG_TOOL_EXPAND_ALL);
                    }


                    @Override
                    public boolean isEnabled() {
                        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
                        TreeItem item = (TreeItem) viewer.testFindItem(ss.getFirstElement());
                        if (item != null) {
                            return item.getItemCount() > 0;
                        }
                        return false;
                    }
                });
                manager.add(new Action(Messages.PatternDicEditorMasterBlock_labelTreeClose) {
                    @Override
                    public void run() {
                        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
                        if (ss != null) {
                            for (Object o : ss.toArray()) {
                                viewer.collapseToLevel(o, TreeViewer.ALL_LEVELS);
                            }
                        }
                    }


                    @Override
                    public ImageDescriptor getImageDescriptor() {
                        return Icons.INSTANCE.getDescriptor(Icons.IMG_TOOL_COLLAPSE_ALL);
                    }


                    @Override
                    public boolean isEnabled() {
                        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
                        TreeItem item = (TreeItem) viewer.testFindItem(ss.getFirstElement());
                        if (item != null) {
                            return item.getItemCount() > 0;
                        }
                        return false;
                    }
                });

                manager.add(new Action(Messages.PatternDicEditorMasterBlock_labelRefRelation) {
                    // 　参照関係ビューを表示する処理
                    @Override
                    public void run() {
                        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                        try {
                            ReferenceRelationView view = (ReferenceRelationView) page.showView(ReferenceRelationView.VIEW_ID);
                            AcquireReferenceRelation acquireRefRel = new AcquireReferenceRelation();
                            List<PatternRecord> patterns = new ArrayList<PatternRecord>();
                            IUIElement element = ((DicEditorInput) editor.getEditorInput()).getUIDictionary();
                            /* 他辞書からの参照を確認する */
                            Set<ICoronaDic> dics = getUIDics(element);
                            for (ICoronaDic dic : dics) {
                                PatternRecords recs = new PatternRecords((IPatternDic) dic);
                                patterns.addAll(recs.getPatternRecords());
                            }
                            patterns.addAll(input.getPatternRecords());

                            view.setTree(acquireRefRel.createRelation(patterns, ss));
                        } catch (PartInitException e) {
                            e.printStackTrace();
                        }
                    }


                    @Override
                    public boolean isEnabled() {
                        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
                        if (ss.isEmpty()) {
                            return false;
                        }
                        return true;
                    }
                });
                /**
                 * @author s.takuro
                 *         #187 構文パターン自動生成
                 *         ひな型登録ダイアログ表示
                 */
                manager.add(new Separator());
                manager.add(new GroupMarker(TemplateAddDialog));
            }
        });

        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
        /* Memo EditorInputがIResourceをAdapterしなければ、下のようにやっても要らんメニューは出ない */
        site.registerContextMenu(menuMgr, viewer);
        site.setSelectionProvider(viewer);
    }


    @Override
    protected void registerPages(DetailsPart detailsPart) {
        /* 選択変更の通知先として登録する */
        detailsPart.registerPage(PatternRecord.class, new PatternRecordDetailsPage(editor));
        detailsPart.registerPage(Term.class, new TermDetailsPage(editor));
        detailsPart.registerPage(Modification.class, new ModificationDetailsPage(editor));
        detailsPart.registerPage(Link.class, new LinkDetailsPage(editor));
        detailsPart.registerPage(Order.class, new OrderDetailsPage(editor));
        detailsPart.registerPage(Sequence.class, new SequenceDetailsPage(editor));
        detailsPart.registerPage(OrOperator.class, new OrDetailsPage(editor));
        detailsPart.registerPage(AndOperator.class, new AndDetailsPage(editor));
        detailsPart.registerPage(NotOperator.class, new NotDetailsPage(editor));
        final PatternDetailsPage srcPage = new ModificationSourceDetailsPage(editor);
        final PatternDetailsPage dstPage = new ModificationDestinationDetailsPage(editor);
        detailsPart.setPageProvider(new IDetailsPageProvider() {
            @Override
            public Object getPageKey(Object object) {
                if (object instanceof ModificationElement) {
                    return object;
                }
                return null;
            }


            @Override
            public IDetailsPage getPage(Object key) {
                if (key instanceof ModificationElement) {
                    switch (((ModificationElement) key).getKind()) {
                    case MODIFICATION_SOURCE:
                        return srcPage;
                    case MODIFICATION_DESTINATION:
                        return dstPage;
                    default:
                        return null;
                    }
                }
                return null;
            }
        });
    }


    @Override
    protected void createToolBarActions(IManagedForm managedForm) {
    }


    /**
     * パターン辞書更新
     */
    public void update() {
        Object element = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
        if (element != null) {
            viewer.update(element, null);
        }
    }


    /**
     * @param element
     *            not null
     */
    public void reveal(Object element) {
        if (element instanceof Object[]) {
            viewer.setSelection(new StructuredSelection((Object[]) element), true);
        } else {
            viewer.reveal(element);
        }
    }


    /**
     * フォーカスをセットする
     * 
     * @return view->focus
     */
    public boolean setFocus() {
        return viewer.getControl().setFocus();
    }

    /* ****************************************
     * 参照パターンに指定できるパターン一覧を作成する LinkDetailsPage.javaから使用されている
     */
    // private static final IPattern[] EMPTY_INPUT = new IPattern[0];
    // /**
    // * 自辞書以外の、参照パターンに使えるパターン一覧
    // */
    // private List<IPattern> linkPatterns;
    /**
     * 自辞書の、参照パターンに使えるパターン一覧
     */
    private List<IPattern> ownLinkPatterns;
    private boolean needUpdate = true;
    Map<Integer, String> dicIdToNameMap = new HashMap<Integer, String>();
    Map<Integer, Integer> patternToDicMap = new HashMap<Integer, Integer>();


    /**
     * 部品属性のパターンを取得する TODO 自辞書が開かれている状態で他の辞書が更新されたのを検知するのは、まだむり
     * 
     * @return ItemList
     */
    IPattern[] createPartPatterns(ICoronaDic dic) {
        if (dic == null) {
            dic = ((DicEditorInput) editor.getEditorInput()).getDictionary();
        } else {
            needUpdate = true;
        }

        if (needUpdate) {
            needUpdate = false;
            /* 自辞書のみ更新 */
            ownLinkPatterns = (getPartPatterns((IPatternDic) dic));
        }

        List<IPattern> margeList = new ArrayList<IPattern>(ownLinkPatterns);
        margeList.removeAll(removedRecord);
        return margeList.toArray(new IPattern[margeList.size()]);
    }


    private List<IPattern> getPartPatterns(IPatternDic dic) {
        /* 以前部品だったパターンがMapに残るが、Listから消えれば参照されることはないので、このままでいい */

        int dicId = dic.getId();
        dicIdToNameMap.put(dicId, dic.getName());

        List<IPattern> ret = new ArrayList<IPattern>();
        List<IDicItem> items = dic.getItems();
        for (IDicItem item : items) {
            IPattern p = (IPattern) item;
            /* IDが0のままLinkに設定すると、正しいリンクを参照できなくなるので、保存されたパターンのみ選択できる */
            if (p.isParts() && (p.getId() != IDicItem.UNSAVED_ID)) {
                ret.add(p);
                patternToDicMap.put(p.getId(), dicId);
            }
        }
        return ret;
    }


    /**
     * UIElementの内容に応じて辞書を返す
     * 
     * @param element
     * @return List<ICoronaDic>
     */
    public Set<ICoronaDic> getUIDics(IUIElement element) {
        Set<ICoronaDic> dics = new HashSet<ICoronaDic>();
        if (element != null) {
            /* プロジェクトビューから辞書を開いた場合 */
            IUIProject uiProject = CoronaModel.INSTANCE.getProject(element);
            ICoronaProject project = uiProject.getObject();
            /* 共通辞書をAdd */
            dics.addAll(project.getDictionarys(IPatternDic.class));
            IUIContainer container = element.getParent().getParent();
            if (container instanceof IUIProduct) {
                /* 固有辞書を削除する場合、自固有辞書を取得 */
                dics.addAll(((IUIProduct) container).getObject().getDictionarys(IPatternDic.class));
            } else {
                /* 共通辞書を削除する場合、プロジェクト配下の全固有辞書を取得 */
                List<ICoronaProduct> products = ((IUIProject) container).getObject().getProducts();
                for (ICoronaProduct product : products) {
                    dics.addAll(product.getDictionarys(IPatternDic.class));
                }
            }
        } else {
            /* データベースビューから辞書を開いた場合 */
            dics.addAll(IoActivator.getService().getDictionarys(IPatternDic.class));
        }
        /* 自辞書を削除 */
        dics.remove(((DicEditorInput) editor.getEditorInput()).getDictionary());

        return dics;
    }

    /* ****************************************
     * DnD ドラッグ開始 1. dragStart(Source) 2. validateDrop(Target) ドラッグやめた 3.
     * dragSetData(Source) 4. performDrop(Target) 5. dragFinished(Source)
     */
    private DragSourceListener dragSource = new DragSourceAdapter() {
        @Override
        public void dragStart(DragSourceEvent event) {
            IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            if (!isMovable(selection)) {
                event.doit = false;
                return;
            }
        }


        @Override
        public void dragSetData(DragSourceEvent event) {
            IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            Object element = selection.getFirstElement();
            if (element instanceof Pattern) {
                if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
                    ((LocalSelectionTransfer) ((DragSource) event.getSource()).getTransfer()[0]).setSelection(selection);
                }
            }
        }


        /**
         * 選択している要素がドラッグ可能かどうかチェックする
         * 
         * <ul>
         * <li>ルート要素はドラッグ禁止 (#421)</li>
         * <li>選択された要素が全て同じ親を持つ時だけドラッグ可能。単一選択もこれに該当する</li>
         * </ul>
         * </pre>
         * 
         * @param selection
         * @return ドラッグ可能であれば true
         */
        private boolean isMovable(IStructuredSelection selection) {
            if (selection == null || selection.size() == 0)
                return false;

            Pattern p1 = (Pattern) selection.getFirstElement();

            for (Object element : selection.toArray()) {
                Pattern p2 = (Pattern) element;
                if (p2 instanceof PatternRecord) {
                    return false;
                }

                if (!p1.getParent().equals(p2.getParent())) {
                    return false;
                }
            }
            return true;
        }
    };

    private class PatternDropTargetListener extends ViewerDropAdapter {
        public PatternDropTargetListener(TreeViewer viewer) {
            super(viewer);
        }


        @Override
        public boolean validateDrop(Object targetObject, int operation, TransferData transferType) {
            IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();

            for (Object source : selection.toArray()) {
                if (source.equals(targetObject))
                    return false;
            }

            switch (getCurrentLocation()) {
            case LOCATION_ON:
                return isValidDropLocationOn(selection, targetObject);
            case LOCATION_BEFORE:
            case LOCATION_AFTER:
                return isValidDropLocationBeforeAndAfter(selection, targetObject);
            case LOCATION_NONE:
                return isValidDropLocationNone(selection);
            default:
                return false;
            }
        }


        /**
         * target 内に対して selection をドロップできるか判定する
         * 
         * <pre>
         * 以下の条件でドロップできない
         * 
         * 1. source がルート要素(PatternRecord) 
         * 2. target がルート要素で sourceが 複数個
         * 3. target が子要素を持てない
         * 4. target が source を子に持てない
         * 5. source が 係り元及び係り先で target が係り受け以外
         * 6. 自身の子(子孫)内にはドロップできない
         * </pre>
         * 
         * @param selection
         * @param targetObject
         * @return ドロップできるならば true
         */
        private boolean isValidDropLocationOn(IStructuredSelection selection, Object targetObject) {
            Pattern target = (Pattern) targetObject;
            int rootChildCount = 0;

            for (Object sourceObject : selection.toArray()) {
                Pattern source = (Pattern) sourceObject;

                if (source instanceof PatternRecord) {
                    return false;
                }

                if (target instanceof PatternRecord) {
                    if (++rootChildCount > 1) {
                        return false;
                    }
                }

                if (!(target instanceof PatternContainer)) {
                    return false;
                }

                if (!((PatternContainer) target).canHaveChild(source.getKind())) {
                    return false;
                }

                if (source instanceof ModificationElement) {
                    if (!(target instanceof Modification)) {
                        return false;
                    }
                }

                if (isLoop((PatternContainer) target, source)) {
                    return false;
                }
            }
            return true;
        }


        /**
         * target の前後に対して source をドロップできるか判定する
         * 
         * <pre>
         * 1. ルート要素(PatternRecord) はルート要素以外の要素前後にドロップできない
         * 2. ルートでない要素はルート要素前後にドロップできない
         * 3. target の親要素が source を子に持てない場合はドロップできない
         * 4. source が係り元もしくは係り先で、target の親要素が係り受けの場合はドロップできない
         * 5. 自身の子(子孫)内にはドロップできない
         * </pre>
         * 
         * @param selection
         * @param targetObject
         * 
         * @return ドロップできるならば true
         */
        private boolean isValidDropLocationBeforeAndAfter(IStructuredSelection selection, Object targetObject) {
            Pattern target = (Pattern) targetObject;

            for (Object sourceObject : selection.toArray()) {
                Pattern source = (Pattern) sourceObject;

                if (source instanceof PatternRecord) {
                    return target instanceof PatternRecord;
                } else {
                    if (target instanceof PatternRecord) {
                        return false;
                    } else if (target == null || source == null) {
                        return false;
                    } else if (target.getParent() != null) {
                        PatternContainer parent = target.getParent();
                        if (!parent.canHaveChild(source.getKind())) {
                            return false;
                        }
                        /* 係り元、係り先が、係り受け以外にドロップされるのはダメ */
                        if (source instanceof ModificationElement) {
                            if (!(parent instanceof Modification)) {
                                return false;
                            }
                        }

                        if (parent.equals(source) || isLoop(parent, source)) {
                            return false;
                        }
                    } else {
                        /* PatternRecordでないものは、最上位にドロップできない */
                        return false;
                    }
                }
            }
            return true;
        }


        /**
         * @param selection
         * @return ドロップできるならば true
         */
        private boolean isValidDropLocationNone(IStructuredSelection selection) {
            for (Object source : selection.toArray()) {
                if (!(source instanceof PatternRecord)) {
                    return false;
                }
            }
            return true;
        }


        /**
         * 移動前の状態で、newParentがnewChildの子だったらループしてしまうのでtrueを返す。
         * 
         * @param newParent
         * @param newChild
         * @return ループするなら true を返す
         */
        private boolean isLoop(PatternContainer newParent, Pattern newChild) {
            /* 親を子に移動させるのはダメ */
            if (newChild instanceof PatternContainer) {
                PatternContainer parent = newParent.getParent();
                while (parent != null) {
                    if (parent.equals(newChild)) {
                        return true;
                    }
                    parent = parent.getParent();
                }
            }
            return false;
        }


        @Override
        public boolean performDrop(Object data) {
            DropTargetEvent event = getCurrentEvent();
            Pattern target = (Pattern) determineTarget(event);
            final int currentLocation = determineLocation(event);

            for (Object elm : ((IStructuredSelection) data).toArray()) {
                Pattern source = (Pattern) elm;
                /* 挿入位置を決定 */
                switch (currentLocation) {
                case LOCATION_ON:
                    PatternContainer oldParent = source.getParent();
                    if (oldParent != null) {
                        oldParent.removeChild(source);
                    }
                    ((PatternContainer) target).addChild(source);
                    source.setParent((PatternContainer) target);
                    break;
                case LOCATION_BEFORE:
                    move(source, target, true);
                    break;
                case LOCATION_AFTER:
                    move(source, target, false);
                    break;
                case LOCATION_NONE:
                    remove(source);
                    add((PatternRecord) source);
                    break;
                default:
                    break;
                }
            }

            dirtyChanged();
            return true;
        }


        /**
         * @param source
         * @param target
         * @param before
         */
        private void move(Pattern source, Pattern target, boolean before) {
            /*
             * sourceもtargetもPatternRecordか、どちらもそうでないときだけ呼ばれることを前提にしている。
             */
            if (source instanceof PatternRecord) {
                if (target instanceof PatternRecord) {
                    PatternRecord sRec = (PatternRecord) source;
                    int sIndex = input.indexOf(sRec);
                    int tIndex = input.indexOf((PatternRecord) target);
                    input.remove(sRec);
                    if (before) {
                        input.add((sIndex > tIndex) ? tIndex : tIndex - 1, sRec);
                    } else {
                        input.add((sIndex > tIndex) ? tIndex + 1 : tIndex, sRec);
                    }
                }
            } else {
                PatternContainer sParent = source.getParent();
                PatternContainer tParent = target.getParent();
                int sIndex = sParent.getChildren().indexOf(source);
                int tIndex = tParent.getChildren().indexOf(target);

                int pos = (before) ? 0 : 1;
                if (sParent.equals(tParent)) {
                    pos += (sIndex > tIndex) ? tIndex : tIndex - 1;
                } else {
                    pos += tIndex;
                }
                sParent.removeChild(source);
                tParent.addChild(pos, source);
                source.setParent(tParent);
            }
        }
    };
}
