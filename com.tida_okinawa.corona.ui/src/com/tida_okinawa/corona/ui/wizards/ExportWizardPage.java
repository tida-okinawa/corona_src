/**
 * @version $Id: ExportWizardPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/29 18:12:35
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.dialogs.WizardExportResourcesPage;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;

/**
 * @author kousuke-morishima
 */
public class ExportWizardPage extends WizardExportResourcesPage {

    public ExportWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
    }


    public boolean finish() {
        saveWidgetValues();
        return true;
    }

    /* ****************************************
     * UI
     */
    protected Combo destinationField;


    @Override
    protected void createDestinationGroup(Composite parent) {
        parent = CompositeUtil.defaultComposite(parent, 3);
        CompositeUtil.createLabel(parent, Messages.ExportWizardPage_labelOutputForlder, 90).pack();
        destinationField = new Combo(parent, SWT.DROP_DOWN);
        destinationField.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        destinationField.addModifyListener(new ModifyListener() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void modifyText(ModifyEvent e) {
                updateWidgetEnablements();
            }
        });
        /* Button btn = */CompositeUtil.createBtn(parent, SWT.PUSH, Messages.ExportWizardPage_buttonReference, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog d = new DirectoryDialog(getShell(), SWT.OPEN);
                String folderPath = d.open();
                if (folderPath != null) {
                    destinationField.setText(folderPath);
                }
            }
        });
    }


    @Override
    protected void createOptionsGroup(Composite parent) {
        // no options
    }

    /* ****************************************
     * save/restore
     */
    private static final String PREFIX = "com.tida_okinawa.corona.ui.wizards.ExportWizardPage"; //$NON-NLS-1$
    private static final String KEY_DESTINATION = PREFIX + "KeyDestinationHistory"; //$NON-NLS-1$


    @Override
    protected void saveWidgetValues() {
        IDialogSettings section = getDialogSettings();
        WizardPageBase.saveComboHistory(section, destinationField.getText(), KEY_DESTINATION, 10);
    }


    @Override
    protected void restoreWidgetValues() {
        IDialogSettings section = getDialogSettings();
        if (section.getArray(KEY_DESTINATION) != null) {
            destinationField.setItems(section.getArray(KEY_DESTINATION));
            if (destinationField.getItemCount() > 0) {
                destinationField.select(0);
            }
        }
    }


    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        // Memo 強引にツリーにフォーカスを移している
        Control[] c = ((Composite) getControl()).getChildren();
        if ((c != null) && (c.length > 0)) {
            c[0].setFocus();
        }
        updateWidgetEnablements();
    }


    /* ****************************************
     * validate
     */
    @Override
    protected void handleTypesEditButtonPressed() {
        super.handleTypesEditButtonPressed();
        updatePageCompletion();
    }


    @Override
    protected boolean validateSourceGroup() {
        List<?> resources = getSelectedResources();
        if (resources.size() == 0) {
            setErrorMessage(Messages.ExportWizardPage_errorMessageExport);
            return false;
        }

        return true;
    }


    @Override
    protected boolean validateDestinationGroup() {
        if (!new File(destinationField.getText()).exists()) {
            setErrorMessage(Messages.ExportWizardPage_errorMessageOutputFolder);
            return false;
        }
        return true;
    }


    @Override
    public void handleEvent(Event event) {
    }

}
