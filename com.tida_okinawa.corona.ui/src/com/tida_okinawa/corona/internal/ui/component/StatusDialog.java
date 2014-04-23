/**
 * @version $Id: StatusDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/25 10:32:55
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.component;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author kousuke-morishima
 */
public class StatusDialog extends Dialog {


    public StatusDialog(Shell shell) {
        super(shell);
    }

    Label message;
    TableViewer viewer;


    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        message = new Label(composite, SWT.NONE);
        message.setText("エラーが発生しました。");

        Table t = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        t.setLinesVisible(true);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 480;
        gd.heightHint = 200;
        t.setLayoutData(gd);

        Button clearButton = new Button(composite, SWT.PUSH);
        clearButton.setText("クリア");
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clearInput();
            }
        });
        clearButton.setVisible(showClearButton);

        viewer = new TableViewer(t);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof IStatus) {
                    Throwable t = ((IStatus) element).getException();
                    if (t == null) {
                        return ((IStatus) element).getMessage();
                    } else {
                        return t.toString() + " : " + ((IStatus) element).getMessage();
                    }
                }
                return element.toString();
            }


            @Override
            public Image getImage(Object element) {
                if (element instanceof IStatus) {
                    if (((IStatus) element).getSeverity() == IStatus.ERROR) {
                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
                    } else if (((IStatus) element).getSeverity() == IStatus.WARNING) {
                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
                    }
                }
                return null;
            }
        });
        viewer.setInput(input);

        return composite;
    }


    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }


    @Override
    protected void okPressed() {
        input.clear();
        super.okPressed();
    }


    void clearInput() {
        input.clear();
        if (viewer != null) {
            viewer.refresh();
        }
    }


    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("エラー");
    }


    @Override
    protected boolean isResizable() {
        return true;
    }

    private boolean showClearButton;


    /**
     * クリアボタンを表示するかどうかを指定する。このメソッドは、 {@link #open()}の呼び出し前にのみ効果を発揮する
     * 
     * @param show
     *            クリアボタンを表示するならtrue
     */
    public void setShowClearButton(boolean show) {
        if (showClearButton != show) {
            showClearButton = show;
        }
    }

    private Set<IStatus> input = new TreeSet<IStatus>(new Comparator<IStatus>() {
        @Override
        public int compare(IStatus o1, IStatus o2) {
            String m2 = o2.getMessage();
            if (m2 == null) {
                return -1;
            }
            String m1 = o1.getMessage();
            if (m1 == null) {
                return 1;
            }
            return m1.compareTo(m2);
        }
    });


    public boolean add(IStatus status) {
        if (input.add(status)) {
            if (viewer != null) {
                viewer.refresh();
            }
            return true;
        }
        return false;
    }


    @Override
    protected void setShellStyle(int newShellStyle) {
        newShellStyle = newShellStyle & (~SWT.APPLICATION_MODAL | SWT.MODELESS);
        super.setShellStyle(newShellStyle);
    }


    /* ****************************************
     * 開いているか
     */
    @Override
    public int open() {
        opened = true;
        return super.open();
    }


    @Override
    public boolean close() {
        opened = false;
        return super.close();
    }

    private boolean opened = false;


    public boolean isOpened() {
        return opened;
    }

}
