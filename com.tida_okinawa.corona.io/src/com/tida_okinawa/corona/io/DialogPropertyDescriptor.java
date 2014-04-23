/**
 * @version $Id: DialogPropertyDescriptor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/21 13:09:10
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * ダイアログを開いて編集できるプロパティ値のデスクリプタ.
 * 
 * @author kousuke-morishima
 */
public class DialogPropertyDescriptor extends PropertyDescriptor {

    /**
     * @param id
     *            property id
     * @param displayName
     *            property name
     */
    public DialogPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }


    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new DialogCellEditor(parent) {
            @Override
            protected Object openDialogBox(Control cellEditorWindow) {
                Shell shell = Display.getDefault().getActiveShell();
                ChangePropertyDialog d = getDialog(shell);
                if (d.open() == Dialog.OK) {
                    Object[] results = d.getResults();
                    StringBuilder value = new StringBuilder(""); //$NON-NLS-1$
                    for (Object o : results) {
                        value.append(", "); //$NON-NLS-1$
                        value.append(o.toString());
                    }
                    return value.toString().substring(2);
                }
                return null;
            }
        };
    }


    protected ChangePropertyDialog getDialog(Shell shell) {
        return new ChangePropertyDialog(shell);
    }

    protected List<Object> initialValues;


    /**
     * ダイアログを開いたときに選択しておくアイテムをセットする
     * 
     * @param value
     *            選択しておくアイテム
     */
    public void setInitialValue(Object value) {
        setInitialValues(new Object[] { value });
    }


    /**
     * ダイアログを開いたときに選択しておくアイテムをセットする
     * 
     * @param values
     *            選択しておくアイテム
     */
    public void setInitialValues(Object[] values) {
        initialValues = new ArrayList<Object>();
        for (Object o : values) {
            initialValues.add(o);
        }
    }


    /**
     * ダイアログを開いたときに選択しておくアイテムをセットする
     * 
     * @param values
     *            選択しておくアイテム
     */
    public void setDefaultValueList(List<Object> values) {
        this.initialValues = values;
    }

    static class ChangePropertyDialog extends Dialog {
        protected ChangePropertyDialog(Shell parentShell) {
            super(parentShell);
        }


        public Object[] getResults() {
            return new Object[0];
        }


        protected String getTitle() {
            return ""; //$NON-NLS-1$
        }


        @Override
        protected boolean isResizable() {
            return true;
        }


        @Override
        protected void setShellStyle(int newShellStyle) {
            newShellStyle |= (isResizable()) ? SWT.RESIZE : SWT.NONE;
            super.setShellStyle(newShellStyle);
        }


        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(getTitle());
        }
    }
}
