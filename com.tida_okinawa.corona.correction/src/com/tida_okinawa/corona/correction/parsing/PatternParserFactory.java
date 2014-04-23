/**
 * @version $Id: PatternParserFactory.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/05 9:42:20
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing;

import java.util.List;

import com.tida_okinawa.corona.correction.parsing.impl.CoronaPatternParser;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * @author shingo-takahashi
 */
public class PatternParserFactory {

    private static PatternParserFactory _instance = new PatternParserFactory();


    private PatternParserFactory() {
        /* このクラスはシングルトン */
    }


    /**
     * PatternParserFactoryのインスタンスを取得する(シングルトン)
     * 
     * @return PatternParserFactoryのインスタンス
     */
    public static PatternParserFactory getInstance() {
        return _instance;
    }


    /**
     * @param dics
     * @return
     */
    public ICoronaPatternParser createPatternParser(List<ICoronaDic> dics) {
        return new CoronaPatternParser(dics);
    }


    /**
     * 
     * @param project
     *            プロジェクト （辞書を使わない場合は null)
     * @param product
     *            プロダクト （辞書を使わない場合は null)
     * @return
     */
    public ICoronaPatternParser createPatternParser(ICoronaProject project, ICoronaProduct product) {
        return new CoronaPatternParser(project, product);
    }
}
