/**
 * @version $Id: IDicFactory.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/19 13:00:43
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.Set;

import com.tida_okinawa.corona.io.model.ITextItem;

/**
 * 辞書オブジェクト・辞書アイテムオブジェクト生成クラスのインタフェース
 * 
 * @author shingo-takahashi
 */
public interface IDicFactory {


    /**
     * ユーザー辞書オブジェクト生成<br>
     * 親辞書有りの場合は、別途設定が必須
     * 
     * @param name
     *            辞書名
     * @param type
     *            ユーザー辞書タイプ定数
     * @return 辞書オブジェクト
     */
    public ICoronaDic createDic(String name, DicType type);


    /**
     * ユーザー辞書オブジェクト生成<br>
     * 辞書リストへの登録は別途プロジェクトおよび、プロダクトに対して行う。
     * 
     * @param name
     *            　辞書名
     * @param file
     *            辞書ファイル名
     * @param type
     *            　ユーザー辞書タイプ定数
     * @return 辞書オブジェクト
     */
    public abstract ICoronaDic createUserDic(String name, String file, DicType type);


    /**
     * ゆらぎ辞書オブジェクト生成<br>
     * 辞書リストへの登録は別途プロジェクトおよび、プロダクトに対して行う。
     * 
     * @param name
     *            辞書名
     * @param parentId
     *            親辞書ID
     * @return 辞書オブジェクト
     */
    public abstract ICoronaDic createFlucDic(String name, int parentId);


    /**
     * ゆらぎ辞書オブジェクト生成<br>
     * 辞書リストへの登録は別途プロジェクトおよび、プロダクトに対して行う。
     * 
     * @param name
     *            辞書名
     * @param parentIds
     *            複数の親辞書IDを格納したSet
     * @return 辞書オブジェクト
     */
    public abstract ICoronaDic createFlucDic(String name, Set<Integer> parentIds);


    /**
     * 同義語辞書オブジェクト生成<br>
     * 辞書リストへの登録は別途プロジェクトおよび、プロダクトに対して行う。
     * 
     * @param name
     *            辞書名
     * @param parentId
     *            親辞書ID
     * @return 辞書オブジェクト
     */
    public abstract ICoronaDic createSynonymDic(String name, int parentId);


    /**
     * 同義語辞書オブジェクト生成<br>
     * 辞書リストへの登録は別途プロジェクトおよび、プロダクトに対して行う。
     * 
     * @param name
     *            辞書名
     * @param parentIds
     *            複数の親辞書IDを格納したSet
     * @return 辞書オブジェクト
     */
    public abstract ICoronaDic createSynonymDic(String name, Set<Integer> parentIds);


    /**
     * ラベル辞書オブジェクト生成
     * 
     * @param name
     *            辞書名
     * @return 辞書オブジェクト
     */
    public abstract ICoronaDic createLabelDic(String name);


    /**
     * ラベル辞書オブジェクト生成
     * 
     * @param name
     *            辞書名
     * @param parentIds
     *            複数の親辞書IDを格納したSet
     * @return 辞書オブジェクト
     */
    public abstract ICoronaDic createLabelDic(String name, Set<Integer> parentIds);


    /**
     * パターン辞書オブジェクト作成
     * 
     * @param name
     *            辞書名
     * @return 辞書オブジェクト
     */
    public abstract ICoronaDic createPatternDic(String name);


    /**
     * 用語アイテムオブジェクトを作成
     * 
     * @param name
     *            　名前
     * @param reading
     *            　よみ
     * @param termPart
     *            　品詞区分文字列
     * @param termClass
     *            　品詞詳細区分文字列
     * @param cform
     *            　活用形文字列
     * @param jumanBase
     *            　JUMAN原文(JUMANインポート用、ユーザー登録時はnull指定）
     * @return　用語アイテムオブジェクト
     */
    public abstract ITerm createTerm(String name, String reading, String termPart, String termClass, String cform, String jumanBase);


    /**
     * ゆらぎ辞書アイテムオブジェクトを作成
     * 
     * @param main
     *            用語アイテムオブジェクト
     * @return ゆらぎ辞書アイテムオブジェクト
     */
    public abstract IFluc createFluc(ITerm main);


    /**
     * 同義語辞書アイテムオブジェクトを作成
     * 
     * @param main
     *            用語アイテムオブジェクト
     * @return 同義語辞書アイテムオブジェクト
     */
    public abstract ISynonym createSynonym(ITerm main);


    /**
     * ラベル辞書アイテムオブジェクトを作成
     * 
     * @param name
     *            　ラベル名
     * @param parent
     *            親ラベル辞書アイテムオブジェクト
     * @return ラベル辞書アイテムオブジェクト
     */
    public abstract ILabel createLabel(String name, ILabel parent);


    /**
     * 構文パターン辞書アイテムオブジェクトを作成
     * 
     * @param name
     *            パターン名
     * @param text
     *            　パターン文字列
     * @param patternType
     *            　パターン区分
     * @param parts
     *            部品パターンフラグ
     * @return 構文パターン辞書アイテムオブジェクト
     */
    public abstract IPattern createPattern(String name, String text, int patternType, boolean parts);


    /**
     * テキストアイテムオブジェクトを作成
     * 
     * @param data
     *            文字列
     * @return テキストアイテムオブジェクト
     */
    public abstract ITextItem createTextItem(String data);


    /**
     * 頻出用語抽出 用語アイテムを作成
     * 
     * @param value
     *            名前
     * @param reading
     *            よみ
     * @param termPart
     *            品詞区分定義文字列
     * @param termClass
     *            品詞詳細区分定義文字列
     * @param cform
     *            活用形定義文字列
     * @param jumanBase
     *            Juman登録形式文字列
     * @return 頻出用語抽出 用語アイテム
     */
    public abstract ITermCount createTermCount(String value, String reading, String termPart, String termClass, String cform, String jumanBase);

}
