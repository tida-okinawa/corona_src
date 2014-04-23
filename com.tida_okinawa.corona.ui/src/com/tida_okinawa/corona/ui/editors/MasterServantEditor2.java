/**
 * @version $Id: MasterServantEditor2.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/04 16:25:44
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.actions.ActionFactory;

import com.tida_okinawa.corona.ui.Icons;

/**
 * @author kousuke-morishima
 */
public abstract class MasterServantEditor2 extends MasterServantEditor {

    /**
     * @see MasterServantEditor#MasterServantEditor()
     */
    public MasterServantEditor2() {
        super();
    }


    /**
     * @param weights
     * @see MasterServantEditor#MasterServantEditor(int[])
     */
    public MasterServantEditor2(int[] weights) {
        super(weights);
    }


    /**
     * @param weights
     * @param masterStyle
     * @param servantStyle
     * @see MasterServantEditor#MasterServantEditor(int[], int, int)
     */
    public MasterServantEditor2(int[] weights, int masterStyle, int servantStyle) {
        super(weights, masterStyle, servantStyle);
    }


    /* *************************
     * キーアクション
     */
    @Override
    protected void createActions() {
        /* 更新(F5) */
        Action action = new Action() {
            @Override
            public void run() {
                if (isDirty()) {
                    /* 編集されていたら */
                    String message = "辞書は編集されています。\n更新を行うと編集内容は破棄され最終保存時の状態に戻ります。";
                    if (MessageDialog.openConfirm(getSite().getShell(), "辞書の更新", message)) {
                        update();
                        masterViewer.setInput(getMasterInput());
                        if (masterViewer.getTable().getItemCount() > 0) {
                            masterViewer.setSelection(null);
                            IStructuredSelection selection = new StructuredSelection(masterViewer.getTable().getItem(0).getData());
                            masterViewer.setSelection(selection);
                        }
                        dirtyChanged();
                    }
                } else {
                    /* 編集されていなかったら */
                    // TODO ↑の編集されていた時の処理と全く同じであるため、メソッド化予定
                    update();
                    masterViewer.setInput(getMasterInput());
                    if (masterViewer.getTable().getItemCount() > 0) {
                        masterViewer.setSelection(null);
                        IStructuredSelection selection = new StructuredSelection(masterViewer.getTable().getItem(0).getData());
                        masterViewer.setSelection(selection);
                    }

                }
            }
        };
        setAction(ActionFactory.REFRESH.getId(), action);

        /* 削除(Delete) */
        action = new Action() {
            @Override
            public void run() {
                if (getActiveViewer().equals(getMasterViewer())) {
                    removeButtonSelected(MASTER);
                } else {
                    removeButtonSelected(SERVANT);
                }
            }
        };
        setAction(ActionFactory.DELETE.getId(), action);
    }


    @Override
    protected Control createMasterArea(Composite parent) {
        ViewForm form = new ViewForm(parent, SWT.FLAT);
        /* メニューボタンを作る */
        form.setTopLeft(createMenuBar(form, MASTER));
        /* アイテム表示領域 */
        form.setContent(super.createMasterArea(form));
        return form;
    }


    @Override
    protected Control createServantArea(Composite parent) {
        ViewForm form = new ViewForm(parent, SWT.FLAT);
        /* メニューボタンを作る */
        form.setTopLeft(createMenuBar(form, SERVANT));
        /* アイテム表示領域 */
        form.setContent(super.createServantArea(form));
        return form;
    }


    /**
     * 一覧を表示する領域の上に、メニューバーを作成する
     * 
     * @param parent
     * @param masterOrServant
     *            どちらの領域に追加するメニューバーなのかを示す
     * @return
     */
    protected ToolBar createMenuBar(Composite parent, final String masterOrServant) {
        ToolBarManager manager = new ToolBarManager();
        ToolBar bar = manager.createControl(parent);
        ToolItem add = new ToolItem(bar, SWT.PUSH);
        add.setImage(Icons.INSTANCE.get(Icons.IMG_TOOL_ADD));
        add.setToolTipText("追加");
        add.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addButtonSelected(masterOrServant);
            }
        });
        ToolItem delete = new ToolItem(bar, SWT.PUSH);
        delete.setImage(Icons.INSTANCE.get(Icons.IMG_TOOL_DELETE));
        delete.setToolTipText("削除");
        delete.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeButtonSelected(masterOrServant);
            }
        });
        return bar;
    }


    /**
     * dirtyフラグを切り替える
     */
    public void dirtyChanged() {
        firePropertyChange(PROP_DIRTY);
    }


    /**
     * 追加ボタンが押されたときに呼び出される
     * 
     * @param masterOrServant
     *            どちらの領域の追加ボタンが押されたのかを示す
     * @see #MASTER
     * @see #SERVANT
     */
    abstract protected void addButtonSelected(String masterOrServant);


    /**
     * 削除ボタンが押されたときに呼び出される
     * 
     * @param masterOrServant
     *            どちらの領域の追加ボタンが押されたのかを示す
     * @see #MASTER
     * @see #SERVANT
     */
    abstract protected void removeButtonSelected(String masterOrServant);


    /**
     * 辞書データ再取得
     */
    abstract protected void update();


    /**
     * アイテムを削除した後、削除したアイテムのひとつ上のアイテムを選択する
     * 
     * @param v
     */
    protected void setNewSelection(TableViewer v) {
        IStructuredSelection s = null;
        Table t = v.getTable();
        if (t.getSelectionCount() == 1) {
            int index = t.getSelectionIndex();
            if (index > 0) {
                s = new StructuredSelection(t.getItem(index - 1).getData());
            } else if (t.getItemCount() > 1) {
                s = new StructuredSelection(t.getItem(1).getData());
            }
        }
        v.refresh();
        v.setSelection(s);
    }
}
