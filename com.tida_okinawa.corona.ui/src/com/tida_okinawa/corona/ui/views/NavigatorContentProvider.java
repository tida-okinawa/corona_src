/**
 * @version $Id: NavigatorContentProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03 11:52:26
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.viewers.Viewer;

import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;

/**
 * @author kousuke-morishima
 */
public class NavigatorContentProvider extends CoronaElementContentProvider {
    @Override
    public Object[] getElements(Object input) {
        if (input instanceof IWorkspaceRoot) {
            IProject[] projects = ((IWorkspaceRoot) input).getProjects();
            for (IProject project : projects) {
                CoronaModel.INSTANCE.adapter(project, true);
            }
            return projects;
        } else if (input instanceof IProject) {
            return super.getElements(CoronaModel.INSTANCE.adapter((IProject) input, false));
        }
        return super.getElements(input);
    }


    @Override
    public Object[] getChildren(Object parent) {
        if (parent instanceof IProject) {
            if (((IProject) parent).isOpen()) {
                return super.getChildren(CoronaModel.INSTANCE.adapter((IProject) parent, false));
            }
            return new Object[0];
        }
        return super.getChildren(parent);
    }


    @Override
    public Object getParent(Object element) {
        return super.getParent(element);
    }


    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IProject) {
            if (((IProject) element).isOpen()) {
                if (CoronaModel.INSTANCE.isCoronaProject((IProject) element)) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return super.hasChildren(element);
    }


    @Override
    public void dispose() {
    }


    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}
