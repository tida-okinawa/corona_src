/**
 * @version $Id: IDicExport.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/17 17:18:27
 * @author yukihiro-kinjyo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.command;

import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.xml.sax.SAXException;

import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * 辞書エクスポート処理のインタフェース
 * 
 * @author yukihiro-kinjyo
 * 
 */
public interface IDicExport {


    /**
     * 辞書エクスポート
     * 
     * @param path
     *            出力先ファイルパス
     * @param dic
     *            エクスポート対象の辞書
     * @param monitor
     *            進捗確認用モニター
     * @return OK_STATUS。エラー時は呼び出し側で例外をキャッチする
     * @throws IOException
     *             出力先ファイルが見つからない。出力文字コードがサポートされていない。
     * @throws ParserConfigurationException
     *             パターン辞書エクスポート時、パースに失敗した
     * @throws SAXException
     *             パターン辞書エクスポート時、XML構造解析に失敗した
     */
    public IStatus export(String path, ICoronaDic dic, IProgressMonitor monitor) throws IOException, ParserConfigurationException, SAXException;


    /**
     * ユーザー辞書エクスポート
     * ラベル辞書を選択が必要
     * 
     * @param path
     *            出力先ファイルパス
     * @param dic
     *            エクスポート対象の辞書
     * @param ldics
     *            このユーザ辞書に関連するラベル辞書
     * @param monitor
     *            進捗確認用モニター
     * @return 成功したらtrue
     * @throws IOException
     *             出力先ファイルが見つからない。出力文字コードがサポートされていない。
     */
    public IStatus export(String path, IUserDic dic, Set<ILabelDic> ldics, IProgressMonitor monitor) throws IOException;


    /**
     * 読み書きする文字コードの設定.
     * 指定されたファイルに書き込む文字コードを指定する。
     * 
     * @param encode
     *            文字コード
     */
    public void setEncode(String encode);


    /**
     * 読み書きする文字コードを取得.
     * 指定されたファイルに書き込む文字コードを指定する。
     * 
     * @return 文字コード
     */
    public String getEncode();
}
