/**
 * @version $Id: NewCategoryDictionaryWizards.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/02 15:30:06
 * @author KMorishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.io.model.dic.DicType;

/**
 * 
 * @author KMorishima
 */
public class NewCategoryDictionaryWizards extends AbstractNewDictionaryWizard {

    @Override
    protected DicType getDicType() {
        return DicType.CATEGORY;
    }

    private NewCategoryDictionaryCreationPage page1 = null;


    @Override
    public void addPages() {
        page1 = new NewCategoryDictionaryCreationPage("分野辞書作成", getDicType(), selection);
        addPage(page1);
    }


    @Override
    protected IUIDictionary createNewFile() {
        page1.createNewFile();
        return page1.getDictionary();
    }
}
