/**
 * @version $Id: ModificationDetailsPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/31 16:02:38
 * @author kousuke-morishima
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

import com.tida_okinawa.corona.correction.parsing.model.Modification;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;

/**
 * @author kousuke-morishima
 */
public class ModificationDetailsPage extends PatternDetailsPage {

    public ModificationDetailsPage(FormEditor editor) {
        super(editor);
    }

    private Combo typeCombo;


    @Override
    public void createContents(Composite parent) {
        parent.setLayout(new FillLayout());

        Section section = createSection(parent, "係り受けの設定", DESCRIPTION_MODIFICATION);
        Composite client = kit.createComposite(section);
        client.setLayout(new GridLayout(2, false));
        client.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label typeLabel = kit.createLabel(client, "係り受け種別  : ");
        /* Memo 並列の場合、２つより多く書きたいかもしれないが、まだ対応できていないのでコメントアウト */
        typeCombo = CompositeUtil.createCombo(client, new String[] { "", "依存(D)"/*
                                                                                 * ,
                                                                                 * "並列(P)"
                                                                                 */});
        typeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        IInformationControl info = getInformationControl(editor.getSite().getShell(), false, new Point(500, 200));
        setHover(info, typeLabel, "「依存」は（私は->走る）のような関係です。形態素・係り受け解析結果の係り受け・係り先を参照してください。");
        setDirtyCheckListenerTo(typeCombo, SWT.Selection);
        section.setClient(client);

    }


    @Override
    protected void doSave() {
        commit();
    }


    @Override
    protected void commit() {
        modification.setType(typeCombo.getSelectionIndex() - 1); // ""-> -1,
                                                                 // "(D)"->0,
                                                                 // "(P)"->1
    }


    @Override
    public void setFocus() {
        typeCombo.setFocus();
    }

    private Modification modification = null;


    @Override
    protected void selectionChanged(IFormPart part, Object selectedObject) {
        modification = (Modification) selectedObject;
        if (modification.getType() == Modification.TYPE_DEPEND) {
            typeCombo.select(1);
        } else if (modification.getType() == Modification.TYPE_PARALLEL) {
            typeCombo.select(2);
        } else {
            typeCombo.select(0);
        }
    }
}
