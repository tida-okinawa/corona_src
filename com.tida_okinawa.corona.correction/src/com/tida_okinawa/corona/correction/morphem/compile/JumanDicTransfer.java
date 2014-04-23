/**
 * @version $Id: JumanDicTransfer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/05 13:00:08
 * @author kobayashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem.compile;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;

import com.tida_okinawa.corona.common.ILogger;
import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;

/**
 * Juman辞書転送機能（サーバーモード）
 * 
 * @author kobayashi
 * 
 */
public class JumanDicTransfer {

    /**
     * 辞書ファイルの一覧
     * <p>
     * 設定では変わらないので static
     */
    final static String[] LOCAL_FILES = { "jumandic.dat", "jumandic.mat", "jumandic.pat", "jumandic.tab", "JUMAN.connect", "JUMAN.grammar", "JUMAN.kankei",
            "JUMAN.katuyou" };

    /**
     * サーバー接続関連（USERNAME, PASSWD, HOSTNAME)
     * <p>
     * 設定を変更後に反映したいので、staticにはしない
     */
    final IPreferenceStore STORE = MorphemePreference.getPreferenceStore();
    final String USERNAME = STORE.getString(MorphemePreference.PREF_SERVER_USERNAME);
    final String PASSWD = STORE.getString(MorphemePreference.PREF_SERVER_PASSWD);
    final String[] HOSTNAME = MorphemePreference.getJumanHostNames();


    /**
     * 転送ファイル関連（LOCAL_DIR, REMOTE_DIR)
     * <p>
     * 設定を変更後に反映したいので、staticにはしない
     */
    final String LOCAL_DIR = STORE.getString(MorphemePreference.PREF_JUMAN_DIC_DIR);
    final String REMOTE_DIR = "/home/" + USERNAME + "/dic/";


    /**
     * 辞書転送機能(自動転送)
     * 辞書コンパイル、ターゲット切り替え時(CleansingMorphemeHandlerから呼び出し)
     * 
     * @throws IOException
     */
    public void dicTrancefer(ILogger logger) throws IOException {
        boolean useserver = STORE.getBoolean(MorphemePreference.PREF_SERVER_MODE);
        if (useserver == true) {
            for (String hostname : HOSTNAME) {

                SshClient client = new SshClient(hostname, USERNAME, PASSWD);
                try {
                    for (String local_file : LOCAL_FILES) {
                        client.upload(LOCAL_DIR + File.separator + local_file, REMOTE_DIR);
                    }
                    logger.getOutStream().println("辞書転送に成功しました:(" + hostname + ")");
                } catch (Exception e) {
                    logger.getErrStream().println("辞書転送に失敗しました:(" + hostname + ")");
                } finally {
                    client.finalize();
                }
            }
        }
    }
}