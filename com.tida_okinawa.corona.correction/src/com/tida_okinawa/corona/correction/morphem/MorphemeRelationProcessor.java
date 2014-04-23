/**
 * @version $Id: MorphemeRelationProcessor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/02 20:34:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.tida_okinawa.corona.correction.common.ExternalProgramExec;
import com.tida_okinawa.corona.correction.common.ExternalProgramExitException;
import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;

/**
 * 
 * 形態素解析, 係り受けを実行
 * <p>
 * サーバー/クライアントモードに対応
 * 
 * @author imai
 */
public class MorphemeRelationProcessor {

    /**
     * KNPのタイムアウト時間
     * 
     * TODO: プリファレンスで調整できるようにする
     */
    static final int KNP_TIMEOUT = 200;

    final int threadId;

    final String[] jumanCmdLine;

    final String[] knpCmdLine;

    String bundleLocation;


    /**
     * 各スレッドのJuman/KNP 実行処理
     * 
     * @param threadId
     *            スレッド番号
     */
    public MorphemeRelationProcessor(int threadId) {
        this.threadId = threadId;
        this.jumanCmdLine = MorphemePreference.getJumanCmdLine(threadId);
        this.knpCmdLine = MorphemePreference.getKnpCmdLine(threadId);
    }


    /**
     * 自動実行用
     * 
     * @param threadId
     *            スレッド番号
     * @param bundleLocation
     *            Juman/KNPのバンドル先
     */
    public MorphemeRelationProcessor(int threadId, String bundleLocation) {
        this.threadId = threadId;
        this.jumanCmdLine = new String[] { bundleLocation + "juman7\\juman.bat" }; //$NON-NLS-1$
        this.knpCmdLine = new String[] { bundleLocation + "knp4\\knp.bat" }; //$NON-NLS-1$
        this.bundleLocation = bundleLocation;
    }


    /**
     * 形態素・係り受け解析
     * 
     * @param in
     * @param out
     * @param err
     * @param knp
     *            係り受け解析をするか
     * @throws IOException
     * @throws FileNotFoundException
     * @throws InterruptedException
     * @throws ExternalProgramExitException
     */
    public void exec(InputStream in, OutputStream out, OutputStream err, boolean knp) throws IOException, FileNotFoundException, InterruptedException,
            ExternalProgramExitException {
        /**
         * note: juman, knp の設定ファイルの都合で、
         * 実行時は tool_path をカレントディレクトリにして、
         * 以下のようなファイルの配置を前提にする
         * 
         * (tool_path)/
         * - juman/
         * - - dic/
         * - - juman.exe
         * - knp/
         * - - dic/
         * - - rule/
         * - - knp.exe
         */

        if (knp) {
            /* 形態素解析処理 */
            ByteArrayOutputStream juman_out = new ByteArrayOutputStream();
            execJuman(in, juman_out, err);

            /* 係り受け解析処理 */
            ByteArrayInputStream knp_in = new ByteArrayInputStream(juman_out.toByteArray());
            execKnp(knp_in, out, err);
        } else {
            /* 形態素解析処理 */
            execJuman(in, out, err);
        }
    }


    /**
     * Juman実行
     * 
     * @param in
     *            入力
     * @param out
     *            出力
     * @param err
     *            エラー
     * @throws IOException
     * @throws InterruptedException
     * @throws ExternalProgramExitException
     */
    public void execJuman(InputStream in, OutputStream out, OutputStream err) throws IOException, InterruptedException, ExternalProgramExitException {
        // 公開されているWindows版は, INIファイルを使うので、INIファイルがあるディレクトリをカレントディレクトリにする
        File dir;
        if (bundleLocation != null) {
            dir = new File(bundleLocation);
        } else {
            dir = MorphemePreference.getJumanIniDir();
        }

        exec(jumanCmdLine, in, out, err, dir);
    }


    /**
     * KNP実行
     * 
     * @param in
     *            入力
     * @param out
     *            出力
     * @param err
     *            エラー
     * @throws IOException
     * @throws InterruptedException
     * @throws ExternalProgramExitException
     */
    public void execKnp(InputStream in, OutputStream out, OutputStream err) throws IOException, InterruptedException, ExternalProgramExitException {
        // 公開されているWindows版は, INIファイルを使うので、INIファイルがあるディレクトリをカレントディレクトリにする
        File dir;
        if (bundleLocation != null) {
            dir = new File(bundleLocation);
        } else {
            dir = MorphemePreference.getKnpIniDir();
        }
        exec(knpCmdLine, in, out, err, dir);
    }


    /**
     * 外部プログラム実行
     * 
     * @param args
     *            コマンドライン
     * @param in
     *            入力
     * @param out
     *            出力
     * @param err
     *            エラー
     * @throws IOException
     * @throws InterruptedException
     * @throws ExternalProgramExitException
     */
    private static void exec(String[] args, InputStream in, OutputStream out, OutputStream err, File dir) throws IOException, InterruptedException,
            ExternalProgramExitException {
        ExternalProgramExec pep = new ExternalProgramExec();
        pep.exec(args, dir, in, out, err);
    }
}
