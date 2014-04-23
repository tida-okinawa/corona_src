/**
 * @file ClaimWorkPatternRecordMap.java
 * @version $Id$
 * 
 * 2013/10/29 11:18:37
 * @author hajime-uchihara
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.tida_okinawa.corona.common.Pair;
import com.tida_okinawa.corona.io.dam.hibernate.connector.impl.ClaimWorkPatternDaoConnector;
import com.tida_okinawa.corona.io.model.IClaimWorkDataRecordList;
import com.tida_okinawa.corona.io.model.IClaimWorkPatternRecordMap;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.table.ITextRecord;

/**
 * @author hajime-uchihara
 * 
 */
public class ClaimWorkPatternRecordMap implements IClaimWorkPatternRecordMap {
    /**
     * TODO 説明を書かねば
     * recordId -> {@link IResultCoronaPattern}
     */
    Map<Integer, IResultCoronaPattern> records;

    /**
     * 構文パターン結果のDB
     */
    public final ClaimWorkPatternDaoConnector connector;

    /**
     * 構文パターン辞書
     */
    IPatternDic[] pdics = null;

    /**
     * 原文のレコード
     */
    final IClaimWorkDataRecordList datas;


    /**
     * TODO 説明を書かねば
     * 
     * @param connector
     *            ClaimWorkPatternDaoConnector
     * @param datas
     *            問い合わせデータリスト
     * @param pdics
     *            パターン辞書リスト
     */
    public ClaimWorkPatternRecordMap(ClaimWorkPatternDaoConnector connector, IClaimWorkDataRecordList datas, IPatternDic[] pdics) {
        this.connector = connector;
        this.datas = datas;
        this.pdics = pdics;
    }


    /**
     * recordsのリセット（例外エラー発生時）
     */
    void reset() {
        /* メモリにキャッシュされているかの判定にも使用しているrecordsをリセット */
        records = null;
    }


    /**
     * 初期化
     */
    void init() {
        records = new TreeMap<Integer, IResultCoronaPattern>();

        try {
            Map<Pair<Integer, Integer>, Map<IPattern, List<String>>> hitInfos = connector.get();
            for (Iterator<Entry<Pair<Integer, Integer>, Map<IPattern, List<String>>>> i = hitInfos.entrySet().iterator(); i.hasNext();) {
                Entry<Pair<Integer, Integer>, Map<IPattern, List<String>>> entry = i.next();
                Pair<Integer, Integer> pair = entry.getKey();
                Map<IPattern, List<String>> patternResult = entry.getValue();
                records.put(pair.getValue1(), new ResultCoronaPattern(pair.getValue1(), patternResult, datas.get(pair.getValue1())));
            }
        } catch (Exception e) {
            // この処理によりメモリ上でもデータなしの状態となる為、init()が再度実行される。
            reset();
            throw e;
        }
        return;
    }


    @Override
    public IPattern[] getPatterns() {
        if (patternMap == null) {
            initPatternMap();
        }
        Collection<IPattern> patterns = patternMap.values();
        return patterns.toArray(new IPattern[patterns.size()]);
    }


    @Override
    public void clear() {
        if (records != null)
            records.clear();
        if (connector != null) {
            connector.clear();
        }
        if (patternMap != null) {
            patternMap.clear();
        }
    }


    @Override
    public boolean containsKey(Object key) {
        return records.containsKey(key);
    }


    @Deprecated
    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Set<java.util.Map.Entry<Integer, IResultCoronaPattern>> entrySet() {
        if (records == null) {
            init();
        }
        return records.entrySet();
    }


    @Override
    public IResultCoronaPattern get(Object key) {
        if (records == null) {
            init();
        }
        return records.get(key);
    }


    @Override
    public boolean isEmpty() {
        if (records == null) {
            init();
        }
        return records.isEmpty();
    }


    @Override
    public Set<Integer> keySet() {
        return records.keySet();
    }


    @Override
    public IResultCoronaPattern put(Integer key, IResultCoronaPattern value) {
        if (records == null) {
            init();
        }
        // TODO 要テスト。#817
        connector.commit(key, value.getHitPositions(0));
        return records.put(key, value);
    }


    @Override
    public void putAll(Map<? extends Integer, ? extends IResultCoronaPattern> m) {
        for (Entry<? extends Integer, ? extends IResultCoronaPattern> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }


    @Override
    public IResultCoronaPattern remove(Object key) {
        return records.remove(key);
    }


    @Override
    public int size() {
        return records.size();
    }


    @Override
    public Collection<IResultCoronaPattern> values() {
        return records.values();
    }


    @Override
    public void add(int recordId, IResultCoronaPattern result) {
        put(recordId, result);
    }


    @Override
    public IResultCoronaPattern getRecord(int recordId) {
        return records.get(recordId);
    }


    @Override
    public void commit() {
        connector.close();
        /* 動的メモリにレコード読み直し */
        init();
    }


    /**
     * {@link IResultCoronaPattern} の実体
     * 
     */
    public class ResultCoronaPattern extends com.tida_okinawa.corona.io.model.ResultCoronaPattern {
        private ITextRecord result;


        ResultCoronaPattern(int recordId, Map<IPattern, List<String>> hitInfos, ITextRecord result) {
            /*
             * 構文解析を再実行した時、この時点でITextRecord#getTextしても元テキストデータが取得できないため、
             * 必要になった時にgetTextしている
             */
            super(recordId, hitInfos, null);
            this.result = result;
        }


        // TODO 要テスト。#817 datas ではなく、resultで取るようにした
        @Override
        public String getText() {
            resultText = result.getText();
            return super.getText();
        }


        // TODO 要テスト。#817 datas ではなく、resultで取るようにした
        @Override
        public String[] getLabels() {
            resultText = result.getText();
            return super.getLabels();
        }


        // TODO 要テスト。#817 datas ではなく、resultで取るようにした
        @Override
        public String getData() {
            return result.getText();
        }


        @Override
        public Map<IPattern, List<String>> getHitPositions(int history) {
            if (history == 0) {
                return connector.get(recordId);
            } else {
                return connector.get(recordId, history);
            }
        }
    }

    /**
     * パターン辞書
     */
    Map<Integer, IPattern> patternMap;


    /**
     * DBのパターン辞書からパターンを取得
     * クレームデータと比較して、パターンの量は少ないので全部読んでおく
     */
    void initPatternMap() {
        patternMap = new HashMap<Integer, IPattern>();
        for (IPatternDic pdic : pdics) {
            for (IDicItem item : pdic.getItems()) {
                IPattern pattern = (IPattern) item;
                patternMap.put(pattern.getId(), pattern);
            }
        }
    }


    /**
     * パターンを取得する
     * 
     * @param patternId
     *            パターンID
     * @return patternIdに一致するパターン
     */
    public IPattern getPattern(int patternId) {
        if (patternMap == null) {
            initPatternMap();
        }
        return patternMap.get(patternId);
    }


    /**
     * パターン辞書取得
     * 
     * @return パターン辞書
     */
    public IPatternDic[] getPatternDics() {
        if (pdics != null) {
            return pdics.clone();
        }
        return new IPatternDic[0];
    }


    /**
     * 取得したパターン辞書をセット
     * 
     * @param pdics
     *            パターン辞書リスト
     */
    public void setPatternDics(IPatternDic[] pdics) {
        this.pdics = pdics;
        initPatternMap();
    }


    /**
     * パターン辞書クリア
     */
    public void clearPatternDics() {
        pdics = null;
        if (patternMap != null) {
            patternMap.clear();
        }
    }
}
