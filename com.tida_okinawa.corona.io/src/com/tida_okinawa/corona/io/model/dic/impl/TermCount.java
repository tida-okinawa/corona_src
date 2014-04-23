/**
 * @version $Id: TermCount.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/27 17:39:02
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.dic.ITermCount;

/**
 * @author shingo-takahashi
 */
public class TermCount extends Term implements ITermCount {

    protected int count = 0;
    protected List<Integer> parentDicIds = new ArrayList<Integer>();


    /**
     * @param value
     * @param reading
     * @param termPart
     * @param termClass
     * @param cform
     * @param jumanBase
     */
    public TermCount(String value, String reading, String termPart, String termClass, String cform, String jumanBase) {
        super(value, reading, termPart, termClass, cform, jumanBase);
    }


    @Override
    public int getCount() {
        return count;
    }


    @Override
    public void setCount(int cnt) {
        this.count = cnt;
    }


    @Override
    public List<Integer> getParentDicIds() {
        return parentDicIds;
    }


    @Override
    public void setParentDicIds(List<Integer> parents) {
        this.parentDicIds = parents;
    }


    public String getStrParentsDicIds() {
        StringBuilder str = new StringBuilder(50);
        if (parentDicIds.size() > 0) {
            for (Integer i : parentDicIds) {
                str.append(" ").append(i);
            }
            return str.substring(1);
        }

        return "";
    }


    public void setParentDicIds(String data) {
        String[] strs = data.split(",");
        parentDicIds.clear();
        for (int i = 0; i < strs.length; i++) {
            try {
                parentDicIds.add(Integer.valueOf(strs[i]));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
    }


    /**
     * @param data
     *            半角スペース区切りで次の情報を持った文字列<br />
     *            "ID 単語 よみ 品詞 品詞詳細 活用詳細 Juman形式 登録元辞書ID（カンマ区切り） 登場回数"
     * @return
     */
    public static ITermCount create(String data) {
        String[] strs = data.split(" ");

        TermCount tc = new TermCount(strs[1], strs[2], strs[3], strs[4], strs[5], strs[6]);
        tc.setId(Integer.parseInt(strs[0]));
        tc.setParentDicIds(strs[7]);
        tc.setCount(Integer.parseInt(strs[8]));
        return tc;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof TermCount)) {
            return false;
        }

        TermCount o1 = this;
        TermCount o2 = (TermCount) obj;
        if (o1.getValue().equals(o2.getValue())) {
            if (o1.getReading().equals(o2.getReading())) {
                if (o1.getTermPart().equals(o2.getTermPart())) {
                    if (o1.getTermClass().equals(o2.getTermClass())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * 半角スペース区切りで次の情報を持った文字列<br />
     * "ID（常に０） 単語 よみ 品詞 品詞詳細 活用詳細 Juman形式 登録元辞書ID（カンマ区切り） 登場回数"
     * IDは出力したくないけど、この並びを変えると頻出エディタを直さないといけない
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(300);
        return toString(buf, " ", 0, getValue(), getReading(), getTermPart().getName(), getTermClass().getName(), getCform().getName(), getJumanBase(),
                getStrParentsDicIds(), count);
    }


    private static String toString(StringBuffer buf, String sepa, Object... args) {
        for (Object arg : args) {
            buf.append(sepa).append(arg);
        }
        return buf.substring(sepa.length());
    }


    @Override
    public Object clone() {
        TermCount ret = new TermCount(getValue(), getReading(), getTermPart().getName(), getTermClass().getName(), getCform().getName(), getJumanBase());
        ret.setCount(count);
        return ret;
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("id", "ID"), new TextPropertyDescriptor("token", "単語"),
                new TextPropertyDescriptor("reading", "読み"), new TextPropertyDescriptor("part", "品詞"), new TextPropertyDescriptor("class", "品詞細分類"),
                new TextPropertyDescriptor("form", "活用形"), new TextPropertyDescriptor("count", "カウント"), new TextPropertyDescriptor("dics", "所属辞書"), };
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
        if (id.equals("count")) {
            return getCount();
        }
        if (id.equals("dics")) {
            return getStrParentsDicIds();
        }
        return null;
    }
}
