/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/20 13:15:39
 * @author shingo-kuniyoshi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.io.messages"; //$NON-NLS-1$

    /**
     * ラベル削除時表示文言
     */
    public static String ChangeParentDictionayAction_dialogDelLabel;

    /**
     * ラベル削除時表示文言
     */
    public static String ChangeParentDictionayAction_dialogDelLabelWord;

    /**
     * 辞書関係削除時表示文言
     */
    public static String ChangeParentDictionayAction_dialogDelRelation;

    /**
     * 辞書関係削除時表示文言
     */
    public static String ChangeParentDictionayAction_dialogDelRelationWord;

    /**
     * 従属語を表す
     */
    public static String ChangeParentDictionayAction_labelChildWord;

    /**
     * ラベルを表す
     */
    public static String ChangeParentDictionayAction_labelLabel;

    /**
     * ラベル代表語を表す
     */
    public static String ChangeParentDictionayAction_labelParentWord;

    /**
     * 改行コード
     */
    public static String ChangeParentDictionayAction_newLine;

    /**
     * リソース読み込みエラー時表示文言
     */
    public static String IoActivator_errReadResource;

    /**
     * カテゴリを表す
     */
    public static String PropertyUtil_category;

    /**
     * 親辞書の変更を表す
     */
    public static String PropertyUtil_changeParentDic;

    /**
     * 子ゆらぎ辞書を表す
     */
    public static String PropertyUtil_childFluc;

    /**
     * 子ラベル辞書を表す
     */
    public static String PropertyUtil_childLabel;

    /**
     * 子同義語辞書を表す
     */
    public static String PropertyUtil_childSynonym;

    /**
     * 問い合わせデータを表す
     */
    public static String PropertyUtil_claimData;

    /**
     * 問い合わせデータ名を表す
     */
    public static String PropertyUtil_claimDataName;

    /**
     * 項目名を表す
     */
    public static String PropertyUtil_columnName;

    /**
     * 作成日を表す
     */
    public static String PropertyUtil_createDate;

    /**
     * データタイプを表す
     */
    public static String PropertyUtil_dataType;

    /**
     * 辞書タイプを表す
     */
    public static String PropertyUtil_dicType;

    /**
     * KNP実行を表す
     */
    public static String PropertyUtil_doKnp;

    /**
     * 編集可能を表す
     */
    public static String PropertyUtil_editable;

    /**
     * 実行処理を表す
     */
    public static String PropertyUtil_execResult;

    /**
     * 処理対象フィールド名を表す
     */
    public static String PropertyUtil_filed;

    /**
     * インポート日付を表す
     */
    public static String PropertyUtil_importDate;

    /**
     * 最終更新日を表す
     */
    public static String PropertyUtil_lastModified;

    /**
     * ロケーションを表す
     */
    public static String PropertyUtil_location;

    /**
     * 新規親辞書作成時表示文言
     */
    public static String PropertyUtil_messageSelNewParentDic;

    /**
     * 名前を表す
     */
    public static String PropertyUtil_name;

    /**
     * 情報無しを表す
     */
    public static String PropertyUtil_noInformation;

    /**
     * 親辞書名を表す
     */
    public static String PropertyUtil_parentName;

    /**
     * レコードを表す
     */
    public static String PropertyUtil_records;
    /**
     * 処理結果を表す
     */
    public static String PropertyUtil_result;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
