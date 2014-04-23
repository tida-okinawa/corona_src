/**
 * @version $Id: NewLabelDictionaryWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/14 12:10:03
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import org.eclipse.jface.dialogs.MessageDialog;

import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * @author kousuke-morishima
 */
public class NewLabelDictionaryWizard extends AbstractNewDictionaryWizard {

    public NewLabelDictionaryWizard() {
    }


    @Override
    protected DicType getDicType() {
        return DicType.LABEL;
    }

    private NewChildDictionaryCreationPage page1 = null;


    @Override
    public void addPages() {
        page1 = new NewLabelDictionaryCreationPage("ラベル辞書作成", getDicType(), selection);
        page1.setReferenceDictionary(new Class<?>[] { IUserDic.class });
        addPage(page1);
    }


    @Override
    public IUIDictionary createNewFile() {
        if (page1.createNewFile() == null) {
            MessageDialog.openWarning(getShell(), "辞書作成失敗", "辞書が作成できませんでした");
            return null;
        }
        return page1.getDictionary();
    }
}
