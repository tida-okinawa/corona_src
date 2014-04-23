/**
 * @version $Id: FrequentTermEditorInput.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/29 15:03:35
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.tida_okinawa.corona.correction.frequent.FrequentRecord;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.table.ITextRecord;

/**
 * @author takayuki-matsumoto, imai
 */
public class FrequentTermEditorInput extends ClaimWorkDataEditorInput {
    /**
     * 対象の辞書一覧
     */
    private List<IUserDic> searchDics;

    private IUIProduct uiProduct;


    /**
     * @param name
     */
    public FrequentTermEditorInput(IUIProduct uiProduct, String name, IClaimWorkData claimWorkData) {
        super(name, claimWorkData);

        this.uiProduct = uiProduct;

        // 対象の辞書一覧
        searchDics = new ArrayList<IUserDic>();

        ICoronaProduct product = uiProduct.getObject();
        ICoronaProject project = IoActivator.getService().getProject(product.getProjectId());
        List<ICoronaDic> dics = product.getDictionarys(IUserDic.class);
        dics.addAll(project.getDictionarys(IUserDic.class));
        for (ICoronaDic dic : dics) {
            searchDics.add((IUserDic) dic);
        }
    }


    public IUIProduct getUIProduct() {
        return uiProduct;
    }


    public SortedSet<FrequentRecord> getItems() {
        List<ITextRecord> records = claimWorkData.getClaimWorkDatas();
        SortedSet<FrequentRecord> items = new TreeSet<FrequentRecord>();
        for (ITextRecord record : records) {
            String text = record.getText();
            FrequentRecord r = new FrequentRecord(text);
            items.add(r);
        }
        return items;
    }


    /**
     * ユーザー辞書の一覧
     * 
     * @return
     */
    public List<IUserDic> getDics() {
        return searchDics;
    }


    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
