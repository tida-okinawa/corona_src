/**
 * @version $Id: PreferencePage.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/10/12 15:38:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * プリファレンスエディター
 * 
 * @author imai
 * 
 */
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public PreferencePage() {
        super(GRID);
        setPreferenceStore(UIActivator.getDefault().getPreferenceStore());
    }

    private Font descFont;


    @Override
    public void init(IWorkbench workbench) {
        Font defaultFont = workbench.getDisplay().getSystemFont();
        if ((defaultFont != null) && (defaultFont.getFontData().length > 0)) {
            descFont = new Font(null, new FontData(defaultFont.getFontData()[0].getName(), 8, SWT.NONE));
        }
    }


    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        /*
         * TODO 一時的に非表示。"形態素・係り受け解析の設定"と同期がとれるように修正してリリースする。
         */
        //        addField(new BooleanFieldEditor(MorphemePreference.PREF_DO_KNP, "係り受け解析(KNP)を行う", parent)); // このフィールドは 形態素解析のセクションにも表示

        IntegerFieldEditor numOfThreadField = new IntegerFieldEditor(PreferenceInitializer.PREF_NUM_THREADS, "処理スレッド数", parent);
        numOfThreadField.setValidRange(1, Runtime.getRuntime().availableProcessors());
        addField(numOfThreadField);

        /* ユーザ用語辞書のレコード表示件数 */
        IntegerFieldEditor numOfRecordField = new IntegerFieldEditor(PreferenceInitializer.PREF_NUM_VIEW_USERDIC_RECORD, "ユーザ用語辞書の表示レコード数", parent);
        numOfRecordField.setValidRange(1, 10000);
        addField(numOfRecordField);
        //
        Label numOfRecordDescription = new Label(parent, SWT.WRAP);
        numOfRecordDescription.setText("件数が多いと、ページ遷移やエディタを開く時間が長くなります。");
        GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1);
        layoutData.horizontalIndent = 35;
        numOfRecordDescription.setLayoutData(layoutData);
        numOfRecordDescription.setFont(descFont);

        addField(new BooleanFieldEditor(PreferenceInitializer.PREF_RESULT_NOMATCH, "構文解析結果に含まないレコードも表示する", parent));

        addField(new BooleanFieldEditor(PreferenceInitializer.PREF_DISP_FREDLG, "頻出用語登録後の警告ダイアログを表示しない", parent));
    }


    @Override
    public void dispose() {
        if (descFont != null) {
            descFont.dispose();
        }
        super.dispose();
    }
}
