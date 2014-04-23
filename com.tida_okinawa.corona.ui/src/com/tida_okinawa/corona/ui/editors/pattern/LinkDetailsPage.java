/**
 * @version $Id: LinkDetailsPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/31 16:28:51
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.Section;

import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;

/**
 * @author kousuke-morishima
 */
public class LinkDetailsPage extends PatternDetailsPage {

    PatternDicEditorMasterBlock masterBlock;
    Boolean comboDisable = false;


    /**
     * エディター右画面を生成
     * 
     * @param editor
     */
    public LinkDetailsPage(FormEditor editor) {
        super(editor);
        if (editor instanceof PatternDicEditor) {
            IFormPage page = editor.getActivePageInstance();
            masterBlock = ((PatternDicPage) page).master;
        }
    }

    ComboViewer nameCombo, dicCombo;
    Button b;


    @Override
    public void createContents(Composite parent) {
        parent.setLayout(new FillLayout());

        Section section = createSection(parent, Messages.LinkDetailsPage_labelPatternSetting, DESCRIPTION_LINK);
        Composite client = kit.createComposite(section);
        client.setLayout(new GridLayout(3, false));
        client.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label dicLabel = kit.createLabel(client, Messages.LinkDetailsPage_labelPatternDic);
        dicCombo = new ComboViewer(client, SWT.NONE);
        dicCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        dicCombo.setContentProvider(new ArrayContentProvider());
        /** #1109 辞書更新機能をカプセル化 */
        updateDics();
        dicCombo.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                ICoronaDic dic = (ICoronaDic) element;
                return dic.getName();
            }
        });
        dicCombo.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateDicItems(dicCombo.getSelection());
            }
        });
        /* #1109 新規に追加されたパターン辞書も参照の選択肢に表示 */
        dicCombo.getCombo().addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
            }


            @Override
            public void focusGained(FocusEvent e) {
                updateDics();
            }
        });
        @SuppressWarnings("unused")
        Label dummy = kit.createLabel(client, null);
        IInformationControl info = getInformationControl(editor.getSite().getShell(), false, new Point(500, 200));
        setHover(info, dicLabel, Messages.LinkDetailsPage_hintPatternDic);

        Label nameLabel = kit.createLabel(client, Messages.LinkDetailsPage_labelPatternName);
        /* #756 入力可能なコンボボックスに変更 */
        nameCombo = new ComboViewer(client, SWT.NONE);
        nameCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        nameCombo.setContentProvider(new ArrayContentProvider());
        nameCombo.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                IPattern p = (IPattern) element;
                return p.getLabel();
            }
        });
        nameCombo.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IPattern p = (IPattern) ((IStructuredSelection) event.getSelection()).getFirstElement();
                if (p != null) {
                    link.setLinkPattern(p);
                } else {
                    link.setLinkPattern(null);
                }
                editor.editorDirtyStateChanged();
                ((PatternDicPage) ((PatternDicEditor) editor).getActivePageInstance()).update();
            }
        });

        nameCombo.getCombo().addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
            }


            @Override
            public void focusGained(FocusEvent e) {
                /* 保存するとここにくるので、ここで更新しておけばよい */
                updateDicItems(dicCombo.getSelection());
            }
        });

        b = kit.createButton(client, Messages.LinkDetailsPage_buttonClear, SWT.PUSH);
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                nameCombo.getCombo().deselectAll();
                link.setLinkPattern(null);
            }
        });

        IInformationControl info2 = getInformationControl(editor.getSite().getShell(), false, new Point(500, 200));
        setHover(info2, nameLabel, Messages.LinkDetailsPage_hintPatternItem);
        section.setClient(client);
    }


    @Override
    protected void doSave() {
        commit();
    }


    @Override
    protected void commit() {
        /* selectionChangedでセット済み */
    }


    @Override
    public void setFocus() {
        nameCombo.getControl().setFocus();
    }

    Link link;


    @Override
    protected void selectionChanged(IFormPart part, Object selectedObject) {
        /* 初期状態の設定 */
        link = (Link) selectedObject;

        ICoronaDic dummyDic = null;
        if (link.getId() != IPatternDic.UNSAVED_ID && !(link.getLabel().isEmpty())) {
            /* 既存アイテムの場合 */
            dummyDic = IoActivator.getService().getDictionary(link.getDicName());
        } else {
            /* 新規アイテムの場合 */
            String dicName = editor.getEditorInput().getName();
            dummyDic = IoActivator.getService().getDictionary(dicName);
        }
        dicCombo.setSelection(new StructuredSelection(dummyDic));
        updateDicItems(new StructuredSelection(dummyDic));

        /* データベースビューから辞書を開いた場合、選択項目をdisable化 */
        if (comboDisable) {
            dicCombo.getCombo().setEnabled(false);
            nameCombo.getCombo().setEnabled(false);
            b.setEnabled(false);
        }
    }


    /** #1109 辞書を最新化する */
    void updateDics() {
        /* リスト内で選択されている位置を取得（未設定の場合は-1） */
        int pos = dicCombo.getCombo().getSelectionIndex();

        Set<ICoronaDic> dics = new HashSet<ICoronaDic>();
        IUIElement element = ((DicEditorInput) editor.getEditorInput()).getUIDictionary();
        if (element != null) {
            /* プロジェクトビューから辞書を開いた場合 */
            /* プロジェクトに紐づくパターン辞書のみをComboboxへ設定する */
            IUIProject uiProject = CoronaModel.INSTANCE.getProject(element);
            ICoronaProject project = uiProject.getObject();
            /* 共通辞書をAdd */
            dics.addAll(project.getDictionarys(IPatternDic.class));

            IUIContainer container = element.getParent().getParent();
            if (container instanceof IUIProduct) {
                List<ICoronaDic> element2 = ((IUIProduct) container).getObject().getDictionarys(IPatternDic.class);
                /* ターゲットに紐づくパターン辞書をAdd */
                dics.addAll(element2);
            }
        } else {
            /* データベースビューから辞書を開いた場合 */
            /* DBから辞書を展開し、選択不可とする */
            dics.addAll(IoActivator.getService().getDictionarys(IPatternDic.class));
            comboDisable = true;
        }
        dicCombo.setInput(dics.toArray());
        /* リスト内で選択されている場合、位置を設定する */
        if (pos >= 0) {
            dicCombo.getCombo().select(pos);
        }
    }


    /**
     * 辞書コンボボックスを最新化する。
     * 
     * @param selection
     */
    void updateDicItems(ISelection selection) {
        /* 部品パターンの初期値設定 */
        IPattern dummy = IoActivator.getDicFactory().createPattern(link.getLabel(), "", 0, true); //$NON-NLS-1$
        dummy.setId(link.getId());
        new StructuredSelection(dummy);

        ICoronaDic coronaDic = (ICoronaDic) ((IStructuredSelection) selection).getFirstElement();
        IPattern[] cur = masterBlock.createPartPatterns(coronaDic);
        PatternContainer parent = link.getParent();
        while (parent != null && !(parent instanceof PatternRecord)) {
            parent = parent.getParent();
        }
        int i = 0;
        for (; i < cur.length; i++) {
            /* 自身を除く */
            if (((PatternRecord) parent).getIPattern().equals(cur[i])) {
                break;
            }
        }

        if (i != cur.length) {
            IPattern[] tmp = new IPattern[cur.length - 1];
            System.arraycopy(cur, 0, tmp, 0, i);
            System.arraycopy(cur, i + 1, tmp, i, tmp.length - i);
            cur = tmp;
        }
        nameCombo.setInput(cur);
        nameCombo.setSelection(new StructuredSelection(dummy));
    }
}
