/**
 * @version $Id: EntryWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/10/26 19:35:35
 * @author kyohei-miyazato
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.controllers.ErratumController;
import com.tida_okinawa.corona.ui.views.db.DataBaseView;
import com.tida_okinawa.corona.uicomponent.IRefreshable;

/**
 * CSV・xlsx・ドキュメントからデータベースへ問い合わせデータを登録するウィザード
 * 
 * @author kyohei-miyazato
 */
public class EntryWizard extends Wizard {
    EntryWizardPage page1;
    ColumnSelectWizardPage page2;
    ErratumWizardPage page3;


    /**
     * コンストラクター
     */
    public EntryWizard() {
        setWindowTitle(Messages.EntryWizard_titleEntryDatabase);
        setDialogSettings(UIActivator.getDefault().getDialogSettings());
    }


    @Override
    public void addPages() {
        page1 = new EntryWizardPage("com.tida_okinawa.corona.ui.wizardpage.EntryWizardPage"); //$NON-NLS-1$
        page2 = new ColumnSelectWizardPage("com.tida_okinawa.corona.ui.wizardpage.ColumnSelectWizardPage"); //$NON-NLS-1$
        page3 = new ErratumWizardPage("com.tida_okinawa.corona.ui.wizardpage.ErratumWizardPage"); //$NON-NLS-1$


        addPage(page1);
        addPage(page2);
        addPage(page3);

        /**
         * 画面遷移前（page1）の状態でXLS形式が選択されている場合、確認メッセージを表示する。
         * OKの場合　　　　次画面へ遷移する。　（page2）
         * CANCELの場合　次画面へ遷移しない。（page1）　
         */
        ((WizardDialog) getContainer()).addPageChangingListener(new IPageChangingListener() {
            @Override
            public void handlePageChanging(PageChangingEvent event) {
                if (event.getCurrentPage().equals(page1) && page1.isXlsTypeSelected()) {
                    MessageBox mb = new MessageBox(getShell(), SWT.OK | SWT.CANCEL);
                    mb.setText(Messages.EntryWizard_climaDataConfirm);
                    mb.setMessage(Messages.EntryWizard_climeDataConfirmText);
                    if (mb.open() == SWT.CANCEL) {
                        event.doit = false;
                    }
                }
            }
        });

        ((WizardDialog) getContainer()).addPageChangedListener(new IPageChangedListener() {
            @Override
            public void pageChanged(PageChangedEvent event) {
                if (event.getSelectedPage().equals(page2) && !page1.isDocTypeSelected()) {
                    List<IFieldHeader> fieldHeaders = null;
                    try {
                        fieldHeaders = page1.getFieldHeaders();
                    } catch (IOException e) {
                        // TODO 自動生成された catch ブロック
                        e.printStackTrace();
                    }
                    page2.setFieldHeaders(fieldHeaders);
                    page3.setFieldHeaders(fieldHeaders);
                }
                ((WizardPageBase) event.getSelectedPage()).setFocus();
            }
        });
    }


    /*
     * 「終了」ボタン押下時の処理
     */
    @Override
    public boolean performFinish() {
        // page2.finished();
        IClaimData claim = page1.finished();
        if (claim != null) {
            // CSV形式の場合
            if (!page1.isDocTypeSelected()) {
                claim.setDispIdField(page2.getIdFieldIndex());
                claim.setProductField(page2.getProductFieldIndex());
                claim.commit();
            }
            List<IFieldHeader> targetFields = getTargetFieldHeaders();
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
            ErratumController controller = new ErratumController(dialog.getShell(), claim, targetFields, null);
            try {
                dialog.run(true, true, controller);
                return true;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                return false;
            } finally {
                /* 現在アクティブなビュー(DataBaseViewであるはず)の表示が更新可能な実装であれば更新する */
                IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
                if (part instanceof IRefreshable) {
                    ((DataBaseView) part).refreshView();
                }
            }
            /*
             * true: Wizardを終了する。
             * false:Wizardを終了しない
             */
        } else {
            MessageDialog.openWarning(getShell(), Messages.EntryWizard_messageError, Messages.EntryWizard_messageFailEntryDatabase);
            return true;
        }
    }


    /**
     * @return 誤記補正対象フィールドの一覧
     */
    public List<IFieldHeader> getTargetFieldHeaders() {
        if (page1.isDocTypeSelected()) {
            return page1.getTargetFieldHeaders();
        }
        return page3.getSelectedFields(false);
    }


    /*
     * 「キャンセル」ボタン押下時の処理
     */
    @Override
    public boolean performCancel() {
        /*
         * true: Wizardを終了する。
         * false:Wizardを終了しない
         */
        return true;
    }


    @Override
    public boolean canFinish() {
        if (page1.isDocTypeSelected() && page1.isPageComplete()) {
            return true;
        }
        return super.canFinish();
    }

}
