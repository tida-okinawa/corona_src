/**
 * @version $Id: LabelDicContentProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/01 12:10:38
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;


import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.ui.editors.LabelDicEditor.LabelRecords;

/**
 * @author kousuke-morishima
 */
public class LabelDicContentProvider implements ITreeContentProvider {
    private static final Object[] EMPTY_ARRAY = new Object[0];


    @Override
    public Object[] getElements(Object input) {
        /* #765 ラベルのリストの場合 */
        if (input instanceof LabelRecords) {
            return ((LabelRecords) input).toArray();
        }
        /* ラベル辞書（最上位）の場合 */
        if (input instanceof DicEditorInput) {
            /* 辞書から子要素を抜き出して返す */
            ICoronaDic dic = ((DicEditorInput) input).getDictionary();
            return ((ILabelDic) dic).getItems().toArray();
        }
        return getChildren(input);
    }


    @Override
    public Object[] getChildren(Object parent) {
        /* 辞書アイテムの場合 */
        if (parent instanceof ILabel) {
            /* 辞書アイテムから子要素を抜き出して返す */
            return ((ILabel) parent).getChildren().toArray();
        }
        return EMPTY_ARRAY;/* 空の配列を返す */
    }


    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof ILabel) {
            return !((ILabel) element).getChildren().isEmpty();
        }

        return true;
    }


    @Override
    /* 親要素を取得する処理 */
    public Object getParent(Object element) {

        if (element instanceof ILabel)/* 辞書アイテムの場合 */
        {
            /* 選択された要素の親要素を取得する処理 */
            return ((ILabel) element).getParent();
        }
        return null;/* 親が存在しない場合、nullを返す */
    }


    @Override
    public void dispose() {
    }


    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}
