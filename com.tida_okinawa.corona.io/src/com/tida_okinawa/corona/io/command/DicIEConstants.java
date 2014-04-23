/**
 * @version $Id: DicIEConstants.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/17 17:15:51
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.command;

/**
 * 辞書インポート・エクスポート定数クラス
 * 
 * @author yukihiro-kinjo
 * 
 */
public class DicIEConstants {

    /**
     * CSV区切り文字
     */
    public static final String SPLIT = ",";//$NON-NLS-1$
    /**
     * 改行コード
     */
    public static final String CRLF = System.getProperty("line.separator");//$NON-NLS-1$
    /**
     * ダブルクォーテーション
     */
    public static final String QUOTATION = "\"";//$NON-NLS-1$
    /**
     * コロン
     */
    public static final String COLON = ":";//$NON-NLS-1$
    /**
     * ラベル分割文字
     */
    public static final String PIPE = "|"; //$NON-NLS-1$
    /**
     * XMLSPLIT文字
     */
    public static final String SPLITEND = ">";//$NON-NLS-1$
    /**
     * XMLCHK文字列
     */
    public static final String XMLCHK = "<LIN";//$NON-NLS-1$
    /**
     * LINK終了文字列
     */
    public static final String LINKEND = "/";//$NON-NLS-1$

    /**
     * [ユーザー辞書]「見出し」位置
     */
    public static final int HEADER = 0;

    /**
     * [ユーザー辞書]「読み」位置
     */
    public static final int READING = 1;

    /**
     * [ユーザー辞書]「品詞」位置
     */
    public static final int PART = 2;

    /**
     * [ユーザー辞書]「品詞詳細」位置
     */
    public static final int CLASS = 3;

    /**
     * [ユーザー辞書]「活用形」位置
     */
    public static final int CFORM = 4;

    /**
     * [ユーザー辞書]「ラベル」位置
     */
    public static final int LABEL = 5;

    /**
     * [ゆらぎ・同義語辞書]「ユーザー辞書名」位置
     */
    public static final int USER_DIC_NAME = 0;

    /**
     * [ゆらぎ・同義語辞書]「見出し」位置
     */
    public static final int DEPEND_HEADER = 1;

    /**
     * [ゆらぎ・同義語辞書]「読み」位置
     */
    public static final int DEPEND_READING = 2;

    /**
     * [ゆらぎ・同義語辞書]「品詞」位置
     */
    public static final int DEPEND_PART = 3;

    /**
     * [ゆらぎ・同義語辞書]「品詞詳細」位置
     */
    public static final int DEPEND_CLASS = 4;

    /**
     * [ゆらぎ・同義語辞書]「活用形」位置
     */
    public static final int DEPEND_CFORM = 5;

    /**
     * [ゆらぎ・同義語辞書]「代表後ユーザー辞書名」位置
     */
    public static final int PARENT_USER_DIC_NAME = 6;

    /**
     * [ゆらぎ・同義語辞書]「代表語見出し」位置
     */
    public static final int SYNONYM = 7;

    /**
     * [ゆらぎ・同義語辞書]「代表語読み」位置
     */
    public static final int SYNONYM_READING = 8;

    /**
     * [ゆらぎ・同義語辞書]「代表語品詞」位置
     */
    public static final int SYNONYM_PART = 9;

    /**
     * [ゆらぎ・同義語辞書]「代表語品詞詳細」位置
     */
    public static final int SYNONYM_CLASS = 10;

    /**
     * [ゆらぎ・同義語辞書]「代表語活用形」位置
     */
    public static final int SYNONYM_CFORM = 11;

    /**
     * [ゆらぎ・同義語辞書]ゆらぎ・同義語のカラムサイズ
     */
    public static final int DEPEND_DIC_COLUMNS = 12; // TODO 20131218 代表語に関連する情報を追加する。（品詞詳細、活用形） 10→12

    /**
     * [ユーザー辞書]ユーザー辞書のカラムサイズ
     */
    public static final int USER_DIC_COLUMNS = 6;

    /**
     * XML出力用コンスタンツ　『PatternDic』
     */
    public static final String PATTERNDIC = "PatternDic";//$NON-NLS-1$
    /**
     * XML出力用コンスタンツ　『Pattern』
     */
    public static final String PATTERN = "Pattern"; //$NON-NLS-1$
    /**
     * XML出力用コンスタンツ　『dicname』
     */
    public static final String DICNAME = "dicname"; //$NON-NLS-1$
    /**
     * XML出力用コンスタンツ　『name』
     */
    public static final String NAME = "name"; //$NON-NLS-1$
    /**
     * XML出力用コンスタンツ　『parts』
     */
    public static final String PARTS = "parts"; //$NON-NLS-1$
    /**
     * XML出力用コンスタンツ　『type』
     */
    public static final String TYPE = "type"; //$NON-NLS-1$
    /**
     * XML出力用コンスタンツ　『<LINK』
     */
    public static final String LINK = "<LINK"; //$NON-NLS-1$
    /**
     * XML出力用コンスタンツ　『 dicname=』
     */
    public static final String SDICNAME = " dicname="; //$NON-NLS-1$
    /**
     * XML出力用コンスタンツ　『 name=』
     */
    public static final String SNAME = " name="; //$NON-NLS-1$
    /**
     * XML出力用コンスタンツ　『 parts=』
     */
    public static final String SPARTS = " parts="; //$NON-NLS-1$
    /**
     * XML出力用コンスタンツ　『 type=』
     */
    public static final String STYPE = " type="; //$NON-NLS-1$

}
