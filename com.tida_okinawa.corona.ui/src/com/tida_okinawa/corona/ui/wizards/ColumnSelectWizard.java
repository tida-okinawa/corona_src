/**
 * @version $Id: ColumnSelectWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/27 14:39:29
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

import com.tida_okinawa.corona.io.model.IClaimData;

/**
 * 
 * @author kyohei-miyazato
 */
public class ColumnSelectWizard extends Wizard {
    private ColumnSelectWizardPage page1;
    private IClaimData claim;


    /**
     * コンストラクター
     * カラム設定変更ウィザードのタイトル
     */
    public ColumnSelectWizard() {
        setWindowTitle(Messages.ColumnSelectWizard_WizardTitle_Select_ID_Target);
    }


    @Override
    public void addPages() {
        page1 = new ColumnSelectWizardPage("ColumnSettingPage"); //$NON-NLS-1$
        addPage(page1);
        if (claim != null) {
            page1.setClaimData(claim);
        }
    }


    @Override
    public boolean performFinish() {
        page1.finished();
        return true;
    }


    /**
     * 問い合わせデータを設定
     * 
     * @param claim
     *            問い合わせデータのDB情報
     */
    public void setClaimData(IClaimData claim) {
        if (claim != null) {
            if (page1 != null) {
                page1.setClaimData(claim);
            }
            this.claim = claim;
        }
    }
}
