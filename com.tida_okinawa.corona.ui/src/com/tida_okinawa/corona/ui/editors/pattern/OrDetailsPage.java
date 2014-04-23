/**
 * @version $Id: OrDetailsPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/25 16:01:18
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.Section;

import com.tida_okinawa.corona.correction.parsing.model.OrOperator;
import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;

/**
 * 
 * @author kyohei-miyazato
 */
public class OrDetailsPage extends PatternDetailsPage {
    private Combo scopeCombo = null;
    private OrOperator or = null;


    public OrDetailsPage(FormEditor editor) {
        super(editor);
    }


    @Override
    public void createContents(Composite parent) {
        parent.setLayout(new FillLayout());

        Section section = createSection(parent, "ORの設定", PatternDetailsPage.DESCRIPTION_OR);
        Composite client = kit.createComposite(section);
        client.setLayout(new GridLayout(3, false));
        client.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label searchLabel = kit.createLabel(client, "検索範囲 : ");
        scopeCombo = CompositeUtil.createCombo(client, new String[] {});
        scopeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ((GridData) scopeCombo.getLayoutData()).horizontalSpan = 2;

        final IInformationControl info = getInformationControl(editor.getSite().getShell(), false, new Point(500, 200));
        setHover(info, searchLabel, TEXT_SCOPE_HOVER);

        setDirtyCheckListenerTo(scopeCombo, SWT.Selection);

        section.setClient(client);
    }


    @Override
    protected void selectionChanged(IFormPart part, Object selectedObject) {
        or = (OrOperator) selectedObject;
        SequenceDetailsPage.doSelectionChanged(or, scopeCombo);
        ((PatternDicPage) ((PatternDicEditor) editor).getActivePageInstance()).update();
    }


    @Override
    protected void doSave() {
        commit();
    }


    @Override
    protected void commit() {


        SearchScopeType search = SearchScopeType.valueOfName(scopeCombo.getText());
        or.setScope(search);
    }
}
