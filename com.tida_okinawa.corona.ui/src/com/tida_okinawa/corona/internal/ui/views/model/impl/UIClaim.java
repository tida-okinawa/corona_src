/**
 * @version $Id: UIClaim.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/13 10:34:16
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.internal.ui.views.model.IUIClaim;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.PropertyUtil;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;

/**
 * @author kousuke-morishima
 */
public class UIClaim extends UIElement implements IUIClaim {
    private int id;


    /* public */UIClaim(IUIContainer parent, IClaimData object, IFile resource) {
        super(parent, object, resource);
        id = object.getId();
    }


    @Override
    public int getId() {
        return id;
    }


    @Override
    public IClaimData getObject() {
        IClaimData ret = null;
        IUIProject uiProject = CoronaModel.INSTANCE.getProject(this);
        for (IClaimData claim : uiProject.getObject().getClaimDatas()) {
            if (claim.getId() == id) {
                ret = claim;
                break;
            }
        }
        return ret;
    }


    @Override
    public IFile getResource() {
        return (IFile) resource;
    }


    /* ****************************************
     * property view
     */
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] sp = super.getPropertyDescriptors();

        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[sp.length + 2];
        PropertyUtil prop = new PropertyUtil();

        int i;
        for (i = 0; i < sp.length; i++) {
            if (PropertyItem.PROP_LASTMODIFIED.getKey().equals(sp[i].getId())) {
                descriptor[i] = prop.getDescriptor(PropertyItem.PROP_IMPORTDATE);
                continue;
            }
            descriptor[i] = sp[i];
        }

        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_COLUMN_NAME);
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_RECORDS);

        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {
        if (PropertyItem.PROP_COLUMN_NAME.getKey().equals(id)) {

            StringBuilder fieldName = new StringBuilder(128);
            for (IFieldHeader header : getObject().getFieldInformations()) {
                fieldName.append(", ").append(header.getName());
            }
            if (fieldName.length() == 0) {
                return PropertyUtil.DEFAULT_VALUE;
            }
            return fieldName.substring(2);
        } else if (PropertyItem.PROP_RECORDS.getKey().equals(id)) {
            IClaimData data = IoActivator.getService().getClaimData(getId());
            return Integer.toString(data.getRecords().size());
        } else if (PropertyItem.PROP_IMPORTDATE.getKey().equals(id)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            IClaimData claim = this.getObject();
            Date date = claim.getLasted();
            if (date != null) {
                return sdf.format(date);
            }
        }

        return super.getPropertyValue(id);
    }
}
