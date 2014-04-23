/**
 * @version $Id: ReadingSorter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/24 17:15:04
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.tida_okinawa.corona.io.model.dic.ITerm;

/**
 * @author kousuke-morishima
 */
public class ReadingSorter extends ViewerSorter {
    @Override
    public int category(Object element) {
        if ("".equals(((ITerm) element).getReading().trim())) {
            return 99;
        }
        return 0;
    }


    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int cat1 = category(e1);
        int cat2 = category(e2);
        if (cat1 == cat2) {
            ITerm t1 = (ITerm) e1;
            ITerm t2 = (ITerm) e2;
            return t1.getReading().compareTo(t2.getReading());
        }
        return cat1 - cat2;
    }
}
