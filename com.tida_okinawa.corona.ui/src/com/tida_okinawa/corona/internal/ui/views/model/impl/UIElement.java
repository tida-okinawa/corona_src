/**
 * @version $Id: UIElement.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 16:16:26
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.io.PropertyUtil;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaObject;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * @author kousuke-morishima
 */
public abstract class UIElement implements IUIElement {
    protected IUIContainer parent;
    protected IResource resource;


    /**
     * @param parent
     *            UI(ProjExpl)上の親
     * @param object
     *            not null
     * @param resource
     *            not null
     */
    /* public */UIElement(IUIContainer parent, ICoronaObject object, IResource resource) {
        Assert.isTrue((object != null) && (resource != null));

        this.parent = parent;
        this.resource = resource;

        initialize();
    }


    private void initialize() {
    }


    @Override
    public int getId() {
        return NO_ID;
    }


    @Override
    public IUIContainer getParent() {
        return parent;
    }


    @Override
    public IUIElement getRoot() {
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }


    @Override
    public void update(IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        if (!(resource instanceof IFile)) {
            return;
        }

        try {
            if (!resource.exists()) {
                if ((parent != null) && !parent.getResource().exists()) {
                    parent.update(monitor);
                }
                ((IFile) resource).create(FileContent.toStream(this), true, monitor);
            } else {
                ((IFile) resource).setContents(FileContent.toStream(this), IResource.FORCE | IResource.KEEP_HISTORY, monitor);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }


    /* ****************************************
     * Adapter
     */
    @Override
    public IResource getResource() {
        return resource;
    }


    /**
     * renameなどで、対応するIResourceが変更されたときに呼び出す。
     * 
     * @param resource
     */
    void setResource(IResource resource) {
        Assert.isLegal(resource != null); // Assert NN

        this.resource = resource;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (IResource.class.equals(adapter)) {
            return this.resource;
        }

        return null;
    }


    /* ****************************************
     * utility
     */
    @Override
    public int hashCode() {
        return getId();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof UIElement)) {
            return false;
        }

        IResource r2 = ((UIElement) obj).resource;
        return resource.equals(r2);
    }


    @Override
    public String toString() {
        return resource.getName();
    }


    /* ****************************************
     * property view
     */

    @Override
    public Object getEditableValue() {
        return "";
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        PropertyUtil prop = new PropertyUtil();
        ICoronaObject o = this.getObject();
        IPropertyDescriptor[] descriptor;
        if (o instanceof ICoronaDic) {
            descriptor = new IPropertyDescriptor[] { prop.getDescriptor(PropertyItem.PROP_LOCATION), prop.getDescriptor(PropertyItem.PROP_LASTMODIFIED),
                    prop.getDescriptor(PropertyItem.PROP_EDITABLE), prop.getDescriptor(PropertyItem.PROP_NAME) };
        } else {
            descriptor = new IPropertyDescriptor[] { prop.getDescriptor(PropertyItem.PROP_LOCATION), prop.getDescriptor(PropertyItem.PROP_LASTMODIFIED),
                    prop.getDescriptor(PropertyItem.PROP_NAME) };
        }
        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {
        ICoronaObject obj = this.getObject();
        if (PropertyItem.PROP_LOCATION.getKey().equals(id)) {
            if ((resource != null) && (resource.exists())) {
                return resource.getLocation().toOSString();
            }
        } else if (PropertyItem.PROP_LASTMODIFIED.getKey().equals(id)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            if (obj instanceof ICoronaDic) {
                Date date = ((ICoronaDic) obj).getLasted();
                return sdf.format(date);
            } else if (obj instanceof IClaimData) {
                Date date = ((IClaimData) obj).getLasted();
                return sdf.format(date);
            }
            if (resource != null) {
                return sdf.format(resource.getLocalTimeStamp());
            }
        } else if (PropertyItem.PROP_EDITABLE.getKey().equals(id)) {
            if (obj instanceof ICoronaDic) {
                if (obj instanceof IUserDic) {
                    IUserDic uDic = (IUserDic) obj;
                    if (DicType.JUMAN.equals(uDic.getDicType())) {
                        return "false";
                    }
                }
                return "true";
            }
            return "false";
        } else if (PropertyItem.PROP_NAME.getKey().equals(id)) {
            if (resource != null) {
                return resource.getName();
            }
        }
        return PropertyUtil.DEFAULT_VALUE;
    }


    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }


    @Override
    public void resetPropertyValue(Object id) {

    }


    @Override
    public void setPropertyValue(Object id, Object value) {

    }

}
