package com.tida_okinawa.corona.correction.morphem.preference;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import com.tida_okinawa.corona.correction.CorrectionActivator;
import com.tida_okinawa.corona.correction.external.ExternalActivator;

/**
 * 形態素・係り受け解析のプリファレンス
 * 
 * @author imai
 * 
 */
public class MorphemePreference extends AbstractPreferenceInitializer {
    /**
     * 係り受け解析(KNP)を実行するか
     */
    final public static String PREF_DO_KNP = "DO_KNP"; //$NON-NLS-1$


    /**
     * サーバーモードのON/OFF
     */
    final public static String PREF_SERVER_MODE = "PREF_SERVER_MODE"; //$NON-NLS-1$

    /**
     * Jumanサーバー設定（サーバーモード時のみ)
     */
    final public static String PREF_JUMAN_SERVER_CONF = "PREF_JUMAN_SERVER_CONF"; //$NON-NLS-1$

    /**
     * Jumanプログラムのパス
     */
    final static String PREF_JUMAN_PROGRAM_PATH = "PREF_JUMAN_PROGRAM_PATH"; //$NON-NLS-1$

    /**
     * Jumanオプション
     */
    final static String PREF_JUMAN_PROGRAM_OPT = "PREF_JUMAN_PROGRAM_OPT"; //$NON-NLS-1$

    /**
     * Juman 辞書ディレクトリ
     */
    final public static String PREF_JUMAN_DIC_DIR = "PREF_JUMAN_DIC_DIR"; //$NON-NLS-1$

    /**
     * KNPサーバー設定（サーバーモード時のみ)
     */
    final static String PREF_KNP_SERVER_CONF = "PREF_KNP_SERVER_CONF"; //$NON-NLS-1$

    /**
     * KNPプログラムのパス
     */
    final static String PREF_KNP_PROGRAM_PATH = "PREF_KNP_PROGRAM_PATH"; //$NON-NLS-1$

    /**
     * KNPオプション
     */
    final static String PREF_KNP_PROGRAM_OPT = "PREF_KNP_PROGRAM_OPT"; //$NON-NLS-1$


    /**
     * 実行ディレクトリ (knp.iniファイルの場所)
     * <p>
     * サーバーモード対応版ではいらない
     */
    final static public String PREF_JUMAN_INI_DIR = "PREF_JUMAN_INI_DIR"; //$NON-NLS-1$

    /**
     * 実行ディレクトリ (juman.iniファイルの場所)
     * <p>
     * サーバーモード対応版ではいらない
     */
    final static public String PREF_KNP_INI_DIR = "PREF_KNP_INI_DIR"; //$NON-NLS-1$

    /**
     * SJIS変換
     * <p>
     * サーバーモード対応版ではいらない
     */
    final static String PREF_CONV_SJIS = "PREF_CONV_SJIS"; //$NON-NLS-1$

    /**
     * サーバーのユーザー名
     * <p>
     * 全サーバーで共通
     */
    final public static String PREF_SERVER_USERNAME = "PREF_SERVER_USERNAME"; //$NON-NLS-1$

    /**
     * サーバーのパスワード
     * <p>
     * 全サーバーで共通
     */
    final public static String PREF_SERVER_PASSWD = "PREF_SERVER_PASSWD";


    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = getPreferenceStore();
        setDefaults(store);
    }


    /**
     * プリファレンスの初期値設定
     * 
     * @param store
     */
    static private void setDefaults(IPreferenceStore store) {
        store.setDefault(PREF_DO_KNP, true);
        store.setDefault(PREF_SERVER_MODE, false);

        // juman ver.7, knp ver.4 Windows版での設定)
        store.setDefault(PREF_JUMAN_PROGRAM_OPT, "-r jumanrc.win"); //$NON-NLS-1$
        store.setDefault(PREF_KNP_PROGRAM_OPT, "-tab -r knprc.win"); //$NON-NLS-1$

        // サーバーモード時有効
        store.setDefault(PREF_JUMAN_SERVER_CONF, "localhost"); //$NON-NLS-1$
        store.setDefault(PREF_KNP_SERVER_CONF, "localhost"); //$NON-NLS-1$

        // Juman, KNP をバンドルしているプラグイン
        String basePath = bundlePath(""); //$NON-NLS-1$
        if (basePath == null || basePath.isEmpty()) {
            // Eclipse以外での起動の場合(バンドルのパスが取得できない)、PATHが通っている前提
            store.setDefault(PREF_JUMAN_PROGRAM_PATH, "juman.exe"); //$NON-NLS-1$
            store.setDefault(PREF_KNP_PROGRAM_PATH, "knp.exe"); //$NON-NLS-1$
        } else {
            // juman ver.7, knp ver.4 Windows版での設定)
            store.setDefault(PREF_JUMAN_INI_DIR, bundlePath("juman7"));
            store.setDefault(PREF_KNP_INI_DIR, bundlePath("knp4"));
            store.setDefault(PREF_JUMAN_PROGRAM_PATH, bundlePath("juman7/juman.exe")); //$NON-NLS-1$
            store.setDefault(PREF_KNP_PROGRAM_PATH, bundlePath("knp4/knp.exe")); //$NON-NLS-1$
            store.setDefault(PREF_JUMAN_DIC_DIR, bundlePath("juman7/dic"));
            // 旧バージョンでの設定
            //            store.setDefault(PREF_JUMAN_INI_DIR, basePath);
            //            store.setDefault(PREF_KNP_INI_DIR, basePath);
            //            store.setDefault(PREF_JUMAN_PROGRAM_PATH, bundlePath("juman/juman.exe")); //$NON-NLS-1$
            //            store.setDefault(PREF_KNP_PROGRAM_PATH, bundlePath("knp/knp.exe")); //$NON-NLS-1$
            //            store.setDefault(PREF_JUMAN_DIC_DIR, bundlePath("juman/dic"));
        }
    }


    /**
     * プリファレンスを取得
     * 
     * @return
     */
    public static IPreferenceStore getPreferenceStore() {
        CorrectionActivator plugin = CorrectionActivator.getDefault();
        if (plugin != null) {
            return plugin.getPreferenceStore();
        }

        // デフォルト値を返す。スクリプト用に暫定
        IPreferenceStore store = new PreferenceStore();
        setDefaults(store);
        return store;
    }


    /**
     * Juman, KNP をバンドルしているプラグインからファイルパスを取得
     * 
     * @param entry
     * @return
     */
    public static String bundlePath(String entry) {
        Plugin plugin = ExternalActivator.getDefault();
        if (plugin == null) {
            // Eclipse以外から実行している
            return null;
        }

        try {
            URL url = FileLocator.toFileURL(plugin.getBundle().getEntry(entry));
            File file = new File(url.toURI());

            return file.getAbsolutePath();
        } catch (Exception e) {
            return ""; //$NON-NLS-1$
        }
    }


    /**
     * KNPを実行するか
     * 
     * @return
     */
    static public boolean doKnp() {
        IPreferenceStore store = getPreferenceStore();
        return store.getBoolean(PREF_DO_KNP);
    }


    /**
     * ShiftJIS (MS932) に変換するか
     * <p>
     * 配布されているWindows版用
     * 
     * @return
     */
    static public boolean convSJIS() {
        IPreferenceStore store = getPreferenceStore();
        return store.getBoolean(PREF_CONV_SJIS);
    }


    /**
     * サーバーを利用するか
     * 
     * @return
     */
    static public boolean useSever() {
        IPreferenceStore store = getPreferenceStore();
        return store.getBoolean(PREF_SERVER_MODE);
    }


    /**
     * juman.ini があるディレクトリ
     * <p>
     * 配布されているWindows版用
     * 
     * @return
     */
    static public File getJumanIniDir() {
        IPreferenceStore store = getPreferenceStore();
        String path = store.getString(PREF_JUMAN_INI_DIR);
        File dir = new File(path);
        if (dir.isDirectory()) {
            return dir;
        }
        return null;
    }


    /**
     * knp.ini があるディレクトリ
     * <p>
     * 配布されているWindows版用
     * 
     * @return
     */
    static public File getKnpIniDir() {
        IPreferenceStore store = getPreferenceStore();
        String path = store.getString(PREF_KNP_INI_DIR);
        File dir = new File(path);
        if (dir.isDirectory()) {
            return dir;
        }
        return null;
    }


    static public File getJumanDicDir() {
        IPreferenceStore store = getPreferenceStore();
        String path = store.getString(PREF_JUMAN_DIC_DIR);
        File dir = new File(path);
        if (dir.isDirectory()) {
            return dir;
        }
        return null;
    }


    static public String getServerUsername() {
        IPreferenceStore store = getPreferenceStore();
        String s = store.getString(PREF_SERVER_USERNAME);
        return s;
    }


    static public String getServerPasswd() {
        IPreferenceStore store = getPreferenceStore();
        String s = store.getString(PREF_SERVER_PASSWD);
        return s;
    }


    /**
     * Jumanサーバーのホスト名を取得
     * 
     * @return
     */
    static public String[] getJumanHostNames() {
        IPreferenceStore store = getPreferenceStore();
        ServerSetting setting = new ServerSetting(store.getString(PREF_JUMAN_SERVER_CONF));
        return setting.getHostNames();
    }


    /**
     * KNPサーバーのホスト名を取得
     * 
     * @return
     */
    static public String[] getKnpHostNames() {
        IPreferenceStore store = getPreferenceStore();
        ServerSetting setting = new ServerSetting(store.getString(PREF_KNP_SERVER_CONF));
        return setting.getHostNames();
    }


    /**
     * Juman実行のコマンドライン
     * 
     * @param threadId
     *            スレッド番号
     * @return
     */
    static public String[] getJumanCmdLine(int threadId) {
        IPreferenceStore store = getPreferenceStore();
        String opt = store.getString(PREF_JUMAN_PROGRAM_OPT);
        ExternalProgramSetting eps = new ExternalProgramSetting(store.getString(PREF_JUMAN_PROGRAM_PATH), opt);

        if (store.getBoolean(PREF_SERVER_MODE)) {
            String[] serverOpt = new ServerSetting(store.getString(PREF_JUMAN_SERVER_CONF)).getServerOpts(threadId);
            return eps.getCommandLine(serverOpt);
        } else {
            return eps.getCommandLine();
        }

    }


    /**
     * KNP実行のコマンドライン
     * 
     * @param threadId
     *            スレッド番号
     * @return
     */
    static public String[] getKnpCmdLine(int threadId) {
        IPreferenceStore store = getPreferenceStore();
        String opt = store.getString(PREF_KNP_PROGRAM_OPT);
        ExternalProgramSetting eps = new ExternalProgramSetting(store.getString(PREF_KNP_PROGRAM_PATH), opt);

        if (store.getBoolean(PREF_SERVER_MODE)) {
            String[] serverOpt = new ServerSetting(store.getString(PREF_KNP_SERVER_CONF)).getServerOpts(threadId);
            return eps.getCommandLine(serverOpt);
        } else {
            return eps.getCommandLine();
        }
    }


    /**
     * スレッド数
     * 
     * @return -1: サーバーモード以外
     */
    static public int getJumanServerNumber() {
        IPreferenceStore store = getPreferenceStore();
        if (useSever()) {
            return new ServerSetting(store.getString(PREF_JUMAN_SERVER_CONF)).getNumOfServers();
        }
        return -1;
    }


    /**
     * スレッド数
     * 
     * @return -1: サーバーモード以外
     */
    static public int getKnpServerNumber() {
        IPreferenceStore store = getPreferenceStore();
        if (useSever()) {
            return new ServerSetting(store.getString(PREF_KNP_SERVER_CONF)).getNumOfServers();
        }
        return -1;
    }
}
