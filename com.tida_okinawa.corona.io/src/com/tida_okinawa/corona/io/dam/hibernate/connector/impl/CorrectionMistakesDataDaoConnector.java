/**
 * @version $Id: CorrectionMistakesDataDaoConnector.java 994 2013-05-28 05:06:30Z yukihiro-kinjyo $
 * 
 * 2012/09/19 19:56:49
 * @author kaori-jiroku
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate.connector.impl;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.bean.ProductBean;
import com.tida_okinawa.corona.io.bean.WorkdatasBean;
import com.tida_okinawa.corona.io.dam.hibernate.ClaimData;
import com.tida_okinawa.corona.io.dam.hibernate.ClaimWorkData;
import com.tida_okinawa.corona.io.dam.hibernate.hql.CommonCreateQuery;

/**
 * 誤記補正データのDB読み書き
 * 
 * @author kaori-jiroku
 * 
 */
public class CorrectionMistakesDataDaoConnector extends ClaimWorkDataDaoConnector {

    /**
     * コンストラクタ
     * 
     * @param claimWorkData
     *            問い合わせデータ
     */
    public CorrectionMistakesDataDaoConnector(ClaimWorkData claimWorkData) {
        super(claimWorkData);
    }


    @Override
    protected String createStatementForGetKeys() {
        ClaimData claimData = (ClaimData) IoActivator.getService().getClaimData(claimWorkData.getClaimId());
        String productDBField = claimData.getFieldInformation(claimData.getProductField()).getName();
        int productId = claimWorkData.getProductId();
        ProductBean product = (ProductBean) CommonCreateQuery.getProductQuery(productId).uniqueResult();
        String productName = null;
        if (product != null) {
            productName = product.getProductName();
        }

        /* SQL作成 */
        StringBuilder strSQL = new StringBuilder(128);
        strSQL.append("SELECT REC_ID FROM ").append(claimData.getTableName()).append(" c, "); //$NON-NLS-1$ //$NON-NLS-2$
        strSQL.append(claimWorkData.getDbName()).append(" cm WHERE c.ID = cm.REC_ID "); //$NON-NLS-1$
        if (productName != null) {
            strSQL.append("AND c.").append(productDBField).append(" = '").append(productName).append("' "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        strSQL.append("AND FLD_ID = ").append(claimWorkData.getFieldId()); //$NON-NLS-1$
        return strSQL.toString();
    }


    @Override
    protected String prepareStatementForGet() throws HibernateException {

        /* レコードIDを元に表示対象のデータ(DATA)を取得するSQL文を作成する */
        StringBuilder strSQL = new StringBuilder(128);
        strSQL.append("SELECT DATA FROM ").append(claimWorkData.getDbName()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
        strSQL.append("WHERE WORK_ID=").append(claimWorkData.getWorkdataId()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
        strSQL.append("AND FLD_ID=").append(claimWorkData.getFieldId()); //$NON-NLS-1$
        strSQL.append(" AND REC_ID= !!1"); //$NON-NLS-1$

        return strSQL.toString();
    }


    @Override
    protected String prepareStatementForCommit() {

        // TODO 20131111 唯一の呼び出し元のAbstractDaoConnectorクラスのdoCommit(K key, T
        // value)がjdbc版での呼び出し階層での確認では未使用となっている。

        String dbName = claimWorkData.getDbName();
        int fieldId = claimWorkData.getFieldId();
        int workDataId = claimWorkData.getWorkdataId();

        if (workDataId <= 0) {
            workDataId = 0;
            Session session = getConnection();
            try {
                StringBuilder sql = new StringBuilder(256);
                sql.append("FROM WorkdatasBean WHERE ").append("projectId =:ProjectId ").append("AND productId =:ProductId ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .append("AND inputTableId =:ClaimId ").append("AND type= :Type"); //$NON-NLS-1$ //$NON-NLS-2$
                @SuppressWarnings("unchecked")
                List<WorkdatasBean> list = session.createQuery(sql.toString()).setInteger("ProjectId", claimWorkData.getProjectId()) //$NON-NLS-1$
                        .setInteger("ProductId", claimWorkData.getProductId()).setInteger("ClaimId", claimWorkData.getClaimId()) //$NON-NLS-1$ //$NON-NLS-2$
                        .setInteger("Type", claimWorkData.getClaimWorkDataType().getIntValue()).list(); //$NON-NLS-1$
                for (WorkdatasBean rs : list) {
                    workDataId = rs.getId();
                    break;
                }
            } catch (HibernateException e) {
                workDataId = -1;
            }

        }
        /* 指定されたテーブルへデータを追加するSQLを作成する */
        StringBuilder sql = new StringBuilder(256);
        sql.append("MERGE INTO ").append(dbName).append(" (FLD_ID, WORK_ID, REC_ID, DATA) "); //$NON-NLS-1$ //$NON-NLS-2$
        sql.append("KEY (FLD_ID, WORK_ID, REC_ID) VALUES "); //$NON-NLS-1$
        sql.append("(").append(fieldId).append(",").append(workDataId).append(", !!1, !!2)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        return sql.toString();
    }


    @Override
    protected String prepareStatementForCommit(int size) {
        String dbName = claimWorkData.getDbName();
        int fieldId = claimWorkData.getFieldId();
        int workDataId = claimWorkData.getWorkdataId();

        if (workDataId <= 0) {
            workDataId = 0;
            Session session = getConnection();
            try {
                StringBuilder strSQL = new StringBuilder(128)
                        .append("FROM WorkdatasBean WHERE projectId =:ProjectId AND productId =:ProductId AND inputTableId =:ClaimId AND type= :Type"); //$NON-NLS-1$
                @SuppressWarnings("unchecked")
                List<WorkdatasBean> workdatasList = session.createQuery(strSQL.toString()).setInteger("ProjectId", claimWorkData.getProjectId()) //$NON-NLS-1$
                        .setInteger("ProductId", claimWorkData.getProductId()).setInteger("ClaimId", claimWorkData.getClaimId()) //$NON-NLS-1$ //$NON-NLS-2$
                        .setInteger("Type", claimWorkData.getClaimWorkDataType().getIntValue()).list(); //$NON-NLS-1$
                if (workdatasList != null && workdatasList.size() > 0) {
                    /* WORKDATASにデータが存在する場合はIDを取得 */
                    WorkdatasBean rs = workdatasList.get(0);
                    workDataId = rs.getId();
                }
            } catch (HibernateException e) {
                workDataId = -1;
            }
        }

        /* 誤記補正結果格納用SQL作成 */
        /* 16はfor内の文字列の予想最大数(fieldId=3, workDataId=5) */
        StringBuilder sql = new StringBuilder(128 + size * 16);

        sql.append("MERGE INTO ").append(dbName).append(" (FLD_ID, WORK_ID, REC_ID, DATA) "); //$NON-NLS-1$ //$NON-NLS-2$
        sql.append("KEY (FLD_ID, WORK_ID, REC_ID) VALUES "); //$NON-NLS-1$
        for (int i = 0, j = 1; i < size; i++, j++) {
            sql.append("(").append(fieldId).append(", ").append(workDataId).append(", !!").append(String.valueOf(j)).append(", '!!") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    .append(String.valueOf(j + 1)).append("' ),"); //$NON-NLS-1$
            j++;
        }
        sql.deleteCharAt(sql.length() - 1);
        return sql.toString();
    }


    @Override
    protected String setParamForGet(String stmt, Integer key) throws HibernateException {
        /* パラメータへレコードIDを設定 */
        stmt = stmt.replaceFirst("!!1", String.valueOf(key)); //$NON-NLS-1$
        return stmt;
    }
}
