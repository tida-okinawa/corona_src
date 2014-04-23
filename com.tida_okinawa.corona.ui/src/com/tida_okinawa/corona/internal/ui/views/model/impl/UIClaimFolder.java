/**
 * @version $Id: UIClaimFolder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/13 9:15:51
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.util.List;

import org.eclipse.core.resources.IContainer;

import com.tida_okinawa.corona.internal.ui.views.model.IClaimFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUIClaimFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProject;

/**
 * @author kousuke-morishima
 */
public class UIClaimFolder extends UIContainer implements IUIClaimFolder {
    private IClaimFolder folder;


    /* public */UIClaimFolder(IUIProject parent, IClaimFolder object, IContainer resource) {
        super(parent, object, resource);
        folder = object;
    }


    @Override
    public IUIProject getParent() {
        return (IUIProject) parent;
    }


    @Override
    public IClaimFolder getObject() {
        return folder;
    }


    @Override
    protected IUIElement[] createChildren() {
        ICoronaProject project = getParent().getObject();
        List<IClaimData> claims = project.getClaimDatas();
        int size = claims.size();
        IUIElement[] children = new IUIElement[size];
        for (int i = 0; i < size; i++) {
            IClaimData claim = claims.get(i);
            children[i] = CoronaModel.INSTANCE.create(this, claim, createFile(claim.getName()));
        }
        return children;
    }

}
