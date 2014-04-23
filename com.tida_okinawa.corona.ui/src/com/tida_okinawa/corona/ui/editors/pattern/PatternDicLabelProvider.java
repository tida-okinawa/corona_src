/**
 * @version $Id: PatternDicLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 18:29:51
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.ui.Icons;

/**
 * @author kousuke-morishima
 */
public class PatternDicLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
        if (element instanceof Pattern) {
            return ((Pattern) element).toString();
        }
        return super.getText(element);
    }

    private static final Icons icon = Icons.INSTANCE;


    @Override
    public Image getImage(Object element) {
        if (element instanceof PatternRecord) {
            if (((PatternRecord) element).isPart()) {
                return icon.get(Icons.IMG_PATTERN_PART);
            }
            return icon.get(Icons.IMG_PATTERN_RECORD);
        } else {
            return icon.get(Icons.IMG_PATTERN_ITEM);
        }
    }
}
