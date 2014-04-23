/**
 * @version $Id: DicFactory.java 33 2013-10-28 11:30:35Z yukihiro-kinjo $
 *
 * 2013/10/28 11:30:00
 * @author yukihiro-kinjo
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.HashSet;
import java.util.Set;

import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicFactory;
import com.tida_okinawa.corona.io.model.dic.ITermCount;
import com.tida_okinawa.corona.io.model.dic.abstraction.AbstractDicFactory;
import com.tida_okinawa.corona.io.model.dic.impl.TermCount;

/**
 * 辞書オブジェクト・辞書アイテムオブジェクト生成クラス
 * 
 * @author yukihiro-kinjo
 */
public final class DicFactory extends AbstractDicFactory {
    private static DicFactory _instance = new DicFactory();


    /**
     * DicFactryのインスタンスを取得する
     */
    private DicFactory() {
        /* このクラスはシングルトン */
    }


    /**
     * 辞書オブジェクト・辞書アイテムオブジェクト生成クラスのインスタンスを取得する
     * 
     * @return iDicFactoryを実装したDicFactoryのインスタンス
     */
    public static IDicFactory getInstance() {
        return _instance;
    }


    @Override
    public ICoronaDic createDic(String name, DicType type) {
        switch (type) {
        case JUMAN:
            break;
        case COMMON:
        case CATEGORY:
        case SPECIAL:
            return createUserDic(name, "", type); //$NON-NLS-1$
        case LABEL:
            return createLabelDic(name);
        case SYNONYM:
            return createSynonymDic(name, new HashSet<Integer>());
        case FLUC:
            return createFlucDic(name, new HashSet<Integer>());
        case PATTERN:
            return createPatternDic(name);
        }
        return null;
    }


    @Override
    public ICoronaDic createUserDic(String name, String file, DicType type) {
        ICoronaDic dic = new UserDic(ICoronaDic.UNSAVED_ID, name, file, type, null);
        dics.add(dic);
        return dic;
    }


    @Override
    public ICoronaDic createFlucDic(String name, int parentId) {
        if (parentId < 1)
            return null;

        ICoronaDic dic = new FlucDic(ICoronaDic.UNSAVED_ID, name, parentId);
        dics.add(dic);
        return dic;
    }


    @Override
    public ICoronaDic createSynonymDic(String name, int parentId) {
        if (parentId < 1)
            return null;

        ICoronaDic dic = new SynonymDic(ICoronaDic.UNSAVED_ID, name, parentId);
        dics.add(dic);
        return dic;
    }


    @Override
    public ICoronaDic createLabelDic(String name) {
        ICoronaDic dic = new LabelDic(ICoronaDic.UNSAVED_ID, name, null);
        dics.add(dic);
        return dic;
    }


    @Override
    public ICoronaDic createPatternDic(String name) {
        ICoronaDic dic = new PatternDic(ICoronaDic.UNSAVED_ID, name, null);
        dics.add(dic);
        return dic;
    }


    @Override
    public ICoronaDic createFlucDic(String name, Set<Integer> parentId) {
        ICoronaDic dic = new FlucDic(ICoronaDic.UNSAVED_ID, name, null, parentId);
        dics.add(dic);
        return dic;
    }


    @Override
    public ICoronaDic createSynonymDic(String name, Set<Integer> parentId) {
        ICoronaDic dic = new SynonymDic(ICoronaDic.UNSAVED_ID, name, null, parentId);
        dics.add(dic);
        return dic;
    }


    @Override
    public ICoronaDic createLabelDic(String name, Set<Integer> parentId) {
        ICoronaDic dic = new LabelDic(ICoronaDic.UNSAVED_ID, name, null, parentId);
        dics.add(dic);
        return dic;
    }


    @Override
    public ITermCount createTermCount(String value, String reading, String termPart, String termClass, String cform, String jumanBase) {
        return new TermCount(value, reading, termPart, termClass, cform, jumanBase);
    }
}
