/**
 * @version $Id: MorphemeListTableLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/08 16:58:46
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
 *         #177 パターン自動生成（係り受け抽出）
 */
public class MorphemeListTableLabelProvider extends LabelProvider implements IMorphemeListTableLabelProvider {

    private Color color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);


    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof IExtractRelationElement) {
            IExtractRelationElement ere = (IExtractRelationElement) element;

            switch (columnIndex) {
            case 0: //出現回数
                return String.valueOf(ere.getCount());
            case 1: //表記
                return ere.getHyouki();
            case 2: //係り先
                IExtractRelationElement dst = ere.getDependDestination();
                return (dst != null) ? dst.getHyouki() : ""; //$NON-NLS-1$
            }
        }
        return ""; //$NON-NLS-1$
    }


    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }


    @Override
    public Color getForeground(Object element) {
        if (element instanceof IExtractRelationElement) {
            if (((IExtractRelationElement) element).getCompletion()) {
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
