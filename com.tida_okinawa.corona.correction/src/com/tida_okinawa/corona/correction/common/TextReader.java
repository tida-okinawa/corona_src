/**
 * @version $Id: TextReader.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/17 12:00:11
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.tida_okinawa.corona.common.Encoding;

// note: MorphRelation と JumanAnalysis で共通の処理

/**
 * テキストファイルを一行ずつ読み込んで、処理する抽象クラス
 * FluctuationRevReader.javaとMorphemeRelationReader.javaの共通処理をまとめた
 */
public abstract class TextReader {

    /**
     * テキストファイルを読む
     * 
     * @param file
     *            解析結果のファイル
     * @param code
     *            文字コード
     * @exception IOException
     *                読み込みエラー
     * @exception FileNotFoundException
     *                fileが見つからない
     */
    public void read(File file, Encoding code) throws IOException, FileNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        try {
            read(fis, code);
        } catch (IOException e) {
            throw e;
        } finally {
            fis.close();
        }
    }


    /**
     * ファイルオープン処理
     * 
     * @param in
     *            変換するストリーム
     * @param code
     *            文字コード
     * @return サポートしていないエンコードの場合、システムデフォルトのエンコードで作成したBufferedReader
     */
    private static BufferedReader toBufferedReader(InputStream in, Encoding code) {
        try {
            return new BufferedReader(new InputStreamReader(in, code.toString()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(System.err);
            return new BufferedReader(new InputStreamReader(in));
        }
    }


    /**
     * ストリームを読み込んで、1行ずつ処理部に引き渡す。
     * このメソッドでは、ストリームはクローズされない。
     * 
     * @param in
     *            解析結果のストリーム
     * @param code
     *            文字コード
     * @exception IOException
     *                読み込みエラー
     */
    public void read(InputStream in, Encoding code) throws IOException {
        BufferedReader bf = toBufferedReader(in, code);
        read(bf);
    }


    /**
     * ストリームを読み込んで、1行ずつ処理部に引き渡す。
     * このメソッドでは、BufferedReaderはクローズされない。
     * 
     * @param br
     *            入力ストリーム
     * @exception IOException
     *                読み込みエラー
     */
    public void read(BufferedReader br) throws IOException {
        /* 最終行まで1行ずつ読み込んで、処理する */
        String line;
        while ((line = br.readLine()) != null) {
            /* note: ここの処理は、クラスごとに異なる */
            process(line);
        }
    }


    /**
     * 1行分のデータを処理する
     * 
     * @param line
     *            1行分の文字列
     */
    protected abstract void process(String line);
}