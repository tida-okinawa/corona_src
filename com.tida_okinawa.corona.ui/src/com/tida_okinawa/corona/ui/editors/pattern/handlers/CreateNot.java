/**
 * @version $Id: CreateNot.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/30 14:52:36
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern.handlers;

import com.tida_okinawa.corona.correction.parsing.model.NotOperator;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternKind;

/**
 * @author kousuke-morishima
 */
public class CreateNot extends CreateHandler {

    @Override
    protected Pattern createPattern(PatternContainer parent) {
        return new NotOperator(parent);
    }


    @Override
    protected PatternKind getCreatePatternKind() {
        return PatternKind.NOT;
    }
}
