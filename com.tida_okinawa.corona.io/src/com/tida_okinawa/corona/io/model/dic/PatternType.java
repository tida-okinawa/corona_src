/**
 * @version $Id: PatternType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 構文パターン分類オブジェクト
 * 
 * @author shingo-takahashi
 * 
 */
public class PatternType implements Comparable<PatternType> {

    static Map<Integer, PatternType> patternTypes = new HashMap<Integer, PatternType>();

    private int id;
    private String patternName;

    /** デフォルトパターン分類 */
    final static public PatternType OTHER = new PatternType(-99, "その他");

    /* スタティックイニシャライザ */
    static {
        addPatternType(OTHER);
    }


    /**
     * コンストラクター
     * 
     * @param id
     *            パターン分類ID
     * @param patternName
     *            パターン分類名
     */
    public PatternType(int id, String patternName) {
        this.id = id;
        this.patternName = patternName;
    }


    /**
     * IDで構文パターン分類を取得
     * 
     * @param id
     *            構文パターン分類ID
     * @return パターン分類オブジェクト
     */
    static public PatternType getPatternType(int id) {
        return patternTypes.get(id);
    }


    /**
     * 構文パターン分類の一覧を取得
     * 
     * @return 構文パターン分類オブジェクトの配列
     */
    static public PatternType[] getPatternTypes() {
        Collection<PatternType> values = patternTypes.values();
        return values.toArray(new PatternType[values.size()]);
    }


    /**
     * 構文パターン分類を追加する
     * 
     * @param type
     *            構文パターン分類オブジェクト
     */
    public static void addPatternType(PatternType type) {
        // TODO staticメソッドを作ったほうがいい(createPatternType(int, String)みたいな)
        patternTypes.put(type.id, type);
    }


    /**
     * 構文パターン分類を削除する
     * 
     * @param type
     *            削除する構文パターン分類オブジェクト
     * @return 成功した場合true
     */
    public static boolean removePatternType(PatternType type) {
        return (patternTypes.remove(type.getId()) != null);
    }


    /**
     * 構文パターン分類IDを取得する
     * 
     * @return 構文パターン分類ID
     */
    public int getId() {
        return id;
    }


    /**
     * 構文パターン分類IDを設定する
     * 
     * @param id
     *            構文パターン分類ID
     */
    public void setId(int id) {
        this.id = id;
    }


    /**
     * 構文パターン分類名を取得する
     * 
     * @return 構文パターン分類名文字列
     */
    public String getPatternName() {
        return patternName;
    }


    /**
     * 構文パターン分類名を設定する
     * 
     * @param patternName
     *            構文パターン分類名文字列
     */
    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }


    @Override
    public boolean equals(Object o) {
        return (o instanceof PatternType) && ((PatternType) o).id == id;
    }


    @Override
    public int hashCode() {
        return this.id;
    }


    @Override
    public String toString() {
        return patternName;
    }


    @Override
    public int compareTo(PatternType o) {
        if (o == null)
            return -1;
        return getId() - o.getId();
    }
}
