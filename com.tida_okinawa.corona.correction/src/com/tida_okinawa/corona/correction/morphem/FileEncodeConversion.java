/**
 * @version $Id: FileEncodeConversion.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/18 10:41:12
 * @author sanenori-makiya
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.tida_okinawa.corona.common.Encoding;

/**
 * ファイルの文字コード変換クラス
 * 
 * @author sanenori-makiya, imai
 */
public class FileEncodeConversion {
    /**
     * 出力文字コード
     */
    final Encoding encoding;

    /** 出力バッファーサイズ */
    final int bufferSize;


    /**
     * コンストラクター
     * 文字コードはMS932(Windows-31J)で初期化される
     */
    public FileEncodeConversion() {
        this(Encoding.MS932, 1024 * 1024);
    }


    /**
     * コンストラクター
     * 
     * @param encoding
     *            出力する文字コード
     * @param buf_size
     *            出力バッファーサイズ
     */
    public FileEncodeConversion(Encoding encoding, int buf_size) {
        this.encoding = encoding;
        this.bufferSize = buf_size;
    }


    /**
     * 文字コード変換ファイル出力処理
     * 
     * @param output
     * @param line
     * @throws IOException
     */
    public void convert(File output, String line) throws IOException {
        FileOutputStream fos = new FileOutputStream(output);
        try {
            convert(fos, line);
        } finally {
            fos.close();
        }
    }


    /**
     * 文字コード変換ファイル出力処理
     * 
     * @param line
     *            入力文字列
     * @return 文字コード変換済み文字列のバイト配列
     */
    public byte[] convert(String line) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);
        try {
            convert(baos, line);
            return baos.toByteArray();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 
     * 文字コード変換ストリーム出力処理
     * 
     * @param os
     *            出力ストリーム
     * @param line
     *            入力文字列
     */
    public void convert(OutputStream os, String line) {

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(os, encoding.toString()));
            pw.println(line);
            pw.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
}
