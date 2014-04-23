/**
 * @version $Id: TemplateItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/21 17:00:46
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.impl;

import com.tida_okinawa.corona.io.model.abstraction.CoronaObject;
import com.tida_okinawa.corona.io.model.dic.ITemplateItem;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateItem extends CoronaObject implements ITemplateItem {

    private int _id;
    private int _templateId = DEFAULT_ID;
    private String _name = "";
    private String _text = "";
    private int _patternType = 0;
    private boolean _parts = false;
    private boolean _inActive = false;
    private boolean dirty = false;


    /**
     * ひな型アイテム
     * 
     * @param name
     *            パターン名
     * @param text
     *            パターン情報
     * @param patternType
     *            パターン分類ID
     * @param parts
     *            参照可否フラグ
     */
    public TemplateItem(String name, String text, int patternType, boolean parts) {
        this(UNSAVED_ID, DEFAULT_ID, name, text, patternType, parts);
    }


    /**
     * ひな型アイテム
     * 
     * @param templateId
     *            ひな型ID
     * @param name
     *            ひな型名
     * @param text
     *            ひな型情報
     * @param patternType
     *            パターン分類ID
     * @param parts
     *            参照可否フラグ
     */
    public TemplateItem(int templateId, String name, String text, int patternType, boolean parts) {
        this(UNSAVED_ID, templateId, name, text, patternType, parts);
    }


    /**
     * ひな型アイテム
     * 
     * @param id
     *            ID
     * @param templateId
     *            ひな型ID
     * @param name
     *            ひな型名
     * @param text
     *            ひな型情報
     * @param patternType
     *            パターン分類ID
     * @param parts
     *            参照可否フラグ
     */
    public TemplateItem(int id, int templateId, String name, String text, int patternType, boolean parts) {
        this._id = id;
        this._templateId = templateId;
        this._name = name;
        this._text = text;
        this._patternType = patternType;
        this._parts = parts;
    }


    /**
     * @param id
     *            ひな型管理用ID
     */
    @Override
    public void setId(int id) {
        this._id = id;
    }


    /**
     * @return _id ひな型管理用ID
     */
    @Override
    public int getId() {
        return _id;
    }


    /**
     * @param templateId
     *            ひな型ID（TEMPLATE_ID）
     */
    @Override
    public void setTemplateId(int templateId) {
        this._templateId = templateId;
        setDirty(true);
    }


    /**
     * @return _dicId ひな型ID（TEMPLATE_ID）
     */
    @Override
    public int getTemplateId() {
        return _templateId;
    }


    /**
     * @param text
     *            ひな型名
     */
    @Override
    public void setName(String text) {
        if (!this._name.equals(text)) {
            _name = (text == null) ? "" : text;
            setDirty(true);
        }
    }


    /**
     * @return _name ひな型名
     */
    @Override
    public String getName() {
        return _name;
    }


    /**
     * @param text
     *            ひな型情報
     */
    @Override
    public void setText(String text) {
        if (!this._text.equals(text)) {
            this._text = (text == null) ? "" : text;
            setDirty(true);
        }
    }


    /**
     * @return _text ひな型情報
     */
    @Override
    public String getText() {
        return _text;
    }


    /**
     * @param patternType
     *            パターン分類.パターン分類ID
     */
    @Override
    public void setPatternType(int patternType) {
        if (this._patternType != patternType) {
            this._patternType = patternType;
            setDirty(true);
        }
    }


    /**
     * @return _patternType パターン分類.パターン分類ID
     */
    @Override
    public int getPatternType() {
        return _patternType;
    }


    /**
     * @param parts
     *            参照可否フラグ
     */
    @Override
    public void setParts(boolean parts) {
        if (this._parts != parts) {
            this._parts = parts;
            setDirty(true);
        }
    }


    /**
     * @return _parts
     *         参照可否フラグ
     */
    @Override
    public boolean isParts() {
        return _parts;
    }


    /**
     * @param inActive
     *            アクティブかどうか
     */
    @Override
    public void setInActive(boolean inActive) {
        this._inActive = inActive;
    }


    /**
     * @return _inActive アクティブかどうか
     */
    @Override
    public boolean isInActive() {
        return _inActive;
    }


    /**
     * @param dirty
     *            ダーティ・フラグ（更新が必要かどうかを判定）
     */
    @Override
    public void setDirty(boolean dirty) {
        if (this.dirty == dirty) {
            return;
        }
        this.dirty = dirty;
    }


    /**
     * @return dirty ダーティ・フラグ（更新が必要かどうかを判定）
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }


    @Override
    public String toString() {
        return "[" + _id + ":" + _text + "]";
    }
}
