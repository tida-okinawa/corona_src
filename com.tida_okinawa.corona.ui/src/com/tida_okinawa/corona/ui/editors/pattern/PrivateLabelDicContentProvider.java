/**
 * @version $Id: PrivateLabelDicContentProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/07 14:44:57
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.util.List;

import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.ui.editors.LabelDicContentProvider;

/**
 * @author kousuke-morishima
 */
public class PrivateLabelDicContentProvider extends LabelDicContentProvider {
    @Override
    public Object[] getElements(Object input) {
        if (input instanceof List<?>) {
            return ((List<?>) input).toArray();
        }
        return super.getElements(input);
    }


    @Override
    public Object[] getChildren(Object parent) {
        if (parent instanceof ILabelDic) {
            return ((ILabelDic) parent).getItems().toArray();
        }
        return super.getChildren(parent);
    }


    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof ILabelDic) {
            return true;
        }
        return super.hasChildren(element);
    }
}
