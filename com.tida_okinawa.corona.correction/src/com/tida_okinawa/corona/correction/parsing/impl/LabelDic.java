/**
 * @version $Id: LabelDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/14 02:10:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.impl;

import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.correction.common.ListMap;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;

/**
 * ラベルパターン照合用
 * 
 * @author imai
 * 
 */
public class LabelDic {
    /**
     * 原形　=> ITerm
     */
    ListMap<String, ITerm> termMap = new ListMap<String, ITerm>();

    /**
     * ITerm => ILabel
     */
    ListMap<ITerm, ILabel> labelMap = new ListMap<ITerm, ILabel>();


    /**
     * ラベル辞書を登録
     * 
     * @param dic
     */
    public void addDic(ILabelDic dic) {
        addDic(dic.getItems());
    }


    /**
     * ラベル辞書を登録
     * 
     * @memo 子ラベルを考慮
     * 
     * @param list
     */
    private void addDic(List<?> list) {
        for (Object item : list) {
            ILabel labelItem = (ILabel) item;
            // 用語チェック
            if (labelItem.getTerms() != null) {
                boolean hitTermMap = false;
                for (ITerm term : labelItem.getTerms()) {
                    // inactiveのチェック
                    if (term.isInActive()) {
                        continue;
                    }
                    List<ITerm> tlist = termMap.get(term.getValue());
                    if (tlist != null && tlist.size() > 0) {
                        for (ITerm t : tlist) {
                            // 同一の用語がすでに登録済み
                            if (t == term) {
                                hitTermMap = true;
                                break;
                            }
                        }
                    }

                    if (!hitTermMap) {
                        // 原形　=> ITerm
                        termMap.add(term.getValue(), term);
                    }
                    // ITerm => ILabel
                    labelMap.add(term, labelItem);
                }
            }
            // 子ラベルチェック
            if (labelItem.getChildren() != null) {
                addDic(labelItem.getChildren());
            }
        }
    }


    /**
     * 原形に該当するラベルの一覧
     * 
     * @param base
     * @return
     */
    List<ILabel> getLabels(String base) {
        List<ILabel> labels = new ArrayList<ILabel>();
        List<ITerm> terms = getTerms(base);
        if (terms != null) {
            for (ITerm term : terms) {
                labels.addAll(getLabels(term));
            }
        }
        return labels;
    }


    /**
     * 原形に該当する品詞の候補
     * 
     * @param base
     * @return null: 該当なし
     */
    List<ITerm> getTerms(String base) {
        return termMap.get(base);
    }


    /**
     * 品詞に該当するラベルの一覧
     * 
     * @param term
     * @return
     */
    List<ILabel> getLabels(ITerm term) {
        List<ILabel> allLabels = new ArrayList<ILabel>();
        List<ILabel> labels = labelMap.get(term);
        if (labels != null) {
            allLabels.addAll(labels);
        }

        return allLabels;
    }

}
