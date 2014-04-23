/**
 * @version $Id: AbstractDicFactory.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/19 13:41:55
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.abstraction;

import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.io.model.ITextItem;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicFactory;
import com.tida_okinawa.corona.io.model.dic.IFluc;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.ISynonym;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.impl.FlucItem;
import com.tida_okinawa.corona.io.model.dic.impl.LabelItem;
import com.tida_okinawa.corona.io.model.dic.impl.PatternItem;
import com.tida_okinawa.corona.io.model.dic.impl.SynonymItem;
import com.tida_okinawa.corona.io.model.dic.impl.Term;

/**
 * @author shingo-takahashi
 */
public abstract class AbstractDicFactory implements IDicFactory {

    /* 生成辞書リスト */
    protected List<ICoronaDic> dics = new ArrayList<ICoronaDic>();


    /**
     * 生成辞書リスト取得
     * 
     * @return dics
     */
    public List<ICoronaDic> getDics() {
        return dics;
    }


    @Override
    public ITerm createTerm(String value, String reading, String termPart, String termClass, String cform, String jumanBase) {
        return new Term(value, reading, termPart, termClass, cform, jumanBase);
    }


    @Override
    public IFluc createFluc(ITerm main) {
        return new FlucItem(main);
    }


    @Override
    public ISynonym createSynonym(ITerm main) {
        return new SynonymItem(main);
    }


    @Override
    public ILabel createLabel(String name, ILabel parent) {
        return new LabelItem(name, parent);
    }


    @Override
    public IPattern createPattern(String name, String text, int patternType, boolean parts) {
        return new PatternItem(name, text, patternType, parts);
    }


    @Override
    public ITextItem createTextItem(String data) {
        return new TextItem(-1, data);
    }
}
