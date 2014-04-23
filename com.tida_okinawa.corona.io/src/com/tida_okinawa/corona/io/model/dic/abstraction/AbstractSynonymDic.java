/**
 * @version $Id: AbstractSynonymDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
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
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;


/**
 * @author shingo-takahashi
 * 
 */
public abstract class AbstractSynonymDic extends AbstractDic implements ISynonymDic {
    protected List<IDicItem> items;
    protected List<IDicItem> delItems = new ArrayList<IDicItem>();

    private int projectId;


    public AbstractSynonymDic(int id, String name, Date lasted) {
        super(id, name, lasted);
    }


    /**
     * @param id
     * @param name
     * @param lasted
     * @param parentId
     */
    public AbstractSynonymDic(int id, String name, Date lasted, Set<Integer> parentId) {
        super(id, name, lasted, parentId);
    }


    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }


    public int getProjectId() {
        return projectId;
    }


    public void setItems(List<IDicItem> items) {
        this.items = items;
        setDirty(true);
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
        if (items.remove(item)) {
            ((DicItem) item).setInActive(true);
            ((DicItem) item).setDicId(UNSAVED_ID);
            delItems.add(item);
            setDirty(true);
        }
    }


    @Override
    public void importDic(String path) {
        importDicDam(path);
    }


    abstract protected void importDicDam(String path);
}
