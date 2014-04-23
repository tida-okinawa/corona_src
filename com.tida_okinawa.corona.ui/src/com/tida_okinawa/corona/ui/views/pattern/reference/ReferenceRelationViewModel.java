/**
 * @version $Id: ReferenceRelationViewModel.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/07/24 17:26:55
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.pattern.reference;

/**
 * 参照関係ビューでツリーへ表示する値及びアイコンを設定するモデル
 * 
 * @author wataru-higa
 */
public class ReferenceRelationViewModel {
    private String referenceWord = null;
    private int iconTypeId = 0;
    /**
     * アイコン無しの定数
     */
    public static final int ICON_TYPE_NONE = 0;
    /**
     * 「参照可能」trueアイコン(赤■)の定数
     */
    public static final int ICON_TYPE_PARTS = 1;
    /**
     * 「参照可能」falseアイコン(緑●)の定数
     */
    public static final int ICON_TYPE_PUBLIC = 2;


    /**
     * コンストラクタ
     * 
     * @param referenceWord
     *            表示名
     * @param iconType
     *            部品パターンはtrue、そうでなければfalse、アイコンを表示しない場合はnull
     */
    public ReferenceRelationViewModel(String referenceWord, Boolean iconType) {
        this.referenceWord = referenceWord;
        if (iconType == null) {
            this.iconTypeId = ICON_TYPE_NONE;
        } else if (iconType) {
            this.iconTypeId = ICON_TYPE_PARTS;
        } else {
            this.iconTypeId = ICON_TYPE_PUBLIC;
        }

    }


    /**
     * 参照パターン名を返却
     * 
     * @return 参照パターン名
     */
    public String getReferenceWord() {
        return this.referenceWord;
    }


    /**
     * 参照パターン名を設定
     * 
     * @param referenceWord
     *            参照パターン名
     */
    public void setReferenceWord(String referenceWord) {
        this.referenceWord = referenceWord;
    }


    /**
     * 参照パターンラベルのアイコンIDを返却
     * 
     * @return アイコンID
     */
    public int getIconTypeId() {
        return this.iconTypeId;
    }


    /**
     * 参照パターンラベルのアイコンIDを設定
     * 
     * @param iconTypeId
     *            アイコンID
     */
    public void setIconTypeId(int iconTypeId) {
        this.iconTypeId = iconTypeId;
    }


}
