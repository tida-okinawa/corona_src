/**
 * @version $Id: NewFluctuationDictionaryWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/02 15:14:43
 * @author KMorishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * 
 * @author KMorishima
 */
public class NewFluctuationDictionaryWizard extends AbstractNewDictionaryWizard {

    @Override
    protected DicType getDicType() {
        return DicType.FLUC;
    }

    private NewFluctuationDictionaryCreationPage page1 = null;


    @Override
    public void addPages() {
        page1 = new NewFluctuationDictionaryCreationPage("ゆらぎ辞書作成", getDicType(), selection);
        page1.setReferenceDictionary(new Class<?>[] { IUserDic.class });
        addPage(page1);
    }


    @Override
    public IUIDictionary createNewFile() {
        page1.createNewFile();
        return page1.getDictionary();
    }
}
