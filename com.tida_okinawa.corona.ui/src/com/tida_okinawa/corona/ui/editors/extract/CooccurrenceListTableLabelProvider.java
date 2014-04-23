/**
 * @version $Id: CooccurrenceListTableLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/22 14:21:32
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author s.takuro
 *         #173 パターン自動生成（共起抽出）
 */
public class CooccurrenceListTableLabelProvider extends LabelProvider implements ICooccurrenceListTableLabelProvider {

    private Color color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);


    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof IExtractCooccurrenceElement) {
            IExtractCooccurrenceElement ece = (IExtractCooccurrenceElement) element;
            switch (columnIndex) {
            case 0: // パターンの種類
                return (ece.getPatternType() != null) ? ece.getPatternType() : "";
            case 1: // 出現回数
                return ece.getCount();
            default: // 単語（エラーの場合は空文字を返す）
                return ece.getTerm(columnIndex - 1);
            }
        }
        return "";
    }


    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }


    @Override
    public Color getForeground(Object element) {
        if (element instanceof IExtractCooccurrenceElement) {
            if (((IExtractCooccurrenceElement) element).getCompletion()) {
                return color;
            }
        }
        return null;
    }


    @Override
    public Color getBackground(Object element) {
        return null;
    }


    @Override
    public void dispose() {
        color.dispose();
        super.dispose();
    }
}
