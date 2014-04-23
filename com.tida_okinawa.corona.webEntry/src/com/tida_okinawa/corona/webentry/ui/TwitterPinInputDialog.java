/**
 * @version $Id: TwitterPinInputDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/20 16:08:04
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.tida_okinawa.corona.webentry.Messages;
import com.tida_okinawa.corona.webentry.twitter.TwitterConnection;

/**
 * Twitterの認証ページを開きPINを取得する為のダイアログ
 * 
 * @author yukihiro-kinjo
 * 
 */
public class TwitterPinInputDialog extends Dialog {

    private Text textPinInput = null;
    private String pin = ""; //$NON-NLS-1$
    private TwitterConnection connection;


    protected TwitterPinInputDialog(Shell parentShell, TwitterConnection connection) {
        super(parentShell);
        this.connection = connection;
    }


    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TwitterPinInputDialog_InputPin);
    }


    @Override
    protected Control createDialogArea(Composite parent) {
        /* UI初期化 */
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new FormLayout());

        Button buttonOpenAuthWebPage = new Button(composite, SWT.NONE);
        buttonOpenAuthWebPage.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* Twitterの認証ページを開く */
                if (!(connection.twitterFistConnectOpenAuthPage())) {
                    MessageDialog.openWarning(getShell(), Messages.TwitterPinInputDialog_Error, Messages.TwitterPinInputDialog_AuthStartFailed);
                }
            }
        });
        FormData fd_buttonOpenAuthWebPage = new FormData();
        fd_buttonOpenAuthWebPage.right = new FormAttachment(100, -162);
        fd_buttonOpenAuthWebPage.left = new FormAttachment(0, 10);
        fd_buttonOpenAuthWebPage.top = new FormAttachment(0, 10);
        buttonOpenAuthWebPage.setLayoutData(fd_buttonOpenAuthWebPage);
        buttonOpenAuthWebPage.setText(Messages.TwitterPinInputDialog_button_OpenTwitterLoginPage);

        Label labelPinInput = new Label(composite, SWT.NONE);
        FormData fd_labelPinInput = new FormData();
        fd_labelPinInput.left = new FormAttachment(buttonOpenAuthWebPage, 0, SWT.LEFT);
        labelPinInput.setLayoutData(fd_labelPinInput);
        labelPinInput.setText(Messages.TwitterPinInputDialog_label_EnterThePinGot);

        textPinInput = new Text(composite, SWT.BORDER);
        fd_labelPinInput.top = new FormAttachment(textPinInput, 3, SWT.TOP);
        FormData fd_textPinInput = new FormData();
        fd_textPinInput.top = new FormAttachment(buttonOpenAuthWebPage, 16);
        fd_textPinInput.right = new FormAttachment(100, -162);
        fd_textPinInput.left = new FormAttachment(labelPinInput, 6);
        textPinInput.setLayoutData(fd_textPinInput);

        return composite;
    }


    @Override
    public boolean close() {
        /* ダイアログが閉じられる際に入力されたPINの内容をフィールドに移動させる */
        pin = this.textPinInput.getText();
        return super.close();
    }


    /**
     * ダイアログに入力されたPINを取得
     * 
     * @return PIN
     */
    public String getPinValue() {
        return pin;
    }


    @Override
    protected boolean isResizable() {
        return true;
    }
}
