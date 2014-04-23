/**
 * @version $Id: CreateModificationSource.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/30 15:00:02
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern.handlers;

import com.tida_okinawa.corona.correction.parsing.model.ModificationElement;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternKind;

/**
 * @author kousuke-morishima
 */
public class CreateModificationSource extends CreateHandler {

    @Override
    protected Pattern createPattern(PatternContainer parent) {
        return new ModificationElement(parent, PatternKind.MODIFICATION_SOURCE);
    }


    @Override
    protected PatternKind getCreatePatternKind() {
        return PatternKind.MODIFICATION_SOURCE;
    }
}
