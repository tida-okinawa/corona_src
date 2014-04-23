/**
 * @version $Id: UILibFolder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/13 9:28:17
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.util.List;

import org.eclipse.core.resources.IContainer;

import com.tida_okinawa.corona.internal.ui.views.model.ILibrary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibrary;
import com.tida_okinawa.corona.io.model.ICoronaDics;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * @author kousuke-morishima
 */
public class UILibFolder extends UIContainer implements IUILibFolder {
    private ILibrary folder;


    /* public */UILibFolder(IUILibrary parent, ILibrary object, IContainer resource) {
        super(parent, object, resource);
        folder = object;
    }


    @Override
    public IUILibrary getParent() {
        return (IUILibrary) parent;
    }


    @Override
    public ILibrary getObject() {
        return folder;
    }


    @Override
    protected IUIElement[] createChildren() {
        ICoronaDics dics = getParent().getObject();
        if (dics == null) {
            return new IUIElement[0];
        } else {
            List<ICoronaDic> dictionaries = dics.getDictionarys(ICoronaDic.class);
            int size = dictionaries.size();
            IUIElement[] children = new IUIElement[size];
            for (int i = 0; i < size; i++) {
                ICoronaDic dic = dictionaries.get(i);
                children[i] = CoronaModel.INSTANCE.create(this, dic, createFile(dic.getName()));
            }
            return children;
        }
    }

}
