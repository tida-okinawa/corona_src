/**
 * @version $Id: UserDicEditorActionBar.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/15 18:01:23
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.tida_okinawa.corona.correction.erratum.Erratum;
import com.tida_okinawa.corona.internal.ui.component.CheckboxWithCombo;
import com.tida_okinawa.corona.internal.ui.component.CheckboxWithText;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.component.SortedFilterList;
import com.tida_okinawa.corona.internal.ui.component.ToolBarGroup;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.io.model.dic.UserDicFieldType;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.editors.IFilteringDataProvider;

/**
 * @author kousuke-morishima
 */
public class UserDicEditorActionBar extends ToolBarGroup {
    UserDicEditor editor;
    IFilteringDataProvider dataProvider;


    public UserDicEditorActionBar(Composite parent, UserDicEditor editor) {
        super(parent);
        this.editor = editor;
        this.dataProvider = editor.dataProvider;
    }


    @Override
    protected void addButtons() {
        Image filterIcon = Icons.INSTANCE.get(Icons.IMG_TOOL_FILTER);
        createToolItem(SWT.PUSH, "", "検索(Ctrl+F)", filterIcon, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openFilterDialog();
            }
        });
    }

    protected static FilterDialog dialog;


    public synchronized void openFilterDialog() {
        if (dialog == null) {
            dialog = new FilterDialog(getControl().getShell(), "単語検索", editor);
            dialog.open();
            dialog = null;
        } else {
            dialog.open();
            dialog = null;
        }
    }


    public synchronized void activeUserDic(UserDicEditor editor) {
        this.editor = editor;
        this.dataProvider = editor.dataProvider;
        if (dialog != null) {
            dialog.setDialogClear();
            dialog.setDialogInfo(editor);
            dialog.setPrevValue();
        }
    }

    static class FilterDialog extends Dialog {
        private String title;
        private UserDicEditor editor2;
        private IFilteringDataProvider dataProvider2;


        protected FilterDialog(Shell parentShell, String title, UserDicEditor editor) {
            super(parentShell);
            this.title = title;
            setDialogInfo(editor);
        }


        public void setDialogClear() {
            setTermText("");
            if (termField.isChecked()) {
                termField.setChecked(false);
            }
            if (partField.isChecked()) {
                setTermPart(0);
                partField.setChecked(false);
            }
            refreshTermClass();
            if (classField.isChecked()) {
                classField.setChecked(false);
            }
            /* ラベル関連情報初期化 */
            labelList.sortedFilterListClear();
            labelList.setCheckedAll(false);

        }


        public synchronized void setDialogInfo(UserDicEditor editor) {
            this.editor2 = editor;
            this.dataProvider2 = editor.dataProvider;
            if (dialog != null) {
                setLabelList(editor2);
            }
        }


        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            super.createButtonsForButtonBar(parent);
            getButton(OK).setText("検索");
            getButton(CANCEL).setText("閉じる");
        }

        private CheckboxWithText termField;
        CheckboxWithCombo partField;
        CheckboxWithCombo classField;
        SortedFilterList labelList;


        @Override
        protected Control createDialogArea(Composite parent) {
            Composite root = CompositeUtil.defaultComposite(parent, 2);
            Label l = CompositeUtil.createLabel(root, "    * = 任意の文字列", -1);
            l.setLayoutData(CompositeUtil.gridData(true, false, 2, 1));

            termField = new CheckboxWithText(root, "単語");
            termField.setLayoutData(CompositeUtil.gridData(true, false, 2, 1));

            partField = new CheckboxWithCombo(root, "品詞");
            partField.setLayoutData(CompositeUtil.gridData(true, false, 2, 1));
            PrivateTermPart termParts = new PrivateTermPart(TermPart.values());
            partField.setItems(termParts.names);
            partField.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    refreshTermClass();
                }
            });
            partField.addCheckStateChangedListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (((Button) e.widget).getSelection()) {
                        refreshTermClass();
                    } else {
                        classField.setItems(new PrivateTermClass(TermClass.values()).names);
                    }
                }
            });

            classField = new CheckboxWithCombo(root, "品詞細分類");
            classField.setLayoutData(CompositeUtil.gridData(true, false, 2, 1));
            PrivateTermClass termClasses = new PrivateTermClass(TermClass.values());
            classField.setItems(termClasses.names);

            /* ラベル */
            Composite labelGroup = CompositeUtil.defaultComposite(root, 2);
            Label messageLabel = CompositeUtil.createLabel(labelGroup, "ラベル", -1);
            messageLabel.setLayoutData(CompositeUtil.gridData(true, false, 2, 1));

            labelList = new SortedFilterList(labelGroup, SWT.CHECK | SWT.BORDER | SWT.SINGLE, new LabelProvider());
            labelList.setContentProvider(new ConPro());
            setLabelList(editor2);
            GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
            layoutData.heightHint = 180;
            labelList.setLayoutData(layoutData);

            Composite btnGroup = CompositeUtil.defaultComposite(labelGroup, 1);
            btnGroup.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, false));
            CompositeUtil.createBtn(btnGroup, SWT.PUSH, "全選択(&A)", new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    labelList.setCheckedAll(true);
                }
            });
            CompositeUtil.createBtn(btnGroup, SWT.PUSH, "全解除(&D)", new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    labelList.setCheckedAll(false);
                }
            });

            setPrevValue();

            return root;
        }


        private void setPrevValue() {
            Map<Object, Object[]> filters = dataProvider2.getFilters();
            for (Entry<Object, Object[]> e : filters.entrySet()) {
                if (e.getKey() instanceof UserDicFieldType) {
                    if (e.getValue().length > 0) {
                        switch ((UserDicFieldType) e.getKey()) {
                        case HEADER:
                            setTermText((String) e.getValue()[0]);
                            break;
                        case PART:
                            setTermPart((Integer) e.getValue()[0]);
                            refreshTermClass();
                            break;
                        case CLASS:
                            setTermClass((Integer) e.getValue()[0]);
                            break;
                        case CFORM:
                            setTermCForm((Integer) e.getValue()[0]);
                            break;
                        case LABEL:
                            labelList.setChecked(e.getValue(), true);
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        }


        @Override
        protected void okPressed() {
            Map<Object, Object[]> filter = new HashMap<Object, Object[]>();
            /* 複数フィールドのフィルタには対応していないので、先頭の一つのみフィルタがかかるはず */
            if (termField.isChecked()) {
                filter.put(UserDicFieldType.HEADER, new Object[] { getTermText() });
            }
            if (partField.isChecked()) {
                filter.put(UserDicFieldType.PART, new Object[] { getTermPart().getIntValue() });
            }
            if (classField.isChecked()) {
                filter.put(UserDicFieldType.CLASS, new Object[] { getTermClass().getIntValue() });
            }

            if (labelList.getChecked().length > 0) {
                filter.put(UserDicFieldType.LABEL, labelList.getChecked());
            }

            dataProvider2.setFilter(filter);
            dataProvider2.first();
            editor2.viewer.setInput(dataProvider2.current(true));
            editor2.toolbarGroup.updateDataNumLabel();
            editor2.addEmptyRow();
        }


        void refreshTermClass() {
            TermPart part = TermPart.valueOfName(partField.getValue());
            PrivateTermClass termClasses = null;
            if (TermPart.NONE.equals(part)) {
                termClasses = new PrivateTermClass(TermClass.values());
            } else if (part != null) {
                List<TermClass> classes = TermClass.values(part.getIntValue());
                termClasses = new PrivateTermClass(classes.toArray(new TermClass[classes.size()]));
            } else {
                termClasses = new PrivateTermClass(TermClass.values());
            }
            classField.setItems(termClasses.names);
        }

        /* ****************************************
         * getter/setter
         */
        private final String REP_PERSENT = Erratum.convertZenkakuString("&persent");


        private String getTermText() {
            String text = Erratum.convertZenkakuString(termField.getValue().trim().replaceAll("\\*", REP_PERSENT));
            return text.replaceAll(REP_PERSENT, "%");
        }


        private void setTermText(String term) {
            if (term != null) {
                termField.setValue(term.replaceAll("%", "*"));
            }
        }


        private TermPart getTermPart() {
            return TermPart.valueOfName(partField.getValue());
        }


        private void setTermPart(int partId) {
            partField.setValue(TermPart.valueOf(partId).getName());
        }


        private TermClass getTermClass() {
            return TermClass.valueOfName(classField.getValue());
        }


        private void setTermClass(int classId) {
            classField.setValue(TermClass.valueOf(classId).getName());
        }


        private void setTermCForm(int cformId) {
            /* フィールドを用意していないので、コメントアウト */
            // cformField.setValue(TermCForm.valueOf(cformId).getName());
        }


        /* ****************************************
         * other
         */
        @Override
        protected boolean isResizable() {
            return true;
        }


        @Override
        protected void setShellStyle(int newShellStyle) {
            newShellStyle = newShellStyle & (~SWT.APPLICATION_MODAL | SWT.MODELESS);
            super.setShellStyle(newShellStyle);
        }


        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(title);
        }


        protected void setLabelList(UserDicEditor editor2) {
            labelList.setInput(editor2.labelGroup.getRelatedlabelDics());
        }


        protected void setBtnEnabled(boolean bFlg) {
            getButton(OK).setEnabled(bFlg);
        }
    }

    static class ConPro extends ArrayContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            List<IDicItem> labels = new ArrayList<IDicItem>();
            Object[] ret = super.getElements(inputElement);
            for (Object o : ret) {
                labels.addAll(toFlat(((ILabelDic) o).getItems()));
                ((ILabelDic) o).getItems();
            }
            return labels.toArray();
        }


        private List<IDicItem> toFlat(List<? extends IDicItem> items) {
            List<IDicItem> ret = new ArrayList<IDicItem>();
            for (IDicItem item : items) {
                ret.add(item);
                ret.addAll(toFlat(((ILabel) item).getChildren()));
            }
            return ret;
        }
    }
}
