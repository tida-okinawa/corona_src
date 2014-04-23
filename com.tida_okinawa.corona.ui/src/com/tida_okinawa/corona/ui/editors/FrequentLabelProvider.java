/**
 * @version $Id: FrequentLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/14 19:15:05
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.tida_okinawa.corona.correction.frequent.FrequentRecord;


/**
 * @author takayuki-matsumoto
 */
public class FrequentLabelProvider extends LabelProvider implements ITableLabelProvider {

    /**
     * コンストラクター
     */
    public FrequentLabelProvider() {


    }


    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }


    @Override
    public String getColumnText(Object element, int columnIndex) {
        String result = "";

        if (element instanceof FrequentRecord) {
            FrequentRecord fr = (FrequentRecord) element;
            switch (columnIndex) {
            case 0: // 用語
                // result = words[0];
                result = fr.getGenkei();
                break;
            case 1: // 登録回数
                result = "" + fr.getCount();
                break;
            case 2: // 登録先辞書
                if (fr.getDestDictionary() != null) {
                    result = fr.getDestDictionary().getName();
                }
                break;
            case 3: // 品詞
                result = fr.getHinshi();
                break;
            case 4: // 品詞詳細
                result = fr.getHinshiSaibunrui();
                break;
            case 5: // 登録元辞書
                result = fr.getRegisteredDics();
                break;
            default:
                break;
            }
        }
        return result;
    }
}
