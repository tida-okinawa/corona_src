/**
 * @version $Id: TemplateModifyDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/21 11:45:00
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.correction.template.ITemplateTermType;
import com.tida_okinawa.corona.correction.template.TemplateContainer;
import com.tida_okinawa.corona.correction.template.TemplateLink;
import com.tida_okinawa.corona.correction.template.TemplateRecord;
import com.tida_okinawa.corona.correction.template.TemplateTerm;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.ITemplateItem;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateModifyDialog extends Dialog {

    private static final int BTN_CLOSE_ID = 100;
    private static final int BTN_ENTRY_ID = 200;

    private String strTitle = null;
    private Object[] expandedNodes = null;
    private TemplateRecords records;
    private TemplateRecord[] record;
    private TemplateTreeLabelProvider treeLabelProvider = null;
    private TemplateTreeContentProvider treeContentProvider = null;
    private boolean setModifyMode = false;


    /**
     * ひな型登録用ダイアログ
     * 
     * @param parentShell
     *            親シェル
     */
    public TemplateModifyDialog(Shell parentShell) {
        super(parentShell);
    }


    /** 新規Shellオブジェクト構成 */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(strTitle);
    }


    /**
     * @param object
     *            ひな型に登録する情報<br/>
     *            （null/PatternRecord/TemplateRecord/ITemplateItem）
     */
    public void setInput(Object object) {
        records = new TemplateRecords(IoActivator.getTemplateFactory());
        /* ひな型レコード生成 */
        TemplateRecord template = new TemplateRecord(object);
        if (setModifyMode == true) {
            template.setId(((TemplateRecord) object).getId());
        }
        this.record = createRecords(template);

        /* 追加アイテム（addItem）に参照のパターンを追加 */
        if (object instanceof Pattern) {
            addPatternLink((Pattern) object);
        }
    }


    /**
     * レコード作成
     * 
     * @param template
     *            ひな型
     * @return ひな型を格納した配列
     */
    private static TemplateRecord[] createRecords(TemplateContainer template) {
        Set<TemplateRecord> ret = new HashSet<TemplateRecord>(1);
        ret.add((TemplateRecord) template);
        return ret.toArray(new TemplateRecord[ret.size()]);
    }


    /**
     * ひな型の編集かどうか
     * 
     * @param setModifyMode
     *            true ：編集<br/>
     *            false：登録（新規）
     */
    public void setExist(boolean setModifyMode) {
        this.setModifyMode = setModifyMode;
        if (setModifyMode != true) {
            this.strTitle = Messages.TEMPLATE_MODIFY_ENTRY_TITLE;
        } else {
            this.strTitle = Messages.TEMPLATE_MODIFY_EDIT_TITLE;
        }
    }


    /**
     * @param provider
     *            TreeViewに設定するContentProvider
     */
    public void setTreeContentProvider(TemplateTreeContentProvider provider) {
        this.treeContentProvider = provider;
    }


    /**
     * @param provider
     *            TreeViewに設定するLabelProvider
     */
    public void setTreeLabelProvider(TemplateTreeLabelProvider provider) {
        this.treeLabelProvider = provider;
    }


    /* ****************************************
     * UI
     */
    private Composite compComponent = null;
    private TreeViewer viewer = null;
    private Text textName = null;
    private Button radioTermFix = null;
    private Button radioTermChg = null;
    private Button radioLinkFix = null;
    private Button radioLinkChg = null;
    private Button radioWord = null;
    private Button radioLabel = null;
    private Composite compName = null;
    private Composite compTerm = null;
    private Composite compRefer = null;
    private StackLayout stackLayout = null;


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
     *            親要素
     */
    private void setTopComposite(Composite parent) {
        Label labelTop = new Label(parent, SWT.NONE);
        if (setModifyMode != true) {
            labelTop.setText(Messages.TEMPLATE_MODIFY_ENTRY_OUTLINE);
        } else {
            labelTop.setText(Messages.TEMPLATE_MODIFY_EDIT_OUTLINE);
        }
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
        compComponent = new Composite(parent, SWT.NONE);
        compComponent.setLayout(new GridLayout());
        GridData layoutRight = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutRight.widthHint = 400;
        layoutRight.heightHint = 400;
        compComponent.setLayoutData(layoutRight);
        setRightComposite(compComponent);
    }


    /**
     * マスター左部の中身
     * 
     * @param parent
     *            親Composite
     */
    private void setLeftComposite(Composite parent) {
        Tree tree = new Tree(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer = new TreeViewer(tree);
        viewer.setContentProvider(treeContentProvider);
        viewer.setLabelProvider(treeLabelProvider);
        if (record != null) {
            viewer.setInput(record);
        }
        /* TableViewer内の項目を切り替えた際に呼ばれるリスナー */
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setComponentActions();
            }
        });
    }


    /**
     * マスター右部の中身<br>
     * 左部のTreeで選択されている要素によって表示を切り替える
     * 
     * @param parent
     *            親Composite
     */
    private void setRightComposite(Composite parent) {
        stackLayout = new StackLayout();
        parent.setLayout(stackLayout);
        setRightCompName(parent);
        setRightCompTerm(parent);
        setRightCompRefer(parent);
        stackLayout.topControl = null;
        parent.layout();
    }


    /**
     * ひな型名選択時の右部のコンポーネント
     * 
     * @param parent
     *            親Composite
     */
    private void setRightCompName(Composite parent) {
        compName = new Composite(parent, SWT.NONE);
        compName.setLayout(new GridLayout());
        Label label = new Label(compName, SWT.NONE);
        label.setText(Messages.TEMPLATE_MODIFY_EXPLAIN_NAME);
        textName = new Text(compName, SWT.SINGLE | SWT.BORDER);
        textName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        textName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                setText();
            }
        });
    }


    /**
     * 単語（Term）選択時の右部のコンポーネント
     * 
     * @param parent
     *            親Composite
     */
    private void setRightCompTerm(Composite parent) {
        compTerm = new Composite(parent, SWT.NONE);
        compTerm.setLayout(new GridLayout());

        Label label = new Label(compTerm, SWT.NONE);
        label.setText(Messages.TEMPLATE_MODIFY_EXPLAIN_TERM);

        final Group groupTermStat = new Group(compTerm, SWT.NONE);
        groupTermStat.setLayout(new GridLayout());
        groupTermStat.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        groupTermStat.setText(Messages.TEMPLATE_STATE_STRING);
        radioTermFix = new Button(groupTermStat, SWT.RADIO);
        radioTermFix.setText(Messages.TEMPLATE_STATE_STRING_FIX);
        radioTermFix.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setStateFix();
            }
        });
        radioTermChg = new Button(groupTermStat, SWT.RADIO);
        radioTermChg.setText(Messages.TEMPLATE_STATE_STRING_CHG);
        radioTermChg.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setStateChg();
            }
        });
        final Group groupTermType = new Group(compTerm, SWT.NONE);
        groupTermType.setLayout(new GridLayout());
        groupTermType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        groupTermType.setText(Messages.TEMPLATE_TYPE_STRING);
        radioWord = new Button(groupTermType, SWT.RADIO);
        radioWord.setText(Messages.TEMPLATE_TYPE_STRING_WORD);
        radioWord.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setTypeWord();
            }
        });
        radioLabel = new Button(groupTermType, SWT.RADIO);
        radioLabel.setText(Messages.TEMPLATE_TYPE_STRING_LABEL);
        radioLabel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setTypeLabel();
            }
        });
    }


    /**
     * 参照（Link）選択時の右部のコンポーネント
     * 
     * @param parent
     *            親Composite
     */
    private void setRightCompRefer(Composite parent) {
        compRefer = new Composite(parent, SWT.NONE);
        compRefer.setLayout(new GridLayout());
        Label label = new Label(compRefer, SWT.NONE);
        label.setText(Messages.TEMPLATE_MODIFY_EXPLAIN_LINK);
        final Group groupReferStat = new Group(compRefer, SWT.NONE);

        groupReferStat.setLayout(new GridLayout());
        groupReferStat.setLayout(new GridLayout());
        groupReferStat.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        groupReferStat.setText(Messages.TEMPLATE_STATE_STRING);
        radioLinkFix = new Button(groupReferStat, SWT.RADIO);
        radioLinkFix.setText(Messages.TEMPLATE_STATE_STRING_FIX);
        radioLinkFix.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setStateFix();
            }
        });
        radioLinkChg = new Button(groupReferStat, SWT.RADIO);
        radioLinkChg.setText(Messages.TEMPLATE_STATE_STRING_CHG);
        radioLinkChg.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setStateChg();
            }
        });
    }


    /* ボタン・バーの設定 */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (setModifyMode != true) {
            createButton(parent, BTN_ENTRY_ID, Messages.TEMPLATE_BUTTON_ENTRY, true);
            createButton(parent, BTN_CLOSE_ID, Messages.TEMPLATE_BUTTON_CLOSE, true);
        } else {
            createButton(parent, BTN_ENTRY_ID, Messages.TEMPLATE_BUTTON_OK, true);
            createButton(parent, BTN_CLOSE_ID, Messages.TEMPLATE_BUTTON_CANCEL, true);
        }
    }


    @Override
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case BTN_ENTRY_ID:
            entryTemplateAction();
            /* 編集の場合はダイアログを閉じる */
            if (setModifyMode == true) {
                setReturnCode(BTN_ENTRY_ID);
                close();
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

    /** コンポーネントの表示切替　 */
    private void setComponentActions() {
        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
        Object object = ss.getFirstElement();

        /* ラジオボタンの設定 */
        setRadioStateChk(object);

        /* 先頭に持ってくるコントロールの設定 */
        if (object instanceof TemplateRecord) {
            stackLayout.topControl = compName;
            setTemplateName();
        } else if (object instanceof TemplateLink) {
            stackLayout.topControl = compRefer;
        } else if (object instanceof TemplateTerm) {
            stackLayout.topControl = compTerm;
        } else {
            stackLayout.topControl = null;
        }
        compComponent.layout();
    }


    /**
     * ラジオボタンの設定（固定or可変）
     * 
     * @param object
     *            選択中の要素
     */
    private void setRadioStateChk(Object object) {
        if (object instanceof TemplateTerm) {
            TemplateTerm term = (TemplateTerm) object;
            /* 状態が固定の場合 */
            if (term.getFixCheck() == true) {
                radioTermFix.setSelection(true);
                radioTermChg.setSelection(false);
                radioWord.setEnabled(false);
                radioLabel.setEnabled(false);
            }
            /* 状態が可変の場合 */
            else {
                radioTermFix.setSelection(false);
                radioTermChg.setSelection(true);
                radioWord.setEnabled(true);
                radioLabel.setEnabled(true);
            }
            /* 単語の場合は種類（Word or Label)の判定も行う */
            setRadioTypeChk(term);

        } else if (object instanceof TemplateLink) {
            if (((TemplateLink) object).getFixCheck() == true) {
                radioLinkFix.setSelection(true);
                radioLinkChg.setSelection(false);
            } else {
                radioLinkFix.setSelection(false);
                radioLinkChg.setSelection(true);
            }
        } else {
            /* ラジオボタンは表示されないが念のためfalseに設定 */
            radioTermFix.setSelection(false);
            radioTermChg.setSelection(false);
            radioLinkFix.setSelection(false);
            radioLinkChg.setSelection(false);
        }
    }


    /**
     * ラジオボタンの設定（単語(word)orラベル(label)）
     * 
     * @param term
     *            単語（Term）
     */
    private void setRadioTypeChk(TemplateTerm term) {

        String state = term.getState();
        /*
         * nullの場合はWordとLabelに文字列が格納されているかどうかによって判定を行う。
         * （Wordに文字列が格納されている場合の優先度高）
         */
        if (state == null) {
            /* 単語（Word）に文字列が格納されているかをチェック */
            if (term.getWord() != null) {
                radioWord.setSelection(true);
                radioLabel.setSelection(false);
                term.setState(ITemplateTermType.TYPE_WORD);
            }
            /* ラベル（Label）に文字列が格納されているかをチェック */
            else if (term.getLabel() != null) {
                radioWord.setSelection(false);
                radioLabel.setSelection(true);
                term.setState(ITemplateTermType.TYPE_LABEL);
            }
            /* 上記のどちらでもない場合 */
            else {
                radioWord.setSelection(false);
                radioLabel.setSelection(false);
                term.setState(ITemplateTermType.TYPE_NULL);
            }
        }
        /* 種類が単語（Word）の場合 */
        else if ((ITemplateTermType.TYPE_WORD).equals(state)) {
            radioWord.setSelection(true);
            radioLabel.setSelection(false);
        }
        /* 種類がラベル（Label）の場合 */
        else if ((ITemplateTermType.TYPE_LABEL).equals(state)) {
            radioWord.setSelection(false);
            radioLabel.setSelection(true);
        }
        /* 上記のどちらでもない場合 */
        else {
            radioWord.setSelection(false);
            radioLabel.setSelection(false);
        }
    }


    /** ラジオボタンのチェック切替（可変から固定） */
    private void setStateFix() {
        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
        Object object = ss.getFirstElement();

        /* 単語の場合 */
        if (object instanceof TemplateTerm) {
            TemplateTerm term = (TemplateTerm) object;
            if (radioTermFix.getSelection() == true) {
                term.setFixCheck(true);
                /* 種類を非活性化 */
                radioWord.setEnabled(false);
                radioLabel.setEnabled(false);
                /* 展開状態を保持して再表示 */
                expandedNodes = viewer.getExpandedElements();
                /* updateでは更新されない */
                viewer.setInput(record);
                viewer.setExpandedElements(expandedNodes);
            }
        }
        /* 参照の場合 */
        else if (object instanceof TemplateLink) {
            TemplateLink link = (TemplateLink) object;
            if (radioLinkFix.getSelection() == true) {
                link.setFixCheck(true);
                /* 展開状態を保持して再表示 */
                expandedNodes = viewer.getExpandedElements();
                /* updateでは更新されない */
                viewer.setInput(record);
                viewer.setExpandedElements(expandedNodes);
            }
        }
    }


    /** ラジオボタンのチェック切替（固定から可変） */
    private void setStateChg() {
        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
        Object object = ss.getFirstElement();

        /* 単語の場合 */
        if (object instanceof TemplateTerm) {
            TemplateTerm term = (TemplateTerm) object;
            if (radioTermChg.getSelection() == true) {
                term.setFixCheck(false);
                /* 種類を活性化 */
                radioWord.setEnabled(true);
                radioLabel.setEnabled(true);
                /* 展開状態を保持して再表示 */
                expandedNodes = viewer.getExpandedElements();
                /* updateでは更新されない */
                viewer.setInput(record);
                viewer.setExpandedElements(expandedNodes);
            }
        }
        /* 参照の場合 */
        else if (object instanceof TemplateLink) {
            TemplateLink link = (TemplateLink) object;
            if (radioLinkChg.getSelection() == true) {
                link.setFixCheck(false);
                /* 展開状態を保持して再表示 */
                expandedNodes = viewer.getExpandedElements();
                /* updateでは更新されない */
                viewer.setInput(record);
                viewer.setExpandedElements(expandedNodes);
            }
        }
    }


    /** ラジオボタンのチェック切替（単語） */
    private void setTypeWord() {
        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
        Object object = ss.getFirstElement();

        if (object instanceof TemplateTerm) {
            if (radioWord.getSelection() == true) {
                ((TemplateTerm) object).setState(ITemplateTermType.TYPE_WORD);
                /* 展開状態を保持して再表示 */
                expandedNodes = viewer.getExpandedElements();
                viewer.update(record, null);
                viewer.setExpandedElements(expandedNodes);
            }
        }
    }


    /** ラジオボタンのチェック切替（ラベル） */
    private void setTypeLabel() {
        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
        Object object = ss.getFirstElement();

        if (object instanceof TemplateTerm) {
            if (radioLabel.getSelection() == true) {
                ((TemplateTerm) object).setState(ITemplateTermType.TYPE_LABEL);
                /* 展開状態を保持して再表示 */
                expandedNodes = viewer.getExpandedElements();
                /* updateでは更新されない */
                viewer.setInput(record);
                viewer.setExpandedElements(expandedNodes);
            }
        }
    }


    /** ひな型管理テーブルへの登録 */
    private void entryTemplateAction() {
        for (TemplateRecord input : record) {
            /* 登録の場合は、連続でひな型を作成できるようにここでIDを初期化しておく */
            if (setModifyMode != true) {
                input.setId(ITemplateItem.DEFAULT_ID);
            }
            records.add(input);
        }
        /* コミット */
        if (IoActivator.getTemplateFactory().commit(null) != true) {
            errorMessageBox(getShell(), strTitle + Messages.TEMPLATE_MESSAGEBOX_ERROR_STRING);
        } else {
            /* 登録できたかどうかの判断ができないので、登録完了時はメッセージを表示する */
            if (setModifyMode != true) {
                okMessageBox(getShell(), strTitle + Messages.TEMPLATE_MESSAGEBOX_TITLE_OK, strTitle + Messages.TEMPLATE_MESSAGEBOX_TEXT_OK);
            }
        }
    }


    /**
     * 参照のパターンを再帰的に追加
     * 
     * @param element
     *            パターン
     */
    private void addPatternLink(Pattern element) {
        if (element == null) {
            new Exception(Messages.TEMPLATE_EXCEPTION_NULL).printStackTrace();
        }

        /* 要素が参照の場合はひな型管理テーブルに追加 */
        if (element instanceof Link) {
            IPattern iPattern = ((Link) element).getLinkPattern();
            if (iPattern != null) {
                /* 参照のパターンを取得 */
                PatternRecord pattern = new PatternRecord(iPattern);
                /* ひな型に登録するアイテムに追加 */
                TemplateRecord template = new TemplateRecord(pattern);
                template.setId(records.checkItems(pattern.getId()));
                template.setTemplateId(pattern.getId());
                template.setPart(true);
                records.add(template);
            }
        }

        if (element instanceof PatternContainer) {
            List<Pattern> children = ((PatternContainer) element).getChildren();
            for (Pattern p : children) {
                addPatternLink(p);
            }
        }
    }


    /* ****************************************
     * 表示用
     */

    /** ひな型名をTextに表示 */
    private void setTemplateName() {
        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
        Object object = ss.getFirstElement();
        if (object instanceof TemplateRecord) {
            textName.setText(((TemplateRecord) object).getName());
        }
    }


    /** Textの内容をひな型名に登録 */
    private void setText() {
        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
        Object object = ss.getFirstElement();
        if (object instanceof TemplateRecord) {
            ((TemplateRecord) object).setName(textName.getText());

            /* 展開状態を保持して再表示 */
            expandedNodes = viewer.getExpandedElements();
            viewer.update(record, null);
            viewer.setExpandedElements(expandedNodes);
        }
    }


    /**
     * メッセージボックス（OKボタンのみ）
     * 
     * @param shell
     *            親Shell
     * @param title
     *            タイトル
     * @param message
     *            メッセージ
     */
    private static void okMessageBox(Shell shell, String title, String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.OK);
        messageBox.setText(title);
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
    protected boolean isResizable() {
        return true;
    }
}
