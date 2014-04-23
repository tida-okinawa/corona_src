/**
 * @version $Id: NewLabelDictionaryCreationPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/14 17:12:56
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;

/**
 * @author kousuke-morishima
 */
public class NewLabelDictionaryCreationPage extends NewChildDictionaryCreationPage {

    public NewLabelDictionaryCreationPage(String title, DicType dicType, IStructuredSelection selection) {
        super(title, dicType, selection);
    }


    @Override
    protected boolean dictionarySettings(IUIDictionary newUIDictionary) {
        List<IUIDictionary> refDics = getReferenceDictionary();
        if (newUIDictionary != null) {
            ICoronaDic newDic = newUIDictionary.getObject();
            if (newDic != null) {
                for (IUIDictionary refDictionary : refDics) {
                    if (refDictionary != null) {
                        ((ILabelDic) newDic).addParentId(refDictionary.getObject().getId());
                    } else {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
