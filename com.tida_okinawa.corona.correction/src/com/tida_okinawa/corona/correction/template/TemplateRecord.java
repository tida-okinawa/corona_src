/**
 * @version $Id: TemplateRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/26 13:35:11
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.ITemplateItem;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 *         ひな型レコード
 */
public class TemplateRecord extends TemplateContainer {

    private ITemplateItem iTemplateItem;
    private Template template;


    /**
     * @param object
     *            ひな型に登録する情報<br/>
     *            ・PatternRecord -> パターンからひな型を生成する場合<br/>
     *            ・TemplateRecord -> ひな型レコードから新たにひな型を生成する場合<br/>
     *            ・ITemplateItem -> ひな型アイテムからレコードを生成する場合<br/>
     *            ・nullの場合は空のひな型を生成
     */
    public TemplateRecord(Object object) {
        super(null);
        init(object);
    }


    private void init(Object object) {

        /* null */
        if (object == null) {
            /* 空データの生成 ：ID = UNSAVED_ID（未保存） */
            this.iTemplateItem = IoActivator.getTemplateFactory().createTemplate("", "", -99, false); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        /* PatternRecord */
        if (object instanceof PatternRecord) {
            this.iTemplateItem = convertPatternToTemplate(((PatternRecord) object).getIPattern());
            return;
        }
        /* TemplateRecord */
        if (object instanceof TemplateRecord) {
            ITemplateItem record = ((TemplateRecord) object).getITemplateItem();
            this.iTemplateItem = IoActivator.getTemplateFactory().createTemplate(record.getTemplateId(), record.getName(), record.getText(),
                    record.getPatternType(), record.isParts());
            return;
        }
        /* ITemplateItem */
        if (object instanceof ITemplateItem) {
            this.iTemplateItem = ((ITemplateItem) object);
            return;
        }
    }


    @Override
    protected void createChildren() {
        super.createChildren();
        /* 読み出したデータがあれば変換 */
        template = TemplateUtil.convertFrom(iTemplateItem.getText());
        if (template != null) {
            /* トップ要素には親が設定されていないので、セットする */
            template.setParent(this);
            /* 追加イベントが起きてしまうので、直接addしている */
            children.add(template);
        }
    }


    /* ****************************************
     * プロパティフィールド
     */

    /**
     * ひな型のID取得
     * 
     * @return ひな型のID
     */
    public int getId() {
        return iTemplateItem.getId();
    }


    /**
     * ひな型のID設定
     * 
     * @param id
     *            ひな型のID
     */
    public void setId(int id) {
        if (iTemplateItem.getId() == id) {
            return;
        }
        iTemplateItem.setId(id);
    }


    /**
     * ひな型のTEMPLATE_ID取得
     * 
     * @return ひな型のTEMPLATE_ID<br/>
     *         0:ひな型（デフォルト値）、n>0:パターンID（参照用）
     */
    public int getTemplateId() {
        return iTemplateItem.getTemplateId();
    }


    /**
     * ひな型のTEMPLATE_ID設定
     * 
     * @param templateId
     *            ひな型のTEMPLATE_ID<br/>
     *            0:ひな型（デフォルト値）、n>0:パターンID（参照用）
     */
    public void setTemplateId(int templateId) {
        if (iTemplateItem.getTemplateId() == templateId) {
            return;
        }
        iTemplateItem.setTemplateId(templateId);
    }


    /**
     * ひな型名取得
     * 
     * @return ひな型名
     */
    public String getName() {
        return iTemplateItem.getName();
    }


    /**
     * ひな型名設定
     * 
     * @param name
     *            ひな型名
     */
    public void setName(String name) {
        if (iTemplateItem.getName().equals(name)) {
            return;
        }
        iTemplateItem.setName((name == null) ? "" : name); //$NON-NLS-1$
    }


    /**
     * ひな型分類ID取得
     * 
     * @return ひな型分類ID
     */
    public int getType() {
        return iTemplateItem.getPatternType();
    }


    /**
     * ひな型分類ID設定
     * 
     * @param type
     *            ひな型分類ID
     */
    public void setType(int type) {
        if (iTemplateItem.getPatternType() == type) {
            return;
        }
        iTemplateItem.setPatternType(type);
    }


    /**
     * 参照可否フラグ取得
     * 
     * @return 参照可否フラグ
     */
    public boolean isPart() {
        return iTemplateItem.isParts();
    }


    /**
     * 参照可否フラグ設定
     * 
     * @param part
     *            参照可否フラグ
     */
    public void setPart(boolean part) {
        if (iTemplateItem.isParts() == part) {
            return;
        }
        iTemplateItem.setParts(part);
    }


    /* ****************************************
     * ひな型
     */

    /**
     * ひな型の取得
     * 
     * @return ひな型（UI表示用）
     */
    public Template getTemplate() {
        return this.template;
    }


    /**
     * ひな型の設定
     * 
     * @param template
     *            ひな型（UI表示用）
     */
    public void setTemplate(Template template) {
        if (this.template == null) {
            if (template == null) {
                return;
            }
        } else {
            if (this.template.equals(template)) {
                return;
            }
        }
        this.template = template;
        propertyChanged();
    }


    /**
     * ひな型アイテム取得
     * 
     * @return ひな型アイテム
     */
    public ITemplateItem getITemplateItem() {
        return iTemplateItem;
    }


    /* ****************************************
     * Pattern
     */

    /* パターンからひな型への変換 */
    private ITemplateItem convertPatternToTemplate(IPattern iPattern) {

        /* パターン文字列（xml形式）からひな型（UI表示用）を生成 */
        template = TemplateUtil.convertFrom(iPattern.getText());
        setTemplate(template);

        /* ひな型（UI表示用）をひな型文字列（xml形式）に変換 */
        String text = TemplateUtil.convertFrom(template);

        /* ひな型アイテム構築 */
        return IoActivator.getTemplateFactory().createTemplate(iPattern.getLabel(), text, iPattern.getPatternType(), iPattern.isParts());
    }


    /* ****************************************
     * 子要素の管理
     */
    @Override
    public boolean hasChildren() {
        if (!childCreated) {
            return super.hasChildren();
        }
        return template != null;
    }


    /* ****************************************
     * 表示更新イベント管理
     */
    @Override
    protected void propertyChanged(Template own) {
        if (!childCreated) {
            createChildren();
        }
        iTemplateItem.setText(TemplateUtil.convertFrom(template));
    }


    /* ****************************************
     * その他
     */
    @Override
    public String toString() {
        return getName();
    }
}
