/**
 * @version $Id: CorrectionPreferenceInitializer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2012/02/17 16:56:00
 * @author kousuke-morishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.tida_okinawa.corona.correction.data.CoronaDocumentInformation;

/**
 * @author kousuke-morishima
 */
public class CorrectionPreferenceInitializer extends AbstractPreferenceInitializer {

    /**
     * デフォルトコンストラクター。処理なし
     */
    public CorrectionPreferenceInitializer() {
    }

    /**
     * 処理スレッド数
     */
    final static public String PREF_NUM_THREADS = "NUM_THREADS"; //$NON-NLS-1$

    /**
     * 誤記補正時、空白の除去を行うかどうか。 {@link #ERASE_ALL_SPACES},
     * {@link #ERASE_VERBOSE_SPACES}, {@link #ERASE_SPACES_NO}
     */
    public static final String PREF_ERRATUM_SPACES = "prefErratumSpaces"; //$NON-NLS-1$
    /**
     * すべての空白を除去する<br/>
     * {@link #PREF_ERRATUM_SPACES}の値
     */
    public static final String ERASE_ALL_SPACES = "0"; //$NON-NLS-1$
    /**
     * 冗長な空白のみを除去する<br/>
     * {@link #PREF_ERRATUM_SPACES}の値
     */
    public static final String ERASE_VERBOSE_SPACES = "1"; //$NON-NLS-1$
    /**
     * 空白を除去しない<br/>
     * {@link #PREF_ERRATUM_SPACES}の値
     */
    public static final String ERASE_SPACES_NO = "2"; //$NON-NLS-1$

    /**
     * 連語抽出時、単語が何度ヒットすれば抽出対象とするか。 {@link #PREF_COLLOCATION_WORD}の値
     */
    public static final String PREF_COLLOCATION_WORD = "prefCollocationWord"; //$NON-NLS-1$

    /**
     * 連語ヒット回数デフォルト値<br/>
     * {@link #PREF_COLLOCATION_WORD}の値
     */
    public static final String COLLOCATION_DEFAULT_NUMBER = "5"; //$NON-NLS-1$

    /** ドキュメント情報 */
    public static final String PREF_DOCCUMENT_INFORMATION = "prefDocumentInformation"; //$NON-NLS-1$
    /** 定義を保存した時に選択していた定義のインデックス */
    public static final String PREF_SELECTED_DOCUMENT_INFORMATION = "prefSelectedDocumentInformation"; //$NON-NLS-1$
    /** ドキュメント分割情報のデフォルト */
    public static final String PREF_DOCCUMENT_INFOMATION_DEFAULT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //$NON-NLS-1$
            "<java version=\"1.7.0_03\" class=\"java.beans.XMLDecoder\">" + //$NON-NLS-1$
            "<object class=\"java.util.ArrayList\">" + //$NON-NLS-1$
            "<void method=\"add\">" + //$NON-NLS-1$
            "<object class=\"com.tida_okinawa.corona.correction.data.CoronaDocumentInformation\">" + //$NON-NLS-1$
            "<void property=\"definitions\">" + //$NON-NLS-1$
            "<object class=\"java.util.ArrayList\">" + //$NON-NLS-1$
            "<void method=\"add\">" + //$NON-NLS-1$
            " <object class=\"com.tida_okinawa.corona.correction.data.CoronaDocumentDefinition\">" + //$NON-NLS-1$
            "<void property=\"definition\">" + //$NON-NLS-1$
            " <string>(${num})</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void method=\"add\">" + //$NON-NLS-1$
            " <object class=\"com.tida_okinawa.corona.correction.data.CoronaDocumentDefinition\">" + //$NON-NLS-1$
            "<void property=\"definition\">" + //$NON-NLS-1$
            " <string>${num}.</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void method=\"add\">" + //$NON-NLS-1$
            " <object class=\"com.tida_okinawa.corona.correction.data.CoronaDocumentDefinition\">" + //$NON-NLS-1$
            "<void property=\"definition\">" + //$NON-NLS-1$
            " <string>${num}-</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void method=\"add\">" + //$NON-NLS-1$
            " <object class=\"com.tida_okinawa.corona.correction.data.CoronaDocumentDefinition\">" + //$NON-NLS-1$
            "<void property=\"definition\">" + //$NON-NLS-1$
            " <string>${num})</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void method=\"add\">" + //$NON-NLS-1$
            " <object class=\"com.tida_okinawa.corona.correction.data.CoronaDocumentDefinition\">" + //$NON-NLS-1$
            "<void property=\"definition\">" + //$NON-NLS-1$
            " <string>(${kan})</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void method=\"add\">" + //$NON-NLS-1$
            " <object class=\"com.tida_okinawa.corona.correction.data.CoronaDocumentDefinition\">" + //$NON-NLS-1$
            "<void property=\"definition\">" + //$NON-NLS-1$
            " <string>(${abc})</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void method=\"add\">" + //$NON-NLS-1$
            " <object class=\"com.tida_okinawa.corona.correction.data.CoronaDocumentDefinition\">" + //$NON-NLS-1$
            "<void property=\"definition\">" + //$NON-NLS-1$
            " <string>${num}_</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void method=\"add\">" + //$NON-NLS-1$
            " <object class=\"com.tida_okinawa.corona.correction.data.CoronaDocumentDefinition\">" + //$NON-NLS-1$
            "<void property=\"definition\">" + //$NON-NLS-1$
            " <string>●○■□◆◇★・＊※</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void property=\"type\">" + //$NON-NLS-1$
            " <int>0</int>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void property=\"name\">" + //$NON-NLS-1$
            " <string>デフォルト</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void method=\"add\">" + //$NON-NLS-1$
            "<object class=\"com.tida_okinawa.corona.correction.data.CoronaDocumentInformation\">" + //$NON-NLS-1$
            "<void property=\"name\">" + //$NON-NLS-1$
            "<string>PDF</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "<void property=\"extension\">" + //$NON-NLS-1$
            "<string>*.pdf</string>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            "</object>" + //$NON-NLS-1$
            "</void>" + //$NON-NLS-1$
            " </object>" + //$NON-NLS-1$
            "</java>"; //$NON-NLS-1$


    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = CorrectionActivator.getDefault().getPreferenceStore();
        final int num_proccessors = Runtime.getRuntime().availableProcessors();
        store.setDefault(PREF_NUM_THREADS, num_proccessors);
        store.setDefault(PREF_ERRATUM_SPACES, ERASE_VERBOSE_SPACES);
        store.setDefault(PREF_COLLOCATION_WORD, COLLOCATION_DEFAULT_NUMBER);
        store.setDefault(PREF_DOCCUMENT_INFORMATION, PREF_DOCCUMENT_INFOMATION_DEFAULT);
        store.setDefault(PREF_SELECTED_DOCUMENT_INFORMATION, 0);
    }


    /**
     * ドキュメント情報リストデフォルト値取得<br/>
     * 
     * @return ドキュメント情報リスト
     */
    public static List<CoronaDocumentInformation> getDefaultDocumentInfomations() {
        IPreferenceStore store = CorrectionActivator.getDefault().getPreferenceStore();
        return toList(store.getDefaultString(PREF_DOCCUMENT_INFORMATION));
    }


    /**
     * ドキュメント情報リスト取得<br/>
     * XMLに変換した文字列でオブジェクトを保存しているため、専用IFにより入出力を行うこと
     * 
     * @return ドキュメント情報リスト
     */
    public static List<CoronaDocumentInformation> getDocumentInfomations() {
        IPreferenceStore store = CorrectionActivator.getDefault().getPreferenceStore();
        String data = store.getString(PREF_DOCCUMENT_INFORMATION);
        if (data.isEmpty()) {
            return new ArrayList<CoronaDocumentInformation>();
        }
        return toList(data);
    }


    /**
     * ドキュメント情報リスト設定<br/>
     * オブジェクトをXMLに変換し、保存する。
     * 
     * @param infos
     *            ドキュメント情報リスト
     */
    public static void setDocumentInfomations(List<CoronaDocumentInformation> infos) {
        IPreferenceStore store = CorrectionActivator.getDefault().getPreferenceStore();
        String data = toXML(infos);
        store.setValue(PREF_DOCCUMENT_INFORMATION, data);
    }


    /**
     * XML化<br/>
     * オブジェクトをXMLに変換する
     * 
     * @param object
     *            対象オブジェクト
     * @return 変換後XML
     */
    public static synchronized String toXML(Object object) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(out);
        enc.writeObject(object);
        enc.close();
        return out.toString();
    }


    /**
     * オブジェクト化<br/>
     * XMLをオブジェクトに変換する
     * 
     * @param data
     *            入力XML
     * @return 変換後オブジェクト
     *         変換可能なオブジェクト情報が入力XMLに含まれていなかったらnull
     */
    static Object toObject(String data) {
        Object ret = null;
        XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(data.getBytes()));
        try {
            ret = d.readObject();
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        d.close();
        return ret;
    }


    private static List<CoronaDocumentInformation> toList(String data) {
        Object obj = toObject(data);
        List<CoronaDocumentInformation> ret = new ArrayList<CoronaDocumentInformation>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                if (o instanceof CoronaDocumentInformation) {
                    ret.add((CoronaDocumentInformation) o);
                }
            }
        }
        return ret;
    }
}
