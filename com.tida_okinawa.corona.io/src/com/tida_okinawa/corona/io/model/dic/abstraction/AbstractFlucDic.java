/**
 * @version $Id: AbstractFlucDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
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
import com.tida_okinawa.corona.io.model.dic.IFlucDic;


/**
 * @author shingo-takahashi
 * 
 */
public abstract class AbstractFlucDic extends AbstractDic implements IFlucDic {
    protected List<IDicItem> items = null;
    protected List<IDicItem> delItems = new ArrayList<IDicItem>();

    private int projectId;


    public AbstractFlucDic(int id, String name, Date lasted) {
        super(id, name, lasted);
    }


    /**
     * @param id
     * @param name
     * @param lasted
     * @param parentId
     */
    public AbstractFlucDic(int id, String name, Date lasted, Set<Integer> parentId) {
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
        // TODO superに作りたい
        if (items.add(item)) {
            ((DicItem) item).setInActive(false);
            ((DicItem) item).setDicId(getId());
            delItems.remove(item);
            setDirty(true);
        }
    }


    @Override
    public void removeItem(IDicItem item) {
        // TODO superに作りたい
        if (item == null)
            return;

        if (items.remove(item)) {
            ((DicItem) item).setInActive(true);
            ((DicItem) item).setDicId(UNSAVED_ID);
            delItems.add(item);
            setDirty(true);
        }
    }


    /**
     * 外部辞書インポート
     * 
     * @param path
     * 
     */
    @Override
    public void importDic(String path) {
        importDicDam(path);
    }


    abstract protected void importDicDam(String path);
}
