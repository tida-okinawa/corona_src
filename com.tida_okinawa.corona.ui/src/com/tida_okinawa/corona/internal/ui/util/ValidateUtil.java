/**
 * @version $Id: ValidateUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/07 15:21:26
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * @author kousuke-morishima
 */
public class ValidateUtil {
    /**
     * 重複なし。
     */
    public static final int DUPLICATE_NO = 0;
    /**
     * ワークスペース内に重複がある
     */
    public static final int DUPLICATE_WS = 1;
    /**
     * データベース内に重複がある
     */
    public static final int DUPLICATE_DB = 2 << 0;

    static final int MAX_LENGTH_PROJECT_NAME = 80;
    static final int MAX_LENGTH_DICTIONARY_NAME = 255;


    /**
     * Database内、およびWorkspace内に重複するプロジェクト名がなければtrue
     * Workspace内をチェックする
     * 
     * @param name
     * @return bit-wise
     * @see #DUPLICATE_NO
     * @see #DUPLICATE_DB
     * @see #DUPLICATE_WS
     */
    public static int isValidProjectName(String name) {
        Assert.isNotNull(name);

        int ret = DUPLICATE_NO;

        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : projects) {
            if (name.equalsIgnoreCase(project.getName())) {
                ret |= DUPLICATE_WS;
                break;
            }
        }

        List<ICoronaProject> dbProjects = IoActivator.getService().getProjects();
        for (ICoronaProject dbProject : dbProjects) {
            if (name.equalsIgnoreCase(dbProject.getName())) {
                ret |= DUPLICATE_DB;
                break;
            }
        }
        return ret;
    }


    /**
     * Database内、およびフォルダ内に重複する辞書名がなければtrue
     * Workspace内をチェックする
     * 
     * @param parent
     *            辞書作成先。データベース内など、親がいない場合はnull
     * @param name
     *            検査する辞書名
     * @return bit-wise
     * @see #DUPLICATE_NO
     * @see #DUPLICATE_DB
     * @see #DUPLICATE_WS
     */
    public static int isValidDictionaryName(IContainer parent, String name) {
        int ret = DUPLICATE_NO;
        if (parent != null) {
            try {
                IResource[] members = parent.members();
                for (IResource member : members) {
                    if (member.getName().equalsIgnoreCase(name)) {
                        ret |= DUPLICATE_WS;
                        break;
                    }
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        List<ICoronaDic> dics = IoActivator.getService().getDictionarys(ICoronaDic.class);
        for (ICoronaDic dic : dics) {
            if (dic.getName().equalsIgnoreCase(name)) {
                ret |= DUPLICATE_DB;
                break;
            }
        }
        return ret;
    }
}
