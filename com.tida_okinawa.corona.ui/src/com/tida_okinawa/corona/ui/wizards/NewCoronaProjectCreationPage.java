/**
 * @version $Id: NewCoronaProjectCreationPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/02 10:03:54
 * @author KMorishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.tida_okinawa.corona.internal.ui.util.ProjectNameValidator;

/**
 * 
 * @author KMorishima
 */
public class NewCoronaProjectCreationPage extends WizardNewProjectCreationPage {
    private static final String PAGE_TITLE = "Coronaプロジェクト作成";


    /**
     * @param pageName
     */
    public NewCoronaProjectCreationPage(String pageName) {
        super(pageName);
        setTitle(PAGE_TITLE);
    }


    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
    }


    @Override
    protected boolean validatePage() {
        if (!super.validatePage()) {
            return false;
        }

        String name = getProjectName();
        String message = ProjectNameValidator.isValid(name);
        if (message != null) {
            setErrorMessage(message);
            return false;
        }

        return true;
    }
}
