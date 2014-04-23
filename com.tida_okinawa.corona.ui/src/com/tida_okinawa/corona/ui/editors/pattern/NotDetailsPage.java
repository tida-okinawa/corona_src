/**
 * @version $Id: NotDetailsPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/25 10:25:58
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
public class NotDetailsPage extends PatternDetailsPage {

    public NotDetailsPage(FormEditor editor) {
        super(editor);
    }


    @Override
    public void createContents(Composite parent) {
        parent.setLayout(new FillLayout());
        Section section = createSection(parent, "Not設定", DESCRIPTION_NOT);
        Composite composite = kit.createComposite(section);
        section.setClient(composite);
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
