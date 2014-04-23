/**
 * @version $Id: PreferenceUtils.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/21 15:18:13
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.ui.PreferenceInitializer;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.data.ConnectionParameter;

/**
 * @author shingo-takahashi
 */
public class PreferenceUtils {
    static IPreferenceStore store = UIActivator.getDefault().getPreferenceStore();


    /**
     * 接続パラメータ取得
     * 
     * <pre>
     * 接続情報を取得できなかった場合、エラーログを出力して空のリストを返す
     * </pre>
     * 
     * @return 接続パラメータリスト
     */
    public static List<ConnectionParameter> getConnectionParameters() {
        try {
            List<ConnectionParameter> cpList = new ArrayList<ConnectionParameter>();
            String[] nameList = asArray(store.getString(PreferenceInitializer.PREF_DB_NAME));
            String[] urlList = asArray(store.getString(PreferenceInitializer.PREF_DB_CONNECTER));
            String[] userList = asArray(store.getString(PreferenceInitializer.PREF_DB_USER));
            String[] passwdList = asArray(store.getString(PreferenceInitializer.PREF_DB_PW));

            for (int i = 0; i < nameList.length; i++) {
                cpList.add(new ConnectionParameter(nameList[i], urlList[i], userList[i], passwdList[i]));
            }
            return cpList;
        } catch (Exception e) {
            CoronaActivator.log(new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, "データベース接続情報を取得できませんでした。", e), false);
            return new ArrayList<ConnectionParameter>();
        }

    }


    public static ConnectionParameter getCurrentConnectionParameter() {
        List<ConnectionParameter> parameters = getConnectionParameters();
        int connectionIndex = getConnectionIndex();
        if (parameters.size() > connectionIndex) {
            return parameters.get(connectionIndex);
        }
        return null;
    }


    /**
     * @return
     */
    public static List<ConnectionParameter> getDefaultConnectionParameters() {
        List<ConnectionParameter> cpList = new ArrayList<ConnectionParameter>();

        String[] nameList = StringConverter.asArray(store.getDefaultString(PreferenceInitializer.PREF_DB_NAME));
        String[] urlList = StringConverter.asArray(store.getDefaultString(PreferenceInitializer.PREF_DB_CONNECTER));
        String[] userList = StringConverter.asArray(store.getDefaultString(PreferenceInitializer.PREF_DB_USER));
        String[] passwdList = StringConverter.asArray(store.getDefaultString(PreferenceInitializer.PREF_DB_PW));

        for (int i = 0; i < nameList.length; i++) {
            cpList.add(new ConnectionParameter(nameList[i], urlList[i], userList[i], passwdList[i]));
        }
        return cpList;
    }


    /**
     * @param cpList
     *            接続パラメータリスト
     */
    public static void setConnectionParameters(List<ConnectionParameter> cpList) {
        StringBuilder name = new StringBuilder(128);
        StringBuilder param = new StringBuilder(128);
        StringBuilder user = new StringBuilder(128);
        StringBuilder pass = new StringBuilder(128);

        if (cpList.size() > 0) {
            for (ConnectionParameter cp : cpList) {
                name.append(",").append(cp.name);
                param.append(",").append(cp.url);
                user.append(",").append(cp.user);
                pass.append(",").append(cp.passwd);
            }
            /* 先頭のカンマを削る */
            name.deleteCharAt(0);
            param.deleteCharAt(0);
            user.deleteCharAt(0);
            pass.deleteCharAt(0);
        }

        store.setValue(PreferenceInitializer.PREF_DB_NAME, name.toString());
        store.setValue(PreferenceInitializer.PREF_DB_CONNECTER, param.toString());
        store.setValue(PreferenceInitializer.PREF_DB_USER, user.toString());
        store.setValue(PreferenceInitializer.PREF_DB_PW, pass.toString());
    }


    /**
     * 接続パラメータインデックス取得
     * 
     * @return アクティブな接続パラメータのインデックス
     */
    public static int getConnectionIndex() {
        return store.getInt(PreferenceInitializer.PREF_DB_INDEX);
    }


    /**
     * 
     * @return 接続パラメータのインデックス
     */
    public static int getDefaultConnectionIndex() {
        return store.getDefaultInt(PreferenceInitializer.PREF_DB_INDEX);
    }


    /**
     * 接続パラメータインデックス設定
     * 
     * @param index
     */
    public static void setConnectionIndex(int index) {
        store.setValue(PreferenceInitializer.PREF_DB_INDEX, index);
    }


    /**
     * ワークスペース設定で係り受け解析を行うかどうかチェックする
     * 
     * @return 係り受け解析を行う設定であれば TRUE
     */
    public static boolean isDoKnp() {
        return MorphemePreference.doKnp();
    }


    /**
     * 配列変換
     * 
     * @param value
     * @return
     */
    private static String[] asArray(String value) {
        /* パスワードに空白入力を許容するため、StringTokenizerからsplitに変えた */
        String[] splits = value.split(",", -1);
        return splits;
    }
}
