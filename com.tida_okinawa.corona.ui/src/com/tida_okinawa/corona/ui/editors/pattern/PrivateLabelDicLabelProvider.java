/**
 * @version $Id: PrivateLabelDicLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/07 14:44:09
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import org.eclipse.swt.graphics.Image;

import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.editors.LabelDicLabelProvider;

/**
 * @author kousuke-morishima
 */
public class PrivateLabelDicLabelProvider extends LabelDicLabelProvider {
    @Override
    public String getText(Object element) {
        if (element instanceof ILabelDic) {
            return ((ILabelDic) element).getName();
        }
        return super.getText(element);
    }


    @Override
    public Image getImage(Object element) {
        if (element instanceof ILabelDic) {
            return Icons.INSTANCE.get(Icons.IMG_BOOK);
        }
        return super.getImage(element);
    }
}
