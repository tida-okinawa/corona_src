/**
 * @version $Id: LabelDicLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/01 12:11:56
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.tida_okinawa.corona.io.model.dic.ILabel;

/**
 * @author kousuke-morishima
 */
public class LabelDicLabelProvider extends LabelProvider {
    @Override
    /* 表示文字を取得する処理 */
    public String getText(Object element) {

        /*
         * LabelDicContentProvider.javaでは”DicEditorInpu”の子要素を返す処理のみ。
         * よって、”DicEditorInpu”がこの処理に来ることはない
         */
        if (element instanceof ILabel) {
            return ((ILabel) element).getName();/* 要素の名前を取得 */
        }
        return super.getText(element);
    }


    @Override
    public Image getImage(Object element) {
        return super.getImage(element);
    }
}
