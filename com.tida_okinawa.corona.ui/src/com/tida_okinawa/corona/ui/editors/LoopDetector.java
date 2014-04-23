/**
 * @version $Id: LoopDetector.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/06 0:48:56
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 親子関係を持つアイテムの参照が循環していないか確認する
 * 
 * @author kousuke-morishima
 */
public class LoopDetector<T> {
    /**
     * Tを比較する方法がないため、TreeSetなどではなくListで実装している
     */
    private Map<Element, Element> map1 = new HashMap<Element, Element>();


    /**
     * ルートアイテムを追加する
     * 
     * @param rootItem
     */
    public void add(T rootItem) {
        Element e1 = new Element(null, rootItem);
        if (!map1.containsKey(e1)) {
            map1.put(e1, e1);
        }
    }


    /**
     * 子を追加する。 {@link #check(Object, Object)}でtrueが返ってきた組み合わせでのみ追加すること
     * 
     * @param p
     *            parent
     * @param c
     *            child
     */
    public void add(T p, T c) {
        /* 親が登録されているか確認 */
        Element e = new Element(null, p);
        if (map1.get(e) == null)
            return;
        // throw new Error();
        /* 子が登録されているか確認 */
        Element parent = map1.get(e);
        Element child = new Element(parent, c);
        if (map1.get(child) == null) {
            /* 新規追加 */
            map1.put(child, child);
        } else {
            /* 親子つけかえ */
            child = map1.get(child);
            if (child.parent != null) {
                child.parent.removeChild(child);
            }
            child.setParent(parent);
            parent.addChild(child);
        }
    }


    /**
     * {@link #add(Object)}または {@link #add(Object, Object)}
     * で追加したアイテムが保持している親子関係を切る
     * 
     * @param own
     */
    public void remove(T own) {
        /* 登録されているか確認 */
        Element e = new Element(null, own);
        if (map1.get(e) == null)
            return;
        // throw new Error();
        /* 自身から子を切り離す */
        Element newOwn = map1.remove(e); /* ついでにMapから消す */
        for (Element child : newOwn.children) {
            child.setParent(null);
        }
        newOwn.children.clear();
        /* 親から自分を切り離す */
        if (newOwn.parent != null) {
            newOwn.parent.removeChild(newOwn);
        }
        newOwn.setParent(null);
    }


    /**
     * parentにchildを登録しても、参照が循環しないか確かめる。
     * trueなら循環していない
     * 
     * @param parent
     * @param child
     * @return
     */
    public boolean check(T parent, T child) {
        Element e1 = new Element(null, parent);
        if (map1.get(e1) == null) {
            return false;
        }
        Element e2 = new Element(null, child);
        if (map1.get(e2) == null) {
            return true;
        }
        if (map1.get(e1).isPredecessor(map1.get(e2))) {
            return false;
        }
        return true;
    }

    private class Element {
        Element parent;
        List<Element> children;
        private T own;


        public Element(Element parent, T own) {
            this.parent = parent;
            this.own = own;
            this.children = new ArrayList<Element>();
            if (parent != null) {
                parent.addChild(this);
            }
        }


        public void setParent(Element parent) {
            this.parent = parent;
        }


        public void addChild(Element child) {
            if (child != null)
                this.children.add(child);
        }


        public void removeChild(Element child) {
            this.children.remove(child);
        }


        /**
         * 親をたどって行って、targetが見つかったらtrue<br />
         * 自身とtargetが同じでもtrue
         * 
         * @param target
         * @return
         */
        public boolean isPredecessor(Element target) {
            if (own.equals(target.own)) {
                return true;
            }
            if (parent != null) {
                return parent.isPredecessor(target);
            }
            return false;
        }


        @Override
        public String toString() {
            String parentString = "";
            if (parent != null) {
                parentString = parent.toString();
            }
            return parentString + "/" + own;
        }


        @Override
        public int hashCode() {
            return own.hashCode();
        }


        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof LoopDetector.Element)) {
                return false;
            }

            @SuppressWarnings("unchecked")
            LoopDetector<T>.Element e2 = (LoopDetector<T>.Element) obj;
            if (own.equals(e2.own)) {
                return true;
            }
            return false;
        }
    }
}
