/**
 * @version $Id: ErratumWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/01 11:44:06
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.ui.controllers.ErratumController;

/**
 * @author kyohei-miyazato
 */
public class ErratumWizard extends Wizard {
    private ColumnSelectWizardPage page1;
    private ErratumWizardPage page2;


    /**
     * コンストラクタ
     */
    public ErratumWizard() {
        setWindowTitle(Messages.ErratumWizard_WizardTitle_Erratum);
    }


    @Override
    public void addPages() {
        if (claim.getProductField() == 0) {
            page1 = new ColumnSelectWizardPage("ColumnSettingPage"); //$NON-NLS-1$
            page2 = new ErratumWizardPage("com.tida_okinawa.corona.ui.wizardpage.erratum"); //$NON-NLS-1$
            addPage(page1);
            addPage(page2);
            if (claim != null) {
                page1.setClaimData(claim);
            }
            final ErratumWizardPage erratumPage = page2;
            final IClaimData finalClaim = claim;
            ((WizardDialog) getContainer()).addPageChangedListener(new IPageChangedListener() {
                @Override
                public void pageChanged(PageChangedEvent event) {
                    if (event.getSelectedPage().equals(erratumPage)) {
                        erratumPage.setFieldHeaders(finalClaim.getFieldInformations());
                    }
                }
            });

        } else {
            page2 = new ErratumWizardPage("com.tida_okinawa.corona.ui.wizardpage.erratum"); //$NON-NLS-1$
            addPage(page2);
            if (claim != null) {
                page2.setClaimData(claim);
            }
        }
    }


    @Override
    public boolean performFinish() {
        List<IFieldHeader> fields = getFieldHeaders();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        ErratumController controller = new ErratumController(dialog.getShell(), claim, fields, null);
        try {
            dialog.run(true, true, controller);
            return true;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    private IClaimData claim;


    /**
     * 誤記補正対象の問い合わせデータをセットする。
     * 
     * @param claim
     *            誤記補正対象の問い合わせデータ
     */
    public void setClaimData(IClaimData claim) {
        if (claim != null) {
            if (claim.getProductField() == 0) {
                /* ターゲットフィールドを選択していなかった場合。 */
                MessageDialog.openError(getShell(), Messages.ErratumWizard_DialogTitle_Failed_Register, Messages.ErratumWizard_ErrorMessage_Select_Target);
            }
            if (page1 != null) {
                page1.setClaimData(claim);
            }
            this.claim = claim;

        }
    }


    /**
     * @return 選択されたヘッダ情報
     */
    public List<IFieldHeader> getFieldHeaders() {
        return page2.getSelectedFields(false);
    }

}
