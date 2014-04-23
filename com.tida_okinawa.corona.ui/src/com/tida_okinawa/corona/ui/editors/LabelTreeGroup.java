/**
 * @version $Id: LabelTreeGroup.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/14 12:05:26
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.ui.Icons;

/**
 * @author kousuke-morishima
 */
public class LabelTreeGroup implements IObjectSyncListener {

    protected CheckboxTreeViewer viewer;
    private Label titleLabel;


    /**
     * @param parent
     * @param style
     * @param site
     *            SelectionProviderの設定を行う場合必須。
     */
    public LabelTreeGroup(Composite parent, int style) {
        createContents(parent, style);
    }


    protected Control createContents(Composite parent, int style) {
        parent = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        parent.setLayout(layout);
        parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

        titleLabel = doCreateTreeLabel(parent);
        doCreateTreeViewer(parent, style);
        return parent;
    }


    /**
     * ラベル表示領域のタイトル
     * 
     * @param parent
     * @return
     */
    protected Label doCreateTreeLabel(Composite parent) {
        Label label = new Label(parent, SWT.LEFT | SWT.CENTER | SWT.WRAP);

        GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
        label.setLayoutData(layoutData);

        label.setBackground(new Color(null, 255, 255, 255));

        Font font = parent.getFont();
        FontData fd = font.getFontData()[0];
        fd.setHeight(11);
        label.setFont(new Font(null, fd));
        label.setText("ラベル一覧");
        return label;
    }


    /**
     * ラベルを表示するツリーを作成する
     * 
     * @param parent
     * @param style
     */
    protected void doCreateTreeViewer(Composite parent, int style) {
        viewer = new CheckboxTreeViewer(new Tree(parent, style));
        viewer.setContentProvider(new ExLabelDicContentProvider());
        viewer.setLabelProvider(new ExLabelDicLabelProvider());
        viewer.addTreeListener(new LabelTreeListener());
        viewer.addDoubleClickListener(new LabelTreeDoubleClickListener());
        viewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof ILabel) {
                    return ((ILabel) element).getId() > IDicItem.UNSAVED_ID;
                } else if (element instanceof ILabelDic) {
                    return true;
                }
                return false;
            }
        });

        Tree tree = viewer.getTree();
        tree.setLayout(new GridLayout());
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }


    /* ****************************************
     * 外部インタフェース
     */
    public void setInput(List<ILabelDic> inputDics) {
        viewer.setInput(inputDics);
        viewer.collapseAll();
        viewer.expandToLevel(2);
    }

    private List<Object> refreshElements = new ArrayList<Object>();
    private boolean isAllRefresh = false;


    /**
     * ツリー表示を更新する
     */
    public void refresh() {
        if (isAllRefresh) {
            isAllRefresh = false;
            refreshElements.clear();
            viewer.refresh();
        } else {
            if (refreshElements.size() == 0) {
                viewer.refresh();
            } else {
                for (Object o : refreshElements) {
                    viewer.update(o, null);
                }
            }
        }
    }


    public void setEnabled(boolean enabled) {
        viewer.getTree().setEnabled(enabled);
        titleLabel.setEnabled(enabled);
        if (enabled) {
            titleLabel.setBackground(new Color(null, 255, 255, 255));
        } else {
            titleLabel.setBackground(null);
        }
    }

    /* ****************************************
     * check
     */
    public static final int WHITE = 0;
    public static final int CHECK = 1;
    public static final int GRAY = 2;


    public void clearCheckedAll() {
        /* クリア */
        Object[] checkedItems = viewer.getCheckedElements();
        for (Object o : checkedItems) {
            viewer.setChecked(o, false);
        }
    }


    /**
     * 指定したラベルにチェックをつける
     * 
     * @param item
     * @param checked
     */
    public boolean setChecked(ILabel item, int checkState) {
        switch (checkState) {
        case WHITE:
            viewer.setGrayed(item, false);
            return viewer.setChecked(item, false);
        case CHECK:
            viewer.setGrayed(item, false);
            return viewer.setChecked(item, true);
        case GRAY:
            /* #767 先にチェックを外すようにする */
            viewer.setChecked(item, false);
            return viewer.setGrayChecked(item, true);
        default:
            return false;
        }
    }

    protected List<ITerm> sourceTerms = new ArrayList<ITerm>(0);
    /**
     * ラベルと数。複数Termを選択されたとき、すべての単語に紐づいたラベルか判断するために数を記憶する。
     */
    // Map<ILabel, Integer> checkLabels = new HashMap<ILabel, Integer>();
    public Map<ILabel, Integer> checkLabels = new HashMap<ILabel, Integer>();


    /**
     * 単語に紐づいているラベルにチェックを入れる<br />
     * チェックを入れる前に、今のチェック状態はクリアされる
     * 
     * @param term
     */
    public void setChecked(ITerm term) {
        clearCheckedAll();
        /*
         * Viewerをリフレッシュする中で、チェックをつけるアイテムを探している
         */
        List<ITerm> terms = new ArrayList<ITerm>(1);
        terms.add(term);
        setChecked(terms);
    }


    /**
     * ラベルツリーの情報を更新する
     * 
     * @param terms
     */
    public void setChecked(List<ITerm> terms) {
        clearCheckedAll();
        sourceTerms = new ArrayList<ITerm>(terms);
        checkLabels.clear();
        viewer.refresh();
        int size = sourceTerms.size();
        for (Entry<ILabel, Integer> e : checkLabels.entrySet()) {
            int checkState = CHECK;
            if (e.getValue() < size) {
                checkState = GRAY;
            }
            setChecked(e.getKey(), checkState);
        }

        /* #767 親ラベルのチェック状態を更新する */
        setParentsChecked();

        checkLabels.clear();
    }


    /**
     * #767 チェックされているラベルの親ラベルを取得し、 親ラベルのチェック状態を更新する
     */
    private void setParentsChecked() {
        for (Entry<ILabel, Integer> e : checkLabels.entrySet()) {
            if (e.getKey().getParent() != null) {
                // 親ラベルがいるので、親の状態を半チェックにしていく
                setParentsState(e.getKey());
            }
        }
    }


    /**
     * #767 親のチェック状態を再帰的に設定
     * 
     * @param label
     *            基準となるチェック状態のラベル
     * @return true：親が存在する / false：親が存在しない
     */
    private boolean setParentsState(ILabel label) {

        if (label.getParent() == null) {
            return false;
        }
        if (checkLabels.get(label.getParent()) == null) {
            // 親ラベルがチェック状態にないので、半チェック状態にする
            setChecked(label.getParent(), GRAY);
        }
        // 親ラベルの上位にさらに親ラベルがいないかチェックする
        if (setParentsState(label.getParent())) {
            return true;
        }
        return false;
    }


    /* ****************************************
     * リスナー
     */
    /**
     * コンストラクタのstyleにSWT.CHECKを指定している場合のみ有効
     * 
     * @param listener
     */
    public void addCheckStateListener(ICheckStateListener listener) {
        viewer.addCheckStateListener(listener);
    }


    public void removeCheckStateListener(ICheckStateListener listener) {
        viewer.removeCheckStateListener(listener);
    }


    @Override
    public void modified(ObjectSyncEvent event) {
        /* ラベル辞書に変更があった時、表示を更新する */
        Object[] items = event.getChangedItems();
        for (Object item : items) {
            if (item instanceof ILabel) {
                if (((ILabel) item).getParent() != null) {
                    refreshElements.add(((ILabel) item).getParent());
                } else {
                    isAllRefresh = true;
                }
            } else if (item instanceof ILabelDic) {
                isAllRefresh = true;
                break;
            }
        }
    }

    class LabelTreeListener implements ITreeViewerListener {
        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
        }


        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            /* ツリーを展開した時、子にチェックを入れる */
            if ((checkLabels != null) && !checkLabels.isEmpty()) {
                int size = sourceTerms.size();
                for (Entry<ILabel, Integer> e : checkLabels.entrySet()) {
                    int checkState = CHECK;
                    if (checkState < size) {
                        checkState = GRAY;
                    }
                    setChecked(e.getKey(), checkState);
                }
                checkLabels.clear();
            }
        }
    }

    /**
     * ダブルクリックでツリーを展開する。
     * 
     * @author KMorishima
     * 
     */
    static class LabelTreeDoubleClickListener implements IDoubleClickListener {
        @Override
        public void doubleClick(DoubleClickEvent event) {
            if (event.getSource() instanceof TreeViewer) {
                TreeViewer v = (TreeViewer) event.getSource();
                ITreeSelection selection = ((ITreeSelection) event.getSelection());
                Object element = selection.getFirstElement();
                if (v.isExpandable(element)) {
                    if (v.getExpandedState(element)) {
                        v.collapseToLevel(element, 1);
                    } else {
                        v.expandToLevel(element, 1);
                    }
                }
                if (true) {
                }
            }
        }
    }

    /* ****************************************
     * LabelProvider & ContentProvider
     */
    class ExLabelDicContentProvider extends LabelDicContentProvider {

        @Override
        public Object[] getElements(Object input) {
            if (input instanceof List<?>) {
                return ((List<?>) input).toArray();
            } else if (input instanceof ILabelDic) {
                /* List<ICoronaDic>に対応 */
                List<IDicItem> ret = new ArrayList<IDicItem>(((ILabelDic) input).getItems());
                return check(ret.toArray());
            }
            return super.getElements(input);
        }


        @Override
        public Object[] getChildren(Object parent) {
            if (parent instanceof ILabelDic) {
                return check(((ILabelDic) parent).getItems().toArray());
            }
            Object[] children = super.getChildren(parent);
            return check(children);
        }


        private Object[] check(Object[] children) {
            for (Object child : children) {
                if (child instanceof ILabel) {
                    for (ITerm term : ((ILabel) child).getTerms()) {
                        if (sourceTerms.contains(term)) {
                            Integer count = checkLabels.get(child);
                            if (count == null) {
                                count = 0;
                            }
                            checkLabels.put((ILabel) child, ++count);
                        }
                    }
                }
            }
            return children;
        }


        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof ILabelDic) {
                return ((ILabelDic) element).getItemCount() > 0;
            }
            return super.hasChildren(element);
        }
    }

    static class ExLabelDicLabelProvider extends LabelDicLabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof ILabelDic) {
                return ((ILabelDic) element).getName();
            }
            return super.getText(element);
        }


        @Override
        public Image getImage(Object element) {
            if (element instanceof ILabelDic) {
                return Icons.INSTANCE.get(Icons.IMG_DIC_LABEL);
            }
            return super.getImage(element);
        }
    }
}
