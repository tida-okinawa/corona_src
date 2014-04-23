/**
 * @version $Id: CategoryOrganizeAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/12 22:58:26
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.PageBook;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.component.LabelCombobox;
import com.tida_okinawa.corona.internal.ui.component.LabelTextbox;
import com.tida_okinawa.corona.internal.ui.component.TreeComponent;
import com.tida_okinawa.corona.internal.ui.component.TreeComponent.DataProvider;
import com.tida_okinawa.corona.internal.ui.util.CategoryNameInputValidator;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.service.IIoService;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.ViewUtil;

/**
 * @author kousuke-morishima
 */
public class CategoryOrganizeAction extends AbstractHandler {
    private CategoryOrganizeDialog dialog;
    private static AtomicBoolean isOpened = new AtomicBoolean(false);


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        /* ダイアログがひとつしか開かないように */
        if (!isOpened.get()) {
            dialog = new CategoryOrganizeDialog(HandlerUtil.getActiveShell(event));
            isOpened.set(true);
        }
        dialog.open();
        if (isOpened.get()) {
            ViewUtil.refreshDatabaseView(0);
        }
        isOpened.set(false);

        return null;
    }

    static class CategoryOrganizeDialog extends Dialog {
        private CategoryNameInputValidator nameValidator;


        protected CategoryOrganizeDialog(Shell shell) {
            super(shell);
            nameValidator = new CategoryNameInputValidator();
        }


        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            ((GridLayout) composite.getLayout()).marginWidth = 2;
            ((GridLayout) composite.getLayout()).horizontalSpacing = 5;

            SashForm sash = CompositeUtil.defaultSashForm(composite, SWT.HORIZONTAL);
            createCategoryList(sash);
            createDetailArea(sash);
            sash.setWeights(new int[] { 1, 1 });
            sash.setBackground(new Color(null, 200, 200, 200));

            tree.setFocus();
            setInput(IoActivator.getService().getCategorys());
            tree.setButtonEnabled(TreeComponent.BUTTON_DELETE, false);

            return composite;
        }

        /* ********************
         * 左ペインのツリー
         */
        private TreeComponent tree;
        private TreeContentProvider contentProvider;


        private void createCategoryList(Composite parent) {
            tree = new TreeComponent(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
            final TreeComponent finalTree = tree;
            tree.addContextMenu(new Action("展開する") {
                @Override
                public void run() {
                    finalTree.getViewer().expandAll();
                }


                @Override
                public ImageDescriptor getImageDescriptor() {
                    return Icons.INSTANCE.getDescriptor(Icons.IMG_TOOL_EXPAND_ALL);
                }
            });

            contentProvider = new TreeContentProvider();
            final TreeContentProvider provider = contentProvider;
            tree.setContentProvider(provider);
            tree.addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    if (selection.size() == 1) {
                        if (selection.getFirstElement() instanceof TextItem) {
                            updatePage(categoryPage);
                        } else if (selection.getFirstElement() instanceof IUserDic) {
                            updatePage(dicPage);
                        }
                    } else if (selection.size() > 1) {
                        Class<?> cls = selection.getFirstElement().getClass();
                        for (Object o : selection.toArray()) {
                            /* 異なる種類のものを選択していたら、詳細ページを表示しない */
                            if (!cls.isInstance(o)) {
                                updatePage(nonePage);
                                return;
                            }
                        }
                        if (TextItem.class.isAssignableFrom(cls)) {
                            /* 分野名はいっぺんに詳細ページを変更させない */
                            updatePage(nonePage);
                        } else if (IUserDic.class.isAssignableFrom(cls)) {
                            updatePage(dicPage);
                        }
                    } else {
                        updatePage(nonePage);
                    }
                }
            });
            tree.setDataProvider(new DataProvider() {
                @Override
                public boolean isRemovable(IStructuredSelection selection) {
                    /* 使われていない分野名のみ削除可能 */
                    for (Object o : selection.toArray()) {
                        if (o instanceof TextItem) {
                            if (provider.getChildren(o).length > 0) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    return true;
                }


                @Override
                public boolean remove(IStructuredSelection selection) {
                    boolean ok = MessageDialog.openConfirm(getShell(), "分野名削除確認", "削除すると元に戻せません。\n\n削除しますか？");
                    if (ok) {
                        IIoService service = IoActivator.getService();
                        for (Object o : selection.toArray()) {
                            service.removeCategory(((TextItem) o).getText());
                            provider.removeChild((TextItem) o);
                            input.remove(o);
                        }
                        setNeedItemUpdate();
                    }
                    return ok;
                }


                @Override
                public Object add(IStructuredSelection selection) {
                    /* 入力文字列で分野を作成 */
                    InputDialog d = new InputDialog(getShell(), "分野名", "", "", nameValidator);
                    if (d.open() == Window.OK) {
                        TextItem newItem = IoActivator.getService().createCategory(d.getValue());
                        if ((newItem != null) && !input.contains(newItem)) {
                            input.add(newItem);
                            setNeedItemUpdate();
                            return newItem;
                        }
                    }
                    return null;
                }


                @Override
                public Object getParent(Object newItem) {
                    return provider.getParent(newItem);
                }
            });
        }

        /* ********************
         * 右ペインの編集領域
         */
        PageBook details;
        Composite categoryPage;
        LabelTextbox catName;
        Composite dicPage;
        LabelCombobox catCombo;
        Composite nonePage;


        private void createDetailArea(Composite parent) {
            details = new PageBook(parent, SWT.NONE);
            GridData detailData = new GridData(SWT.FILL, SWT.FILL, true, true);
            detailData.widthHint = 200;
            details.setLayoutData(detailData);

            categoryPage = CompositeUtil.defaultComposite(details, 1);
            catName = new LabelTextbox(categoryPage, "分野名");
            catName.setLabelWidth(30, 50);
            catName.getTextbox().addFocusListener(categoryNameFocusListener);
            catName.getTextbox().addKeyListener(categoryNameKeyListener);

            dicPage = CompositeUtil.defaultComposite(details, 1);
            catCombo = new LabelCombobox(dicPage, SWT.DROP_DOWN | SWT.READ_ONLY, "分野");
            catCombo.setLabelLayout(new GridData());
            catCombo.setTextLayout(new GridData(SWT.FILL, SWT.NONE, true, false));
            catCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    /* コンボでEnterを押したとき、辞書を反映させる */
                    applyDicCategory();
                }
            });
            catCombo.getTextbox().addFocusListener(categoryComboFocusListener);

            nonePage = CompositeUtil.defaultComposite(details, 1);
            updatePage(nonePage);
        }

        /* 辞書の分野を変更する処理 */
        List<IUserDic> currentDics = new ArrayList<IUserDic>();
        private FocusListener categoryComboFocusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateComboItem();

                /* 処理対象の辞書を保持 */
                currentDics.clear();
                IStructuredSelection selection = getSelection();
                for (Object o : selection.toArray()) {
                    if (o instanceof IUserDic) {
                        currentDics.add((IUserDic) o);
                    }
                }
            }


            @Override
            public void focusLost(FocusEvent e) {
                /* コンボからフォーカスが外れたら、辞書を反映させる */
                applyDicCategory();
            }
        };


        /**
         * 辞書の分野を、コンボで選択したものに変更する
         */
        void applyDicCategory() {
            Set<TextItem> categories = new HashSet<TextItem>();
            TextItem newCategory = (TextItem) catCombo.getSelection();
            if (newCategory != null) {
                /* 辞書の分野変更を確定 */
                for (IUserDic dic : currentDics) {
                    categories.add(dic.getDicCategory());
                    dic.setDicCategory(newCategory);
                    dic.commit(false, new NullProgressMonitor());
                }
                /*
                 * ツリーの更新
                 * 子の位置を移動する。子がいるかどうかの情報を更新する。
                 */
                for (TextItem cat : categories) {
                    contentProvider.removeChild(cat);
                    tree.refresh(cat);
                }
                contentProvider.addChild(newCategory);
                tree.refresh(newCategory);
                /* 移動した辞書を見えるように */
                tree.getViewer().setSelection(new StructuredSelection(currentDics), true);
            }
        }

        /* 分野名編集処理 */
        private FocusListener categoryNameFocusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                /* 編集する分野を保持 */
                IStructuredSelection selection = getSelection();
                if (selection.getFirstElement() instanceof TextItem) {
                    currentCat = (TextItem) selection.getFirstElement();
                } else {
                    currentCat = null;
                }
            }


            @Override
            public void focusLost(FocusEvent e) {
                updateCategoryName();
            }
        };
        private KeyListener categoryNameKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.CR) {
                    e.doit = false;
                    updateCategoryName();
                }
            }
        };

        TextItem currentCat;


        /**
         * 分野名を、入力された名前に更新
         */
        void updateCategoryName() {
            String newName = catName.getText();
            if ((currentCat != null) && !currentCat.getText().equals(newName) && nameValidator.isValid(newName) == null) {
                if (IoActivator.getService().modifyCategory(currentCat.getId(), newName)) {
                    currentCat.setText(catName.getText());
                    setNeedItemUpdate();
                    tree.refresh(currentCat);
                }
            }
        }

        /* 分野名コンボボックス更新処理 */
        boolean needItemUpdate = true;


        void setNeedItemUpdate() {
            needItemUpdate = true;
        }


        /**
         * 分野名コンボボックスのアイテム一覧を更新
         */
        void updateComboItem() {
            if (needItemUpdate) {
                needItemUpdate = false;
                TextItem oldItem = (TextItem) catCombo.getSelection();
                catCombo.setInput(IoActivator.getService().getCategorys());
                if (oldItem != null) {
                    catCombo.setText(oldItem.getText());
                }
            }
        }


        /* ****************************************
         * ツリーでの選択変更に伴う処理
         */
        /**
         * 詳細ページ切り替え処理
         * 
         * @param showPage
         */
        void updatePage(Composite showPage) {
            details.showPage(showPage);
            /* 詳細ページの初期値設定 */
            if (categoryPage.equals(showPage)) {
                IStructuredSelection selection = tree.getSelection();
                String text;
                if (selection.size() == 1) {
                    text = ((TextItem) selection.getFirstElement()).getText();
                } else {
                    text = "";
                }
                catName.setText(text);
            } else if (dicPage.equals(showPage)) {
                updateComboItem();

                IStructuredSelection selection = tree.getSelection();
                String text;
                if (selection.size() == 1) {
                    text = ((IUserDic) selection.getFirstElement()).getDicCategory().getText();
                } else if (selection.size() > 1) {
                    IUserDic dic = (IUserDic) selection.getFirstElement();
                    TextItem cat = dic.getDicCategory();
                    text = cat.getText();
                    for (Object o : selection.toArray()) {
                        /* 異なる種類のものを選択していたら、詳細ページを表示しない */
                        if (!cat.equals(((IUserDic) o).getDicCategory())) {
                            text = "";
                            break;
                        }
                    }
                } else {
                    text = "";
                }

                if ("".equals(text)) {
                    catCombo.clearText();
                } else {
                    catCombo.setText(text);
                }
            }
        }


        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.OK_ID, "閉じる", false);
        }

        /* ****************************************
         */
        List<TextItem> input = null;


        public void setInput(List<TextItem> input) {
            this.input = input;
            if (tree != null) {
                tree.setInput(input);
            }
        }


        /* ****************************************
         */
        IStructuredSelection getSelection() {
            return tree.getSelection();
        }


        /* ****************************************
         * other
         */
        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText("分野名整理");
        }


        @Override
        protected void setShellStyle(int newShellStyle) {
            newShellStyle &= ~SWT.APPLICATION_MODAL;
            super.setShellStyle(newShellStyle | SWT.MODELESS);
        }


        @Override
        protected boolean isResizable() {
            return true;
        }

        /*
         * ContentProvider
         */
        private static class TreeContentProvider implements ITreeContentProvider {
            public TreeContentProvider() {
            }

            private Map<TextItem, Integer> hasChildren = new HashMap<TextItem, Integer>();


            /**
             * parentに子をひとつ追加したことを通知する
             * 
             * @param parent
             */
            public void addChild(TextItem parent) {
                Integer cnt = hasChildren.get(parent);
                if (cnt == null) {
                    cnt = 0;
                }
                cnt++;
                hasChildren.put(parent, cnt);
            }


            /**
             * parentから、子をひとつ削除したことを通知する
             * 
             * @param parent
             */
            public void removeChild(TextItem parent) {
                Integer cnt = hasChildren.get(parent);
                if (cnt == null) {
                    cnt = 1;
                }
                cnt--;
                hasChildren.put(parent, cnt);
            }


            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }


            @Override
            public void dispose() {
            }


            @Override
            public boolean hasChildren(Object element) {
                if (element instanceof TextItem) {
                    Integer cnt = hasChildren.get(element);
                    if (cnt == null) {
                        return true;
                    }
                    return hasChildren.get(element) > 0;
                }
                return false;
            }


            @Override
            public Object getParent(Object element) {
                if (element instanceof IUserDic) {
                    return ((IUserDic) element).getDicCategory();
                }
                return null;
            }


            @Override
            public Object[] getElements(Object input) {
                return ((List<?>) input).toArray();
            }


            @Override
            public Object[] getChildren(Object parent) {
                if (parent instanceof TextItem) {
                    List<Object> ret = new ArrayList<Object>();
                    for (Object o : IoActivator.getService().getDictionarys(IUserDic.class)) {
                        if (parent.equals(((IUserDic) o).getDicCategory())) {
                            ret.add(o);
                        }
                    }
                    hasChildren.put((TextItem) parent, ret.size());
                    return ret.toArray();
                }
                return new Object[0];
            }
        }
    }
}
