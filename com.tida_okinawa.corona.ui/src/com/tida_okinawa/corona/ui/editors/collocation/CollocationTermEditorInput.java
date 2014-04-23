/**
 * @version $Id: CollocationTermEditorInput.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/06 15:48:54
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.collocation;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ui.IEditorInput;

import com.tida_okinawa.corona.correction.frequent.FrequentRecord;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.ui.editors.FrequentTermEditorInput;

/**
 * @author wataru-higa
 * 
 */
public class CollocationTermEditorInput extends FrequentTermEditorInput implements IEditorInput {

    private List<String> tmtResult;
    private SortedSet<FrequentRecord> items;


    /**
     * @param uiProduct
     *            Product
     * @param name
     *            Name
     * @param claimWorkData
     *            ClaimWorkData
     * @param tmtResult
     *            TMTResult
     */
    public CollocationTermEditorInput(IUIProduct uiProduct, String name, IClaimWorkData claimWorkData, List<String> tmtResult) {
        super(uiProduct, name, claimWorkData);
        this.tmtResult = tmtResult;
    }


    @Override
    public SortedSet<FrequentRecord> getItems() {
        if (items == null) {
            items = new TreeSet<FrequentRecord>();
            for (String result : tmtResult) {
                FrequentRecord c = new FrequentRecord(result);
                /* 不要な読み情報が入っているので、なくす */
                c.setYomi(""); //$NON-NLS-1$
                items.add(c);
            }
        }
        return items;
    }


    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
