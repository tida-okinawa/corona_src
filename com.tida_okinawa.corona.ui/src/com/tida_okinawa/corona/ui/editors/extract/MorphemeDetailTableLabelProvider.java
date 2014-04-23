/**
 * @version $Id: MorphemeDetailTableLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/08 16:59:36
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * @author s.takuro
 *         #177 パターン自動生成（係り受け抽出）
 */
public class MorphemeDetailTableLabelProvider extends LabelProvider implements IMorphemeDetailTableLabelProvider {

    Map<String, IExtractRelationElement> prevDetailMap = null;
    private Color color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);


    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof IExtractRelationElement) {
            IExtractRelationElement ere = (IExtractRelationElement) element;

            switch (columnIndex) {
            case 0: // チェックボックス
                return ""; //$NON-NLS-1$
            case 1: // 係り元
                return createModificationString(ere);
            case 2: // 係り先
                return createModificationString(ere.getDependDestination());
            }
            return ""; //$NON-NLS-1$
        }
        return null;
    }


    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }


    @Override
    public Color getForeground(Object element) {
        if (prevDetailMap != null) {
            if (element instanceof IExtractRelationElement) {
                IExtractRelationElement ere = (IExtractRelationElement) element;
                IExtractRelationElement ereDst = ere.getDependDestination();
                StringBuilder ereHyouki = new StringBuilder(100);
                ereHyouki.append(ere.getHyouki()).append(" ").append(ereDst.getHyouki()); //$NON-NLS-1$
                /* 要素が登録済みアイテムのマップに含まれる場合 */
                if (prevDetailMap.containsKey(ereHyouki.toString())) {
                    if (prevDetailMap.get(ereHyouki.toString()).getCompletion()) {
                        return color;
                    }
                }
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


    @Override
    public void setPrevDetailMap(Map<String, IExtractRelationElement> prevDetailMap) {
        this.prevDetailMap = prevDetailMap;
    }


    private static String createModificationString(IExtractRelationElement ere) {
        if ("".equals(ere.getHyouki())) { //$NON-NLS-1$
            return Messages.EXTRACT_RELATION_WILDCARD;
        }
        StringBuilder str = new StringBuilder(100);
        List<MorphemeElement> morphemes = ere.getMorphemes();
        for (MorphemeElement morpheme : morphemes) {
            if (morpheme == null) {
                str.append(Messages.EXTRACT_RELATION_WILDCARD);
            } else {
                str.append(morpheme.getHyouki());
            }
        }
        return str.toString();
    }
}
