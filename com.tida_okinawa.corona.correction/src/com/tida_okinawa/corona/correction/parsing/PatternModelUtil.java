/**
 * @version $Id: PatternModelUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/16 17:53:03
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.tida_okinawa.corona.correction.parsing.model.AndOperator;
import com.tida_okinawa.corona.correction.parsing.model.IScopePattern;
import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.correction.parsing.model.Modification;
import com.tida_okinawa.corona.correction.parsing.model.ModificationElement;
import com.tida_okinawa.corona.correction.parsing.model.NotOperator;
import com.tida_okinawa.corona.correction.parsing.model.OrOperator;
import com.tida_okinawa.corona.correction.parsing.model.Order;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.ScopePattern;
import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;
import com.tida_okinawa.corona.correction.parsing.model.Sequence;
import com.tida_okinawa.corona.correction.parsing.model.Term;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * @author kousuke-morishima
 */
public class PatternModelUtil {

    /**
     * 検索範囲に矛盾がないか確認する。子が親よりも広い検索範囲を保持していたら、親の検索範囲を子にセットする。
     * ただし、Linkパターンの参照元よりも参照先が広い場合には置き換えは行われない。
     * 
     * @param pattern
     *            確認するパターン
     * @param maxScope
     *            設定できる最大範囲。patternがこの範囲よりも小さな範囲だったとき、patternのもつ範囲が優先される。
     */
    public static void scopeCheck(Pattern pattern, SearchScopeType maxScope) {
        if (pattern instanceof IScopePattern) {
            SearchScopeType scope = ((IScopePattern) pattern).getScope();
            // int値の大きいほうが、範囲は小さい。範囲が小さいほうが優先される
            if (scope.getIntValue() >= maxScope.getIntValue()) {
                maxScope = scope;
            } else {
                ((IScopePattern) pattern).setScope(maxScope);
            }
        } else if (pattern instanceof ModificationElement) {
            maxScope = SearchScopeType.SEARCH_SEGMENT;
        }

        if (pattern instanceof PatternContainer) {
            for (Pattern child : ((PatternContainer) pattern).getChildren()) {
                if (child instanceof IScopePattern) {
                    SearchScopeType childScorpe = ((IScopePattern) child).getScope();
                    if (childScorpe.compareTo(maxScope) < 0) {
                        ((IScopePattern) child).setScope(maxScope);
                    }
                }

                if (child.hasChildren()) {
                    scopeCheck(child, maxScope);
                }
            }
        }
    }


    /**
     * patternおよび親方向のパターンに指定されている検索範囲の最小値を取得する。<br/>
     * このメソッドは、patternの検索範囲が正規化されている（親の範囲 >= 子の範囲になっている）場合にのみ正しく動作する。
     * 
     * @param pattern
     *            このパターンから探索を開始する
     * @param defValue
     *            探索範囲の初期値。いずれのパターンもIScopePatternでなかったとき、この値が返される。
     * @return patternおよび親方向のパターンが保持している最小範囲
     */
    public static SearchScopeType minSST(Pattern pattern, SearchScopeType defValue) {
        if (pattern instanceof IScopePattern) {
            SearchScopeType scope = ((IScopePattern) pattern).getScope();
            defValue = (defValue.getIntValue() > scope.getIntValue()) ? defValue : scope;
        }

        PatternContainer parent = pattern.getParent();
        if (parent == null) {
            return defValue;
        }
        if (parent instanceof IScopePattern) {
            /* IScopePattenは親以下の範囲しか持たないので、最初に見つかったIScopePatternの値を返す */
            SearchScopeType scope = ((IScopePattern) parent).getScope();
            if (scope.getIntValue() > defValue.getIntValue()) {
                /* 数字の大きいほうが、範囲が小さい */
                return scope;
            }
            return defValue;
        }
        return minSST(parent, defValue);
    }


    /**
     * 与えられたパターンの冗長な表現を省く。
     * Linkパターンの展開は行わない。
     * <p>
     * 例：Orパターンの下に、同じ検索範囲のOrパターン
     * <p>
     * 
     * @param own
     *            最適化対象のパターン
     */
    public static void organize(Pattern own) {
        if (own instanceof PatternContainer) {
            PatternContainer container = (PatternContainer) own;
            List<Pattern> children = container.getChildren();
            Stack<Entry<Integer, PatternContainer>> moveParentStack = new Stack<Map.Entry<Integer, PatternContainer>>();
            int ownCategory = category(own);
            for (Iterator<Pattern> itr = children.iterator(); itr.hasNext();) {
                Pattern child = itr.next();
                organize(child);
                if (ownCategory == category(child)) {
                    int insert = children.indexOf(child);
                    itr.remove();
                    moveParentStack.push(new SimpleEntry<Integer, PatternContainer>(insert, (PatternContainer) child));
                }
            }
            while (!moveParentStack.isEmpty()) {
                Entry<Integer, PatternContainer> e = moveParentStack.pop();
                children.addAll(e.getKey(), e.getValue().getChildren());
            }
        }
    }


    /**
     * 空の単語の検査
     * 
     * @param pattern
     *            編集対象パターン
     * @return エラー発生時はtrue
     */
    public static boolean checkEmptyTerm(Pattern pattern) {

        if (pattern instanceof Term) {
            /* 空の単語の判定 */

            // 品詞(PART)
            TermPart part = ((Term) pattern).getPart();
            if (part != null && part != TermPart.NONE) {
                return false;
            }
            // 品詞(CLASS)
            TermClass wordClass = ((Term) pattern).getWordClass();
            if (wordClass != null && wordClass != TermClass.NONE) {
                return false;
            }
            // 原形(単語)
            String word = ((Term) pattern).getWord();
            if (word != null && !word.equals("")) {
                return false;
            }
            // ラベル
            String label = ((Term) pattern).getLabel();
            if (label != null && !"".equals(label)) {
                return false;
            }
            return true;
        } else if (pattern instanceof PatternContainer) {
            /* 子をもつ可能性がある */
            List<Pattern> children = ((PatternContainer) pattern).getChildren();
            for (Pattern child : children) {
                /* 子の形態素を検査する */
                if (checkEmptyTerm(child)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 子がないパターンの検査
     * 
     * @param pattern
     *            編集対象パターン
     * @return エラー発生時はtrue
     */
    public static boolean checkEmptyChild(Pattern pattern) {

        if (pattern instanceof Term) {
            return false;
        } else if (pattern instanceof Link) {
            /* 参照パターンの展開を実施後なので、ここは実行されないはず。 */
            return false;
        } else if (pattern instanceof PatternContainer) {
            /* 子をもつ可能性がある */
            if (!((PatternContainer) pattern).hasChildren()) {
                /* 子がないパターンと判定 */
                return true;
            }
            List<Pattern> children = ((PatternContainer) pattern).getChildren();
            for (Pattern child : children) {
                /* 子の形態素を検査する */
                if (checkEmptyChild(child)) {
                    /* 子がないパターンと判定 */
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 係り元、係り先パターンが複数の子を持つ場合、Orを間にかませる
     * 
     * @param newPattern
     *            編集対象パターン
     */
    public static void insertOrForModification(Pattern newPattern) {

        if (newPattern instanceof Modification) {
            /* 係り受け */
            Modification patternModification = (Modification) newPattern;
            ModificationElement src = patternModification.getSource();
            ModificationElement dst = patternModification.getDestination();
            /* 係り元 */
            if (src != null && src.hasChildren() && src.getChildren().size() > 1) {
                /* 子が複数定義されている場合 */

                /* Orを間にかませる */
                insertOr(src);
            }
            /* 係り先 */
            if (dst != null && dst.hasChildren() && dst.getChildren().size() > 1) {
                /* 子が複数定義されている場合 */

                /* Orを間にかませる */
                insertOr(dst);
            }
        } else if (newPattern instanceof PatternContainer) {
            /* 子をもつ可能性がある */
            if (!((PatternContainer) newPattern).hasChildren()) {
                /* 子がない場合 */
                return;
            }

            /* 子の形態素を検査する */
            List<Pattern> children = ((PatternContainer) newPattern).getChildren();
            for (Pattern child : children) {
                /* 係り元、係り先パターンが複数の子を持つ場合、Orを間にかませる（再帰処理起動） */
                insertOrForModification(child);
            }
        }
    }


    /**
     * 係り元、係り先パターンが複数の子を持つ場合、Orを間にかませる
     * 
     * @param data
     *            編集対象子パターン(係り元、又は係り先パターン)
     */
    private static void insertOr(ModificationElement data) {
        /* 現行の子を確保 */
        List<Pattern> children = data.getChildren();

        /* or のコンテナーを作る */
        Pattern patternOr = new OrOperator(data);
        List<Pattern> childrenOr = ((OrOperator) patternOr).getChildren();
        /* 現行の子をor のコンテナーにセット */
        childrenOr.addAll(children);
        /* 現行の子をカット */
        children.clear();
        /* or のコンテナーを唯一の子としてセット */
        children.add(0, patternOr);
    }


    private static int category(Pattern pattern) {
        int ret = 0;
        if (pattern instanceof PatternContainer) {
            if (pattern instanceof OrOperator) {
                ret = 10;
            } else if (pattern instanceof Sequence) {
                ret = 20;
            } else if (pattern instanceof Order) {
                ret = 30;
            } else if (pattern instanceof AndOperator) {
                ret = 40;
            } else if (pattern instanceof NotOperator) {
                ret = 50;
            } else if (pattern instanceof ModificationElement) {
                ret = 60;
            }
            if (pattern instanceof ScopePattern) {
                ret += ((ScopePattern) pattern).getScope().getIntValue();
            }
        }
        return ret;
    }

}
