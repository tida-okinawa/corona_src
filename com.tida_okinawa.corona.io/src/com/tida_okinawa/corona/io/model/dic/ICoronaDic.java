/**
 * @version $Id: ICoronaDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.tida_okinawa.corona.io.exception.CoronaError;
import com.tida_okinawa.corona.io.model.ICoronaComponent;


/**
 * 辞書オブジェクトのインターフェース
 * 
 * @author shingo-takahashi
 * 
 */
public interface ICoronaDic extends ICoronaComponent {

    /** DB未保存時に設定されるID */
    int UNSAVED_ID = -1;


    /**
     * 辞書IDを取得.
     * このIDはDB上で管理される
     * 
     * @return 辞書ID
     */
    public abstract int getId();


    /**
     * 辞書IDを設定.
     * このIDはDB上で管理される
     * 
     * @param id
     *            辞書ID
     */
    public abstract void setId(int id);


    /**
     * 辞書の表示名を取得
     * 
     * @return 辞書表示名
     */
    public abstract String getName();


    /**
     * 辞書の表示名を設定
     * 
     * @param name
     *            辞書表示名
     */
    public abstract void setName(String name);


    /**
     * 辞書の最終更新日時を取得
     * 
     * @return 辞書最終更新日時を示すDateオブジェクト
     */
    public abstract Date getLasted();


    /**
     * 辞書の最終更新日をセットする
     * 
     * @param date
     *            辞書の最終更新日
     */
    public abstract void setLasted(Date date);


    /**
     * 辞書の作成日時を返却する
     * 
     * @return may be null. 辞書作成日時を示すDateオブジェクト
     */
    public abstract Date getCreationTime();


    /**
     * 辞書の作成日時を設定する
     * 
     * @param date
     *            作成日時を設定したDateオブジェクト
     */
    public abstract void setCreationTime(Date date);


    /**
     * 辞書内の辞書アイテムリストを取得する
     * 
     * @return IDicItem型の辞書アイテム
     */
    public abstract List<IDicItem> getItems();


    /**
     * ページング指定した範囲でアイテムを取得する. <code>page * block</code>件目（0始まり）から、
     * <code>limit</code>件のアイテムを取得する
     * 
     * @param page
     *            何ページ目か
     * @param limit
     *            １ページ当たりのアイテム数
     * @return 取得したアイテムのリスト
     */
    public abstract List<?> getItemsPaging(int page, int limit);


    /**
     * 指定したIDの辞書アイテムを取得する
     * 
     * @param itemId
     *            取得するアイテムのID
     * @return 該当するIDのアイテムがなければnull
     */
    public abstract IDicItem getItem(int itemId);


    /**
     * 辞書アイテム追加
     * 
     * @param item
     *            IDicItemを実装した辞書アイテム
     */
    public abstract void addItem(IDicItem item);


    /**
     * 辞書アイテム削除
     * 
     * @param item
     *            IDicItemを実装した辞書アイテム
     */
    public abstract void removeItem(IDicItem item);


    /**
     * 親辞書IDを取得する
     * 
     * @return 親辞書ID
     */
    public abstract int getParentId();


    /**
     * 親辞書を追加する
     * 
     * @param id
     *            親辞書ID
     */
    public abstract void addParentId(int id);


    /**
     * 親辞書を設定する
     * 
     * @param id
     *            親辞書ID
     */
    public abstract void setParentId(int id);


    /**
     * 親辞書を除去する
     * 
     * @param id
     *            親辞書ID
     */
    public abstract void removeParentId(int id);


    /**
     * 複数の親辞書IDを取得する
     * 
     * @return 複数の親辞書IDを格納したSet
     */
    public abstract Set<Integer> getParentIds();


    /**
     * 複数の親辞書を設定する
     * 
     * @param ids
     *            複数の親辞書IDを格納したSet
     */
    public abstract void setParentIds(Set<Integer> ids);


    /**
     * 辞書ファイルインポート
     * 
     * @param path
     *            取り込む辞書ファイルのパス
     */
    public abstract void importDic(String path);


    /**
     * 辞書ファイルインポート
     * 
     * @param path
     *            取り込む辞書ファイルのパス
     * @param parentDicName
     *            親辞書名
     * @param dicType
     *            作成する辞書種別
     */
    public abstract void importDic(String path, String parentDicName, DicType dicType);


    /**
     * 辞書ファイルエクスポート
     * 
     * @param path
     *            出力ファイルパス
     * @param encoding
     *            文字コード
     */
    public abstract void exportDic(String path, String encoding);


    /**
     * 辞書アイテムに変更があるかを取得
     * 
     * @return 変更があればtrue
     */
    public abstract boolean isDirty();


    /**
     * ダーティ(辞書アイテム変更有)フラグの設定
     * 
     * @param dirty
     *            変更有:true 変更なし:false
     */
    public abstract void setDirty(boolean dirty);


    /**
     * 
     * 辞書データ再取得
     * 
     * @return 更新が正常に終了したらtrue
     */
    public abstract boolean updateRecords();


    /**
     * 辞書のエラー情報を取得する
     * 
     * @return エラー情報オブジェクトのList
     */
    public abstract List<CoronaError> getErrors();


    /**
     * 辞書のアイテム(レコード)数を取得する
     * 
     * @return アイテム(レコード)数
     */
    public int getItemCount();
}
