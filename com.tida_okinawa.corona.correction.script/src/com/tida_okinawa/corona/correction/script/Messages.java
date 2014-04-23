/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/30 18:53:24
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import org.eclipse.osgi.util.NLS;

/**
 * @author kousuke-morishima
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.correction.script.messages"; //$NON-NLS-1$

    /**
     * 割り込み発生時エラー文言
     */
    public static String AutoCleansing_ErrorMessage_InterruptedOccurred;

    /**
     * DB接続失敗時エラー文言
     */
    public static String AutoCleansing_ErrorMessage_NoDatabaseConnection;

    /**
     * 想定外エラー時エラー文言
     */
    public static String AutoCleansing_ErrorMessage_UnExpectedErrorOccurred;

    /**
     * ターゲット無し時エラー文言
     */
    public static String Cleansing2_NoProduct;

    /**
     * プロジェクト無し時エラー文言
     */
    public static String Cleansing2_NoProject;

    /**
     * DB接続詳細情報
     */
    public static String Erratum_ErrorMessage_ArgsDetail;

    /**
     * 誤記補正対象フィールド異常値時エラー文言
     */
    public static String Erratum_ErrorMessage_InvalidFieldsId;

    /**
     * データ無し時エラー文言
     */
    public static String Erratum_ErrorMessage_NoData;

    /**
     * プロジェクト無し時エラー文言
     */
    public static String Erratum_ErrorMessage_NoProject;

    /**
     * 引数個数異常時エラー文言
     */
    public static String ErrorMessage_ArgsNum;

    /**
     * 列番号不正時エラー文言
     */
    public static String ErrorMessage_InvalidColumnNumber;

    /**
     * 引数の数がおかしいときのエラー
     */
    public static String Export_ARGS;

    /**
     * CSV出力
     */
    public static String Export_CSV;

    /**
     * 指定列名が不正時のエラー
     */
    public static String ExportCSV_INVALID_CLUMN;

    /**
     * 該当なし有りフラグのエラー
     */
    public static String ExportCSV_NO_MATCH_ARG;

    /**
     * CSV出力時のエラー
     */
    public static String ExportCSV_NOT_WRITABLE;

    /**
     * DB接続詳細情報
     */
    public static String Import_ErrorMessage_ArgsDetail;

    /**
     * データ名既存時エラー文言
     */
    public static String Import_ErrorMessage_ExistsDataName;

    /**
     * ヘッダーフラグ不正時エラー文言
     */
    public static String Import_ErrorMessage_InvalidBoolean;

    /**
     * SQLファイル無し時エラー文言
     */
    public static String Import_ErrorMessage_No_SqlFile;

    /**
     * CSVファイル無し時エラー文言
     */
    public static String Import_ErrorMessage_NoCsvFile;

    /**
     * フォルダー既存時エラー文言
     */
    public static String Import_ErrorMessage_NoExistsFolder;

    /**
     * 引数詳細
     */
    public static String ImportTwitter_ErrorMessage_ArgsDetail;

    /**
     * 認証失敗時エラー文言
     */
    public static String ImportTwitter_ErrorMessage_Failed_Auth;

    /**
     * ワークスペース無し時エラー文言
     */
    public static String ImportTwitter_ErrorMessage_NoWorkspace;

    /**
     * Twitterアクセストークン無し時エラー文言
     */
    public static String ImportTwitter_ErrorMessage_TryManually;

    /**
     * TwitterAPIエラー時エラー文言
     */
    public static String ImportTwitter_ErrorMessage_UnExpectedErrorOccurred;

    /**
     * 引数詳細
     */
    public static String Morpheme_ErrorMessage_ArgsDetail;

    /**
     * KNP実行フラグ不正時エラー文言
     */
    public static String Morpheme_ErrorMessage_InvalidBoolean;

    /**
     * バンドルID不正時エラー文言
     */
    public static String Morpheme_ErrorMessage_InvalidBundleId;

    /**
     * インストールフォルダ無し時エラー文言
     */
    public static String Morpheme_ErrorMessage_NoInstallFolder;

    /**
     * Juman実行ファイル無し時エラー文言
     */
    public static String Morpheme_ErrorMessage_NoJumanExe;

    /**
     * KNP実行ファイル無し時エラー文言
     */
    public static String Morpheme_ErrorMessage_NoKnpExe;

    /**
     * 誤記補正を表す
     */
    public static String Name_Erratum;

    /**
     * インポートを表す
     */
    public static String Name_Import;

    /**
     * Twitterインポートを表す
     */
    public static String Name_TwitterImport;

    /**
     * 形態素解析を表す
     */
    public static String NameMorpheme;

    /**
     * ゆらぎ・同義語を表す
     */
    public static String NameSynonym;

    /**
     * 構文パターンを表す
     */
    public static String NamePattern;

    /**
     * DB接続情報詳細
     */
    public static String Pattern_ErrorMessage_ArgsDetail;

    /**
     * 複数ヒットフラグ不正時エラー文言
     */
    public static String Pattern_ErrorMessage_InvalidBoolean;

    /**
     * 入力データ種別不正時エラー文言
     */
    public static String Pattern_ErrorMessage_InvalidInputType;

    public static String ResultPatternEditorActionDelegate_claim;

    public static String ResultPatternEditorActionDelegate_div;

    public static String ResultPatternEditorActionDelegate_field;

    public static String ResultPatternEditorActionDelegate_pattern;

    public static String ResultPatternEditorActionDelegate_record;

    public static String ResultPatternEditorActionDelegate_text;

    /**
     * DB接続情報詳細
     */
    public static String Synonym_ErrorMessage_ArgsDetail;

    /**
     * 入力データ種別不正時エラー文言
     */
    public static String Synonym_ErrorMessage_InvalidInputType;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
