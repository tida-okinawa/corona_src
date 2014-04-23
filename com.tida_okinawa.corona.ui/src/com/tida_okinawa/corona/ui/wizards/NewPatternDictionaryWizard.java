/**
 * @version $Id: NewPatternDictionaryWizard.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/02 15:23:25
 * @author KMorishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import com.tida_okinawa.corona.io.model.dic.DicType;

/**
 * 
 * @author KMorishima
 */
public class NewPatternDictionaryWizard extends AbstractNewDictionaryWizard {

    @Override
    protected DicType getDicType() {
        return DicType.PATTERN;
    }

}
