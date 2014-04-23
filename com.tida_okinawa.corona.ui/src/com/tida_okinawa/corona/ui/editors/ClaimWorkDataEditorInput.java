/**
 * @version $Id: ClaimWorkDataEditorInput.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/01 20:21:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.Date;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;

public class ClaimWorkDataEditorInput implements IEditorInput {

    final IClaimWorkData claimWorkData;
    final String name;

    int claimID;
    int fieldID;
    ClaimWorkDataType type;
    Date lasted;


    public ClaimWorkDataEditorInput(String name, IClaimWorkData claimWorkData) {
        this.name = name;
        this.claimWorkData = claimWorkData;
        claimID = claimWorkData.getClaimId();
        fieldID = claimWorkData.getFieldId();
        type = claimWorkData.getClaimWorkDataType();
        lasted = claimWorkData.getLasted();
    }


    @Override
    public boolean equals(Object obj) {
        /* 1．そのものと、objが全く同じものだったらtrue */
        if (super.equals(obj)) {
            return true;
        }

        /* オブジェクトがClaimWorkDataEditorInputと同じか比較 */
        if (!(obj instanceof ClaimWorkDataEditorInput)) {
            return false;
        }

        /* 2．オブジェクトがIClaimWorkDataだったようだ。IClaimWorkData型の変数に入れる */
        ClaimWorkDataEditorInput i2 = (ClaimWorkDataEditorInput) obj;
        IClaimWorkData work = i2.getClaimWorkData();

        /* 3．this.claimWorkDataと、2で作った変数のclaimDataを比較 */
        if ((claimID == work.getClaimId()) && (fieldID == work.getFieldId()) && type == work.getClaimWorkDataType() && lasted.equals(work.getLasted())) {
            return true;
        }

        return false;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        return null;
    }


    @Override
    public boolean exists() {
        return true;
    }


    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public IPersistableElement getPersistable() {
        return null;
    }


    @Override
    public String getToolTipText() {
        return "";
    }


    IClaimWorkData getClaimWorkData() {
        return claimWorkData;
    }
}
