package com.tida_okinawa.corona.io.dam.hibernate.hql;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.io.bean.WorkdatasBean;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;

/**
 * @author Kamakura
 * 
 */
public class ClaimWorkDataDao {


    /**
     * 取得された先頭レコードの、Select句で始めに指定されたカラムの値を返す。
     * 単一カラムデータ(DIC_ID,PROJECT_IDなどID系)の取得に使用する。取得するカラムの定義型はintでなければならない。
     * 
     * @param strSQL
     * @return 実行結果　0:該当なし、-1:システムエラー、それ以外：対象ID
     */
    public static int getFirstInt(String strSQL) {
        int id = 0;
        try {
            id = getFirstInt4Throws(strSQL);
        } catch (HibernateException e) {
            debugOutput(strSQL);
            e.printStackTrace();
            return -1;
        }
        return id;
    }


    /**
     * 取得された先頭レコードの、Select句で始めに指定されたカラムの値を返す。
     * 単一カラムデータ(DIC_ID,PROJECT_IDなどID系)の取得に使用する。取得するカラムの定義型はintでなければならない。
     * 
     * @param strHQL
     *            実行SQL文字列
     * @return 実行結果　0:該当なし、それ以外：対象ID
     * @throws HibernateException
     *             HQL実行時になんらかの問題が発生した場合にスローされる
     */
    public static int getFirstInt4Throws(String strHQL) throws HibernateException {
        int id = 0;

        Session session = IoService.getInstance().getSession();

        try {

            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(strHQL).list();
            if (list != null && list.size() > 0) {
                try {
                    Object rr = list.get(0);
                    if (rr != null) {
                        id = Integer.parseInt(rr.toString());
                    }
                } catch (Exception ex) {
                    id = -1;
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return id;
    }


    /**
     * 渡したHQL文をログ出力する
     * 
     * @param strHQL
     *            ログ出力HQL文字列
     */
    private static void debugOutput(String strHQL) {
        CoronaActivator.debugLog("Error HQL : " + strHQL); //$NON-NLS-1$
    }


    /**
     * データ登録用SQL実行
     * 内部的にはexecuteSQLThrows()を実行するが、
     * 内部的に例外が発生した場合、例外をスローせずfalseを返却する。
     * 
     * @param strSQL
     *            実行SQL文字列
     * @return 実行結果
     */
    public static boolean executeSQL(String strSQL) {

        try {
            return executeSQL4Throws(strSQL);

        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * データ登録用SQL実行
     * 
     * @param hql
     *            実行SQL文字列
     * @return 実行結果。例外が発生限りは常にtrueを返却する
     * @throws HibernateException
     *             HQL実行時になんらかの問題が発生した場合にスローされる
     */
    public static boolean executeSQL4Throws(String hql) throws HibernateException {

        Session session = IoService.getInstance().getSession();

        try {
            session.createSQLQuery(hql);
        } catch (HibernateException e) {
            debugOutput(hql);
            throw e;
        }
        return true;
    }


    /**
     * ワークデータID取得
     * 
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     * @param claimId
     *            問い合わせデータID
     * @param type
     *            中間データ種別
     * @return ワークデータID
     */
    public static int getClaimWorkDataId(int projectId, int productId, int claimId, ClaimWorkDataType type) {

        StringBuilder strSQL = new StringBuilder(128).append("SELECT ID FROM WORKDATAS WHERE PROJECT_ID =").append(projectId); //$NON-NLS-1$
        strSQL.append(" AND PRODUCT_ID =").append(productId).append(" AND INPUT_TABLE_ID =").append(claimId); //$NON-NLS-1$ //$NON-NLS-2$
        strSQL.append(" AND TYPE= ").append(type.getIntValue()); //$NON-NLS-1$

        return getFirstInt(strSQL.toString());
    }


    /**
     * 問い合わせ中間データ登録
     * 
     * @param workDataId
     *            中間データID
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     * @param claimId
     *            問い合わせデータID
     * @param type
     *            問い合わせ種別
     * @param historyId
     *            履歴ID
     * @return ワークデータID
     */
    public static int insertClaimWorkData(int workDataId, int projectId, int productId, int claimId, ClaimWorkDataType type, int historyId) {

        // ワークデータテーブルに対象中間データを設定
        Session session = IoService.getInstance().getSession();
        WorkdatasBean rs = (WorkdatasBean) session.get(WorkdatasBean.class, workDataId);
        if (rs != null) {
            // 「INSERT IGNORE INTO WORKDATAS....」の機能置換
            return getClaimWorkDataId(projectId, productId, claimId, type);
        }

        StringBuilder strSQL = new StringBuilder(150).append("INSERT INTO WORKDATAS "); //$NON-NLS-1$
        strSQL.append("(ID, PROJECT_ID, PRODUCT_ID, INPUT_TABLE_ID, TYPE, HISTORY_ID, LINK, LASTED) "); //$NON-NLS-1$
        strSQL.append("VALUES (").append(workDataId).append(",").append(projectId).append(","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        strSQL.append(productId).append(",").append(claimId).append(","); //$NON-NLS-1$ //$NON-NLS-2$

        if (ClaimWorkDataType.CORRECTION_MISTAKES.equals(type)) {
            // 誤記補正の場合は、HISTORY_IDをnullに補正(呼び出し元からは0でわたってくる)
            strSQL.append(type.getIntValue()).append(", null, null, null)"); //$NON-NLS-1$
        } else {
            // 誤記補正以外
            strSQL.append(type.getIntValue()).append(", ").append(historyId).append(",").append(" null, null)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        executeSQL(strSQL.toString());

        return getClaimWorkDataId(projectId, productId, claimId, type);
    }
}
