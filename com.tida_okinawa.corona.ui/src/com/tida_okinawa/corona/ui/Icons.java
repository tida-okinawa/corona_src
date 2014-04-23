/**
 * @version $Id: Icons.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 21:45:11
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui;

import java.net.URL;
import java.util.WeakHashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * @author kousuke-morishima
 */
public class Icons {
    /*
     * 1. 定数を定義する。
     * 定数は、iconsフォルダからの相対パス。
     * 2. initに登録する。
     */
    public static final String IMG_CORONA = "corona2.png"; //$NON-NLS-1$
    public static final String IMG_LIBRARY = "books.png"; //$NON-NLS-1$
    public static final String IMG_BOOK = "book.png"; //$NON-NLS-1$

    public static final String IMG_PROJECT = "prj_obj.gif"; //$NON-NLS-1$
    public static final String IMG_FOLDER = "fldr_obj.gif"; //$NON-NLS-1$

    public static final String IMG_CLAIM = "claim.gif"; //$NON-NLS-1$
    public static final String IMG_CORRECTION = "correction.gif"; //$NON-NLS-1$
    public static final String IMG_DICTIONARY = "dictionary.gif"; //$NON-NLS-1$
    public static final String IMG_PRODUCT = "product.gif"; //$NON-NLS-1$

    public static final String IMG_PATTERN_RECORD = "obj16/ptn_obj.gif"; //$NON-NLS-1$
    public static final String IMG_PATTERN_PART = "obj16/ptn_part_obj.gif"; //$NON-NLS-1$
    public static final String IMG_PATTERN_ITEM = "obj16/ptn_item_obj.gif"; //$NON-NLS-1$

    /* 辞書 */
    public static final String IMG_DIC_JUMAN = "userdic-juman.gif"; //$NON-NLS-1$
    public static final String IMG_DIC_COMMON = "userdic-common.gif"; //$NON-NLS-1$
    public static final String IMG_DIC_CATEGORY = "userdic-category.gif"; //$NON-NLS-1$
    public static final String IMG_DIC_SPECIAL = "userdic-special.gif"; //$NON-NLS-1$
    public static final String IMG_DIC_FLUC = "dic-fluc.gif"; //$NON-NLS-1$
    public static final String IMG_DIC_LABEL = "dic-label.gif"; //$NON-NLS-1$
    public static final String IMG_DIC_PATTERN = "dic-pattern.gif"; //$NON-NLS-1$
    public static final String IMG_DIC_SYNONYM = "dic-synonym.gif"; //$NON-NLS-1$
    /* 処理結果 */
    public static final String IMG_RESLUT_BASE = "result-bese.gif"; //$NON-NLS-1$
    public static final String IMG_RESLUT_CORRECTION_MISTAKES = "result-correction.gif"; //$NON-NLS-1$
    public static final String IMG_RESLUT_MORPHOLOGICAL = "result-morphological.gif"; //$NON-NLS-1$
    public static final String IMG_RESLUT_DEPENDENCY_STRUCTURE = "result-morphological.gif"; //$NON-NLS-1$
    public static final String IMG_RESLUT_CORRECTION_FLUC = "result-fluc.gif"; //$NON-NLS-1$
    public static final String IMG_RESLUT_CORRECTION_SYNONYM = "result-synonym.gif"; //$NON-NLS-1$
    public static final String IMG_RESLUT_RESLUT_PATTERN = "result-pattern.gif"; //$NON-NLS-1$
    public static final String IMG_RESLUT_LASTED = "result-lasted.gif"; //$NON-NLS-1$
    public static final String IMG_RESLUT_FREQUENT = "result-frequent.gif"; //$NON-NLS-1$

    public static final String IMG_TOOL_ADD = "elcl16/add.gif"; //$NON-NLS-1$
    public static final String IMG_TOOL_DELETE = "elcl16/delete.gif"; //$NON-NLS-1$
    public static final String IMG_TOOL_RENAME = "elcl16/rename.gif"; //$NON-NLS-1$
    public static final String IMG_TOOL_PREV = "elcl16/backward_nav.gif"; //$NON-NLS-1$
    public static final String IMG_TOOL_NEXT = "elcl16/forward_nav.gif"; //$NON-NLS-1$
    public static final String IMG_TOOL_FIRST = "elcl16/first_nav.gif"; //$NON-NLS-1$
    public static final String IMG_TOOL_LAST = "elcl16/last_nav.gif"; //$NON-NLS-1$
    public static final String IMG_TOOL_FILTER = "elcl16/filter.gif"; //$NON-NLS-1$
    public static final String IMG_TOOL_EXPAND_ALL = "elcl16/expandall.gif"; //$NON-NLS-1$
    public static final String IMG_TOOL_COLLAPSE_ALL = "elcl16/collapseall.gif"; //$NON-NLS-1$

    public static final String IMG_OBJ_ITEM = "obj16/item.gif"; //$NON-NLS-1$

    public static final String IMG_OVR_ERROR = "ovr16/error_ovr.gif"; //$NON-NLS-1$


    private UIActivator activator;
    private WeakHashMap<String, Image> iconCache;


    private Icons() {
        if (UIActivator.getDefault() == null) {
            throw new IllegalStateException(Messages.Icons_ErrorMessage_CannotUseThisClass);
        }
        activator = UIActivator.getDefault();
        iconCache = new WeakHashMap<String, Image>(40);
    }

    /** このクラスのシングルトンインスタンス */
    public static final Icons INSTANCE = new Icons();


    /**
     * icons以下のイメージを登録する。<br/>
     * {@link UIActivator#initializeImageRegistry(ImageRegistry)}から、起動時に１回だけ呼ばれる
     * 
     * @param registry
     *            イメージ管理クラス
     */
    public void init(ImageRegistry registry) {
        /* ここに、ツールで使用するイメージを登録する */
        registry(registry, IMG_CORONA);
        registry(registry, IMG_LIBRARY);
        registry(registry, IMG_BOOK);
        registry(registry, IMG_CLAIM);
        registry(registry, IMG_CORRECTION);
        registry(registry, IMG_DICTIONARY);
        registry(registry, IMG_PRODUCT);
        registry(registry, IMG_PROJECT);
        registry(registry, IMG_FOLDER);
        registry(registry, IMG_PATTERN_RECORD);
        registry(registry, IMG_PATTERN_PART);
        registry(registry, IMG_PATTERN_ITEM);

        registry(registry, IMG_DIC_JUMAN);
        registry(registry, IMG_DIC_COMMON);
        registry(registry, IMG_DIC_CATEGORY);
        registry(registry, IMG_DIC_SPECIAL);
        registry(registry, IMG_DIC_FLUC);
        registry(registry, IMG_DIC_LABEL);
        registry(registry, IMG_DIC_PATTERN);
        registry(registry, IMG_DIC_SYNONYM);

        registry(registry, IMG_RESLUT_BASE);
        registry(registry, IMG_RESLUT_CORRECTION_MISTAKES);
        registry(registry, IMG_RESLUT_MORPHOLOGICAL);
        registry(registry, IMG_RESLUT_DEPENDENCY_STRUCTURE);
        registry(registry, IMG_RESLUT_CORRECTION_FLUC);
        registry(registry, IMG_RESLUT_CORRECTION_SYNONYM);
        registry(registry, IMG_RESLUT_RESLUT_PATTERN);
        registry(registry, IMG_RESLUT_LASTED);
        registry(registry, IMG_RESLUT_FREQUENT);

        registry(registry, IMG_TOOL_ADD);
        registry(registry, IMG_TOOL_DELETE);
        registry(registry, IMG_TOOL_RENAME);
        registry(registry, IMG_TOOL_PREV);
        registry(registry, IMG_TOOL_NEXT);
        registry(registry, IMG_TOOL_FIRST);
        registry(registry, IMG_TOOL_LAST);
        registry(registry, IMG_TOOL_FILTER);
        registry(registry, IMG_TOOL_EXPAND_ALL);
        registry(registry, IMG_TOOL_COLLAPSE_ALL);

        registry(registry, IMG_OBJ_ITEM);

        registry(registry, IMG_OVR_ERROR);
    }


    private void registry(ImageRegistry registry, String key) {
        URL url = activator.getBundle().getEntry("icons/" + key); //$NON-NLS-1$
        ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
        registry.put(key, descriptor);
    }


    /**
     * imageKeyに対応した{@link ImageDescriptor}を返す
     * 
     * @param imageKey
     *            取得したいイメージのキー
     * @return ImageDescriptor
     * @see Icons
     */
    public ImageDescriptor getDescriptor(String imageKey) {
        return activator.getImageRegistry().getDescriptor(imageKey);
    }


    /**
     * imageKeyに対応した{@link Image}を返す。
     * リソースがすでに作成されていればキャッシュを返す。
     * <p>
     * Actionやコンテクストメニューのアイコンに使用するなど、特に{@link ImageDescriptor}
     * が必要な場合を除き、こちらを使用すること
     * </p>
     * 
     * @param imageKey
     *            取得したいイメージのキー
     * @return Image
     */
    public Image get(String imageKey) {
        Image image = iconCache.get(imageKey);
        if (image == null) {
            image = getDescriptor(imageKey).createImage();
            iconCache.put(imageKey, image);
        }
        return image;
    }
}
