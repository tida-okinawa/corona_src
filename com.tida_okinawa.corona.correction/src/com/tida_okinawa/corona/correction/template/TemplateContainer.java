/**
 * @version $Id: TemplateContainer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/27 11:38:20
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import java.util.ArrayList;
import java.util.List;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 *         ひな型コンテナ（子の削除/変更は非対応）
 */
public abstract class TemplateContainer extends Template {

    /** 位置情報なし */
    public static final int NO_POS = -1;

    protected boolean childCreated = false;


    /**
     * @param parent
     *            親要素
     */
    public TemplateContainer(TemplateContainer parent) {
        super(parent);
    }

    protected List<Template> children;


    /**
     * @return 子要素の取得
     */
    public List<Template> getChildren() {
        if (!childCreated) {
            /* 存在しない場合は空の子要素を作成 */
            createChildren();
        }
        return children;
    }


    /**
     * 子要素の生成（ひな型用）
     */
    protected void createChildren() {
        childCreated = true;
        children = new ArrayList<Template>();
    }


    @Override
    public boolean hasChildren() {
        if (!childCreated) {
            return true;
        }
        return children.size() != 0;
    }


    /**
     * 子要素の追加（ひな型用）
     * 
     * @param child
     *            子要素
     */
    public void addChild(Template child) {
        if (!childCreated) {
            createChildren();
        }
        children.add(child);
        childAdded(this, child, NO_POS);
    }


    /**
     * 子要素の追加（ひな型用）
     * 
     * @param index
     *            位置
     * @param child
     *            子要素
     */
    public void addChild(int index, Template child) {
        if ((index < 0) || (index > children.size())) {
            return;
        }
        if (!childCreated) {
            createChildren();
        }
        children.add(index, child);
        childAdded(this, child, index);
    }


    void childAdded(TemplateContainer parent, Template child, int position) {
        if (getParent() != null) {
            getParent().childAdded(parent, child, position);
        }
    }
}
