/**
 * @version $Id: CollocationTermPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/06 11:27:02
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.collocation;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.editor.FormEditor;

import com.tida_okinawa.corona.correction.erratum.Erratum;
import com.tida_okinawa.corona.correction.frequent.FrequentRecord;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.ui.editors.EditorUtil;
import com.tida_okinawa.corona.ui.editors.FrequentLabelProvider;
import com.tida_okinawa.corona.ui.editors.FrequentTermPage;
import com.tida_okinawa.corona.ui.editors.user.ComboItem;
import com.tida_okinawa.corona.ui.editors.user.PrivateTermClass;


/**
 * @author wataru-higa
 * 
 */
public class CollocationTermPage extends FrequentTermPage {

    /**
     * @param editor
     *            エディタ
     */
    public CollocationTermPage(FormEditor editor) {
        super(editor, "Collocation.UniqueIdentifier", Messages.CollocationTermPage_EDITOR_TAB); //$NON-NLS-1$
        formTitle = Messages.CollocationTermPage_EDITOR_TITLE;
    }


    @Override
    protected void createTable(Table tbl) {
        createColumnWithSort(tbl, Messages.CollocationTermPage_EDITOR_COLUMN_ORG, 130, sortHeader);
        tbl.setSortColumn(createColumnWithSort(tbl, Messages.CollocationTermPage_EDITOR_COLUMN_HIT, 80, sortCount));
        tbl.setSortDirection(SWT.DOWN);
        createColumnWithSort(tbl, Messages.CollocationTermPage_EDITOR_COLUMN_DIC, 120, null);
        createColumnWithSort(tbl, Messages.CollocationTermPage_EDITOR_COLUMN_TERM_PART, 120, null);
        createColumnWithSort(tbl, Messages.CollocationTermPage_EDITOR_COLUMN_TERM_PART_DITAIL, 120, null);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new FrequentLabelProvider());
        viewer.setSorter(sortCount);

        setCellEditor();
    }


    private void setCellEditor() {
        Table tbl = viewer.getTable();
        TextCellEditor orgTextCellEditor = new TextCellEditor(tbl);

        dicCellEditor = new ComboBoxCellEditor(tbl, new String[0]);
        ComboBoxCellEditor partCellEditor = new ComboBoxCellEditor(tbl, getPartItems().getNames());
        detailComboEditor = new ComboBoxCellEditor(tbl, new String[0]);
        final CellEditor[] editors = new CellEditor[] { orgTextCellEditor, null, dicCellEditor, partCellEditor, detailComboEditor, null, null };

        viewer.setColumnProperties(properties);
        viewer.setCellEditors(editors);
        viewer.setCellModifier(new CollocationRegisterCellModifier());
        EditorUtil.setFocusMoveListener(editors, viewer, 0, 2, 3, 4);
    }


    @Override
    protected ViewerFilter createFilter() {
        return null;
    }

    class CollocationRegisterCellModifier extends FrequentRegisterCellModifier {

        @Override
        public boolean canModify(Object element, String property) {
            if (property.equals(PROP_COUNT)) {
                return false;
            }
            return true;
        }


        @Override
        public Object getValue(Object element, String property) {
            FrequentRecord data = (FrequentRecord) element;
            if (property.equals(PROP_ORG)) {
                return data.getGenkei();
            } else if (property.equals(PROP_DIC)) {
                ICoronaDic destDic = data.getDestDictionary();
                DicNameItem destDics = getDestDicItem();
                dicCellEditor.setItems(destDics.getNames());
                return destDics.getIndex(destDic);
            } else if (property.equals(PROP_PART)) {
                ComboItem<TermPart> termParts = getPartItems();
                return termParts.getIndex(TermPart.valueOfName(data.getHinshi()));
            } else if (property.equals(PROP_DETAIL)) {
                /* 品詞詳細アイテム設定 */
                TermPart termPart = TermPart.valueOfName(data.getHinshi());
                ComboItem<TermClass> termClass = createTermClass(termPart.getIntValue());
                String[] classes = termClass.getNames();
                detailComboEditor.setItems(classes);
                return termClass.getIndex(TermClass.valueOfName(data.getHinshiSaibunrui()));
            }
            return null;
        }


        @Override
        public void modify(Object element, String property, Object value) {
            // ユーザが編集を確定した時に呼ばれる
            // 値を、モデルに反映させる。
            boolean modify = false;

            FrequentRecord data = (FrequentRecord) ((TableItem) element).getData();
            if (property.equals(PROP_ORG)) {
                String genkei = ((String) value).trim();
                modify = !equals(genkei, data.getGenkei());
                if (modify) {
                    genkei = Erratum.convertZenkakuString(genkei);
                    data.setGenkei(genkei);
                    /* #477 よみに自動で単語の文字列をいれる */
                    data.setYomi(genkei);
                }
            } else if (property.equals(PROP_DIC)) {
                Integer index = (Integer) value;
                // Memo もし遅いようなら, getValueからmodiryの間だけフィールドに持つ  */
                IUserDic newDic = (IUserDic) getDestDicItem().get(index);
                modify = !equals(data.getDestDictionary(), newDic);
                data.setDestDictionary(newDic);

            } else if (property.equals(PROP_PART)) {
                /* 品詞設定 */
                Integer index = (Integer) value;
                TermPart newValue = getPartItems().get(index);
                if (newValue == null) {
                    modify = !equals(data.getHinshi(), ""); //$NON-NLS-1$
                    data.setHinshi(""); //$NON-NLS-1$
                    newValue = TermPart.NONE;
                } else if (!newValue.getName().equals(data.getHinshi())) {
                    modify = true;
                    data.setHinshi(newValue.getName());
                }
                if (modify) {
                    /* 品詞の値が変わったとき */
                    String[] detailItems = createTermClass(newValue.getIntValue()).getNames();
                    detailComboEditor.setItems(detailItems);
                    data.setHinshiSaibunrui(detailItems[0]);
                }

            } else if (property.equals(PROP_DETAIL)) {
                Integer index = (Integer) value;
                int partId = TermPart.valueOfName(data.getHinshi()).getIntValue();
                TermClass tc = createTermClass(partId).get(index);
                String termClass;
                if (tc != null) {
                    termClass = tc.getName();
                } else {
                    termClass = ""; //$NON-NLS-1$
                }
                modify = !equals(data.getHinshiSaibunrui(), termClass);
                data.setHinshiSaibunrui(termClass);
            }
            viewer.update(data, null);
            /* element のisDirty をチェックする */
            if (modify) {
                uncommittedItems.add(data);
                dirtyChanged();
            }
        }


        private boolean equals(Object o1, Object o2) {
            if (o1 == null) {
                if (o2 == null) {
                    return true;
                } else {
                    return false;
                }
            }
            return o1.equals(o2);
        }


        /**
         * @return 共通辞書と辞書フォルダからJuman辞書を除いたユーザ辞書を取ってくる
         */
        private DicNameItem getDestDicItem() {
            Collection<ICoronaDic> ret = getDictionaries();
            for (Iterator<ICoronaDic> itr = ret.iterator(); itr.hasNext();) {
                ICoronaDic dic = itr.next();
                if (DicType.JUMAN.equals(((IUserDic) dic).getDicType())) {
                    itr.remove();
                }
            }
            return new DicNameItem(ret.toArray(new ICoronaDic[ret.size()]));
        }

        private class DicNameItem extends ComboItem<ICoronaDic> {
            public DicNameItem(ICoronaDic[] items) {
                super(items, true);
            }


            @Override
            protected String toName(ICoronaDic item) {
                return item.getName();
            }
        }


        private PrivateTermClass createTermClass(int partId) {
            return new PrivateTermClass(TermClass.values(partId).toArray(new TermClass[TermClass.values(partId).size()]));
        }
    }
}
