/**
 * @version $Id: Matcher.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 11:45:23
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.tida_okinawa.corona.common.Pair;
import com.tida_okinawa.corona.correction.morphem.ISyntaxStructureElement;
import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.correction.parsing.PatternModelUtil;
import com.tida_okinawa.corona.correction.parsing.model.AndOperator;
import com.tida_okinawa.corona.correction.parsing.model.IQuantifier;
import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.correction.parsing.model.Modification;
import com.tida_okinawa.corona.correction.parsing.model.ModificationElement;
import com.tida_okinawa.corona.correction.parsing.model.NotOperator;
import com.tida_okinawa.corona.correction.parsing.model.OrOperator;
import com.tida_okinawa.corona.correction.parsing.model.Order;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.QuantifierType;
import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;
import com.tida_okinawa.corona.correction.parsing.model.Sequence;
import com.tida_okinawa.corona.correction.parsing.model.Term;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.cleansing.HitPosition;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * 文（形態素結果）とパターンがマッチするか判定する.
 * 構文解析処理のコア。
 * <p>
 * 処理スレッドごとにインスタンスを作成すること
 * </p>
 * 
 * @author imai
 * 
 */
class Matcher {

    /*
     * TODO ヒットしない場合の戻り値をnullにしたい。
     * 無駄なインスタンス生成があって困る
     */

    /** ラベル-hit用語関係 */
    private Map<ILabel, String> label2Term = new HashMap<ILabel, String>();
    private Map<Integer, Pattern> id2pattern;
    private LabelDic labelDic;

    /** 係り受け解析結果（対象のテキスト） */
    final SyntaxStructure ss;

    /** ssの形態素のリスト */
    final List<MorphemeElement> morphemes;


    /**
     * コンストラクタ
     * 
     * @param ss
     *            係り受け解析結果（対象のテキスト）
     * @param id2pattern
     *            ラベル-hit用語
     * @param labelDic
     *            対象ラベル
     */
    Matcher(SyntaxStructure ss, Map<Integer, Pattern> id2pattern, LabelDic labelDic) {
        this.ss = ss;
        this.morphemes = ss.getMorphemeElemsnts();
        this.id2pattern = id2pattern;
        this.labelDic = labelDic;
    }


    /**
     * パターンと照合する
     * 
     * @param pattern
     *            パターン
     * @return ヒット位置オブジェクト(合致した形態素の位置情報をHitPositionで格納)
     */
    HitPosition match(Pattern pattern) {
        return match(pattern, 0);
    }


    /**
     * パターンと照合する。
     * 
     * @param pattern
     *            パターン
     * @param index
     *            走査開始位置
     * @return ヒット位置オブジェクト(合致した形態素の位置情報をHitPositionで格納)
     * */
    HitPosition match(Pattern pattern, int index) {
        if (index < 0 || morphemes.size() <= index) {
            /* ヒットなしで戻り値を準備 */
            return new HitPosition(HitPosition.PatternKind.NONE);
        }

        label2Term.clear();
        HitPosition hitPosition = match(pattern, index, morphemes.size());

        return hitPosition;
    }


    /**
     * パターンとの照合
     * 
     * @param pattern
     *            パターン（モデル）
     * @param start
     *            検索開始位置。この値を検索位置に含む。
     * @param end
     *            検索終了位置。この値を検索位置に含まない。
     * @return ヒット位置オブジェクト(合致した形態素の位置情報をHitPositionで格納)
     *         複合パターン {@link PatternContainer} の場合もHitPositionで返す
     */
    HitPosition match(Pattern pattern, int start, int end) {
        /* 戻り値用領域の確保 */
        HitPosition hitPosition = null;

        /* 処理選択 */
        if (pattern instanceof Term) {
            hitPosition = match((Term) pattern, start, end);
        } else if (pattern instanceof OrOperator) {
            hitPosition = match((OrOperator) pattern, start, end);
        } else if (pattern instanceof Order) {
            hitPosition = match((Order) pattern, start, end);
        } else if (pattern instanceof Sequence) {
            hitPosition = match((Sequence) pattern, start, end);
        } else if (pattern instanceof AndOperator) {
            hitPosition = match((AndOperator) pattern, start, end);
        } else if (pattern instanceof NotOperator) {
            hitPosition = match((NotOperator) pattern, start, end);
        } else if (pattern instanceof Modification) {
            hitPosition = match((Modification) pattern, start, end);
        } else if (pattern instanceof ModificationElement) {
            hitPosition = match((ModificationElement) pattern, start, end);
        } else if (pattern instanceof Link) {
            hitPosition = match((Link) pattern, start, end);
        } else {
            // never
            throw new UnsupportedOperationException(pattern.toString() + ":" + pattern.getClass().getName()); //$NON-NLS-1$
        }

        return hitPosition;
    }


    /* 用語または用語群 (単語) */
    HitPosition match(Term pattern, int start, int end) {
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.TERM);
        TermPart part = pattern.getPart(); // 品詞(PART)
        TermClass wordClass = pattern.getWordClass(); // 品詞(CLASS)
        String word = pattern.getWord(); // 原形(単語)
        String label = pattern.getLabel(); // ラベル
        QuantifierType quantifierType = pattern.getQuant(); //数量子 

        // 20130214 チケット#1085
        /* 設定が全て未設定の場合、ヒットなしとする。 */
        /* 連続での数量子のみ設定の場合もヒットなしとなる。（範囲が０の為） */
        if ((part == TermPart.NONE) && (wordClass == TermClass.NONE) && "".equals(word) && "".equals(label)) { //$NON-NLS-1$ //$NON-NLS-2$
            /* 数量子0チェック */
            if (quantifierType != QuantifierType.QUANT_NONE) {
                /* 数量子の場合、(検索開始位置, 0)をヒット位置として追加 */
                hitPosition.addHitPosition(new int[] { start, 0 });
            }

            /* ヒット無し */
            return hitPosition;
        }

        for (int i = start; i < end; i++) {
            MorphemeElement morpheme = morphemes.get(i);

            /* 品詞(PART) */
            if (part != TermPart.NONE && !part.getName().equals(morpheme.getHinshi())) {
                continue;
            }
            /* 品詞(CLASS) */
            if (wordClass != TermClass.NONE && !wordClass.getName().equals(morpheme.getHinshiSaibunrui())) {
                continue;
            }
            /* 原形(単語) */
            if (!"".equals(word) && !word.equals(morpheme.getGenkei())) { //$NON-NLS-1$
                continue;
            }
            /* ラベル */
            if (!"".equals(label)) { //$NON-NLS-1$
                boolean matchedLabel = false;
                List<ILabel> labels = labelDic.getLabels(morpheme.getGenkei());
                for (ILabel l : labels) {
                    matchedLabel = isLabel(l, label);
                    if (matchedLabel) {
                        /* ラベルに対応する用語を保持 */
                        label2Term.put(l, morpheme.getGenkei());
                        break;
                    }
                }
                if (!matchedLabel) {
                    /* ラベルに該当しない */
                    continue;
                }
            }

            /* 戻り値 */
            hitPosition.addHitPosition(new int[] { i, 1 });
            /* 数量子0チェック */
            if (quantifierType != QuantifierType.QUANT_NONE) {
                /* 数量子の場合、(検索開始位置, 0)をヒット位置として追加 */
                hitPosition.addHitPosition(new int[] { start, 0 });
            }

            return hitPosition;
        }

        /* 数量子0チェック */
        if (quantifierType != QuantifierType.QUANT_NONE) {
            hitPosition.addHitPosition(new int[] { start, 0 });
        }

        return hitPosition;
    }


    /* AND */
    HitPosition match(AndOperator pattern, int start, int end) {
        if (start == end) {
            /* ヒットなし */
            return new HitPosition(HitPosition.PatternKind.AND);
        }
        /* 戻り値用領域の確保 */
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.AND);

        /* 子がひとつもない場合、ヒット位置なしのヒットとする。 */
        if (pattern.getChildren().size() <= 0) {
            /* ヒット位置なしとして追加 */
            hitPosition.addHitPosition(new int[] { start, 0 });

            return hitPosition;
        }

        /* スコープで検索範囲を変更 */
        switch (pattern.getScope()) {
        case SEARCH_SEGMENT: { /* 文節毎に判定 */
            int searchStart = 0;
            int searchEnd = -1;

            int startIndex = ss.getSyntaxStructureElement(this.morphemes.get(start)).getIndex();
            int endIndex = ss.getSyntaxStructureElement(this.morphemes.get(end - 1)).getIndex();
            boolean isFirstMatch = false;
            for (int i = startIndex; i <= endIndex; i++) {
                List<MorphemeElement> list = ss.get(i).getMorphemes();
                if (isFirstMatch == false) {
                    // 先頭は文節途中から開始の可能性アリ

                    /* 指定位置情報を絶対値に変換 */
                    searchStart = start;
                    searchEnd = list.size() + start - list.indexOf(this.morphemes.get(start));

                    isFirstMatch = true;
                } else {
                    /* 指定位置情報を絶対値に変換 */
                    searchStart = searchEnd;
                    searchEnd = list.size() + searchStart;
                }

                /* 終端位置情報を確認 */
                if (searchEnd > end) {
                    searchEnd = end;
                }
                hitPosition = matchAND(pattern, searchStart, searchEnd);
                if (hitPosition.isHit()) {
                    return hitPosition;
                }
            }
            break;
        }
        case SEARCH_SENTENCE: { /* 文毎に判定 */
            int searchStart = start;
            int searchEnd = -1;

            int indexSentence = this.morphemes.get(start).indexSentence;
            for (int i = start; i < end; i++) {
                if (indexSentence != this.morphemes.get(i).indexSentence) {
                    searchEnd = i;
                    //searchEnd = i + start;

                    hitPosition = matchAND(pattern, searchStart, searchEnd);
                    if (hitPosition.isHit()) {
                        return hitPosition;
                    }
                    searchStart = i;
                    indexSentence = this.morphemes.get(i).indexSentence;
                }
            }
            /* 最終行 */
            searchEnd = end;

            hitPosition = matchAND(pattern, searchStart, searchEnd);
            if (hitPosition.isHit()) {
                return hitPosition;
            }
            break;
        }
        default: /* 文章全体で判定 */
            return matchAND(pattern, start, end);
        }

        /* ヒットなし */
        return new HitPosition(HitPosition.PatternKind.AND);
    }


    /* OR */
    HitPosition match(OrOperator pattern, int start, int end) {
        if (start == end) {
            /* ヒットなし */
            return new HitPosition(HitPosition.PatternKind.OR);
        }
        /* 戻り値用領域の確保 */
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.OR);

        /* 子がひとつもない場合、ヒット位置なしのヒットとする。 */
        if (pattern.getChildren().size() <= 0) {
            /* ヒット位置なしとして追加 */
            hitPosition.addHitPosition(new int[] { start, 0 });

            return hitPosition;
        }

        /* スコープで検索範囲を変更 */
        switch (pattern.getScope()) {
        case SEARCH_SEGMENT: { /* 文節毎に判定 */
            int searchStart = 0;
            int searchEnd = -1;

            int startIndex = ss.getSyntaxStructureElement(this.morphemes.get(start)).getIndex();
            int endIndex = ss.getSyntaxStructureElement(this.morphemes.get(end - 1)).getIndex();
            boolean isFirstMatch = false;
            for (int i = startIndex; i <= endIndex; i++) {
                List<MorphemeElement> list = ss.get(i).getMorphemes();
                if (isFirstMatch == false) {
                    // 先頭は文節途中から開始の可能性アリ

                    /* 指定位置情報を絶対値に変換 */
                    searchStart = start;
                    searchEnd = list.size() + start - list.indexOf(this.morphemes.get(start));

                    isFirstMatch = true;
                } else {
                    /* 指定位置情報を絶対値に変換 */
                    searchStart = searchEnd;
                    searchEnd = list.size() + searchStart;
                }

                /* 終端位置情報を確認 */
                if (searchEnd > end) {
                    searchEnd = end;
                }
                hitPosition = matchOR(pattern, searchStart, searchEnd);
                if (hitPosition.isHit()) {
                    return hitPosition;
                }
            }
            break;
        }
        case SEARCH_SENTENCE: { /* 文毎に判定 */
            int searchStart = start;
            int searchEnd = -1;

            int indexSentence = this.morphemes.get(start).indexSentence;
            for (int i = start; i < end; i++) {
                if (indexSentence != this.morphemes.get(i).indexSentence) {
                    searchEnd = i;

                    hitPosition = matchOR(pattern, searchStart, searchEnd);
                    if (hitPosition.isHit()) {
                        return hitPosition;
                    }
                    searchStart = i;
                    indexSentence = this.morphemes.get(i).indexSentence;
                }
            }
            /* 最終行 */
            searchEnd = end;

            hitPosition = matchOR(pattern, searchStart, searchEnd);
            if (hitPosition.isHit()) {
                return hitPosition;
            }
            break;
        }
        default: /* 文章全体で判定 */
            hitPosition = matchOR(pattern, start, end);
            return hitPosition;
        }

        /* ヒットなし */
        return new HitPosition(HitPosition.PatternKind.OR);
    }


    /* NOT */
    HitPosition match(NotOperator pattern, int start, int end) {
        // 20130214 チケット#1085
        if (start >= end) {
            /* ヒット位置なしとして追加 */
            HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.NOT);
            hitPosition.addHitPosition(new int[] { start, 0 });

            return hitPosition;
        }

        /* 子がひとつもない場合、ヒット位置なしのヒットとする。 */
        if (pattern.getChildren().size() <= 0) {
            /* ヒット位置なしとして追加 */
            HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.NOT);
            hitPosition.addHitPosition(new int[] { start, 0 });

            return hitPosition;
        }

        Pattern child = pattern.getChildren().get(0);
        HitPosition hitPosition = match(child, start, end);

        if (!hitPosition.isHit()) {
            /* ヒットした場合、必ず (検索開始位置, 0) を返す。 */
            HitPosition hitPosReturn = new HitPosition(HitPosition.PatternKind.NOT);
            hitPosReturn.addHitPosition(new int[] { start, 0 });

            return hitPosReturn;
        }

        /* ヒットなし */
        return new HitPosition(HitPosition.PatternKind.NOT);
    }


    /* Sequence(連続) */
    HitPosition match(Sequence pattern, int start, int end) {
        if (start == end) {
            /* ヒットなし */
            return new HitPosition(HitPosition.PatternKind.SEQUENCE);
        }
        /* 戻り値用領域の確保 */
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.SEQUENCE);

        /* 子がひとつもない場合、ヒット位置なしのヒットとする。 */
        if (pattern.getChildren().size() <= 0) {
            /* ヒット位置なしとして追加 */
            hitPosition.addHitPosition(new int[] { start, 0 });

            return hitPosition;
        }

        /* スコープで検索範囲を変更 */
        switch (pattern.getScope()) {
        case SEARCH_SEGMENT: { /* 文節毎に判定 */
            int searchStart = 0;
            int searchEnd = -1;

            int startIndex = ss.getSyntaxStructureElement(this.morphemes.get(start)).getIndex();
            int endIndex = ss.getSyntaxStructureElement(this.morphemes.get(end - 1)).getIndex();
            boolean isFirstMatch = false;
            for (int i = startIndex; i <= endIndex; i++) {
                List<MorphemeElement> list = ss.get(i).getMorphemes();
                if (isFirstMatch == false) {
                    // 先頭は文節途中から開始の可能性アリ

                    /* 指定位置情報を絶対値に変換 */
                    searchStart = start;
                    searchEnd = list.size() + start - list.indexOf(this.morphemes.get(start));

                    isFirstMatch = true;
                } else {
                    /* 指定位置情報を絶対値に変換 */
                    searchStart = searchEnd;
                    searchEnd = list.size() + searchStart;
                }

                /* 終端位置情報を確認 */
                if (searchEnd > end) {
                    searchEnd = end;
                }
                hitPosition = matchSequence(pattern, searchStart, searchEnd);
                if (hitPosition.isHit()) {
                    return hitPosition;
                }
            }
            break;
        }
        case SEARCH_SENTENCE: { /* 文毎に判定 */
            int searchStart = start;
            int searchEnd = -1;

            int indexSentence = this.morphemes.get(start).indexSentence;
            for (int i = start; i < end; i++) {
                if (indexSentence != this.morphemes.get(i).indexSentence) {
                    searchEnd = i;

                    hitPosition = matchSequence(pattern, searchStart, searchEnd);
                    if (hitPosition.isHit()) {
                        return hitPosition;
                    }
                    searchStart = i;
                    indexSentence = this.morphemes.get(i).indexSentence;
                }
            }
            // 最終行
            searchEnd = end;

            hitPosition = matchSequence(pattern, searchStart, searchEnd);
            if (hitPosition.isHit()) {
                return hitPosition;
            }
            break;
        }
        default: // 文章全体で判定
            return matchSequence(pattern, start, end);
        }
        return new HitPosition(HitPosition.PatternKind.SEQUENCE);
    }


    /* 順序 */
    HitPosition match(Order pattern, int start, int end) {
        if (start == end) {
            /* ヒットなし */
            return new HitPosition(HitPosition.PatternKind.ORDER);
        }
        /* 戻り値用領域の確保 */
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.ORDER);

        /* 子がひとつもない場合、ヒット位置なしのヒットとする。 */
        if (pattern.getChildren().size() <= 0) {
            /* ヒット位置なしとして追加 */
            hitPosition.addHitPosition(new int[] { start, 0 });

            return hitPosition;
        }

        /* スコープで検索範囲を変更 */
        switch (pattern.getScope()) {
        case SEARCH_SEGMENT: { /* 文節毎に判定 */
            int searchStart = 0;
            int searchEnd = -1;

            int startIndex = ss.getSyntaxStructureElement(this.morphemes.get(start)).getIndex();
            int endIndex = ss.getSyntaxStructureElement(this.morphemes.get(end - 1)).getIndex();
            boolean isFirstMatch = false;
            for (int i = startIndex; i <= endIndex; i++) {
                List<MorphemeElement> list = ss.get(i).getMorphemes();
                if (isFirstMatch == false) {
                    // 先頭は文節途中から開始の可能性アリ

                    /* 指定位置情報を絶対値に変換 */
                    searchStart = start;
                    searchEnd = list.size() + start - list.indexOf(this.morphemes.get(start));

                    isFirstMatch = true;
                } else {
                    /* 指定位置情報を絶対値に変換 */
                    searchStart = searchEnd;
                    searchEnd = list.size() + searchStart;
                }

                /* 終端位置情報を確認 */
                if (searchEnd > end) {
                    searchEnd = end;
                }
                hitPosition = matchOrder(pattern, searchStart, searchEnd);
                if (hitPosition.isHit()) {
                    return hitPosition;
                }
            }
            break;
        }
        case SEARCH_SENTENCE: { /* 文毎に判定 */
            int searchStart = start;
            int searchEnd = -1;

            int indexSentence = this.morphemes.get(start).indexSentence;
            for (int i = start; i < end; i++) {
                if (indexSentence != this.morphemes.get(i).indexSentence) {
                    searchEnd = i;

                    hitPosition = matchOrder(pattern, searchStart, searchEnd);
                    if (hitPosition.isHit()) {
                        return hitPosition;
                    }
                    searchStart = i;
                    indexSentence = this.morphemes.get(i).indexSentence;
                }
            }
            // 最終行
            searchEnd = end;

            hitPosition = matchOrder(pattern, searchStart, searchEnd);
            if (hitPosition.isHit()) {
                return hitPosition;
            }
            break;
        }
        default: /* 文章全体で判定 */
            return matchOrder(pattern, start, end);
        }

        /* ヒットなし */
        return new HitPosition(HitPosition.PatternKind.ORDER);
    }


    // 係り受け
    HitPosition match(Modification pattern, int start, int end) {
        ModificationElement src = pattern.getSource();
        ModificationElement dst = pattern.getDestination();

        if ((src == null) && (dst == null)) {
            /* 9:係り元,係り先がない */
            return matchModification09(start);
        } else if (src == null) {
            /* 係り先のみ */

            if (dst.hasChildren()) {
                /* 6:子がある */
                return matchModification06(pattern, start, end);
            } else {
                /* 8:子がない */
                return matchModification08(pattern, start, end);
            }
        } else if (dst == null) {
            /* 係り元のみ */

            if (src.hasChildren()) {
                /* 5:子がある */
                return matchModification05(pattern, start, end);
            } else {
                /* 7:子がない */
                return matchModification07(pattern, start, end);
            }
        } else {
            /* 係り元先両方ある */

            if (src.hasChildren()) {
                /* 係り元の子がある */
                if (dst.hasChildren()) {
                    /* 1:係り先の子がある */
                    return matchModification01(pattern, start, end);
                } else {
                    /* 2:係り先の子がない */
                    return matchModification02(pattern, start, end);
                }
            } else {
                /* 係り元の子がない */
                if (dst.hasChildren()) {
                    /* 3:係り先の子がある */
                    return matchModification03(pattern, start, end);
                } else {
                    /* 4:係り先の子がない */
                    return matchModification04(start);
                }
            }
        }

    }


    /* リンク */
    HitPosition match(Link pattern, int start, int end) {
        /*
         * 事前にLinkを展開していた場合にここに来ると、すべてNO_MATCHになる。
         * ここに来るのは展開できなかったLink＝id2patternに入っていないパターン＝partPatternはnull
         */
        Pattern partPattern = id2pattern.get(pattern.getId());
        if (partPattern != null) {
            Pattern newPart = partPattern.clone();
            SearchScopeType min = PatternModelUtil.minSST(pattern, SearchScopeType.SEARCH_ALL);
            PatternModelUtil.scopeCheck(newPart, min);
            return match(newPart, start, end);
        }

        /* 戻り値用領域の確保 */
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.LINK);
        /* ヒット位置なしとして追加 */
        hitPosition.addHitPosition(new int[] { start, 0 });

        return hitPosition;
    }


    private static final String getDependType(Modification pattern) {
        /* matchModificationXX系で多用されていたのでメソッド化 */
        String type = null;
        switch (pattern.getType()) {
        case Modification.TYPE_DEPEND:
            type = "D"; //$NON-NLS-1$
            break;
        case Modification.TYPE_PARALLEL:
            type = "P"; //$NON-NLS-1$
            break;
        default:
            break;
        }
        return type;
    }


    /**
     * 指定された文節の位置を返す。
     * 位置は、ヒット情報と同様で0:先頭位置、1:長さの形式
     * 
     * @param element
     *            探したい文節に含まれる形態素
     * @return １文節の位置情報
     */
    private int[] getSegmentPosition(ISyntaxStructureElement element) {
        List<MorphemeElement> morphemes = element.getMorphemes();
        int srcHitStart = this.morphemes.indexOf(morphemes.get(0));
        int srcHitEnd = this.morphemes.indexOf(morphemes.get(morphemes.size() - 1));
        return new int[] { srcHitStart, srcHitEnd - srcHitStart + 1 };
    }


    /**
     * 次の文節の先頭位置を返す
     * 
     * @param currentHitStart
     *            直前にヒットした文節の位置
     * @return 次に検索する文節の開始位置。次がない場合は-1
     */
    private int nextSegmentIndex(int currentHitStart) {
        int nextIndex = ss.getSyntaxStructureElement(this.morphemes.get(currentHitStart)).getIndex() + 1;
        if (nextIndex < ss.size()) {
            List<MorphemeElement> list = ss.get(nextIndex).getMorphemes();
            return this.morphemes.indexOf(list.get(0));
        } else {
            return -1;
        }
    }


    /**
     * 係り元の子があり、係り先の子がある場合の検索処理<br/>
     * 係り元の子がどこかにヒットし、「ヒット位置の係り先」に係り先の子がヒットしたらヒット。
     * 
     * @param pattern
     *            検索するパターン
     * @param start
     *            検索開始位置(include)
     * @param end
     *            検索終了位置(exclude)
     * @return ヒット情報
     */
    HitPosition matchModification01(Modification pattern, int start, int end) {
        ModificationElement src = pattern.getSource();
        ModificationElement dst = pattern.getDestination();

        int searchPosBegin = start;
        HitPosition hitPosS = null;

        while (searchPosBegin < end) {
            /* 検索先頭位置情報をリセットする。 */
            int nextSearchPos = searchPosBegin;

            // パターンを照合する
            hitPosS = match(src, nextSearchPos, end);
            if (!hitPosS.isHit()) {
                // ヒットなし
                return new HitPosition(HitPosition.PatternKind.MODIFICATION);
            }
            /* ヒットあり */

            /* 係り元・先を取得 */
            ISyntaxStructureElement sseSrc = ss.getSyntaxStructureElement(this.morphemes.get(hitPosS.getRange()[0]));
            ISyntaxStructureElement sseDst = sseSrc.getDependDestination(getDependType(pattern));
            if (sseDst != null) {
                /* 係り先文節あり */
                /* dstの内容がsseDstに含まれているか */
                MorphemeElement morpheme0 = sseDst.getMorphemes().get(0);
                MorphemeElement morphemeN = sseDst.getMorphemes().get(sseDst.getMorphemes().size() - 1);
                int newStart = this.morphemes.indexOf(morpheme0);
                int newEnd = this.morphemes.indexOf(morphemeN) + 1;
                /* 係り先でヒット位置を探す */
                HitPosition hitPosD = match(dst, newStart, newEnd);
                if (hitPosD.isHit()) {
                    /* 指定範囲以外を検出時はヒットなしとする */
                    int[] range = hitPosD.getRange();
                    if ((start <= range[0]) && ((range[0] + range[1]) <= end)) {
                        /* ヒットした係り元の位置を格納する */
                        HitPosition hitPosReturn = new HitPosition(HitPosition.PatternKind.MODIFICATION);
                        hitPosReturn.addHitPosition(getSegmentPosition(sseSrc));
                        return hitPosReturn;
                    }
                }
            }

            /* この検索開始位置ではヒットしない為、検索開始位置をずらす（次の文節の先頭へ） */
            searchPosBegin = nextSegmentIndex(hitPosS.getRange()[0]);
            if (searchPosBegin == -1) {
                break;
            }
        }

        // ヒットなし
        return new HitPosition(HitPosition.PatternKind.MODIFICATION);
    }


    /**
     * 係り元の子があり、係り先の子がない場合の検索処理
     * 係り元の子がどこかにヒットし、「ヒット位置の係り先」があればヒット。
     * 
     * @param pattern
     *            検索するパターン
     * @param start
     *            検索開始位置(include)
     * @param end
     *            検索終了位置(exclude)
     * @return ヒット情報
     */
    HitPosition matchModification02(Modification pattern, int start, int end) {
        ModificationElement src = pattern.getSource();

        int searchPosBegin = start;
        int searchBtm = end;
        HitPosition hitPosCurrent = null;

        while (searchPosBegin < searchBtm) {
            /* 検索先頭位置情報をリセットする。 */
            int nextSearchPos = searchPosBegin;

            searchBtm = end;

            /* パターンを照合する */
            hitPosCurrent = match(src, nextSearchPos, searchBtm);
            if (!hitPosCurrent.isHit()) {
                // ヒットなし
                return new HitPosition(HitPosition.PatternKind.MODIFICATION);
            }
            /* ヒットあり */

            /* 係り元・先を取得 */
            ISyntaxStructureElement sseSrc = ss.getSyntaxStructureElement(morphemes.get(hitPosCurrent.getRange()[0]));
            ISyntaxStructureElement sseDst = sseSrc.getDependDestination(getDependType(pattern));
            if (sseDst != null) {
                int[] segmentPos = getSegmentPosition(sseSrc);
                if ((start <= segmentPos[0]) && ((segmentPos[0] + segmentPos[1]) <= end)) {
                    /* ヒットとし 範囲は係り元の方にする */
                    HitPosition hitPosReturn = new HitPosition(HitPosition.PatternKind.MODIFICATION);
                    hitPosReturn.addHitPosition(segmentPos);
                    return hitPosReturn;
                }
            }

            /* この検索開始位置ではヒットしない為、検索開始位置をずらす（次の文節の先頭へ） */
            searchPosBegin = nextSegmentIndex(hitPosCurrent.getRange()[0]);
            if (searchPosBegin == -1) {
                break;
            }
        }

        /* ヒットなし */
        return new HitPosition(HitPosition.PatternKind.MODIFICATION);
    }


    /**
     * 係り元の子がなく、係り先の子がある場合の検索処理
     * 係り先の子がどこかにヒットし、「ヒット位置の係り元」に係り元の子がヒットしたらヒット。
     * 
     * @param pattern
     *            検索するパターン
     * @param start
     *            検索開始位置(include)
     * @param end
     *            検索終了位置(exclude)
     * @return ヒット情報
     */
    HitPosition matchModification03(Modification pattern, int start, int end) {
        ModificationElement dst = pattern.getDestination();

        int searchPosBegin = start;
        int searchBtm = end;
        HitPosition hitPosCurrent = null;

        while (searchPosBegin < searchBtm) {
            /* 検索先頭位置情報をリセットする。 */
            int nextSearchPos = searchPosBegin;

            searchBtm = end;

            /* パターンを照合する */
            hitPosCurrent = match(dst, nextSearchPos, searchBtm);
            if (!hitPosCurrent.isHit()) {
                /* ヒットなし */
                return new HitPosition(HitPosition.PatternKind.MODIFICATION);
            }
            /* ヒットあり */

            /* 係り先・元を取得 */
            MorphemeElement tergetMopheme = this.morphemes.get(hitPosCurrent.getRange()[0]);
            ISyntaxStructureElement sseDst = ss.getSyntaxStructureElement(tergetMopheme);
            List<ISyntaxStructureElement> sseSrcs = sseDst.getDependSources(getDependType(pattern));
            for (ISyntaxStructureElement element : sseSrcs) {
                /* 検索範囲内でもっとも前にある係り元文節を戻り値にする */
                int[] segmentPos = getSegmentPosition(element);

                /* 検索範囲以外を検出時はヒット対象としない。 */
                if ((start <= segmentPos[0]) && ((segmentPos[0] + segmentPos[1]) <= end)) {
                    /* ヒット範囲は、ヒット文節全体を指定 */
                    HitPosition hitPosReturn = new HitPosition(HitPosition.PatternKind.MODIFICATION);
                    hitPosReturn.addHitPosition(segmentPos);

                    return hitPosReturn;
                }
            }

            /* この検索開始位置ではヒットしない為、検索開始位置をずらす（次の文節の先頭へ） */
            searchPosBegin = nextSegmentIndex(hitPosCurrent.getRange()[0]);
            if (searchPosBegin == -1) {
                break;
            }
        }

        /* ヒットなし */
        return new HitPosition(HitPosition.PatternKind.MODIFICATION);
    }


    /**
     * 係り元の子がなく、係り先の子もない場合の検索処理
     * 何かが何かに係る文節にヒット。
     * 
     * @param start
     *            検索開始位置(include)
     * @return startを含む文節
     */
    HitPosition matchModification04(int start) {
        /* TODO このメソッド内のTODOが終わっているのか判断する */
        /* TODO 何かが何かにかかるときにヒットさせる（優先度:低） */
        /* 戻り値用領域の確保 */
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.MODIFICATION);

        /* 係り元を取得 */
        ISyntaxStructureElement sseSrc = ss.getSyntaxStructureElement(this.morphemes.get(start));
        /* 係り元情報から係り先文節の有無を確認 */
        if (sseSrc.getDependDestination() != null) {

            /* 係り元文節の先頭と終端を取得 */
            int sPos = this.morphemes.indexOf(sseSrc.getMorphemes().get(0));
            int ePos = this.morphemes.indexOf(sseSrc.getMorphemes().get(sseSrc.getMorphemes().size() - 1));

            /* 文節内での検索開始位置にある形態素の位置を取得 */
            int ssPos = sseSrc.getMorphemes().indexOf(this.morphemes.get(start));

            /* ヒット範囲を算出 */
            int endSrc = ePos - sPos + 1 - ssPos;
            if (endSrc <= 0) {
                endSrc = 1;
            }

            // TODO return start, sseSrc.end
            /* ヒット情報（ヒット位置＝start,ヒット範囲＝係り元文節の形態素個数 - 文節内でのstartの位置） */
            hitPosition.addHitPosition(new int[] { start, endSrc });
            return hitPosition;
        }
        /* ヒット位置なしとして追加 */
        // hitPosition.addHitPosition(new int[] { start, 0 });
        return hitPosition;
    }


    /**
     * 係り元の子があり、係り先がない場合の検索処理
     * 係り元の子がどこかにヒットし、「ヒット位置の係り先」がなかったらヒット。
     * 
     * @param pattern
     *            検索するパターン
     * @param start
     *            検索開始位置(include)
     * @param end
     *            検索終了位置(exclude)
     * @return ヒット情報
     */
    HitPosition matchModification05(Modification pattern, int start, int end) {
        ModificationElement src = pattern.getSource();

        int searchPosBegin = start;
        int searchBtm = end;
        HitPosition hitPosCurrent = null;

        while (searchPosBegin < searchBtm) {
            /* 検索先頭位置情報をリセットする。 */
            int nextSearchPos = searchPosBegin;
            searchBtm = end;

            /* パターンを照合する */
            hitPosCurrent = match(src, nextSearchPos, searchBtm);
            if (!hitPosCurrent.isHit()) {
                /* ヒットなし */
                return new HitPosition(HitPosition.PatternKind.MODIFICATION);
            }
            /* ヒットあり */

            /* 係り元・先の照合 */
            ISyntaxStructureElement sseSrc = ss.getSyntaxStructureElement(morphemes.get(hitPosCurrent.getRange()[0]));
            ISyntaxStructureElement sseDst = sseSrc.getDependDestination(getDependType(pattern));
            if (sseDst == null) {
                /* ヒットとし 範囲は係り元の方にする */
                HitPosition hitPosReturn = new HitPosition(HitPosition.PatternKind.MODIFICATION);
                hitPosReturn.addHitPosition(getSegmentPosition(sseSrc));
                return hitPosReturn;
            }

            /* この検索開始位置ではヒットしない為、検索開始位置をずらす（次の文節の先頭へ） */
            searchPosBegin = nextSegmentIndex(hitPosCurrent.getRange()[0]);
            if (searchPosBegin == -1) {
                break;
            }
        }

        /* ヒットなし */
        return new HitPosition(HitPosition.PatternKind.MODIFICATION);
    }


    /**
     * 係り元がなく、係り先の子がある場合の検索処理
     * 係り先の子がどこかにヒットし、「ヒット位置の係り元」がなかったらヒット。
     * 
     * @param pattern
     *            検索するパターン
     * @param start
     *            検索開始位置(include)
     * @param end
     *            検索終了位置(exclude)
     * @return ヒット情報
     */
    HitPosition matchModification06(Modification pattern, int start, int end) {
        ModificationElement dst = pattern.getDestination();

        int searchPosBegin = start;
        HitPosition hitPosCurrent = null;

        while (searchPosBegin < end) {
            /* 検索先頭位置情報をリセットする。 */
            int nextSearchPos = searchPosBegin;

            /* パターンを照合する */
            hitPosCurrent = match(dst, nextSearchPos, end);
            if (!hitPosCurrent.isHit()) {
                /* ヒットなし */
                return new HitPosition(HitPosition.PatternKind.MODIFICATION);
            }
            /* ヒットあり */

            /* 係り先・元の照合 */
            ISyntaxStructureElement sseDst = ss.getSyntaxStructureElement(morphemes.get(hitPosCurrent.getRange()[0]));
            List<ISyntaxStructureElement> sseSrcs = sseDst.getDependSources(getDependType(pattern));
            if (sseSrcs.size() == 0) {
                /* 係り元がないので、ヒット範囲は係り先の方にする */
                HitPosition hitPosReturn = new HitPosition(HitPosition.PatternKind.MODIFICATION);
                hitPosReturn.addHitPosition(getSegmentPosition(sseDst));
                return hitPosReturn;
            }

            /* この検索開始位置ではヒットしない為、検索開始位置をずらす（次の文節の先頭へ） */
            searchPosBegin = nextSegmentIndex(hitPosCurrent.getRange()[0]);
            if (searchPosBegin == -1) {
                break;
            }
        }

        /* ヒットなし */
        return new HitPosition(HitPosition.PatternKind.MODIFICATION);
    }


    /**
     * 係り元の子がなく、係り先がない場合の検索処理
     * 何にも係らない文節にヒット。
     * 
     * @param pattern
     *            検索するパターン
     * @param start
     *            検索開始位置(include)
     * @param end
     *            検索終了位置(exclude)
     * @return ヒット情報
     */
    HitPosition matchModification07(Modification pattern, int start, int end) {
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.MODIFICATION);
        /* 先頭文節を求める */
        MorphemeElement startMorpheme = this.morphemes.get(start);
        ISyntaxStructureElement startElement = ss.getSyntaxStructureElement(startMorpheme);
        int startIndex = startElement.getIndex();
        if (startElement.getMorphemes().indexOf(startMorpheme) > 0) {
            startIndex++;
        }
        /* 末端文節を求める */
        int endIndex = ss.getSyntaxStructureElement(this.morphemes.get(end - 1)).getIndex();

        /* 文節単位で検査 */
        for (int i = startIndex; i <= endIndex; i++) {
            ISyntaxStructureElement sseSrc = ss.get(i);
            ISyntaxStructureElement sseDst = sseSrc.getDependDestination(getDependType(pattern));
            if (sseDst == null) {
                /* 係り先が検出できなかった文節をヒット位置とする */
                hitPosition.addHitPosition(getSegmentPosition(sseSrc));
                return hitPosition;
            }
        }

        /* ヒットなし：検索終端位置までHit条件が発生しなかった場合 */
        return hitPosition;
    }


    /**
     * 係り元がなく、係り先の子がない場合の検索処理
     * 何にも係られない文節にヒット。
     * 
     * @param pattern
     *            検索するパターン
     * @param start
     *            検索開始位置(include)
     * @param end
     *            検索終了位置(exclude)
     * @return ヒット情報
     */
    HitPosition matchModification08(Modification pattern, int start, int end) {
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.MODIFICATION);

        /* 先頭文節を求める */
        MorphemeElement startMorpheme = this.morphemes.get(start);
        ISyntaxStructureElement startElement = ss.getSyntaxStructureElement(startMorpheme);
        int startIndex = startElement.getIndex();
        if (startElement.getMorphemes().indexOf(startMorpheme) > 0) {
            /* 文節の途中から始まっている場合、その文節は検索範囲外とする */
            startIndex++;
        }
        /* 末端文節を求める */
        int endIndex = ss.getSyntaxStructureElement(this.morphemes.get(end - 1)).getIndex();

        /* 文節単位で検査 */
        for (int i = startIndex; i <= endIndex; i++) {
            ISyntaxStructureElement sseDst = ss.get(i);
            List<ISyntaxStructureElement> sseSrcs = sseDst.getDependSources(getDependType(pattern));
            if (sseSrcs.size() == 0) {
                /* 係り元が検出できなかった文節をヒット位置とする */
                hitPosition.addHitPosition(getSegmentPosition(sseDst));
                return hitPosition;
            }
        }

        /* 検索終端位置までHit条件が発生しなかった場合 */
        return hitPosition;
    }


    /**
     * 係り元も、係り先もない場合の検索処理
     * 
     * @param start
     *            検索開始位置(include)
     * @return ヒット位置なしのヒット
     */
    HitPosition matchModification09(int start) {
        /* 戻り値用領域の確保 */
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.MODIFICATION);

        /* ヒット位置なしとして追加 */
        hitPosition.addHitPosition(new int[] { start, 0 });
        return hitPosition;
    }


    // 係り元/係り先
    HitPosition match(ModificationElement pattern, int start, int end) {
        if (start >= end) {
            /* ヒットなし */
            return new HitPosition(HitPosition.PatternKind.MODIFICATION);
        }

        Pattern child0 = pattern.getChildren().get(0);
        HitPosition hitPosition = match(child0, start, end);
        if (hitPosition.isHit()) {
            return hitPosition;
        }
        return new HitPosition(HitPosition.PatternKind.MODIFICATION);
    }


    // AND照合
    HitPosition matchAND(AndOperator andPattern, int start, int end) {
        /* 子要素取得 */
        List<Pattern> patterns = andPattern.getChildren();
        /* 初期値 */
        HitPosition hitPosCurrent = null;
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.AND);
        /* ヒット情報取得 */
        for (Pattern pattern : patterns) {
            hitPosCurrent = match(pattern, start, end);
            if (!hitPosCurrent.isHit()) {
                /* ヒットなし */
                return new HitPosition(HitPosition.PatternKind.AND);
            }
            hitPosition.addHitPosition(hitPosCurrent);
        }
        return hitPosition;
    }


    /**
     * OR照合
     * 
     * @param orPattern
     *            OrOperator
     * @param searchStart
     *            検索開始位置
     * @param searchEnd
     *            検索終端位置
     * @return match位置(または判定結果）
     */
    HitPosition matchOR(OrOperator orPattern, int searchStart, int searchEnd) {
        HitPosition hitPosChild = null;
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.OR);
        for (Pattern child : orPattern.getChildren()) {
            hitPosChild = match(child, searchStart, searchEnd);
            if (hitPosChild.isHit()) {
                if (!hitPosition.isHit()) {
                    /* 初ヒット */
                    hitPosition = hitPosChild;
                } else {
                    /* 検索範囲内でもっとも前にヒットしたものを記憶 */
                    int[] matchPosition = hitPosition.getRange();
                    int[] matchChild = hitPosChild.getRange();
                    if (matchPosition[0] > matchChild[0]) {
                        /* 保存中のヒット位置より今回取得したヒット位置が先頭に近い場合 */
                        /* 同一ヒット位置の場合はこの分岐は実行されないので、パターン内でより上に定義されている方が記憶される。 */

                        /* ヒット位置をセット */
                        hitPosition = hitPosChild;
                    }
                }
            }
        }

        HitPosition hitPosReturn = new HitPosition(HitPosition.PatternKind.OR);
        hitPosReturn.addHitPosition(hitPosition);
        return hitPosReturn;
    }


    /* Sequence(連続) child解析 */
    HitPosition matchSequence(Sequence pattern, int searchStart, int searchEnd) {

        // memo 20130129 レビュー結果によりPair<HitPosition, QuantifierType>で定義したオブジェクトを使用する。
        // memo 20130130 パターンによっては正常にヒットしない場合があり、修正する。
        //       数量子のみでヒットなしの場合、ヒットなし
        //       数量子が複数個存在する場合の巻き戻りにおける戻り位置（検索開始位置）を修正する。
        //       ヒット位置情報に数量子付単語でヒットなしの情報を含まない。

        int searchPosBegin = searchStart;
        int searchBtm = searchEnd;
        List<Pattern> children = pattern.getChildren();

        HitPosition hitPosCurrent = null;
        /* ヒット情報を一時保存領域を確保する。 */
        /* Integer ＝ヒットしたパターンのインデックス、Pair<HitPosition ＝ヒット情報, Integer ＝ 数量子情報 > */
        Stack<Entry<Integer, Pair<HitPosition, QuantifierType>>> hitInfoStack = new Stack<Map.Entry<Integer, Pair<HitPosition, QuantifierType>>>();
        while (searchPosBegin < searchBtm) {
            /* 検索先頭位置情報をリセットする。 */
            int nextSearchPos = searchPosBegin;

            /* 検査すべきパターンの先頭の子で検索を実施する。 */
            hitPosCurrent = match(children.get(0), nextSearchPos, searchEnd);
            /* 先頭がヒットしない場合、以降のヒットは期待できないので、ここでヒットの検査を実行する。 */
            if (!hitPosCurrent.isHit()) {
                /* ヒットなし */
                return new HitPosition(HitPosition.PatternKind.SEQUENCE);
            } else {
                /* ヒットあり */

                /* パターン情報を取得 */
                Pattern child = children.get(0);

                /* 子が１個の場合 */
                if (1 == children.size()) {
                    /* 数量子付きでヒットなし（ヒット位置なしのヒット） */
                    if (child instanceof Term) {
                        if ((hitPosCurrent.getRange()[1] == 0) && ((Term) child).getQuant() != QuantifierType.QUANT_NONE) {
                            /* ヒットなしで戻る。 */
                            return new HitPosition(HitPosition.PatternKind.SEQUENCE);
                        }
                    }

                    return hitPosCurrent;
                }

                /* ヒット情報を保存する。 */
                if (child instanceof Term) {
                    /* ヒット情報を格納（単語） */
                    Pair<HitPosition, QuantifierType> pairHit = new Pair<HitPosition, QuantifierType>(hitPosCurrent, ((Term) child).getQuant());
                    hitInfoStack.push(new SimpleEntry<Integer, Pair<HitPosition, QuantifierType>>(0, pairHit));
                } else {
                    /* ヒット情報を格納（単語以外） */
                    Pair<HitPosition, QuantifierType> pairHit = new Pair<HitPosition, QuantifierType>(hitPosCurrent, QuantifierType.QUANT_NONE);
                    hitInfoStack.push(new SimpleEntry<Integer, Pair<HitPosition, QuantifierType>>(0, pairHit));
                }
            }

            /* 検査すべきパターンの先頭の子以降で検索を実施する。（ループは１からスタート） */

            /* 検索開始位置をリセット */
            // #1121修正(ヒット位置の１ずらした値から次回の検索を始める為、ここではsearchPosBeginにヒット位置のみセット)
            searchPosBegin = hitPosCurrent.getRange()[0];
            nextSearchPos = searchPosBegin + hitPosCurrent.getRange()[1];

            for (int currPatternIndex = 1; currPatternIndex < children.size(); currPatternIndex++) {
                Pattern child = children.get(currPatternIndex);

                // チケット#1106
                if (child instanceof NotOperator) {
                    /* NOT */
                    if ((nextSearchPos + 1) <= searchEnd) {
                        searchBtm = nextSearchPos + 1;
                    } else {
                        searchBtm = searchEnd;
                    }
                    /* 解析を実施 */
                    hitPosCurrent = match(child, nextSearchPos, searchBtm);

                } else {
                    searchBtm = searchEnd;
                    /* 検索範囲を確認 */
                    if ((nextSearchPos < searchBtm) && (nextSearchPos >= 0)) {
                        /* 解析を実施 */
                        hitPosCurrent = match(child, nextSearchPos, searchBtm);
                    } else {

                        /* 指定範囲を逸脱している場合、ヒットなしとして以降の処理を実行する */
                        hitPosCurrent = new HitPosition(HitPosition.PatternKind.SEQUENCE);

                        // #1122 終端が数量子付で範囲外の場合ヒットとしておく 
                        //    検索対象が一文または文節（検索終了位置を超えて検索する可能性がある）の場合で、検索中のパターンが数量子付単語のときは、ヒット位置なしのヒットとする。（検索結果のヒット情報としては扱われない）
                        if (child instanceof Term) {
                            if (((Term) child).getQuant() != QuantifierType.QUANT_NONE) {
                                /* 数量子の場合、(検索開始位置, 0)をヒット位置として追加 */
                                hitPosCurrent.addHitPosition(new int[] { nextSearchPos, 0 });
                            }
                        }
                    }
                }

                /* ヒット判定 */
                boolean noHitFlg = true;

                /* (連続の条件として)ヒットであるか確認 */
                if (hitPosCurrent.isHit()) {
                    /* ヒット位置なしのヒットでない場合 */
                    if (hitPosCurrent.getRange()[0] == nextSearchPos) {
                        /* ヒット情報を保存する。 */
                        if (child instanceof IQuantifier) {
                            /* 数量子付で普通にヒットした場合、ヒット情報を格納する。位置なしヒットのみの場合は無視する */
                            if (hitPosCurrent.getRange()[1] != 0) {
                                /* ヒット情報を格納（単語） */
                                Pair<HitPosition, QuantifierType> pairHit = new Pair<HitPosition, QuantifierType>(hitPosCurrent,
                                        ((IQuantifier) child).getQuant());
                                hitInfoStack.push(new SimpleEntry<Integer, Pair<HitPosition, QuantifierType>>(currPatternIndex, pairHit));
                            }

                        } else {
                            /* ヒット情報を格納（単語以外） */
                            Pair<HitPosition, QuantifierType> pairHit = new Pair<HitPosition, QuantifierType>(hitPosCurrent, QuantifierType.QUANT_NONE);
                            hitInfoStack.push(new SimpleEntry<Integer, Pair<HitPosition, QuantifierType>>(currPatternIndex, pairHit));
                        }
                        /* 検索開始位置をリセット */
                        nextSearchPos = hitPosCurrent.getRange()[0] + hitPosCurrent.getRange()[1];
                        /* ヒットあり判定 */
                        noHitFlg = false;
                    } else {
                        /* 数量子付単語 */
                        if ((child instanceof IQuantifier) && !QuantifierType.QUANT_NONE.equals(((IQuantifier) child).getQuant())) {
                            // TODO この条件を満たすとき、hitPosCurrent.getRange()[0] == nextSearchPos は必ずtrueになるのでデッドコード。（テストしている時間がないので、コメントアウトしない）
                            /* この場合、ヒットなしの処理は実施しない。 */
                            noHitFlg = false;
                        }
                    }

                    /* ヒット位置なしのヒット */
                    if (hitPosCurrent.getRange()[1] == 0) {
                        // TODO この条件を満たすとき、hitPosCurrent.getRange()[0] == nextSearchPos は必ずtrueになるのでnoHitFlgはすでにfalse。（テストしている時間がないので、コメントアウトしない）
                        /* 数量子付単語 */
                        if ((child instanceof IQuantifier) && !QuantifierType.QUANT_NONE.equals(((IQuantifier) child).getQuant())) {
                            /* ヒットなしの処理は実施しない。 */
                            noHitFlg = false;
                        }
                    }

                    if (((currPatternIndex + 1) == children.size()) && !noHitFlg) {
                        /* 終端パターンの処理がHIT場合（次の子がない場合）は、HITとする */

                        /* ヒット情報を戻り値として準備する。 */
                        HitPosition hitPosReturnVal = new HitPosition(HitPosition.PatternKind.SEQUENCE);
                        Iterator<Entry<Integer, Pair<HitPosition, QuantifierType>>> iter = hitInfoStack.iterator();
                        while (iter.hasNext()) {
                            Entry<Integer, Pair<HitPosition, QuantifierType>> e = iter.next();
                            Pair<HitPosition, QuantifierType> pairHit = e.getValue();

                            /* 数量子付きでないか、ヒット位置なしでない場合（数量子付単語のヒット無しでない場合） */
                            if ((pairHit.getValue1().getRange()[1] != 0) || QuantifierType.QUANT_NONE.equals(pairHit.getValue2())) {

                                /* 数量子付単語のヒット無し以外のヒット情報を対象とする。 */
                                hitPosReturnVal.addHitPosition(pairHit.getValue1());
                            }
                        }
                        /* ヒットありorヒット無しとして処理終了 */
                        return hitPosReturnVal;
                    }
                }

                if (noHitFlg) {
                    /* ヒットなし */
                    /* 数量子付き単語の場合、HITしない場合でも戻り値としてヒット位置なしのヒット */

                    boolean reStart = false;
                    while (!hitInfoStack.isEmpty()) {
                        /* ヒット情報に数量子付き単語があるか調査 */
                        Entry<Integer, Pair<HitPosition, QuantifierType>> e = hitInfoStack.pop();
                        int hitPatternIndex = e.getKey();
                        Pair<HitPosition, QuantifierType> pairHit = e.getValue();

                        /* 数量子でHit？ */
                        if (!QuantifierType.QUANT_NONE.equals(pairHit.getValue2())) {
                            // memo 20130130 test
                            /* ヒット位置なしのヒット以外の場合、ヒット情報を巻き戻す。 */
                            if (pairHit.getValue1().getRange()[1] != 0) {
                                /* パターンの位置を巻き戻す */
                                currPatternIndex = hitPatternIndex;
                                /* 巻き戻しの対象となった数量子付単語のヒット開始位置を */
                                nextSearchPos = pairHit.getValue1().getRange()[0];

                                reStart = true;
                                break;
                            }
                        }
                    }
                    if (reStart) {
                        /* パターンの位置を巻き戻した位置以降から再実行 */
                        continue;
                    }

                    /* ヒット情報からのパターンの位置の巻き戻しが実行されなかった場合 */
                    /* 現在の検索開始位置ではヒットしない為、検索開始位置をずらす（＋１） */
                    searchPosBegin++;
                    break;
                }
            }
        }

        // ヒットなし
        return new HitPosition(HitPosition.PatternKind.SEQUENCE);
    }


    /* Order(順序) 実行 */
    HitPosition matchOrder(Order pattern, int start, int end) {
        /* 戻り値用領域の確保 */
        HitPosition hitPosition = new HitPosition(HitPosition.PatternKind.ORDER);
        HitPosition hitPosChild = null;

        List<Pattern> children = pattern.getChildren();

        Pattern patternN = children.get(children.size() - 1);
        int searchPosBegin = start;
        int searchend = end;
        int nextSearchPos = searchPosBegin;

        for (Pattern child : children) {
            /* 終了判定（検索終了位置に達した場合、ヒットなし） */
            if (nextSearchPos >= searchend) {
                /* いずれかの子が検索範囲内でヒットしなかった時点で、ヒットなしとする */
                return new HitPosition(HitPosition.PatternKind.ORDER);
            }

            /* 前のパターンが合致した箇所以降で照合する */
            hitPosChild = match(child, nextSearchPos, searchend);

            if (!hitPosChild.isHit()) {
                /* 子すべてがヒットする前に、検索終了位置に達した場合、ヒットなし */
                /* （現在の検索開始位置以降にヒットする要因が存在しない場合、ヒットなし） */
                return new HitPosition(HitPosition.PatternKind.ORDER);
            } else {
                /* ヒットあり */
                hitPosition.addHitPosition(hitPosChild);

                if (patternN == child) {
                    /* 最後のパターンまで合致した */

                    /* 先頭の子がヒットした位置から、末尾の子がヒットした位置までの範囲を戻り値とする */
                    HitPosition hitPosReturn = new HitPosition(HitPosition.PatternKind.ORDER);
                    hitPosReturn.addHitPosition(hitPosition.getRange());
                    return hitPosReturn;
                }
                /* ヒットした位置と範囲を取得 */
                int[] matches = hitPosChild.getRange();
                nextSearchPos = matches[0] + matches[1];
            }
        }

        /* ヒットなし */
        return new HitPosition(HitPosition.PatternKind.ORDER);
    }


    /**
     * ラベル探索処理
     * 
     * @param label
     *            検索対象
     * @param target
     *            検索キー
     * @return
     *         ヒット有無
     */
    private boolean isLabel(ILabel label, String target) {
        if (target.equals(label.getName())) {
            return true;
        }
        if (label.getParent() != null) {
            return isLabel(label.getParent(), target);
        }
        return false;
    }


    public Map<ILabel, String> getLabelToTerm() {
        return label2Term;
    }
}