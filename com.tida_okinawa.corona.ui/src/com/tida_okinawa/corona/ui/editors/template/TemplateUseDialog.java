/**
 * @version $Id: TemplateUseDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/21 11:45:00
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;

import com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.correction.template.ITemplateTermType;
import com.tida_okinawa.corona.correction.template.Template;
import com.tida_okinawa.corona.correction.template.TemplateContainer;
import com.tida_okinawa.corona.correction.template.TemplateLink;
import com.tida_okinawa.corona.correction.template.TemplateRecord;
import com.tida_okinawa.corona.correction.template.TemplateTerm;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ITemplateItem;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;
import com.tida_okinawa.corona.ui.editors.EditorUtil;
import com.tida_okinawa.corona.ui.editors.pattern.InternalElementTreeSelectionDialog;
import com.tida_okinawa.corona.ui.editors.pattern.PatternRecords;
import com.tida_okinawa.corona.ui.editors.pattern.PrivateLabelDicContentProvider;
import com.tida_okinawa.corona.ui.editors.pattern.PrivateLabelDicLabelProvider;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 *         ひな型利用ダイアログ本体
 */
public class TemplateUseDialog extends Dialog {

    private TemplateRecords templateRecords;
    private TemplateTreeContentProvider treeContentProvider = null;
    private TemplateTreeLabelProvider treeLabelProvider = null;


    /**
     * @param parentShell
     *            親シェル
     */
    public TemplateUseDialog(Shell parentShell) {
        super(parentShell);
        templateRecords = new TemplateRecords(IoActivator.getTemplateFactory());
    }


    /** 新規Shellオブジェクト構成 */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TEMPLATE_USE_TITLE);
    }


    /**
     * @param provider
     *            TreeViewに設定するコンテンツプロバイダー
     */
    public void setTreeContentProvider(TemplateTreeContentProvider provider) {
        this.treeContentProvider = provider;
    }


    /**
     * @param provider
     *            TreeViewに設定するラベルプロバイダー
     */
    public void setTreeLabelProvider(TemplateTreeLabelProvider provider) {
        this.treeLabelProvider = provider;
    }


    /* ****************************************
     * UI
     */

    private static final int BTN_CLOSE_ID = 100;
    private static final int BTN_ENTRY_ID = 200;
    private static final int BTN_CREATE_ID = 300;
    private static TreeViewer treeViewer = null;
    private static TableViewer tableViewer = null;
    private static Tree treeLeft = null;
    private static Table tableRight = null;
    private static Button btnEdit = null;
    private static Button btnDelete = null;


    @Override
    protected Control createDialogArea(Composite parent) {

        /* マスター全体の作成 */
        Composite compAll = new Composite(parent, SWT.NONE);
        compAll.setLayout(new GridLayout());
        compAll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        /* マスター上部の作成 */
        Composite compTitle = new Composite(compAll, SWT.NONE);
        compTitle.setLayout(new GridLayout());
        compTitle.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        setTopComposite(compTitle);
        Label labelSeparator1 = new Label(compAll, SWT.SEPARATOR | SWT.HORIZONTAL);
        labelSeparator1.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        /* マスター中央部の作成 */
        Composite compMiddle = new Composite(compAll, SWT.NONE);
        compMiddle.setLayout(new GridLayout(2, false));
        compMiddle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setMiddleComposite(compMiddle);

        /* マスター下部の作成(セパレート表示) */
        Composite compButton = new Composite(compAll, SWT.NONE);
        compButton.setLayout(new GridLayout());
        compButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Label labelSeparator2 = new Label(compButton, SWT.SEPARATOR | SWT.HORIZONTAL);
        labelSeparator2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        return parent;
    }


    /**
     * マスター上部の中身
     * 
     * @param parent
     *            親Composite
     */
    @SuppressWarnings("static-method")
    private void setTopComposite(Composite parent) {
        Label labelTop = new Label(parent, SWT.NONE);
        labelTop.setText(Messages.TEMPLATE_USE_OUTLINE);
    }


    /**
     * 中央の領域のコンポーネント配置
     * 
     * @param parent
     *            親Composite
     */
    private void setMiddleComposite(Composite parent) {
        /* マスター中央部・左側の作成 */
        Composite compTree = new Composite(parent, SWT.BORDER);
        compTree.setLayout(new GridLayout());
        GridData layoutLeft = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutLeft.widthHint = 400;
        layoutLeft.heightHint = 400;
        compTree.setLayoutData(layoutLeft);
        setLeftComposite(compTree);

        /* マスター中央部・右側の作成 */
        Composite compTable = new Composite(parent, SWT.BORDER);
        compTable.setLayout(new GridLayout());
        GridData layoutRight = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutRight.widthHint = 400;
        layoutRight.heightHint = 400;
        compTable.setLayoutData(layoutRight);
        setRightComposite(compTable);
    }


    /**
     * マスター左部の中身
     * 
     * @param parent
     *            親Composite
     */
    private void setLeftComposite(Composite parent) {
        /* ボタン表示 */
        Composite compBtn = new Composite(parent, SWT.NONE);
        compBtn.setLayout(new GridLayout(2, false));
        compBtn.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
        btnEdit = new Button(compBtn, SWT.PUSH);
        btnEdit.setText(Messages.TEMPLATE_BUTTON_EDIT);
        btnEdit.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* ひな型登録ダイアログを開く　 */
                openTemplateModifyDialog();
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        btnDelete = new Button(compBtn, SWT.PUSH);
        btnDelete.setText(Messages.TEMPLATE_BUTTON_DELETE);
        btnDelete.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* ひな型を削除する */
                if (questionMessageBox(getShell(), Messages.TEMPLATE_MESSAGEBOX_DELETE) == SWT.OK) {
                    delTemplate();
                    templateRecords = new TemplateRecords(IoActivator.getTemplateFactory());
                    treeViewer.setInput(templateRecords);
                    if (templateRecords.isTemplateTerm() >= 0) {
                        /* 先頭にカーソルをセット */
                        treeViewer.setSelection(new StructuredSelection(templateRecords.getTemplateRecords().toArray()[0]));
                    } else {
                        warningMessageBox(getShell(), Messages.TEMPLATE_MESSAGEBOX_NOTHING);
                        setReturnCode(SWT.OK);
                        close();
                    }
                }
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        /* Tree表示 */
        treeLeft = new Tree(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.BORDER);
        GridData gridTree = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeLeft.setLayoutData(gridTree);
        treeViewer = new TreeViewer(treeLeft);
        treeViewer.setContentProvider(treeContentProvider);
        treeViewer.setLabelProvider(treeLabelProvider);
        treeViewer.setInput(templateRecords);
        /* 先頭にカーソルをセット */
        int pos = templateRecords.isTemplateTerm();
        treeViewer.setSelection(new StructuredSelection(templateRecords.get(pos)));
        /* TreeViewer内の項目を切り替えた際に呼ばれるリスナー */
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                /* コンポーネント（右側）の表示切替 */
                setEnabledComponent();
            }
        });
    }


    /**
     * マスター右部の中身
     * 
     * @param parent
     *            親Composite
     */
    private void setRightComposite(Composite parent) {
        /* ボタン表示 */
        Composite compBtn = new Composite(parent, SWT.NONE);
        compBtn.setLayout(new GridLayout(2, false));
        compBtn.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
        Button btnEdit = new Button(compBtn, SWT.PUSH);
        btnEdit.setText(Messages.TEMPLATE_BUTTON_CLEAR);
        btnEdit.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = tableRight.getSelectionIndex();
                if (index == -1)
                    return;
                tableRight.remove(index);
                editModels.remove(index);
                tmpTemplate.remove(index);
                /* 削除した分の新規入力アイテムを追加 */
                createNewInput(1, properties.size(), false);
                /* テーブルから入力アイテムがなくなった、あるいは、 テーブルのアイテムが1つ＆そのアイテムが空の場合 */
                if (tmpTemplate.size() == 0 || (tmpTemplate.size() == 1 && editModels.get(0).chkAllInvalidItem())) {
                    setEnabledComponent2(true);
                }
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        Button btnDelete = new Button(compBtn, SWT.PUSH);
        btnDelete.setText(Messages.TEMPLATE_BUTTON_CLEAR_ALL);
        btnDelete.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (questionMessageBox(getShell(), Messages.TEMPLATE_MESSAGEBOX_CLEAR_ALL) == SWT.OK) {
                    tableRight.removeAll();
                    createNewInput(1, properties.size(), true);
                    setEnabledComponent2(true);
                }
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        /* テーブルの生成 */
        createTable(parent, ((IStructuredSelection) treeViewer.getSelection()).getFirstElement());
    }


    /* ****************************************
     * Component
     */
    private static Button okButton = null;


    /** ボタン・バーの設定 */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, BTN_CREATE_ID, Messages.TEMPLATE_BUTTON_CREATE, true);
        okButton.setEnabled(false);
        createButton(parent, BTN_CLOSE_ID, Messages.TEMPLATE_BUTTON_CLOSE, true);
    }


    /** ボタン押下時に呼ばれる */
    @Override
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case BTN_CREATE_ID:
            if (questionMessageBox(getShell(), Messages.TEMPLATE_MESSAGEBOX_CREATE) == SWT.OK) {
                entryPatternAction();
            }
            break;
        case BTN_CLOSE_ID:
            setReturnCode(BTN_CLOSE_ID);
            close();
            break;
        default:
            super.buttonPressed(buttonId);
            break;
        }
    }


    /* ****************************************
     * Action
     */


    /**
     * コンポーネントの表示切替（その１）<br/>
     * Tableの選択状態によって、編集・削除ボタンの活性／非活性を切り替える
     * （ルートの要素以外は非活性。子の編集・削除ができるように見えてしまうので）
     */
    private void setEnabledComponent() {
        IStructuredSelection ss = (IStructuredSelection) treeViewer.getSelection();
        Object object = ss.getFirstElement();
        if (object instanceof TemplateRecord) {
            btnEdit.setEnabled(true);
            btnDelete.setEnabled(true);
        } else {
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
        }
        createTable(tableRight.getParent(), object);
    }


    /**
     * コンポーネントの表示切替（その２）<br/>
     * Tableの入力状態によって、Treeと編集・削除ボタンの活性／非活性を切り替える
     * 
     * @param enable
     *            true:活性、false:非活性
     */
    private static void setEnabledComponent2(boolean enable) {
        boolean enableBtn = enable;
        IStructuredSelection ss = (IStructuredSelection) treeViewer.getSelection();
        Object object = ss.getFirstElement();

        if (enable == true) {
            if (object instanceof TemplateRecord) {
                enableBtn = true;
            } else {
                enableBtn = false;
            }
        }
        btnEdit.setEnabled(enableBtn);
        btnDelete.setEnabled(enableBtn);
        /* Treeの活性／非活性 */
        treeLeft.setEnabled(enable);
    }


    /** ひな型登録ダイアログを開く */
    private void openTemplateModifyDialog() {
        IStructuredSelection ss = (IStructuredSelection) treeViewer.getSelection();
        Object object = ss.getFirstElement();

        if (object instanceof TemplateRecord) {
            /* ダイアログ表示 */
            TemplateModifyDialog dialog = new TemplateModifyDialog(getShell());
            dialog.setExist(true);
            dialog.setTreeContentProvider(new TemplateTreeContentProvider());
            dialog.setTreeLabelProvider(new TemplateTreeLabelProvider());
            dialog.setInput(object);
            if (dialog.open() == BTN_ENTRY_ID) {
                templateRecords = new TemplateRecords(IoActivator.getTemplateFactory());
                treeViewer.setInput(templateRecords);
            }
            /* カーソルを先頭にセット */
            treeViewer.setSelection(new StructuredSelection(templateRecords.getTemplateRecords().toArray()[0]));
        }
    }


    /** ひな型の削除 */
    private void delTemplate() {
        IStructuredSelection ss = (IStructuredSelection) treeViewer.getSelection();
        Object object = ss.getFirstElement();

        if (object instanceof TemplateRecord) {
            /* 削除アイテムに追加してコミット */
            templateRecords.remove((TemplateRecord) object);
            IoActivator.getTemplateFactory().commit(null);
        }
    }

    /* ****************************************
     * Pattern
     */

    private FormEditor formEditor;
    private PatternRecords patternRecords = null;


    /**
     * @param editor
     *            辞書情報（パターンの登録用）
     */
    public void setFormEditor(FormEditor editor) {
        this.formEditor = editor;
    }


    /**
     * ひな型利用ダイアログで作成したパターンレコード一覧の取得
     * 
     * @return PatternRecords
     */
    public PatternRecords getPatternRecords() {
        return patternRecords;
    }


    /**
     * ひな型利用ダイアログで作成したパターンレコード一覧の設定
     * 
     * @param patternRecords
     *            パターンレコード一覧
     */
    public void setPatternRecords(PatternRecords patternRecords) {
        this.patternRecords = patternRecords;
    }


    /** パターン辞書への登録 */
    private void entryPatternAction() {
        boolean resultLink = true;
        boolean resultPattern = true;

        /* 対象のパターン辞書のレコード一覧を取得 */
        DicEditorInput dicInput = (DicEditorInput) formEditor.getEditorInput();
        ICoronaDic coronaDic = dicInput.getDictionary();
        if (!(coronaDic instanceof IPatternDic)) {
            errorMessageBox(getShell(), Messages.TEMPLATE_MESSAGEBOX_ERROR_GET_PATTERN);
            return;
        }
        /* 登録するデータ一覧をローカルで保管（登録した情報は削除するため） */
        List<Template> privateTmpTmplate = new ArrayList<Template>();
        privateTmpTmplate.addAll(tmpTemplate);
        List<EditModel> privateEditModels = new ArrayList<EditModel>();
        privateEditModels.addAll(editModels);
        int cntInput = 0;
        /* ひな型をパターンに変換 */
        for (Template tmp : privateTmpTmplate) {
            /* 対象のテーブル行がすべて埋まっている場合 */
            if (privateEditModels.get(cntInput).chkAllValidItem()) {
                if (tmp instanceof TemplateRecord) {
                    /* 作成するデータに参照（Link）の項目があれば追加 */
                    if (getTemplateLink(coronaDic, tmp) != true) {
                        resultLink = false;
                        break;
                    }
                    patternRecords.add(convertTemplateToPattern(tmp));
                    /* 先頭の（登録済）情報を削除 */
                    tmpTemplate.remove(0);
                    editModels.remove(0);
                    tableRight.remove(0);
                }
            } else {
                /* 作成に失敗したアイテムがある（但し、残りテーブルのアイテムが残り１つ＆そのテーブルが空の場合はエラーとしない） */
                if (!((tmpTemplate.size() == 0) || (tmpTemplate.size() == 1 && editModels.get(0).chkAllInvalidItem()))) {
                    resultPattern = false;
                }
            }
            cntInput++;
        }
        /* テーブルから入力アイテムがなくなった、あるいは、残りテーブルのアイテムが残り１つ＆そのテーブルが空の場合 */
        if ((tmpTemplate.size() == 0) || (tmpTemplate.size() == 1 && editModels.get(0).chkAllInvalidItem())) {
            createNewInput(1, properties.size(), true);
            setEnabledComponent2(true);
        }
        if (resultPattern != true) {
            if (resultLink == true) {
                errorMessageBox(getShell(), Messages.TEMPLATE_MESSAGEBOX_ERROR_CREATE);
            } else {
                errorMessageBox(getShell(), Messages.TEMPLATE_MESSAGEBOX_ERROR_CREATE_LINK);
            }
        }
        okButtonEnable();
    }


    /**
     * ひな型をパターンに変換
     * 
     * @param template
     *            ひな型
     * @return パターン
     */
    private static PatternRecord convertTemplateToPattern(Template template) {
        ITemplateItem tmpItem = ((TemplateRecord) template).getITemplateItem();
        /* 新規パターンレコード作成 */
        PatternRecord record = new PatternRecord(IoActivator.getDicFactory().createPattern(tmpItem.getName(), tmpItem.getText(), tmpItem.getPatternType(),
                tmpItem.isParts()));
        record.getIPattern().setDirty(true);
        return record;
    }


    /**
     * 再帰的にパターンにひな型の参照（Link）の要素を追加
     * 
     * @param coronaDic
     *            登録先の辞書
     * @param element
     *            追加する要素
     * @return 処理結果
     */
    private boolean getTemplateLink(ICoronaDic coronaDic, Template element) {

        if (element == null) {
            new Exception(Messages.TEMPLATE_EXCEPTION_NULL).printStackTrace();
        }

        if (element instanceof TemplateLink) {
            int linkId = 0;
            boolean addFlag = true;
            ITemplateItem linkItem = ((TemplateLink) element).getLinkTemplate();
            if (linkItem != null) {
                /* ひな型のテキストをパターンに変換 */
                Pattern convertPattern = DicPatternUtil.convertFrom(linkItem.getText());
                String convertString = DicPatternUtil.convertFrom(convertPattern);
                /* 現在の辞書に、ひな型に登録された参照（Link）のパターンが存在するかどうかをチェック */
                for (PatternRecord record : patternRecords.getPatternRecords()) {
                    if (convertString.equals(record.getIPattern().getText())) {
                        linkId = record.getId();
                        addFlag = false;
                        break;
                    }
                }

                if (addFlag == true) {
                    /* パターンの生成、登録レコードに追加 */
                    PatternRecord input = new PatternRecord(IoActivator.getDicFactory().createPattern(linkItem.getName(), convertString,
                            linkItem.getPatternType(), linkItem.isParts()));
                    input.getIPattern().setDirty(true);
                    patternRecords.add(input);

                    /* 参照（Link）の要素を対象のパターン辞書にコミット */
                    if (coronaDic.commit(new NullProgressMonitor()) != true) {
                        return false;
                    }
                    linkId = patternRecords.get(patternRecords.indexOf(input)).getId();
                }

                /* 参照（Link）先IDの更新 */
                linkItem.setTemplateId(linkId);
                ((TemplateLink) element).setLinkTemplate(linkItem);
            }
        }

        if (element instanceof TemplateContainer) {
            List<Template> children = ((TemplateContainer) element).getChildren();
            for (Template p : children) {
                if (getTemplateLink(coronaDic, p) != true) {
                    return false;
                }
            }
        }
        return true;
    }


    /* ****************************************
     * Table
     */

    private int cntColumns;
    private List<String> properties = null; // カラムを特定するための名前
    private List<TemplateRecord> comboRecord = null; // Comboに対応するひな型
    private String[] comboValue = null; // Comboに表示する文字列
    private static List<EditModel> editModels = null; // TableViewerに表示するアイテム
    private static CellEditor[] cellEditor = null;


    private void createTable(Composite composite, Object object) {

        /* 既にtableRightが設定されている場合は再描画のために破棄 */
        if (tableRight != null) {
            tableRight.dispose();
        }
        tableRight = new Table(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        tableRight.setLayout(new GridLayout());
        tableRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tableRight.setLinesVisible(true);
        tableRight.setHeaderVisible(true);

        Template template = getRootTemplate(object);
        if (template != null) {
            cntColumns = 0;
            properties = new ArrayList<String>(templateRecords.getTemplateRecords().size());
            /* カラムを生成 */
            createTableColumn(template);
        } else {
            return;
        }

        tableViewer = new TableViewer(tableRight);
        /* カラム毎のプロパティの設定 */
        String[] columns = new String[properties.size()];
        for (int i = 0; i < properties.size(); i++) {
            columns[i] = properties.get(i);
        }
        tableViewer.setColumnProperties(columns);

        /* Comboのアイテムを生成 */
        createComboItem();

        /* 各カラムのエディタの設定 */
        cellEditor = new CellEditor[properties.size()];
        for (int i = 0; i < properties.size(); i++) {
            if ((properties.get(i)).startsWith(Messages.TEMPLATE_USE_PROPERTY_TEXT)) {
                cellEditor[i] = new TextCellEditor(tableRight);
            } else if ((properties.get(i)).startsWith(Messages.TEMPLATE_USE_PROPERTY_DIALOG)) {
                cellEditor[i] = new NewDialogCellEditor(tableRight);
            } else if ((properties.get(i)).startsWith(Messages.TEMPLATE_USE_PROPERTY_COMBO)) {
                cellEditor[i] = new ComboBoxCellEditor(tableRight, comboValue);
            }
        }
        tableViewer.setCellEditors(cellEditor);
        tableViewer.setCellModifier(new ICellModifier() {
            String output = ""; //$NON-NLS-1$


            /* 編集確定時に呼ばれる */
            @Override
            public void modify(Object element, String property, Object value) {
                int selection = 0;
                if (element instanceof TableItem) {
                    selection = ((TableItem) element).getParent().getSelectionIndex();
                    element = ((TableItem) element).getData();
                }

                if (property.startsWith(Messages.TEMPLATE_USE_PROPERTY_TEXT)) {
                    int index = Integer.parseInt(property.substring(Messages.TEMPLATE_USE_PROPERTY_TEXT.length()));
                    output = (String) value;
                    ((EditModel) element).setText(index, output);
                } else if (property.startsWith(Messages.TEMPLATE_USE_PROPERTY_DIALOG)) {
                    int index = Integer.parseInt(property.substring(Messages.TEMPLATE_USE_PROPERTY_DIALOG.length()));
                    output = (String) value;
                    ((EditModel) element).setText(index, output);
                } else if (property.startsWith(Messages.TEMPLATE_USE_PROPERTY_COMBO)) {
                    /* comboの値はIntegerなので、値との紐付けが必要 */
                    int index = Integer.parseInt(property.substring(Messages.TEMPLATE_USE_PROPERTY_COMBO.length()));
                    if ((Integer) value >= 0) {
                        output = comboValue[(Integer) value];
                    } else {
                        output = ""; //$NON-NLS-1$
                    }
                    ((EditModel) element).setText(index, output);
                }
                tableViewer.update(element, null);

                /*
                 * ひな型の一時データにも情報を反映。
                 * テーブルのアイテムを新規追加した際に呼ばれると、１つ前の行のアイテムがクリアされるので""の場合は処理しない。
                 * （そもそも、パターンを作成するかどうかはeditModelsで判定するので""であることを登録する必要がない）
                 */
                if (!(output.equals(""))) { //$NON-NLS-1$
                    cntTmp = 0;
                    /*
                     * getSelectionIndex()で位置を取得すると、すべて入力後、
                     * 次の行をクリックした場所の情報まで格納されてしまう。
                     */
                    editTmpTemplate(tmpTemplate.get(selection), property, value);
                }
                chkEditModel();
            }


            /* クリック直後に呼ばれる */
            @SuppressWarnings("unused")
            @Override
            public Object getValue(Object element, String property) {
                expandTreeElement();
                if (property.startsWith(Messages.TEMPLATE_USE_PROPERTY_TEXT)) {
                    int index = Integer.parseInt(property.substring(Messages.TEMPLATE_USE_PROPERTY_TEXT.length()));
                    return ((EditModel) element).getText(index);
                } else if (property.startsWith(Messages.TEMPLATE_USE_PROPERTY_DIALOG)) {
                    int index = Integer.parseInt(property.substring(Messages.TEMPLATE_USE_PROPERTY_DIALOG.length()));
                    return ((EditModel) element).getText(index);
                } else if (property.startsWith(Messages.TEMPLATE_USE_PROPERTY_COMBO)) {
                    int index = Integer.parseInt(property.substring(Messages.TEMPLATE_USE_PROPERTY_COMBO.length()));
                    for (int i = 0; i < comboValue.length; i++) {
                        if (comboValue[i].equals(((EditModel) element).getText(index))) {
                            return i;
                        }
                        return 0;
                    }
                }
                return ""; //$NON-NLS-1$
            }


            /* 編集可能かどうかの判定 */
            @Override
            public boolean canModify(Object element, String property) {
                return true;
            }


            /* Treeの選択中の要素を展開する */
            private void expandTreeElement() {
                IStructuredSelection ss = (IStructuredSelection) treeViewer.getSelection();
                Object object = ss.getFirstElement();

                if (object instanceof Template) {
                    Template parent = (Template) object;
                    while (parent.getParent() != null) {
                        parent = parent.getParent();
                    }
                    if (parent instanceof TemplateRecord) {
                        treeViewer.expandToLevel(parent, AbstractTreeViewer.ALL_LEVELS);
                    }
                }
            }


            /* EditModelの状態チェック */
            private void chkEditModel() {
                /* 選択中のテーブル行がある場合 */
                if (tableRight.getSelectionIndex() >= 0) {
                    /* 選択中の行に対応するTemplate取得 */
                    Template tmp = tmpTemplate.get(tableRight.getSelectionIndex());
                    if (tmp != null) {
                        /*
                         * テーブルの行の新規作成判定
                         * （一か所も空欄がなく、また、次のひな型が未登録の場合は新規作成）
                         */
                        if ((editModels.get(tableRight.getSelectionIndex()).chkAllValidItem() == true)
                                && (tmpTemplate.size() == (tableRight.getSelectionIndex() + 1))) {
                            createNewInput(1, properties.size(), false);
                        }
                    }
                }
            }
        });
        tableViewer.setContentProvider(new ArrayContentProvider());
        PrivateLabelProvider privateLabelProvider = new PrivateLabelProvider();
        privateLabelProvider.setProperties(properties);
        tableViewer.setLabelProvider(privateLabelProvider);
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                okButtonEnable();
            }

        });
        createNewInput(1, properties.size(), true);
        composite.layout();
    }


    /**
     * 再帰的にカラムを作成（プロパティも併せて生成）
     * 
     * @param element
     *            ひな型の要素
     */
    private void createTableColumn(Template element) {
        if (element == null) {
            new Exception(Messages.TEMPLATE_EXCEPTION_NULL).printStackTrace();
        }

        /* ひな型名の入力用 */
        if (cntColumns == 0 && element.getParent() == null) {
            /* カラム生成 */
            TableColumn column = new TableColumn(tableRight, SWT.NONE);
            column.setText(Messages.TEMPLATE_USE_COLUMN_PATTERN_NAME);
            column.setWidth(80);
            properties.add(Messages.TEMPLATE_USE_PROPERTY_TEXT + cntColumns);
            cntColumns++;
        }
        /* 要素の状態が可変の場合 */
        if (element instanceof TemplateTerm) {
            TemplateTerm term = (TemplateTerm) element;
            if (term.getFixCheck() != true) {
                /* カラム生成 */
                TableColumn column = new TableColumn(tableRight, SWT.NULL);
                column.setText(Messages.TEMPLATE_OUTPUT_ELEMENT + cntColumns);
                /* 単語（Term）の場合 */
                if (element instanceof TemplateTerm) {
                    if (ITemplateTermType.TYPE_WORD.equals(((TemplateTerm) element).getState())) {
                        properties.add(Messages.TEMPLATE_USE_PROPERTY_TEXT + cntColumns);
                    } else if (ITemplateTermType.TYPE_LABEL.equals(((TemplateTerm) element).getState())) {
                        properties.add(Messages.TEMPLATE_USE_PROPERTY_DIALOG + cntColumns);
                    }
                }
                cntColumns++;
                column.setWidth(80);
            }
        }
        /* 参照（Link）の場合 */
        else if (element instanceof TemplateLink) {
            TemplateLink link = (TemplateLink) element;
            if (link.getFixCheck() != true) {
                TableColumn column = new TableColumn(tableRight, SWT.NULL);
                column.setText(Messages.TEMPLATE_OUTPUT_ELEMENT + cntColumns);
                properties.add(Messages.TEMPLATE_USE_PROPERTY_COMBO + cntColumns);
                cntColumns++;
                column.setWidth(80);
            }
        }

        if (element instanceof TemplateContainer) {
            List<Template> children = ((TemplateContainer) element).getChildren();
            for (Template p : children) {
                createTableColumn(p);
            }
        }
    }


    /** Comboのアイテムを生成 */
    private void createComboItem() {
        int cntLink = 0;
        List<String> tmpValue = new ArrayList<String>(templateRecords.getTemplateRecords().size());
        comboRecord = new ArrayList<TemplateRecord>(templateRecords.getTemplateRecords().size());
        for (TemplateRecord record : templateRecords.getTemplateRecords()) {
            if (record.getTemplateId() > 0) {
                comboRecord.add(record);
                tmpValue.add(record.getName());
            }
        }
        if (tmpValue.size() == 0) {
            /* 参照の要素がない場合は空文字を出力 */
            comboValue = new String[1];
            comboValue[0] = ""; //$NON-NLS-1$
        } else {
            comboValue = new String[tmpValue.size()];
            for (String value : tmpValue) {
                comboValue[cntLink++] = value;
            }
        }
    }


    /**
     * 新規入力アイテムの生成
     * 
     * @param num
     *            作成するアイテム数
     * @param size
     *            作成するアイテムのサイズ
     * @param clear
     *            アイテムをクリアするかどうか
     */
    private static void createNewInput(int num, int size, boolean clear) {
        /* 新規でテーブルのアイテムを生成する場合 */
        if (clear == true) {
            tmpTemplate = new ArrayList<Template>(100);
            editModels = new ArrayList<EditModel>(100);
        }
        createTmpTemplate();
        for (int i = 0; i < num; i++) {
            editModels.add(new EditModel(size));
        }
        tableViewer.setInput(editModels.toArray());

        /* タブ移動の設定 */
        int[] tabCnt = new int[cellEditor.length];
        for (int i = 0; i < cellEditor.length; i++) {
            tabCnt[i] = i;
        }
        EditorUtil.setFocusMoveListener(cellEditor, tableViewer, tabCnt);
    }

    /**
     * テーブル用ダイアログセルエディタ
     */
    private static class NewDialogCellEditor extends DialogCellEditor {

        public NewDialogCellEditor(Composite parent) {
            super(parent);
        }


        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            final ITreeContentProvider lConPro = new PrivateLabelDicContentProvider();
            final LabelProvider lLPro = new PrivateLabelDicLabelProvider();

            List<ICoronaDic> input = createDialogInput(ILabelDic.class);
            IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

            InternalElementTreeSelectionDialog d = new InternalElementTreeSelectionDialog(editor.getSite().getShell(), lLPro, lConPro);
            d.setTitle(Messages.TEMPLATE_USE_LABEL_DIALOG_TITLE);
            d.setMessage(Messages.TEMPLATE_USE_LABEL_DIALOG_OUTLINE);
            d.setEmptyListMessage(Messages.TEMPLATE_USE_LABEL_DIALOG_EMPTY);
            d.setValidator(new Class<?>[] { ILabel.class });
            d.setInput(input);
            d.setAllowMultiple(false);
            d.open();
            if (d.getReturnCode() == Dialog.OK) {
                Object result = d.getFirstResult();
                return ((ILabel) result).getName();
            }
            return ""; //$NON-NLS-1$
        }


        List<ICoronaDic> createDialogInput(Class<? extends ICoronaDic> clazz) {
            List<ICoronaDic> input = new ArrayList<ICoronaDic>();
            input.addAll(IoActivator.getService().getDictionarys(clazz));
            return input;
        }
    }


    /**
     * テーブル用ラベルプロバイダー
     */
    private static class PrivateLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {

        private int columnIndex;
        private List<String> properties = null;


        public PrivateLabelProvider() {
            setEnabledComponent2(true);
        }


        public void setProperties(List<String> properties) {
            this.properties = properties;
        }


        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }


        @Override
        public String getColumnText(Object element, int columnIndex) {
            this.columnIndex = columnIndex;
            String strCol;
            EditModel model = (EditModel) element;
            if ((properties.get(columnIndex)).startsWith(Messages.TEMPLATE_USE_PROPERTY_TEXT)) {
                strCol = (model.getText(columnIndex));
            } else if ((properties.get(columnIndex)).startsWith(Messages.TEMPLATE_USE_PROPERTY_DIALOG)) {
                strCol = (model.getText(columnIndex));
            } else if ((properties.get(columnIndex)).startsWith(Messages.TEMPLATE_USE_PROPERTY_COMBO)) {
                strCol = (model.getText(columnIndex));
            } else {
                strCol = Messages.TEMPLATE_USE_STRING_ERROR;
            }
            chkTableInput(strCol);
            /* 入力チェック */
            if (!(strCol.equals(""))) { //$NON-NLS-1$
                return strCol;
            } else {
                return Messages.TEMPLATE_USE_STRING_ENTRY;
            }
        }


        @Override
        public Color getForeground(Object element) {
            if (((EditModel) element).getText(columnIndex).equals("")) { //$NON-NLS-1$
                return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
            }
            return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
        }


        @Override
        public Color getBackground(Object element) {
            return null;
        }


        private static void chkTableInput(String strCol) {
            /* テーブルに入力があった場合 */
            if ((!(strCol.equals(""))) && (!(strCol.equals(Messages.TEMPLATE_USE_STRING_ERROR)))) { //$NON-NLS-1$
                setEnabledComponent2(false);
            }
            okButtonEnable();
        }
    }


    /**
     * テーブル用編集モデル
     */
    private static class EditModel {

        private String[] texts = null;


        private EditModel(int size) {
            texts = new String[size];
            for (int i = 0; i < size; i++) {
                texts[i] = ""; //$NON-NLS-1$
            }
        }


        /**
         * テーブル行のアイテムがすべて入力されているかどうかの確認
         * 
         * @return true： すべて入力<br>
         *         false：最低１つは未入力
         */
        public boolean chkAllValidItem() {
            boolean result = true;
            if (texts != null) {
                for (int i = 0; i < getSize(); i++) {
                    if (texts[i].equals("")) { //$NON-NLS-1$
                        result = false;
                        break;
                    }
                }
            }
            return result;
        }


        /**
         * テーブル行のアイテムがすべて未入力かどうかの確認
         * 
         * @return true：すべて未入力
         *         false：最低１つは入力
         */
        public boolean chkAllInvalidItem() {
            boolean result = true;
            if (texts != null) {
                for (int i = 0; i < getSize(); i++) {
                    if (!(texts[i].equals(""))) { //$NON-NLS-1$
                        result = false;
                        break;
                    }
                }
            }
            return result;
        }


        public int getSize() {
            if (texts != null) {
                return texts.length;
            }
            return 0;
        }


        public String getText(int index) {
            return texts[index];
        }


        public void setText(int index, String text) {
            this.texts[index] = text;
        }
    }

    /* ****************************************
     * ひな型
     */
    private static List<Template> tmpTemplate = null;
    private int cntTmp = 0;


    /** パターン作成用のデータを格納する一時リストの作成 */
    private static void createTmpTemplate() {
        /*
         * 未選択の場合
         * （ひな型利用ダイアログからのひな型編集後や、ひな型削除ボタン押下後は空なのでその対策）
         */
        if (treeViewer.getSelection().isEmpty() != true) {
            /* 新規でIStructuredSelectionを生成 */
            IStructuredSelection ss = new StructuredSelection(((IStructuredSelection) treeViewer.getSelection()).getFirstElement());
            Object object = ss.getFirstElement();
            tmpTemplate.add(getRootTemplate(object));
        }
    }


    /**
     * 再帰的にひな型を編集
     * 
     * @param element
     *            ひな型の要素<br/>
     * @param property
     *            カラムの種類
     * @param input
     *            登録するデータ
     */
    private void editTmpTemplate(Template element, String property, Object input) {

        /* 更新が完了したのでリターン */
        if (cntTmp == -1) {
            return;
        }

        if (element == null) {
            new Exception(Messages.TEMPLATE_EXCEPTION_NULL).printStackTrace();
        }

        /* ひな型名の更新 */
        if (cntTmp == 0 && element.getParent() == null) {
            if (properties.get(cntTmp).equals(property)) {
                if (element instanceof TemplateRecord) {
                    ((TemplateRecord) element).setName((String) input);
                    cntTmp = -1;
                    return;
                }
            }
            cntTmp++;
        }
        /* 要素の状態が可変の場合 */
        else if (element instanceof TemplateTerm) {
            TemplateTerm term = (TemplateTerm) element;
            if (term.getFixCheck() != true) {
                if (ITemplateTermType.TYPE_WORD.equals(((TemplateTerm) element).getState())) {
                    if (properties.get(cntTmp).equals(property)) {
                        /* 単語（Word）の更新。パターン変換時にラベルが邪魔なのでクリア */
                        term.setWord((String) input);
                        term.setLabel(""); //$NON-NLS-1$
                        cntTmp = -1;
                        return;
                    }
                } else if (ITemplateTermType.TYPE_LABEL.equals(((TemplateTerm) element).getState())) {
                    if (properties.get(cntTmp).equals(property)) {
                        /* 単語（Label）の更新。パターン変換時に単語が邪魔なのでクリア */
                        term.setWord(""); //$NON-NLS-1$
                        term.setLabel((String) input);
                        cntTmp = -1;
                        return;
                    }
                }
                cntTmp++;
            }
        }
        /* 参照（Link）の場合 */
        else if (element instanceof TemplateLink) {
            TemplateLink link = (TemplateLink) element;
            if (link.getFixCheck() != true) {
                if (properties.get(cntTmp).equals(property)) {
                    link.setLinkTemplate(comboRecord.get((Integer) input).getITemplateItem());
                    cntTmp = -1;
                    return;
                }
                cntTmp++;
            }
        }

        if (element instanceof TemplateContainer) {
            List<Template> children = ((TemplateContainer) element).getChildren();
            for (Template p : children) {
                editTmpTemplate(p, property, input);
            }
        }
    }


    /* ****************************************
     * その他
     */

    /* OKボタンの有効化／無効化 */
    private static void okButtonEnable() {
        if (okButton != null) {
            if (tableViewer != null) {
                /* 登録アイテムが入力されているかの判定 */
                boolean selected = false;
                Table table = tableViewer.getTable();
                for (int i = 0; i < table.getItemCount(); i++) {
                    if (table.getItem(i).getData() != null) {
                        Object object = table.getItem(i).getData();
                        if (object instanceof EditModel) {
                            EditModel editModel = (EditModel) object;
                            /* すべて入力されている箇所がある場合 */
                            if (editModel.chkAllValidItem()) {
                                selected = true;
                                break;
                            }
                        }
                    }
                }
                if (selected) {
                    okButton.setEnabled(true);
                } else {
                    okButton.setEnabled(false);
                }
            }
        }
    }


    /**
     * Treeで選択中の要素のひな型レコード（先頭）を取得する
     * 
     * @param object
     *            選択中の要素
     * @return ひな型レコード
     */
    private static TemplateRecord getRootTemplate(Object object) {
        Template template = null;
        if (object instanceof Template) {
            template = (Template) object;
            while (template.getParent() != null) {
                template = template.getParent();
            }
        }
        /* 新規のひな型レコードを生成してリターン（新規アイテム生成時にidが重複するのを避けるため） */
        if (template instanceof TemplateRecord) {
            return new TemplateRecord(template);
        }

        return null;
    }


    /**
     * メッセージボックス（確認用）
     * 
     * @param shell
     *            親Shell
     * @param message
     *            メッセージ
     * @return 処理結果
     */
    private static int questionMessageBox(Shell shell, String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
        messageBox.setText(Messages.TEMPLATE_MESSAGEBOX_TEXT_QUESTION);
        messageBox.setMessage(message);
        return messageBox.open();
    }


    /**
     * メッセージボックス（確認用）
     * 
     * @param shell
     *            親Shell
     * @param message
     *            メッセージ
     */
    private static void warningMessageBox(Shell shell, String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
        messageBox.setText(Messages.TEMPLATE_MESSAGEBOX_TEXT_WARNING);
        messageBox.setMessage(message);
        messageBox.open();
    }


    /**
     * メッセージボックス（エラー用）
     * 
     * @param shell
     *            親Shell
     * @param message
     *            メッセージ
     */
    private static void errorMessageBox(Shell shell, String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        messageBox.setText(Messages.TEMPLATE_MESSAGEBOX_TEXT_ERROR);
        messageBox.setMessage(message);
        messageBox.open();
    }


    @Override
    public boolean close() {
        boolean inputData = false;
        for (EditModel model : editModels) {
            if (!model.chkAllInvalidItem()) {
                inputData = true;
                break;
            }
        }
        /* 入力がある場合のみメッセージボックス表示 */
        if (inputData == true) {
            if (questionMessageBox(getShell(), Messages.TEMPLATE_MESSAGEBOX_CLOSE) == SWT.CANCEL) {
                return false;
            }
        }
        /* nullにしておかないと次回起動時にエラーが発生する */
        okButton = null;
        return super.close();
    }


    @Override
    protected boolean isResizable() {
        return true;
    }
}
