/**
 * @version $Id: Template.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/27 1:16:43
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public abstract class Template {

    private TemplateContainer parent;


    /**
     * ひな型
     * 
     * @param parent
     *            親要素
     */
    public Template(TemplateContainer parent) {
        this.parent = parent;
    }


    /**
     * 親要素の取得
     * 
     * @return 親要素
     */
    public TemplateContainer getParent() {
        return parent;
    }


    /**
     * 親要素の設定
     * 
     * @param parent
     *            親要素
     * 
     */
    public void setParent(TemplateContainer parent) {
        if (parent == null) {
            if (this.parent == null) {
                return;
            }
        } else {
            if (parent.equals(this.parent)) {
                return;
            }
        }
        this.parent = parent;
    }


    /**
     * 今現在子どもがいるか
     * 
     * @return true: いる<br/>
     *         false: いない
     */
    public boolean hasChildren() {
        return false;
    }


    /**
     * {@link #propertyChanged(Template)}の簡易呼び出し版メソッド
     */
    public void propertyChanged() {
        propertyChanged(this);
    }


    /**
     * ownのプロパティに変更があった
     * 
     * @param template
     *            ひな型（UI表示用）
     */
    protected void propertyChanged(Template template) {
        if (parent != null) {
            parent.propertyChanged(template);
        }
    }
}
