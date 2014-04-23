/**
 * @version $Id: TmpPatternDicLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/23 10:20:26
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;

/**
 * @author shingo-takahashi
 */
public class TmpPatternDicLabelProvider extends PatternDicLabelProvider {
    private IPatternDic dic;


    public TmpPatternDicLabelProvider(IPatternDic pdic) {
        super();
        this.dic = pdic;
    }


    @Override
    public String getText(Object element) {
        if (element instanceof Link) {
            return dic.getItemName(((Link) element).getId()) + " (参照)";
        }
        return super.getText(element);
    }
}
