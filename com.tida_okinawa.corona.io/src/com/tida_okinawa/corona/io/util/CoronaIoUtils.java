/**
 * @version $Id: CoronaIoUtils.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/01 21:04:24
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.util;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.dam.hibernate.UserDic;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.table.TableType;

/**
 * CoronaIO用ユーティリティクラス
 * 
 * @author shingo-takahashi
 */
public class CoronaIoUtils {

    /**
     * 数字をカンマで区切った文字列をIntegerのコレクション(Set)に変換する.
     * ただし変換後の数値が0である場合はSetに格納されない
     * 
     * @param str
     *            変換対象の数字カンマ区切り文字列
     * @return 変換されたInteger型のSet
     */
    public static Set<Integer> stringToIntSet(String str) {
        Set<Integer> list = new HashSet<Integer>();
        String[] strs = str.split(","); //$NON-NLS-1$

        for (int i = 0; i < strs.length; i++) {
            if (strs[i].isEmpty()) {
                continue;
            }
            try {
                if (Integer.parseInt(strs[i]) == 0) {
                    continue;
                }
                list.add(Integer.valueOf(strs[i]));
            } catch (NumberFormatException e) {
            }
        }
        return list;
    }


    /**
     * Integer型を格納したListを、数字をカンマ区切りにした文字列に変換する
     * 
     * @param list
     *            変換対象のInteger型のList.
     * @return 変換されたカンマ区切り文字列。変換対象となるListに問題がある場合空文字を返す。
     */
    public static String intListToString(Collection<Integer> list) {
        if ((list == null) || list.size() == 0)
            return ""; //$NON-NLS-1$

        StringBuilder str = new StringBuilder(20);
        for (Integer i : list) {
            str.append(",").append(String.format("%03d", i)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return str.substring(1);
    }


    /**
     * エラーログ表示（単一）
     * 
     * @param status
     * @param str
     * @param exception
     * @see IStatus
     */
    public static void setErrorLog(int status, String str, Throwable exception) {
        ILog logger = IoActivator.getDefault().getLog();
        IStatus sts = new Status(status, IoActivator.getPluginId(), str, exception);
        logger.log(sts);
    }


    /**
     * エラーログ表示（複数）
     * 
     * @param status
     * @param title
     * @param strs
     * @param exception
     * @see IStatus
     */
    public static void setErrorLogs(int status, String title, List<String> strs, Throwable exception) {
        ILog logger = IoActivator.getDefault().getLog();
        MultiStatus sts = new MultiStatus(IoActivator.getPluginId(), status, title, exception);
        for (String str : strs) {
            sts.add(new Status(status, IoActivator.getPluginId(), str));
        }
        logger.log(sts);
    }


    /**
     * @param dbName
     *            問合せデータを格納しているテーブル名
     * @param tableType
     *            dbNameを変換して、このタイプのテーブル名にする
     * @param projectId
     *            {@link TableType#WORK_DATA}, {@link TableType#RESULT_DATA}
     *            のときは必要
     * @return DBのテーブル名
     */
    public static String createWorkTableName(String dbName, TableType tableType, int projectId) {
        if (dbName == null) {
            new Exception("dbName is null").printStackTrace(); // Memo debug //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }

        dbName = getTableNameSuffix(dbName);
        String str;
        if (TableType.WORK_DATA.equals(tableType) || TableType.RESULT_DATA.equals(tableType)) {
            str = String.format("%s%s_%03d", tableType.getName(), dbName.toUpperCase(), projectId); //$NON-NLS-1$
        } else {
            str = String.format("%s%s", tableType.getName(), dbName.toUpperCase()); //$NON-NLS-1$
        }

        return str;
    }


    /**
     * {@link #createWorkTableName(String, TableType, int)}
     * で作成したテーブル名から、末尾のユーザ指定部分だけを抜き出す。
     * USR_CLAIM_TABLE1だったら、TABLE1だけを返す。<br/>
     * USR_WORK_TABLE1_002でも、TABLE1だけを返す。
     * 
     * @param tableName
     * @return DBテーブル名
     */
    public static String getTableNameSuffix(String tableName) {
        if (tableName.startsWith(TableType.CLAIM_DATA.getName())) {
            tableName = tableName.substring(TableType.CLAIM_DATA.getName().length());
        } else if (tableName.startsWith(TableType.CORRECTION_MISTAKES_DATA.getName())) {
            tableName = tableName.substring(TableType.CORRECTION_MISTAKES_DATA.getName().length());
        } else if (tableName.startsWith(TableType.WORK_DATA.getName())) {
            /*
             * createWorkTableNameで作成されるWorkDataテーブルには、"%03d"のサフィックスがつくので、末尾4文字を削る
             */
            tableName = tableName.substring(TableType.WORK_DATA.getName().length(), tableName.length() - 4);
        } else if (tableName.startsWith(TableType.RESULT_DATA.getName())) {
            /*
             * createWorkTableNameで作成されるWorkDataテーブルには、"%03d"のサフィックスがつくので、末尾4文字を削る
             */
            tableName = tableName.substring(TableType.RESULT_DATA.getName().length(), tableName.length() - 4);
        }
        return tableName;
    }


    /**
     * juman辞書のインポート
     * 
     * @param dic
     *            辞書データ
     * @param name
     *            登録辞書名
     * @param path
     *            ファイルパス
     */
    public static void jumanImport(ICoronaDic dic, String name, String path) {
        /* juman辞書インポートメソッドを呼び出す */
        ((UserDic) dic).importDic(path, name, DicType.JUMAN);

    }


    /**
     * オブジェクトで渡されたデータをチェックし、文字列型で結果を返す
     * 
     * @param obj
     *            オブジェクト
     * @return 文字列
     */
    public static String convertToString(Object obj) {

        String value = "";
        try {
            if (obj instanceof Clob) {
                value = ((Clob) obj).getSubString(1, (int) ((Clob) obj).length());
            } else {
                value = String.valueOf(obj);
            }
            return value;
        } catch (SQLException | ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }
}
