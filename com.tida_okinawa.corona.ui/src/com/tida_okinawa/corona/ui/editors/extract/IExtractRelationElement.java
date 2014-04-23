/**
 * @version $Id: IExtractRelationElement.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/11 14:52:26
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.util.List;

import com.tida_okinawa.corona.correction.morphem.ISyntaxStructureElement;
import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * @author s.takuro
 *         #177 パターン自動生成（係り受け抽出）
 */
public interface IExtractRelationElement {
    /**
     * 係り受け抽出した結果をまとめて追加
     * 
     * @param sse
     *            SyntaxStructureElement
     * @return true:成功、false:失敗
     */
    boolean convertSyntaxToExtractRelation(ISyntaxStructureElement sse);


    /**
     * 出現回数をインクリメント
     */
    void incCount();


    /**
     * 出現回数を取得
     * 
     * @return 出現回数
     */
    int getCount();


    /**
     * この文節/句を構成する形態素リストを設定
     * 
     * @param morphemeElement
     *            形態素リスト
     */
    void setMorphemes(List<MorphemeElement> morphemeElement);


    /**
     * この文節/句を構成する形態素リストを取得
     * 
     * @return 形態素リスト
     */
    List<MorphemeElement> getMorphemes();


    /**
     * この文節/句の表記を取得
     * 
     * @param hyouki
     *            表記
     */
    void setHyouki(String hyouki);


    /**
     * この文節/句の表記を取得
     * 
     * @return 表記
     */
    String getHyouki();


    /**
     * この文節/句の読みを設定
     * 
     * @param yomi
     *            読み
     */
    void setYomi(String yomi);


    /**
     * この文節/句の読みを取得
     * 
     * @return 読み
     */
    String getYomi();


    /**
     * この文節/句の係り先を設定
     * 
     * @param sse
     *            係り先の要素
     * @return true:係り先がある、false:係り先がない
     */
    boolean setDependDestination(ISyntaxStructureElement sse);


    /**
     * この文節/句の係り先を取得
     * 
     * @return 係り先
     */
    IExtractRelationElement getDependDestination();


    /**
     * この文節/句の係り先を設定
     * 
     * @param morphemeEre
     *            係り元となる形態素
     * @param morphemeDst
     *            係り先となる形態素
     * @return 結果（係り元と係り先の両方がnullだったらfalse）
     */
    boolean setExtractRelationElement(List<MorphemeElement> morphemeEre, List<MorphemeElement> morphemeDst);


    /**
     * パターン辞書に登録済みかどうかを取得する
     * 
     * @return true:登録済み、false:未登録
     */
    boolean getCompletion();


    /**
     * パターン辞書に登録済みかどうかを設定する
     * 
     * @param completion
     *            true:登録済み、false:未登録
     */
    void setCompletion(boolean completion);
}
