/**
 * @version $Id: FrequentTermUndefPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/13 17:23:12
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.forms.editor.FormEditor;

import com.tida_okinawa.corona.correction.frequent.FrequentRecord;

/**
 * @author takayuki-matsumoto
 */
public class FrequentTermUndefPage extends FrequentTermPage {

    /**
     * @param editor
     */
    public FrequentTermUndefPage(FormEditor editor) {
        super(editor, "FrequentTermPage.UniqueIdentifier", "未定義語");
        formTitle = "頻出用語抽出フォーム（未定義語）";
    }


    @Override
    protected ViewerFilter createFilter() {
        ViewerFilter vf = new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof FrequentRecord) {
                    FrequentRecord fr = (FrequentRecord) element;
                    if ("未定義語".equals(fr.getHinshi())) {
                        fr.setHinshi("");
                        fr.setHinshiSaibunrui("");
                        fr.setYomi("");
                        fr.setUndefine(true);
                        return true;
                    }
                    if (fr.isUndefine()) {
                        return true;
                    }
                    return false;
                }
                return true;
            }
        };
        return vf;
    }
}
