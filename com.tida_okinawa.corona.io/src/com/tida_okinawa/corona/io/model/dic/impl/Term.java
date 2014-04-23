/**
 * @version $Id: Term.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.impl;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.TermCForm;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.io.model.dic.abstraction.DicItem;


/**
 * @author shingo-takahashi
 * 
 */
public class Term extends DicItem implements ITerm {
    private String value = "";

    private TermPart termPart = TermPart.NONE;

    private TermClass termClass = TermClass.NONE;

    private String reading = "";

    private TermCForm cform = TermCForm.NONE;

    private String jumanBase = "";


    /**
     * @param name
     * @param reading2
     * @param termClass2
     * @param termPart2
     * @param ctype2
     * @param cform2
     */
    public Term(String value, String reading, String termPart, String termClass, String cform, String jumanBase) {
        setValue(value);
        setReading(reading);
        setTermPart(TermPart.valueOfName(termPart));
        setTermClass(TermClass.valueOf(termClass, this.termPart));
        setCform(TermCForm.valueOf(cform, this.termPart, this.termClass));
        setJumanBase(jumanBase);
        setDirty(false);
    }


    public Term(String value, String reading, TermPart termPart, TermClass termClass, TermCForm cform, String jumanBase) {
        setValue(value);
        setReading(reading);
        setTermPart(termPart);
        setTermClass(termClass);
        setCform(cform);
        setJumanBase(jumanBase);
        setDirty(false);
    }


    @Override
    public void setValue(String value) {
        if (value == null) {
            value = "";
        }
        if (!this.value.equals(value)) {
            this.value = value;
            setDirty(true);
        }
    }


    @Override
    public String getValue() {
        return value;
    }


    @Override
    public void setTermPart(TermPart termPart) {
        if (termPart == null) {
            termPart = TermPart.NONE;
        }
        if (!this.termPart.equals(termPart)) {
            this.termPart = termPart;
            setDirty(true);
        }
    }


    @Override
    public TermPart getTermPart() {
        return termPart;
    }


    @Override
    public void setTermClass(TermClass termClass) {
        if (termClass == null) {
            termClass = TermClass.NONE;
        }
        if (!this.termClass.equals(termClass)) {
            this.termClass = termClass;
            setDirty(true);
        }
    }


    @Override
    public TermClass getTermClass() {
        return termClass;
    }


    @Override
    public void setReading(String reading) {
        if (reading == null) {
            reading = "";
        }
        if (!this.reading.equals(reading)) {
            this.reading = reading;
            setDirty(true);
        }
    }


    @Override
    public String getReading() {
        return reading;
    }


    @Override
    public void setCform(TermCForm cform) {
        if (cform == null) {
            cform = TermCForm.NONE;
        }
        if (!this.cform.equals(cform)) {
            this.cform = cform;
            setDirty(true);
        }
    }


    @Override
    public TermCForm getCform() {
        return cform;
    }


    @Override
    public void setJumanBase(String jumanBase) {
        if (jumanBase == null) {
            jumanBase = "";
        }
        if (!this.jumanBase.equals(jumanBase)) {
            setDirty(true);
            this.jumanBase = jumanBase;
        }
    }


    @Override
    public String getJumanBase() {
        return jumanBase;
    }


    @Override
    public boolean isError() {
        /* 単語　空欄チェック　 */
        if (getValue().equals("")) {
            return true;
        }
        /* 単語　64文字チェック　 */
        if (getValue().length() > 64) {
            return true;
        }

        /* 品詞　空欄チェック　 */
        // 品詞は空欄にできない

        /* 品詞詳細　空欄チェック　 */
        // 品詞に対する品詞詳細がある場合は空欄にできない
        // 品詞によっては空欄の場合がある

        /* 読み　空欄チェック　 */
        if (getReading().equals("")) {
            return true;
        }
        if (getReading().length() > 64) {
            return true;
        }

        return false;
    }


    @Override
    public String getKeyword() {
        StringBuilder buf = new StringBuilder(100);
        buf.append(value).append(",").append(reading).append(",").append(termPart.getName());
        return buf.toString();
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

        if (!(obj instanceof Term)) {
            return false;
        }

        Term t2 = (Term) obj;
        if (_id == UNSAVED_ID) {
            if (t2._id == UNSAVED_ID) {
                /* 未保存単語をコピペしたとき、ユーザ用語辞書の#列が同じ値になってしまうので、Object比較 */
                return this == obj;
            }
            return false;
        }
        if (t2._id == UNSAVED_ID) {
            return false;
        }
        return _id == t2._id;
    }


    @Override
    public String toString() {
        return value;
    }


    @Override
    public Object clone() {
        Term ret = new Term(value, reading, termPart, termClass, cform, jumanBase);
        return ret;
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("id", "ID"), new TextPropertyDescriptor("token", "単語"),
                new TextPropertyDescriptor("reading", "読み"), new TextPropertyDescriptor("part", "品詞"), new TextPropertyDescriptor("class", "品詞細分類"),
                new TextPropertyDescriptor("form", "活用形"), };
        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals("id")) {
            return String.valueOf(getId());
        }
        if (id.equals("reading")) {
            return getReading();
        }
        if (id.equals("token")) {
            return getValue();
        }
        if (id.equals("part")) {
            return getTermPart().getName();
        }
        if (id.equals("class")) {
            return getTermClass().getName();
        }
        if (id.equals("form")) {
            return getCform().getName();
        }
        return null;
    }
}
