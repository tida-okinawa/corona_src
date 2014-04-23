/**
 * @version $Id: IoActivator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * ${date} ${time}
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.tida_okinawa.corona.io.model.IModelFactory;
import com.tida_okinawa.corona.io.model.dic.IDicFactory;
import com.tida_okinawa.corona.io.model.dic.ITemplateFactory;
import com.tida_okinawa.corona.io.service.IDicService;
import com.tida_okinawa.corona.io.service.IIoService;

/**
 * @author shingo-takahashi
 * 
 */
public class IoActivator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "com.tida_okinawa.corona.io"; //$NON-NLS-1$

    // The shared instance
    private static IoActivator plugin;

    private static ResourceBundle bundle = null;


    /**
     * The constructor
     */
    public IoActivator() {
    }


    @Override
    public synchronized void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        try {
            bundle = ResourceBundle.getBundle("plugin", Locale.JAPANESE); //$NON-NLS-1$

        } catch (MissingResourceException ex) {
            System.out.println(Messages.IoActivator_errReadResource + ex.toString());
            return;
        }
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
    public static IoActivator getDefault() {
        return plugin;
    }


    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }


    /**
     * リソース取得
     * 
     * @return 取得されたリソース
     */
    public static ResourceBundle getResource() {
        return bundle;
    }


    /**
     * プラグインIDを取得
     * 
     * @return プラグインID
     */
    public static String getPluginId() {
        return PLUGIN_ID;
    }


    /**
     * @return may be null if stopped
     */
    public static IPreferenceStore getStore() {
        IoActivator plugin = getDefault();
        if (plugin != null) {
            return plugin.getPreferenceStore();
        }
        return null;
    }


    /**
     * 辞書関連ユーティリティのインスタンスを取得
     * 
     * @return 辞書関連のユーティリティのインスタンス
     */
    public static IDicService getDicUtil() {
        return com.tida_okinawa.corona.io.dam.hibernate.DicService.getInstance();
    }


    /**
     * IOサービスのインスタンス取得
     * 
     * @return IOサービスのインスタンス
     */
    public static IIoService getService() {
        return com.tida_okinawa.corona.io.dam.hibernate.IoService.getInstance();
    }


    /**
     * モデルファクトリーのインスタンスを取得
     * 
     * @return モデルファクトリーのインスタンス
     */
    public static IModelFactory getModelFactory() {
        return com.tida_okinawa.corona.io.dam.hibernate.ModelFactory.getInstance();
    }


    /**
     * 辞書ファクトリーのインスタンスを取得
     * 
     * @return モデルファクトリーのインスタンス
     */
    public static IDicFactory getDicFactory() {
        return com.tida_okinawa.corona.io.dam.hibernate.DicFactory.getInstance();
    }


    /**
     * ひな型のインスタンス取得
     * 
     * @author s.takuro （187 構文パターン自動生成）
     * @return ひな型のインスタンス
     */
    public static ITemplateFactory getTemplateFactory() {
        return com.tida_okinawa.corona.io.dam.hibernate.TemplateFactory.getInstance();
    }
}
