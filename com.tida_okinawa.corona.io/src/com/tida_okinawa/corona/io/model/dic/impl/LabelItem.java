/**
 * @version $Id: LabelItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.abstraction.DicItem;


/**
 * @author shingo-takahashi
 * 
 */
public class LabelItem extends DicItem implements ILabel {

    private ILabel _parent;
    private int _parentId;

    private List<ILabel> _children = new ArrayList<ILabel>();
    private List<ILabel> _delChildren = new ArrayList<ILabel>();

    private String _name;

    private List<ITerm> _terms = new ArrayList<ITerm>();
    private List<ITerm> _delTerms = new ArrayList<ITerm>();


    public LabelItem(String name, ILabel parent) {
        this._name = name;
        this._parent = parent;
        this._children = new ArrayList<ILabel>();
        if (parent != null) {
            parent.addChild(this);
            setDicId(parent.getComprehensionDicId());
        }
    }


    @Override
    public ILabel getParent() {
        return _parent;
    }


    @Override
    public void setParent(ILabel parent) {

        /*
         * Memo エディタで使うようになるとバグを生みそう。 削除とか。
         * parentの子の移し替えは別メソッドを呼び出してもらうようにすると安全かなぁ
         */

        /* 前の親から自分を外す */
        if (this._parent != null) {
            this._parent.getChildren().remove(this);
        }
        /* 新しい親に自分をセットする */
        this._parent = parent;
        if (this._parent != null) {
            this._parent.getChildren().add(this);
        }

        if (parent != null) {
            if (_parentId != parent.getId()) {
                /* 親の変更のみdirtyフラグを立てる */
                _parentId = parent.getId();
                setDirty(true);
            }
        } else {
            if (_parentId != 0) {
                _parentId = 0;
                setDirty(true);
            }
        }
    }


    @Override
    public List<ILabel> getChildren() {
        return _children;
    }


    @Override
    public void setChildren(List<ILabel> children) {
        this._children = children;
        // // TODO 親子関係の整合性が崩れないように持つ
        // /* うまくできていないようなので、コメントアウト */
        // /* 前の子の親を外す */
        // for (ILabel label : this._children) {
        // label.setParent(null);
        // }
        // /* 新しい子に自分をセットする */
        // this._children = children;
        // for (ILabel label : this._children) {
        // label.setParent(this);
        // }
        // this.dirty = true;
    }


    @Override
    public String getName() {
        return _name;
    }


    @Override
    public void setName(String name) {
        if (!this._name.equals(name)) {
            this._name = name;
            setDirty(true);
        }
    }


    @Override
    public List<ITerm> getTerms() {
        return _terms;
    }


    @Override
    public void setTerms(List<ITerm> terms) {
        this._terms = terms;
        setDirty(true);
    }


    /**
     * @param parentId
     */
    public void setParentId(int parentId) {
        if (this._parentId == parentId) {
            return;
        }

        this._parentId = parentId;
        setDirty(true);
    }


    public int getParentId() {
        return this._parentId;
    }


    @Override
    public boolean addChild(ILabel label) {
        if (_children.add(label)) {
            setDirty(true);
            return true;
        }
        return false;
    }


    @Override
    public boolean removeChild(ILabel label) {
        if (label == null)
            return false;

        if (_children.remove(label)) {
            ((DicItem) label).setInActive(true);
            _delChildren.add(label);
            setDirty(true);
            return true;
        }
        return false;
    }


    @Override
    public void addTerm(ITerm term) {
        if (!_terms.contains(term)) {
            if (_terms.add(term)) {
                _delTerms.remove(term); // Memo 再度紐づけられたとき、削除は取り消す Morishima
                setDirty(true);
            }
        }
    }


    @Override
    public void removeTerm(ITerm term) {
        if (term == null)
            return;

        if (_terms.remove(term)) {
            _delTerms.add(term);
            setDirty(true);
        }
    }


    @Override
    public String toString() {
        return _name;
    }


    /**
     * 
     * @return
     */
    public List<ITerm> getDelTerms() {
        return _delTerms;
    }


    public void clearDelTerms() {
        _delTerms.clear();
    }


    @Override
    public int hashCode() {
        if (_id == UNSAVED_ID) {
            return 47;
        }
        return _id;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LabelItem)) {
            return false;
        }
        LabelItem l2 = (LabelItem) obj;
        if (_id == UNSAVED_ID) {
            if (l2._id == UNSAVED_ID) {
                return equals(l2);
            }
            return false;
        }
        if (l2._id == UNSAVED_ID) {
            return false;
        }
        return _id == l2._id;
    }


    private boolean equals(LabelItem l2) {
        /* いずれかのidが-1のとき、フィールドを比べる */
        return _name.equals(l2._name) && _parentId == l2._parentId;
    }


    @Override
    public String getTreeName() {
        String str = this._name;
        if (this._parent != null) {
            str = _parent.getTreeName() + "/" + str;
        }
        return str;
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> descriptor = new ArrayList<IPropertyDescriptor>();
        descriptor.add(new TextPropertyDescriptor("id", "ID"));
        descriptor.add(new TextPropertyDescriptor("name", "ラベル名"));
        descriptor.add(new TextPropertyDescriptor("term", "用語"));
        for (int i = 0; i < getChildren().size(); i++) {
            descriptor.add(new PropertyDescriptor("child" + i, "子ラベル[" + i + "]"));
        }
        return descriptor.toArray(new IPropertyDescriptor[descriptor.size()]);

    }


    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals("id")) {
            return String.valueOf(getId());
        }
        if (id.equals("name")) {
            return getTreeName();
        }
        if (id instanceof String) {
            String s = (String) id;
            if (s.contains("child")) {
                s = s.replace("child", "");
                return getChildren().get(Integer.parseInt(s));
            }

        }

        if (id.equals("term")) {
            StringBuilder strBuf = new StringBuilder("");
            if (getTerms().size() < 1) {
                return null;
            }
            for (ITerm t : getTerms()) {
                strBuf.append(" , ");
                strBuf.append(t.getValue());
            }
            return strBuf.toString().substring(3);
        }
        return null;

    }
}
