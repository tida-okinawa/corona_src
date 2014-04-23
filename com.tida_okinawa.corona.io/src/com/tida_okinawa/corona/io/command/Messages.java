/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/10 14:00:27
 * @author shingo-kuniyoshi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.command;

import org.eclipse.osgi.util.NLS;

/**
 * @author shingo-kuniyoshi
 *         　インポート・エクスポート処理で使用している文字列の外部化
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.io.command.messages"; //$NON-NLS-1$
    /**
     * 辞書ヘッダー情報（共通）
     */
    public static String DicExport_csvHeader;


    /**
     * ゆらぎ・同義語辞書ヘッダー情報
     */
    public static String DicExport_csvHeaderSynonym;


    /**
     * ユーザー辞書ヘッダー情報
     */
    public static String DicExport_csvHeaderUser;


    /**
     * プログレスバー表示文言（エクスポート処理）
     */
    public static String DicExport_monitorExport;


    /**
     * プログレスバー表示文言（データ構築）
     */
    public static String DicExport_monitorMakingData;


    /**
     * プログレスバー表示文言（ファイル出力）
     */
    public static String DicExport_monitorOutput;


    /**
     * ゆらぎ・同義語辞書ヘッダー情報
     */
    public static String DicExport_synonymHeader;


    /**
     * エラーログ表示文言（インポート対象外）
     */
    public static String DicImport_errLogUnsupport;


    /**
     * プログレスバー表示文言（コミット）
     */
    public static String DicImport_monitorCommit;


    /**
     * プログレスバー表示文言（復号処理）
     */
    public static String DicImport_monitorDecode;


    /**
     * プログレスバー表示文言（インポート開始）
     */
    public static String DicImport_monitorImport;

    public static String DicImportCommiter_addLabelInformation;
    public static String DicImportCommiter_comma;
    public static String DicImportCommiter_dicSplit;
    public static String DicImportCommiter_dot;
    public static String DicImportCommiter_doubleQuote;
    public static String DicImportCommiter_duplicateChildWord;
    public static String DicImportCommiter_duplicateDependData;
    public static String DicImportCommiter_duplicateParentWord;
    /**
     * エラーログ表示文言（要素数エラー)
     */
    public static String DicImportCommiter_errLogCsv;

    /**
     * エラーログ表示文言（すべてのレコードが処理対象外）
     */
    public static String DicImportCommiter_errLogCsvAllRecord;

    /**
     * エラーログ表示文言（CSV不正）
     */
    public static String DicImportCommiter_errLogCsvFail;

    /**
     * エラーログ表示文言（辞書コミッター）
     */
    public static String DicImportCommiter_errLogDicCommiter;

    /**
     * エラーログ表示文言（レコードが既に存在する）
     */
    public static String DicImportCommiter_errLogExist;

    /**
     * エラーログ表示文言（既存レコード表示用）
     */
    public static String DicImportCommiter_errLogExistData;

    /**
     * エラーログ表示文言（参照パターンID異常）
     */
    public static String DicImportCommiter_errLogPattern;

    /**
     * つかってなくない？
     */
    public static String DicImportCommiter_errLogRepTerm;

    /**
     * つかってなくない？
     */
    public static String DicImportCommiter_errLogUnknownParent;

    /**
     * プログレスバー表示文言（インポート辞書名）
     */
    public static String DicImportCommiter_errLogUserDicImport;

    /**
     * 従属語未設定時表示文言
     */
    public static String DicImportCommiter_errNoSubs;

    /**
     * 従属語未設定時表示文言
     */
    public static String DicImportCommiter_errNoSubsData;

    /**
     * ラベル辞書ID取得エラー（Exception）
     */
    public static String DicImportCommiter_exceptionLabel;
    public static String DicImportCommiter_ldicWithDot;

    /**
     * プログレスバー表示文言（コミット処理）
     */
    public static String DicImportCommiter_monitorCommit;

    /**
     * プログレスバー表示文言（ラベル辞書）
     */
    public static String DicImportCommiter_monitorLabelDic;

    /**
     * プログレスバー表示文言（ユーザー辞書コミット処理）
     */
    public static String DicImportCommiter_monitorUserDicCommit;
    public static String DicImportCommiter_patternOverride;
    public static String DicImportCommiter_pdicWithDot;
    public static String DicImportCommiter_udicWithDot;
    public static String DicImportCommiter_zerSpace;

    /**
     * エラーログ表示文言（文字コードエラー）
     */
    public static String DicImportDecoder_errLogCharcter;

    /**
     * つかってなくない？
     */
    public static String DicImportDecoder_errLogData;

    /**
     * エラーログ表示文言（復号処理）
     */
    public static String DicImportDecoder_errLogDecoder;

    /**
     * エラーログ表示文言（入出力エラー）
     */
    public static String DicImportDecoder_errLogIO;

    /**
     * エラーログ表示文言（XML構文エラー）
     */
    public static String DicImportDecoder_errLogParse;

    /**
     * エラーログ表示文言（構文パターン不正データ）
     */
    public static String DicImportDecoder_errLogPattern;

    /**
     * エラーログ表示文言（SAXエラー）
     */
    public static String DicImportDecoder_errLogSAX;

    /**
     * エラーログ表示文言（CSV出力エラー）
     */
    public static String PatternDicExport_errLogCsvOutputFail;
    public static String PatternDicExport_errNonLink;

    /**
     * エラーログ表示文言（ファイル出力エラー）
     */
    public static String PatternDicExport_monitorFileOutput;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
