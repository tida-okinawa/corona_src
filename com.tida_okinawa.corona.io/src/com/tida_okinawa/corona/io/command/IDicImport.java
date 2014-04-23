/**
 * @version $Id: IDicImport.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/10 17:28:04
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.command;

import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.io.model.TextItem;

/**
 * 辞書インポート処理のインタフェース
 * 
 * @author shingo-takahashi
 * 
 */
public interface IDicImport {

    /**
     * 辞書インポート処理
     * 
     * @param path
     *            インポートする辞書のファイルパス
     * @param dicName
     *            インポートしたときに、DBに登録する辞書名
     * @param category
     *            分野名（分野辞書のインポート時のみ必須）
     * @param monitor
     *            進捗確認用モニター
     * @param ignoreLabel
     *            ラベル情報インポート有無
     * @return 成功ならtrue
     * @throws IOException
     *             指定したファイルが見つからない
     * @throws SQLException
     *             辞書のコミットに失敗した
     * @throws UnsupportedOperationException
     *             対応していないファイルを指定された
     */
    public boolean import0(String path, String dicName, TextItem category, IProgressMonitor monitor, Boolean ignoreLabel) throws IOException, SQLException,
            UnsupportedOperationException;


    /**
     * 読み書きする文字コードの設定.
     * 指定されたファイルを読み込む文字コードを指定する。
     * 
     * @param encode
     *            文字コード
     */
    public void setEncode(String encode);


    /**
     * 読み書きする文字コードを取得.
     * 指定されたファイルを読み込む文字コードを指定する。
     * 
     * @return 文字コード
     */
    public String getEncode();

}
