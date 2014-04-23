/**
 * @version $Id: AbstractLabelDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.abstraction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;

/**
 * ラベル辞書抽象クラス
 * 
 * @author OSC
 * 
 */
public abstract class AbstractLabelDic extends AbstractDic implements ILabelDic {
    protected List<IDicItem> items;
    protected List<IDicItem> delItems = new ArrayList<IDicItem>();


    public AbstractLabelDic(int id, String name, Date lasted) {
        super(id, name, lasted);
    }


    /**
     * @param id
     * @param name
     * @param lasted
     * @param parentId
     */
    public AbstractLabelDic(int id, String name, Date lasted, Set<Integer> parentId) {
        super(id, name, lasted, parentId);
    }


    @Override
    public List<IDicItem> getItems() {

        if (items == null) {
            updateRecords();
        }
        return items;
    }


    @Override
    public void addItem(IDicItem item) {
        if (items == null) {
            updateRecords();
        }
        if (items.add(item)) {
            ((DicItem) item).setInActive(false);
            ((DicItem) item).setDicId(getId());
            delItems.remove(item);
            setDirty(true);
        }
    }


    @Override
    public void removeItem(IDicItem item) {
        if (item == null)
            return;

        if (items == null) {
            updateRecords();
        }

        assert item instanceof ILabel;

        ILabel label = (ILabel) item;
        ILabel parent = label.getParent();
        boolean res = false;
        if (parent != null) {
            res = parent.removeChild(label);
        } else {
            res = items.remove(label); /* トップレベルの場合 */
        }
        if (res) {
            ((DicItem) item).setInActive(true);
            ((DicItem) item).setDicId(UNSAVED_ID);
            delItems.add(item);
            setDirty(true);
        }
    }


    /**
     * アイテム設定
     * 
     * @param items
     */
    public void setItems(List<IDicItem> items) {
        this.items = items;
        setDirty(true);
    }

}
