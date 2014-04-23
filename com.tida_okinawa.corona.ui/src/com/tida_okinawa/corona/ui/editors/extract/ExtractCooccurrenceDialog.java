/**
 * @version $Id: ExtractCooccurrenceDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/08 9:47:29
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.tida_okinawa.corona.correction.collocation.CorrectionStringList;
import com.tida_okinawa.corona.correction.parsing.model.AndOperator;
import com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil;
import com.tida_okinawa.corona.correction.parsing.model.Modification;
import com.tida_okinawa.corona.correction.parsing.model.OrOperator;
import com.tida_okinawa.corona.correction.parsing.model.Order;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternKind;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;
import com.tida_okinawa.corona.correction.parsing.model.Sequence;
import com.tida_okinawa.corona.correction.parsing.model.Term;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicName;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * @author s.takuro
 *         #173 パターン自動生成（共起抽出）
 */
public class ExtractCooccurrenceDialog extends Dialog {

    private static final int BTN_CLOSE_ID = 100;
    private static final int BTN_ENTRY_ID = 200;

    private ArrayContentProvider tableContentProvider;
    private CooccurrenceListTableLabelProvider tableLabelProvider;
    private List<IDicName> dicNames;
    private List<ExtractCooccurrenceElement> extractCooccurrenceElement;
    private int cooccurrenceInt;


    /**
     * 共起抽出UI（ダイアログ）
     * 
     * @param parentShell
     *            親シェル
     */
    protected ExtractCooccurrenceDialog(Shell parentShell) {
        super(parentShell);
    }


    /** 新規Shellオブジェクト構成 */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.EXTRACT_COOCCURRENCE_TITLE);
    }


    /**
     * 係り受け抽出一覧のコンテンツ・プロバイダーの設定
     * 
     * @param provider
     *            TableViewerに設定するContentProvider
     */
    public void setTableContentProvider(ArrayContentProvider provider) {
        this.tableContentProvider = provider;
    }


    /**
     * 係り受け抽出一覧のラベル・プロバイダーの設定
     * 
     * @param provider
     *            TableViewerに設定するLabelProvider
     */
    public void setTableLabelProvider(CooccurrenceListTableLabelProvider provider) {
        this.tableLabelProvider = provider;
    }


    /**
     * 全パターン辞書名の設定
     * 
     * @param dicNames
     *            辞書名一覧
     */
    public void setDicName(List<IDicName> dicNames) {
        this.dicNames = dicNames;
    }


    /**
     * 共起語の設定
     * 
     * @param element
     *            共起語リスト
     */
    public void setExtractCooccurrenceElement(List<ExtractCooccurrenceElement> element) {
        this.extractCooccurrenceElement = element;
    }


    /**
     * 共起語数の設定
     * 
     * @param cooccurrenceNum
     *            共起語数
     */
    public void setExtractCount(int cooccurrenceNum) {
        this.cooccurrenceInt = cooccurrenceNum;
    }


    /* ****************************************
     * UI
     */

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
     * 上部の領域のコンポーネント配置
     * 
     * @param parent
     *            親Composite
     */
    @SuppressWarnings("static-method")
    private void setTopComposite(Composite parent) {
        Label labelOutline = new Label(parent, SWT.NONE);
        labelOutline.setText(Messages.EXTRACT_COOCCURRENCE_OUTLINE);
    }


    /**
     * 中央の領域のコンポーネント配置
     * 
     * @param parent
     *            親Composite
     */
    private void setMiddleComposite(Composite parent) {

        /* 上側のコンボボックス（種類）用 Composite作成 */
        Composite compComboType = new Composite(parent, SWT.NONE);
        compComboType.setLayout(new GridLayout());
        compComboType.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        setComboType(compComboType);

        /* 上側のコンボボックス（保存先）用 Composite作成 */
        Composite compComboSaved = new Composite(parent, SWT.NONE);
        compComboSaved.setLayout(new GridLayout());
        compComboSaved.setLayoutData(new GridData(SWT.END, SWT.NONE, false, false));
        setComboSaved(compComboSaved);

        /* 下側のテーブル用のComposite作成 */
        Composite compTable = new Composite(parent, SWT.BORDER);
        compTable.setLayout(new GridLayout());
        GridData layoutTable = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutTable.horizontalSpan = 2;
        layoutTable.widthHint = 800;
        layoutTable.heightHint = 400;
        compTable.setLayoutData(layoutTable);
        setTableList(compTable);
    }


    /* ****************************************
     * Component
     */
    private TableViewer cooccurrenceTableViewer = null;
    private ModifyListener comboModifyListener = null;
    private Combo comboToPatternType = null;
    private Combo comboToSave = null;
    private Button okButton = null;
    private List<TableColumn> tableColumns = null;
    private ExtractCooccurrenceTableSorter tableSorter = null;


    /**
     * 種類選択用コンボ（左）
     * 
     * @param parent
     *            親Composite
     */
    private void setComboType(Composite parent) {
        Composite compCombo = new Composite(parent, SWT.NONE);
        compCombo.setLayout(new GridLayout(2, false));
        compCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
        Label labelCombo = new Label(compCombo, SWT.NONE);
        labelCombo.setText(Messages.EXTRACT_COOCCURRENCE_COMBO_KIND);
        comboToPatternType = new Combo(compCombo, SWT.READ_ONLY);
        comboToPatternType.setItems(createComboItemToPatternType());
        comboModifyListener = getModifyListener();
        comboToPatternType.addModifyListener(getModifyListener());
    }


    /**
     * 保存先選択用コンボ（右）
     * 
     * @param parent
     *            親Composite
     */
    private void setComboSaved(Composite parent) {
        Composite compCombo = new Composite(parent, SWT.NONE);
        compCombo.setLayout(new GridLayout(2, false));
        compCombo.setLayoutData(new GridData(SWT.END, SWT.NONE, false, false));
        Label labelCombo = new Label(compCombo, SWT.NONE);
        labelCombo.setText(Messages.EXTRACT_COOCCURRENCE_COMBO_ENTRY);
        labelCombo.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
        comboToSave = new Combo(compCombo, SWT.READ_ONLY);
        comboToSave.setItems(createComboItemToSave());
        comboToSave.select(0);
    }


    /**
     * 共起語表示用テーブル（下）
     * 
     * @param parent
     *            親Composite
     */
    private void setTableList(Composite parent) {
        cooccurrenceTableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
        cooccurrenceTableViewer.setUseHashlookup(true);
        Table cooccurrenceTable = cooccurrenceTableViewer.getTable();
        GridData gdMorphemeList = new GridData(GridData.FILL, SWT.FILL, true, true);
        cooccurrenceTable.setToolTipText(""); //$NON-NLS-1$
        cooccurrenceTable.setHeaderVisible(true);
        cooccurrenceTable.setLinesVisible(true);
        cooccurrenceTable.setLayoutData(gdMorphemeList);
        cooccurrenceTableViewer.setLabelProvider(tableLabelProvider);
        cooccurrenceTableViewer.setContentProvider(tableContentProvider);
        cooccurrenceTableViewer.setInput(extractCooccurrenceElement);
        cooccurrenceTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                comboModifyListener = null;
                comboToPatternType.setItems(createComboItemToPatternType());
                comboModifyListener = getModifyListener();
            }
        });
        tableSorter = new ExtractCooccurrenceTableSorter();
        cooccurrenceTableViewer.setSorter(tableSorter);

        /* カラム生成 */
        tableColumns = new ArrayList<TableColumn>(100);
        tableColumns.add(createTableColumn(0, cooccurrenceTable, SWT.LEFT, Messages.EXTRACT_COOCCURRENCE_TABLE_COLUMN_KIND, 100));
        tableColumns.add(createTableColumn(1, cooccurrenceTable, SWT.LEFT, Messages.EXTRACT_COOCCURRENCE_TABLE_COLUMN_NUMBER, 100));
        for (int i = 1; i <= cooccurrenceInt; i++) {
            tableColumns.add(createTableColumn(i + 1, cooccurrenceTable, SWT.LEFT, Messages.EXTRACT_COOCCURRENCE_TABLE_COLUMN_TERM + i, 100));
        }
    }


    /**
     * テーブルのカラムの生成
     * 
     * @param pos
     *            カラムの位置
     * @param table
     *            テーブル
     * @param style
     *            スタイル
     * @param title
     *            タイトル
     * @param width
     *            横幅
     * @return TableColumn
     */
    private TableColumn createTableColumn(final int pos, Table table, int style, String title, int width) {
        TableColumn tc = new TableColumn(table, style);
        tc.setText(title);
        tc.setResizable(true);
        tc.setWidth(width);
        tc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tableSorter.setPos(pos);
                tableSorter.changeSortMode();
                cooccurrenceTableViewer.refresh();
            }
        });
        return tc;
    }


    /** ボタン・バーの設定 */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, BTN_ENTRY_ID, Messages.EXTRACT_COOCCURRENCE_BUTTON_ENTER, true);
        okButton.setEnabled(false);
        createButton(parent, BTN_CLOSE_ID, Messages.EXTRACT_COOCCURRENCE_BUTTON_CLOSE, true);
    }


    @Override
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case BTN_ENTRY_ID:
            if (questionMessageBox(getShell(), Messages.EXTRACT_COOCCURRENCE_CONFIRM_CREATE) == SWT.OK) {
                ErrorNumber result = entryPatternAction();
                /* 作成成功時のメッセージは表示させない */
                if (result != ErrorNumber.RESULT_CREATE_OK) {
                    if (result == ErrorNumber.RESULT_NOT_PATTERNDIC) {
                        errorMessageBox(getShell(), Messages.EXTRACT_ERROR_NOT_PATTERNDIC);
                    } else if (result == ErrorNumber.RESULT_NOT_SAVELOCATION) {
                        warningMessageBox(getShell(), Messages.EXTRACT_ERROR_NOT_SAVE_LOCATION);
                    } else if (result == ErrorNumber.RESULT_NOT_CHECK) {
                        warningMessageBox(getShell(), Messages.EXTRACT_ERROR_NOT_CHOOSE);
                    } else {
                        errorMessageBox(getShell(), Messages.EXTRACT_ERROR_NOT_CREATE_PATTERN);
                    }
                }
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
     * Componentサポート
     */

    /**
     * Combo用ModifyListenerの取得
     * 
     * @return ModifyListener
     */
    private ModifyListener getModifyListener() {
        return new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (cooccurrenceTableViewer != null && comboModifyListener != null) {
                    /* 選択中のテーブルアイテムに、Comboで選択したパターンのタイプを設定する */
                    IStructuredSelection ss = (IStructuredSelection) cooccurrenceTableViewer.getSelection();
                    if (ss != null) {
                        Object[] array = ss.toArray();
                        for (Object element : array) {
                            if (element instanceof ExtractCooccurrenceElement) {
                                ((ExtractCooccurrenceElement) element).setPatternType(comboToPatternType.getText());
                            }
                        }
                        cooccurrenceTableViewer.update(array, null);
                    }
                    /* 登録アイテムが選択されているかの判定 */
                    boolean selected = false;
                    for (ExtractCooccurrenceElement element : extractCooccurrenceElement) {
                        if (!("".equals(element.getPatternType()))) { //$NON-NLS-1$
                            selected = true;
                            break;
                        }
                    }
                    if (selected) {
                        okButton.setEnabled(true);
                    } else {
                        okButton.setEnabled(false);
                    }
                }
            }
        };
    }


    /**
     * 保存先用のComboアイテムを生成
     * 
     * @return Comboに表示する文字列
     */
    private String[] createComboItemToSave() {
        if (dicNames == null) {
            return new String[0];
        }
        int cnt = 0;
        String[] comboItem = new String[dicNames.size()];
        /* 辞書アイテムのソート（辞書名で昇順） */
        Collections.sort(dicNames, new Comparator<IDicName>() {
            @Override
            public int compare(IDicName o1, IDicName o2) {
                String s1 = o1.getDicName();
                String s2 = o2.getDicName();
                return s1.compareToIgnoreCase(s2);
            }
        });
        /* 辞書選択コンボにアイテム追加 */
        for (IDicName name : dicNames) {
            comboItem[cnt++] = name.getDicName();
        }
        return comboItem;
    }


    /**
     * パターンの種類選択用のComboアイテムを生成
     * 
     * @return Comboに表示する文字列
     */
    private String[] createComboItemToPatternType() {
        boolean enableModify = true;
        List<String> comboItem = new ArrayList<String>(6);
        /* テーブルの行が選択されている場合 */
        if (cooccurrenceTableViewer != null) {
            IStructuredSelection ss = (IStructuredSelection) cooccurrenceTableViewer.getSelection();
            if (ss != null) {
                Object[] array = ss.toArray();
                for (Object element : array) {
                    /* 選択された単語が3つ以上の場合、係り受けは選択不可 */
                    if (element instanceof ExtractCooccurrenceElement) {
                        if (2 < ((ExtractCooccurrenceElement) element).getSize()) {
                            enableModify = false;
                            break;
                        }
                    }
                }
            }
        }
        comboItem.add(""); //$NON-NLS-1$
        comboItem.add(PatternKind.AND.toString());
        comboItem.add(PatternKind.OR.toString());
        if (enableModify == true) {
            comboItem.add(PatternKind.MODIFICATION.toString());
        }
        comboItem.add(PatternKind.SEQUENCE.toString());
        comboItem.add(PatternKind.ORDER.toString());
        return comboItem.toArray(new String[comboItem.size()]);
    }


    /* ****************************************
     * 登録
     */
    private boolean resultEntry = false;

    private enum ErrorNumber {
        RESULT_CREATE_OK, RESULT_CREATE_NG, RESULT_NOT_PATTERNDIC, RESULT_NOT_SAVELOCATION, RESULT_NOT_CHECK
    }


    /**
     * パターン辞書に登録
     * 
     * @return エラー番号
     */
    private ErrorNumber entryPatternAction() {
        if ("".equals(comboToSave.getText())) { //$NON-NLS-1$
            /* 保存先が未選択の場合 */
            return ErrorNumber.RESULT_NOT_SAVELOCATION;
        }
        /* Comboで選択中の登録先辞書を取得 */
        IDicName dicName = dicNames.get(comboToSave.getSelectionIndex());
        ICoronaDic coronaDic = IoActivator.getService().getDictionary(dicName.getDicId());
        if (!(coronaDic instanceof IPatternDic)) {
            /* パターン辞書の取得に失敗した場合 */
            return ErrorNumber.RESULT_NOT_PATTERNDIC;
        }
        if (entryExec(coronaDic) != true) {
            /* 一つもチェックが入っていなければエラーメッセージを表示してリターン */
            return ErrorNumber.RESULT_NOT_CHECK;
        }
        /* コミット */
        if (coronaDic.commit(new NullProgressMonitor()) != true) {
            return ErrorNumber.RESULT_CREATE_NG;
        }
        /* updateで更新不可なので再セット */
        cooccurrenceTableViewer.setInput(extractCooccurrenceElement);
        okButton.setEnabled(false);
        return ErrorNumber.RESULT_CREATE_OK;
    }


    /**
     * 登録の実行処理
     * 
     * @param coronaDic
     *            登録先辞書
     * @return true:成功、false:失敗（登録アイテムがない）
     */
    private boolean entryExec(final ICoronaDic coronaDic) {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        try {
            dialog.run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.EXTRACT_MESSAGEBOX_TITLE_ENTRY, extractCooccurrenceElement.size() * 6);
                    monitor.subTask(Messages.EXTRACT_MESSAGEBOX_TEXT_EXEC_JUMAN);

                    for (ExtractCooccurrenceElement element : extractCooccurrenceElement) {
                        PatternRecord input = createPatternRecord(element, monitor);
                        if (input != null) {
                            element.setCompletion(true);
                            element.setPatternType(""); //$NON-NLS-1$
                            input.getIPattern().setDirty(true);
                            ((IPatternDic) coronaDic).addItem(input.getIPattern());
                            resultEntry = true;
                        }
                    }
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resultEntry;
    }


    /**
     * パターンレコードの生成
     * 
     * @param ece
     *            係り受け抽出結果
     * @param monitor
     *            プログレス・モニター
     * @return パターンレコード
     */
    private PatternRecord createPatternRecord(IExtractCooccurrenceElement ece, IProgressMonitor monitor) {
        StringBuilder label = new StringBuilder(128);
        PatternRecord record = new PatternRecord();
        Pattern pattern = createPattern(record, ece, monitor);
        if (pattern == null) {
            return null;
        }
        for (int i = 1; i <= ece.getSize(); i++) {
            if (i > 1) {
                label.append(",").append(ece.getTerm(i)); //$NON-NLS-1$
            } else {
                label.append(ece.getTerm(i));
            }
        }
        record.addChild(pattern);
        record.setLabel(label.toString());

        /* レコードをテキストに変換して登録（レコードに子を追加しただけではデータベースに登録されないので） */
        String convertString = DicPatternUtil.convertFrom(record);
        record.getIPattern().setText(convertString);
        monitor.worked(1);
        return record;
    }


    /**
     * パターンを作成する
     * 
     * @param record
     *            パターンレコード
     * @param ece
     *            共起抽出結果
     * @param monitor
     *            プログレス・モニター
     * @return パターン
     */
    private Pattern createPattern(PatternRecord record, IExtractCooccurrenceElement ece, IProgressMonitor monitor) {
        /* パターン生成 */
        String patternType = ece.getPatternType();
        PatternContainer pattern = null;
        if (PatternKind.AND.toString().equals(patternType)) {
            pattern = new AndOperator(record);
        } else if (PatternKind.OR.toString().equals(patternType)) {
            pattern = new OrOperator(record);
        } else if (PatternKind.ORDER.toString().equals(patternType)) {
            pattern = new Order(record);
        } else if (PatternKind.SEQUENCE.toString().equals(patternType)) {
            pattern = new Sequence(record);
        } else if (PatternKind.MODIFICATION.toString().equals(patternType)) {
            pattern = new Modification(record, true);
        } else {
            return null;
        }
        /* Juman実行 */
        List<MorphemeElement[]> morphemeLists = correctionExtractCooccurrence(ece.getTerms(), monitor);
        if (morphemeLists.size() == 0) {
            return null;
        }
        if (pattern instanceof Modification) {
            /* 係り受けの場合は係り元と係り先を追加 */
            createChild(((Modification) pattern).getSource(), morphemeLists.get(0));
            createChild(((Modification) pattern).getDestination(), morphemeLists.get(1));
        } else {
            /* 係り受け以外は単語の数だけ子を追加 */
            for (MorphemeElement[] morphemeResults : morphemeLists) {
                createChild(pattern, morphemeResults);
            }
        }
        monitor.worked(1);
        return pattern;
    }


    /**
     * 共起抽出した語をJuman実行にかける
     * 
     * @param texts
     *            共起抽出語リスト
     * @param monitor
     *            プログレス・モニター
     * @return Juman実行結果
     */
    private static List<MorphemeElement[]> correctionExtractCooccurrence(List<String> texts, IProgressMonitor monitor) {
        CorrectionStringList correctionList = new CorrectionStringList();
        List<MorphemeElement[]> morphemeList = null;
        /* Juman実行 */
        List<String> jumanLists = correctionList.jumanExec(texts, monitor);
        morphemeList = new ArrayList<MorphemeElement[]>(100);
        for (String jumanList : jumanLists) {
            /* Juman実行結果を","単位で区切って格納 */
            String[] jumanResults = jumanList.split(","); //$NON-NLS-1$
            List<MorphemeElement> input = new ArrayList<MorphemeElement>(jumanResults.length);
            for (String jumanResult : jumanResults) {
                /* 先頭が"@"から始まるものは排除 */
                String[] rengo = jumanResult.split(" "); //$NON-NLS-1$
                if (!"@".equals(rengo[0])) { //$NON-NLS-1$
                    input.add(new MorphemeElement(jumanResult));
                }
            }
            morphemeList.add(input.toArray(new MorphemeElement[input.size()]));
        }
        monitor.worked(1);
        monitor.subTask(Messages.EXTRACT_MESSAGEBOX_TEXT_ENTRY_PATTERN);
        return morphemeList;
    }


    /**
     * 子要素の生成
     * （このメソッド自体は作成する単語数が複数の場合にSequenceを生成する）
     * 
     * @param element
     *            親パターン
     * @param morphemes
     *            形態素（本メソッドで呼び出している単語の生成にて使用）
     */
    private void createChild(PatternContainer element, MorphemeElement[] morphemes) {
        /* 複数の形態素を持つ場合はSequenceを生成する（但し、既に親がSequenceの場合を除く） */
        if (morphemes.length > 1 && !(element instanceof Sequence)) {
            Sequence sequence = new Sequence(element);
            element.addChild(sequence);
            /* "文節"に設定 */
            sequence.setScope(SearchScopeType.SEARCH_SEGMENT);
            setTerms(sequence, morphemes);
        } else {
            setTerms(element, morphemes);
        }
    }


    /**
     * 単語の生成
     * 
     * @param element
     *            親パターン
     * @param morphemes
     *            形態素
     */
    @SuppressWarnings("static-method")
    private void setTerms(PatternContainer element, MorphemeElement[] morphemes) {
        for (MorphemeElement morpheme : morphemes) {
            Term term = new Term(element);
            term.setWord(morpheme.getGenkei());
            term.setPart(TermPart.valueOfName(morpheme.getHinshi()));
            term.setWordClass(TermClass.valueOfName(morpheme.getHinshiSaibunrui()));
            element.addChild(term);
        }
    }


    /* ****************************************
     * その他
     */

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
        messageBox.setText(Messages.EXTRACT_MESSAGEBOX_TEXT_QUESTION);
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
        messageBox.setText(Messages.EXTRACT_MESSAGEBOX_TEXT_WARNING);
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
        messageBox.setText(Messages.EXTRACT_MESSAGEBOX_TEXT_ERROR);
        messageBox.setMessage(message);
        messageBox.open();
    }


    @Override
    protected boolean isResizable() {
        return true;
    }
}
