/**
 * @version $Id: PrivateUserDicLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/01 16:00:09
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.Icons;

/**
 * TermDetailsPageで表示するダイアログ用のラベルプロバイダー
 * 
 * @author wataru-higa
 */
class PrivateUserDicLabelProvider extends LabelProvider {
    private Icons icons = Icons.INSTANCE;


    @Override
    public String getText(Object element) {
        if (element instanceof IUserDic) {
            return ((IUserDic) element).getName();
        } else if (element instanceof ITerm) {
            ITerm t = (ITerm) element;
            StringBuilder ret = new StringBuilder(t.getValue()).append(" : ").append(t.getReading());
            return ret.toString();
        }
        return super.getText(element);
    }


    @Override
    public Image getImage(Object element) {
        if (element instanceof ITerm) {
            return null;
        } else if (element instanceof IUserDic) {
            IUserDic dic = (IUserDic) element;
            switch (dic.getDicType()) {
            case COMMON:
                return Icons.INSTANCE.get(Icons.IMG_DIC_COMMON);
            case SPECIAL:
                return Icons.INSTANCE.get(Icons.IMG_DIC_SPECIAL);
            case CATEGORY:
                return Icons.INSTANCE.get(Icons.IMG_DIC_CATEGORY);
            default:
                return icons.get(Icons.IMG_LIBRARY);
            }
        }
        return null;
    }
}
