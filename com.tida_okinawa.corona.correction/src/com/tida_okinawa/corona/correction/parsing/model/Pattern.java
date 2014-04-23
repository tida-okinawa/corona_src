/**
 * @version $Id: Pattern.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/30 11:31:58
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;


/**
 * @author kousuke-morishima
 */
public abstract class Pattern implements Cloneable, IAdaptable {
    private PatternContainer parent;


    /**
     * @param parent
     *            may be null
     */
    public Pattern(PatternContainer parent) {
        this.parent = parent;
    }


    public PatternContainer getParent() {
        return parent;
    }


    public void setParent(PatternContainer parent) {
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


    public Pattern getPatternModel() {
        Pattern ptn = getParent();
        if (ptn == null) {
            return this;
        }
        return ptn.getPatternModel();
    }


    /**
     * @return 今現在子どもがいるか
     */
    public boolean hasChildren() {
        return false;
    }


    /**
     * {@link #propertyChanged(Pattern)}の簡易呼び出し版メソッド
     */
    public void propertyChanged() {
        propertyChanged(this);
    }


    /**
     * ownのプロパティに変更があった
     * 
     * @param own
     * @param force
     */
    protected void propertyChanged(Pattern own) {
        if (parent != null) {
            if (!ignore) {
                parent.propertyChanged(own);
            }
        }
    }

    private boolean ignore = false;


    /**
     * @return trueなら、プロパティの変更を上（親パターンを経由して自身が属している辞書へ）に通知しない。
     */
    public boolean isIgnoreChange() {
        return ignore;
    }


    /**
     * 内容に変更があったことを通知するかどうかを切り替える。
     * 通知は、親パターンを経由して自身が属している辞書へ通知される。
     * 無駄な更新処理が実行されないようにするために使用する。<br />
     * 現在は、子の追加・削除に対してはこの設定に関係なく上に通知される。
     * 
     * @param ignore
     *            trueなら、プロパティの変更を通知しないようにする。falseなら通知するようになり、通知も同時に行われる
     */
    public void setIgnoreChange(boolean ignore) {
        this.ignore = ignore;
        if (ignore == false) {
            propertyChanged();
        }
    }


    @Override
    public abstract Pattern clone();


    @Override
    public abstract String toString();

    /* ****************************************
     * 構文解析用
     */
    /* #539 対応で、CoronaPatternParserから使う予定 */
    private boolean extractedLink = false;


    /**
     * 自身が内包するLinkパターンを、Linkパターンが参照する先のパターンに展開（置き換え）済みかどうかを返す<br/>
     * default = false;
     * 
     * @return
     */
    public boolean isExtractedLink() {
        return extractedLink;
    }


    /**
     * 自身が内包するLinkパターンを、Linkパターンが参照する先のパターンに展開（置き換え）済みかどうかを指定する<br/>
     * 
     * @param extracted
     */
    public void setExtractedLink(boolean extracted) {
        this.extractedLink = extracted;
    }


    /**
     * 兄弟の次アイテムチェック
     * 
     * @return 次がいなければnull
     */
    public Pattern getNextOrOperator() {
        if (parent != null) {
            if (parent instanceof OrOperator) {
                int index = parent.children.indexOf(this);
                if (parent.children.size() > index + 1) {
                    return parent.children.get(index + 1);
                }
            }
            return parent.getNextOrOperator();
        }
        return null;
    }


    /**
     * Orを親方向に持つかチェック
     * 
     * @return 親方向にOrがいなければnull
     */
    public Pattern getParentOrOperator() {
        if (parent != null) {
            if (parent instanceof OrOperator) {
                return parent;
            }
            return parent.getParentOrOperator();
        }
        return null;
    }

    Set<Integer> hits = new HashSet<Integer>();


    public void clear() {
        hits.clear();
    }


    public void setHit(int index) {
        hits.add(index);
    }


    public Set<Integer> getHit() {
        return hits;
    }


    /**
     * @return このパターンのパターン種別を返す
     */
    public abstract PatternKind getKind();


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySource.class)) {
            return source;
        }
        return null;
    }

    private IPropertySource source = new IPropertySource() {
        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("kind", "パターン型") };
            return descriptor;
        }


        @Override
        public Object getPropertyValue(Object id) {
            if (id.equals("kind")) {
                return getKind();
            }
            return null;
        }


        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }


        @Override
        public void resetPropertyValue(Object id) {
        }


        @Override
        public void setPropertyValue(Object id, Object value) {
        }


        @Override
        public Object getEditableValue() {
            return null;
        }

    };
}
