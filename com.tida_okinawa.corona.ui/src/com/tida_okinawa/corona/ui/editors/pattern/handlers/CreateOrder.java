/**
 * @version $Id: CreateOrder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/30 14:53:15
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern.handlers;

import com.tida_okinawa.corona.correction.parsing.model.Order;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternKind;

/**
 * @author kousuke-morishima
 */
public class CreateOrder extends CreateHandler {

    @Override
    protected Pattern createPattern(PatternContainer parent) {
        return new Order(parent);
    }


    @Override
    protected PatternKind getCreatePatternKind() {
        return PatternKind.ORDER;
    }
}
