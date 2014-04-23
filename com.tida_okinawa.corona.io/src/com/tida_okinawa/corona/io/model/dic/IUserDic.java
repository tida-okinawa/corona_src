/**
 * @version $Id: IUserDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/19 13:21:05
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.List;

import com.tida_okinawa.corona.io.model.TextItem;

/**
 * ユーザー辞書インタフェース
 * 
 * @author shingo-takahashi
 */
public interface IUserDic extends ICoronaDic {

    /**
     * 辞書ファイル名を取得
     * 
     * @return 辞書ファイル名
     */
    public abstract String getFileName();


    /**
     * 辞書ファイル名を設定する
     * 
     * @param filename
     *            辞書ファイル名
     */
    public abstract void setFileName(String filename);


    /**
     * 辞書タイプを取得
     * 
     * @return 辞書タイプ定数
     */
    public abstract DicType getDicType();


    /**
     * 辞書分野を取得する
     * 
     * @return 辞書分野が設定されたテキストアイテムオブジェクト
     */
    public abstract TextItem getDicCategory();


    /**
     * 辞書分野を設定する
     * 
     * @param dicCategory
     *            辞書分野を設定したテキストアイテムオブジェクト
     */
    public abstract void setDicCategory(TextItem dicCategory);


    @Override
    public abstract List<?> getItemsPaging(int page, int limit);


    /**
     * 以降のgetItems系メソッドで取得するアイテムのソート順を指定する
     * 
     * @param sortField
     *            ソート対象
     * @param order
     *            昇順/降順フラグ trueの場合昇順
     */
    public abstract void setSortOrder(UserDicFieldType sortField, boolean order);


    /**
     * 以降のgetItems系メソッドで取得するアイテムにフィルタをかける。
     * 
     * @param filterField
     *            フィルタ対象のフィールド。must not null
     * @param values
     *            フィルタする値
     * @param acceptOrNot
     *            valuesの値を表示するならtrue、非表示にするならfalse
     */
    public abstract void setFilter(UserDicFieldType filterField, Object[] values, boolean acceptOrNot);


    /**
     * 以降のgetItems系メソッドで取得するアイテムにかけるフィルタを追加する。同じfilterFieldが指定されると、
     * そのvaluesは上書きされる<br />
     * 
     * @param filterField
     *            フィルタ対象のフィールド。must not null
     * @param values
     *            フィルタする値
     */
    public abstract void addFilter(UserDicFieldType filterField, Object[] values);


    /**
     * フィルターの設定をクリアする
     */
    public abstract void clearFilter();


    /**
     * @param acceptOrNot
     *            フィルタした値を表示するならtrue、非表示にするならfalse
     * @see #setFilter(UserDicFieldType, Object[], boolean)
     */
    public abstract void setAcceptOrNot(boolean acceptOrNot);


    /**
     * 引数がnullではないもので、And検索した結果を返す。<br/>
     * <p>
     * likeが使用できるのは、wordとreadのみ。<br/>
     * 任意の位置に'%'を入れる。
     * </p>
     * 
     * @param word
     *            検索対象にしないならnull
     * @param read
     *            検索対象にしないならnull
     * @param part
     *            検索対象にしないならnull
     * @param wordClass
     *            検索対象にしないならnull
     * @param cform
     *            検索対象にしないならnull
     * @param equals
     *            完全一致で探すならtrue。likeを使った検索を行うならfalse
     * @return マッチしたアイテムすべて。１件もなかったら空リスト。なんらかのエラーによって検索に失敗した場合はnull。
     */
    public List<ITerm> findItems(String word, String read, TermPart part, TermClass wordClass, TermCForm cform, boolean equals);


    /**
     * 現在のフィルタ条件で取得できるデータの総件数を返す
     * 
     * @return　現在のフィルタ条件で取得できるデータの総件数
     */
    public abstract int getRecCount();
}
