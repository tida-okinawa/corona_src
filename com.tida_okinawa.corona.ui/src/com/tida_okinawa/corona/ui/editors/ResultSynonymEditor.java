/**
 * @version $Id: ResultSynonymEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/12 01:36:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * ゆらぎ・同義語補正結果表示
 * 
 * {@link ResultMorphemeEditor} に 置き換え前の表記の列を追加
 * 
 * @author imai
 * 
 */
public class ResultSynonymEditor extends ResultMorphemeEditor {

    /**
     * エディターID。
     * この変数はResultMorphemeEditorの同盟変数を意図的にシャドーイングしている。
     */
    @SuppressWarnings("hiding")
    public static final String EDITOR_ID = "com.tida_okinawa.corona.ui.editor.synonymresulteditor";


    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        // 置き換え前の表記の列を追加
        CompositeUtil.createColumn(morphemeTreeViewer, "同義語", 160);

        final ITableLabelProvider orgProvider = (ITableLabelProvider) morphemeTreeViewer.getLabelProvider();
        morphemeTreeViewer.setLabelProvider(new ColorTableLabelProvider() {
            @Override
            public void removeListener(ILabelProviderListener listener) {
                orgProvider.removeListener(listener);
            }


            @Override
            public boolean isLabelProperty(Object element, String property) {
                return orgProvider.isLabelProperty(element, property);
            }


            @Override
            public void dispose() {
                orgProvider.dispose();
            }


            @Override
            public void addListener(ILabelProviderListener listener) {
                orgProvider.addListener(listener);
            }


            @Override
            public String getColumnText(Object element, int columnIndex) {
                String text = orgProvider.getColumnText(element, columnIndex);
                if (element instanceof MorphemeElement) {
                    MorphemeElement morphme = (MorphemeElement) element;
                    if (columnIndex == 7) {
                        text = morphme.getOriginalHyouki();
                    }
                }
                return text;
            }


            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                return orgProvider.getColumnImage(element, columnIndex);
            }
        });
    }

    private static class ColorTableLabelProvider extends TableLabelProvider implements ITableColorProvider {
        public ColorTableLabelProvider() {
        }


        @Override
        public Color getForeground(Object element, int columnIndex) {
            if (!(element instanceof MorphemeElement)) {
                return null;
            }
            MorphemeElement morpheme = (MorphemeElement) element;
            if (morpheme.getOriginalHyouki() != null) {
                if (columnIndex == 1) {
                    return foreground;
                }
            }

            return null;
        }


        @Override
        public Color getBackground(Object element, int columnIndex) {
            return null;
        }
    }

    /**
     * オレンジ
     */
    static final Color foreground = new Color(null, 255, 130, 0);


    @Override
    void setResultText(SyntaxStructure ss) {
        String text = ss.getText();
        resultText.setText(text);

        int fromIndex = 0;
        for (MorphemeElement element : ss.getMorphemeElemsnts()) {
            if (element.getOriginalHyouki() != null) {
                String hyouki = element.getHyouki();
                int start = text.indexOf(element.getHyouki(), fromIndex);
                int length = hyouki.length();

                StyleRange style = new StyleRange(start, length, foreground, null);
                style.fontStyle = SWT.BOLD;
                resultText.setStyleRange(style);

                fromIndex = start + length;
            }
        }
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySheetPage.class)) {
            return new PropertySheetPage();
        }
        return super.getAdapter(adapter);
    }
}
