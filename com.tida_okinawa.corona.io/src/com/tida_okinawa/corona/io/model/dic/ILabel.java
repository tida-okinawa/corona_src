/**
 * @version $Id: ILabel.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.List;

/**
 * ラベル辞書アイテムのインタフェース
 * 
 * @author shingo-takahashi
 * 
 */
public interface ILabel extends IDicItem {

    /**
     * 親ラベル辞書アイテムを取得する
     * 
     * @return parent 親ラベル辞書アイテム
     */
    public abstract ILabel getParent();


    /**
     * 親ラベルを設定する. {@link #getChildren()}.add()を内部で呼び出し、親子関係の整合性を保っているので、
     * 親子関係の変更は、このメソッドを使って行う<br />
     * 
     * @param parent
     *            セットする親ラベル辞書アイテムオブジェクト
     */
    public abstract void setParent(ILabel parent);


    /**
     * 子ラベル辞書アイテムを取得する
     * 
     * @return 子ラベル辞書アイテムList
     */
    public abstract List<ILabel> getChildren();


    /**
     * 子ラベルを追加する
     * 
     * @param label
     *            子ラベル辞書アイテム
     * @return 追加できたらtrue
     */
    public abstract boolean addChild(ILabel label);


    /**
     * 子ラベルを削除する
     * 
     * @param label
     *            子ラベル辞書アイテム
     * @return 削除できたらtrue
     */
    public abstract boolean removeChild(ILabel label);


    /**
     * 子ラベルを設定する
     * 
     * @param children
     *            子ラベル辞書アイテムオブジェクトを格納したList
     */
    public abstract void setChildren(List<ILabel> children);


    /**
     * ラベル名を取得する
     * 
     * @return ラベル名
     */
    public abstract String getName();


    /**
     * 最上位の親ラベルアイテムから自身のラベルアイテムまでのパスを出力する.
     * [親/親/.../自身]
     * 
     * @return 親ラベルから自身までのパス文字列
     */
    public abstract String getTreeName();


    /**
     * ラベル名を設定する
     * 
     * @param name
     *            ラベル名文字列
     */
    public abstract void setName(String name);


    /**
     * このラベルが紐づけられている用語アイテムのListを取得する
     * 
     * @return 用語アイテムオブジェクトのList
     */
    public abstract List<ITerm> getTerms();


    /**
     * このラベルに紐づける用語アイテムを追加する
     * 
     * @param term
     *            用語アイテムオブジェクト
     */
    public abstract void addTerm(ITerm term);


    /**
     * このラベルへ紐づける用語を設定する
     * 
     * @param terms
     *            用語アイテムオブジェクトのList
     */
    public abstract void setTerms(List<ITerm> terms);


    /**
     * このラベルの紐づけから指定した用語を削除する
     * 
     * @param term
     *            用語アイテムオブジェクト
     */
    public abstract void removeTerm(ITerm term);


}
