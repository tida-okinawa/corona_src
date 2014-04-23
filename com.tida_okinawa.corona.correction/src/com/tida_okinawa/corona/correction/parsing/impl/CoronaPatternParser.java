/**
 * @version $Id: CoronaPatternParser.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 11:45:23
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.correction.CorrectionActivator;
import com.tida_okinawa.corona.correction.common.StringUtil;
import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.correction.parsing.CoronaPatternException;
import com.tida_okinawa.corona.correction.parsing.CoronaPatternException.PatternError;
import com.tida_okinawa.corona.correction.parsing.ICoronaPatternParser;
import com.tida_okinawa.corona.correction.parsing.PatternModelUtil;
import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternDecoder;
import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.ResultCoronaPattern;
import com.tida_okinawa.corona.io.model.cleansing.HitPosition;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;

/**
 * 構文解析処理.
 * ICoronaPatternParserの実装クラス
 * 
 * @author shingo-takahashi
 */
public class CoronaPatternParser implements ICoronaPatternParser {
    /* ユーザー辞書 */
    private List<IUserDic> userDics = new ArrayList<IUserDic>();

    /* ラベル辞書 */
    LabelDic labelDic = new LabelDic();

    /* パターン辞書　=> パターン(モデル) */
    private Map<IPattern, Pattern> patternMap = new HashMap<IPattern, Pattern>();

    /** 部品パターンのID => 部品パターン(モデル) */
    private Map<Integer, Pattern> id2pattern = new HashMap<Integer, Pattern>();
    private PatternDecoder decoder = new PatternDecoder();

    /**
     * 複数Hitを行うならtrue
     */
    private boolean maltiHitFlag;


    /**
     * コンストラクター
     * 
     * @param project
     *            プロジェクト （辞書を使わない場合は null)
     * @param product
     *            プロダクト （辞書を使わない場合は null)
     */
    public CoronaPatternParser(ICoronaProject project, ICoronaProduct product) {
        /* コンストラクタでは追加しない */
    }


    /**
     * コンストラクター （テスト用）
     * 
     * @param dics
     *            辞書
     */
    public CoronaPatternParser(List<ICoronaDic> dics) {
        addDics(dics);
    }


    /**
     * 辞書を登録
     * 
     * @param dics
     *            ICoronaDicを実装した辞書のリスト
     */
    private void addDics(List<ICoronaDic> dics) {
        if (dics == null)
            return;

        for (ICoronaDic dic : dics) {
            addDic(dic);
        }
    }


    /**
     * 辞書を登録
     * 
     * @param dic
     *            ICoronaDicを実装した辞書
     */
    @Override
    public void addDic(ICoronaDic dic) {
        if (dic instanceof IUserDic) {
            addDic((IUserDic) dic);
        }
        if (dic instanceof ILabelDic) {
            addDic((ILabelDic) dic);
        }
        if (dic instanceof IPatternDic) {
            addDic((IPatternDic) dic);
        }
    }


    /**
     * ユーザー辞書を登録
     * 
     * @param dic
     *            ユーザー辞書
     */
    void addDic(IUserDic dic) {
        userDics.add(dic);
    }


    /**
     * ラベル辞書を登録
     * 
     * @param dic
     *            ラベル辞書
     */
    void addDic(ILabelDic dic) {
        labelDic.addDic(dic);
    }


    /**
     * パターン辞書を登録
     * 
     * @param dic
     *            パターン辞書
     */
    void addDic(IPatternDic dic) {
        MultiStatus status = new MultiStatus(CorrectionActivator.PLUGIN_ID, PatternError.ERROR_PATTERN.getCode(), PatternError.ERROR_PATTERN.getMessage(), null);
        // String(XML) -> Pattern(モデル)
        /* パターンのモデル(XML)を解析 */
        for (IDicItem item : dic.getItems()) {
            IPattern pattern = (IPattern) item;
            String patternText = pattern.getText();
            Pattern patternModel = decoder.encode(patternText);

            if (patternModel != null) {
                MultiStatus itemStatus = add(pattern, patternModel);
                if (!itemStatus.isOK()) {
                    status.addAll(itemStatus);
                }
            } else {
                status.add(createErrorStatus(PatternError.NO_CONTENTS, pattern, null));
            }
        }

        if (!status.isOK()) {
            if (CoronaActivator.getDefault() != null) {
                CoronaActivator.getDefault().getLog().log(status);
            }
        }
    }


    MultiStatus add(IPattern pattern, Pattern patternModel) {
        MultiStatus status = new MultiStatus(CorrectionActivator.PLUGIN_ID, PatternError.ERROR_PATTERN.getCode(), PatternError.ERROR_PATTERN.getMessage(), null);
        if (!pattern.isParts()) { // 部品は除く
            /* 構文解析前処理 */
            try {
                /* 参照パターンの展開 */
                Pattern newPattern = extractLinkPattern(patternModel, new HashSet<Pattern>());

                /* 検索範囲の補正 */
                PatternModelUtil.scopeCheck(newPattern, PatternModelUtil.minSST(newPattern, SearchScopeType.SEARCH_ALL));

                /* ネストする冗長なパターンの展開 */
                PatternModelUtil.organize(newPattern);

                /* 空の単語の検査 */
                if (PatternModelUtil.checkEmptyTerm(newPattern)) {
                    status.add(createErrorStatus(PatternError.TERM_EMPTY, pattern, null));
                }

                /* 子がないパターンの検査 */
                if (PatternModelUtil.checkEmptyChild(newPattern)) {
                    status.add(createErrorStatus(PatternError.NO_CHILD, pattern, null));
                }

                /* 係り元、係り先パターンが複数の子を持つ場合、Orを間にかませる */
                PatternModelUtil.insertOrForModification(newPattern);
                // memo 20130118 レビュー結果反映

                patternMap.put(pattern, newPattern); /* 再設定 */
            } catch (CoronaPatternException ex) {
                status.add(createErrorStatus(ex.getErrorType(), pattern, ex));
            }
        } else {
            id2pattern.put(pattern.getId(), patternModel);
        }

        return status;
    }


    private static final IStatus createErrorStatus(PatternError err, IPattern pattern, Throwable t) {
        String message = err.getMessage() + "：" + pattern.getLabel(); //$NON-NLS-1$
        IStatus s = new Status(IStatus.WARNING, CorrectionActivator.PLUGIN_ID, err.getCode(), message, t);
        System.err.println(message);
        return s;
    }


    /**
     * patternに含まれるLinkを参照先のパターンで置き換える。
     * 
     * @param pattern
     *            パターン
     * @param visited
     *            循環検出用のリスト。初回呼び出し時に、空のリストを渡す。
     * @return 置き換え終了したパターン
     * @throws CoronaPatternException
     *             パターンが循環している。Linkの参照先がみつからない。patternがnull。
     */
    Pattern extractLinkPattern(Pattern pattern, Collection<Pattern> visited) throws CoronaPatternException {
        if (pattern == null) {
            throw new CoronaPatternException(PatternError.NO_CONTENTS);
        }
        if (visited.contains(pattern)) {
            throw new CoronaPatternException(PatternError.LOOP_LINK);
        }
        if (pattern.isExtractedLink()) {
            /* 展開済みのものはそのまま返す */
            return pattern;
        }

        visited.add(pattern);
        Pattern patternModel = null; // return model

        if (pattern instanceof PatternContainer) {
            /* PatternContainerは展開不要なので、子を展開に行く */
            List<Pattern> children = ((PatternContainer) pattern).getChildren();
            for (Pattern child : children) {
                Pattern newChild = extractLinkPattern(child, visited);
                if (newChild != child) { // Compare by Object
                    /* 旧パターン(Link)を新パターンに置き換える */
                    int index = children.indexOf(child);
                    child.setParent(null);
                    children.set(index, newChild);
                    newChild.setParent((PatternContainer) pattern);
                }
            }
            patternModel = pattern;
        } else if (pattern instanceof Link) {
            /* Linkの指しているパターンを取得する */
            patternModel = id2pattern.get(((Link) pattern).getId());
            /* Memo 辞書間参照のための修正 */
            if (patternModel == null) {
                IPattern destPatternItem = (IPattern) IoActivator.getDicUtil().getItem(((Link) pattern).getId(), DicType.PATTERN);
                if (destPatternItem != null) {
                    patternModel = decoder.encode(destPatternItem.getText());
                    /* Memo id2patternを使いたいので、PatternModelUtilに移植できない */
                    id2pattern.put(destPatternItem.getId(), patternModel);
                }
            }
            if (patternModel != null) {
                /* まずパターンを展開。展開したものをclone */
                Pattern tmp = extractLinkPattern(patternModel, visited);
                patternModel = tmp.clone();
            } else {
                throw new CoronaPatternException(PatternError.BLANK_LINK);
            }
        } else {
            patternModel = pattern;
        }
        patternModel.setExtractedLink(true);
        visited.remove(pattern);
        return patternModel;
    }


    @Override
    public Integer getPatternMapSize() {
        return patternMap.size();
    }


    @Override
    public IResultCoronaPattern parsing(String target) {
        final String tagS = "【";
        final String tagE = "】";

        Map<IPattern, List<String>> hitInfos = new HashMap<IPattern, List<String>>();
        List<IPattern> results = new ArrayList<IPattern>(); /* 合致したパターンのリスト */
        StringBuilder text = new StringBuilder(1000);
        List<String> lList = new ArrayList<String>();
        HitPosition hitPos = null;

        /* 係り受け解析結果を構文解析する */
        SyntaxStructure ss = new SyntaxStructure(StringUtil.splitFast(target));

        Matcher matcher = new Matcher(ss, id2pattern, labelDic);

        for (Entry<IPattern, Pattern> e : patternMap.entrySet()) {
            IPattern pattern = e.getKey();
            Pattern patternModel = e.getValue();

            /* パターンごとのヒット位置保持オブジェクト */
            List<String> hitPositionsCurrent = new ArrayList<String>();

            /* 各パターンと照合する */
            int[] matchPosition = { -1, 0 };/* 初回Whileで利用するので、{-1,0}で初期化 */

            if (maltiHitFlag) {
                while (true) {
                    hitPos = matcher.match(patternModel, matchPosition[0] + 1);
                    if (!hitPos.isHit()) {
                        // ヒットなし
                        break;
                    }
                    hitPositionsCurrent.add(hitPos.toString());
                    // memo 20130125 解析処理内では指定範囲外でのヒットの場合は、ヒット無しとする。
                    // （係り受け パターン１，３の場合に可能性がある。）
                    // （検索開始位置より前の位置でのヒット情報はあり得ないので、無限ループ対策はここでは不要とする。）
                    /* 次回の検索開始位置に関する情報を直近のヒット位置に指定する */
                    matchPosition = hitPos.getRange();
                }

            } else {
                hitPos = null;
                hitPos = matcher.match(patternModel);
                if (hitPos.isHit()) {
                    hitPositionsCurrent.add(hitPos.toString());
                }
            }

            if (!hitPositionsCurrent.isEmpty()) {
                hitInfos.put(pattern, hitPositionsCurrent);

                /* パターンに合致した */
                results.add(pattern);

                boolean bInit = false;
                /* パターン中のラベル-用語関係をストック */
                for (Entry<ILabel, String> entry : matcher.getLabelToTerm().entrySet()) {
                    /* パターン区分 */
                    PatternType pType = PatternType.getPatternType(pattern.getPatternType());
                    if (pType != null) {
                        lList.add(pType.getPatternName());
                    } else {
                        lList.add(""); //$NON-NLS-1$
                    }
                    /* パターンラベル */
                    lList.add(pattern.getLabel());
                    /* パターン中のHit用語ラベル */
                    lList.add(entry.getKey().getTreeName());
                    /* ラベルにHitした用語 */
                    lList.add(entry.getValue());
                    bInit = true;
                }
                if (!bInit) {
                    /* パターン区分 */
                    PatternType pType = PatternType.getPatternType(pattern.getPatternType());
                    if (pType != null) {
                        lList.add(pType.getPatternName());
                    } else {
                        lList.add(""); //$NON-NLS-1$
                    }
                    /* パターンラベル */
                    lList.add(pattern.getLabel());
                    lList.add(""); //$NON-NLS-1$
                    lList.add(""); //$NON-NLS-1$
                }
            }
        }

        /* 原文の表記をセット */
        // TODO: 表示用のデータ (XML?)にする
        for (MorphemeElement me : ss.getMorphemeElemsnts()) {
            StringBuilder token = new StringBuilder(200);
            token.append(me.getHyouki());

            /* 全ラベルを付与する */
            List<ILabel> labels = labelDic.getLabels(me.getGenkei());
            if (labels.size() > 0) {
                for (ILabel label : labels) {
                    String strLabels = label.getName();
                    while (label.getParent() != null) {
                        label = label.getParent();
                        strLabels = label.getName() + "/" + strLabels;
                    }
                    token.insert(0, tagE).insert(0, strLabels).insert(0, tagS);
                    token.append(tagS).append("/").append(strLabels).append(tagE);

                    // TODO:ラベルに対する係り受け関係を表現
                    // TODO:ラベル単語の抽出
                }
            }
            text.append(token);
        }

        /* ラベル情報を埋め込む */
        if (lList.size() > 0) {
            for (String s : lList) {
                text.append("%&%&%&").append(s);
            }
        }
        return new ResultCoronaPattern(-1, hitInfos, text.toString());
    }


    @Override
    public void setMaltiHit(boolean isMalti) {
        this.maltiHitFlag = isMalti;
    }
}
