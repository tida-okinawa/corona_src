/**
 * @version $Id: TemplateLink.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 17:57:50
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import java.util.List;
import java.util.Set;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ITemplateItem;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateLink extends VariableTemplate {
    /**
     * 未登録
     */
    public static final int NO_ID = -1;


    /**
     * 参照
     * 
     * @param parent
     *            親要素
     */
    public TemplateLink(TemplateContainer parent) {
        super(parent);
        id = NO_ID;
        label = ""; //$NON-NLS-1$
    }

    private int id;
    private String label;
    private ITemplateItem linkTemplate;


    /**
     * 参照するひな型のID取得
     * 
     * @return ひな型のID
     */
    public int getId() {
        return id;
    }


    void setId(int id) {
        if (this.id == id) {
            return;
        }
        this.id = id;
        propertyChanged();
    }


    /**
     * 参照するひな型名の取得
     * 
     * @return ひな型名
     */
    public String getLabel() {
        ITemplateItem template = getLinkTemplate();
        if (template != null) {
            return template.getName();
        }
        return label;
    }


    /**
     * 参照するひな型名の設定
     * 
     * @param label
     *            ひな型名
     */
    public void setLabel(String label) {
        label = (label == null) ? "" : label; //$NON-NLS-1$
        if (this.label.equals(label)) {
            return;
        }
        this.label = label;
        propertyChanged();
    }


    /**
     * 参照するひな型アイテムの設定
     * 
     * @param iTemplateItem
     *            ひな型アイテム
     */
    public void setLinkTemplate(ITemplateItem iTemplateItem) {
        this.linkTemplate = iTemplateItem;
        if (iTemplateItem != null) {
            setId(iTemplateItem.getTemplateId());
            setLabel(iTemplateItem.getName());
        } else {
            setId(-1);
            setLabel(""); //$NON-NLS-1$
        }
    }


    /**
     * 参照するひな型アイテムの取得
     * 
     * @return ひな型
     */
    public ITemplateItem getLinkTemplate() {
        boolean finded = false;
        if ((id != ITemplateItem.DEFAULT_ID) && (linkTemplate == null)) {
            List<ITemplateItem> items = IoActivator.getTemplateFactory().getItems();
            for (ITemplateItem item : items) {
                /* TEMPLATE_IDに格納された値が一致した場合 */
                if (item != null && item.getTemplateId() == id) {
                    linkTemplate = item;
                    finded = true;
                    break;
                }
            }
        }
        /* items内で見つからなかった場合 */
        if (finded != true && (linkTemplate == null)) {
            Set<ITemplateItem> items = IoActivator.getTemplateFactory().getAddItems();
            for (ITemplateItem item : items) {
                /* TEMPLATE_IDに格納された値が一致した場合 */
                if (item != null && item.getTemplateId() == id) {
                    linkTemplate = item;
                    finded = true;
                    break;
                }
            }
        }
        return linkTemplate;
    }


    @Override
    public String toString() {
        return getLabel() + Messages.TEMPLATE_LINK_STRING;
    }
}
