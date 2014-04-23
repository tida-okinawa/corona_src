/**
 * @version $Id: TreeComponent.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/13 9:51:08
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.component;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * @author kousuke-morishima
 */
public class TreeComponent extends AbstractWidget {


    public TreeComponent(Composite parent, int style) {
        createContents(parent, style);
    }

    private TreeViewer viewer;
    private Button addButton;
    private Button removeButton;


    private void createContents(Composite parent, int style) {
        Composite composite = CompositeUtil.defaultComposite(parent, 2);
        /* Tree領域 */
        Tree tree = new Tree(composite, style);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer = new TreeViewer(tree);

        GridData viewerData = new GridData(SWT.FILL, SWT.FILL, true, true);
        viewerData.widthHint = 200;
        viewerData.heightHint = 300;
        viewer.getTree().setLayoutData(viewerData);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (provider != null) {
                    if (provider.isRemovable((IStructuredSelection) event.getSelection())) {
                        setButtonEnabled(BUTTON_DELETE, true);
                    } else {
                        setButtonEnabled(BUTTON_DELETE, false);
                    }
                }
            }
        });

        createButtonArea(composite);

        hookContextMenu();
    }


    /* ********************
     * Button
     */
    protected void createButtonArea(Composite composite) {
        /* Button領域 */
        Composite btnClient = CompositeUtil.defaultComposite(composite, 1);
        btnClient.setLayout(new GridLayout());
        GridData layoutData = new GridData();
        layoutData.verticalAlignment = SWT.TOP;
        btnClient.setLayoutData(layoutData);
        final TreeViewer finalViewer = viewer;
        addButton = CompositeUtil.createBtn(btnClient, SWT.PUSH, "追加", new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (provider != null) {
                    IStructuredSelection selection = (IStructuredSelection) finalViewer.getSelection();
                    Object newItem = provider.add(selection);
                    if (newItem != null) {
                        Object parent = provider.getParent(newItem);
                        if (parent != null) {
                            finalViewer.add(parent, newItem);
                        } else {
                            refresh(null);
                        }
                    }
                }
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        removeButton = CompositeUtil.createBtn(btnClient, SWT.PUSH, "削除", new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (provider != null) {
                    IStructuredSelection selection = (IStructuredSelection) finalViewer.getSelection();
                    if (provider.isRemovable(selection)) {
                        if (provider.remove(selection)) {
                            for (Object o : selection.toArray()) {
                                finalViewer.remove(o);
                            }
                        }
                    }
                }
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }

    public static final int BUTTON_ADD = 0;
    public static final int BUTTON_DELETE = 1;


    /**
     * @param buttonId
     * @param enabled
     * @see #BUTTON_ADD
     * @see #BUTTON_DELETE
     */
    public void setButtonEnabled(int buttonId, boolean enabled) {
        switch (buttonId) {
        case BUTTON_ADD:
            addButton.setEnabled(enabled);
            break;
        case BUTTON_DELETE:
            removeButton.setEnabled(enabled);
            break;
        default:
            break;
        }
    }


    /* ****************************************
     */
    public TreeViewer getViewer() {
        return viewer;
    }


    /* ****************************************
     */
    /**
     * ツリーにContentProviderをセットする
     * 
     * @param provider
     */
    public void setContentProvider(ITreeContentProvider provider) {
        viewer.setContentProvider(provider);
    }


    /**
     * ツリーにLabelProviderをセットする
     * 
     * @param labelProvider
     */
    public void setLabelProvider(LabelProvider labelProvider) {
        viewer.setLabelProvider(labelProvider);
    }


    /**
     * 表示データをセットする
     * 
     * @param input
     */
    public void setInput(Object input) {
        if (input != null) {
            viewer.setInput(input);
        }
    }

    DataProvider provider;


    /**
     * アイテムを追加、削除するときに使うProviderをセットする
     * 
     * @param provider
     */
    public void setDataProvider(DataProvider provider) {
        this.provider = provider;
    }


    /**
     * 指定したアイテム以下をリフレッシュする。nullならツリー全体をリフレッシュする。
     * 
     * @param element
     */
    public void refresh(Object element) {
        if (element == null) {
            viewer.refresh();
        } else {
            viewer.refresh(element);
        }
    }


    /**
     * @return 現在ツリーで選択しているデータ
     */
    public IStructuredSelection getSelection() {
        return (TreeSelection) viewer.getSelection();
    }


    /* ****************************************
     * Listener
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        viewer.addSelectionChangedListener(listener);
    }


    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        viewer.removeSelectionChangedListener(listener);
    }

    /* ****************************************
     * Context Menu
     */
    private List<IAction> menus = new ArrayList<IAction>();


    protected void hookContextMenu() {
        final List<IAction> finalMenus = menus;
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                for (IAction a : finalMenus) {
                    manager.add(a);
                }
            }
        });

        viewer.getTree().setMenu(manager.createContextMenu(viewer.getTree()));
    }


    public void addContextMenu(IAction action) {
        menus.add(action);
    }


    /* ****************************************
     * Other
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }


    @Override
    public void dispose() {
    }


    @Override
    protected int handling() {
        return 0;
    }

    /**
     * ツリーアイテム操作を補助するクラス
     * 
     * @author kousuke-morishima
     * 
     */
    public static class DataProvider {
        public DataProvider() {
        }


        /**
         * 削除処理
         * 
         * @param selection
         * @return 削除に成功したらtrue
         */
        public boolean remove(IStructuredSelection selection) {
            return false;
        }


        /**
         * 削除できるか
         * 
         * @param selection
         * @return
         */
        public boolean isRemovable(IStructuredSelection selection) {
            return false;
        }


        /**
         * @param selection
         * @return 追加したアイテム
         */
        public Object add(IStructuredSelection selection) {
            return null;
        }


        /**
         * newItemの親を取得する。
         * 
         * @param newItem
         * @return
         */
        public Object getParent(Object newItem) {
            return null;
        }
    }
}
