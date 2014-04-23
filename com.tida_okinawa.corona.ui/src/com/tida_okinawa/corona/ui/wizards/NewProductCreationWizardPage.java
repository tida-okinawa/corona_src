/**
 * @version $Id: NewProductCreationWizardPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03 15:17:48
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.StringUtil;
import com.tida_okinawa.corona.internal.ui.actions.CoronaElementDeleteOperation;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.component.ElementListSelectionDialog1;
import com.tida_okinawa.corona.internal.ui.component.TextWithBrowseButtonGroup;
import com.tida_okinawa.corona.internal.ui.util.CollectionUtil;
import com.tida_okinawa.corona.internal.ui.views.model.IUIClaim;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author kousuke-morishima
 */
public class NewProductCreationWizardPage extends WizardPageBase {
    protected NewProductCreationWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName);

        setTitle(Messages.NewProductCreationWizardPage_wizardTitleEntryProduct);
        setDescription(Messages.NewProductCreationWizardPage_wizardPageDescription);

        Object item = selection.getFirstElement();
        IProject project = null;
        if (item instanceof IUIElement) {
            project = ((IUIElement) item).getResource().getProject();
        } else if (item instanceof IResource) {
            project = ((IResource) item).getProject();
        }
        setSelectedProject(project);
    }


    /* ****************************************
     * インタフェース
     */
    /**
     * ターゲットアイテムを作って返す
     * 
     * @return
     */
    public List<IUIProduct> createProducts() {
        /* 入力内容を更新 */
        getSelectedProductNames(true);
        getSelectedFields(true);

        IUIProject uiProject = getSelectedUIProject();
        if (uiProject == null) {
            return new ArrayList<IUIProduct>(0);
        }
        final ICoronaProject coronaProject = uiProject.getObject();
        if (coronaProject == null) {
            return new ArrayList<IUIProduct>(0);
        }
        final IClaimData currentClaim = getSelectedClaimData();

        /* 新規問い合わせデータなら追加する */
        boolean isNewClaim = !includeProject(currentClaim);
        if (isNewClaim) {
            coronaProject.addClaimData(currentClaim);
            IUIClaim uiClaim = CoronaModel.INSTANCE.getClaim(uiProject, currentClaim);
            uiClaim.update(null);
        }

        /*
         * 既存ターゲットのマイニングフィールドを変更した場合、更新する
         */
        int claimId = currentClaim.getId();
        for (IUIProduct uiProduct : uiProject.getProducts()) {
            updateMiningFields(uiProduct, claimId);
        }

        List<IUIProduct> products = new ArrayList<IUIProduct>(); /* 新規に作ったターゲットを入れておく */
        for (String name : selectedProductNames) {
            /* 既存ターゲットであれば、何もしなくていい */
            if (!isNewClaim && includeProject(name)) {
                continue;
            }

            /* IFolderを作る(handleを取得する) */
            IFolder productFolder = selectedProject.getFolder(StringUtil.convertValidFileName(name));

            /* ICoronaProductを作る(既存ターゲットであれば、キャッシュが返ってくる) */
            IUIProduct uiProduct = (IUIProduct) CoronaModel.INSTANCE.create(productFolder, name);
            ICoronaProduct product = uiProduct.getObject();
            if (product == null) {
                CoronaActivator.log(new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, Messages.NewProductCreationWizardPage_errorProduct + name
                        + Messages.NewProductCreationWizardPage_errorCreate, new NullPointerException(Messages.NewProductCreationWizardPage_error)), false);
                continue;
            }

            products.add(uiProduct);

            // 新規ターゲットなので、追加するだけ
            for (IFieldHeader field : selectedFields) {
                product.addMiningField(claimId, field.getId());
            }

            /* ターゲット(IFolder)を作る */
            uiProduct.update(null);
            /* 子フォルダを作る */
            for (IUIElement child : uiProduct.getChildren()) {
                child.update(null);
            }
            /* ターゲットに紐づいた辞書、処理結果があれば作る */
            List<ICoronaDic> dics = product.getDictionarys(ICoronaDic.class);
            for (ICoronaDic dic : dics) {
                IUIDictionary uiDic = CoronaModel.INSTANCE.getDic(uiProduct, dic);
                uiDic.update(null);
            }
            Set<IClaimWorkData> works = product.getClaimWorkDatas();
            for (IClaimWorkData work : works) {
                IUIWork uiWork = CoronaModel.INSTANCE.getWork(uiProduct, work);
                uiWork.update(null);
            }
        }

        return products;
    }


    /**
     * uiProductが現在選択している問合せデータに関連するターゲットだったら、マイニングフィールドを更新する。
     * 
     * @param uiProduct
     *            検査対象のターゲット
     * @param claimId
     *            今回選択した問合せデータのID
     */
    private void updateMiningFields(IUIProduct uiProduct, int claimId) {
        ICoronaProduct product = uiProduct.getObject();
        if (product == null) {
            return;
        }

        Collection<Integer> currentMiningFields = product.getMiningFields(claimId);
        /*
         * 関連する問合せデータに対しては、必ずマイニングフィールドがひとつ以上存在するので
         * 空なら関係ない問合せデータと判断できる
         */
        if (currentMiningFields.isEmpty()) {
            return;
        }

        /*
         * addMiningField, removeMiningFieldはいちいちDBに書き込みにいくので、
         * 追加分、削除分を先に抽出
         */
        Collection<Integer> newFields = new ArrayList<Integer>(selectedFields.size());
        for (IFieldHeader fieldHeader : selectedFields) {
            newFields.add(fieldHeader.getId());
        }
        Collection<Integer> added = new ArrayList<Integer>();
        Collection<Integer> deleted = new ArrayList<Integer>();
        CollectionUtil.diff(currentMiningFields, newFields, added, deleted);

        /* 追加されたフィールドをマイニング対象に設定 */
        for (Integer fieldId : added) {
            product.addMiningField(claimId, fieldId);
            IClaimWorkData work = product.getClaimWorkData(claimId, ClaimWorkDataType.CORRECTION_MISTAKES, fieldId);
            CoronaModel.INSTANCE.getWork(uiProduct, work).update(null);
        }

        /* 削除されたフィールドをマイニング対象から削除。 */
        if (deleted.size() > 0) {
            List<IUIWork> deleteWorks = new ArrayList<IUIWork>();
            for (Integer fieldId : deleted) {
                product.removeMiningFeild(claimId, fieldId);
                for (Iterator<IClaimWorkData> itr = product.getClaimWorkDatas().iterator(); itr.hasNext();) {
                    IClaimWorkData work = itr.next();
                    if (work.getFieldId() == fieldId && work.getClaimId() == claimId) {
                        deleteWorks.add(CoronaModel.INSTANCE.getWork(uiProduct, work));
                    }
                }
            }
            try {
                IUIElement[] deleteObjects = deleteWorks.toArray(new IUIElement[deleteWorks.size()]);
                CoronaElementDeleteOperation op = new CoronaElementDeleteOperation(deleteObjects, ""); //$NON-NLS-1$
                op.execute(null, null);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /* ****************************************
     * UI構築
     */
    private boolean isUiCreated;


    @Override
    public void createControl(Composite parent) {
        Composite composite = CompositeUtil.defaultComposite(parent, 1);
        Composite projectGroup = CompositeUtil.defaultComposite(composite, 1);
        ((GridData) projectGroup.getLayoutData()).grabExcessVerticalSpace = false;
        createProjectGroup(projectGroup);

        SashForm productGroup = CompositeUtil.defaultSashForm(composite, SWT.HORIZONTAL);
        ((GridData) productGroup.getLayoutData()).heightHint = 350;

        createClaimList(productGroup);
        SashForm subComposite = CompositeUtil.defaultSashForm(productGroup, SWT.VERTICAL);
        createProductList(subComposite);
        createFieldList(subComposite);
        productGroup.setWeights(new int[] { 1, 1 });
        productGroup.setSashWidth(5);
        subComposite.setWeights(new int[] { 1, 1 });
        subComposite.setSashWidth(5);

        setControl(composite);
        isUiCreated = true;

        claimListViewer.getTable().select(0);
        claimListViewer.getControl().setFocus();
        setClaimDatas(getSelectedProject());
        claimListViewer.getTable().setSelection(0);
        updateList();

        setPageComplete(fieldValidate());
    }


    /* ********************
     * Project
     */
    private TextWithBrowseButtonGroup projectGroup;


    private void createProjectGroup(Composite parent) {
        projectGroup = new TextWithBrowseButtonGroup(parent, Messages.NewProductCreationWizardPage_groupProjectToCreate, "..."); //$NON-NLS-1$
        GridData layoutData = new GridData(SWT.NONE, SWT.NONE, false, false);
        layoutData.widthHint = 500;
        projectGroup.setTextLayout(layoutData);
        projectGroup.setEditable(false);
        projectGroup.addButtonSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* 表示するプロジェクトをCoronaProjectに限定 */
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                List<IProject> input = new ArrayList<IProject>(Arrays.asList(projects));
                for (Iterator<IProject> itr = input.iterator(); itr.hasNext();) {
                    IProject project = itr.next();
                    if (!CoronaModel.INSTANCE.isCoronaProject(project)) {
                        itr.remove();
                    }
                }

                ElementListSelectionDialog1 dialog = new ElementListSelectionDialog1(getShell(), new LabelProvider() {
                    @Override
                    public Image getImage(Object element) {
                        return Icons.INSTANCE.get(Icons.IMG_PROJECT);
                    }


                    @Override
                    public String getText(Object element) {
                        return ((IProject) element).getName();
                    }
                });
                dialog.setTitle(Messages.NewProductCreationWizardPage_messageSelectProject);
                dialog.setMessage(Messages.NewProductCreationWizardPage_messageSelectProject);
                dialog.setElements(input.toArray());
                dialog.setInitialSelections(new Object[] { getSelectedProject() });
                if (dialog.open() == Dialog.OK) {
                    Object o = dialog.getFirstResult();
                    if (o instanceof IProject) {
                        setSelectedProject((IProject) o);
                        setClaimDatas((IProject) o);
                    } else {
                        setSelectedProject(null);
                        setClaimDatas(null);
                    }

                    setPageComplete(fieldValidate());
                }
            }
        });
        setSelectedProjectText(getSelectedProject());
    }

    private IProject selectedProject;


    /**
     * @return may be null
     */
    public IProject getSelectedProject() {
        return selectedProject;
    }


    void setSelectedProject(IProject project) {
        if (selectedProject == null) {
            if (project != null) {
                selectedProject = project;
                setSelectedProjectText(project);
                updateList();
            }
        } else {
            if (!selectedProject.equals(project)) {
                selectedProject = project;
                setSelectedProjectText(project);
                updateList();
            }
        }
    }


    private void setSelectedProjectText(IProject project) {
        if (projectGroup != null) {
            if (project == null) {
                projectGroup.clearText();
            } else {
                projectGroup.setText("/" + project.getName()); //$NON-NLS-1$
            }
        }
    }


    /**
     * @return may be null
     */
    private IUIProject getSelectedUIProject() {
        return (IUIProject) CoronaModel.INSTANCE.adapter(selectedProject, false);
    }


    /* ********************
     * Claim
     */
    private TableViewer claimListViewer;


    private void createClaimList(Composite parent) {
        /* 問い合わせデータの一覧を表示する */
        Composite composite = CompositeUtil.defaultComposite(parent, 1);
        Label l = new Label(composite, SWT.NONE);
        l.setText(Messages.NewProductCreationWizardPage_labelDataFile);

        claimListViewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
        claimListViewer.setContentProvider(new ArrayContentProvider());
        claimListViewer.setLabelProvider(new ColorLabelProvider());
        if (initialClaimDatas != null) {
            claimListViewer.setInput(initialClaimDatas);
        }

        Table t = claimListViewer.getTable();
        t.setLayout(new GridLayout());
        t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        claimListViewer.addSelectionChangedListener(tableItemSelectionChangedListener);
    }

    private List<IClaimData> initialClaimDatas;


    /**
     * @param project
     *            may be null
     */
    void setClaimDatas(IProject project) {
        /* TODO setSelectedProjectと統合したい */
        /*
         * 問い合わせデータの一覧を表示する
         */
        List<IClaimData> ret = null;

        ret = IoActivator.getService().getClaimDatas();
        Collections.sort(ret, new Comparator<IClaimData>() {
            @Override
            public int compare(IClaimData o1, IClaimData o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        if (claimListViewer != null) {
            claimListViewer.setInput(ret);
        } else {
            initialClaimDatas = ret;
        }
    }


    /**
     * @return 選択している問い合わせデータ
     */
    public IClaimData getSelectedClaimData() {
        if ((claimListViewer != null) && (claimListViewer.getSelection() != null)) {
            return (IClaimData) ((IStructuredSelection) claimListViewer.getSelection()).getFirstElement();
        }

        return null;
    }

    /* ********************
     * Product
     */
    TableViewer productList;
    Button productDeselectButton;


    private void createProductList(Composite parent) {
        /* 選択した問い合わせデータにあるターゲット一覧を表示する */
        Composite composite = CompositeUtil.defaultComposite(parent, 2);
        Composite tableGroup = CompositeUtil.defaultComposite(composite, 1);
        Label l = new Label(tableGroup, SWT.NONE);
        l.setText(Messages.NewProductCreationWizardPage_labelProductName);

        productList = new TableViewer(tableGroup, SWT.CHECK | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
        productList.setContentProvider(new ArrayContentProvider());
        productList.setLabelProvider(new ColorLabelProvider());

        Table t = productList.getTable();
        t.setLayout(new GridLayout());
        t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        t.addSelectionListener(productSelection);
        t.addSelectionListener(callValidate);

        Composite buttonGroup = CompositeUtil.defaultComposite(composite, 1);
        ((GridLayout) buttonGroup.getLayout()).marginTop = l.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        GridData layoutData = (GridData) buttonGroup.getLayoutData();
        layoutData.grabExcessHorizontalSpace = false;
        layoutData.minimumWidth = 100;
        /* Button selectAllButton = */CompositeUtil.createBtn(buttonGroup, SWT.PUSH, Messages.NewProductCreationWizardPage_buttonAllEnable,
                selectAllButtonListener);
        productDeselectButton = CompositeUtil.createBtn(buttonGroup, SWT.PUSH, Messages.NewProductCreationWizardPage_buttonAllDisable,
                deselectAllButtonListener);
    }

    private List<String> selectedProductNames;


    public List<String> getSelectedProductNames(boolean force) {
        if (force || selectedProductNames == null) {
            selectedProductNames = new ArrayList<String>();

            if (productList != null) {
                TableItem[] items = productList.getTable().getItems();
                for (TableItem item : items) {
                    if (item.getChecked()) {
                        selectedProductNames.add(((String) item.getData()).trim());
                    }
                }
            }
        }
        return selectedProductNames;
    }

    /* ********************
     * Mining Field
     */
    TableViewer fieldList;
    Button fieldDeselectButton;


    private void createFieldList(Composite parent) {
        /* 選択した問い合わせデータのカラム一覧を表示する */
        Composite composite = CompositeUtil.defaultComposite(parent, 2);
        Composite fieldGroup = CompositeUtil.defaultComposite(composite, 1);
        Label l = new Label(fieldGroup, SWT.NONE);
        l.setText(Messages.NewProductCreationWizardPage_labelMiningField);
        fieldList = new TableViewer(fieldGroup, SWT.CHECK | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
        fieldList.setContentProvider(new ArrayContentProvider());
        fieldList.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof IFieldHeader) {
                    return ((IFieldHeader) element).getDispName();
                }
                return super.getText(element);
            }
        });

        Table t = fieldList.getTable();
        t.setLayout(new GridLayout());
        t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        t.addSelectionListener(fieldSelection);
        t.addSelectionListener(callValidate);

        Composite buttonGroup = CompositeUtil.defaultComposite(composite, 1);
        ((GridLayout) buttonGroup.getLayout()).marginTop = l.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        GridData layoutData = (GridData) buttonGroup.getLayoutData();
        layoutData.grabExcessHorizontalSpace = false;
        layoutData.minimumWidth = 100;
        fieldDeselectButton = CompositeUtil.createBtn(buttonGroup, SWT.PUSH, Messages.NewProductCreationWizardPage_buttonAllDisable, deselectAllButtonListener);
    }

    private List<IFieldHeader> selectedFields;


    /**
     * @param force
     * @return チェックを入れたフィールド
     */
    public List<IFieldHeader> getSelectedFields(boolean force) {
        if (selectedFields == null || force) {
            selectedFields = new ArrayList<IFieldHeader>();

            if (fieldList != null) {
                TableItem[] items = fieldList.getTable().getItems();
                for (TableItem item : items) {
                    if (item.getChecked()) {
                        selectedFields.add((IFieldHeader) item.getData());
                    }
                }
            }
        }

        return selectedFields;
    }


    /* ****************************************
     * ページチェック
     */
    boolean fieldValidate() {
        boolean valid = true;
        if (getSelectedProject() == null) {
            if (valid) {
                setErrorMessage(Messages.NewProductCreationWizardPage_errorMessageSelectProject);
            }
            valid = false;
        }

        if (getSelectedClaimData() == null) {
            if (valid) {
                setErrorMessage(Messages.NewProductCreationWizardPage_errorMessageSelectDataFile);
            }
            valid = false;
        }

        if (getSelectedProductNames(true).size() == 0) {
            if (valid) {
                setErrorMessage(Messages.NewProductCreationWizardPage_errorMessageSelectProduct);
            }
            valid = false;
        }

        if (getSelectedFields(true).size() == 0) {
            if (valid) {
                setErrorMessage(Messages.NewProductCreationWizardPage_errorMessageSelectField);
            }
            valid = false;
        }

        if (valid) {
            setErrorMessage(null);
        }
        return valid;
    }

    /* ****************************************
     * Listeners
     */
    private final ISelectionChangedListener tableItemSelectionChangedListener = new ISelectionChangedListener() {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            updateList();
            setPageComplete(fieldValidate());
        }
    };

    private static final Object[] EMPTY_ARRAY = new Object[0];


    void updateList() {
        if (!isUiCreated) {
            return;
        }
        IStructuredSelection selection = (IStructuredSelection) claimListViewer.getSelection();
        IClaimData claim = (IClaimData) selection.getFirstElement();
        if (claim != null) {
            /* ターゲット一覧 */
            /* Memo IClaimData#getProductsが遅い。10万件にSelect Group Byが遅い。遅いけどふつう？ */
            productList.setInput(claim.getProducts());
            /* マイニングフィールド一覧(誤記補正したフィールドだけ表示する) */
            List<IFieldHeader> rest = new ArrayList<IFieldHeader>();
            List<IFieldHeader> fields = claim.getFieldInformations();
            List<Integer> fieldIds = new ArrayList<Integer>(claim.getCorrectionMistakesFields());
            for (IFieldHeader field : fields) {
                for (Iterator<Integer> itr = fieldIds.iterator(); itr.hasNext();) {
                    Integer id = itr.next();
                    if (field.getId() == id) {
                        itr.remove();
                        rest.add(field);
                        break;
                    }
                }
            }
            fieldList.setInput(rest);

            /* チェックボックスの状態を初期化 */
            IUIProject uiProject = getSelectedUIProject();
            if (uiProject != null) {
                ICoronaProject project = getSelectedUIProject().getObject();

                /* ターゲットの初期選択 */
                TableItem[] pItems = productList.getTable().getItems();
                for (TableItem item : pItems) {
                    item.setChecked(false);
                    if (includeProject((String) item.getData())) {
                        item.setChecked(true);
                    }
                }

                /* マイニングフィールドの初期選択 */
                TableItem[] fItems = fieldList.getTable().getItems();
                if (project.getProducts().size() > 0) {
                    Set<TableItem> checkItems = new HashSet<TableItem>(fItems.length * 4 / 3);
                    for (ICoronaProduct product : project.getProducts()) {
                        Set<Integer> ids = product.getMiningFields(claim.getId());
                        for (TableItem item : fItems) {
                            /* 選択済みではないマイニングフィールドがチェックされてしまう問題を修正 */
                            item.setChecked(false);
                            for (Integer id : ids) {
                                if (((IFieldHeader) item.getData()).getId() == id) {
                                    checkItems.add(item);
                                    break;
                                }
                            }
                        }
                    }
                    for (TableItem item : checkItems) {
                        item.setChecked(true);
                    }
                } else {
                    for (TableItem item : fItems) {
                        item.setChecked(false);
                    }
                }
            }
        } else {
            productList.setInput(EMPTY_ARRAY);
            fieldList.setInput(EMPTY_ARRAY);
        }
    }

    private final SelectionListener selectAllButtonListener = new SuperSelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            setCheckedAll(productList, true);
            /* マイニング対象フィールドがひとつだけであればそのマイニング対象フィールドもチェックする */
            TableItem[] items = fieldList.getTable().getItems();
            if (items.length == 1) {
                if (!items[0].getChecked()) {
                    items[0].setChecked(true);
                }
            }
            super.widgetSelected(e);
        }
    };

    private final SelectionListener deselectAllButtonListener = new SuperSelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (((Button) e.getSource()).equals(productDeselectButton)) {
                setCheckedAll(productList, false);
            } else if (((Button) e.getSource()).equals(fieldDeselectButton)) {
                setCheckedAll(fieldList, false);
            }
            super.widgetSelected(e);
        }
    };


    void setCheckedAll(TableViewer viewer, boolean checked) {
        Table t = viewer.getTable();
        t.removeSelectionListener(callValidate);
        TableItem[] items = t.getItems();
        if (viewer.equals(productList)) {
            for (TableItem item : items) {
                if (!includeProject((String) item.getData())) {
                    item.setChecked(checked);
                }
            }
        } else {
            for (TableItem item : items) {
                item.setChecked(checked);
            }
        }
        t.addSelectionListener(callValidate);
    }

    class SuperSelectionListener implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            setPageComplete(fieldValidate());
        }


        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    };

    private SelectionListener callValidate = new SuperSelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if ((e.detail & SWT.CHECK) != 0) {
                setPageComplete(fieldValidate());
            }
        };
    };
    private SelectionListener fieldSelection = new SuperSelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if ((e.detail & SWT.CHECK) != 0) {
                /* マイニング対象フィールドにチェックを入れた時、ターゲットがひとつだけであればそのターゲットもチェックする */
                if ((getSelectedFields(true).size() == 1) && ((TableItem) e.item).getChecked()) {
                    TableItem[] items = productList.getTable().getItems();
                    if (items.length == 1) {
                        if (!items[0].getChecked()) {
                            items[0].setChecked(true);
                        }
                    }
                }
                super.widgetSelected(e);
            }
        };
    };
    private SelectionListener productSelection = new SuperSelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if ((e.detail & SWT.CHECK) != 0) {
                /* マイニングフィールドのずれをなくすため、登録済みのターゲットはチェックを外せなくする */
                String name = (String) ((TableItem) e.item).getData();
                if (includeProject(name)) {
                    ((TableItem) e.item).setChecked(true);
                }
                /* ターゲットにチェックを入れた時、マイニング対象フィールドがひとつだけであればそのマイニング対象フィールドもチェックする */
                if ((getSelectedProductNames(true).size() == 1) && ((TableItem) e.item).getChecked()) {
                    TableItem[] items = fieldList.getTable().getItems();
                    if (items.length == 1) {
                        if (!items[0].getChecked()) {
                            items[0].setChecked(true);
                        }
                    }
                }
                super.widgetSelected(e);
            }
        };
    };


    /**
     * 現在選択しているプロジェクトに、指定の問合せデータが登録されていればtrue
     * 
     * @param target
     * @return
     */
    boolean includeProject(IClaimData target) {
        IUIProject uiProject = getSelectedUIProject();
        if (uiProject == null) {
            return false;
        }
        final ICoronaProject coronaProject = uiProject.getObject();
        if (coronaProject == null) {
            return false;
        }

        List<IClaimData> registerdClaims = coronaProject.getClaimDatas();
        for (IClaimData claim : registerdClaims) {
            if (claim.getId() == target.getId()) {
                return true;
            }
        }
        return false;
    }


    /**
     * ターゲット名が、選択しているプロジェクトの、選択している問合せデータと紐づいて登録されていればtrue
     * 
     * @param productName
     *            検査するターゲット名
     * @return
     */
    boolean includeProject(String productName) {
        IUIProject uiProject = getSelectedUIProject();
        if (uiProject == null) {
            return false;
        }
        ICoronaProject project = uiProject.getObject();
        if (project == null) {
            return false;
        }
        IClaimData selectedClaim = getSelectedClaimData();
        if (selectedClaim == null) {
            return false;
        }

        for (IUIProduct uiProduct : uiProject.getProducts()) {
            ICoronaProduct product = uiProduct.getObject();
            if (product.getName().equals(productName)) {
                return product.getClaimDatas().contains(selectedClaim);
            }
        }
        return false;
    }


    @Override
    public void setFocus() {
        claimListViewer.getTable().setFocus();
    }

    /* ****************************************
     * LabelProvider
     */
    private class ColorLabelProvider extends LabelProvider implements IColorProvider {
        public final Color CLAIM_COLOR;
        public final Color PRODUCT_COLOR;


        public ColorLabelProvider() {
            CLAIM_COLOR = new Color(null, 0, 0, 255);
            PRODUCT_COLOR = new Color(null, 160, 160, 160);
        }


        @Override
        public String getText(Object element) {
            if (element instanceof IClaimData) {
                IClaimData claim = (IClaimData) element;
                return Messages.bind(Messages.NewProductCreationWizardPage_Claim_TableAndFile, CoronaIoUtils.getTableNameSuffix(claim.getTableName()),
                        claim.getFileName());
            }
            return super.getText(element);
        }


        @Override
        public Color getForeground(Object element) {
            if (element instanceof IClaimData) {
                if (includeProject((IClaimData) element)) {
                    return CLAIM_COLOR;
                }
            } else if (element instanceof String) {
                if (includeProject((String) element)) {
                    return PRODUCT_COLOR;
                }
            }
            return null;
        }


        @Override
        public Color getBackground(Object element) {
            return null;
        }


        @Override
        public void dispose() {
            CLAIM_COLOR.dispose();
            PRODUCT_COLOR.dispose();
            super.dispose();
        }
    }
}
