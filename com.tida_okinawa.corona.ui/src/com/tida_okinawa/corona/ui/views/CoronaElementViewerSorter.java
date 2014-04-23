/**
 * @version $Id: CoronaElementViewerSorter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/25 12:56:51
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views;

import java.text.Collator;
import java.util.Date;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;

import com.tida_okinawa.corona.internal.ui.views.model.IUIClaim;
import com.tida_okinawa.corona.internal.ui.views.model.IUIClaimFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUICorrectionFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.ui.PreferenceInitializer;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author kousuke-morishima
 */
public class CoronaElementViewerSorter extends org.eclipse.jface.viewers.ViewerSorter {

    public CoronaElementViewerSorter() {
    }


    public CoronaElementViewerSorter(Collator collator) {
        super(collator);
    }

    public static final int TYPE = 0;
    public static final int NAME = 1;
    private int sort = UIActivator.getDefault().getPreferenceStore().getInt(PreferenceInitializer.PREF_DIC_SORT_TYPE);


    public void setSortKind(int kind) {
        if (kind == TYPE || kind == NAME) {
            UIActivator.getDefault().getPreferenceStore().setValue(PreferenceInitializer.PREF_DIC_SORT_TYPE, kind);
        }
    }

    private CoronaDicComparator comparator = new CoronaDicComparator();


    @Override
    public int category(Object element) {
        if (element instanceof IUIDictionary) {
            int diff = 0;
            ICoronaDic dic = ((IUIDictionary) element).getObject();
            if (sort == TYPE) {
                diff = comparator.category(dic);
            } else if (sort == NAME) {
                diff = 0;
            }
            return 10 + diff;

        } else if (element instanceof IUIClaim) {
            return 50;
        } else if (element instanceof IUIProduct) {
            return 1;
        } else if (element instanceof IProject) {
            return 0;
        } else if (element instanceof IUILibFolder) {
            return 3;
        } else if (element instanceof IUICorrectionFolder) {
            return 4;
        } else if (element instanceof IUIClaimFolder) {
            return 5;
        } else {
            return 99;
        }
    }


    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int ret = category(e1) - category(e2);
        if (ret == 0) {
            if (e1 instanceof IUIWork) {
                IClaimWorkData work1 = ((IUIWork) e1).getObject();
                IClaimWorkData work2 = ((IUIWork) e2).getObject();
                int id1 = work1.getFieldId();
                int id2 = work2.getFieldId();
                ret = id1 - id2;
                if (ret == 0) {
                    Date date1 = work1.getLasted();
                    Date date2 = work2.getLasted();
                    if (date2 == null) {
                        return -1;
                    }
                    if (date1 == null) {
                        return 1;
                    }
                    if (date2.equals(date1)) {
                        return 0;
                    } else if (date2.after(date1)) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                return ret;
            } else if (e1 instanceof IUIDictionary) {
                return comparator.compare(((IUIDictionary) e1).getObject(), ((IUIDictionary) e2).getObject());
            } else if (e1 instanceof IUIElement) {
                return ((IUIElement) e1).getResource().getName().compareTo(((IUIElement) e2).getResource().getName());
            }
            return super.compare(viewer, e1, e2);
        }
        return ret;
    }
}
