/**
 * @version $Id: IUIElementAdapterFactory.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/23 16:27:26
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui;

import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;

import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;

/**
 * @author kousuke-morishima
 */
public class IUIElementAdapterFactory implements IAdapterFactory {
    private static final Class<?>[] CLASSES = new Class[] { IResource.class };


    @Override
    public Object getAdapter(Object adaptable, @SuppressWarnings("rawtypes") Class key) {
        if (IResource.class.equals(key)) {
            if (adaptable instanceof IUIElement) {
                return ((IUIElement) adaptable).getResource();
            }
        }
        return null;
    }


    @SuppressWarnings("rawtypes")
    @Override
    public Class[] getAdapterList() {
        return Arrays.copyOf(CLASSES, CLASSES.length);
    }

}
