/**
 * @version $Id: MorphemePreferencePage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/24 16:06:40
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem.preference;

import java.io.IOException;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.ILogger;
import com.tida_okinawa.corona.correction.CorrectionActivator;
import com.tida_okinawa.corona.correction.morphem.compile.JumanDicTransfer;

/**
 * 形態素・係り受け解析のプリファレンスページ
 * 
 * @author imai
 * 
 */
public class MorphemePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public MorphemePreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(CorrectionActivator.getDefault().getPreferenceStore());
    }


    @Override
    public void init(IWorkbench workbench) {

    }


    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();

        addField(new BooleanFieldEditor(MorphemePreference.PREF_DO_KNP, Messages.MorphemePreferencePage_fieldEditorKnp, parent)); // このフィールドは Coronaのセクションにも表示
        addField(new BooleanFieldEditor(MorphemePreference.PREF_SERVER_MODE, Messages.MorphemePreferencePage_fieldEditorUseServerMode, parent));
        addField(new StringFieldEditor(MorphemePreference.PREF_SERVER_USERNAME, Messages.MorphemePreferencePage_fieldEditorUserName, parent));
        addField(new StringFieldEditor(MorphemePreference.PREF_SERVER_PASSWD, Messages.MorphemePreferencePage_fieldEditorPassword, parent));
        addField(new MutilineTextFieldEditor(MorphemePreference.PREF_JUMAN_SERVER_CONF, Messages.MorphemePreferencePage_fieldEditorJumanSetting, parent));

        addField(new FileFieldEditor(MorphemePreference.PREF_JUMAN_PROGRAM_PATH, Messages.MorphemePreferencePage_fieldEditorJumanPath, parent));
        addField(new StringFieldEditor(MorphemePreference.PREF_JUMAN_PROGRAM_OPT, Messages.MorphemePreferencePage_fieldEditorJumanOption, parent));
        addField(new DirectoryFieldEditor(MorphemePreference.PREF_JUMAN_INI_DIR, Messages.MorphemePreferencePage_fieldEditorJumanFolder, parent));
        addField(new DirectoryFieldEditor(MorphemePreference.PREF_JUMAN_DIC_DIR, Messages.MorphemePreferencePage_fieldEditorJumanDicFolder, parent));

        addField(new MutilineTextFieldEditor(MorphemePreference.PREF_KNP_SERVER_CONF, Messages.MorphemePreferencePage_fieldEditorKnpServerSetting, parent));

        addField(new FileFieldEditor(MorphemePreference.PREF_KNP_PROGRAM_PATH, Messages.MorphemePreferencePage_fieldEditorKnpPath, parent));
        addField(new StringFieldEditor(MorphemePreference.PREF_KNP_PROGRAM_OPT, Messages.MorphemePreferencePage_fieldEditorKnpOption, parent));
        addField(new DirectoryFieldEditor(MorphemePreference.PREF_KNP_INI_DIR, Messages.MorphemePreferencePage_fieldEditorKnpFolder, parent));

        addField(new BooleanFieldEditor(MorphemePreference.PREF_CONV_SJIS, Messages.MorphemePreferencePage_fieldEditorConvSJIS, parent));

        final Button testBtn = new Button(parent, SWT.NONE);
        testBtn.setText(Messages.MorphemePreferencePage_buttonSettingTest);
        testBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                performApply(); // 変更内容を反映
                MorphemeSettingTester.test();
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        //辞書転送ボタン kobayashi
        final Button dicBtn = new Button(parent, SWT.NONE);
        dicBtn.setText(Messages.MorphemePreferencePage_textSendDic);
        dicBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ILogger logger = CoronaActivator.getDefault().getLogger();
                JumanDicTransfer jdt = new JumanDicTransfer();
                try {
                    jdt.dicTrancefer(logger);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

    }

}


/**
 * 複数行を入力できる {@link StringFieldEditor}
 * 
 */
class MutilineTextFieldEditor extends FieldEditor {
    Text textField;


    public MutilineTextFieldEditor(String name, String labelText, Composite parent) {
        init(name, labelText);
        createControl(parent);
    }


    @Override
    protected void adjustForNumColumns(int numColumns) {
        GridData gd = (GridData) textField.getLayoutData();
        gd.horizontalSpan = numColumns - 1;
        // We only grab excess space if we have to
        // If another field editor has more columns then
        // we assume it is setting the width.
        gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
    }


    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        getLabelControl(parent);

        GridData gd = new GridData();
        gd.horizontalSpan = numColumns - 1;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.grabExcessHorizontalSpace = true;
        getTextField(parent).setLayoutData(gd);
    }


    protected Text getTextField(Composite parent) {
        if (textField == null) {
            textField = new Text(parent, SWT.MULTI | SWT.V_SCROLL);
        }
        return textField;
    }


    @Override
    protected void doLoad() {
        String value = getPreferenceStore().getString(getPreferenceName());
        textField.setText(value);
    }


    @Override
    protected void doLoadDefault() {
        String value = getPreferenceStore().getDefaultString(getPreferenceName());
        textField.setText(value);
    }


    @Override
    protected void doStore() {
        getPreferenceStore().setValue(getPreferenceName(), textField.getText());

    }


    @Override
    public int getNumberOfControls() {
        return 1;
    }
}
