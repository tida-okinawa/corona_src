/**
 * @version $Id: RenameInputDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/10 16:37:22
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author kyohei-miyazato
 */
public class RenameInputDialog extends InputDialog {
    private String ext;
    private IInputValidator validator;


    /**
     * @param parentShell
     * @param dialogTitle
     * @param dialogMessage
     * @param initialValue
     * @param validator
     * @param ext
     */
    public RenameInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator, String ext) {
        super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
        this.ext = ext;
        this.validator = validator;
    }


    @Override
    protected void validateInput() {
        String errorMessage = null;
        Text text = getText();
        String inputName = text.getText();
        if (validator != null) {
            if (!"".equals(ext)) {
                if (inputName.endsWith(ext)) {
                    errorMessage = validator.isValid(inputName);
                } else {
                    errorMessage = validator.isValid(inputName + ext);
                }
            } else {
                errorMessage = validator.isValid(inputName + ext);
            }
        }
        setErrorMessage(errorMessage);
    }
}
