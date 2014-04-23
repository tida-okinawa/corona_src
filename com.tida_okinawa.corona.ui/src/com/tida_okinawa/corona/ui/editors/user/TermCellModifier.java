/**
 * @version $Id: TermCellModifier.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/27 14:05:54
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPartSite;

import com.tida_okinawa.corona.correction.erratum.Erratum;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author kousuke-morishima
 */
public class TermCellModifier implements ICellModifier {
    private IWorkbenchPartSite site;
    private PrivateTermPart termParts;


    public TermCellModifier(IWorkbenchPartSite site) {
        this.site = site;

        init();
    }


    private void init() {
        properties = new ArrayList<String>();
        properties.add(PROP_WORD);
        properties.add(PROP_PART);
        properties.add(PROP_CLASS);

        if (UIActivator.isAlpha()) {
            /* α版対応　品詞だけをコンボに出力 */
            termParts = new PrivateTermPart(new TermPart[] { TermPart.NOUN });
        } else {
            TermPart[] part = TermPart.values();
            /* なしは表示しない */
            TermPart[] part2 = new TermPart[part.length - 1];
            for (int i = 1; i < part.length; i++) {
                part2[i - 1] = part[i];
            }
            termParts = new PrivateTermPart(part2);
        }
    }

    /* ****************************************
     * Litener
     */
    private ListenerList listeners = new ListenerList();


    /**
     * セルの編集が終わったタイミングで、通知を受けるリスナーを登録する。
     * handleEventが、{@link CellModifierEvent}と一緒に呼び出される。
     * 
     * @param listener
     */
    public void addFinishModifyListener(Listener listener) {
        listeners.add(listener);
    }


    public void removeFinishModifyLitener(Listener listener) {
        listeners.remove(listener);
    }


    private void fireEvent(Object element, String property, Object value) {
        CellModifierEvent e = new CellModifierEvent();
        e.data = element;
        e.property = property;
        e.value = value;
        Object[] listeners = this.listeners.getListeners();
        for (Object l : listeners) {
            ((Listener) l).handleEvent(e);
        }
    }

    public static class CellModifierEvent extends Event {
        /**
         * 編集したセルを表すプロパティ
         */
        public String property;
        /**
         * 編集確定後の値
         */
        public Object value;
    }

    /* ****************************************
     * getter
     */
    public static final String PROP_WORD = "word";
    public static final String PROP_PART = "part";
    public static final String PROP_CLASS = "wordClass";

    private List<String> properties;


    /**
     * @return このCellModifierで編集可能なプロパティを返す
     */
    public List<String> properties() {
        return properties;
    }

    private List<CellEditor> editors;
    private ComboBoxCellEditor detailComboEditor;


    public List<CellEditor> createEditors(Table tbl) {
        if (editors == null) {
            editors = new ArrayList<CellEditor>(3);
            /* 品詞詳細アイテム設定 */
            detailComboEditor = new ComboBoxCellEditor(tbl, new String[0]);
            editors.add(new TextCellEditor(tbl));
            editors.add(new ComboBoxCellEditor(tbl, termParts.names));
            editors.add(detailComboEditor);
        }

        return editors;
    }


    /* ****************************************
     * Modifier
     */
    @Override
    public void modify(Object element, String property, Object value) {
        // ユーザが編集を確定した時に呼ばれる
        // 値を、モデルに反映させる。
        ITerm term = (ITerm) ((TableItem) element).getData();
        if (property.equals(PROP_WORD)) {
            String word = (String) value;
            Erratum erratum = new Erratum();
            String corrected = erratum.convert(word);
            /* 全角へ変換 */
            term.setValue(corrected);

            // IllegalWord メッセージ
            if (erratum.hasIllegalWords()) {
                MessageDialog.openConfirm(site.getShell(), "単語", "無効な文字列が含まれています");
            }

            /* 品詞が未入力だったら、品詞にデフォルト値を設定 */
            if (!word.trim().equals("")) {
                if (term.getTermPart().equals(TermPart.NONE)) {
                    term.setTermPart(TermPart.NOUN);
                    /* 品詞詳細アイテム設定 */
                    PrivateTermClass termClasses = createTermClass(TermPart.NOUN.getIntValue());
                    detailComboEditor.setItems(termClasses.names);
                    /* 品詞詳細アイテム の最初を選択 */
                    term.setTermClass(termClasses.get(0));
                }
            }
        } else if (property.equals(PROP_PART)) {
            int index = (Integer) value;
            /* 品詞設定 */
            if (index >= 0) {
                /* 品詞の値が変わったとき */
                TermPart newPart = termParts.items[index];
                if (!newPart.equals(term.getTermPart())) {
                    term.setTermPart(newPart);
                    /* 品詞詳細取得 */
                    PrivateTermClass termClasses = createTermClass(newPart.getIntValue());
                    detailComboEditor.setItems(termClasses.names);
                    term.setTermClass(termClasses.get(0));
                }
            }

        } else if (property.equals(PROP_CLASS)) {
            int index = (Integer) value;
            if (index != -1) {
                /* 品詞に対する品詞詳細リスト */
                term.setTermClass(createTermClass(term.getTermPart().getIntValue()).get(index));
            }
        }

        fireEvent(term, property, value);
    }


    @Override
    public Object getValue(Object element, String property) {
        // canModifyでtrueを返すと呼ばれる
        // 編集対象になった項目の、現在の値を返す
        ITerm term = (ITerm) element;
        if (property.equals(PROP_WORD)) {
            return term.getValue();
        } else if (property.equals(PROP_PART)) {
            /* ”なし”の場合は-１ */
            if (TermPart.NONE.equals(term.getTermPart())) {
                return -1;
            }
            return termParts.getIndex(term.getTermPart());
        } else if (property.equals(PROP_CLASS)) {
            /* 品詞詳細アイテム設定 */
            PrivateTermClass termClasses = createTermClass(term.getTermPart().getIntValue());
            detailComboEditor.setItems(termClasses.names);
            /* 品詞に対する品詞詳細リスト */
            if (TermClass.NONE.equals(term.getTermClass())) {
                return -1;
            }
            return termClasses.getIndex(term.getTermClass());
        }

        return null;
    }


    @Override
    public boolean canModify(Object element, String property) {
        for (String prop : properties) {
            if (prop.equals(property)) {
                return true;
            }
        }
        return false;
    }


    private static PrivateTermClass createTermClass(int partId) {
        /* 品詞詳細アイテム設定 */
        return new PrivateTermClass(TermClass.values(partId).toArray(new TermClass[TermClass.values(partId).size()]));
    }
}
