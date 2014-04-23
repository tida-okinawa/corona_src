/**
 * @version $Id: FrequentEditData.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/31 16:58:02
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.correction.frequent.FrequentRecord;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.ITermCount;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * @author kousuke-morishima
 */
class FrequentEditData {
    /**
     * 登録先辞書
     */
    private ICoronaDic dic;
    /**
     * この単語が登録されている辞書の名前のリスト
     */
    List<String> dicNameList;
    /**
     * 単語
     */
    private ITermCount term;

    private List<ICoronaDic> dicList;

    private boolean dirty = false;


    /**
     * @param s
     * @param dics
     *            登録先辞書
     */
    FrequentEditData(String s, List<ICoronaDic> dics) {
        this(s, dics, null);
    }


    FrequentEditData(String s, List<ICoronaDic> dics, ICoronaDic dic) {
        FrequentRecord fr = new FrequentRecord(s);

        this.dic = dic;
        term = IoActivator.getDicFactory().createTermCount(fr.getGenkei(), fr.getYomi(), fr.getHinshi(), fr.getHinshiSaibunrui(), fr.getCform(), "");
        term.setCount(fr.getCount());
        dicList = dics;

        /* 　未定義語は品詞詳細と読みを空白にする　 */
        if (term.getTermPart().equals(TermPart.UNKNOWN)) {
            term.setReading("");
            term.setTermPart(TermPart.NONE);
            term.setTermClass(TermClass.NONE);
        }
    }


    public ITerm getTerm() {
        return this.term;
    }


    public Integer getCount() {
        return term.getCount();
    }


    public void addCount(int count) {
        term.setCount(term.getCount() + count);
    }


    public List<String> getRegisteredDics() {
        if (dicNameList == null) {
            createDicNameList(term.getParentDicIds());
        }
        return this.dicNameList;
    }


    private void createDicNameList(List<Integer> list) {
        dicNameList = new ArrayList<String>();
        for (Integer id : list) {
            for (int i = 0; i < dicList.size(); i++) {
                if (dicList.get(i).getId() == id) {
                    dicNameList.add(dicList.get(i).getName());
                }
            }
        }
    }


    /**
     * @return 登録先辞書
     */
    public ICoronaDic getDictionary() {
        return this.dic;
    }


    /**
     * @param dic
     *            登録先辞書
     */
    public void setDictionary(ICoronaDic dic) {
        /* 違う場合はdirty=true */
        if (this.dic == null) {
            if (dic == null) {
                return;
            }
        } else {
            if (dic != null) {
                if (this.dic.equals(dic)) {
                    return;
                }
            }
        }
        this.dirty = true;
        this.dic = dic;
    }


    boolean isDicDirty() {
        return this.dirty;
    }


    void clearDicDirty() {
        this.dirty = false;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FrequentEditData)) {
            return false;
        }

        FrequentEditData f2 = (FrequentEditData) obj;
        return term.equals(f2.term);
    }
}
