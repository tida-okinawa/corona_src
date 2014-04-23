/**
 * @version $Id: DicEditorDisposer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/21 2:24:58
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.List;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * @author kousuke-morishima
 */
public class DicEditorDisposer implements IPartListener {
    private IWorkbenchPart part;
    private List<ICoronaDic> dics;


    /**
     * @param editor
     *            このインスタンスを登録する先のエディタ<br />
     *            このエディタが閉じた時だけ働く
     * @param dics
     *            エディタが閉じたときに、updateを呼び出す辞書群
     */
    public DicEditorDisposer(IWorkbenchPart part, List<ICoronaDic> dics) {
        this.part = part;
        this.dics = dics;
    }


    @Override
    public void partClosed(IWorkbenchPart part) {
        if (part.equals(this.part)) {
            for (ICoronaDic dic : dics) {
                if (dic.isDirty()) {
                    dic.update();
                }
            }
        }
    }


    @Override
    public void partActivated(IWorkbenchPart part) {
    }


    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
    }


    @Override
    public void partDeactivated(IWorkbenchPart part) {
    }


    @Override
    public void partOpened(IWorkbenchPart part) {
    }


    public void addDictionaries(List<ICoronaDic> dics) {
        this.dics.addAll(dics);
    }


    public void addDictionary(ICoronaDic dic) {
        dics.add(dic);
    }
}
