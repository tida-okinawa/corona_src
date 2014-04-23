/**
 * @version $Id: TemplateRecords.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/27 9:23:48
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tida_okinawa.corona.correction.template.Template;
import com.tida_okinawa.corona.correction.template.TemplateLink;
import com.tida_okinawa.corona.correction.template.TemplateRecord;
import com.tida_okinawa.corona.io.model.dic.ITemplateFactory;
import com.tida_okinawa.corona.io.model.dic.ITemplateItem;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateRecords {
    private static final String PREFIX = Messages.TEMPLATE_RECORDS_PREFIX;
    private int no;

    private ITemplateFactory iTemplate;
    private List<TemplateRecord> templateRecords;

    private Map<Integer, String> templateName = new HashMap<Integer, String>();


    /**
     * ひな型のレコード一覧
     * 
     * @param iTemplate
     *            ひな型アイテム
     */
    public TemplateRecords(ITemplateFactory iTemplate) {
        no = 0;
        this.iTemplate = iTemplate;

        /* データベースからひな型のアイテムを取得 */
        List<ITemplateItem> items = iTemplate.getItems();

        /* 取得したひな型のアイテム一覧をマッピング */
        for (ITemplateItem item : items) {
            templateName.put(item.getId(), item.getName());
        }

        /* 取得したひな型のアイテム一覧をひな型レコード（複）に追加 */
        templateRecords = new ArrayList<TemplateRecord>(items.size() + 10);
        for (ITemplateItem item : items) {
            TemplateRecord rec = new TemplateRecord(item);
            init(rec);
            templateRecords.add(rec);
        }

        for (TemplateRecord rec : templateRecords) {
            String name = rec.getName();
            if (rec.getName().startsWith(PREFIX)) {
                try {
                    int num = Integer.parseInt(name.substring(PREFIX.length()));
                    if (no < num) {
                        no = num;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
    }


    private void init(Template template) {
        if (template instanceof TemplateLink) {
            ((TemplateLink) template).setLabel(templateName.get(((TemplateLink) template).getId()));
        }
    }


    /**
     * @param record
     *            ひな型の追加
     */
    public void add(TemplateRecord record) {
        add(size(), record);
    }


    /**
     * @param index
     *            インデックス数
     * @param record
     *            ひな型の追加
     */
    public void add(int index, TemplateRecord record) {
        if ((index < 0) || (index > size())) {
            return;
        }

        if (record.getId() == ITemplateItem.DEFAULT_ID) {
            record.getITemplateItem().setDirty(true);
        }

        if ("".equals(record.getName())) { //$NON-NLS-1$
            record.setName(nextTemplateName());
        }

        iTemplate.addItem(record.getITemplateItem());
        templateRecords.add(index, record);
    }


    /**
     * ひな型の削除
     * 
     * @param record
     *            削除するレコード
     */
    public void remove(TemplateRecord record) {
        iTemplate.removeItem(record.getITemplateItem());
        templateRecords.remove(record);
    }


    /**
     * ひな型レコード一覧の取得
     * 
     * @return ひな型レコード一覧
     */
    public List<TemplateRecord> getTemplateRecords() {
        return new ArrayList<TemplateRecord>(templateRecords);
    }


    /**
     * ひな型の取得
     * 
     * @param index
     *            インデックス
     * @return ひな型
     */
    public TemplateRecord get(int index) {
        return templateRecords.get(index);
    }


    /**
     * ひな型のインデックスの取得
     * 
     * @param record
     *            ひな型
     * @return インデックス
     */
    public int indexOf(TemplateRecord record) {
        return templateRecords.indexOf(record);
    }


    /**
     * 次のひな型名の生成
     * 
     * @return ひな型名
     */
    public String nextTemplateName() {
        no++;
        return PREFIX + no;
    }


    /**
     * @return ひな型のサイズ
     */
    public int size() {
        return templateRecords.size();
    }


    /**
     * ひな型に単語（Term）があるかどうかの判定
     * 
     * @return n>0:単語、-1:参照 or データがない
     */
    public int isTemplateTerm() {
        for (int pos = 0; pos < templateRecords.size(); pos++) {
            if (templateRecords.get(pos).getTemplateId() == 0) {
                return pos;
            }
        }
        return -1;
    }


    /**
     * TEMPLATE_IDが一致するひな型のIDを取得する
     * 
     * @param templateId
     *            TEMPLATE_ID
     * @return ID
     */
    public int checkItems(int templateId) {
        int id = ITemplateItem.DEFAULT_ID;
        List<ITemplateItem> items = iTemplate.getItems();
        for (ITemplateItem item : items) {
            if (item.getTemplateId() == templateId) {
                id = item.getId();
                break;
            }
        }
        return id;
    }
}
