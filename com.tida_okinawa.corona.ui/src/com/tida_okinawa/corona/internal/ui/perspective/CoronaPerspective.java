/**
 * @version $Id: CoronaPerspective.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/01 14:13:19
 * @author KMorishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * 
 * @author KMorishima
 */
public class CoronaPerspective implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();

        IFolderLayout left = layout.createFolder("navigator", IPageLayout.LEFT, 0.20f, editorArea);
        left.addView("org.eclipse.ui.navigator.ProjectExplorer");

        IFolderLayout bottom = layout.createFolder("console", IPageLayout.BOTTOM, 0.75f, editorArea);
        bottom.addView("org.eclipse.ui.views.PropertySheet");
        bottom.addView("org.eclipse.ui.console.ConsoleView");
        bottom.addView("org.eclipse.pde.runtime.LogView");

        IFolderLayout right = layout.createFolder("database", IPageLayout.RIGHT, 0.75f, editorArea);
        right.addView("com.tida_okinawa.corona.ui.DataBaseView");

        layout.addNewWizardShortcut("com.tida_okinawa.corona.ui.wizard.new.coronaProject");
        layout.addNewWizardShortcut("com.tida_okinawa.corona.ui.wizard.new.product");
        layout.addNewWizardShortcut("com.tida_okinawa.corona.ui.wizard.new.userdictionary");
        layout.addNewWizardShortcut("com.tida_okinawa.corona.ui.wizard.new.commondictionary");
        layout.addNewWizardShortcut("com.tida_okinawa.corona.ui.wizard.new.domaindictionary");
        layout.addNewWizardShortcut("com.tida_okinawa.corona.ui.wizard.new.patterndictionary");
        layout.addNewWizardShortcut("com.tida_okinawa.corona.ui.wizard.new.labelDictionary");
        layout.addNewWizardShortcut("com.tida_okinawa.corona.ui.wizard.new.flucDictionary");
        layout.addNewWizardShortcut("com.tida_okinawa.corona.ui.wizard.new.synonymDictionary");
    }

}
