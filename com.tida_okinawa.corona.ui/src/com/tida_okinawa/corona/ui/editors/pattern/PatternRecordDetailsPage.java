/**
 * @version $Id: PatternRecordDetailsPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 18:56:49
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.Section;

import com.tida_okinawa.corona.AbstractInputValidator;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.internal.ui.actions.PartPatternModifyAction;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;
import com.tida_okinawa.corona.ui.views.db.action.DeletePatternTypeAction;

/**
 * @author kousuke-morishima
 */
public class PatternRecordDetailsPage extends PatternDetailsPage {
    PatternDicEditor pEditor;


    /**
     * パターン辞書エディタ画面右側を作成
     * 
     * @param editor
     */
    public PatternRecordDetailsPage(FormEditor editor) {
        super(editor);
        assert editor instanceof PatternDicEditor;
        this.pEditor = (PatternDicEditor) editor;
    }

    private Text nameText;
    ComboViewer typeCombo;
    private Combo partCombo;
    private PatternType patternType; /* #755 パターン種別変更用 */


    @Override
    public void createContents(Composite parent) {
        parent.setLayout(new FillLayout());

        Section section = createSection(parent, Messages.PatternRecordDetailsPage_labelPatternSetting, DESCRIPTION_RECORD);
        Composite client = kit.createComposite(section);
        client.setLayout(new GridLayout(3, false));
        client.setLayoutData(CompositeUtil.gridData(true, true, 2, 1));

        Label nameLabel = kit.createLabel(client, Messages.PatternRecordDetailsPage_labelPatternName);
        nameText = kit.createText(client, ""); //$NON-NLS-1$
        nameText.setLayoutData(CompositeUtil.gridData(true, false, 2, 1));
        /* #1354 制限文字数オーバーのメッセージを表示 */
        nameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (nameText.getText().length() > 80) {
                    /* 80文字に制限 */
                    String name = nameText.getText().substring(0, 80);
                    nameText.setText(name);
                    nameText.setSelection(name.length(), name.length());
                    /* メッセージの表示 */
                    PatternDicPage page = (PatternDicPage) editor.getActivePageInstance();
                    MessageBox messageBox = new MessageBox(page.getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText(Messages.PatternRecordDetailsPage_messageLimitTitle);
                    messageBox.setMessage(Messages.PatternRecordDetailsPage_messageLimitOver);
                    messageBox.open();
                }
            }
        });
        Label typeLabel = kit.createLabel(client, Messages.PatternRecordDetailsPage_labelPatternCategory);
        Combo combo = new Combo(client, SWT.DROP_DOWN);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        typeCombo = new ComboViewer(combo);
        typeCombo.setContentProvider(new ArrayContentProvider());
        typeCombo.setLabelProvider(new PatternTypeLabelProvider());
        typeCombo.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
                if (selected instanceof PatternType) {
                    /* #755 パターンのタイプを記憶しておく */
                    patternType = (PatternType) selected;
                }
            }
        });
        /* #755 選択中の全要素に対してパターン分類の変更を反映 */
        typeCombo.getCombo().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (record != null && patternType != null) {
                    /* 整合性チェック（変更した選択対象が、コンボで選択したアイテムと一致するかどうかの判定） */
                    Combo combo = (Combo) e.getSource();
                    String item = combo.getItem(combo.getSelectionIndex());
                    if (!patternType.getPatternName().equals(item)) {
                        return;
                    }
                    /* パターン分類の変更 */
                    IStructuredSelection ss = getStructuredSelection();
                    for (Object o : ss.toArray()) {
                        ((PatternRecord) o).setType(patternType.getId());
                    }
                    patternType = null;
                }
            }
        });
        Composite btnGroup = CompositeUtil.defaultComposite(client, 2);
        btnGroup.setLayout(CompositeUtil.gridLayout(2, 0, 0, 0, 0));
        btnGroup.setLayoutData(CompositeUtil.gridData(false, false, 1, 1));
        CompositeUtil.createBtn(btnGroup, SWT.PUSH, Messages.PatternRecordDetailsPage_buttonPatternMaking, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PatternType newType = createNewPatternType(editor.getSite().getShell());
                if (newType != null) {
                    typeCombo.setInput(pEditor.getPatternTypes());
                    typeCombo.setSelection(new StructuredSelection(newType));
                    /* パターン分類の新規追加データの反映（変更） */
                    IStructuredSelection ss = getStructuredSelection();
                    for (Object o : ss.toArray()) {
                        ((PatternRecord) o).setType(patternType.getId());
                    }
                }
            }
        });
        CompositeUtil.createBtn(btnGroup, SWT.PUSH, Messages.PatternRecordDetailsPage_buttonPatternSort, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ISelection current = typeCombo.getSelection();
                new PatternTypeDialog(pEditor.getSite().getShell()).open();
                typeCombo.setInput(pEditor.getPatternTypes());
                typeCombo.setSelection(current);
            }
        });

        Label partLabel = kit.createLabel(client, Messages.PatternRecordDetailsPage_labelPatternRef);
        partCombo = CompositeUtil.createCombo(client, new String[] { Messages.PatternRecordDetailsPage_labelParts,
                Messages.PatternRecordDetailsPage_labelPattern });
        partCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        partCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Combo combo = (Combo) e.getSource();
                String bool = combo.getItem(combo.getSelectionIndex());
                /* #755 選択中の全要素に対してパターン種別の変更を反映する */
                IStructuredSelection ss = getStructuredSelection();
                for (Object o : ss.toArray()) {
                    if (o instanceof PatternRecord) {
                        PatternRecord patternRecord = (PatternRecord) o;
                        /* パターンへの切り替え */
                        if (Messages.PatternRecordDetailsPage_labelPattern.equals(bool)) {
                            Set<ICoronaDic> dics = new HashSet<ICoronaDic>();
                            List<PatternRecord> patterns = new ArrayList<PatternRecord>();
                            IUIElement element = ((DicEditorInput) editor.getEditorInput()).getUIDictionary();
                            if (element != null) {
                                IUIProject uiProject = CoronaModel.INSTANCE.getProject(element);
                                ICoronaProject project = uiProject.getObject();
                                dics.addAll(project.getDictionarys(IPatternDic.class));

                                IUIContainer container = element.getParent().getParent();
                                if (container instanceof IUIProduct) {
                                    Set<ICoronaDic> element2 = new HashSet<ICoronaDic>();
                                    element2.addAll(((IUIProduct) container).getObject().getDictionarys(IPatternDic.class));
                                    /* ターゲットに紐づくパターン辞書をAdd */
                                    dics.addAll(element2);
                                }
                            } else {
                                /* データベースビューからエディタが開かれているため全パターン辞書に対して参照確認を行う */
                                dics.addAll(IoActivator.getService().getDictionarys(IPatternDic.class));
                            }
                            for (ICoronaDic dic : dics) {
                                PatternRecords recs = new PatternRecords((IPatternDic) dic);
                                patterns.addAll(recs.getPatternRecords());
                            }

                            PartPatternModifyAction action = new PartPatternModifyAction(patterns);
                            action.set(patternRecord);
                            action.preview(editor.getEditorSite().getShell());
                            if (Status.CANCEL_STATUS.equals(action.getResult())) {
                                /* 選択している一番上のパターンの詳細設定ページにあるComboの表示を差し戻す */
                                if (record != null && record.equals(patternRecord)) {
                                    combo.setText(Messages.PatternRecordDetailsPage_labelParts);
                                }
                            } else {
                                patternRecord.setPart(false);
                            }
                        }
                        /* 部品への切り替え */
                        else if (Messages.PatternRecordDetailsPage_labelParts.equals(bool)) {
                            patternRecord.setPart(true);
                        }
                    }
                }
            }
        });

        final IInformationControl info = getInformationControl(editor.getSite().getShell(), false, new Point(500, 200));
        setHover(info, nameLabel, Messages.PatternRecordDetailsPage_hintPatternName);
        setHover(info, typeLabel, Messages.PatternRecordDetailsPage_hintPatternCategory);
        setHover(info, partLabel, Messages.PatternRecordDetailsPage_hintPatternRef);

        setDirtyCheckListenerTo(nameText, SWT.Modify);
        setDirtyCheckListenerTo(typeCombo.getControl(), SWT.Selection);
        setDirtyCheckListenerTo(partCombo, SWT.Selection);

        section.setClient(client);
    }


    @Override
    protected void doSave() {
        commit();
        /*
         * #755
         * 本来commit()内部で行われていた処理だが、commit()でこの判定によってパターン種別を設定すると、
         * 複数パターン選択時に、パターン毎にパターン種別を設定できない。よって、doSave()の方に移動。
         */
        if (Messages.PatternRecordDetailsPage_labelParts.equals(partCombo.getText())) {
            record.setPart(true);
        } else if (Messages.PatternRecordDetailsPage_labelPattern.equals(partCombo.getText())) {
            record.setPart(false);
        }
    }


    @Override
    protected void commit() {
        record.setLabel(nameText.getText());
    }


    @Override
    public void setFocus() {
        nameText.setFocus();
    }

    PatternRecord record;


    @Override
    protected void selectionChanged(IFormPart part, Object selectedObject) {
        record = (PatternRecord) selectedObject;

        nameText.setText(record.getLabel());
        PatternType patternType = PatternType.getPatternType(record.getType());
        if (patternType == null) {
            patternType = PatternType.OTHER;
        }
        typeCombo.setInput(pEditor.getPatternTypes());
        typeCombo.setSelection(new StructuredSelection(patternType));
        if (record.isPart()) {
            partCombo.setText(Messages.PatternRecordDetailsPage_labelParts);
        } else {
            partCombo.setText(Messages.PatternRecordDetailsPage_labelPattern);
        }
    }


    /* ****************************************
     * パターン分類関連
     */
    /**
     * @return 作成したら、作成したパターン分類。
     */
    PatternType createNewPatternType(Shell shell) {
        InputDialog dialog = new InputDialog(shell, Messages.PatternRecordDetailsPage_labelNewPatternCategory,
                Messages.PatternRecordDetailsPage_labelInputPatternName, null, new AbstractInputValidator() {
                    @Override
                    public String isValid(String newText) {
                        String message = super.isValid(newText);
                        if (message != null) {
                            return message;
                        }
                        int singleQuortIndex = newText.indexOf("'"); //$NON-NLS-1$
                        if (singleQuortIndex != -1) {
                            return Messages.PatternRecordDetailsPage_16;
                        }
                        return (newText.trim().length() > 0) ? null : Messages.PatternRecordDetailsPage_17;
                    }


                    @Override
                    public int getLimit() {
                        return 20;
                    }
                });
        PatternType newType = null;
        if (dialog.open() == Dialog.OK) {
            newType = pEditor.createPatternType(dialog.getValue());
        }
        return newType;
    }

    /**
     * パターン分類を整理するダイアログ。追加と削除ができる。
     * 
     * @author kousuke-morishima
     * 
     */
    class PatternTypeDialog extends Dialog {
        public PatternTypeDialog(Shell shell) {
            super(shell);
        }

        TableViewer viewer = null;
        private Button addButton;
        private Button delButton;


        @Override
        protected Control createDialogArea(Composite parent) {
            parent = (Composite) super.createDialogArea(parent);
            Composite composite = CompositeUtil.defaultComposite(parent, 2);

            viewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI);
            viewer.setContentProvider(new ArrayContentProvider());
            viewer.setLabelProvider(new PatternTypeLabelProvider());
            viewer.setInput(getEditablePatternTypes());
            GridData tableLD = new GridData(SWT.FILL, SWT.FILL, true, true);
            tableLD.heightHint = 250;
            tableLD.widthHint = 200;
            viewer.getTable().setLayoutData(tableLD);

            Composite btnGroup = CompositeUtil.defaultComposite(composite, 1);
            btnGroup.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));
            addButton = CompositeUtil.createBtn(btnGroup, SWT.PUSH, Messages.PatternRecordDetailsPage_18, new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    PatternType newType = createNewPatternType(getShell());
                    if (newType != null) {
                        viewer.setInput(getEditablePatternTypes());
                        viewer.setSelection(new StructuredSelection(newType));
                    }
                }
            });
            delButton = CompositeUtil.createBtn(btnGroup, SWT.PUSH, Messages.PatternRecordDetailsPage_19, new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                    List<PatternType> delPTypes = new ArrayList<PatternType>();
                    for (Object o : selection.toArray()) {
                        /* デフォルトで入れている「その他」などを消せないようにIDチェック */
                        if (((PatternType) o).getId() > 0) {
                            delPTypes.add((PatternType) o);
                        }
                    }
                    DeletePatternTypeAction action = new DeletePatternTypeAction(delPTypes);
                    if (action.isEnabled()) {
                        action.preview(editor.getSite().getShell());
                        viewer.setInput(getEditablePatternTypes());
                    }
                }
            });
            GridData btnLayoutData = new GridData(80, SWT.DEFAULT);
            addButton.setLayoutData(btnLayoutData);
            delButton.setLayoutData(btnLayoutData);

            return parent;
        }


        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, OK, Messages.PatternRecordDetailsPage_20, true);
        }


        @Override
        protected void okPressed() {
            editor.editorDirtyStateChanged();
            super.okPressed();
        }


        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.PatternRecordDetailsPage_21);
        }


        PatternType[] getEditablePatternTypes() {
            PatternType[] types = pEditor.getPatternTypes();
            int dst = 0;
            for (int i = 0; i < types.length; i++) {
                PatternType type = types[i];
                if (type.getId() > 0) {
                    types[dst++] = type;
                }
            }
            PatternType[] ret = new PatternType[dst];
            System.arraycopy(types, 0, ret, 0, dst);
            return ret;
        }


        @Override
        protected boolean isResizable() {
            return true;
        }
    }

    static class PatternTypeLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            return ((PatternType) element).getPatternName();
        }
    }
}
