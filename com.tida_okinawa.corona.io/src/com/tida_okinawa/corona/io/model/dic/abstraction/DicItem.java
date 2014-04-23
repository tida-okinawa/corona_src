/**
 * @version $Id: DicItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.abstraction;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.abstraction.CoronaObject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;

/**
 * @author shingo-takahashi
 * 
 */
public abstract class DicItem extends CoronaObject implements IDicItem, Comparable<IDicItem> {
    protected int _id;

    protected int dicId = ICoronaDic.UNSAVED_ID;

    protected boolean inActive = false;

    private boolean dirty = false;


    public DicItem() {
        this(UNSAVED_ID);
    }


    public DicItem(int id) {
        _id = id;
    }


    @Override
    public void setId(int id) {
        this._id = id;
    }


    @Override
    public int getId() {
        return _id;
    }


    @Override
    public boolean isDirty() {
        return dirty;
    }


    @Override
    public void setDirty(boolean dirty) {
        if (this.dirty == dirty) {
            return;
        }
        this.dirty = dirty;
        dirtyChanged();
    }


    private void dirtyChanged() {
        if (dicId == ICoronaDic.UNSAVED_ID) {
            return;
        }

        // TODO 子でOverrideして、親辞書の種類を絞りたい
        ICoronaDic parent = ((IoService) IoActivator.getService()).getDictionary(dicId);
        if (parent != null) {
            if (dirty) {
                ((AbstractDic) parent).addDirty(this);
            } else {
                ((AbstractDic) parent).removeDirty(this);
            }
        }
    }


    /**
     * @param dicId
     */
    public void setDicId(int dicId) {
        this.dicId = dicId;
    }


    @Override
    public int getComprehensionDicId() {
        return dicId;
    }


    /**
     * @return inActive
     */
    @Override
    public boolean isInActive() {
        return inActive;
    }


    /**
     * @param inActive
     *            セットする inActive
     */
    public void setInActive(boolean inActive) {
        this.inActive = inActive;
    }


    @Override
    public int compareTo(IDicItem o) {
        if (o == null)
            return -1;
        return o.getId() - getId();
    }


    @Override
    abstract public String toString();
}