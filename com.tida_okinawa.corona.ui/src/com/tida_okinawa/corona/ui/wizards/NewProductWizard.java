/**
 * @version $Id: NewProductWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03 15:10:05
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * @author kousuke-morishima
 */
public class NewProductWizard extends BasicNewResourceWizard {

    public NewProductWizard() {
    }


    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        super.init(workbench, selection);
        setWindowTitle(Messages.NewProductWizard_windowTitleEntryProduct);
    }

    private NewProductCreationWizardPage page1 = null;


    @Override
    public void addPages() {
        page1 = new NewProductCreationWizardPage("", getSelection()); //$NON-NLS-1$
        addPage(page1);
    }


    @Override
    public boolean performFinish() {
        page1.createProducts();
        return true;
    }

}
