/**
 * @version $Id: UIProduct.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 18:17:19
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.internal.ui.CoronaConstants;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUICorrectionFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.PropertyUtil;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;

/**
 * @author kousuke-morishima
 */
public class UIProduct extends UIContainer implements IUIProduct {
    private int id;


    /* public */UIProduct(IUIContainer parent, ICoronaProduct object, IFolder resource) {
        super(parent, object, resource);

        IUIProject uiProject = (IUIProject) CoronaModel.INSTANCE.getUIContainer(IUIProject.class, parent);
        id = object.getId();
        if (uiProject != null) {
            ICoronaProject project = uiProject.getObject();
            if (id != 0) {
                for (ICoronaProduct product : project.getProducts()) {
                    if (product.getId() == id) {
                        return;
                    }
                }
            }
            project.addProduct(object);
        }
        id = object.getId();
    }


    @Override
    public int getId() {
        return id;
    }


    @Override
    public ICoronaProduct getObject() {
        ICoronaProduct ret = null;
        IUIProject uiProject = CoronaModel.INSTANCE.getProject(this);
        for (ICoronaProduct product : uiProject.getObject().getProducts()) {
            if (product.getId() == id) {
                ret = product;
                break;
            }
        }
        return ret;
    }


    @Override
    public IFolder getResource() {
        return (IFolder) resource;
    }


    @Override
    protected IUIElement[] createChildren() {
        int i = 0;
        List<IClaimData> claims = this.getObject().getClaimDatas();
        IUIElement[] children = new IUIElement[1 + claims.size()];
        children[i++] = CoronaModel.INSTANCE.create(this, new Library(CoronaConstants.LIBRARY_NAME), createFolder(CoronaConstants.LIBRARY_NAME));

        // 処理結果フォルダをクレーム毎に
        for (IClaimData claim : claims) {
            String name = CoronaConstants.createCorrectionFolderName(claim.getName());
            children[i++] = CoronaModel.INSTANCE.create(this, new CorrectionFolder(name), createFolder(name));
        }
        return children;
    }


    @Override
    public IUIWork[] getWorks() {
        IUIElement[] children = getChildren();
        IUIWork[][] uiWorks = new IUIWork[children.length - 1][];
        int size = 0;
        for (int i = 1; i < children.length; i++) {
            uiWorks[i - 1] = ((IUICorrectionFolder) children[i]).getWorks();
            size += uiWorks[i - 1].length;
        }
        Collection<IUIWork> ret = new ArrayList<IUIWork>(size);
        for (int i = 0; i < children.length - 1; i++) {
            for (int j = 0; j < uiWorks[i].length; j++) {
                ret.add(uiWorks[i][j]);
            }
        }
        return ret.toArray(new IUIWork[ret.size()]);
    }


    /* ****************************************
     * property view
     */

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] sp = super.getPropertyDescriptors();

        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[sp.length + 2];

        int i;
        for (i = 0; i < sp.length; i++) {
            descriptor[i] = sp[i];
        }

        PropertyUtil prop = new PropertyUtil();
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CLAIMDATA);
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_RESULT);

        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {

        if (PropertyItem.PROP_CLAIMDATA.getKey().equals(id)) {
            StringBuilder fileName = new StringBuilder(128);
            ICoronaProduct product = getObject();
            if (product != null) {
                for (IClaimData data : product.getClaimDatas()) {
                    fileName.append(",").append(data.getName());
                }
                if (fileName.length() > 0) {
                    return fileName.substring(1);
                }
                return PropertyUtil.DEFAULT_VALUE;
            }
            return PropertyUtil.GET_PROPERTY_ERROR_MESSAGE;
        } else if (PropertyItem.PROP_RESULT.getKey().equals(id)) {
            ICoronaProduct product = getObject();
            if (product != null) {
                Set<IClaimWorkData> cwd = product.getClaimWorkDatas();

                /* 最初の値を初期値に設定　 */
                if (cwd.size() > 0) {
                    Date lasted = ((IClaimWorkData) cwd.toArray()[0]).getLasted();
                    IClaimWorkData lastWork = (IClaimWorkData) cwd.toArray()[0];

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
            }
            return PropertyUtil.GET_PROPERTY_ERROR_MESSAGE;
        }
        return super.getPropertyValue(id);
    }
}
