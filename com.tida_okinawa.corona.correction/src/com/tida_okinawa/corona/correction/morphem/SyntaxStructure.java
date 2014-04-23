/**
 * @version $Id: SyntaxStructure.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/02 20:34:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.correction.common.StringUtil;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.dic.TermClass;

/**
 * 形態素・係り受け解析結果
 * 
 * @author imai
 * 
 */
public class SyntaxStructure extends ArrayList<ISyntaxStructureElement> {

    /**
     * オブジェクトのシリアライズ用ID
     */
    private static final long serialVersionUID = -1797704075541229412L;

    /**
     * Juman-KNP の出力のまま(デバッグ用)
     */
    final List<String> original;

    /**
     * 係り先
     */
    private final Map<Integer, Relation> destsMap = new HashMap<Integer, Relation>();

    /**
     * 係り元
     */
    private final Map<Integer, List<Relation>> sourcesMap = new HashMap<Integer, List<Relation>>();

    /**
     * １つのテキストを、句読点で区切ってknpにかけるのに対応
     */
    int index0 = 0;
    int indexSentence = 0;


    /**
     * 句/文節を作る
     * 
     * @param text
     *            KNPの結果のテキスト ex. *1 <文頭> ...
     * @return 文節/句
     */
    private ISyntaxStructureElement createElement(String text) {
        int index = size();
        ISyntaxStructureElement newElement = new SyntaxStructureElement(index, text);
        super.add(newElement);

        return newElement;
    }


    /**
     * コンストラクタ
     * 
     * @param text
     *            KNPの解析結果
     */
    public SyntaxStructure(String text) {
        this(StringUtil.splitFast(text));
        // note: DBには 解析結果(List<String>)を連結したものを入れている
    }


    /**
     * コンストラクタ
     * 
     * @param texts
     *            KNPの解析結果
     */
    public SyntaxStructure(List<String> texts) {
        this(texts, "+"); //$NON-NLS-1$
    }


    /**
     * 形態素のリストを取得
     * 
     * @return 形態素
     */
    public List<MorphemeElement> getMorphemeElemsnts() {
        List<MorphemeElement> list = new ArrayList<MorphemeElement>();
        for (ISyntaxStructureElement element : this) {
            list.addAll(element.getMorphemes());
        }
        return list;
    }


    /**
     * 形態素→文節/句
     * 
     * @param me
     * @return 文節/句
     */
    public ISyntaxStructureElement getSyntaxStructureElement(MorphemeElement me) {
        for (ISyntaxStructureElement element : this) {
            if (element.getMorphemes().contains(me)) {
                return element;
            }
        }
        // never
        return null;
    }


    /**
     * コンストラクタ
     * 
     * @param texts
     *            KNPの解析結果
     * @param type
     *            対象 "*" or "+"
     */
    private SyntaxStructure(List<String> texts, String type) {
        original = texts;

        /**
         * 処理中の句/文節
         */
        ISyntaxStructureElement lastElement = null;

        for (String text : texts) {
            if (text.startsWith("#")) { //$NON-NLS-1$
                // コメント行
            } else if (text.startsWith("@")) { //$NON-NLS-1$
                // Juman 次点候補 （無視する）
            } else if (text.startsWith(type)) {
                lastElement = createElement(text);
                Relation relation = createRelation(lastElement, text);
                if (relation.getDest() >= 0) { // 係り先がなければ -1
                    destsMap.put(lastElement.getIndex(), relation);
                    List<Relation> srcs = sourcesMap.get(relation.getDest());
                    if (srcs == null) {
                        srcs = new ArrayList<Relation>();
                        sourcesMap.put(relation.getDest(), srcs);
                    }
                    srcs.add(relation);
                }
            } else if (text.startsWith("*") || text.startsWith("+") || text.trim().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                // 対象外の行, 空白行
                // type ("*" or "+") は先に処理される
            } else if (text.equals("EOS")) { //$NON-NLS-1$
                // 次の文の文節の開始番号
                index0 = size();
            } else {
                // 形態素
                if (lastElement == null) {
                    lastElement = createElement(""); //$NON-NLS-1$
                    // 形態素だけを処理した場合
                }
                MorphemeElement morpheme = new MorphemeElement(text);
                lastElement.getMorphemes().add(morpheme);
                morpheme.indexSentence = indexSentence;
                if (morpheme.getHinshiSaibunrui().equals(TermClass.PERIOD.getName())) {
                    indexSentence++; // 句点で分割
                }
            }
        }
    }


    /**
     * {@link Relation}を作る
     * 
     * @param srcElement
     * @param text
     * @return 係り受けリレーション
     */
    private Relation createRelation(ISyntaxStructureElement srcElement, String text) {
        String s = text.substring(1).trim(); // #, * を除く
        // 10D の D を除く
        for (int pos = 0; pos < s.length(); pos++) {
            char c = s.charAt(pos);
            if ((c != '-') && (c < '0' || '9' < c)) {
                int dst = Integer.parseInt(s.substring(0, pos));
                if (dst > 0) { // dst==-1 : 係り先なし
                    dst += index0; // 前の文の番号だけずらす
                }
                String type = s.substring(pos, pos + 1);
                return new Relation(srcElement.getIndex(), dst, type);
            }
        }
        throw new IllegalArgumentException(text);
    }


    /**
     * 表記をつなげて原文を取得
     * 
     * @return 原文
     */
    public String getText() {
        StringBuilder text = new StringBuilder(3000);
        for (ISyntaxStructureElement element : this) {
            text.append(element.getHyouki());
        }
        return text.toString();
    }


    @Override
    public String toString() {
        // DB格納用の文字列を作る
        // EOS はなくなるが、構文パターンに影響ないのでそのまま
        StringBuilder sb = new StringBuilder(1000000); // 100KB
        for (ISyntaxStructureElement element : this) {
            sb.append(element.toString());
        }
        return sb.toString();
    }

    /**
     * 係り受け先/元
     * 
     */
    static public class Relation {
        int src;
        int dest;
        String type;


        Relation(int src, int dest, String type) {
            this.src = src;
            this.dest = dest;
            this.type = type;
        }


        /**
         * 係先の文節/句のID
         * 
         * @return 係り元ID
         */
        public int getSrc() {
            return src;
        }


        /**
         * 係り元の文節/句のID
         * 
         * @return 係り先ID
         */
        public int getDest() {
            return dest;
        }


        /**
         * 係先のタイプ
         * "D", "P"
         * 
         * @return 係り先のタイプ
         */
        public String getType() {
            return type;
        }


        @Override
        public String toString() {
            return src + "-" + type + "->" + dest; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * 句/文節
     * 
     */
    class SyntaxStructureElement implements ISyntaxStructureElement {

        /**
         * この文節/句の番号(デバッグ用)
         */
        final int index;

        /**
         * KNPの結果のテキスト
         * ex. *1 <文頭> ...
         */
        final String source;

        /**
         * この文節/句を構成する形態素
         */
        final List<MorphemeElement> morphemes = new ArrayList<MorphemeElement>();


        /**
         * 
         * @param index
         * @param text
         *            KNPの結果のテキスト ex. *1 <文頭> ...
         */
        SyntaxStructureElement(int index, String text) {
            this.index = index;
            this.source = text;
        }


        @Override
        public int getIndex() {
            return index;
        }


        @Override
        public List<MorphemeElement> getMorphemes() {
            return morphemes;
        }


        @Override
        public ISyntaxStructureElement getDependDestination() {
            return getDependDestination(null);
        }


        @Override
        public List<ISyntaxStructureElement> getDependSources() {
            return getDependSources(null);
        }


        @Override
        public String getHyouki() {
            return getText(MorphemeElement.HYOUKI);
        }


        @Override
        public String getYomi() {
            return getText(MorphemeElement.YOMI);
        }


        /**
         * 文節/句を構成する形態素をつなげる
         * 
         * @param index
         * @return 形態素
         */
        private String getText(int index) {
            StringBuilder s = new StringBuilder(2000);
            for (MorphemeElement morpheme : morphemes) {
                s.append(morpheme.get(index));
            }
            return s.toString();
        }


        @Override
        public ISyntaxStructureElement getDependDestination(String type) {
            Relation relation = destsMap.get(index);
            if (relation != null) {
                if (type == null || type.equals(relation.getType())) {
                    return SyntaxStructure.this.get(relation.getDest());
                }
            }
            return null;
        }


        @Override
        public List<ISyntaxStructureElement> getDependSources(String type) {
            List<Relation> relations = sourcesMap.get(index);
            List<ISyntaxStructureElement> srcs = new ArrayList<ISyntaxStructureElement>();
            if (relations != null) {
                for (Relation relation : relations) {
                    if (type == null || type.equals(relation.getType())) {
                        srcs.add(SyntaxStructure.this.get(relation.getSrc()));
                    }
                }
            }
            return srcs;
        }


        @Override
        public String toString() {
            // DB格納用のテキストを作る
            StringBuilder sb = new StringBuilder(10000); // 10KB
            if (!source.isEmpty()) { // KNPをやっていない場合 source=""
                // インデックスの差し替え
                // TODO:係り受けタイプがこれ以外にあるかも？
                String str = ""; //$NON-NLS-1$
                int index = -1;
                if (this.getDependDestination("D") != null) { //$NON-NLS-1$
                    index = this.getDependDestination("D").getIndex(); //$NON-NLS-1$
                    str = "+ " + index + source.substring(source.indexOf("D"));// 未格 //$NON-NLS-1$ //$NON-NLS-2$
                } else if (this.getDependDestination("P") != null) { //$NON-NLS-1$
                    index = this.getDependDestination("P").getIndex(); //$NON-NLS-1$
                    str = "+ " + index + source.substring(source.indexOf("P"));// 連体 //$NON-NLS-1$ //$NON-NLS-2$
                } else if (this.getDependDestination("A") != null) { //$NON-NLS-1$
                    index = this.getDependDestination("A").getIndex(); //$NON-NLS-1$
                    str = "+ " + index + source.substring(source.indexOf("A"));// 同格連体 //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    str = source;
                }
                sb.append(str);
                sb.append("\n"); //$NON-NLS-1$
            }
            for (MorphemeElement element : getMorphemes()) {
                sb.append(element.toString());
                sb.append("\n"); //$NON-NLS-1$
            }
            return sb.toString();
        }


        @Override
        public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
            if (adapter.equals(IPropertySource.class)) {
                return source1;
            }
            return null;
        }

        private IPropertySource source1 = new IPropertySource() {
            @Override
            public IPropertyDescriptor[] getPropertyDescriptors() {
                IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] {
                        new TextPropertyDescriptor("id", "ID"), //$NON-NLS-1$ //$NON-NLS-2$
                        new TextPropertyDescriptor("text", Messages.SyntaxStructure_NOTATION), new TextPropertyDescriptor("src", Messages.MODIFICATION_DESTINATION), new TextPropertyDescriptor("dist", Messages.MODIFICATION_SOURCE), }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                return descriptor;
            }


            @Override
            public Object getPropertyValue(Object id) {
                if (id.equals("id")) { //$NON-NLS-1$
                    return String.valueOf(getIndex());
                }
                if (id.equals("text")) { //$NON-NLS-1$
                    return getHyouki();
                }
                if (id.equals("src")) { //$NON-NLS-1$
                    if (getDependSources() != null && !getDependSources().isEmpty()) {
                        StringBuilder buf = new StringBuilder();
                        for (ISyntaxStructureElement src : getDependSources()) {
                            buf.append(" , " + src.getIndex() + ":" + src.getHyouki()); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        return buf.toString().substring(3);
                    }
                    return ""; //$NON-NLS-1$
                }
                if (id.equals("dist")) { //$NON-NLS-1$
                    if (getDependDestination() != null) {
                        // return getDst();
                        return String.valueOf(getDependDestination().getIndex()) + ":" + getDependDestination().getHyouki(); //$NON-NLS-1$
                    }
                    return ""; //$NON-NLS-1$
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
    }
}
