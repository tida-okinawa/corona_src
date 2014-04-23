/**
 * @version $Id: CleansingViewerControl.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/06 11:32:25
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaDicPri;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.ui.Icons;

/**
 * @author takayuki-matsumoto
 */
public class CleansingViewerControl {

    CheckboxTableViewer viewer;
    List<ICoronaDicPri> pris;


    /**
     * クレンジング実行ウィザードのコンポジット(コンテナー)設定値を定義
     * 
     * @param parent
     */
    public CleansingViewerControl(Composite parent) {
        createContents(parent);
    }


    /* ****************************************
     * UI
     */
    private Button selectAllButton;
    private Button deselectAllButton;
    private Button upButton;
    private Button downButton;


    private void createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 10;
        gridLayout.marginHeight = 0;
        composite.setLayout(gridLayout);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(layoutData);

        viewer = CheckboxTableViewer.newCheckList(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE);
        viewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getElement() instanceof ICoronaDicPri) {
                    ((ICoronaDicPri) event.getElement()).setInActive(!event.getChecked());
                    fireCheckStateChanged();
                }
            }
        });

        /* カラム設定 */
        final Table tbl = viewer.getTable();
        layoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
        layoutData.heightHint = 200;
        tbl.setLayoutData(layoutData);
        tbl.setHeaderVisible(true);
        CompositeUtil.createColumn(tbl, "辞書名", 300);

        viewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dragSource);
        viewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() }, new DicDropTargetListener(viewer));
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new CleansingLabelProvider());
        viewer.setInput(this.pris);

        Composite buttonGroup = CompositeUtil.defaultComposite(composite, 1);
        ((GridLayout) buttonGroup.getLayout()).makeColumnsEqualWidth = true;
        ((GridData) buttonGroup.getLayoutData()).grabExcessHorizontalSpace = true;

        selectAllButton = CompositeUtil.createBtn(buttonGroup, SWT.PUSH, "全選択(&A)", selectAllButtonListener);
        selectAllButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));

        deselectAllButton = CompositeUtil.createBtn(buttonGroup, SWT.PUSH, "全解除(&D)", deselectAllButtonListener);
        deselectAllButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));

        upButton = CompositeUtil.createBtn(buttonGroup, SWT.PUSH, "優先度を上げる(&U)", selectUpButtonListener);
        upButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));

        downButton = CompositeUtil.createBtn(buttonGroup, SWT.PUSH, "優先度を下げる(&W)", selectDownButtonListener);
        downButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
    }


    /**
     * 共通・個人設定のどちらかが有効でない場合にボタン、ビューを無効化(グレーアウト)
     * 
     * @param enabled
     */
    public void setControlsEnabled(boolean enabled) {
        selectAllButton.setEnabled(enabled);
        deselectAllButton.setEnabled(enabled);
        upButton.setEnabled(enabled);
        downButton.setEnabled(enabled);
        viewer.getTable().setEnabled(enabled);
    }


    /**
     * @param list
     *            must not null
     */
    public void setInput(List<ICoronaDicPri> list) {
        this.pris = list;
        viewer.setInput(list);
        if (list.size() > 0) {
            for (ICoronaDicPri pri : list) {
                viewer.setChecked(pri, !pri.isInActive());
            }
            fireCheckStateChanged();
        }
    }

    private boolean usePriorityFunction = true;


    /**
     * @return 優先度設定機能を使える状態ならtrue
     */
    boolean isUsePriorityFunction() {
        return usePriorityFunction;
    }


    /**
     * 優先度設定機能を使うかどうか
     * 
     * @param use
     */
    public void setUsePriorityFunction(boolean use) {
        upButton.setVisible(use);
        downButton.setVisible(use);
        usePriorityFunction = use;
    }


    /**
     * ウィザードを開く際のフォーカス位置を指定(チェックボックス)
     * 
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /* ****************************************
     * リスナー
     */
    private final SelectionListener selectAllButtonListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            TableItem[] items = viewer.getTable().getItems();
            for (TableItem item : items) {
                /* チェックボックスのチェックを入れる　 */
                item.setChecked(true);
                /* Inactiveフラグにデータを設定　 */
                ICoronaDicPri pri = (ICoronaDicPri) item.getData();
                pri.setInActive(false);
            }
            fireCheckStateChanged();
        }
    };

    private final SelectionListener deselectAllButtonListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            TableItem[] items = viewer.getTable().getItems();
            for (TableItem item : items) {
                /* チェックボックスのチェックを外す　 */
                item.setChecked(false);
                /* Inactiveフラグにデータを設定　 */
                ICoronaDicPri pri = (ICoronaDicPri) item.getData();
                pri.setInActive(true);
            }
            fireCheckStateChanged();
        }
    };

    private final SelectionListener selectUpButtonListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            int index = viewer.getTable().getSelectionIndex();
            ICoronaDicPri dic = (ICoronaDicPri) viewer.getElementAt(index);

            if (index > 0) {
                pris.remove(dic);
                pris.add(index - 1, dic);
                viewer.setInput(pris);
                /* スクロールさせる　 */
                TableItem item = viewer.getTable().getItem(index - 1);
                viewer.getTable().showItem(item);
            }
        }
    };

    private final SelectionListener selectDownButtonListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            Integer index = viewer.getTable().getSelectionIndex();
            ICoronaDicPri dic = (ICoronaDicPri) viewer.getElementAt(index);

            if (index < viewer.getTable().getItemCount() - 1) {
                pris.add(index + 2, dic);
                pris.remove(dic);
                viewer.setInput(pris);
                /* スクロールさせる　 */
                TableItem item = viewer.getTable().getItem(index + 1);
                viewer.getTable().showItem(item);
            }
        }
    };

    /* ****************************************
     * 外部リスナー
     */
    private ListenerList listeners = new ListenerList();


    protected void fireCheckStateChanged() {
        for (Object l : listeners.getListeners()) {
            ((Listener) l).handleEvent(null);
        }
    }


    /**
     * チェックの状態が変わった通知を受け取るリスナーを登録する
     * eventにはnullが渡ってくるので、本当に状態が変わったことしかわからない
     * 
     * @param listener
     */
    public void addCheckStateChangedListener(Listener listener) {
        listeners.add(listener);
    }


    /* ****************************************
     * DnD
     * ドラッグ開始
     * 1. dragStart(Source)
     * 2. validateDrop(Target)
     * ドラッグやめた
     * 3. dragSetData(Source)
     * 4. performDrop(Target)
     * 5. dragFinished(Source)
     */
    private DragSourceListener dragSource = new DragSourceAdapter() {
        @Override
        public void dragStart(DragSourceEvent event) {
            /* 優先度入れ替え機能を使わないならキャンセル */
            event.doit = isUsePriorityFunction();
        }


        @Override
        public void dragSetData(DragSourceEvent event) {
            IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            Object element = selection.getFirstElement();
            if (element instanceof ICoronaDicPri) {
                if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
                    ((LocalSelectionTransfer) ((DragSource) event.getSource()).getTransfer()[0]).setSelection(selection);
                }
            }
        }
    };

    private class DicDropTargetListener extends ViewerDropAdapter {
        public DicDropTargetListener(CheckboxTableViewer viewer) {
            super(viewer);
        }


        @Override
        public boolean validateDrop(Object targetObject, int operation, TransferData transferType) {
            return true;
        }


        @Override
        public boolean performDrop(Object data) {
            ICoronaDicPri srcDic = (ICoronaDicPri) ((IStructuredSelection) data).getFirstElement();
            ICoronaDicPri dstDic = (ICoronaDicPri) getCurrentTarget();

            if (dstDic == null) {
                return true;
            }

            Integer dstIndex = pris.indexOf(dstDic);
            Integer srcIndex = pris.indexOf(srcDic);
            Integer location = getCurrentLocation();
            if (location == LOCATION_BEFORE) {
                if (dstIndex - srcIndex > 0) {
                    pris.add(dstIndex, srcDic);
                    pris.remove(srcDic);
                } else {
                    pris.remove(srcDic);
                    pris.add(dstIndex, srcDic);
                }
            } else if (location == LOCATION_AFTER) {
                if (dstIndex - srcIndex > 0) {
                    pris.add(dstIndex + 1, srcDic);
                    pris.remove(srcDic);
                } else {
                    pris.remove(srcDic);
                    pris.add(dstIndex + 1, srcDic);
                }
            } else if (location == LOCATION_ON) {
                pris.remove(srcDic);
                pris.add(dstIndex, srcDic);
            }

            viewer.setInput(pris);

            return true;
        }
    };

    private static class CleansingLabelProvider extends LabelProvider {

        public CleansingLabelProvider() {
        }


        @Override
        public String getText(Object element) {
            if (element instanceof ICoronaDicPri) {
                int dicId = ((ICoronaDicPri) element).getDicId();
                ICoronaDic dic = IoActivator.getService().getDictionary(dicId);
                if (dic != null) {
                    return dic.getName();
                } else {
                    return "辞書を習得できません";
                }
            }
            return super.getText(element);
        }


        @Override
        public Image getImage(Object element) {
            /* 辞書 */
            if (element instanceof ICoronaDicPri) {
                int dicId = ((ICoronaDicPri) element).getDicId();
                element = IoActivator.getService().getDictionary(dicId);
                /* 一般辞書 */
                if (((ICoronaDic) element).getName().endsWith(".cdic")) {
                    return Icons.INSTANCE.get(Icons.IMG_DIC_COMMON);
                }
                /* 分野辞書 */
                if (((ICoronaDic) element).getName().endsWith(".ddic")) {
                    return Icons.INSTANCE.get(Icons.IMG_DIC_CATEGORY);
                }
                /* 固有辞書 */
                if (((ICoronaDic) element).getName().endsWith(".dic")) {
                    return Icons.INSTANCE.get(Icons.IMG_DIC_SPECIAL);
                }
                /* ゆらぎ辞書 */
                if (((ICoronaDic) element).getName().endsWith(".fdic")) {
                    return Icons.INSTANCE.get(Icons.IMG_DIC_FLUC);
                }
                /* ラベル辞書 */
                if (((ICoronaDic) element).getName().endsWith(".ldic")) {
                    return Icons.INSTANCE.get(Icons.IMG_DIC_LABEL);
                }
                /* 構文パターン辞書 */
                if (((ICoronaDic) element).getName().endsWith(".pdic")) {
                    return Icons.INSTANCE.get(Icons.IMG_DIC_PATTERN);
                }
                /* 同義語辞書 */
                if (((ICoronaDic) element).getName().endsWith(".sdic")) {
                    return Icons.INSTANCE.get(Icons.IMG_DIC_SYNONYM);
                }
                /* JUMAN辞書 */
                if (((ICoronaDic) element).getName().endsWith(".jdic")) {
                    return Icons.INSTANCE.get(Icons.IMG_DIC_JUMAN);
                }
            }
            return super.getImage(element);
        }
    }
}
