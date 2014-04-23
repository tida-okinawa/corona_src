/**
 * @version $Id: AbstractNewDictionaryWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/02 15:06:46
 * @author KMorishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.ui.TIDA;
import com.tida_okinawa.corona.ui.ViewUtil;

/**
 * 
 * @author KMorishima
 */
public abstract class AbstractNewDictionaryWizard extends BasicNewResourceWizard {
    private static final String TITLE = Messages.NewDictionaryWizard_WizardTitle_CreateDictionary;


    /**
     * 辞書新規登録ウィザードのタイトルをセットする
     */
    public AbstractNewDictionaryWizard() {
        setWindowTitle(TITLE);
    }


    /**
     * @return 作成する辞書の拡張子を返す
     */
    abstract protected DicType getDicType();

    private NewDictionaryCreationPage page1;


    @Override
    public void addPages() {
        String title = TITLE;
        switch (getDicType()) {
        case COMMON:
            title = Messages.NewDictionaryWizard_PageTitle_CreateCommonDictionary;
            break;
        case SPECIAL:
            title = Messages.NewDictionaryWizard_PageTitle_CreateSpecialDictionary;
            break;
        case PATTERN:
            title = Messages.NewDictionaryWizard_PageTitle_CreatePatternDictionary;
            break;
        default:
            break;
        }
        page1 = new NewDictionaryCreationPage(title, getDicType(), this.selection);
        addPage(page1);
    }


    @Override
    public boolean performFinish() {
        IUIDictionary uiDic = createNewFile();
        if (uiDic == null) {
            System.err.println("何らかの理由により、辞書作成に失敗"); // TODO is this needed
            return false;
        }

        // Open editor on new file.
        IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
        try {
            if (dw != null) {
                IWorkbenchPage page = dw.getActivePage();
                if (page != null) {
                    TIDA.openEditor(page, uiDic, true);
                }
            }
        } catch (PartInitException e) {
            e.printStackTrace();
        }

        ViewUtil.refreshDatabaseView(0);

        return true;
    }


    protected IUIDictionary createNewFile() {
        page1.createNewFile();
        return page1.getDictionary();
    }
}
