/**
 * @version $Id: PartFilter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/21 22:30:20
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * @author kousuke-morishima
 */
public class PartFilter extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof ITerm) {
            ITerm term = (ITerm) element;
            for (TermPart part : validParts) {
                if (part.equals(term.getTermPart())) {
                    return true;
                }
            }
        }
        return false;
    }

    private TermPart[] validParts = new TermPart[0];


    public PartFilter(TermPart[] validParts) {
        this.validParts = validParts;
    }
}
