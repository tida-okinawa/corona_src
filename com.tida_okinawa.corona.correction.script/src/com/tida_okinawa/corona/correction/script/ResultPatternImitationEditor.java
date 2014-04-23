/**
 * @version $Id: ResultPatternImitationEditor.java 1148 2014-03-24 09:40:11Z yukihiro-kinjyo $
 *
 * 2014/03/24 09:40:11
 * @author yukihiro-kinjyo
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.table.ITextRecord;
import com.tida_okinawa.corona.ui.editors.IResultPatternEditorInput;

/**
 * @author yukihiro-kinjyo
 * 
 */
public class ResultPatternImitationEditor {
    final IClaimWorkPattern claimWorkPattern;

    int claimID;
    int fieldID;
    ClaimWorkDataType type;
    Date lasted;

    /**
     * パターンマッチング結果1
     * 
     * パターン区分 → パターンのリスト
     * 
     */
    Map<PatternType, TreeSet<IPattern>> results1 = new TreeMap<PatternType, TreeSet<IPattern>>(comparator_PatternType);

    /**
     * パターンマッチング結果
     * 
     * パターン → パターンに該当したテキストのリスト
     * 
     */
    TreeMap<IPattern, TreeSet<IResultCoronaPattern>> results2 = new TreeMap<IPattern, TreeSet<IResultCoronaPattern>>(comparator_IPattern);

    /**
     * 該当なし結果
     * 
     * 全てのパターンで該当なしになったテキストのリスト
     * 
     */
    Set<Integer> patternNAList = new HashSet<Integer>();

    /**
     * この解析結果で扱うパターン分類
     */
    private Map<Integer, PatternType> patternTypes = new HashMap<Integer, PatternType>();


    /**
     * @param claimWorkPattern
     *            構文解析結果中間データ
     * @param resultNomatch
     *            該当なしを出力する場合true
     */
    public ResultPatternImitationEditor(final IClaimWorkPattern claimWorkPattern, boolean resultNomatch) {
        this.claimWorkPattern = claimWorkPattern;

        claimID = claimWorkPattern.getClaimId();
        fieldID = claimWorkPattern.getFieldId();
        type = claimWorkPattern.getClaimWorkDataType();
        lasted = claimWorkPattern.getLasted();

        // 該当しない処理 用のマップ
        // パターン区分 => {パターン区分のどのパターンにも該当しないレコード}
        HashMap<PatternType, Set<Integer>> naRecords = new HashMap<PatternType, Set<Integer>>();

        PatternType[] tmp = claimWorkPattern.getPatternTypes();
        for (PatternType type : tmp) {
            patternTypes.put(type.getId(), type);
        }
        /* #1087 パターン分類が「（デフォルト）その他」のみのとき、該当なしが0件になる問題に対応 */
        patternTypes.put(PatternType.OTHER.getId(), PatternType.OTHER);

        /* 該当なし処理 -- (1) 各パターン分類ごとに全データのIDを用意する */
        for (ITextRecord record : claimWorkPattern.getClaimWorkDatas()) {
            patternNAList.add(record.getId());
        }
        for (Entry<Integer, PatternType> e : patternTypes.entrySet()) {
            Set<Integer> records = new HashSet<Integer>(patternNAList);
            naRecords.put(e.getValue(), records);
        }

        /* ツリー表示のためのマップを作成 */
        List<IResultCoronaPattern> records = claimWorkPattern.getClaimWorkPatterns();
        for (IResultCoronaPattern record : records) {
            addRecord(record);
            /* 該当なし処理 -- (2) ヒットしたレコードを除去する */
            for (Entry<IPattern, List<String>> e : record.getHitPositions(0).entrySet()) {
                PatternType type = patternTypes.get(e.getKey().getPatternType());
                if (type != null) {
                    naRecords.get(type).remove(record.getRecordId());
                    // 該当したクレームデータのIDを除去
                    patternNAList.remove(record.getRecordId());
                }
            }
        }

        // 該当なし処理　(3) パターンを作る
        // マップ作成時は、パターン区分がどれだけあるかわからないので、ここで作成
        if (resultNomatch) {
            for (Entry<Integer, PatternType> e : patternTypes.entrySet()) {
                PatternType type = e.getValue();
                PatternNA patternNA = new PatternNA(type);
                TreeSet<IPattern> patterns = results1.get(type);
                if (patterns == null) {
                    patterns = new TreeSet<IPattern>(comparator_IPattern);
                    results1.put(type, patterns);
                }
                patterns.add(patternNA);
                TreeSet<IResultCoronaPattern> set = new TreeSet<IResultCoronaPattern>(compartor_IResultCoronaPattern);
                results2.put(patternNA, set);
                for (final int rid : naRecords.get(type)) {
                    set.add(new IResultCoronaPattern() {

                        @Override
                        public String getText() {
                            String inputText = claimWorkPattern.getClaimWorkData(rid);
                            int end = inputText.indexOf("%&%&%&");
                            if (end > 0) {
                                return inputText.substring(0, end);
                            }
                            return inputText;
                        }


                        @Override
                        public String[] getLabels() {
                            return new String[0];
                        }


                        @Override
                        public int getRecordId() {
                            return rid;
                        }


                        @Override
                        public String getData() {
                            return null;
                        }


                        @Override
                        public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
                            if (adapter.equals(IPropertySource.class)) {
                                return source;
                            }
                            return null;
                        }

                        private IPropertySource source = new IPropertySource() {
                            @Override
                            public IPropertyDescriptor[] getPropertyDescriptors() {
                                IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("id", "ID"),
                                        new TextPropertyDescriptor("text", "テキスト"), new TextPropertyDescriptor("label", "パターン名"), };
                                return descriptor;
                            }


                            @Override
                            public Object getPropertyValue(Object id) {
                                if (id.equals("id")) {
                                    return String.valueOf(getRecordId());
                                }
                                if (id.equals("text")) {
                                    return getText();
                                }
                                if (id.equals("label")) {
                                    return getLabels();
                                }
                                return null;
                            }


                            @Override
                            public boolean isPropertySet(Object id) {
                                return false;
                            }


                            @Override
                            public void resetPropertyValue(Object id) {
                            }


                            @Override
                            public void setPropertyValue(Object id, Object value) {
                            }


                            @Override
                            public Object getEditableValue() {
                                return null;
                            }
                        };


                        @Override
                        public Map<IPattern, List<String>> getHitPositions(int history) {
                            return new HashMap<IPattern, List<String>>(0);
                        }
                    });
                }
            }
            // 全てのパターンに該当がない場合のデータの情報を編集
            PatternType type = new PatternType(-999, "該当なし");
            TreeSet<IPattern> patterns = new TreeSet<IPattern>(comparator_IPattern);
            results1.put(type, patterns);
            PatternNA patternNA = new PatternNA(type);
            patterns.add(patternNA);
            TreeSet<IResultCoronaPattern> set = getNAPatterns();
            results2.put(patternNA, set);
        }
    }


    public IClaimWorkPattern getClaimWorkPattern() {
        return claimWorkPattern;
    }


    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (!(obj instanceof ResultPatternImitationEditor)) {
            return false;
        }

        IResultPatternEditorInput input = (IResultPatternEditorInput) obj;
        // testH25 20131002
        /* lasted.equals()実行時でのnull例外への対策 */
        if (input.getClaimWorkPattern().getLasted() == null || lasted == null) {
            return false;
        }
        // testH25 20131002
        if (claimID == input.getClaimWorkPattern().getClaimId() && fieldID == input.getClaimWorkPattern().getFieldId()
                && type == input.getClaimWorkPattern().getClaimWorkDataType() && lasted.equals(input.getClaimWorkPattern().getLasted())) {
            return true;
        }

        return false;
    }


    /**
     * ContentProvider用
     * 
     * @return PatternType型
     */
    public PatternType[] getElements() {
        return results1.keySet().toArray(new PatternType[results1.size()]);
    }


    /**
     * ContentProvider用
     * 
     * @param pattern
     *            パターン辞書アイテム
     * @return パターンにヒットしたレコードの情報
     */
    public IResultCoronaPattern[] getChildren(IPattern pattern) {
        final TreeSet<IResultCoronaPattern> texts = results2.get(pattern);
        return texts.toArray(new IResultCoronaPattern[texts.size()]);
    }


    /**
     * ContentProvider用
     * 
     * @param patternType
     *            パターン分類
     * 
     * @return
     */
    public IPattern[] getChildren(PatternType patternType) {
        TreeSet<IPattern> patterns = results1.get(patternType);
        /* パターンをHit件数の降順でソート。かつ、「該当なし」は一番下。 */
        IPattern[] obj = patterns.toArray(new IPattern[patterns.size()]);
        Arrays.sort(obj, new Comparator<IPattern>() {
            @Override
            public int compare(IPattern p1, IPattern p2) {
                int i1 = getChildren(p1).length;
                int i2 = getChildren(p2).length;
                if ("該当なし".equals(p2.getLabel())) {
                    return -1;
                } else {
                    return i2 - i1;
                }
            }
        });
        return obj;
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

    /**
     * 該当なし（どのパターンにも該当しなかった）のパターン
     */
    static class PatternNA implements IPattern {
        /**
         * パターン区分
         */
        PatternType type;

        int id;

        final static int NA_ID = 0x80000000;


        PatternNA(PatternType type) {
            this.type = type;
            this.id = NA_ID + type.getId();
        }


        @Override
        public void setId(int id) {
        }


        @Override
        public int getId() {
            return id;
        }


        @Override
        public int getComprehensionDicId() {
            return 0;
        }


        @Override
        public boolean isDirty() {
            return false;
        }


        @Override
        public void setDirty(boolean dirty) {
        }


        @Override
        public boolean isInActive() {
            return false;
        }


        @Override
        public void setPatternType(int patternType) {
            //
        }


        @Override
        public int getPatternType() {
            return type.getId();
        }


        @Override
        public void setLabel(String text) {
            //
        }


        @Override
        public String getLabel() {
            if ("該当なし".equals(type.getPatternName())) {
                return "パターンなし";
            } else {
                return "該当なし";
            }
        }


        @Override
        public void setText(String text) {
        }


        @Override
        public String getText() {
            return "該当なし";
        }


        @Override
        public void setParts(boolean parts) {
        }


        @Override
        public boolean isParts() {
            return false;
        }


        /*
         * プロパティビュー対応でICoronaDicにIPropertySourceをextendsしたので、
         * 空のメソッド群を追加
         */
        @Override
        public Object getEditableValue() {
            return null;
        }


        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("label", "パターン名"), };
            return descriptor;
        }


        @Override
        public Object getPropertyValue(Object id) {
            return getLabel();
        }


        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }


        @Override
        public void resetPropertyValue(Object id) {
        }


        @Override
        public void setPropertyValue(Object id, Object value) {
        }
    }


    void addRecord(IResultCoronaPattern record) {
        if (record == null || record.getHitPositions(0) == null) {
            return;
        }
        Map<IPattern, List<String>> patterns1 = record.getHitPositions(0);
        for (Entry<IPattern, List<String>> e : patterns1.entrySet()) {
            IPattern pattern = e.getKey();
            if (pattern == null) {
                // 処理後に削除されたパターンの場合ここにくる
                System.err.println("rec#" + record.getRecordId() + " pattern has null");
                continue;
            }
            TreeSet<IResultCoronaPattern> records = results2.get(pattern);
            if (records == null) {
                records = new TreeSet<IResultCoronaPattern>(compartor_IResultCoronaPattern);
                results2.put(pattern, records);
            }
            records.add(record);

            PatternType patternType = PatternType.getPatternType(pattern.getPatternType());
            if (patternType == null) {
                patternType = PatternType.OTHER;
            }
            TreeSet<IPattern> patterns = results1.get(patternType);
            if (patterns == null) {
                patterns = new TreeSet<IPattern>(comparator_IPattern);
                results1.put(patternType, patterns);
            }
            patterns.add(pattern);
        }
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
            long longResult = (long) o1.getId() - (long) o2.getId();
            return Long.signum(longResult);
        }
    };

    final static Comparator<IResultCoronaPattern> compartor_IResultCoronaPattern = new Comparator<IResultCoronaPattern>() {
        @Override
        public int compare(IResultCoronaPattern o1, IResultCoronaPattern o2) {
            return o1.getRecordId() - o2.getRecordId();
        }
    };


    /**
     * 全パターンで該当しなかったデータのテキストを返す
     * 
     * @return
     */
    public TreeSet<IResultCoronaPattern> getNAPatterns() {
        TreeSet<IResultCoronaPattern> set = new TreeSet<IResultCoronaPattern>(compartor_IResultCoronaPattern);
        for (final int rid : patternNAList) {
            set.add(new IResultCoronaPattern() {
                @Override
                public String getText() {
                    String inputText = claimWorkPattern.getClaimWorkData(rid);
                    int end = inputText.indexOf("%&%&%&");
                    if (end > 0) {
                        return inputText.substring(0, end);
                    }
                    return inputText;
                }


                @Override
                public String[] getLabels() {
                    return new String[0];
                }


                @Override
                public int getRecordId() {
                    return rid;
                }


                @Override
                public String getData() {
                    return null;
                }


                @Override
                public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
                    if (adapter.equals(IPropertySource.class)) {
                        return source;
                    }
                    return null;
                }

                private IPropertySource source = new IPropertySource() {
                    @Override
                    public IPropertyDescriptor[] getPropertyDescriptors() {
                        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("id", "ID"),
                                new TextPropertyDescriptor("text", "テキスト"), new TextPropertyDescriptor("label", "パターン名"), };
                        return descriptor;
                    }


                    @Override
                    public Object getPropertyValue(Object id) {
                        if (id.equals("id")) {
                            return getRecordId();
                        }
                        if (id.equals("text")) {
                            return getText();
                        }
                        if (id.equals("label")) {
                            return getLabels();
                        }
                        return null;
                    }


                    @Override
                    public boolean isPropertySet(Object id) {
                        return false;
                    }


                    @Override
                    public void resetPropertyValue(Object id) {
                    }


                    @Override
                    public void setPropertyValue(Object id, Object value) {
                    }


                    @Override
                    public Object getEditableValue() {
                        return null;
                    }

                };


                @Override
                public Map<IPattern, List<String>> getHitPositions(int history) {
                    return new HashMap<IPattern, List<String>>(0);
                }
            });
        }
        return set;
    }


    public int getClaimId() {
        return this.claimID;
    }


    public int getFieldId() {
        return this.fieldID;
    }
}
