/**
 * @version $Id: UICorrectionFolder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/13 9:23:41
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.util.Date;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.internal.ui.CoronaConstants;
import com.tida_okinawa.corona.internal.ui.util.ClaimUtil;
import com.tida_okinawa.corona.internal.ui.views.model.ICorrectionFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUICorrectionFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.PropertyUtil;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;

/**
 * @author kousuke-morishima
 */
public class UICorrectionFolder extends UIContainer implements IUICorrectionFolder {
    private ICorrectionFolder folder;


    /* public */UICorrectionFolder(IUIProduct parent, ICorrectionFolder object, IContainer resource) {
        super(parent, object, resource);
        folder = object;
    }


    @Override
    public IUIProduct getParent() {
        return (IUIProduct) parent;
    }


    @Override
    public ICorrectionFolder getObject() {
        return folder;
    }


    @Override
    protected IUIElement[] createChildren() {
        ICoronaProduct product = getParent().getObject();
        if (product == null) {
            return new IUIWork[0];
        } else {
            Set<IClaimWorkData> works = product.getClaimWorkDatas();
            int i = 0;
            for (IClaimWorkData work : works) {
                String ClaimName = CoronaConstants.createCorrectionFolderName(ClaimUtil.getClaimName(work.getClaimId()));
                if (ClaimName.equals(folder.getName())) {
                    i++;
                }
            }
            IUIWork[] children = new IUIWork[i];

            i = 0;
            for (IClaimWorkData work : works) {
                String claimName = CoronaConstants.createCorrectionFolderName(ClaimUtil.getClaimName(work.getClaimId()));
                if (claimName.equals(folder.getName())) {
                    String name = work.getClaimWorkDataType().getName() + "(" + ClaimUtil.getFieldName(work.getClaimId(), work.getFieldId()) + ")";
                    children[i] = (IUIWork) CoronaModel.INSTANCE.create(this, work, createFile(name));
                    i++;
                }
            }
            return children;
        }
    }


    @Override
    public IUIWork[] getWorks() {
        return (IUIWork[]) getChildren();
    }


    /* *******************************
     * property view
     */

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] sp = super.getPropertyDescriptors();
        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[sp.length + 1];

        int i;
        for (i = 0; i < sp.length; i++) {
            descriptor[i] = sp[i];
        }

        PropertyUtil prop = new PropertyUtil();
        /* Forの最後のi++が利いているのでiを1増やす必要はない */
        descriptor[i] = prop.getDescriptor(PropertyItem.PROP_RESULT);

        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {
        if (PropertyItem.PROP_RESULT.getKey().equals(id)) {
            ICoronaProduct product = getParent().getObject();
            Set<IClaimWorkData> cwd = product.getClaimWorkDatas();

            /* 最初の値を初期値に設定 */
            Date lasted = ((IClaimWorkData) product.getClaimWorkDatas().toArray()[0]).getLasted();
            IClaimWorkData lastWork = (IClaimWorkData) product.getClaimWorkDatas().toArray()[0];

            for (IClaimWorkData work : cwd) {
                if (lasted == null) {
                    /* 処理順が後ろのものが先に取得されるので、lastedがnullなら何もしない */
                } else {
                    if ((work.getLasted() != null) && work.getLasted().after(lasted)) {
                        lastWork = work;
                        lasted = work.getLasted();
                    }
                }
            }
            return lastWork.getClaimWorkDataType().getName();
        }
        return super.getPropertyValue(id);
    }
}
