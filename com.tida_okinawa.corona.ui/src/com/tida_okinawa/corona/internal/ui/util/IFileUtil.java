/**
 * @version $Id: IFileUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/19 19:01:11
 * @author yoshikazu-imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.tida_okinawa.corona.correction.common.FileUtil;

/**
 * Eclipse {@link IFileUtil} 用のユーティリティ
 * 
 * @author imai
 * 
 */
public class IFileUtil {
    /**
     * ファイルコピー {@link IFile} -> {@link File}
     * 
     * @param to
     * @param from
     * @throws IOException
     * @throws CoreException
     */
    static public void copy(File to, IFile from) throws IOException, CoreException {
        InputStream is = from.getContents();
        try {
            FileUtil.copy(to, is);
        } finally {
            is.close();
        }
    }


    /**
     * ファイルコピー {@link File} -> {@link IFile}
     * 
     * @param to
     * @param from
     * @throws FileNotFoundException
     * @throws CoreException
     */
    static public void copy(IFile to, File from) throws FileNotFoundException, CoreException {
        to.refreshLocal(IResource.DEPTH_ONE, null);
        if (to.exists()) {
            to.delete(true, null);
        }
        FileInputStream input = new FileInputStream(from);
        try {
            to.create(input, true, null);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 複数ファイルコピー
     * 
     * {@link IFile}[] -> {@link File}
     * 
     * @param toDir
     * @param fromFiles
     * @throws IOException
     * @throws CoreException
     */
    static public void copy(File toDir, IFile[] fromFiles) throws IOException, CoreException {
        for (IFile from : fromFiles) {
            File to = new File(toDir, from.getName());
            copy(to, from);
        }
    }


    /**
     * 複数ファイルコピー {@link File}[] -> {@link IFolder}
     * 
     * @param toDir
     * @param fromFiles
     * @throws FileNotFoundException
     * @throws CoreException
     */
    static public void copy(IFolder toDir, File[] fromFiles) throws FileNotFoundException, CoreException {
        for (File from : fromFiles) {
            IFile to = toDir.getFile(from.getName());
            copy(to, from);
        }
        toDir.refreshLocal(IResource.DEPTH_ONE, null);
    }
}
