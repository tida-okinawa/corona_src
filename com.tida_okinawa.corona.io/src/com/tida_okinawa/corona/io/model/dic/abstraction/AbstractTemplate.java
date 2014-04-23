/**
 * @version $Id: AbstractTemplate.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/22 14:47:23
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.abstraction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.tida_okinawa.corona.io.model.abstraction.CoronaObject;
import com.tida_okinawa.corona.io.model.dic.ITemplateFactory;
import com.tida_okinawa.corona.io.model.dic.ITemplateItem;
import com.tida_okinawa.corona.io.model.dic.impl.TemplateItem;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public abstract class AbstractTemplate extends CoronaObject implements ITemplateFactory {
    protected List<ITemplateItem> items;
    protected List<ITemplateItem> removeItems = new ArrayList<ITemplateItem>();
    protected Set<ITemplateItem> addItems = new TreeSet<ITemplateItem>(new Comparator<ITemplateItem>() {
        @Override
        public int compare(ITemplateItem o1, ITemplateItem o2) {
            /* 内容が同じでも別の辞書に登録された参照の可能性もあるので、ひな型IDが異なる時点で登録候補とする */
            int ret = o1.getTemplateId() - o2.getTemplateId();
            if (ret != 0) {
                return ret;
            }
            if (o1.getText().equals(o2.getText())) {
                return 0;
            }
            return 1;
        }
    });


    /**
     * ひな型の抽象化レイヤ
     */
    public AbstractTemplate() {
    }


    @Override
    public List<ITemplateItem> getItems() {
        if (items == null) {
            updateRecords();
        }
        return new ArrayList<ITemplateItem>(items);
    }


    @Override
    public void addItem(ITemplateItem item) {
        addItems.add(item);
    }


    @Override
    public void removeItem(ITemplateItem item) {
        removeItems.add(item);
    }


    @Override
    public ITemplateItem createTemplate(String name, String text, int patternType, boolean parts) {
        return new TemplateItem(ITemplateItem.DEFAULT_ID, name, text, patternType, parts);
    }


    @Override
    public ITemplateItem createTemplate(int templateId, String name, String text, int patternType, boolean parts) {
        return new TemplateItem(templateId, name, text, patternType, parts);
    }
}
