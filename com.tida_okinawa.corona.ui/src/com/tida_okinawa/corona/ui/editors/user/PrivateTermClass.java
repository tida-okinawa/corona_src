/**
 * @version $Id: PrivateTermClass.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/27 15:27:07
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import com.tida_okinawa.corona.io.model.dic.TermClass;

/**
 * @author kousuke-morishima
 */
public class PrivateTermClass extends ComboItem<TermClass> {
    public PrivateTermClass(TermClass[] items) {
        super(items);
    }


    public PrivateTermClass(TermClass[] items, boolean includeBlank) {
        super(items, includeBlank);
    }


    @Override
    protected String toName(TermClass item) {
        return item.getName();
    }
}
