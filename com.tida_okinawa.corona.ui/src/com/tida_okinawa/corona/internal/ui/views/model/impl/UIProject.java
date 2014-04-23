/**
 * @version $Id: UIProject.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 17:34:28
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.util.List;

import org.eclipse.core.resources.IProject;

import com.tida_okinawa.corona.common.StringUtil;
import com.tida_okinawa.corona.internal.ui.CoronaConstants;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;

/**
 * @author kousuke-morishima
 */
public class UIProject extends UIContainer implements IUIProject {
    private int id;


    /* public */UIProject(IUIContainer parent, ICoronaProject object, IProject resource) {
        super(parent, object, resource);
        if (object.getId() == 0) {
            IoActivator.getService().addProject(object);
        }
        this.id = object.getId();
    }


    @Override
    public int getId() {
        return id;
    }


    @Override
    public ICoronaProject getObject() {
        ICoronaProject project = IoActivator.getService().getProject(id);
        if (project == null) {
            new IllegalStateException("デバッグ用。Project does not exists. (Project is already deleted from DataBase.)").printStackTrace();
            return null;
        }
        return project;
    }


    @Override
    public IProject getResource() {
        return (IProject) resource;
    }


    @Override
    protected IUIElement[] createChildren() {
        /* DBのプロジェクト名とWorkSpaceのフォルダ名が一致すればChildrenを作成 */
        if (getObject().getName().equals(resource.getName())) {
            List<ICoronaProduct> products = getObject().getProducts();
            int size = products.size();
            /* ターゲットの数 + 共通辞書フォルダ１ + 問合せデータフォルダ１ */
            IUIElement[] children = new IUIElement[size + 2];
            ;
            /* ターゲットがあれば、ターゲットは以下のChildrenを作成 */
            int i = 0;
            for (; i < size; i++) {
                ICoronaProduct product = products.get(i);
                children[i] = CoronaModel.INSTANCE.create(this, product, createFolder(StringUtil.convertValidFileName(product.getName())));
            }
            children[i++] = CoronaModel.INSTANCE.create(this, new Library(CoronaConstants.COMMON_LIBRARY_NAME),
                    createFolder(CoronaConstants.COMMON_LIBRARY_NAME));
            children[i++] = CoronaModel.INSTANCE.create(this, new ClaimFolder(), createFolder(CoronaConstants.CLAIM_FOLDER_NAME));
            return children;
        } else {
            return new IUIElement[0];
        }
    }


    @Override
    public IUIProduct[] getProducts() {
        IUIElement[] children = getChildren();
        IUIProduct[] ret = new IUIProduct[children.length - 2];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (IUIProduct) children[i];
        }
        return ret;
    }

}
