/**
 * @version $Id: ExtractRelationDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/07 15:48:24
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
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

import com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil;
import com.tida_okinawa.corona.correction.parsing.model.Modification;
import com.tida_okinawa.corona.correction.parsing.model.ModificationElement;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
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
 *         #177 パターン自動生成（係り受け抽出）
 */
public class ExtractRelationDialog extends Dialog {

    private final int BTN_CLOSE_ID = 100;
    private final int BTN_ENTRY_ID = 200;

    private ArrayContentProvider morphemeListTableContentProvider = null;
    private IMorphemeListTableLabelProvider morphemeListTableLabelProvider = null;
    private ArrayContentProvider morphemeDetailTableContentProvider = null;
    private IMorphemeDetailTableLabelProvider morphemeDetailTableLabelProvider = null;
    private List<IDicName> dicNames = null;


    /**
     * 係り受け抽出UI（ダイアログ）
     * 
     * @param parentShell
     *            親シェル
     */
    public ExtractRelationDialog(Shell parentShell) {
        super(parentShell);
        prevDetailMap = new HashMap<String, IExtractRelationElement>();
    }


    /** 新規Shellオブジェクト構成 */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.EXTRACT_RELATION_TITLE);
    }


    /**
     * 係り受け抽出一覧のコンテンツ・プロバイダーの設定
     * 
     * @param provider
     *            TableViewerに設定するContentProvider
     */
    public void setMorphemeListTableContentProvider(ArrayContentProvider provider) {
        this.morphemeListTableContentProvider = provider;
    }


    /**
     * 係り受け抽出一覧のラベル・プロバイダーの設定
     * 
     * @param provider
     *            TableViewerに設定するLabelProvider
     */
    public void setMorphemeListTableLabelProvider(IMorphemeListTableLabelProvider provider) {
        this.morphemeListTableLabelProvider = provider;
    }


    /**
     * 係り受け抽出詳細のコンテンツ・プロバイダーの設定
     * 
     * @param provider
     *            TreeViewerに設定するContentProvider
     */
    public void setMorphemeDetailTreeContentProvider(ArrayContentProvider provider) {
        this.morphemeDetailTableContentProvider = provider;
    }


    /**
     * 係り受け抽出詳細のラベル・プロバイダーの設定
     * 
     * @param provider
     *            TreeViewerに設定するLabelProvider
     */
    public void setMorphemeDetailTableLabelProvider(IMorphemeDetailTableLabelProvider provider) {
        this.morphemeDetailTableLabelProvider = provider;
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
     * 係り受け抽出結果の設定
     * 
     * @param extractRelationList
     *            係り受け抽出結果
     */
    public void setExtractRelationList(IExtractRelationElement[] extractRelationList) {
        this.extractRelationList = extractRelationList;
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
        labelOutline.setText(Messages.EXTRACT_RELATION_OUTLINE);
    }


    /**
     * 中央の領域のコンポーネント配置
     * 
     * @param parent
     *            親Composite
     */
    private void setMiddleComposite(Composite parent) {
        /* 上側のコンボボックス用のComposite作成 */
        Composite compCombo = new Composite(parent, SWT.NONE);
        compCombo.setLayout(new GridLayout());
        GridData layoutCombo = new GridData(SWT.END, SWT.NONE, false, false);
        layoutCombo.horizontalSpan = 2;
        compCombo.setLayoutData(layoutCombo);
        setComboSaved(compCombo);

        /* 左側のテーブル用のComposite作成 */
        Composite compTableResult = new Composite(parent, SWT.NONE);
        compTableResult.setLayout(new GridLayout());
        GridData layoutLeft = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutLeft.heightHint = 400;
        compTableResult.setLayoutData(layoutLeft);
        Label labelResult = new Label(compTableResult, SWT.NONE);
        labelResult.setText(Messages.EXTRACT_RERATION_LABEL_RESULT);
        setMorphemeListTable(compTableResult);

        /* 右側のテーブル用のComposite作成 */
        Composite compTableDetail = new Composite(parent, SWT.NONE);
        compTableDetail.setLayout(new GridLayout());
        GridData layoutRight = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutRight.heightHint = 400;
        compTableDetail.setLayoutData(layoutRight);
        Label labelDetail = new Label(compTableDetail, SWT.NONE);
        labelDetail.setText(Messages.EXTRACT_RERATION_LABEL_DETAIL);
        setMorphemeDetailTree(compTableDetail);
    }


    /* ****************************************
     * Component
     */
    private TableViewer morphemeListTableViewer = null;
    private TableViewer morphemeDetailTableViewer = null;
    private Combo comboToSave = null;
    private Button okButton = null;


    /**
     * 保存先選択用コンボ
     * 
     * @param parent
     *            親Composite
     */
    private void setComboSaved(Composite parent) {
        Composite compCombo = new Composite(parent, SWT.NONE);
        compCombo.setLayout(new GridLayout(2, false));
        compCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        Label labelCombo = new Label(compCombo, SWT.NONE);
        labelCombo.setText(Messages.EXTRACT_RELATION_COMBO_ENTRY);
        comboToSave = new Combo(compCombo, SWT.READ_ONLY);
        comboToSave.setItems(createComboItem());
        comboToSave.select(0);
    }


    /**
     * 係り受け一覧表示用テーブル
     * 
     * @param parent
     *            親Composite
     */
    private void setMorphemeListTable(Composite parent) {
        morphemeListTableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.VIRTUAL | SWT.BORDER);
        Table morphemeListTable = morphemeListTableViewer.getTable();
        GridData gdMorphemeList = new GridData(GridData.FILL, SWT.FILL, true, true);
        morphemeListTable.setToolTipText(""); //$NON-NLS-1$
        morphemeListTable.setHeaderVisible(true);
        morphemeListTable.setLinesVisible(true);
        morphemeListTable.setLayoutData(gdMorphemeList);

        createTableColumn(morphemeListTable, SWT.LEFT, Messages.EXTRACT_RELATION_TABLE_COLUMN_NUMBER, 70);
        createTableColumn(morphemeListTable, SWT.LEFT, Messages.EXTRACT_RELATION_TABLE_COLUMN_RECENT, 180);
        createTableColumn(morphemeListTable, SWT.LEFT, Messages.EXTRACT_RELATION_TABLE_COLUMN_DEST, 180);

        morphemeListTableViewer.setLabelProvider(morphemeListTableLabelProvider);
        morphemeDetailTableLabelProvider.setPrevDetailMap(prevDetailMap);
        morphemeListTableViewer.setContentProvider(morphemeListTableContentProvider);
        morphemeListTableViewer.setInput(extractRelationList);
        morphemeListTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            IStructuredSelection prevSelection = new StructuredSelection();


            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection) event.getSelection();
                    if (!prevSelection.equals(ss)) {
                        Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
                        if (o instanceof IExtractRelationElement) {
                            ExtractRelationElement ere = (ExtractRelationElement) o;
                            ExtractRelation extractRelation = new ExtractRelation();
                            extractRelationDetail = extractRelation.createRelationDetail(ere);
                            morphemeDetailTableLabelProvider.setPrevDetailMap(prevDetailMap);
                            morphemeDetailTableViewer.setInput(extractRelationDetail);
                            prevSelection = ss;
                            selectedRelation = ere;
                            /* 詳細画面が切り替わるので登録ボタンも無効化 */
                            okButton.setEnabled(false);
                        }
                    }
                }
            }
        });
    }


    /**
     * 係り受け詳細表示用ツリー
     * 
     * @param parent
     *            親Composite
     */
    private void setMorphemeDetailTree(Composite parent) {
        morphemeDetailTableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.CHECK | SWT.BORDER);
        GridData gdMorphemeDetail = new GridData(GridData.FILL, SWT.FILL, true, true);
        Table morphemeDetailTable = morphemeDetailTableViewer.getTable();
        morphemeDetailTable.setToolTipText(""); //$NON-NLS-1$
        morphemeDetailTable.setLayoutData(gdMorphemeDetail);
        morphemeDetailTable.setHeaderVisible(true);
        morphemeDetailTable.setLinesVisible(true);

        createTableColumn(morphemeDetailTable, SWT.LEFT, "", 30); //$NON-NLS-1$
        createTableColumn(morphemeDetailTable, SWT.LEFT, Messages.EXTRACT_RELATION_TABLE_COLUMN_RECENT, 200);
        createTableColumn(morphemeDetailTable, SWT.LEFT, Messages.EXTRACT_RELATION_TABLE_COLUMN_DEST, 200);

        morphemeDetailTableViewer.setLabelProvider(morphemeDetailTableLabelProvider);
        morphemeDetailTableViewer.setContentProvider(morphemeDetailTableContentProvider);
        morphemeDetailTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                /* 登録アイテムが選択されているかの判定 */
                boolean selected = false;
                Table table = morphemeDetailTableViewer.getTable();
                for (int i = 0; i < table.getItemCount(); i++) {
                    if (table.getItem(i).getChecked()) {
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
        });
    }


    /**
     * テーブルのカラムの生成
     * 
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
    @SuppressWarnings("static-method")
    private TableColumn createTableColumn(Table table, int style, String title, int width) {
        TableColumn tc = new TableColumn(table, style);
        tc.setText(title);
        tc.setResizable(true);
        tc.setWidth(width);
        return tc;
    }


    /** ボタン・バーの設定 */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, BTN_ENTRY_ID, Messages.EXTRACT_RELATION_BUTTON_ENTRY, true);
        okButton.setEnabled(false);
        createButton(parent, BTN_CLOSE_ID, Messages.EXTRACT_RELATION_BUTTON_CLOSE, true);
    }


    @Override
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case BTN_ENTRY_ID:
            if (questionMessageBox(getShell(), Messages.EXTRACT_RELATION_CREATE_CHECK) == SWT.OK) {
                ErrorNumber result = entryPatternAction();
                if (result == ErrorNumber.RESULT_CREATE_OK) {
                    morphemeListTableViewer.update(extractRelationList, null);
                    morphemeDetailTableViewer.update(extractRelationDetail, null);
                } else if (result == ErrorNumber.RESULT_NOT_PATTERNDIC) {
                    errorMessageBox(getShell(), Messages.EXTRACT_ERROR_NOT_PATTERNDIC);
                } else if (result == ErrorNumber.RESULT_NOT_SAVELOCATION) {
                    warningMessageBox(getShell(), Messages.EXTRACT_ERROR_NOT_SAVE_LOCATION);
                } else if (result == ErrorNumber.RESULT_NOT_CHECK) {
                    warningMessageBox(getShell(), Messages.EXTRACT_ERROR_NOT_CHOOSE);
                } else {
                    errorMessageBox(getShell(), Messages.EXTRACT_ERROR_NOT_CREATE_PATTERN);
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
     * 登録
     */

    private enum ErrorNumber {
        RESULT_CREATE_OK, RESULT_CREATE_NG, RESULT_NOT_PATTERNDIC, RESULT_NOT_SAVELOCATION, RESULT_NOT_CHECK
    }

    /* 登録時に、登録された要素を持つリストの色を変更させる為に使用 */
    Map<String, IExtractRelationElement> prevDetailMap = null;
    private IExtractRelationElement selectedRelation = null;
    private IExtractRelationElement[] extractRelationList = null;
    private IExtractRelationElement[] extractRelationDetail = null;


    /* パターン辞書に登録 */
    private ErrorNumber entryPatternAction() {
        if ("".equals(comboToSave.getText())) { //$NON-NLS-1$
            /* 保存先が未選択の場合 */
            return ErrorNumber.RESULT_NOT_SAVELOCATION;
        }
        boolean chk = false;
        /* Comboで選択中の登録先辞書を取得 */
        IDicName dicName = dicNames.get(comboToSave.getSelectionIndex());
        ICoronaDic coronaDic = IoActivator.getService().getDictionary(dicName.getDicId());
        if (!(coronaDic instanceof IPatternDic)) {
            /* パターン辞書の取得に失敗した場合 */
            return ErrorNumber.RESULT_NOT_PATTERNDIC;
        }
        Table table = morphemeDetailTableViewer.getTable();
        /* チェックの入った項目をパターンレコード一覧に追加 */
        for (int i = 0; i < table.getItemCount(); i++) {
            if (table.getItem(i).getChecked()) {
                IExtractRelationElement ereDetail = extractRelationDetail[i];
                PatternRecord input = createPatternRecord(ereDetail);
                if (input != null) {
                    chk = true;
                    /* 登録パターンに設定する */
                    input.getIPattern().setDirty(true);
                    ((IPatternDic) coronaDic).addItem(input.getIPattern());
                    /* 登録済みアイテムに設定する */
                    selectedRelation.setCompletion(true);
                    ereDetail.setCompletion(true);
                    StringBuilder ereHyouki = new StringBuilder(100);
                    ereHyouki.append(ereDetail.getHyouki()).append(" ").append(ereDetail.getDependDestination().getHyouki());
                    if (!prevDetailMap.containsKey(ereHyouki.toString())) {
                        prevDetailMap.put(ereHyouki.toString(), ereDetail);
                    }
                    /* 登録した係り受けはチェックを外す（同時に登録ボタンも無効化） */
                    table.getItem(i).setChecked(false);
                    okButton.setEnabled(false);
                }
            }
        }
        if (!chk) {
            /* 一つもチェックが入っていなければエラーメッセージを表示してリターン */
            return ErrorNumber.RESULT_NOT_CHECK;
        }
        /* コミット */
        if (coronaDic.commit(new NullProgressMonitor()) != true) {
            return ErrorNumber.RESULT_CREATE_NG;
        }
        return ErrorNumber.RESULT_CREATE_OK;
    }


    /**
     * パターンレコードの生成
     * 
     * @param ere
     *            係り受け抽出した要素
     * @return パターンレコード
     */
    private PatternRecord createPatternRecord(IExtractRelationElement ere) {
        /* 係り受けを持つパターンの作成 */
        String name = ere.getHyouki() + ere.getDependDestination().getHyouki();
        PatternRecord record = new PatternRecord(IoActivator.getDicFactory().createPattern(name, "", -99, false)); //$NON-NLS-1$
        Modification modification = new Modification(record, true);
        record.addChild(modification);
        /* 係り元／係り先の作成 */
        createMorphemeElement(modification.getSource(), ere);
        createMorphemeElement(modification.getDestination(), ere.getDependDestination());
        /* レコードをテキストに変換して登録（レコードに子を追加しただけではデータベースに登録されないので） */
        String convertString = DicPatternUtil.convertFrom(record);
        record.getIPattern().setText(convertString);
        return record;
    }


    /**
     * 形態素の要素を作成
     * 
     * @param modificationElement
     *            係り元、係り先パターンを表すクラス
     * @param ere
     *            係り受け抽出した要素
     */
    private void createMorphemeElement(ModificationElement modificationElement, IExtractRelationElement ere) {
        List<MorphemeElement> morphemes = ere.getMorphemes();
        PatternContainer sequence = null;

        /* 形態素が0個の場合は、ワイルドカードなので空の単語を追加 */
        if (morphemes.size() == 0) {
            Term empty = new Term(modificationElement);
            modificationElement.addChild(empty);
            return;
        }
        /* 形態素が1個の場合 */
        else if (morphemes.size() == 1) {
            for (MorphemeElement morpheme : morphemes) {
                if (morpheme != null) {
                    Term single = new Term(modificationElement);
                    createTermElement(single, morpheme);
                    modificationElement.addChild(single);
                    return;
                }
            }
        }
        /* 形態素が1個より多い場合 */
        else {
            /* 連続の要素を追加 */
            sequence = new Sequence(modificationElement);
            modificationElement.addChild(sequence);
            /* "文節"に設定 */
            ((Sequence) sequence).setScope(SearchScopeType.SEARCH_SEGMENT);
            for (MorphemeElement morpheme : morphemes) {
                Term multi = new Term(sequence);
                /* nullの場合は空の単語を追加する */
                if (morpheme != null) {
                    createTermElement(multi, morpheme);
                }
                sequence.addChild(multi);
            }
        }
    }


    /**
     * 単語（Word）の要素を追加
     * 
     * @param term
     *            単語
     * @param morpheme
     *            係り受け
     */
    @SuppressWarnings("static-method")
    private void createTermElement(Term term, MorphemeElement morpheme) {
        term.setWord(morpheme.getGenkei());
        term.setPart(TermPart.valueOfName(morpheme.getHinshi()));
        term.setWordClass(TermClass.valueOfName(morpheme.getHinshiSaibunrui()));
        term.setHitElement(morpheme);
    }


    /* ****************************************
     * その他
     */

    /* Comboのアイテムを生成 */
    private String[] createComboItem() {
        if (dicNames == null) {
            return null;
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


    /* メッセージボックス（確認用） */
    private static int questionMessageBox(Shell shell, String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
        messageBox.setText(Messages.EXTRACT_MESSAGEBOX_TEXT_QUESTION);
        messageBox.setMessage(message);
        return messageBox.open();
    }


    /* メッセージボックス（要注意） */
    private static void warningMessageBox(Shell shell, String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
        messageBox.setText(Messages.EXTRACT_MESSAGEBOX_TEXT_WARNING);
        messageBox.setMessage(message);
        messageBox.open();
    }


    /* メッセージボックス（エラー） */
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
