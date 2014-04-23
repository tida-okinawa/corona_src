/**
 * @version $Id: UIContainer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 16:57:50
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.model.ICoronaObject;

/**
 * @author kousuke-morishima
 */
public abstract class UIContainer extends UIElement implements IUIContainer {
    private IUIElement[] children;
    private boolean needUpdate;


    /* public */UIContainer(IUIContainer parent, ICoronaObject object, IContainer resource) {
        super(parent, object, resource);

        children = null;
        needUpdate = false;
    }


    @Override
    public IContainer getResource() {
        return (IContainer) resource;
    }


    @Override
    public void update(IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        if (!(resource instanceof IFolder)) {
            return;
        }

        IFolder iFolder = (IFolder) resource;
        try {
            if (!iFolder.exists()) {
                if ((parent != null) && !parent.getResource().exists()) {
                    parent.update(monitor);
                }
                iFolder.create(true, true, monitor);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }


    /* ****************************************
     * ContentProvider用
     */
    @Override
    public boolean hasChildren() {
        if (children == null) {
            return true;
        }
        if (needUpdate) {
            needUpdate = false;
            children = createChildren();
        }
        return children.length > 0;
    }


    @Override
    public void modifiedChildren() {
        needUpdate = true;
    }


    /**
     * 最新の子を取得するか指定できる。
     * 
     * @param update
     * @return
     */
    final IUIElement[] getChildren(boolean update) {
        if (children == null) {
            children = new IUIElement[0];
        }
        needUpdate = update;
        return getChildren();
    }


    @Override
    public final IUIElement[] getChildren() {
        if ((children == null) || needUpdate) {
            children = createChildren();
            needUpdate = false;
        }
        return Arrays.copyOf(children, children.length);
    }


    /**
     * 最新のデータを取得する必要があるときに呼ばれる
     * 
     * @return
     */
    abstract protected IUIElement[] createChildren();


    /* ****************************************
     * utility
     */
    protected final IFolder createFolder(String name) {
        return getResource().getFolder(new Path(name));
    }


    protected final IFile createFile(String name) {
        return getResource().getFile(new Path(name));
    }

    /* ****************************************
     * Property
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");


    @Override
    public Object getPropertyValue(Object id) {
        if (PropertyItem.PROP_LASTMODIFIED.getKey().equals(id)) {
            String timestamp = sdf.format(getResource().getLocalTimeStamp());
            for (IUIElement child : getChildren()) {
                String childTimestamp = (String) child.getPropertyValue(id);
                if (timestamp.compareTo(childTimestamp) < 0) {
                    timestamp = childTimestamp;
                }
            }
            return timestamp;
        }
        return super.getPropertyValue(id);
    }
}
