/**
 * @version $Id: PrivateTermPart.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/27 15:26:30
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * @author kousuke-morishima
 */
public class PrivateTermPart extends ComboItem<TermPart> {
    public PrivateTermPart(TermPart[] items) {
        super(items);
    }


    public PrivateTermPart(TermPart[] items, boolean includeBlank) {
        super(items, includeBlank);
    }


    @Override
    protected String toName(TermPart item) {
        return item.getName();
    }
}
