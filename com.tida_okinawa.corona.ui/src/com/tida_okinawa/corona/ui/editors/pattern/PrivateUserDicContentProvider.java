/**
 * @version $Id: PrivateUserDicContentProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/01 16:00:09
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * @author wataru-higa
 */
class PrivateUserDicContentProvider implements ITreeContentProvider {
    private final Object[] EMPTY_ARRAY = new Object[0];


    @Override
    public Object[] getElements(Object input) {
        if (input instanceof List<?>) {
            return ((List<?>) input).toArray();
        }
        return EMPTY_ARRAY;
    }


    @Override
    public Object[] getChildren(Object parent) {
        if (parent instanceof IUserDic) {
            return ((IUserDic) parent).getItems().toArray();
        }
        return EMPTY_ARRAY;
    }


    @Override
    public boolean hasChildren(Object element) {
        return element instanceof IUserDic;
    }


    @Override
    public Object getParent(Object element) {
        return null;
    }


    @Override
    public void dispose() {
    }


    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
