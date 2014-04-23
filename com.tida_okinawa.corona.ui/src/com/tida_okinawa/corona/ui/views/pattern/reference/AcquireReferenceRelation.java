/**
 * @version $Id: AcquireReferenceRelation.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/07/26 15:25:28
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.pattern.reference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeNode;

import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * 部品と部品を参照する構文パターンの親子関係(ツリー)を作成するクラス
 * 
 * @author wataru-higa
 * 
 */
public class AcquireReferenceRelation {
    /**
     * 部品と部品を参照している構文パターンの関係ツリーを作成するメソッド
     * 部品と部品を参照している構文パターンの親子関係データを返却する
     * 
     * @param patternRecordList
     *            辞書内すべての構文パターン
     * @param selectedPattern
     *            エディタで選択された要素または構文パターン
     * @return 部品と部品を参照している構文パターンの親子関係データを返却(TreeNode)
     */
    public TreeNode[] createRelation(List<PatternRecord> patternRecordList, IStructuredSelection selectedPattern) {

        Object[] selectParts = selectedPattern.toArray();

        List<PatternRecord> selectedPatternRecordList = getTop(selectParts);
        List<List<PatternRecord>> accordanceToParent = getAccordanceChildrenList(patternRecordList, selectedPatternRecordList);
        TreeNode[] treeNodeResult = setReferenceReleationTree(selectedPatternRecordList, accordanceToParent);

        return treeNodeResult;
    }


    /**
     * パターンエディタで選択された要素から最上位の要素(構文パターン)を取得するメソッド
     * 同じ構文パターンがリストへ格納されないように重複判定処理も行う
     * 
     * @param selectPattern
     *            パターンエディタで選択された要素
     * @return エディタで選択された構文パターンのリストを返却
     */
    private static List<PatternRecord> getTop(Object[] selectPattern) {
        List<PatternRecord> patternRecordParentList = new ArrayList<PatternRecord>();
        for (Object parts : selectPattern) {
            Pattern pattern = (Pattern) parts;
            // 選択された要素に親要素があれば親要素を取得
            while (pattern.getParent() != null) {
                pattern = pattern.getParent();
            }
            // 重複チェックの為、同じ構文パターンでなければリストへ追加
            if (!patternRecordParentList.contains(pattern)) {
                patternRecordParentList.add((PatternRecord) pattern);
            }
        }
        return patternRecordParentList;
    }


    /**
     * 部品を基に部品を参照している構文パターンを抽出するメソッド
     * 抽出された構文パターンは部品の子としてリストへ格納する
     * selectedPatternRecordListのi番目と戻り値のi番目が対応するように子のリストを格納している
     * 
     * @param allPatternRecordList
     *            辞書内すべての構文パターン
     * @param selectedPatternRecordList
     *            部品リスト
     * @return 部品に対する子のリスト
     * 
     */
    private List<List<PatternRecord>> getAccordanceChildrenList(List<PatternRecord> allPatternRecordList, List<PatternRecord> selectedPatternRecordList) {
        List<List<PatternRecord>> ret = new ArrayList<List<PatternRecord>>();
        // 部品リストの分、処理を繰り返す
        for (PatternRecord selectedPattern : selectedPatternRecordList) {
            List<PatternRecord> childrenList = new ArrayList<PatternRecord>();
            // 辞書内すべての構文パターンの分、処理を繰り返す
            for (PatternRecord patternRecord : allPatternRecordList) {
                // 選択した部品と同等でない場合にLINK検索処理を実行する
                if (!selectedPattern.equals(patternRecord)) {
                    // 選択した構文パターン(部品)が参照元で使用されていれば子リストへ追加
                    if (searchLink(selectedPattern, patternRecord)) {
                        childrenList.add(patternRecord);
                    }
                }
            }
            ret.add(childrenList);
        }
        return ret;
    }


    /**
     * 部品が検索対象の構文パターン内で使用されているかをチェックするメソッド
     * 検索対象構文パターン内のLINK要素IDと部品IDが一致した場合にtrueを返却
     * 
     * @param targetPattern
     *            部品
     * @param pattern
     *            検索対象構文パターン
     * @return LINK要素IDと部品IDが一致した場合にtrueを返却
     */
    private boolean searchLink(PatternRecord targetPattern, PatternContainer pattern) {
        for (Pattern child : pattern.getChildren()) {
            // 選択した部品IDとLINK要素IDが一致すればフラグをセット
            if (child instanceof PatternContainer) {
                if (child.hasChildren()) {
                    if (searchLink(targetPattern, (PatternContainer) child)) {
                        return true;
                    }
                }
            } else if (child instanceof Link) {
                if (targetPattern.getIPattern().getId() == ((Link) child).getId()) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * TreeNodeに部品と部品を参照している構文パターンの親子関係を紐付けするメソッド
     * 
     * @param selectedPatternRecordList
     *            部品リスト
     * @param children
     *            部品を参照している構文パターンのリスト
     * @return 部品と部品を参照している構文パターンの親子関係を返却(TreeNode)
     */
    private static TreeNode[] setReferenceReleationTree(List<PatternRecord> selectedPatternRecordList, List<List<PatternRecord>> children) {
        TreeNode[] treeNode = new TreeNode[selectedPatternRecordList.size()];
        int parentIndex = 0;
        // 部品を親に設定
        for (PatternRecord parent : selectedPatternRecordList) {
            treeNode[parentIndex] = new TreeNode(new ReferenceRelationViewModel(parent.toString(), parent.getIPattern().isParts()));
            // 部品を参照している構文パターンを子へ設定
            List<PatternRecord> childrenPattern = children.get(parentIndex);
            if (childrenPattern.size() == 0) {
                // 部品を参照している構文パターンがない場合
                TreeNode[] child = new TreeNode[1];
                child[0] = new TreeNode(new ReferenceRelationViewModel("参照されている構文パターンはありません", null));
                child[0].setParent(treeNode[parentIndex]);
                treeNode[parentIndex].setChildren(child);
            } else {
                // 部品を参照している構文パターンがある場合
                TreeNode[] childNode = new TreeNode[childrenPattern.size()];
                int childIndex = 0;
                for (PatternRecord child : childrenPattern) {
                    int dicId = child.getIPattern().getComprehensionDicId();
                    ICoronaDic coronaDic = IoActivator.getService().getDictionary(dicId);
                    // 辞書名がnullでない場合に辞書名を表示
                    if (coronaDic != null) {
                        childNode[childIndex] = new TreeNode(new ReferenceRelationViewModel(child.toString() + "　(" + coronaDic.getName() + ")", child
                                .getIPattern().isParts()));
                    } else {
                        childNode[childIndex] = new TreeNode(new ReferenceRelationViewModel(child.toString(), child.getIPattern().isParts()));
                    }
                    childNode[childIndex].setParent(treeNode[parentIndex]);
                    childIndex++;
                }
                treeNode[parentIndex].setChildren(childNode);
            }
            parentIndex++;
        }
        return treeNode;
    }
}
