/**
 * @version $Id: AbstractPatternDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.abstraction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;


/**
 * @author shingo-takahashi
 * 
 */
public abstract class AbstractPatternDic extends AbstractDic implements IPatternDic {
    protected List<IDicItem> items;
    protected List<IDicItem> delItems = new ArrayList<IDicItem>();
    /* #482対応。すぐに消さないので、すぐに追加しもしない。 */
    protected Set<IDicItem> addItems = new TreeSet<IDicItem>(new Comparator<IDicItem>() {
        @Override
        public int compare(IDicItem o1, IDicItem o2) {
            int ret = o1.getId() - o2.getId();
            if (ret != 0) {
                return ret;
            }
            if (o1.equals(o2)) {
                return 0;
            }
            return 1; /* 後から入ってきたものを後ろに */
        }
    });


    public AbstractPatternDic(int id, String name, Date lasted) {
        super(id, name, lasted);
    }


    @Override
    public List<IDicItem> getItems() {
        if (items == null) {
            updateRecords();
        }
        return new ArrayList<IDicItem>(items);
    }


    @Override
    public void addItem(IDicItem item) {
        if (addItems.add(item)) {
            ((DicItem) item).setInActive(false);
            ((DicItem) item).setDicId(getId());
            delItems.remove(item);
            setDirty(true);
        }
    }


    /* TODO getRemoveItemを作りたい！！ */

    @Override
    public void removeItem(IDicItem item) {
        if (item == null)
            return;

        /* TODO 保存せずに閉じて開きなおしたとき、アイテムの順番が変わってしまうので、ここで消さない */
        /* commit時、削除できたものをitemsから除去するような修正は済(PatternDic#commitのみ) */
        // items.remove(item);
        addItems.remove(item);
        ((DicItem) item).setInActive(true);
        delItems.add(item);
        ((DicItem) item).setDicId(UNSAVED_ID);
        setDirty(true);
    }


    @Override
    public String getItemName(int id) {
        return "";
    }

}
