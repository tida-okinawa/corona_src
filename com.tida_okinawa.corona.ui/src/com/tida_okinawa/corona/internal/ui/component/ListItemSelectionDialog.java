/**
 * @version $Id: ListItemSelectionDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2010/12/20
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.component;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

/**
 * 一覧からアイテムを選択するUIを提供する
 * 
 * @author KMorishima
 * 
 */
public class ListItemSelectionDialog extends ElementTreeSelectionDialog {

    public ListItemSelectionDialog(Shell parent, ILabelProvider labelProvider) {
        super(parent, labelProvider, createContentProvider());
    }


    protected static ITreeContentProvider createContentProvider() {
        return new ITreeContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }


            @Override
            public void dispose() {
            }


            @Override
            public boolean hasChildren(Object element) {
                return false;
            }


            @Override
            public Object getParent(Object element) {
                return null;
            }


            @Override
            public Object[] getElements(Object input) {
                return ((List<?>) input).toArray();
            }


            @Override
            public Object[] getChildren(Object parent) {
                return new Object[0];
            }
        };
    }
}
