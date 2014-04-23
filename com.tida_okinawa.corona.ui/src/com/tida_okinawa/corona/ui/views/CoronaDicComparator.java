/**
 * @version $Id: CoronaDicComparator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/15 17:09:21
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views;

import java.util.Comparator;

import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * @author kousuke-morishima
 */
public class CoronaDicComparator implements Comparator<ICoronaDic> {
    final static Integer CATEGORY_NONE = 0;
    final static Integer CATEGORY_SPECIAL = 1;
    final static Integer CATEGORY_CATEGORY = 2;
    final static Integer CATEGORY_COMMON = 3;
    final static Integer CATEGORY_LABEL = 4;
    final static Integer CATEGORY_FLUC = 5;
    final static Integer CATEGORY_SYNONYM = 6;
    final static Integer CATEGORY_PATTERN = 7;
    final static int CATEGORY_JUMAN = 9;


    public int category(Object element) {
        if (element instanceof IUserDic) {
            IUserDic uDic = (IUserDic) element;
            if (DicType.SPECIAL.equals(uDic.getDicType())) {
                return CATEGORY_SPECIAL;
            } else if (DicType.CATEGORY.equals(uDic.getDicType())) {
                return CATEGORY_CATEGORY;
            } else if (DicType.COMMON.equals(uDic.getDicType())) {
                return CATEGORY_COMMON;
            } else if (DicType.JUMAN.equals(uDic.getDicType())) {
                return CATEGORY_JUMAN;
            }
        } else if (element instanceof ILabelDic) {
            return CATEGORY_LABEL;
        } else if (element instanceof IFlucDic) {
            return CATEGORY_FLUC;
        } else if (element instanceof ISynonymDic) {
            return CATEGORY_SYNONYM;
        } else if (element instanceof IPatternDic) {
            return CATEGORY_PATTERN;
        }
        return CATEGORY_NONE;
    }


    @Override
    public int compare(ICoronaDic d1, ICoronaDic d2) {
        int ret = category(d1) - category(d2);
        if (ret == 0) {
            return d1.getName().compareTo(d2.getName());
        }
        return ret;
    }

}
