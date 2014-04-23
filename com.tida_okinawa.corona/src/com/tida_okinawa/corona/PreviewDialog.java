/**
 * @version $Id: PreviewDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/18 19:54:11
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.tida_okinawa.corona.common.TreeCheckStateManager;

/**
 * 処理のプレビュー用ダイアログ
 * 変更のあるアイテム一覧を表示するマスター領域と、マスター領域で選択したアイテムの詳細を表示する詳細領域をもつ
 * 
 * @author kousuke-morishima
 */
public class PreviewDialog extends Dialog {
    /**
     * プレビューダイアログ
     * 
     * @param shell
     *            このダイアログの親Shell
     * @param check
     *            アイテム一覧にチェックボックスをつけるか
     */
    public PreviewDialog(Shell shell, boolean check) {
        super(shell);
        this.check = check;
    }

    /* ****************************************
     * UI
     */
    private boolean check;

    TreeCheckStateManager checker = null;
    protected TreeViewer itemViewer = null;

    private LabelProvider treeLabelProvider;

    private Label detailLabel;
    private StyledText detailText = null;
    private LabelProvider detailLabelProvider = null;

    private String message = ""; //$NON-NLS-1$


    @Override
    protected Control createDialogArea(Composite parent) {

        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createMessageArea(root);

        SashForm sash = new SashForm(root, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sash.setLayoutData(layoutData);
        sash.setOrientation(SWT.VERTICAL);
        createListItemArea(sash);
        createDetailArea(sash);
        sash.setWeights(weight);

        return root;
    }


    /* ********************
     * Message
     */
    protected void createMessageArea(Composite parent) {
        Label message = new Label(parent, SWT.WRAP);
        GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
        layoutData.heightHint = 50;
        message.setLayoutData(layoutData);
        message.setText(getMessage());
    }


    /**
     * @return ダイアログのラベルメッセージを返す
     */
    public String getMessage() {
        return message;
    }


    /**
     * @param message
     *            ダイアログのラベルメッセージ
     */
    public void setMessage(String message) {
        this.message = message;
    }


    /* ********************
     * Item List
     */
    protected void createListItemArea(Composite parent) {
        int style = (check) ? SWT.CHECK : SWT.NONE; // MEMO 更新するアイテムを選択できるようにする
        itemViewer = new TreeViewer(parent, style | SWT.SINGLE | SWT.BORDER);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        itemViewer.getTree().setLayoutData(layoutData);
        itemViewer.setContentProvider(treeContentProvider);
        itemViewer.addSelectionChangedListener(listener);
        if (treeLabelProvider != null) {
            itemViewer.setLabelProvider(treeLabelProvider);
        }
        if (input != null) {
            itemViewer.setInput(input);
        }

        checker = TreeCheckStateManager.manageTreeCheckState(itemViewer.getTree());
    }

    private ITreeContentProvider treeContentProvider;


    /**
     * @param provider
     *            変更のあったアイテムのコンテントプロバイダー
     */
    public void setTreeContentProvider(ITreeContentProvider provider) {
        treeContentProvider = provider;
    }


    /**
     * @param labelProvider
     *            変更のあったアイテムのラベルプロバイダー
     */
    public void setTreeLabelProvider(LabelProvider labelProvider) {
        treeLabelProvider = labelProvider;
    }


    /**
     * マスター領域で選択しているアイテムの詳細を表示する
     * 
     * @param parent
     *            親コンポーネント
     */
    protected void createDetailArea(Composite parent) {
        parent = new Composite(parent, SWT.NONE);
        parent.setLayout(new GridLayout(1, false));
        parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        detailLabel = new Label(parent, SWT.NONE);
        detailLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        if (detailLabelText != null) {
            detailLabel.setText(detailLabelText);
        }
        detailText = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        detailText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        detailText.setEditable(false);

        if (detailLabelProvider == null) {
            detailLabelProvider = new LabelProvider();
        }
    }


    /**
     * 上部のツリーで選択されたアイテムの詳細情報を提供するラベルプロバイダーを設定する
     * 
     * @param labelProvider
     *            詳細領域に表示するメッセージのラベルプロバイダー
     */
    public void setDetailLabelProvider(LabelProvider labelProvider) {
        detailLabelProvider = labelProvider;
    }

    private String detailLabelText;


    /**
     * 詳細領域のタイトルを設定する
     * 
     * @param label
     *            must not null
     */
    public void setDetailLabelText(String label) {
        if (detailLabel != null) {
            detailLabel.setText(label);
        } else {
            detailLabelText = label;
        }
    }


    /* ********************
     * buttons
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, Messages.PreviewDialog_labelExecute, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    private Object input;


    /**
     * @param input
     *            アイテム一覧に表示するアイテム
     */
    public void setInput(Object input) {
        this.input = input;
    }

    /* ****************************************
     * 外観
     */
    private int[] weight = new int[] { 2, 4 };


    /**
     * @return 一覧と詳細の表示幅比率
     */
    public int[] getWeight() {
        return weight.clone();
    }


    /**
     * @param weight
     *            一覧と詳細の表示幅比率
     */
    public void setWeight(int[] weight) {
        if (weight != null) {
            this.weight = weight.clone();
        }
    }


    /**
     * @return ダイアログタイトル
     */
    protected String getTitle() {
        return Messages.PreviewDialog_labelPreview;
    }


    /* ****************************************
     */
    /**
     * 一覧領域でチェックされているアイテム。コンストラクタにfalseを渡している場合、すべてのアイテムが返る。
     * 
     * @return 一覧でチェックされたアイテム。
     */
    public Object[] getCheckedItems() {
        TreeItem[] items = checker.getCheckedTreeItems();
        Object[] checkedItems = new Object[items.length];
        int i = 0;
        for (TreeItem item : items) {
            checkedItems[i++] = item.getData();
        }
        return checkedItems;
    }

    /* ****************************************
     * Listener
     */
    private ISelectionChangedListener listener = new ISelectionChangedListener() {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            PreviewDialog.this.selectionChanged(event);
        }
    };


    /**
     * @param event
     *            一覧のアイテムの選択が変わったときに呼ばれる
     */
    protected void selectionChanged(SelectionChangedEvent event) {
        selectionChanged((IStructuredSelection) event.getSelection());
    }


    protected void selectionChanged(IStructuredSelection selection) {
        if (detailLabelProvider instanceof IStyledLabelProvider) {
            StyledString styledString = ((IStyledLabelProvider) detailLabelProvider).getStyledText(selection.getFirstElement());
            detailText.setText(styledString.getString());
            detailText.setStyleRanges(styledString.getStyleRanges());
        } else {
            detailText.setText(detailLabelProvider.getText(selection.getFirstElement()));
        }
    }


    /* ****************************************
     * other
     */
    @Override
    public void create() {
        super.create();
        /* 先頭のアイテムは展開しておく */
        Tree tree = itemViewer.getTree();
        if (tree.getItemCount() > 0) {
            itemViewer.expandToLevel(tree.getItem(0).getData(), TreeViewer.ALL_LEVELS);
        }
    }


    @Override
    protected boolean isResizable() {
        return true;
    }


    @Override
    protected Point getInitialSize() {
        return new Point(500, 500);
    }


    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(getTitle());
        newShell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                checker.dispose();
            }
        });
    }

    /**
     * 行数節約のための抽象クラス.
     * ITreeContentProviderを実装すると、中身空っぽのメソッドを定義しなくてはならず面倒なので作成された抽象クラス。
     * 
     * @author kousuke-morishima
     */
    public static abstract class ContentProvider implements ITreeContentProvider {
        @Override
        public void dispose() {
        }


        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }


        @Override
        public Object[] getElements(Object inputElement) {
            return new Object[0];
        }


        @Override
        public Object[] getChildren(Object parentElement) {
            return new Object[0];
        }


        @Override
        public Object getParent(Object element) {
            return null;
        }


        @Override
        public boolean hasChildren(Object element) {
            return false;
        }
    }
}
