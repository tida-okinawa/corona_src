/**
 * @version $Id: CoronaConstants.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/26 23:25:41
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui;

/**
 * 文字列定数クラス
 * 
 * @author kousuke-morishima
 */
public class CoronaConstants {

    /** 共通辞書名 */
    public static final String COMMON_LIBRARY_NAME = Messages.CoronaConstants_FolderName_CommonDictionary;

    /** 辞書名 */
    public static final String LIBRARY_NAME = Messages.CoronaConstants_FolderName_Dictionary;

    /** 処理対象フォルダ名 */
    public static final String CLAIM_FOLDER_NAME = Messages.CoronaConstants_FolderName_ClaimData;

    /** 処理結果フォルダ名 */
    public static final String CORRECTION_FOLDER_NAME = Messages.CoronaConstants_FolderName_Correction;


    /**
     * @param claimName
     *            問い合わせデータ登録名
     * @return 処理結果(問い合わせデータ登録名) 形式のフォルダ名
     */
    public static final String createCorrectionFolderName(String claimName) {
        return Messages.bind("{0}({1})", CORRECTION_FOLDER_NAME, claimName); //$NON-NLS-1$
    }
}
