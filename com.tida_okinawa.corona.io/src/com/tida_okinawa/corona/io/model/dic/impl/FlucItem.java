/**
 * @version $Id: FlucItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.dic.IDependSub;
import com.tida_okinawa.corona.io.model.dic.IFluc;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.abstraction.DicItem;


/**
 * @author shingo-takahashi
 * 
 */
public class FlucItem extends DicItem implements IFluc {

    protected ITerm _main;

    protected Map<Integer, IDependSub> _subs = new HashMap<Integer, IDependSub>();
    protected List<FlucSubItem> _delSubs = new ArrayList<FlucSubItem>();


    /**
     * @param main
     */
    public FlucItem(ITerm main) {
        Assert.isNotNull(main); // Assert
        this._main = main;
    }


    @Override
    public ITerm getMain() {
        return _main;
    }


    @Override
    public void setMain(ITerm main) {
        if ((main == null) || _main.equals(main)) {
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
        FlucSubItem item = (FlucSubItem) this._subs.get(sub.getId());
        if (item == null) {
            item = new FlucSubItem(0, sub, this);
            _subs.put(sub.getId(), item);
            setDirty(true);
        }
        item.setDicId(getComprehensionDicId());
    }


    @Override
    public boolean removeSub(ITerm sub) {
        FlucSubItem item = (FlucSubItem) this._subs.get(sub.getId());
        if (item != null) {
            if (_subs.remove(sub.getId()) != null) {
                this._delSubs.add(item);
                item.setDicId(UNSAVED_ID);

                setDirty(true);
                return true;
            }
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
     *            セットする _subs
     */
    public void setSubs(Map<Integer, IDependSub> subs) {
        this._subs = subs;
        setDirty(true);
    }


    /**
     * @return _delSubs
     */
    public List<FlucSubItem> getDelSubs() {
        return _delSubs;
    }


    /**
     * @param _delSubs
     *            セットする _delSubs
     */
    public void setDelSubs(List<FlucSubItem> delSubs) {
        this._delSubs = delSubs;
    }


    @Override
    public String toString() {
        return "[" + _id + ":" + _main.getValue() + "]";
    }


    @Override
    public boolean isDirty() {
        if (super.isDirty()) {
            return true;
        }

        for (Entry<Integer, IDependSub> entry : getSubs().entrySet()) {
            FlucSubItem sub = (FlucSubItem) entry.getValue();
            if (sub.isDirty()) {
                return true;
            }
        }
        return false;
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


    @Override
    public int hashCode() {
        if (_id == UNSAVED_ID) {
            return 47;
        }
        return _id;
    }


    @Override
    public boolean equals(Object object) {
        // TODO: equals実装漏れ
        return super.equals(object);
    }
}
