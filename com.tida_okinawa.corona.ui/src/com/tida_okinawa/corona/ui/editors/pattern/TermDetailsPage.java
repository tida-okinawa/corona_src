/**
 * @version $Id: TermDetailsPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/31 14:19:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.Section;

import com.tida_okinawa.corona.correction.collocation.CorrectionStringList;
import com.tida_okinawa.corona.correction.erratum.Erratum;
import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.QuantifierType;
import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;
import com.tida_okinawa.corona.correction.parsing.model.Sequence;
import com.tida_okinawa.corona.correction.parsing.model.Term;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaDics;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;

/**
 * @author kousuke-morishima
 */
public class TermDetailsPage extends PatternDetailsPage {

    /**
     * @param editor
     *            辞書情報
     */
    public TermDetailsPage(FormEditor editor) {
        super(editor);
    }

    Text wordText;
    private Button wordButton;
    private Combo partCombo;
    private Combo classCombo;
    private Combo quantCombo;
    Text labelText;
    private Button labelButton;
    private Label quantLabel;
    private Button morphemeButton;

    private static class TermLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof ITerm) {
                ITerm term = (ITerm) element;
                return getString(50, ":", term.getValue(), term.getTermPart().getName(), term.getTermClass().getName()); //$NON-NLS-1$
            }
            return super.getText(element);
        }


        private final static String getString(int initCapa, String sepa, Object... args) {
            StringBuilder buf = new StringBuilder(initCapa);
            for (Object arg : args) {
                buf.append(sepa).append(arg);
            }
            buf.delete(0, sepa.length());
            return buf.toString();
        }
    };


    @Override
    public void createContents(Composite parent) {
        parent.setLayout(new FillLayout());

        Section section = createSection(parent, "単語の設定", DESCRIPTION_TERM);
        Composite client = kit.createComposite(section);
        client.setLayout(new GridLayout(4, false));
        client.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label wordLabel = kit.createLabel(client, "単語 : ");
        wordText = kit.createText(client, ""); //$NON-NLS-1$
        wordText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        wordText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Listener[] ls = wordText.getListeners(SWT.Modify);
                for (Listener l : ls) {
                    wordText.removeListener(SWT.Modify, l);
                }
                wordText.setText(Erratum.convertZenkakuString(wordText.getText()));
                for (Listener l : ls) {
                    wordText.addListener(SWT.Modify, l);
                }
            }
        });
        wordButton = kit.createButton(client, " ... ", SWT.PUSH); //$NON-NLS-1$
        wordButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /*
                 * 登録済み単語の選択ダイアログを開く
                 * ユーザ辞書（固有辞書、分野辞書、一般辞書）、Jumanからインポートした一般辞書を表示する
                 */
                /** #1385 単語選択ダイアログを表示の高速化のためにカスタマイズ */
                TermElementListSelectionDialog d = new TermElementListSelectionDialog(editor.getSite().getShell(), new TermLabelProvider());
                d.setTitle("単語選択");
                d.setMessage("指定する単語を選択してください。");
                d.setDicList(createDialogInput(IUserDic.class));
                d.setConvertZenkaku(true);
                d.setImeMode(SWT.NATIVE);
                d.setMultipleSelection(false);
                d.open();

                if (d.getReturnCode() == Dialog.OK) {
                    Object result = d.getFirstResult();
                    wordText.setText(((ITerm) result).getValue());
                }
            }
        });
        /**
         * @author s.takuro
         *         #187 構文パターン自動生成（形態素化）
         */
        final Composite p = parent;
        morphemeButton = kit.createButton(client, Messages.TermDetailsPage_buttonMorpheme, SWT.PUSH);
        morphemeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean result;
                if ("".equals(wordText.getText())) { //$NON-NLS-1$
                    MessageBox messageError = new MessageBox(p.getShell(), SWT.ICON_ERROR);
                    messageError.setText(Messages.TermDetailsPage_messageErrorTitle);
                    messageError.setMessage(Messages.TermDetailsPage_messageErrorNotTerm);
                    messageError.open();
                    return;
                } else {
                    /* 対象のパターンを形態素化 */
                    result = morphemePattern(wordText.getText());

                    if (result != true) {
                        MessageBox messageError = new MessageBox(p.getShell(), SWT.ICON_ERROR);
                        messageError.setText(Messages.TermDetailsPage_messageErrorTitle);
                        messageError.setMessage(Messages.TermDetailsPage_messageErrorMorpheme);
                        messageError.open();
                        return;
                    }
                }
            }
        });

        Label partLabel = kit.createLabel(client, "品詞 : ");
        partCombo = CompositeUtil.createCombo(client, new String[] {});
        partCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
        TermPart[] parts = TermPart.values();
        for (TermPart part : parts) {
            partCombo.add(part.getName());
        }
        partCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* 選択されたアイテムによって、品詞詳細の内容を変える */
                updateWordClassItems();
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        Label classLabel = kit.createLabel(client, "品詞詳細 : ");
        classCombo = CompositeUtil.createCombo(client, new String[] {});
        classCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

        Label labelLabel = kit.createLabel(client, "ラベル : ");
        labelText = kit.createText(client, "", SWT.BORDER); //$NON-NLS-1$
        labelText.setEnabled(false);
        labelText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        labelButton = kit.createButton(client, " ... ", SWT.PUSH); //$NON-NLS-1$
        final ITreeContentProvider lConPro = new PrivateLabelDicContentProvider();
        final LabelProvider lLPro = new PrivateLabelDicLabelProvider();
        labelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<ICoronaDic> input = createDialogInput(ILabelDic.class);

                InternalElementTreeSelectionDialog d = new InternalElementTreeSelectionDialog(editor.getSite().getShell(), lLPro, lConPro);
                d.setTitle("ラベル選択");
                d.setMessage("指定するラベルを選択してください。");
                d.setEmptyListMessage("ラベル辞書が登録されていません。");
                d.setValidator(new Class<?>[] { ILabel.class });
                d.setInput(input);
                d.setAllowMultiple(false);
                d.open();

                if (d.getReturnCode() == Dialog.OK) {
                    Object result = d.getFirstResult();
                    if (result != null) {
                        labelText.setText(((ILabel) result).getName());
                    } else {
                        labelText.setText(""); //$NON-NLS-1$
                    }
                }
            }
        });
        CompositeUtil.createBtn(client, SWT.PUSH, "クリア", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                labelText.setText(""); //$NON-NLS-1$
            }
        });

        /* 数量子 */
        quantLabel = kit.createLabel(client, "数量子 : ");
        quantCombo = CompositeUtil.createCombo(client, new String[] {});
        quantCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
        QuantifierType[] quants = QuantifierType.values();
        for (QuantifierType quant : quants) {
            quantCombo.add(quant.getName());
        }

        final IInformationControl info = getInformationControl(editor.getSite().getShell(), false, new Point(500, 200));
        setHover(info, wordLabel,
                "単語は形態素で分節された用語を言います。「動詞」、「助動詞」、「形容詞」は原形を入力してください。原形は「形態素・係り受け結果」から参照することができます。\n　　例) 表記='でしょう'　原形='だろう'の場合、'だろう'と入力してください。\n");
        setHover(info, partLabel, "品詞は「形態素・係り受け結果」から参照することができます。");
        setHover(info, classLabel, "品詞の詳細分類は「形態素・係り受け結果」から参照することができます。");
        setHover(info, labelLabel, "ラベルはユーザ用語辞書でラベル付与した単語にマッチします。");
        setHover(info, quantLabel,
                "数量子は「ミス(を)した」、「ミス(は)した」、「ミス()した」のように1単語の差異を簡略化する機能です。この場合(を)と(は)は「助詞」なので品詞を「助詞」にして数量子を「0..1」にします。　数量子は、「連続」パターンの子の場合にのみ使用できます。");
        setDirtyCheckListenerTo(wordText, SWT.Modify);
        setDirtyCheckListenerTo(partCombo, SWT.Selection);
        setDirtyCheckListenerTo(classCombo, SWT.Selection);
        setDirtyCheckListenerTo(labelText, SWT.Modify);
        setDirtyCheckListenerTo(quantCombo, SWT.Selection);

        section.setClient(client);
    }


    List<ICoronaDic> createDialogInput(Class<? extends ICoronaDic> clazz) {
        List<ICoronaDic> input = new ArrayList<ICoronaDic>();

        IUIDictionary uiDic = ((DicEditorInput) editor.getEditorInput()).getUIDictionary();
        if (uiDic != null) {
            ICoronaDics parentObject = CoronaModel.INSTANCE.getLibrary(uiDic).getObject();
            if (parentObject instanceof ICoronaProject) {
                /* 共通辞書のパターン辞書を開いている */
                input.addAll(parentObject.getDictionarys(clazz));
            } else if (parentObject instanceof ICoronaProduct) {
                /* ターゲット下のパターン辞書を開いている */
                ICoronaProject coronaProject = CoronaModel.INSTANCE.getProject(uiDic).getObject();
                input.addAll(coronaProject.getDictionarys(clazz));
                input.addAll(parentObject.getDictionarys(clazz));
            } else {
                throw new IllegalStateException("プロジェクトデータを取得できませんでした");
            }
        } else {
            input.addAll(IoActivator.getService().getDictionarys(clazz));
        }

        return input;
    }


    @Override
    protected void doSave() {
        commit();
    }


    @Override
    protected void commit() {
        /* 誤記補正（文字の全角化）もやる */
        term.setWord(Erratum.convertZenkakuString(wordText.getText()));

        TermPart part = TermPart.valueOfName(partCombo.getText());
        term.setPart(part);

        TermClass termClass = TermClass.valueOf(classCombo.getText(), part);
        term.setWordClass(termClass);

        term.setLabel(labelText.getText());

        QuantifierType quant = QuantifierType.valueOfName(quantCombo.getText());
        term.setQuant(quant);
    }


    @Override
    public void setFocus() {
        wordText.setFocus();
    }

    private Term term;


    @Override
    protected void selectionChanged(IFormPart part, Object selectedObject) {
        term = (Term) selectedObject;
        wordText.setText(term.getWord());
        TermPart termPart = term.getPart();
        if (term.getPart() == null) {
            termPart = TermPart.NONE;
        }
        partCombo.setText(termPart.getName());

        /* SelectionChangedイベントが起きないから、強制的に内容を変更させる */
        updateWordClassItems();

        TermClass wordClass = term.getWordClass();
        if (wordClass == null) {
            wordClass = TermClass.NONE;
        }
        classCombo.setText(wordClass.getName());

        String label = term.getLabel();
        if (label == null) {
            label = ""; //$NON-NLS-1$
        }
        labelText.setText(label);

        QuantifierType termQuant = term.getQuant();
        if (term.getQuant() == null) {
            termQuant = QuantifierType.QUANT_NONE;
        }
        quantCombo.setText(termQuant.getName());
        /* Sequenceの子である時だけ、このオブジェクトを表示する */
        PatternContainer p = term.getParent();
        if (p instanceof Sequence) {
            quantCombo.setVisible(true);
            quantLabel.setVisible(true);
        } else {
            quantCombo.setVisible(false);
            quantLabel.setVisible(false);
        }
    }


    void updateWordClassItems() {
        classCombo.removeAll();
        TermPart part = TermPart.valueOfName(partCombo.getText());
        if (part == null)
            part = TermPart.NONE;
        List<TermClass> classes = TermClass.values(part.getIntValue());
        if (classes != null) {
            for (TermClass tClass : classes) {
                classCombo.add(tClass.getName());
            }
        }
    }


    /**
     * @author s.takuro
     *         #187 構文パターン自動生成（形態素化）
     */
    final char NL_CODE = '\n';


    /**
     * Treeで選択中のパターンを形態素化
     * 
     * @param text
     *            形態素化する文字列
     * @return 処理結果
     */
    public boolean morphemePattern(String text) {

        /* 形態素化の結果を取得 */
        boolean result = false;
        List<String> input = new ArrayList<String>(1);
        List<SyntaxStructure> ssList = new ArrayList<SyntaxStructure>(100);
        input.add(text);
        CorrectionStringList correctionList = new CorrectionStringList();
        try {
            String jumanResult = correctionList.exec(input).get(0);
            String[] jumanResults = jumanResult.split(",");
            for (int i = 0; i < jumanResults.length; i++) {
                /* 環境依存文字を含む結果を除去 */
                String[] jumanElement = jumanResults[i].split(" ");
                if (jumanElement[0].indexOf("?") == -1) {
                    /* 形態素化 */
                    SyntaxStructure ss = new SyntaxStructure(jumanResults[i]);
                    if (ss.size() != 0) {
                        ssList.add(ss);
                    }
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return result;
        }
        if (ssList.size() > 0) {
            result = morphemeElements(ssList, term);
        }
        return result;
    }


    /**
     * 選択中の項目を形態素化した要素に置き換える
     * 
     * @param ssList
     *            形態素リスト
     * @param pattern
     *            登録先パターン
     * @return true:成功、false:失敗
     */
    private boolean morphemeElements(List<SyntaxStructure> ssList, Pattern pattern) {
        if (pattern == null || pattern.getParent() == null) {
            return false;
        }
        PatternContainer parent = pattern.getParent();

        /* 元の位置を保持するために挿入位置を取得 */
        int index = parent.getChildren().indexOf(pattern);

        /* 親と削除対象の要素の関連を除去 */
        parent.removeChild(pattern);
        pattern.setParent(null);

        /* 単語が2つより少ない場合はSequence下に作成しない。また、Sequence下にSequenceは作成不可 */
        if (ssList.size() < 2 || parent instanceof Sequence) {
            createTerm(ssList, parent, index);
        } else {
            PatternContainer sequence = new Sequence(parent);
            /* "一文"に設定 */
            ((Sequence) sequence).setScope(SearchScopeType.SEARCH_SENTENCE);
            createTerm(ssList, sequence, -1);
            /* 親と追加対象の要素を紐づけ */
            parent.addChild(index, sequence);
        }
        return true;
    }


    /**
     * 単語の生成
     * 
     * @param ssList
     *            形態素リスト
     * @param parent
     *            親パターン
     * @param index
     *            単語を生成する位置（-1の場合は設定しない）
     */
    @SuppressWarnings("static-method")
    private void createTerm(List<SyntaxStructure> ssList, PatternContainer parent, int index) {
        for (SyntaxStructure ss : ssList) {
            Term term = new Term(parent);
            MorphemeElement morphemeElement = ss.getMorphemeElemsnts().get(0);
            term.setWord(morphemeElement.getGenkei());
            term.setPart(TermPart.valueOfName(morphemeElement.getHinshi()));
            term.setWordClass(TermClass.valueOfName(morphemeElement.getHinshiSaibunrui()));
            if (index != -1) {
                parent.addChild(index++, term);
            } else {
                parent.addChild(term);
            }
        }
    }
}
