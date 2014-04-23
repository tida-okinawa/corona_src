/**
 * @version $Id: FrequentTermPage.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/09/29 16:10:40
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.progress.UIJob;

import com.tida_okinawa.corona.correction.frequent.FrequentRecord;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.util.Pair;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.editors.user.ComboItem;
import com.tida_okinawa.corona.ui.editors.user.PrivateTermClass;
import com.tida_okinawa.corona.ui.editors.user.PrivateTermPart;

/**
 * @author takayuki-matsumoto
 */
public class FrequentTermPage extends FormPage {
    /*
     * TODO 未定義語を辞書に保存できるようにする
     */

    protected String formTitle;

    protected boolean doSaveFlg = false;

    /**
     * 登録先辞書一覧に表示しない辞書一覧
     */
    public// List<IUserDic> jumanDics; // Memo unused
    List<FrequentRecord> errorItems = new ArrayList<FrequentRecord>();

    /**
     * @param editor
     */
    TermPart[] termParts;


    public FrequentTermPage(FormEditor editor) {
        this(editor, "FrequentTermPage.UniqueIdentifier", "登録済み用語");
    }


    /**
     * @param editor
     * @param id
     * @param title
     */
    public FrequentTermPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        this.formTitle = "頻出用語抽出フォーム（登録済み用語）";

        if (UIActivator.isAlpha()) {
            termParts = new TermPart[] { TermPart.NOUN };
        } else {
            // TODO なしは抜かなきゃ
            termParts = TermPart.values();
        }
    }

    /* ****************************************
     * 保存
     */
    /**
     * doSaveでDBに保存されたアイテム<br />
     * &lt;編集用アイテム, &lt;保存した用語, 用語を保存した辞書&gt;&gt;
     */
    Map<FrequentRecord, Pair<ITerm, ICoronaDic>> committedItems = new HashMap<FrequentRecord, Pair<ITerm, ICoronaDic>>();


    @Override
    public void doSave(IProgressMonitor monitor) {

        super.doSave(monitor);

        /* 未定義ページをまだ開いていないとき、保存する意味がないので */
        if (!uiCreated) {
            return;
        }

        /* 初期化処理 */
        errorItems.clear();
        doSaveFlg = false;
        for (Iterator<FrequentRecord> itr = uncommittedItems.iterator(); itr.hasNext();) {
            FrequentRecord rec = itr.next();
            if (isValid(rec)) {
                String value = rec.getGenkei();
                String reading = rec.getYomi();
                /* #477 よみ自動入力（よみが空だった場合に原形の文字列を入れる） */
                if (reading.isEmpty()) {
                    reading = value;
                }
                String termPart = rec.getHinshi();
                String termClass = rec.getHinshiSaibunrui();
                String cform = rec.getCform();
                Pair<ITerm, ICoronaDic> pair = committedItems.get(rec);
                ICoronaDic newDic = rec.getDestDictionary();
                if (pair == null) {
                    ITerm item = IoActivator.getDicFactory().createTerm(value, reading, termPart, termClass, cform, "");
                    if (newDic != null) {
                        newDic.addItem(item);
                        committedItems.put(rec, new Pair<ITerm, ICoronaDic>(item, newDic));
                    }
                } else {
                    ITerm item = pair.getValue1();
                    // 辞書が違っていたら、前の辞書から消して今の辞書に入れる
                    ICoronaDic oldDic = pair.getValue2();
                    if (newDic == null) {
                        // 登録先辞書が空白にされた。消す
                        oldDic.removeItem(item);
                        committedItems.remove(rec);
                    } else {
                        // 登録先辞書が同じだろうが違かろうが(辞書内のアイテムとはインスタンスが異なるため)。消して
                        oldDic.removeItem(item);
                        // 入れる(前のデータを使いまわすと、IDがあって入らない)
                        ITerm newItem = IoActivator.getDicFactory().createTerm(value, reading, termPart, termClass, cform, "");
                        newDic.addItem(newItem);
                        committedItems.put(rec, new Pair<ITerm, ICoronaDic>(newItem, newDic)); // 保存した辞書を更新するために置き換える
                    }
                }
                itr.remove();
            } else {
                if (rec.getDestDictionary() != null) {
                    errorItems.add(rec);
                } else {
                    // 対象外のデータはuncommittedItemsより抜く
                    /* 自身のfor文の中でremoveしちゃだめだよー */
                    // uncommittedItems.remove(rec);
                    itr.remove(); // こっちを使うこと!
                }
            }
        }

        Collection<ICoronaDic> commitDics = getDictionaries();
        for (ICoronaDic dic : commitDics) {
            if (dic.isDirty()) {
                dic.commit(monitor);
                doSaveFlg = true; /* 保存処理実行済み */
            }
        }
        if (errorItems.size() == 0) {
            // エラーがない場合のみフラグを解除
            dirtyChanged();
        }
    }


    boolean isValid(FrequentRecord data) {

        /* ユーザ用語辞書で、単語の文字数は64文字制限しているので、ここでも制限 */
        if (data.getGenkei().length() > 64) {
            return false;
        }

        /* 品詞、品詞詳細が空白　 */
        if (data.getGenkei().equals("") || data.getHinshi().equals("") || data.getHinshiSaibunrui().equals("")) {
            return false;
        }
        /* 登録できる品詞以外を選択　 */
        boolean findName = false;
        for (TermPart part : termParts) {
            if (data.getHinshi().equals(part.getName())) {
                findName = true;
                break;
            }
        }
        if (!findName) {
            return false;
        }
        return true;
    }


    @Override
    public boolean isDirty() {
        return !uncommittedItems.isEmpty();
    }


    public void dirtyChanged() {
        getEditor().editorDirtyStateChanged();
    }

    /* ****************************************
     * UI構築
     */
    private boolean uiCreated = false;
    public TableViewer viewer;
    FrequentRecord[] inputData;

    /**
     * ソート(回数)
     */
    protected ViewerSorter sortCount = new ViewerSorter() {
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            assert viewer instanceof TableViewer;
            Table tbl = ((TableViewer) viewer).getTable();
            if (tbl.getSortDirection() == SWT.DOWN) {
                if (!(e2 instanceof FrequentRecord)) {
                    return -1;
                }
                if (!(e1 instanceof FrequentRecord)) {
                    return 1;
                }
                return ((FrequentRecord) e2).getCount() - ((FrequentRecord) e1).getCount();
            } else {
                if (!(e1 instanceof FrequentRecord)) {
                    return -1;
                }
                if (!(e2 instanceof FrequentRecord)) {
                    return 1;
                }
                return ((FrequentRecord) e1).getCount() - ((FrequentRecord) e2).getCount();
            }
        }

    };
    /**
     * ソート(原形)
     */
    protected ViewerSorter sortHeader = new ViewerSorter() {
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {

            assert viewer instanceof TableViewer;
            Table tbl = ((TableViewer) viewer).getTable();
            if (tbl.getSortDirection() == SWT.DOWN) {
                if (!(e2 instanceof FrequentRecord)) {
                    return -1;
                }
                if (!(e1 instanceof FrequentRecord)) {
                    return 1;
                }
                return ((FrequentRecord) e2).getGenkei().compareTo(((FrequentRecord) e1).getGenkei());
            } else {
                if (!(e1 instanceof FrequentRecord)) {
                    return -1;
                }
                if (!(e2 instanceof FrequentRecord)) {
                    return 1;
                }
                return ((FrequentRecord) e1).getGenkei().compareTo(((FrequentRecord) e2).getGenkei());
            }
        }

    };


    @Override
    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
        form.setText(formTitle);
        form.getBody().setLayout(new GridLayout());
        form.getBody().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite composite = CompositeUtil.defaultComposite(form.getBody(), 1);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

        /* カラム設定 */
        Table tbl = viewer.getTable();
        tbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tbl.setHeaderVisible(true);
        createTable(tbl);

        FrequentTermEditorInput input = (FrequentTermEditorInput) getEditorInput();
        ViewerFilter vf = createFilter();
        if (vf != null) {
            viewer.setFilters(new ViewerFilter[] { vf });
        }
        inputData = input.getItems().toArray(new FrequentRecord[input.getItems().size()]);

        viewer.setInput(inputData);

        uiCreated = true;

        setEditKeyListener(viewer, 2);
    }


    protected void createTable(Table tbl) {
        createColumnWithSort(tbl, "用語", 130, sortHeader);
        tbl.setSortColumn(createColumnWithSort(tbl, "回数", 80, sortCount));
        tbl.setSortDirection(SWT.DOWN);
        createColumnWithSort(tbl, "登録先辞書", 120, null);
        createColumnWithSort(tbl, "品詞", 120, null);
        createColumnWithSort(tbl, "品詞詳細", 120, null);
        createColumnWithSort(tbl, "登録元辞書", 120, null);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new FrequentLabelProvider());
        viewer.setSorter(sortCount);

        setCellEditor();
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


    /**
     * 
     * @param parent
     * @param title
     * @param width
     * @param sortField
     */
    protected TableColumn createColumnWithSort(final Table parent, String title, int width, final ViewerSorter sortField) {
        final TableColumn column = CompositeUtil.createColumn(parent, title, width);
        if (sortField != null) {
            column.addSelectionListener(new SelectionAdapter() {
                private boolean asc = false;


                @Override
                public void widgetSelected(SelectionEvent e) {
                    asc = !asc;
                    parent.setSortColumn(column);
                    parent.setSortDirection((asc) ? SWT.UP : SWT.DOWN);
                    viewer.setSorter(sortField);
                    viewer.setInput(inputData);
                }
            });
        }
        return column;
    }


    /**
     * 表示アイテムのフィルタを返す。
     * 
     * @return フィルタ。使わない場合、null
     */
    protected ViewerFilter createFilter() {
        ViewerFilter vf = new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof FrequentRecord) {
                    FrequentRecord fr = (FrequentRecord) element;
                    fr.getRegisteredDics();
                    return TermPart.NOUN.getName().equals(fr.getHinshi());
                }
                return true;
            }
        };

        return vf;
    }

    /* ********************
     * 編集機能
     */
    public static final String PROP_ORG = "org";
    public static final String PROP_COUNT = "count";
    public static final String PROP_DIC = "dic";
    public static final String PROP_PART = "part";
    public static final String PROP_DETAIL = "detail";
    private static final String PROP_SRCDIC = "srcdic";
    protected static final String[] properties = new String[] { PROP_ORG, PROP_COUNT, PROP_DIC, PROP_PART, PROP_DETAIL, PROP_SRCDIC };
    protected ComboBoxCellEditor dicCellEditor;
    protected ComboBoxCellEditor detailComboEditor;


    private void setCellEditor() {
        Table tbl = viewer.getTable();

        dicCellEditor = new ComboBoxCellEditor(tbl, new String[0]);
        ComboBoxCellEditor partCellEditor = new ComboBoxCellEditor(tbl, getPartItems().getNames());
        detailComboEditor = new ComboBoxCellEditor(tbl, new String[0]);
        final CellEditor[] editors = new CellEditor[] { null, null, dicCellEditor, partCellEditor, detailComboEditor, null, null };

        viewer.setColumnProperties(properties);
        viewer.setCellEditors(editors);
        viewer.setCellModifier(new FrequentRegisterCellModifier());
        EditorUtil.setFocusMoveListener(editors, viewer, 2, 3, 4);
    }


    protected ComboItem<TermPart> getPartItems() {
        PrivateTermPart ret = new PrivateTermPart(termParts, false);
        return ret;
    }


    /* ********************
     * 登録元辞書Job
     */
    UpdateJob dicsJob;


    public void startJob() {
        if (dicsJob == null) {
            /* 登録元辞書を探すJobを開始する */
            dicsJob = new UpdateJob(viewer, inputData);
            dicsJob.setUser(false);
            dicsJob.setSystem(true);
        }
        if (uiCreated) {
            if (dicsJob.getState() != Job.RUNNING) {
                dicsJob.schedule();
            }
        }
    }


    public void cancelJob() {
        if (dicsJob != null) {
            dicsJob.cancel();
        }
    }

    private class UpdateJob extends Job {
        TableViewer viewer_uj;
        FrequentRecord[] records;
        private int currentIndex = 0;


        public UpdateJob(TableViewer viewer, FrequentRecord[] records) {
            super("登録元辞書を探すJob");
            setSystem(true);
            this.viewer_uj = viewer;
            Object[] filtered = records;
            for (ViewerFilter filter : viewer.getFilters()) {
                filtered = filter.filter(viewer, new Object(), filtered);
            }
            viewer.getSorter().sort(viewer, filtered);
            this.records = new FrequentRecord[filtered.length];
            System.arraycopy(filtered, 0, this.records, 0, filtered.length);
        }


        @Override
        protected void canceling() {
            /*
             * エディタが非アクティブになったり、タブが未定義語に切り替わった時に処理される。
             * キャンセルのタイミングによっては、表示が更新される前に処理が中断してしまうことに対応
             */
            updateViewer(updateWaitElements.clone());
        }

        FrequentRecord[] updateWaitElements = new FrequentRecord[0];


        void updateViewer(FrequentRecord[] updateElements) {
            /* Jobがキャンセルされた場合、要素の末尾がnullのままのことがあるので取り除く */
            int cnt = 0;
            for (FrequentRecord rec : updateElements) {
                if (rec != null) {
                    cnt++;
                }
            }
            FrequentRecord[] dest = new FrequentRecord[cnt];
            System.arraycopy(updateElements, 0, dest, 0, cnt);
            viewer_uj.update(dest, null);
        }


        @Override
        public IStatus run(IProgressMonitor monitor) {
            if (viewer_uj.getTable().isDisposed()) {
                return Status.CANCEL_STATUS;
            }

            if (records.length == 0) {
                return Status.OK_STATUS;
            }

            /* 辞書件数が多くなると検索に時間がかかるため、１０件ずつしか処理しない */
            int iterations = Math.min(10, records.length - currentIndex);
            updateWaitElements = new FrequentRecord[iterations];
            for (int i = 0; i < iterations; i++) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }

                try {
                    records[currentIndex].createRegisteredDics(((FrequentTermEditorInput) getEditorInput()).getDics());
                } catch (SQLException e) {
                    /*
                     * 本来は、forを抜けて更新処理そのものも止めるべきだが、今は手を入れない
                     */
                    System.err.println("データベース接続がないため、登録元辞書を取得できません。");
                }
                updateWaitElements[i] = records[currentIndex++];
            }
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            final FrequentRecord[] updateElements = updateWaitElements.clone();
            Job uiJob = new UIJob("") {
                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    updateViewer(updateElements);
                    return Status.OK_STATUS;
                }
            };
            uiJob.setSystem(true);
            uiJob.schedule();

            if (currentIndex < records.length) {
                /* 表示更新を挟ませるため、ディレイを設定（長いほど他の処理は早くなるが、この処理は遅くなる） */
                schedule(100);
            } else {
                //
            }

            return Status.OK_STATUS;
        }

    }

    /**
     * 変更されたけど、まだdoSaveでDBに保存されていないアイテム。保存されたアイテムが編集された場合もここで管理する
     */
    public Set<FrequentRecord> uncommittedItems = new HashSet<FrequentRecord>();

    public class FrequentRegisterCellModifier implements ICellModifier {
        @Override
        public boolean canModify(Object element, String property) {
            if (property.equals(PROP_ORG) || property.equals(PROP_COUNT)) {
                return false;
            }
            return true;
        }


        @Override
        public Object getValue(Object element, String property) {
            FrequentRecord data = (FrequentRecord) element;
            if (property.equals(PROP_DIC)) {
                ICoronaDic destDic = data.getDestDictionary();
                DicNameItem destDics = getDestDicItem();
                dicCellEditor.setItems(destDics.getNames());
                return destDics.getIndex(destDic);
            } else if (property.equals(PROP_PART)) {
                ComboItem<TermPart> termParts = getPartItems();
                return termParts.getIndex(TermPart.valueOfName(data.getHinshi()));
            } else if (property.equals(PROP_DETAIL)) {
                /* 品詞詳細アイテム設定 */
                TermPart termPart = TermPart.valueOfName(data.getHinshi());
                ComboItem<TermClass> termClass = createTermClass(termPart.getIntValue());
                String[] classes = termClass.getNames();
                detailComboEditor.setItems(classes);
                return termClass.getIndex(TermClass.valueOfName(data.getHinshiSaibunrui()));
            }
            return null;
        }


        @Override
        public void modify(Object element, String property, Object value) {
            // ユーザが編集を確定した時に呼ばれる
            // 値を、モデルに反映させる。
            boolean modify = false;

            FrequentRecord data = (FrequentRecord) ((TableItem) element).getData();
            if (property.equals(PROP_DIC)) {
                Integer index = (Integer) value;
                IUserDic newDic = (IUserDic) getDestDicItem().get(index); 
                // Memo もし遅いようなら getValueからmodiryの間だけフィールドに持つ
                modify = !equals(data.getDestDictionary(), newDic);
                data.setDestDictionary(newDic);

            } else if (property.equals(PROP_PART)) {
                /* 品詞設定 */
                Integer index = (Integer) value;
                TermPart newValue = getPartItems().get(index);
                if (newValue == null) {
                    modify = !equals(data.getHinshi(), "");
                    data.setHinshi("");
                    newValue = TermPart.NONE;
                } else if (!newValue.getName().equals(data.getHinshi())) {
                    modify = true;
                    data.setHinshi(newValue.getName());
                }
                if (modify) {
                    /* 品詞の値が変わったとき */
                    String[] detailItems = createTermClass(newValue.getIntValue()).getNames();
                    detailComboEditor.setItems(detailItems);
                    data.setHinshiSaibunrui(detailItems[0]);
                }

            } else if (property.equals(PROP_DETAIL)) {
                Integer index = (Integer) value;
                int partId = TermPart.valueOfName(data.getHinshi()).getIntValue();
                TermClass tc = createTermClass(partId).get(index);
                String termClass;
                if (tc != null) {
                    termClass = tc.getName();
                } else {
                    termClass = "";
                }
                modify = !equals(data.getHinshiSaibunrui(), termClass);
                data.setHinshiSaibunrui(termClass);
            }
            viewer.update(data, null);
            /* element のisDirty をチェックする */
            if (modify) {
                uncommittedItems.add(data);
                dirtyChanged();
            }
        }


        private boolean equals(Object o1, Object o2) {
            if (o1 == null) {
                if (o2 == null) {
                    return true;
                } else {
                    return false;
                }
            }
            return o1.equals(o2);
        }


        /**
         * @return 共通辞書と辞書フォルダからJuman辞書を除いたユーザ辞書を取ってくる
         */
        private DicNameItem getDestDicItem() {
            Collection<ICoronaDic> ret = getDictionaries();
            for (Iterator<ICoronaDic> itr = ret.iterator(); itr.hasNext();) {
                ICoronaDic dic = itr.next();
                if (DicType.JUMAN.equals(((IUserDic) dic).getDicType())) {
                    itr.remove();
                }
            }
            return new DicNameItem(ret.toArray(new ICoronaDic[ret.size()]));
        }

        private class DicNameItem extends ComboItem<ICoronaDic> {
            public DicNameItem(ICoronaDic[] items) {
                super(items, true);
            }


            @Override
            protected String toName(ICoronaDic item) {
                return item.getName();
            }
        }


        private PrivateTermClass createTermClass(int partId) {
            return new PrivateTermClass(TermClass.values(partId).toArray(new TermClass[TermClass.values(partId).size()]));
        }
    }


    /**
     * @return 今現在有効な辞書
     */
    public Collection<ICoronaDic> getDictionaries() {
        IUIProduct uiProduct = ((FrequentTermEditorInput) getEditorInput()).getUIProduct();
        ICoronaProduct product = uiProduct.getObject();
        ICoronaProject project = CoronaModel.INSTANCE.getProject(uiProduct).getObject();
        Set<ICoronaDic> ret = new TreeSet<ICoronaDic>(new Comparator<ICoronaDic>() {
            @Override
            public int compare(ICoronaDic o1, ICoronaDic o2) {
                if (o2 == null) {
                    return -1;
                }
                if (o1 == null) {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        ret.addAll(product.getDictionarys(IUserDic.class));
        ret.addAll(project.getDictionarys(IUserDic.class));

        // JUMAN辞書は除く
        for (Iterator<ICoronaDic> itr = ret.iterator(); itr.hasNext();) {
            ICoronaDic dic = itr.next();
            if (DicType.JUMAN.equals(((IUserDic) dic).getDicType())) {
                itr.remove();
            }
        }

        return ret;
    }

}
