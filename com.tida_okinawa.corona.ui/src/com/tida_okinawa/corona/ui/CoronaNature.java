/**
 * @version $Id: CoronaNature.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 15:50:42
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * @author kousuke-morishima
 */
public class CoronaNature implements IProjectNature {
    public static final String ID = UIActivator.PLUGIN_ID + ".CoronaNature";
    private IProject project;


    @Override
    public void configure() throws CoreException {
    }


    @Override
    public void deconfigure() throws CoreException {
    }


    @Override
    public IProject getProject() {
        return project;
    }


    @Override
    public void setProject(IProject project) {
        this.project = project;
    }
}
