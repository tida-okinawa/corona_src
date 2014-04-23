/**
 * @version $Id: Messages.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/31 14:52:32
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import org.eclipse.osgi.util.NLS;

/**
 * @author s.takuro
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.ui.editors.extract.messages"; //$NON-NLS-1$
    /** 「閉じる」ボタン */
    public static String EXTRACT_COOCCURRENCE_BUTTON_CLOSE;
    /** 「登録」ボタン　 */
    public static String EXTRACT_COOCCURRENCE_BUTTON_ENTER;
    /** 「種類」コンボ */
    public static String EXTRACT_COOCCURRENCE_COMBO_KIND;
    /** 「登録先」コンボ */
    public static String EXTRACT_COOCCURRENCE_COMBO_ENTRY;
    /** パターン作成確認 */
    public static String EXTRACT_COOCCURRENCE_CONFIRM_CREATE;
    /** 最小共起語数 */
    public static String EXTRACT_COOCCURRENCE_MIN_NUMBER;
    /** 共起抽出ダイアログの概要 */
    public static String EXTRACT_COOCCURRENCE_OUTLINE;
    /** 共起抽出の条件の設定確認メッセージ */
    public static String EXTRACT_COOCCURRENCE_SETTING_CONDITION;
    /** 「共起語数」 ラベル */
    public static String EXTRACT_COOCCURRENCE_SETTING_COMBO_NUMBER;
    /** 共起数の範囲 */
    public static String EXTRACT_COOCCURRENCE_SETTING_COMBO_SCOPE;
    /** 出現順を考慮するかどうかのチェックボックスのテキスト */
    public static String EXTRACT_COOCCURRENCE_SETTING_CHECK;
    /** 共起抽出の設定画面のタイトル */
    public static String EXTRACT_COOCCURRENCE_SETTING_TITLE;
    /** 「種類」カラム */
    public static String EXTRACT_COOCCURRENCE_TABLE_COLUMN_KIND;
    /** 「出現回数」カラム */
    public static String EXTRACT_COOCCURRENCE_TABLE_COLUMN_NUMBER;
    /** 「単語」カラム */
    public static String EXTRACT_COOCCURRENCE_TABLE_COLUMN_TERM;
    /** 共起抽出のタイトル */
    public static String EXTRACT_COOCCURRENCE_TITLE;
    /** 未登録データのエラーメッセージ */
    public static String EXTRACT_ERROR_NOT_CHOOSE;
    /** パターン作成失敗のエラーメッセージ */
    public static String EXTRACT_ERROR_NOT_CREATE_PATTERN;
    /** パターン辞書取得失敗のエラーメッセージ */
    public static String EXTRACT_ERROR_NOT_PATTERNDIC;
    /** 保存先未選択のエラーメッセージ */
    public static String EXTRACT_ERROR_NOT_SAVE_LOCATION;
    /** 問い合わせデータがないときのエラーメッセージ */
    public static String EXTRACT_HANDLER_ERROR_CLAIMDATA_NOTHING;
    /** 係り受け抽出の出現回数がすべて1回 */
    public static String EXTRACT_HANDLER_ERROR_ONCE_ONLY_RELATION;
    /** 係り受け解析が行われていないときのエラーメッセージ */
    public static String EXTRACT_HANDLER_ERROR_RELATION;
    /** パターン辞書が存在しない場合のエラーメッセージ */
    public static String EXTRACT_HANDLER_ERROR_PATTERNDIC_NOTHING;
    /** パターン辞書が開かれている場合のエラーメッセージ */
    public static String EXTRACT_HANDLER_ERROR_PATTERNDIC_OPENED;
    /** 確認メッセージのタイトル */
    public static String EXTRACT_MESSAGEBOX_TEXT_QUESTION;
    /** 注意メッセージのタイトル */
    public static String EXTRACT_MESSAGEBOX_TEXT_WARNING;
    /** パターン辞書への登録失敗 */
    public static String EXTRACT_MESSAGEBOX_TEXT_ENTRY_PATTERN;
    /** エラーメッセージのタイトル */
    public static String EXTRACT_MESSAGEBOX_TEXT_ERROR;
    /** Juman実行中 */
    public static String EXTRACT_MESSAGEBOX_TEXT_EXEC_JUMAN;
    /** 登録中のメッセージボックスのタイトル */
    public static String EXTRACT_MESSAGEBOX_TITLE_ENTRY;
    /** 「閉じる」ボタン */
    public static String EXTRACT_RELATION_BUTTON_CLOSE;
    /** 「登録」ボタン */
    public static String EXTRACT_RELATION_BUTTON_ENTRY;
    /** 「登録先」コンボ */
    public static String EXTRACT_RELATION_COMBO_ENTRY;
    /** 登録確認 */
    public static String EXTRACT_RELATION_CREATE_CHECK;
    /** プログレスモニタタイトル */
    public static String EXTRACT_RELATION_MONITOR_BIGINTASK;
    /** プログレスモニタ 係り受け抽出実行中表示内容 */
    public static String EXTRACT_RELATION_MONITOR_SUBTASK;
    /** 係り受け抽出ダイアログの概要 */
    public static String EXTRACT_RELATION_OUTLINE;
    /** 「出現回数」カラム */
    public static String EXTRACT_RELATION_TABLE_COLUMN_NUMBER;
    /** 「係り元」カラム */
    public static String EXTRACT_RELATION_TABLE_COLUMN_RECENT;
    /** 「係り先」カラム */
    public static String EXTRACT_RELATION_TABLE_COLUMN_DEST;
    /** 係り受け抽出のタイトル */
    public static String EXTRACT_RELATION_TITLE;
    /** 問い合わせデータが係り受け解析済みか（true） */
    public static String EXTRACT_RELATION_TRUE;
    /** 空文字 */
    public static String EXTRACT_RELATION_WILDCARD;
    /** 「登録候補一覧」ラベル */
    public static String EXTRACT_RERATION_LABEL_DETAIL;
    /** 「係り受け抽出結果」ラベル */
    public static String EXTRACT_RERATION_LABEL_RESULT;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
