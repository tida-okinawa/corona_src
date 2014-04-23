/**
 * @version $Id: UIElementLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 17:07:00
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views;

import org.eclipse.swt.graphics.Image;

import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;

/**
 * @author kousuke-morishima
 */
public class UIElementLabelProvider extends CoronaElementLabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof IUIElement) {
            return element.toString();
        }
        return super.getText(element);
    }


    @Override
    public Image getImage(Object element) {
        if (element instanceof IUIElement) {
            element = ((IUIElement) element).getObject();
        }
        return super.getImage(element);
    }
}
