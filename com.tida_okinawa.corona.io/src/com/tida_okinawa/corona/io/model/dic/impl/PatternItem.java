/**
 * @version $Id: PatternItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.impl;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.dic.abstraction.DicItem;


public class PatternItem extends DicItem implements IPattern {

    private int _patternType = 0;

    private String _text = "";

    private boolean _parts = false;

    private String _label = "";


    public PatternItem(String label, String text, int patternType, boolean parts) {
        this(0, label, text, patternType, parts);
    }


    public PatternItem(int id, String label, String text, int patternType, boolean parts) {
        super(id);
        this._label = label;
        this._text = text;
        this._patternType = patternType;
        this._parts = parts;
    }


    @Override
    public void setPatternType(int patternType) {
        if (this._patternType != patternType) {
            this._patternType = patternType;
            setDirty(true);
        }
    }


    @Override
    public int getPatternType() {
        return _patternType;
    }


    @Override
    public void setText(String text) {
        if (!this._text.equals(text)) {
            this._text = (text == null) ? "" : text;
            setDirty(true);
        }
    }


    @Override
    public String getText() {
        return _text;
    }


    @Override
    public void setParts(boolean parts) {
        if (this._parts != parts) {
            this._parts = parts;
            setDirty(true);
        }
    }


    @Override
    public boolean isParts() {
        return _parts;
    }


    @Override
    public void setLabel(String text) {
        if (!this._label.equals(text)) {
            _label = (text == null) ? "" : text;
            setDirty(true);
        }
    }


    @Override
    public String getLabel() {
        return _label;
    }


    @Override
    public String toString() {
        return "[" + _id + ":" + _text + "]";
    }


    @Override
    public int hashCode() {
        if (_id == UNSAVED_ID) {
            return 47;
        }
        return _id;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PatternItem)) {
            return false;
        }

        PatternItem p2 = (PatternItem) obj;
        if ((_id == UNSAVED_ID) || (p2._id == UNSAVED_ID)) {
            return false;
        }
        return _id == p2._id;
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("id", "ID"), new TextPropertyDescriptor("type", "パターン分類"),
                new TextPropertyDescriptor("label", "パターン名"), };
        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals("id")) {
            return String.valueOf(getId());
        }
        if (id.equals("type")) {
            return PatternType.getPatternType(getPatternType()).getPatternName();
        }
        if (id.equals("label")) {
            return getLabel();
        }
        if (id.equals("test")) {
            return getText();
        }

        return null;
    }
}
