/**
 * @version $Id: ModificationDestinationDetailsPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/25 10:28:49
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author kousuke-morishima
 */
public class ModificationDestinationDetailsPage extends PatternDetailsPage {

    public ModificationDestinationDetailsPage(FormEditor editor) {
        super(editor);
    }


    @Override
    public void createContents(Composite parent) {
        parent.setLayout(new FillLayout());
        Section section = createSection(parent, "係り先の設定", DESCRIPTION_MODIFI_DST);
        Composite client = kit.createComposite(section);
        section.setClient(client);
    }


    @Override
    protected void selectionChanged(IFormPart part, Object selectedObject) {
    }


    @Override
    protected void doSave() {
    }


    @Override
    protected void commit() {
    }

}
