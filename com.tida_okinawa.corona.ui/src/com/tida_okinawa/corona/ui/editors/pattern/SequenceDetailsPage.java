/**
 * @version $Id: SequenceDetailsPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/25 11:01:01
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

import com.tida_okinawa.corona.correction.parsing.model.IScopePattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.ScopePattern;
import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;
import com.tida_okinawa.corona.correction.parsing.model.Sequence;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;

/**
 * 
 * @author kyohei-miyazato
 */
public class SequenceDetailsPage extends PatternDetailsPage {
    private Combo scopeCombo = null;
    private Sequence sequence = null;


    public SequenceDetailsPage(FormEditor editor) {
        super(editor);
    }


    @Override
    public void createContents(Composite parent) {
        parent.setLayout(new FillLayout());

        Section section = createSection(parent, "連続の設定", PatternDetailsPage.DESCRIPTION_SEQUENCE);
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
        sequence = (Sequence) selectedObject;
        doSelectionChanged(sequence, scopeCombo);
        ((PatternDicPage) ((PatternDicEditor) editor).getActivePageInstance()).update();
    }


    public static void doSelectionChanged(ScopePattern pattern, Combo combo) {
        SearchScopeType scope = pattern.getScope();

        /* 親より広い範囲のscopeを選べないようにする */
        PatternContainer parent = pattern.getParent();
        SearchScopeType parentScope;
        if (parent instanceof IScopePattern) {
            parentScope = ((IScopePattern) parent).getScope();
        } else {
            parentScope = SearchScopeType.SEARCH_ALL;
        }

        SearchScopeType[] searchs = SearchScopeType.values();
        combo.removeAll();
        switch (parentScope) {
        case SEARCH_ALL:
            combo.add(searchs[0].getName());
            combo.add(searchs[1].getName());
            combo.add(searchs[2].getName());
            break;
        case SEARCH_SENTENCE:
            combo.add(searchs[1].getName());
            combo.add(searchs[2].getName());
            break;
        case SEARCH_SEGMENT:
            combo.add(searchs[2].getName());
            break;
        default:
            break;
        }

        /* コンボボックスの初期値 */
        if (scope.getIntValue() < parentScope.getIntValue()) {
            scope = parentScope;
            pattern.setScope(scope);
        }
        combo.setText(scope.getName());
    }


    @Override
    protected void doSave() {
        commit();
    }


    @Override
    protected void commit() {
        SearchScopeType scope = SearchScopeType.valueOfName(scopeCombo.getText());
        sequence.setScope(scope);
    }
}