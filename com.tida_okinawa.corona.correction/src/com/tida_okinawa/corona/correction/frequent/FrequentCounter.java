/**
 * @version $Id: FrequentCounter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/08 14:41:52
 * @author kenta-uechi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.frequent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * 
 * 頻出数を集計する
 * 
 * @author kenta-uechi, imai
 */
public class FrequentCounter {

    /**
     * 集計結果
     * 
     * 品詞情報=>カウント
     */
    TreeMap<Term, FrequentRecord> tm = new TreeMap<Term, FrequentRecord>();


    /**
     * 集計結果を取得する
     * 
     * @return 単語とその集計結果
     */
    public TreeMap<Term, FrequentRecord> getTreeMap() {
        return tm;
    }


    /**
     * 品詞情報をカウント
     * 
     * @param me
     *            Jumanの結果のテキストの１行分
     */
    public void count(MorphemeElement me) {
        Term term = Term.createTerm(me);
        /* 分割した文字列が８つ以上の場合、頻出チェック処理 */
        count(term);
    }


    /**
     * 形態素情報をカウント
     * 
     * @param term
     *            カウント対象の単語
     */
    synchronized void count(Term term) {
        /* 頻出語に該当した場合、カウント処理 */
        FrequentRecord record = tm.get(term);

        if (hinshiCheck(term)) {
            if (record == null) {
                record = new FrequentRecord(term);
                tm.put(term, record);
            }
            record.count++;
        }
    }


    /**
     * @return 頻出用語カウント結果
     */
    public Collection<FrequentRecord> getRecords() {
        return tm.values();
    }

    /**
     * 頻出語の集計対象にする品詞の一覧
     */
    static final List<String> Hinshis = Arrays.asList(new String[] { Messages.FrequentCounter_VERB, Messages.FrequentCounter_ADJECTIVE,
            Messages.FrequentCounter_NOUN, Messages.FrequentCounter_UNDEFINED_WORD });


    /**
     * 単語がカウント対象の品詞かどうか判定する。
     * 
     * @param term
     *            検査対象の単語
     * @return カウント対象ならtrue
     */
    public static boolean hinshiCheck(Term term) {
        /* 単語が頻出語に該当するかチェック */
        return Hinshis.contains(term.getHinshi());
    }
}
