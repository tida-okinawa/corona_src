/**
 * @version $Id: ClaimWorkDataDaoConnector.java 1031 2013-07-01 07:16:37Z wataru-higa $
 * 
 * 2011/10/28 11:43:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate.connector.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.bean.ProductBean;
import com.tida_okinawa.corona.io.bean.WorkdatasBean;
import com.tida_okinawa.corona.io.dam.hibernate.ClaimData;
import com.tida_okinawa.corona.io.dam.hibernate.ClaimWorkData;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.dam.hibernate.hql.CommonCreateQuery;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * 中間データのDB読み書き
 * 
 * @author imai
 * 
 */
public class ClaimWorkDataDaoConnector extends AbstractDaoConnector<Integer, String> {

    protected ClaimWorkData claimWorkData;


    /**
     * @return 問い合わせデータ
     */
    public ClaimWorkData getClaimWorkData() {
        return claimWorkData;
    }


    /**
     * @param claimWorkData
     *            問い合わせデータ
     */
    public void setClaimWorkData(ClaimWorkData claimWorkData) {
        this.claimWorkData = claimWorkData;

    }


    /**
     * @param claimWorkData
     *            問い合わせデータ
     */
    public ClaimWorkDataDaoConnector(ClaimWorkData claimWorkData) {
        this.claimWorkData = claimWorkData;
    }


    /**
     * @param claimWorkData
     *            問い合わせデータ
     * @param historyId
     *            履歴ID
     */
    public ClaimWorkDataDaoConnector(ClaimWorkData claimWorkData, int historyId) {
        this.claimWorkData = claimWorkData;
    }


    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }


    @Override
    Session getConnection() throws HibernateException {
        Session conn = ((IoService) IoService.getInstance()).getSession();
        return conn;
    }


    @Override
    protected String createStatementForGetKeys() {
        /* createStatementFotGetKeysで履歴IDを渡すようにしたため、引数なしのやつは使わない */
        return null;
    }


    /**
     * 抽出対象のレコードIDを取得するSQLを作成する(履歴IDも条件に含める)
     * 
     * @param historyId
     *            履歴ID
     * @return SQL文
     */
    protected String createStatementForGetKeys(int historyId) {
        ClaimData claimData = (ClaimData) IoActivator.getService().getClaimData(claimWorkData.getClaimId());
        String productDBField = claimData.getFieldInformation(claimData.getProductField()).getName();
        int productId = claimWorkData.getProductId();
        ProductBean product = (ProductBean) CommonCreateQuery.getProductQuery(productId).uniqueResult();
        String productName = product.getProductName();

        String strSQL;

        ClaimWorkDataType type = claimWorkData.getClaimWorkDataType();
        if (ClaimWorkDataType.FREQUENTLY_APPERING.equals(type)) {
            /* 頻出用語抽出の場合 */
            strSQL = "select rec_id from " + claimWorkData.getDbName() + " t where work_id =" + claimWorkData.getWorkdataId(); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            /* それ以外の場合 */
            strSQL = "SELECT REC_ID FROM " + claimData.getTableName() + " c, " + claimWorkData.getDbName() + " t, WORKDATAS w WHERE c.ID = t.REC_ID "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (productName != null) {
                strSQL += "AND c." + productDBField + " ='" + productName + "' "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            strSQL += "AND w.ID = t.WORK_ID AND t.FLD_ID = " + claimWorkData.getFieldId() + " AND w.TYPE = " + type.getIntValue(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        strSQL += " AND t.HISTORY_ID = " + historyId; //$NON-NLS-1$
        return strSQL;
    }


    @Override
    protected Integer getKey(Object[] rs) throws HibernateException {
        int recordId = rs[0] == null ? 0 : Integer.parseInt(rs[0].toString()); //rs.getInt("REC_ID");
        return recordId;
    }


    @Override
    protected String prepareStatementForGet() throws HibernateException {

        StringBuilder strSQL = new StringBuilder(128);
        strSQL.append("SELECT DATA FROM ").append(claimWorkData.getDbName()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
        // TODO 20131206 Hibernate版H2DataBaseでは、このままではNGの為、カットしておく。
        //        strSQL.append("USE INDEX (").append(claimWorkData.getDbName()).append("_INDEX) "); //$NON-NLS-1$ //$NON-NLS-2$
        strSQL.append("WHERE WORK_ID=").append(claimWorkData.getWorkdataId()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
        strSQL.append("AND FLD_ID=").append(claimWorkData.getFieldId()); //$NON-NLS-1$
        strSQL.append(" AND HISTORY_ID = !!1"); //$NON-NLS-1$
        strSQL.append(" AND REC_ID = !!2"); //$NON-NLS-1$

        return strSQL.toString();
    }


    @Override
    protected String setParamForGet(String stmt, Integer key) throws HibernateException {

        // TODO 20131105 このメソッドはjdbc版では呼び出し階層での確認では未使用となっている。

        //stmt.setInt(1, key);
        return stmt.replaceFirst("!!1", String.valueOf(key)); //$NON-NLS-1$
    }


    /**
     * prepareStatementForGetで作成したSQL文のパラメータにレコードIDと履歴IDを設定する
     * 
     * @param stmt
     *            SQLステートメント
     * @param key
     *            レコードID
     * @param historyId
     *            履歴ID
     * @throws HibernateException
     */
    protected String setParamForGet(String stmt, int key, int historyId) throws HibernateException {
        stmt = stmt.replaceFirst("!!1", String.valueOf(historyId)); //$NON-NLS-1$
        stmt = stmt.replaceFirst("!!2", String.valueOf(key)); //$NON-NLS-1$
        return stmt;
    }


    @Override
    protected String getResultDirect(List<Object[]> list) throws HibernateException {
        //String data = rs.getString("DATA");
        String data = null;
        /*
         * TODO 20131203 listの内容は Object[]の場合とObjectとなる場合があるので、この判定が必要
         * （selectでの１行あたりの取得個数は１個の場合Objectとなってしまう。）
         */
        if (list.get(0) instanceof Object[]) {
            Object[] rs = list.get(0);
            //data = (String) rs[0];
            data = CoronaIoUtils.convertToString(rs[0]);
        } else {
            Object rs = list.get(0);
            //data = (String) rs;
            data = CoronaIoUtils.convertToString(rs);
        }
        return data;
    }


    @Override
    protected String prepareStatementForCommit() {
        String dbName = claimWorkData.getDbName();
        int fieldId = claimWorkData.getFieldId();
        int workDataId = claimWorkData.getWorkdataId();

        if (workDataId <= 0) {
            /* ClaimWorkDataDao.getClaimWorkDataId を置換する。 */

            workDataId = 0;
            Session session = getConnection();
            try {
                String hql = "FROM WorkdatasBean WHERE projectId =:ProjectId AND productId =:ProductId AND inputTableId =:ClaimId AND type= :Type"; //$NON-NLS-1$
                @SuppressWarnings("unchecked")
                List<WorkdatasBean> list = session.createQuery(hql).setInteger("ProjectId", claimWorkData.getProjectId()) //$NON-NLS-1$
                        .setInteger("ProductId", claimWorkData.getProductId()) //$NON-NLS-1$
                        .setInteger("ClaimId", claimWorkData.getClaimId()) //$NON-NLS-1$
                        .setInteger("Type", claimWorkData.getClaimWorkDataType().getIntValue()) //$NON-NLS-1$
                        .list();
                for (WorkdatasBean rs : list) {
                    workDataId = rs.getId();
                    break;
                }
            } catch (HibernateException e) {
                workDataId = -1;
            }
        }


        /* 登録用の履歴IDを取得 */
        int historyId = claimWorkData.getHistoryId();

        StringBuilder sql = new StringBuilder(256);
        sql.append("MERGE INTO ").append(dbName); //$NON-NLS-1$
        sql.append(" (FLD_ID, WORK_ID, REC_ID, HISTORY_ID, DATA) "); //$NON-NLS-1$
        sql.append("KEY (FLD_ID, WORK_ID, REC_ID, HISTORY_ID) "); //$NON-NLS-1$
        sql.append("VALUES (").append(fieldId).append(",").append(workDataId).append(", !!1, ").append(historyId).append(", '!!2')"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        //        sql.append("INSERT INTO ").append(dbName); //$NON-NLS-1$
        //        sql.append(" (FLD_ID, WORK_ID, REC_ID, HISTORY_ID, DATA) VALUES "); //$NON-NLS-1$
        //        sql.append("(").append(fieldId).append(",").append(workDataId).append(", !!1, ").append(historyId).append(", '!!2')"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        //        sql.append(" ON DUPLICATE KEY UPDATE DATA =VALUES(DATA);"); //$NON-NLS-1$

        return sql.toString();
    }


    @Override
    protected String setParamForCommit(String stmt, Integer key, String value) throws HibernateException {
        stmt = stmt.replaceFirst("!!1", String.valueOf(key)); //$NON-NLS-1$
        stmt = stmt.replaceFirst("!!2", value); //$NON-NLS-1$
        return stmt;
    }


    @Override
    protected String prepareStatementForCommit(int size) {
        String dbName = claimWorkData.getDbName();
        int fieldId = claimWorkData.getFieldId();
        int workDataId = claimWorkData.getWorkdataId();

        if (workDataId <= 0) {
            /* ClaimWorkDataDao.getClaimWorkDataId を置換する。 */
            workDataId = 0;
            Session session = getConnection();
            try {
                String hql = "FROM WorkdatasBean WHERE projectId =:ProjectId AND productId =:ProductId AND inputTableId =:ClaimId AND type= :Type"; //$NON-NLS-1$
                @SuppressWarnings("unchecked")
                List<WorkdatasBean> list = session.createQuery(hql).setInteger("ProjectId", claimWorkData.getProjectId()) //$NON-NLS-1$
                        .setInteger("ProductId", claimWorkData.getProductId()) //$NON-NLS-1$
                        .setInteger("ClaimId", claimWorkData.getClaimId()) //$NON-NLS-1$
                        .setInteger("Type", claimWorkData.getClaimWorkDataType().getIntValue()) //$NON-NLS-1$
                        .list();
                for (WorkdatasBean rs : list) {
                    workDataId = rs.getId();
                    break;
                }
            } catch (HibernateException e) {
                workDataId = -1;
            }

        }
        /* 登録用の履歴IDを取得 */
        int historyId = claimWorkData.getHistoryId();

        /* 16はfor内の文字列の予想最大数(fieldId=3, workDataId=5) */
        StringBuilder sql = new StringBuilder(128 + size * 16);
        sql.append("MERGE INTO ").append(dbName); //$NON-NLS-1$
        sql.append(" (FLD_ID, WORK_ID, REC_ID, HISTORY_ID, DATA) "); //$NON-NLS-1$
        sql.append(" KEY (FLD_ID, WORK_ID, REC_ID, HISTORY_ID) VALUES "); //$NON-NLS-1$
        for (int i = 0, j = 1; i < size; i++, j++) {
            sql.append("(").append(fieldId).append(",").append(workDataId).append(", !!").append(String.valueOf(j)).append(", "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            j++;
            sql.append(historyId).append(", '!!").append(String.valueOf(j)).append("' ),"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sql.deleteCharAt(sql.length() - 1);

        //        sql.append("INSERT INTO ").append(dbName); //$NON-NLS-1$
        //        sql.append(" (FLD_ID, WORK_ID, REC_ID, HISTORY_ID, DATA) VALUES "); //$NON-NLS-1$
        //        for (int i = 0, j = 1; i < size; i++, j++) {
        //            sql.append("(").append(fieldId).append(",").append(workDataId).append(", !!").append(String.valueOf(j)).append(", "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        //            j++;
        //            sql.append(historyId).append(", '!!").append(String.valueOf(j)).append("' ),"); //$NON-NLS-1$ //$NON-NLS-2$
        //        }
        //        sql.deleteCharAt(sql.length() - 1);
        //        sql.append(" ON DUPLICATE KEY UPDATE DATA =VALUES(DATA);"); //$NON-NLS-1$

        return sql.toString();
    }


    @Override
    protected String setParamForCommit(String stmt, List<Data> datas) throws HibernateException {
        int i = 1;
        for (Data data : datas) {
            stmt = stmt.replaceFirst("!!" + String.valueOf(i), String.valueOf(data.key)); //$NON-NLS-1$
            stmt = stmt.replaceFirst("!!" + String.valueOf(i + 1), data.value); //$NON-NLS-1$
            i = i + 2;
        }
        return stmt;
    }


    /**
     * レコードIDと履歴IDを元に表示対象のデータ(DATA)を取得する
     * 
     * @param key
     *            レコードID
     * @param historyId
     *            履歴ID
     * @return 条件に一致するデータ
     */
    public String get(Integer key, int historyId) {
        try {
            Session session = getConnection();
            if (stmtForGet == null) {
                String strSQL = prepareStatementForGet();
                stmtForGet = strSQL;
            }
            String sqlExe = setParamForGet(stmtForGet, key, historyId);

            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(sqlExe).list();
            if (list != null) {
                if (list.size() > 0) {
                    String value = getResultDirect(list);
                    return value;
                }
            }
        } catch (HibernateException e) {
            System.err.println(e + ":" + key); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * キー一覧取得
     * 
     * @param historyId
     *            履歴ID
     * @return 取得できなかった場合は、サイズ0のSet<Integer>が返る
     * 
     */
    public Set<Integer> getKeys(int historyId) {
        try {
            /* コミット終了まで待つ */
            while (committer != null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
            String strSQL = createStatementForGetKeys(historyId);
            Session session = getConnection();
            @SuppressWarnings("unchecked")
            List<Object> list = session.createSQLQuery(strSQL).list();
            if (list != null) {
                keys = new HashSet<Integer>(10000);
                for (Object rs : list) {
                    Object[] idArr = new Object[2];
                    idArr[0] = rs;
                    Integer key = getKey(idArr);
                    if (key != null) {
                        keys.add(key);
                    }
                }
            }
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        } catch (HibernateException e) {
            e.printStackTrace();
            keys = new HashSet<Integer>(0);
        }
        return keys;
    }
}
