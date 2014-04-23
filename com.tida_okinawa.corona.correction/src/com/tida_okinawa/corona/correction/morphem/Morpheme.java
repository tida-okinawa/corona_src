/**
 * @version $Id: Morpheme.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/26 17:26:01
 * @author sanenori-makiya
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.correction.common.ExternalProgramExitException;
import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;

/**
 * @author sanenori-makiya
 */
public class Morpheme {

    final MorphemeRelationProcessor mp;


    /**
     * 
     * @param mp
     *            Juman/KNPの実行処理
     */
    public Morpheme(MorphemeRelationProcessor mp) {
        this.mp = mp;
    }


    /**
     * 形態素解析（句点で区切って処理する）
     * 
     * @param text
     *            must not null
     * @param doKnp
     *            係り受け解析を行う
     * @param kuten
     *            句点
     * @param err
     *            エラー出力先
     * @return 形態素解析結果
     */
    public List<String> process(String text, boolean doKnp, OutputStream err, String... kuten) {
        return process(text, doKnp, err, MorphemePreference.convSJIS(), kuten);
    }


    /**
     * 形態素解析（句点で区切って処理する）
     * 
     * @param text
     *            must not null
     * @param doKnp
     *            係り受け解析を行う
     * @param kuten
     *            句点
     * @param err
     *            エラー出力先
     * @param convSJIS
     *            入出力ストリームをSJIS(MS932)で行うかどうか
     * @return 形態素解析結果
     */
    public List<String> process(String text, boolean doKnp, OutputStream err, boolean convSJIS, String... kuten) {
        // 句点や"？"を改行に置き換える
        // note: 長い文のままだとknpでメモリ不足になる
        for (String k : kuten) {
            text = text.replaceAll(k, k + "\n"); //$NON-NLS-1$
        }

        List<String> morphemeRelationCorrectionList = process(text, doKnp, convSJIS, err);

        return morphemeRelationCorrectionList;
    }


    /**
     * 形態素解析
     * 
     * @param text
     *            解析対象テキスト
     * @param doKnp
     *            係り受け解析を実行する場合、true
     * @param convSJIS
     *            入出力ストリームをSJIS(MS932)に変換するならtrue
     * @param err
     *            エラー出力用ストリーム
     * @return 形態素解析結果
     */
    public List<String> process(String text, boolean doKnp, boolean convSJIS, OutputStream err) {
        /* UIへ返却するリスト */
        List<String> morphemeRelationCorrectionList = new ArrayList<String>();

        /* 形態素、係り受け解析実行 */
        try {
            // 文末が改行でないとJumanが終わらない?
            if (!text.endsWith("\n")) { //$NON-NLS-1$
                text += "\n"; //$NON-NLS-1$
            }

            byte[] inputBytes;
            if (convSJIS) {
                inputBytes = text.getBytes(Encoding.MS932.toString());
            } else {
                inputBytes = text.getBytes();
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            //	ByteArrayOutputStream error = new ByteArrayOutputStream(); 
            ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);

            mp.exec(input, output, err, doKnp);


            /* 形態素、係り受け解析結果修正クラスのインスタンス化 */
            MorphemeRelationReader mrr = new MorphemeRelationReader();

            /* 形態素、係り受け解析結果修正 (格解析抽出) */
            //            ByteArrayInputStream result_bais = new ByteArrayInputStream(result_baos.toByteArray());
            //            mrr.read(result_bais, Encoding.MS932);
            InputStreamReader output_r;

            if (convSJIS) {
                output_r = new InputStreamReader(new ByteArrayInputStream(output.toByteArray()), Encoding.MS932.toString());
            } else {
                output_r = new InputStreamReader(new ByteArrayInputStream(output.toByteArray()));
            }
            mrr.read(new BufferedReader(output_r));
            morphemeRelationCorrectionList = mrr.getResultList();

        } catch (IOException e) {
            e.printStackTrace(System.err);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        } catch (ExternalProgramExitException e) {
            e.printStackTrace(System.err);
        }
        return morphemeRelationCorrectionList;
    }

}
