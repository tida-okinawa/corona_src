/**
 * @version $Id: LabelDicEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/01 12:07:44
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import com.tida_okinawa.corona.correction.erratum.Erratum;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.ui.editors.user.UserDicEditor;


/**
 * @author kousuke-morishima
 */
public class LabelDicEditor extends AbstractDicEditor {
    public static final String EDITOR_ID = "com.tida_okinawa.corona.ui.editor.labeldic";

    TreeViewer viewer;
    private ObjectSyncManager sync;

    IStructuredSelection selection;
    IPartListener disposer;


    public LabelDicEditor() {
        sync = ObjectSyncManager.INSTANCE;
    }


    @Override
    public void createPartControl(Composite parent) {
        Composite composite = CompositeUtil.defaultComposite(parent, 1);
        composite.setBackground(new Color(null, 255, 255, 255));

        /* #765 ラベル検索機能の追加 */
        Composite searchComposite = new Composite(composite, SWT.NONE);
        searchComposite.setLayout(new GridLayout(3, false));
        searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        searchComposite.setBackground(new Color(null, 255, 255, 255));
        searchText = new Text(searchComposite, SWT.BORDER | SWT.SINGLE);
        GridData textGrid = CompositeUtil.gridData(false, true, 1, 1);
        textGrid.widthHint = 150;
        searchText.setLayoutData(textGrid);
        searchText.addSelectionListener(new SelectionAdapter() {
            @Override
            /** ラベル検索用テキストボックスにてEnterキー押下 */
            public void widgetDefaultSelected(SelectionEvent e) {
                searchLabel();
            }
        });
        Button btnSearch = new Button(searchComposite, SWT.NONE);
        btnSearch.setText("検索");
        btnSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            /** 検索ボタン押下 */
            public void widgetSelected(SelectionEvent e) {
                searchLabel();
            }
        });
        Button btnClear = new Button(searchComposite, SWT.NONE);
        btnClear.setText("クリア");
        btnClear.addSelectionListener(new SelectionAdapter() {
            @Override
            /** クリアボタン押下 */
            public void widgetSelected(SelectionEvent e) {
                searchText.setText("");
                searchLabel();
            }
        });
        /* エディタ上部のラベルに説明を表示 */
        Label label = new Label(composite, SWT.NONE);
        label.setText("右クリックでメニューが表示されます。");
        label.setBackground(new Color(null, 255, 255, 255));
        /* ツリー構築 */
        viewer = new TreeViewer(new Tree(composite, SWT.MULTI));
        viewer.setContentProvider(new LabelDicContentProvider());
        viewer.setLabelProvider(new LabelDicLabelProvider());
        viewer.addSelectionChangedListener(selectionListener);
        Tree tree = viewer.getTree();
        tree.setLayout(new GridLayout());
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewer.setInput(getEditorInput());
        super.createPartControl(parent);
        getSite().setSelectionProvider(new ISelectionProvider() {
            @Override
            public void addSelectionChangedListener(ISelectionChangedListener listener) {
                viewer.addSelectionChangedListener(listener);
            }


            @Override
            public ISelection getSelection() {
                return viewer.getSelection();
            }


            @Override
            public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            }


            @Override
            public void setSelection(ISelection selection) {
            }
        });
    }


    private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            selection = (IStructuredSelection) event.getSelection();
        }
    };

    private ILabelDic labelDic;


    @Override
    protected boolean validDictionary(ICoronaDic dic) {
        if (dic instanceof ILabelDic) {
            labelDic = (ILabelDic) dic;
            return true;
        }
        return false;
    }


    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        try {
            // TODO ただし、まだうまく動かない？
            /*
             * このラベル辞書を参照しているユーザ辞書が開かれていて、かつ、
             * そのユーザ辞書がまだこの辞書の存在を知らなかったら新しく作った辞書の情報を伝える
             */
            for (IEditorReference editorRef : getRelatedUserDicEditor(labelDic)) {
                UserDicEditor editor = (UserDicEditor) editorRef.getEditor(false);
                editor.addRelated(labelDic);
            }
        } catch (PartInitException e) {
            e.printStackTrace();
        }

        /* リスナ登録 */
        List<ICoronaDic> revertDics = new ArrayList<ICoronaDic>();
        revertDics.add(labelDic);
        disposer = new DicEditorDisposer(this, revertDics);
        getSite().getPage().addPartListener(disposer);
    }


    @Override
    public void dispose() {
        super.dispose();
        /* リスナ削除 */
        getSite().getPage().removePartListener(disposer);
    }


    public static List<IEditorReference> getRelatedUserDicEditor(ILabelDic labelDic) throws PartInitException {

        List<IEditorReference> ret = new ArrayList<IEditorReference>();
        IEditorReference[] editorRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();

        for (IEditorReference ref : editorRefs) {
            if (!(ref.getEditorInput() instanceof DicEditorInput)) {
                continue;
            }
            DicEditorInput refInput = (DicEditorInput) ref.getEditorInput();
            for (int parentId : labelDic.getParentIds()) {
                if (refInput.getDictionary().getId() == parentId) {
                    ret.add(ref);
                    break;
                }
            }

        }
        return ret;
    }


    /* ****************************************
     * 編集インタフェース
     */
    /**
     * ラベル追加処理
     * 
     * @param name
     *            追加するラベル名
     * @param parent
     *            追加するラベルの親
     * @param parentChk
     *            トップレベルにラベルを追加する場合、trueを指定する
     */
    public void add(String name, ILabel parent, Boolean parentChk) {
        ILabel newItem = null;
        if (parentChk) {
            /* トップレベルラベル作成 */
            newItem = IoActivator.getDicFactory().createLabel(name, null);
            labelDic.addItem(newItem);
        } else {
            /* 子ラベル作成 */
            newItem = IoActivator.getDicFactory().createLabel(name, parent);
        }

        markDirty();

        /* 表示の更新 */
        viewer.refresh();
        viewer.setSelection(new StructuredSelection(newItem), true);

        fireLabelModify(newItem, ObjectSyncEvent.ADDED);

        /* #756 検索結果を編集した場合の再表示 */
        searchLabel();
    }


    /**
     * ラベル削除処理
     * 
     * @param label
     *            削除するラベル
     */
    public void remove(ILabel label) {
        labelDic.removeItem(label);

        markDirty();

        /* 表示の更新 */
        viewer.refresh();
        if (label.getParent() != null) {
            viewer.setSelection(new StructuredSelection(label.getParent()));
        }

        fireLabelModify(label, ObjectSyncEvent.REMOVED);

        /* #756 検索結果を編集した場合の再表示 */
        searchLabel();
    }


    /**
     * ラベル名を変更する
     */
    public void rename() {
        /* 選択中の要素を取得 */
        ILabel label = (ILabel) selection.getFirstElement();
        if (label != null) {
            /* 名前の変更をダイアログで行う */
            InputDialog dialog = new InputDialog(getSite().getShell(), "名前の入力", "名前を入力してください。", label.getName(), new LabelNameInputValidator());
            if (dialog.open() == Window.OK) {
                /* 名前を直接編集できるようにしたいが。。。 */
                String newName = dialog.getValue();
                String oldName = label.getName();
                /* 現在の名前と入力された名前を比較。異なる場合、変更処理 */
                if (!oldName.equals(newName)) {
                    label.setName(newName);/* 入力された名前を設定 */

                    markDirty();
                    /* 表示の更新 */
                    viewer.update(label, null);

                    fireLabelModify(label, ObjectSyncEvent.MODIFIED);
                }
            }
        }
        /* #756 検索結果を編集した場合の再表示 */
        searchLabel();
    }


    /**
     * @param modified
     * @param type
     * @see ObjectSyncEvent#ADDED
     * @see ObjectSyncEvent#REMOVED
     * @see ObjectSyncEvent#MODIFIED
     */
    private void fireLabelModify(ILabel modified, int type) {
        /* よそで開いているかもしれないユーザ辞書へ変更を伝える */
        sync.modified(labelDic, new Object[] { modified }, type);
    }


    @Override
    protected void createActions() {
        // 更新(F5)
        Action action = new Action() {
            @Override
            public void run() {
                if (isDirty()) {
                    /* 編集されていたら */
                    String message = "辞書は編集されています。\n更新を行うと編集内容は破棄され最終保存時の状態に戻ります。";
                    if (MessageDialog.openConfirm(getSite().getShell(), "辞書の更新", message)) {
                        ((DicEditorInput) getEditorInput()).getDictionary().update();
                        viewer.setInput(getEditorInput());
                        markDirty();
                    }
                } else {
                    /* 編集されていなかったら */
                    // TODO ↑の編集されていた時の処理と全く同じであるため、メソッド化予定
                    ((DicEditorInput) getEditorInput()).getDictionary().update();
                    viewer.setInput(getEditorInput());
                }
                /* #756 検索結果を編集した場合の再表示 */
                searchLabel();
            }
        };
        setAction(ActionFactory.REFRESH.getId(), action);

        // 削除(Delete)
        action = new Action() {
            @Override
            public void run() {
                if ((selection != null) && (selection.getFirstElement() instanceof ILabel)) {
                    for (Object o : selection.toList()) {
                        remove((ILabel) o);
                    }
                }
            }
        };
        setAction(ActionFactory.DELETE.getId(), action);

        // 名前変更(F2)
        action = new Action() {
            @Override
            public void run() {
                if (selection != null)
                    rename();
            }
        };
        setAction(ActionFactory.RENAME.getId(), action);
    }


    @Override
    protected void hookContextMenu() {
        /* コンテクストメニューが出せるように設定 */
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
        getSite().registerContextMenu(menuMgr, viewer);
    }


    /* ****************************************
     * 保存関連
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        /* コミットが成功したら保存完了 */
        if (labelDic.commit(monitor)) {
            firePropertyChange(PROP_DIRTY);
        }
    }


    @Override
    public boolean isDirty() {
        return labelDic.isDirty();
    }


    void markDirty() {
        firePropertyChange(PROP_DIRTY);
    }


    @Override
    public void doSaveAs() {
    }


    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }


    @Override
    public void setFocus() {
        viewer.getTree().setFocus();
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
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySheetPage.class)) {
            return new PropertySheetPage();
        }
        return super.getAdapter(adapter);
    }

    /* ****************************************
     * #765 ラベル検索機能用
     */
    Text searchText;


    /** ラベル検索 */
    private void searchLabel() {
        String word = searchText.getText();
        if (word == null || "".equals(word)) {
            /* 検索文字列が空白の場合は全データ再表示 */
            viewer.setInput(getEditorInput());
            return;
        }
        LabelRecords labelRecords = new LabelRecords();
        ICoronaDic coronaDic = ((DicEditorInput) getEditorInput()).getDictionary();
        if (coronaDic instanceof ILabelDic) {
            for (IDicItem item : coronaDic.getItems()) {
                searchLabelWork(labelRecords, word, (ILabel) item);
                /* 検索文字列を全角化して検索 */
                String zenkaku = Erratum.convertZenkakuString(word);
                if (!zenkaku.equals(word)) {
                    searchLabelWork(labelRecords, zenkaku, (ILabel) item);
                }
            }
        }
        viewer.setInput(labelRecords);
    }


    /**
     * 再帰的にテキストに入力された文字列を検索
     * 
     * @param labelRecords
     *            見つかったラベルの格納先クラス
     * @param word
     *            検索する用語
     * @param label
     *            検索対象となるラベル
     */
    private void searchLabelWork(LabelRecords labelRecords, String word, ILabel label) {
        /* 見つかったらリストに追加（部分一致） */
        if (label.getName() != null && (label.getName().indexOf(word) != -1)) {
            labelRecords.add(label);
        }
        /* 子のラベルも検索 */
        for (ILabel child : label.getChildren()) {
            searchLabelWork(labelRecords, word, child);
        }
    }


    /** ラベル管理クラス */
    public static class LabelRecords {
        private List<ILabel> labelList;


        /**
         * ラベルをリストで管理する
         */
        public LabelRecords() {
            labelList = new ArrayList<ILabel>(10);
        }


        /**
         * ラベルの追加
         * 
         * @param label
         *            ラベル
         */
        public void add(ILabel label) {
            labelList.add(label);
        }


        /**
         * ラベルリストを配列に変換
         * 
         * @return ラベルリストの配列
         */
        public Object[] toArray() {
            return labelList.toArray();
        }
    }
}
