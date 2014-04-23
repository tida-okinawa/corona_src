/**
 * @version $Id: CreateManifest.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/08 11:48:17
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

/**
 * @author kousuke-morishima
 * 
 */
public class CreateManifest {

    /**
     * マニフェスト作成クラス
     * 
     * @param args
     */
    public static void main(String[] args) {
        //new CreateManifest().create("C:\\インストーラ作成\\Corona-32bit\\Corona\\plugins", "META-INF\\Manifest.txt");
        new CreateManifest().create("C:\\インストーラ作成\\Corona-64bit\\Corona\\plugins", "META-INF\\Manifest.txt");
    }


    /**
     * マニフェスト作成
     * 
     * @param jarFolderPath
     * @param outPath
     */
    public void create(String jarFolderPath, String outPath) {
        java.io.File jarFolder = new java.io.File(jarFolderPath);
        java.io.File outFile = new java.io.File(outPath);

        if (!jarFolder.isDirectory()) {
            System.out.println("invalid jar folder"); //$NON-NLS-1$
        }

        System.out.println("read: " + jarFolder.getAbsolutePath()); //$NON-NLS-1$
        System.out.println("write:" + outFile.getAbsolutePath()); //$NON-NLS-1$

        java.lang.StringBuilder classPaths = new java.lang.StringBuilder(1024);
        System.out.println("Read jar folder"); //$NON-NLS-1$
        classPaths.append("Class-Path:"); //$NON-NLS-1$
        for (String fileName : jarFolder.list()) {
            if (fileName.endsWith(".jar")) { //$NON-NLS-1$
                classPaths.append(" ").append(fileName); //$NON-NLS-1$
            }
        }
        System.out.println("Write Manifest file"); //$NON-NLS-1$
        java.io.BufferedWriter bw = null;
        try {
            bw = new java.io.BufferedWriter(new java.io.FileWriter(outFile));
            bw.write("Manifest-Version: 1.0"); //$NON-NLS-1$
            bw.newLine();
            int offset = 70;
            while (offset < classPaths.length()) {
                classPaths.insert(offset, "\r\n "); //$NON-NLS-1$
                offset += 70 + 2;
            }
            bw.write(classPaths.toString());
            bw.newLine();
            bw.write("Main-Class: com.tida_okinawa.corona.correction.script.Erratum"); //$NON-NLS-1$
            bw.newLine();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (java.io.IOException e) {
                }
            }
        }
        System.out.println("end"); //$NON-NLS-1$
    }
}
