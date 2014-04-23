/**
 * @version $Id: ExternalActivator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/22 17:33:00
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.external;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ExternalActivator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "com.tida_okinawa.corona.correction.external"; //$NON-NLS-1$

    // The shared instance
    private static ExternalActivator plugin;

    // このプラグインの内容 ... juman, knpを取得
    private BundleContext context = null;


    /**
     * The constructor
     */
    public ExternalActivator() {
    }


    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        this.context = context;
    }


    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }


    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static ExternalActivator getDefault() {
        return plugin;
    }


    /**
     * バンドルしているリソース・ファイルを取得
     * 
     * @param entryPath
     *            取得したいリソースへのパス
     * @return リソースファイル
     * @throws IOException
     *             何らかの理由でパス変換に失敗した
     * @throws URISyntaxException
     *             呼び出しもとを見ると、juman.iniやknp.iniの記述内容にミスがあったことを想定しているようだが、
     *             このメソッドではチェックしていない。
     *             呼び出しもとでチェックする必要がある
     */
    public File getEntryFile(String entryPath) throws IOException, URISyntaxException {
        // \ -> /
        entryPath = entryPath.replace(File.separatorChar, '/');

        URL url1 = getDefault().getEntry(entryPath);
        if (url1 == null) {
            throw new RuntimeException(entryPath + "をWorkspace内URLに変換できません。");
        }
        URL url2 = FileLocator.toFileURL(url1);
        if (url2 == null) {
            throw new RuntimeException("url21 is null, entryPath=" + entryPath + " url1=" + url1);
        }
        String path = FileLocator.resolve(url2).getPath();

        File file = new File(path);
        return file;
    }


    /**
     * バンドルしているリソースのパス (eclipse内でしか使えない)
     * 
     * @param entryPath
     *            取得したいリソースへのパス
     * @return リソースのURL
     */
    public URL getEntry(String entryPath) {
        return context.getBundle().getEntry(entryPath);
    }

}
