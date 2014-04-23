/**
 * @version $Id: WebEntryWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/19 19:41:44
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry.ui;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.uicomponent.IRefreshable;
import com.tida_okinawa.corona.webentry.Messages;
import com.tida_okinawa.corona.webentry.WebEntryActivator;

/**
 * Webから情報を取得し問い合わせデータとして取り込むウィザード
 * 
 * @author yukihiro-kinjo
 * 
 */
public class WebEntryWizard extends Wizard {

    WebEntryServiceSettingPage page1;
    WebEntryTwitterSearchSettingPage page2;


    /**
     * コンストラクター
     */
    public WebEntryWizard() {
        setWindowTitle(Messages.WebEntryWizard_WebEntryWizardTitle);
        setDialogSettings(WebEntryActivator.getDefault().getDialogSettings());
    }


    @Override
    public void addPages() {
        /* ウィザードページをセットアップ */
        page1 = new WebEntryServiceSettingPage("com.tida_okinawa.corona.webentry.ui.WebEntryServiceSettingPage"); //$NON-NLS-1$
        page2 = new WebEntryTwitterSearchSettingPage("com.tida_okinawa.corona.webentry.ui.WebEntryTwitterSearchSettingPage"); //$NON-NLS-1$
        addPage(page1);
        addPage(page2);

        /* ページ切り替え時に最上位の入力要素にフォーカスを当てるリスナーを登録 */
        ((WizardDialog) getContainer()).addPageChangedListener(new PageChangedSetFocusListener());
    }


    @Override
    public boolean performFinish() {
        /* 完了処理 */
        setNeedsProgressMonitor(true);
        IClaimData claim = page2.finished();
        if (claim != null) {
            /* 現在アクティブなビュー(DataBaseViewであるはず)の表示が更新可能な実装であれば更新する */
            IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
            if (part instanceof IRefreshable) {
                /* DataBaseViewの表示データに、今追加したデータを追加する */
                ((IRefreshable) part).refreshView();
            }
        } else {
            return false;
        }

        return true;
    }


    private static final class PageChangedSetFocusListener implements IPageChangedListener {
        @Override
        public void pageChanged(PageChangedEvent event) {
            ((WizardPageBase) event.getSelectedPage()).setFocus();
        }
    }
}
