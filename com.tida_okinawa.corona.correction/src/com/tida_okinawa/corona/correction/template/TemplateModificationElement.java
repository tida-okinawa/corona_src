/**
 * @version $Id: TemplateModificationElement.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 18:00:36
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.correction.parsing.model.IModification;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateModificationElement extends TemplateContainer {
    protected String kind = Messages.TEMPLATE_MODIFICATION_TYPE;
    /** 係り元 */
    public static final int TYPE_SOURCE = 0;
    /** 係り先 */
    public static final int TYPE_DEST = 1;

    private int type;
    private List<IModification> modificationChildren;


    /**
     * 係り受けの要素（係り元／係り先）
     * 
     * @param parent
     *            親要素
     * @param type
     *            係り受けタイプ
     * @see #TYPE_SOURCE
     * @see #TYPE_DEST
     */
    public TemplateModificationElement(TemplateContainer parent, int type) {
        super(parent);
        this.type = type;
    }


    /**
     * 係り受けタイプ取得
     * 
     * @return 係り受けタイプ
     */
    public int getType() {
        return type;
    }


    /**
     * 係り受けタイプ設定
     * 
     * @param type
     *            係り受けタイプ
     * @see #TYPE_SOURCE
     * @see #TYPE_DEST
     */
    public void setType(int type) {
        if (this.type == type) {
            return;
        }
        this.type = type;
        propertyChanged();
    }


    /**
     * 係り受けの子要素の取得
     * 
     * @return 係り受けの子要素のリスト
     */
    public List<IModification> getModificationChildren() {
        if (modificationChildren == null) {
            modificationChildren = new ArrayList<IModification>();
        }
        return modificationChildren;
    }


    @Override
    public String toString() {
        String typeStr = null;
        if (type == TYPE_SOURCE) {
            typeStr = Messages.TEMPLATE_MODIFICATION_TYPE_SOURCE;
        } else if (type == TYPE_DEST) {
            typeStr = Messages.TEMPLATE_MODIFICATION_TYPE_DEST;
        }
        return Messages.TEMPLATE_MODIFICATION_STRING1 + typeStr + Messages.TEMPLATE_MODIFICATION_STRING2;
    }
}
