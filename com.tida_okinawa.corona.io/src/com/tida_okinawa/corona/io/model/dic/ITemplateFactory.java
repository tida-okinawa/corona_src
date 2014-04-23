/**
 * @version $Id: ITemplateFactory.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/27 0:47:02
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.List;
import java.util.Set;

import com.tida_okinawa.corona.io.model.ICoronaComponent;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public interface ITemplateFactory extends ICoronaComponent {
    /**
     * ひな型アイテムの取得
     * 
     * @return ひな型一覧
     */
    public List<ITemplateItem> getItems();


    /**
     * 追加するひな型アイテムの取得
     * 
     * @return ひな型一覧
     */
    public Set<ITemplateItem> getAddItems();


    /**
     * レコードの更新
     * 
     * @return 更新結果(true/false)
     */
    public boolean updateRecords();


    /**
     * アイテムの追加
     * 
     * @param item
     *            addItemsに追加
     */
    public void addItem(ITemplateItem item);


    /**
     * アイテムの削除
     * 
     * @param item
     *            delItemsに追加
     */
    public void removeItem(ITemplateItem item);


    /**
     * 辞書のアイテム(レコード)数を取得する
     * 
     * @return アイテム(レコード)数
     */
    public int getItemCount();


    /**
     * ひな型の新規作成（TEMPLATE_IDはデフォルト値に設定）
     * 
     * @param name
     *            ひな型名
     * @param text
     *            ひな型情報
     * @param patternType
     *            ひな型分類ID
     * @param parts
     *            参照可否フラグ
     * @return ひな型アイテム
     */
    public ITemplateItem createTemplate(String name, String text, int patternType, boolean parts);


    /**
     * ひな型の新規作成
     * 
     * @param templateId
     *            ひな型ID
     * @param name
     *            ひな型名
     * @param text
     *            ひな型情報
     * @param patternType
     *            ひな型分類ID
     * @param parts
     *            参照可否フラグ
     * @return ひな型アイテム
     */
    public ITemplateItem createTemplate(int templateId, String name, String text, int patternType, boolean parts);
}
