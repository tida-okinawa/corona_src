/**
 * @version $Id: SynonymItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.dic.IDependSub;
import com.tida_okinawa.corona.io.model.dic.ISynonym;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.abstraction.DicItem;


/**
 * @author shingo-takahashi
 * 
 */
public class SynonymItem extends DicItem implements ISynonym {

    protected ITerm _main;

    protected Map<Integer, IDependSub> _subs = new HashMap<Integer, IDependSub>();
    protected List<SynonymSubItem> _delSubs = new ArrayList<SynonymSubItem>();


    /**
     * @param main
     */
    public SynonymItem(ITerm main) {
        this._main = main;
    }


    @Override
    public ITerm getMain() {
        return _main;
    }


    @Override
    public void setMain(ITerm main) {
        if (main == null) {
            return;
        }
        this._main = main;
        setDirty(true);
    }


    @Override
    public List<ITerm> getSub() {
        List<ITerm> list = new ArrayList<ITerm>();
        /* Memo 一時対応。debugプロジェクトの機能でインポートされたとき、不都合があるので消す */
        for (Iterator<Entry<Integer, IDependSub>> itr = _subs.entrySet().iterator(); itr.hasNext();) {
            Entry<Integer, IDependSub> entry = itr.next();
            if (entry.getValue().getTerm() != null) {
                list.add(entry.getValue().getTerm());
            } else {
                itr.remove();
            }
        }
        return list;
    }


    @Override
    public void setSub(List<ITerm> sub) {
    }


    @Override
    public void addSub(ITerm sub) {
        SynonymSubItem item = (SynonymSubItem) this._subs.get(sub.getId());
        if (item == null) {
            item = new SynonymSubItem(0, sub, this);
            _subs.put(sub.getId(), item);
            setDirty(true);
        }
        item.setDicId(getComprehensionDicId());
    }


    @Override
    public boolean removeSub(ITerm sub) {
        SynonymSubItem item = (SynonymSubItem) this._subs.remove(sub.getId());
        if (item != null) {
            this._delSubs.add(item);
            item.setDicId(UNSAVED_ID);

            setDirty(true);
            return true;
        }
        return false;
    }


    /**
     * @return _subs
     */
    @Override
    public Map<Integer, IDependSub> getSubs() {
        return _subs;
    }


    /**
     * @param _subs
     */
    public void setSubs(Map<Integer, IDependSub> subs) {
        this._subs = subs;
        setDirty(true);
    }


    /**
     * @return _delSubs
     */
    public List<SynonymSubItem> getDelSubs() {
        return _delSubs;
    }


    /**
     * @param _delSubs
     */
    public void setDelSubs(List<SynonymSubItem> delSubs) {
        this._delSubs = delSubs;
    }


    @Override
    public boolean isDirty() {
        if (super.isDirty()) {
            return true;
        }

        for (Entry<Integer, IDependSub> entry : getSubs().entrySet()) {
            SynonymSubItem sub = (SynonymSubItem) entry.getValue();
            if (sub.isDirty()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "[" + _id + ":" + _main.getValue() + "]";
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
        if (super.equals(obj)) {
            return true;
        }

        if (!(obj instanceof SynonymItem)) {
            return false;
        }

        SynonymItem s2 = (SynonymItem) obj;
        if (_id == UNSAVED_ID) {
            if (s2._id == UNSAVED_ID) {
                return equals(s2);
            }
            return false;
        }
        if (s2._id == UNSAVED_ID) {
            return false;
        }
        return _id == s2._id;
    }


    private boolean equals(SynonymItem s2) {
        // Memo Listの比較は重いのでどうしよう
        return _main.equals(s2._main);
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> descriptor = new ArrayList<IPropertyDescriptor>();
        descriptor.add(new TextPropertyDescriptor("id", "ID"));
        descriptor.add(new TextPropertyDescriptor("name", "代表語"));
        for (int i = 0; i < getSub().size(); i++) {
            descriptor.add(new TextPropertyDescriptor(i, "従属語[" + i + "]"));
        }
        return descriptor.toArray(new IPropertyDescriptor[descriptor.size()]);
    }


    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals("id")) {
            return String.valueOf(getId());
        }
        if (id.equals("name")) {
            if (getMain() != null) {
                return getMain();
            }
        }
        if (id instanceof Integer) {
            return getSub().get((Integer) id);
        }

        return null;
    }
}
