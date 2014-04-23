/**
 * @version $Id: ResultPatternEditorInput2.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/09/02 21:49:11
 * @author imai
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.ui.PreferenceInitializer;
import com.tida_okinawa.corona.ui.UIActivator;

public class ResultPatternEditorInput2 implements IEditorInput, IResultPatternEditorInput {
    final String name;
    final IClaimWorkPattern claimWorkPattern;
    final boolean resultNomatch;

    int claimID;
    int fieldID;
    ClaimWorkDataType type;
    Date lasted;

    private Object[] results;

    /**
     * この解析結果で扱うパターン分類
     */
    private Map<Integer, PatternType> patternTypes = new HashMap<Integer, PatternType>();


    public ResultPatternEditorInput2(String name, final IClaimWorkPattern claimWorkPattern) {
        resultNomatch = UIActivator.getDefault().getPreferenceStore().getBoolean(PreferenceInitializer.PREF_RESULT_NOMATCH);
        this.name = name;
        this.claimWorkPattern = claimWorkPattern;
        claimID = claimWorkPattern.getClaimId();
        fieldID = claimWorkPattern.getFieldId();
        type = claimWorkPattern.getClaimWorkDataType();
        lasted = claimWorkPattern.getLasted();
        if (!resultNomatch) {
            results = claimWorkPattern.getClaimWorkPatterns().toArray();
        } else {
            List<Object> lst = new ArrayList<Object>();
            IClaimData claim = IoActivator.getService().getClaimData(claimWorkPattern.getClaimId());
            String product = IoActivator.getService().getProductName(claimWorkPattern.getProductId());
            for (IRecord rec : claim.getRecords()) {
                // 対象ターゲットのみ
                if (!rec.getField(claim.getProductField()).getValue().equals(product)) {
                    continue;
                }
                try {
                    IResultCoronaPattern result = claimWorkPattern.getClaimWorkPattern(rec.getRecordId());

                    if (result != null) {
                        lst.add(result);
                    } else {
                        lst.add(rec);
                    }
                } catch (IndexOutOfBoundsException e) {// 範囲外もあり
                    // TODO ここ何もなくていいの？？？
                }
            }
            results = lst.toArray();
        }
    }


    @Override
    public IClaimWorkPattern getClaimWorkPattern() {
        return claimWorkPattern;
    }


    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (!(obj instanceof ResultPatternEditorInput2)) {
            return false;
        }

        ResultPatternEditorInput2 input = (ResultPatternEditorInput2) obj;
        if (claimID == input.getClaimWorkPattern().getClaimId() && fieldID == input.getClaimWorkPattern().getFieldId()
                && type == input.getClaimWorkPattern().getClaimWorkDataType() && lasted.equals(input.getClaimWorkPattern().getLasted())) {
            return true;
        }

        return false;
    }


    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }


    @Override
    public boolean exists() {
        return false;
    }


    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public IPersistableElement getPersistable() {
        return null;
    }


    @Override
    public String getToolTipText() {
        return "";
    }


    /**
     * ContentProvider用
     * 
     * @return
     */
    public Object[] getElements() {
        return Arrays.copyOf(results, results.length);
    }


    /**
     * ContentProvider用
     * 
     * @return
     */
    public Object[] getChildren(IResultCoronaPattern result) {
        // TODO 要テスト。#817
        return result.getHitPositions(0).keySet().toArray();
    }


    /**
     * ContentProvider用
     * 
     * @return
     */
    public Object[] getChildren(IPattern pattern) {
        return new Object[] { PatternType.getPatternType(pattern.getPatternType()) };
    }


    /**
     * ContentProvider用<br />
     * IPatternの親を返す
     * 
     * @param pattern
     * @return
     */
    public PatternType getParent(IPattern pattern) {
        PatternType type = patternTypes.get(pattern.getPatternType());
        return type;
    }

    final static Comparator<PatternType> comparator_PatternType = new Comparator<PatternType>() {
        @Override
        public int compare(PatternType o1, PatternType o2) {
            /* パターン分類の辞書順ソート(大文字小文字区別なし)。「その他」「該当なし」は一番下。 */
            int id1 = o1.getId();
            int id2 = o2.getId();
            if ((id1 > 0) && (id2 > 0)) {
                String s1 = o1.getPatternName();
                String s2 = o2.getPatternName();
                int ret = s1.compareToIgnoreCase(s2);
                return ret;
            } else {
                return id2 - id1;
            }
        }
    };

    final static Comparator<IPattern> comparator_IPattern = new Comparator<IPattern>() {
        @Override
        public int compare(IPattern o1, IPattern o2) {
            if ((o1.getPatternType() > 0) && (o2.getPatternType() > 0)) {
                String s1 = PatternType.getPatternType(o1.getPatternType()).getPatternName();
                String s2 = PatternType.getPatternType(o2.getPatternType()).getPatternName();
                int ret = s1.compareToIgnoreCase(s2);
                return ret;
            }
            return o1.getId() - o2.getId();
        }
    };

    final static Comparator<IResultCoronaPattern> compartor_IResultCoronaPattern = new Comparator<IResultCoronaPattern>() {
        @Override
        public int compare(IResultCoronaPattern o1, IResultCoronaPattern o2) {
            return o1.getRecordId() - o2.getRecordId();
        }
    };


    @Override
    public int getClaimId() {
        return this.claimID;
    }


    @Override
    public int getFieldId() {
        return this.fieldID;
    }
}
