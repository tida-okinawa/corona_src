/**
 * @version $Id: UserDicEditor.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/08/02 15:34:23
 * @author KMorishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import static com.tida_okinawa.corona.ui.editors.user.TermCellModifier.PROP_CLASS;
import static com.tida_okinawa.corona.ui.editors.user.TermCellModifier.PROP_PART;
import static com.tida_okinawa.corona.ui.editors.user.TermCellModifier.PROP_WORD;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.tida_okinawa.corona.internal.ui.actions.UserDicEditorCCP;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibrary;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaDics;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.io.model.dic.UserDicFieldType;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.PreferenceInitializer;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.editors.AbstractDicEditor;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;
import com.tida_okinawa.corona.ui.editors.EditorUtil;
import com.tida_okinawa.corona.ui.editors.IFilteringDataProvider;
import com.tida_okinawa.corona.ui.editors.LabelRelationGroup;

/**
 * 
 * @author KMorishima
 */
public class UserDicEditor extends AbstractDicEditor {
    public static final String EDITOR_ID = "com.tida_okinawa.corona.ui.editor.productdic";


    public UserDicEditor() {
    }

    Set<IDicItem> errorItems = new HashSet<IDicItem>();


    @Override
    public void doSave(IProgressMonitor monitor) {
        if (errorItems.size() > 0) {
            StringBuilder message = new StringBuilder("以下のアイテムは入力情報に不足があるため保存されません。\n");
            /* ただし、エラーアイテムに新たに紐づけたラベルは保存する */
            for (IDicItem item : errorItems) {
                ITerm term = (ITerm) item;
                message.append(term.getValue()).append(":");
                message.append(term.getTermPart().getName()).append(":");
                message.append(term.getTermClass().getName()).append(":");
            }
            MessageDialog.openInformation(getSite().getShell(), "", message.toString());
        }

        /* 保存 */
        try {
            monitor.beginTask("保存", 10);
            /* ユーザ辞書を保存 */
            if (((UserDicDataProvider) dataProvider).commit(new SubProgressMonitor(monitor, 5))) {
                /* ラベルと単語の紐づけを保存 */
                List<ILabelDic> labelDics = labelGroup.getRelatedlabelDics();
                for (ILabelDic dic : labelDics) {
                    dic.commit(new SubProgressMonitor(monitor, 5));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openWarning(getSite().getShell(), "Exception", e.getMessage());
        } finally {
            monitor.done();
            viewer.setInput(dataProvider.current(true));
            addEmptyRow();

            /* エラーで保存されていないアイテムが残っていれば、dirty状態を維持する */
            udic.setDirty(!errorItems.isEmpty());
            firePropertyChange(PROP_DIRTY);
        }
    }

    private IUserDic udic;
    IFilteringDataProvider dataProvider;
    private IPartListener listener;


    @Override
    protected boolean validDictionary(ICoronaDic dic) {
        if (dic instanceof IUserDic) {
            udic = (IUserDic) dic;
            return true;
        }
        return false;
    }

    private boolean editable;


    /**
     * @return 辞書を編集できるならtrue
     */
    public boolean isEditable() {
        return editable;
    }


    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        IPreferenceStore store = UIActivator.getDefault().getPreferenceStore();

        Assert.isLegal(coronaDic instanceof IUserDic);
        dataProvider = new UserDicDataProvider((IUserDic) coronaDic, store.getInt(PreferenceInitializer.PREF_NUM_VIEW_USERDIC_RECORD));

        editable = !DicType.JUMAN.equals(((IUserDic) coronaDic).getDicType());

        final ICoronaDic finalDic = coronaDic;
        listener = new IPartListener() {
            @Override
            public void partOpened(IWorkbenchPart part) {
            }


            @Override
            public void partDeactivated(IWorkbenchPart part) {
                if (UserDicEditorActionBar.dialog != null) {
                    UserDicEditorActionBar.dialog.setBtnEnabled(false);
                }
            }


            @Override
            public void partClosed(IWorkbenchPart part) {
                /* 最新にアップデート＆未保存データを破棄 */
                if (UserDicEditor.this.equals(part)) {
                    finalDic.update();

                    List<ILabelDic> labelDics = labelGroup.getRelatedlabelDics();
                    for (ICoronaDic dic : labelDics) {
                        if (dic.isDirty()) {
                            try {
                                if (!isOpenRelatedLabelDicEditor((ILabelDic) dic)) {
                                    dic.update();
                                }
                            } catch (PartInitException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }


            private boolean isOpenRelatedLabelDicEditor(ILabelDic labelDic) throws PartInitException {
                IEditorReference[] editorRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
                for (IEditorReference ref : editorRefs) {
                    if (ref.getEditorInput() instanceof DicEditorInput) {
                        DicEditorInput refInput = (DicEditorInput) ref.getEditorInput();
                        if (refInput.getDictionary().getId() == labelDic.getId()) {
                            return true;
                        }
                    }
                }
                return false;
            }


            @Override
            public void partBroughtToTop(IWorkbenchPart part) {
            }


            @Override
            public void partActivated(IWorkbenchPart part) {
                if (UserDicEditor.this.equals(part)) {
                    if (labelGroup != null) {
                        labelGroup.refresh();
                    }
                    assert part instanceof UserDicEditor;
                    actionBar.activeUserDic((UserDicEditor) part);
                    if (UserDicEditorActionBar.dialog != null) {
                        UserDicEditorActionBar.dialog.setBtnEnabled(true);
                    }
                }
            }
        };
        /* リスナ登録 */
        getSite().getPage().addPartListener(listener);
    }


    @Override
    public void dispose() {
        super.dispose();
        /* リスナ削除 */
        getSite().getPage().removePartListener(listener);
        labelGroup.dispose();
    }


    @Override
    public boolean isDirty() {
        // 編集があったらtrueに設定する→保存ボタンが押せるようになる　doSaveへ
        if (udic.isDirty()) {
            return true;
        }
        for (ILabelDic ldic : labelGroup.getRelatedlabelDics()) {
            if (ldic.isDicRelationDirty(udic.getId())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void doSaveAs() {
    }


    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }


    /* ****************************************
     * UI構築
     */
    @Override
    public void createPartControl(Composite parent) {
        SashForm composite = CompositeUtil.defaultSashForm(parent, SWT.HORIZONTAL);

        createRecordGroup(composite);
        createLabelTreeGroup(composite);
        composite.setWeights(new int[] { 8, 2 });

        List<Object> data = dataProvider.first();
        toolbarGroup.updateDataNumLabel(); // 編集不可のとき、ラベルが更新されないので */
        addEmptyRow();
        viewer.setInput(data);

        // 開いた直後はアイテムを選択していないのでfalse
        labelGroup.setEnabled(false);

        super.createPartControl(parent);
        getSite().setSelectionProvider(viewer);
    }

    /* ********************
     * レコード表示領域
     */
    TableViewer viewer;
    RecordNavToolBarGroup toolbarGroup;
    UserDicEditorActionBar actionBar;


    private void createRecordGroup(Composite parent) {
        ViewForm viewForm = new ViewForm(parent, SWT.BORDER);
        viewForm.setLayout(new GridLayout());
        viewForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewForm.setBorderVisible(false);

        /* ツールバー領域1 */
        toolbarGroup = new RecordNavToolBarGroup(viewForm, dataProvider);
        toolbarGroup.addPageMovedListener(new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.data.equals(RecordNavToolBarGroup.LAST_BTN) || event.data.equals(RecordNavToolBarGroup.NEXT_BTN)) {
                    addEmptyRow();
                }
                viewer.setSelection(new StructuredSelection(viewer.getTable().getItem(0).getData()), true);
            }
        });
        viewForm.setTopLeft(toolbarGroup.getControl());

        /* ツールバー領域2 */
        actionBar = new UserDicEditorActionBar(viewForm, this);
        viewForm.setTopRight(actionBar.getControl());

        /* データ表示領域 */
        viewer = new TableViewer(viewForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new UserDicLabelProvider(this, viewer, dataProvider));
        viewer.addSelectionChangedListener(tableSelectionChangedListener);
        if (editable) {
            if (UIActivator.isAlpha()) {
                viewer.addFilter(new PartFilter(new TermPart[] { TermPart.NOUN, TermPart.NONE }));
            }
        }

        viewForm.setContent(viewer.getControl());
        toolbarGroup.setViewer(viewer); // データ操作対象のViewerをセット

        /* カラム設定 */
        final Table tbl = viewer.getTable();
        tbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tbl.setHeaderVisible(true);
        createColumnWithSort(tbl, "", 20, null).setResizable(false);
        createColumnWithSort(tbl, "#", 50, UserDicFieldType.NONE);
        /* #477 よみのカラム削除により、デフォルトでソートするカラムを単語に変更 */
        tbl.setSortColumn(createColumnWithSort(tbl, "単語*", 110, UserDicFieldType.HEADER));
        createColumnWithSort(tbl, "品詞*", 120, UserDicFieldType.PART);
        createColumnWithSort(tbl, "品詞細分類*", 120, UserDicFieldType.CLASS);
        tbl.setSortDirection(SWT.UP);
        createColumnWithSort(tbl, "ラベル", 120, UserDicFieldType.LABEL);

        if (editable) {
            /* 編集設定 */
            TermCellModifier modifier = new TermCellModifier(getSite());
            modifier.addFinishModifyListener(new Listener() {
                @Override
                public void handleEvent(Event event) {
                    ITerm term = (ITerm) event.data;

                    /* #477 よみに自動で単語の文字列をいれる */
                    term.setReading(term.getValue());

                    if (isLastRow(term) && (term.getValue().trim().length() > 0)) {
                        /* 辞書に追加する */
                        ((UserDicDataProvider) dataProvider).addData(emptyRow);
                        addEmptyRow();
                    }

                    viewer.update(term, null);
                    if (!isLastRow(term) && term.isError()) {
                        errorItems.add(term);
                    } else {
                        errorItems.remove(term);
                    }

                    /* element のisDirty をチェックする */
                    if (term.isDirty()) {
                        markDirty();
                    }
                }
            });
            String[] properties = new String[] { "", "", PROP_WORD, PROP_PART, PROP_CLASS, "" };
            viewer.setColumnProperties(properties);
            List<CellEditor> editors = modifier.createEditors(tbl);
            editors.add(0, null);
            editors.add(0, null);
            editors.add(null);
            viewer.setCellEditors(editors.toArray(new CellEditor[editors.size()]));
            viewer.setCellModifier(modifier);
            /* キーボードでの入力支援 */
            EditorUtil.setFocusMoveListener(editors.toArray(new CellEditor[editors.size()]), viewer, 2, 3, 4);
            setEditKeyListener(viewer, 2);
        }
    }


    /**
     * @param parent
     * @param title
     * @param width
     * @param sortField
     *            nullならクリックしてもソートしない
     */
    private TableColumn createColumnWithSort(final Table parent, String title, int width, final UserDicFieldType sortField) {
        final TableColumn column = CompositeUtil.createColumn(parent, title, width);
        if (sortField != null) {
            column.addSelectionListener(new SelectionAdapter() {
                private boolean asc = true;


                @Override
                public void widgetSelected(SelectionEvent e) {
                    asc = !asc;
                    ((UserDicDataProvider) dataProvider).setOrder(sortField, asc);
                    viewer.setInput(dataProvider.current(true));
                    addEmptyRow();
                    parent.setSortColumn(column);
                    parent.setSortDirection((asc) ? SWT.UP : SWT.DOWN);
                }
            });
        }
        return column;
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

    /* ********************
     * ラベル辞書エリア作成
     */
    LabelRelationGroup labelGroup;


    private void createLabelTreeGroup(Composite parent) {
        labelGroup = new LabelRelationGroup(parent, SWT.CHECK);
        labelGroup.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                // プログラムからのsetCheckでは呼ばれない */
                // 何も選択していなときは、ここにはこない

                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                if ((selection.size() == 1) && isLastRow((ITerm) selection.getFirstElement())) {
                    /* emptyRowでreturnしてしまうと、先にラベルを選べない */
                    /* LabelGroupに無駄なキャッシュを抱えてしまうので、末行ではラベル選択をさせない */
                    return;
                }

                Object checkedItem = event.getElement();
                ((CheckboxTreeViewer) event.getSource()).setGrayed(checkedItem, false);

                if (checkedItem instanceof ILabel) {
                    List<ITerm> terms = new ArrayList<ITerm>(selection.size());

                    /* #767 複数選択のチェック */

                    // ラベルと用語の紐付けを外すかどうかをチェックするフラグ(true:紐付けを外す)
                    boolean labelRemoveFlag = true;

                    // 選択した用語が複数ある場合、選択した用語すべてにラベルが紐づいているかチェックする
                    // 紐づいてい無いものが選択されている場合、既存のリレーションは外さないようフラグをfalseにする。
                    if (selection.size() > 1) {
                        for (Object o : selection.toArray()) {
                            List<ITerm> labelTerms = ((ILabel) checkedItem).getTerms();
                            if (!labelTerms.contains(o)) {
                                labelRemoveFlag = false;
                                break;
                            }
                        }
                    }

                    for (Object o : selection.toArray()) {
                        if (o instanceof ITerm) {

                            /* #767 ラベルの紐付けを更新 */
                            if (event.getChecked()) {
                                // イベントのステータスがTrueの場合、現状と同じ紐付けを行う
                                checkLabel((ILabel) checkedItem, (ITerm) o, true);
                            } else {
                                // イベントStatusがFalseの場合、その用語が既にラベルと紐づいているかチェックする
                                List<ITerm> labelTerms = ((ILabel) checkedItem).getTerms();
                                if (!labelTerms.contains(o)) {
                                    // 用語とラベルが紐づいていない場合、用語とラベルを紐づける(半チェック状態または未チェック状態)
                                    checkLabel((ILabel) checkedItem, (ITerm) o, true);
                                } else {
                                    //用語がラベル紐づいている場合、紐づけを外すかどうかのフラグをチェックする
                                    if (labelRemoveFlag) {
                                        //ラベルのリレーションを外す
                                        checkLabel((ILabel) checkedItem, (ITerm) o, false);
                                    }
                                }
                            }

                            terms.add((ITerm) o);
                        }
                    }
                    /* #767 ラベル情報を更新する */
                    labelGroup.setChecked(terms);

                    // TODO つど保存したいが、ラベル辞書が取れない
                    /* ラベルカラムの表示を更新 */
                    viewer.update(terms.toArray(), null);
                }
            }
        });

        /* 関連付けるラベル辞書(複数)を取得する */
        List<ICoronaDic> children;
        if (isLocalFile()) {
            IUILibrary uiLib = CoronaModel.INSTANCE.getLibrary(uiDic);
            ICoronaDics lib = uiLib.getObject();
            if (lib != null) {
                children = searchChildDictionaries(udic, lib.getDictionarys(ILabelDic.class));
            } else {
                children = new ArrayList<ICoronaDic>(0);
            }
        } else {
            children = searchChildDictionaries(udic, IoActivator.getService().getDictionarys(ILabelDic.class));
        }
        List<ILabelDic> labelDics = new ArrayList<ILabelDic>(children.size());
        for (ICoronaDic child : children) {
            if (child instanceof ILabelDic) {
                labelDics.add((ILabelDic) child);
            }
        }
        labelGroup.setRelatedLabelDics(labelDics, udic.getId());
    }


    /**
     * このユーザ辞書で選択できるラベル辞書を増やす
     * 
     * @param labelDic
     */
    public void addRelated(ILabelDic labelDic) {
        labelGroup.addRelated(labelDic);
    }


    /**
     * このユーザ辞書で選択できるラベル辞書から除く
     * 
     * @param labelDic
     */
    public void removeRelated(ILabelDic labelDic) {
        labelGroup.removeRelated(labelDic);
    }

    private List<ILabel> labelsForEmptyRow = new ArrayList<ILabel>();


    public List<ILabel> getLabels(ITerm term) {
        return labelGroup.getRelatedLabels(term);
    }


    /**
     * state == true ? labelにtermを紐づける<br />
     * state == false ? labelからtermを消す。<br />
     * termがemptyRowなら、バッファとしてため込む
     * 
     * @param label
     * @param term
     * @param state
     */
    void checkLabel(ILabel label, ITerm term, boolean state) {
        if (state) {
            if (isLastRow(term)) {
                labelsForEmptyRow.add(label);
            }
            label.addTerm(term);
        } else {
            if (isLastRow(term)) {
                labelsForEmptyRow.remove(label);
            }
            label.removeTerm(term);
        }
        markDirty();
    }

    private ISelectionChangedListener tableSelectionChangedListener = new ISelectionChangedListener() {
        /** ラベルとの紐づけに使う */
        IStructuredSelection currentSelection = new StructuredSelection();


        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection prevSelection = currentSelection;
            currentSelection = (IStructuredSelection) event.getSelection();
            if (!currentSelection.equals(prevSelection)) {
                List<ITerm> terms = new ArrayList<ITerm>(currentSelection.size());
                for (Object o : currentSelection.toArray()) {
                    if (o instanceof ITerm) {
                        terms.add((ITerm) o);
                    }
                }
                if (!terms.isEmpty()) {
                    labelGroup.setEnabled(true);
                    labelGroup.setChecked(terms);
                } else {
                    labelGroup.setEnabled(false);
                    labelGroup.clearCheckedAll();
                }
            }
        }
    };


    /* ****************************************
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }


    public void add(ITerm term) {
        List<ITerm> terms = new ArrayList<ITerm>(1);
        terms.add(term);
        add(terms);
    }


    public void add(List<ITerm> terms) {
        if (editable) {
            boolean add = false;
            boolean removeEmptyRow = dataProvider.removeData(emptyRow);
            for (ITerm term : terms) {
                if (term != null) {
                    if (term.isError()) {
                        errorItems.add(term);
                    }
                    dataProvider.addData(term);
                    add = true;
                }
            }
            if (add) {
                /* 空行の件数が入ってしまうので、空行を戻す前に件数表示を更新 */
                toolbarGroup.updateDataNumLabel();
            }
            if (removeEmptyRow) {
                /* addEmptyRow()ほどたくさん処理しなくていい。空行を追加するだけ */
                emptyRow = ((UserDicDataProvider) dataProvider).addEmptyData();
            }
            if (add || removeEmptyRow) {
                viewer.refresh();
            }
        }
        markDirty();
    }


    /**
     * 選択されているアイテムを削除する。ただし、最終行（空行）は消さない
     */
    public void remove() {
        boolean remove = false;

        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

        Object[] elements = selection.toArray();
        if (elements.length > 0) {
            int size = elements.length - 1;
            for (int i = 0; i < size; i++) {
                Object o = elements[i];
                if (o instanceof ITerm) {
                    ITerm term = (ITerm) o;
                    dataProvider.removeData(term);
                    errorItems.remove(term);

                    remove = true;
                }
            }
            /*
             * 空行だったら消さない。
             * 空行の可能性があるのは一番最後だけなので、最後だけチェックする
             */
            Object o = elements[size];
            if (o instanceof ITerm) {
                if (!isLastRow((ITerm) o)) {
                    ITerm term = (ITerm) o;
                    dataProvider.removeData(term);
                    errorItems.remove(term);

                    remove = true;
                }
            }

            if (remove) {
                /* 用語に紐づくラベルがある場合、紐付けを外す */
                List<ILabel> labels = labelGroup.getRelatedLabels((ITerm) o);
                for (ILabel label : labels) {
                    checkLabel(label, (ITerm) o, false);
                }
                markDirty();
                viewer.refresh();
                toolbarGroup.updateDataNumLabel();
            }
        }
    }


    void markDirty() {
        firePropertyChange(PROP_DIRTY);
    }

    ITerm emptyRow;


    public boolean isLastRow(ITerm term) {
        if (emptyRow != null) {
            return ((Object) emptyRow).equals(term);
        }
        return false;
    }


    void addEmptyRow() {
        if (editable) {
            toolbarGroup.updateDataNumLabel();
            emptyRow = ((UserDicDataProvider) dataProvider).addEmptyData();
            if (emptyRow != null) {
                /* 空行のまま保存されたときに消す紐づけをクリア */
                labelsForEmptyRow.clear();

                viewer.refresh();
            }
        }
    }


    @Override
    public String getPartName() {
        return getEditorInput().getName();
    }


    @Override
    public String getTitleToolTip() {
        return getEditorInput().getToolTipText();
    }


    @Override
    public Image getTitleImage() {
        if (DicType.JUMAN.equals(udic.getDicType())) {
            return Icons.INSTANCE.get(Icons.IMG_DIC_JUMAN);
        } else if (DicType.COMMON.equals(udic.getDicType())) {
            return Icons.INSTANCE.get(Icons.IMG_DIC_COMMON);
        } else if (DicType.CATEGORY.equals(udic.getDicType())) {
            return Icons.INSTANCE.get(Icons.IMG_DIC_CATEGORY);
        } else if (DicType.SPECIAL.equals(udic.getDicType())) {
            return Icons.INSTANCE.get(Icons.IMG_DIC_SPECIAL);
        }
        return super.getTitleImage();
    }


    /* *************************
     * アクション関連
     */
    public static final String UDE_EDIT = "ude_edit";


    @Override
    protected void hookContextMenu() {
        /* コンテクストメニューが出せるように設定 */
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                Action action = ccp.getCutAction();
                if (action != null) {
                    manager.add(action);
                }
                action = ccp.getCopyAction();
                if (action != null) {
                    manager.add(action);
                }
                action = ccp.getPasteAction();
                if (action != null) {
                    manager.add(action);
                }
                manager.add(new GroupMarker(UDE_EDIT));
                manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
        getSite().registerContextMenu(menuMgr, viewer);
    }

    UserDicEditorCCP ccp = null;


    @Override
    protected void createActions() {
        ccp = new UserDicEditorCCP(this);
        viewer.addSelectionChangedListener(ccp);
        setAction(ActionFactory.CUT.getId(), ccp.getCutAction());
        setAction(ActionFactory.COPY.getId(), ccp.getCopyAction());
        setAction(ActionFactory.PASTE.getId(), ccp.getPasteAction());

        /* 全選択（最終行は除く） */
        Action action = new Action() {
            @Override
            public void run() {
                TableItem[] items = viewer.getTable().getItems();
                int length = items.length;
                TableItem lastItem = items[length - 1];
                if (lastItem.getData() instanceof ITerm) {
                    ITerm term = (ITerm) lastItem.getData();
                    if (isLastRow(term)) {
                        length--;
                    }
                }
                List<Object> select = new ArrayList<Object>(length);
                for (int i = 0; i < length; i++) {
                    select.add(items[i].getData());
                }
                viewer.setSelection(new StructuredSelection(select));
            }
        };
        setAction(ActionFactory.SELECT_ALL.getId(), action);

        action = new Action() {
            @Override
            public void run() {
                actionBar.openFilterDialog();
            }
        };
        setAction(ActionFactory.FIND.getId(), action);

        // 編集モード(F2)
        action = new Action() {
            @Override
            public void run() {
                EditorUtil.editMode(viewer, 2);
            }
        };
        setAction(ActionFactory.RENAME.getId(), action);

        // 更新(F5)
        final ICoronaDic finalDic = coronaDic;
        action = new Action() {
            @Override
            public void run() {
                /* */
                if (isDirty()) {
                    /* 編集されていたら */
                    String message = "辞書は編集されています。\n更新を行うと編集内容は破棄され最終保存時の状態に戻ります。";
                    if (MessageDialog.openConfirm(getSite().getShell(), "辞書の更新", message)) {
                        /*
                         * Memo 辞書更新日のチェックによって保存に失敗する状況になった時、
                         * この条件を入れるとその状況が改善されない
                         */
                        // if (finalDic.isDirty()) {
                        finalDic.update();
                        // }
                        for (ILabelDic ldic : labelGroup.getRelatedlabelDics()) {
                            if (ldic.isDirty()) {
                                ldic.updateRecords();
                            }
                        }
                        labelGroup.clearCache();
                        labelGroup.refresh();
                        ISelection prevSelection = viewer.getSelection();
                        viewer.setInput(dataProvider.current(true));
                        /* labelGroupのチェックを更新するために、用語を再選択する */
                        viewer.setSelection(null);
                        viewer.setSelection(prevSelection);
                        addEmptyRow();
                        markDirty();
                    }
                } else {
                    /* 編集されていなかったら */
                    // TODO ↑の編集されていた時の処理と全く同じであるため、メソッド化予定
                    finalDic.update();
                    for (ILabelDic ldic : labelGroup.getRelatedlabelDics()) {
                        if (ldic.isDirty()) {
                            ldic.updateRecords();
                        }
                    }
                    labelGroup.clearCache();
                    labelGroup.refresh();
                    ISelection prevSelection = viewer.getSelection();
                    viewer.setInput(dataProvider.current(true));
                    /* labelGroupのチェックを更新するために、用語を再選択する */
                    viewer.setSelection(null);
                    viewer.setSelection(prevSelection);
                    addEmptyRow();
                }
            }
        };
        setAction(ActionFactory.REFRESH.getId(), action);

        // 削除(Delete)
        action = new Action() {
            @Override
            public void run() {
                if (isEditable())
                    remove();
            }
        };
        setAction(ActionFactory.DELETE.getId(), action);


        /**
         * #512 単語レコード選択中か、それ以外(文字列入力中も含む)を選択中かの場合でアクションを切り替える
         */
        viewer.getControl().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setGlobalActions(true);
            }


            @Override
            public void focusLost(FocusEvent e) {
                setGlobalActions(false);
            }
        });
    }

}
