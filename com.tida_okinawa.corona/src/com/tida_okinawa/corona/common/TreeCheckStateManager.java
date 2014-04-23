/**
 * @version $Id: TreeCheckStateManager.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/21 19:51:00
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * TreeItemのチェック状態を管理する。親にチェックしたら子もチェックされるようになる。
 * 
 * TODO フィルタされるTreeに対してこのクラスを使用できるようにする
 * TreeItemを保持しているが、フィルタされるとTreeItemがdisposeされてうまく動かない
 * 
 * @author kousuke-morishima
 */
public class TreeCheckStateManager {

    /**
     * 指定されたツリーのチェック状態を管理する
     * 
     * @param tree
     *            管理するツリー
     * @return treeの管理クラス
     */
    public static TreeCheckStateManager manageTreeCheckState(Tree tree) {
        return new TreeCheckStateManager(tree);
    }

    private Tree tree;


    private TreeCheckStateManager(Tree tree) {
        this.tree = tree;

        tree.addSelectionListener(selectionListener);
        tree.addTreeListener(treeListener);

        setParentCheck(true);
        setChildrenCheck(true);
    }

    private boolean isParentCheck;


    /**
     * 子のチェック状態が変更されたとき、親のチェック状態に変更があるか確認し、必要ならば親のチェック状態を更新するかどうか。
     * 
     * @return 親のチェック状態も一緒に変更するなら、true
     */
    public boolean isParentCheck() {
        return isParentCheck;
    }


    /**
     * 子のチェック状態が変更されたとき、親のチェック状態に変更があるか確認し、必要ならば親のチェック状態を更新するかどうか。
     * 
     * @param enabled
     *            親のチェック状態も一緒に変更するなら、true
     */
    public void setParentCheck(boolean enabled) {
        isParentCheck = enabled;
    }

    private boolean isChildrenCheck;


    /**
     * 親のチェック状態が変更されたとき、子のチェック状態に変更があるか確認し、必要ならば子のチェック状態を更新するかどうか。
     * 
     * @return 子のチェック状態も一緒に変更するなら、true
     */
    public boolean isChildrenCheck() {
        return isChildrenCheck;
    }


    /**
     * 親のチェック状態が変更されたとき、子のチェック状態に変更があるか確認し、必要ならば子のチェック状態を更新するかどうか。
     * 
     * @param enabled
     *            子のチェック状態も一緒に変更するなら、true
     */
    public void setChildrenCheck(boolean enabled) {
        isChildrenCheck = enabled;
    }

    /* ****************************************
     * Listeners
     */
    private SelectionAdapter selectionListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.detail == SWT.CHECK) {
                TreeItem item = (TreeItem) e.item;
                parentCheck(item);
                childCheck(item);
            }
        }
    };
    private TreeAdapter treeListener = new TreeAdapter() {
        private final Object PRESENT = new Object();
        /**
         * すでに子が展開されたアイテムの管理
         */
        Map<TreeItem, Object> populate = new HashMap<TreeItem, Object>();


        @Override
        public void treeExpanded(TreeEvent e) {
            TreeItem item = (TreeItem) e.item;
            if (!populate.containsKey(item)) {
                populate.put(item, PRESENT);
                childCheck(item);
            }
        }
    };


    /**
     * 子のチェック状態を変更する。子がまだ展開されていないときは何もしない
     * 
     * @param item
     *            チェックされたアイテム
     */
    void childCheck(TreeItem item) {
        if (!isChildrenCheck) {
            return;
        }
        boolean checked = item.getChecked();
        item.setGrayed(false);
        for (TreeItem child : item.getItems()) {
            child.setChecked(checked);
            child.setGrayed(false);
        }
    }


    /**
     * 親のチェック状態を変更する
     * 
     * @param item
     *            チェックされたアイテム
     */
    void parentCheck(TreeItem item) {
        if (!isParentCheck) {
            return;
        }
        boolean checked = item.getChecked();
        if (checked) {
            // onになった
            TreeItem parent = item.getParentItem();
            while (parent != null) {
                // 子が全部チェックだったらチェック
                int parentState = TreeCheckStateManager.CHECK;
                for (TreeItem ti : parent.getItems()) {
                    if (!ti.getChecked()) {
                        parentState = TreeCheckStateManager.CHECK_GRAY;
                        break;
                    }
                }
                check(parent, parentState);
                parent = parent.getParentItem();
            }
        } else {
            TreeItem parent = item.getParentItem();
            while (parent != null) {
                // 子が全部白だったら白
                int parentState = TreeCheckStateManager.CHECK_WHITE;
                for (TreeItem ti : parent.getItems()) {
                    if (ti.getChecked()) {
                        parentState = TreeCheckStateManager.CHECK_GRAY;
                        break;
                    }
                }
                check(parent, parentState);
                parent = parent.getParentItem();
            }
        }
    }

    private static final int CHECK_WHITE = 0;
    private static final int CHECK_GRAY = 1;
    private static final int CHECK = 2;


    /**
     * @param item
     *            チェック状態を変更するアイテム
     * @param state
     *            {@link #CHECK_WHITE} or {@link #CHECK_GRAY} or {@link #CHECK}
     */
    private static void check(TreeItem item, int state) {
        switch (state) {
        case CHECK_WHITE:
            item.setChecked(false);
            item.setGrayed(false);
            break;
        case CHECK_GRAY:
            item.setChecked(true);
            item.setGrayed(true);
            break;
        case CHECK:
            item.setChecked(true);
            item.setGrayed(false);
            break;
        default:
            throw new IllegalArgumentException("stateの値が不正です[" + state + "]");
        }
    }


    /* ****************************************
     * getter
     */
    /* TODO 高速化の余地あり。チェックしたときに記憶。populateされていないアイテムを探して探査 */
    /**
     * @return チェックしたアイテムの一覧
     */
    public TreeItem[] getCheckedTreeItems() {
        List<TreeItem> ret = new ArrayList<TreeItem>(tree.getItems().length * 2);
        for (TreeItem item : tree.getItems()) {
            ret.addAll(getCheckedTreeItems(item));
        }
        return ret.toArray(new TreeItem[ret.size()]);
    }


    private List<TreeItem> getCheckedTreeItems(TreeItem parent) {
        List<TreeItem> ret = new ArrayList<TreeItem>(parent.getItems().length * 2);
        ret.add(parent);
        for (TreeItem item : parent.getItems()) {
            ret.addAll(getCheckedTreeItems(item));
        }
        return ret;
    }


    /**
     * コンポーネントの後処理。使用するダイアログやコンポーネントのdisposeメソッド内で呼び出すこと。
     */
    public void dispose() {
        if (tree.isDisposed()) {
            return;
        }
        tree.removeSelectionListener(selectionListener);
        tree.removeTreeListener(treeListener);
        tree.dispose();
    }

}
