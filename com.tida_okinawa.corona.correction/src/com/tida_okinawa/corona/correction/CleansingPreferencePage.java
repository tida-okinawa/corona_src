/**
 * @version $Id: CleansingPreferencePage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2012/02/21 14:17:53
 * @author kousuke-morishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction;

import static com.tida_okinawa.corona.common.CleansingNameVariable.COLLOCATION;
import static com.tida_okinawa.corona.common.CleansingNameVariable.MISTAKE_CORRECT;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * Coronaのプリファレンス(Eclipseの設定内)の
 * クレンジングに関するページの実装
 * 
 * @author kousuke-morishima
 */
public class CleansingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    /** コンストラクター */
    public CleansingPreferencePage() {
    }


    @Override
    public void init(IWorkbench workbench) {
    }

    // 誤記補正用
    private static String ERASE_COMBO_WORD_ALL_SPACES = Messages.CleansingPreferencePage_comboAllSpace;
    private static String ERASE_COMBO_WORD_VERBOSE_SPACES = Messages.CleansingPreferencePage_comboVervoseSpace;
    private static String ERASE_COMBO_WORD_SPACES_NO = Messages.CleansingPreferencePage_comboNoSpace;
    private static String ERASE_EXPLANATION_WORD_ALL_SPACES = Messages.CleansingPreferencePage_labelAllSpace;
    private static String ERASE_EXPLANATION_WORD_VERBOSE_SPACES = Messages.CleansingPreferencePage_labelVerboseSpace;
    private static String ERASE_EXPLANATION_WORD_SPACES_NO = Messages.CleansingPreferencePage_labelNoSpace;
    private Combo erraCombo;
    private Label description;

    // 連語抽出用
    Text collocationText;


    @Override
    protected Control createContents(Composite parent) {
        IPreferenceStore store = CorrectionActivator.getDefault().getPreferenceStore();

        // 以下誤記補正グループ
        // 親コンポジット
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        // グループ(誤記補正)
        Group erratumGroup = new Group(composite, SWT.NONE);
        GridLayout erraGridLayout = new GridLayout(2, false);
        erratumGroup.setLayout(erraGridLayout);
        erraGridLayout.horizontalSpacing = 3;
        erratumGroup.setText(MISTAKE_CORRECT);
        erratumGroup.setLayout(erraGridLayout);
        erratumGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // グループ内ラベル1
        Label erraComboLabel = new Label(erratumGroup, SWT.NONE);
        erraComboLabel.setText(Messages.CleansingPreferencePage_labelTargetSpace);

        // コンボボックス
        erraCombo = new Combo(erratumGroup, SWT.READ_ONLY);
        erraCombo.setItems(new String[] { ERASE_COMBO_WORD_ALL_SPACES, ERASE_COMBO_WORD_VERBOSE_SPACES, ERASE_COMBO_WORD_SPACES_NO });

        // コンボボックスの文言を取得
        String state = store.getString(CorrectionPreferenceInitializer.PREF_ERRATUM_SPACES);
        if (state.equals(CorrectionPreferenceInitializer.ERASE_ALL_SPACES)) {
            erraCombo.setText(ERASE_COMBO_WORD_ALL_SPACES);
        } else if (state.equals(CorrectionPreferenceInitializer.ERASE_VERBOSE_SPACES)) {
            erraCombo.setText(ERASE_COMBO_WORD_VERBOSE_SPACES);
        } else if (state.equals(CorrectionPreferenceInitializer.ERASE_SPACES_NO)) {
            erraCombo.setText(ERASE_COMBO_WORD_SPACES_NO);
        }

        // グループ内ラベル２(コンボボックス内容説明文)
        description = new Label(erratumGroup, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1);
        description.setLayoutData(gridData);
        setEraseSpaceDescription(erraCombo.getText());

        erraCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setEraseSpaceDescription(erraCombo.getText());
            }
        });

        // 以下連語抽出グループ
        Group collocationGroup = new Group(composite, SWT.NONE);
        GridLayout collocationGridLayout = new GridLayout(2, false);
        collocationGroup.setLayout(collocationGridLayout);
        collocationGridLayout.horizontalSpacing = 3;
        collocationGroup.setText(COLLOCATION);
        collocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label collocationComboLabel = new Label(collocationGroup, SWT.NONE);
        collocationComboLabel.setText(Messages.CleansingPreferencePage_labelHitCount);

        collocationText = new Text(collocationGroup, SWT.BORDER | SWT.RIGHT);
        collocationText.setText(store.getString(CorrectionPreferenceInitializer.PREF_COLLOCATION_WORD));
        GridData collGridData = new GridData(SWT.NONE, SWT.FILL, true, false);
        collGridData.widthHint = 50;
        collocationText.setLayoutData(collGridData);
        collocationText.addModifyListener(new ModifyListener() {


            @Override
            public void modifyText(ModifyEvent event) {
                String text = collocationText.getText();
                try {
                    int hitNum = Integer.parseInt(text);
                    if (hitNum <= 0) {
                        setValid(false);
                        return;
                    }
                } catch (NumberFormatException e) {
                    setValid(false);
                    return;
                }
                setValid(true);
            }
        });
        return null;
    }


    /**
     * コンボボックス表示中の文言に一致する説明文を設定するメソッド
     * 
     * @param newValue
     *            コンボボックスで選択された値
     */
    private void setEraseSpaceDescription(String newValue) {
        if (ERASE_COMBO_WORD_ALL_SPACES.equals(newValue)) {
            description.setText(ERASE_EXPLANATION_WORD_ALL_SPACES);
        } else if (ERASE_COMBO_WORD_VERBOSE_SPACES.equals(newValue)) {
            description.setText(ERASE_EXPLANATION_WORD_VERBOSE_SPACES);
        } else if (ERASE_COMBO_WORD_SPACES_NO.equals(newValue)) {
            description.setText(ERASE_EXPLANATION_WORD_SPACES_NO);
        } else {
            description.setText(""); //$NON-NLS-1$
        }
    }


    @Override
    public boolean performOk() {
        IPreferenceStore store = CorrectionActivator.getDefault().getPreferenceStore();
        store.setValue(CorrectionPreferenceInitializer.PREF_ERRATUM_SPACES, getEraseSpaceStatus(description.getText()));
        if (collocationText.getText().length() != 0) {
            collocationText.setText(String.valueOf(Integer.parseInt(collocationText.getText())));
            store.setValue(CorrectionPreferenceInitializer.PREF_COLLOCATION_WORD, String.valueOf(Integer.parseInt(collocationText.getText())));
        }
        return true;
    }


    /**
     * 表示中の説明文に一致する設定値を返却するメソッド
     * このメソッドは設定値を保存する際に呼び出される
     * 
     * @param newValue
     *            コンボボックスで選択された値
     * @return 説明文に一致する設定値
     */
    private static String getEraseSpaceStatus(String newValue) {
        if (ERASE_EXPLANATION_WORD_ALL_SPACES.equals(newValue)) {
            return CorrectionPreferenceInitializer.ERASE_ALL_SPACES;
        } else if (ERASE_EXPLANATION_WORD_VERBOSE_SPACES.equals(newValue)) {
            return CorrectionPreferenceInitializer.ERASE_VERBOSE_SPACES;
        } else if (ERASE_EXPLANATION_WORD_SPACES_NO.equals(newValue)) {
            return CorrectionPreferenceInitializer.ERASE_SPACES_NO;
        } else {
            return ""; //$NON-NLS-1$
        }
    }


    @Override
    protected void performDefaults() {
        super.performDefaults();
        // 誤記補正のデフォルト値に設定
        erraCombo.setText(ERASE_COMBO_WORD_VERBOSE_SPACES);
        description.setText(ERASE_EXPLANATION_WORD_VERBOSE_SPACES);
        // 連語抽出のデフォルト値に設定
        collocationText.setText(CorrectionPreferenceInitializer.COLLOCATION_DEFAULT_NUMBER);
    }
}
