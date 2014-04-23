/**
 * @version $Id: ClaimDataDaoConnector.java 994 2013-05-28 05:06:30Z yukihiro-kinjyo $
 * 
 * 2011/10/28 11:43:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate.connector.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.IField;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.impl.Field;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * 問い合わせ情報のDB読み書き
 * 
 * @author imai
 * 
 */
public class ClaimDataDaoConnector extends AbstractDaoConnector<Integer, List<IField>> {
    final IClaimData claimData;


    /**
     * @param claimData
     *            問い合わせデータ
     */
    public ClaimDataDaoConnector(IClaimData claimData) {
        this.claimData = claimData;
    }


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
        String strSQL = "SELECT ID FROM " + claimData.getTableName(); //$NON-NLS-1$
        return strSQL;
    }


    @Override
    protected Integer getKey(Object[] rs) throws HibernateException {
        int recordId = rs[0] == null ? 0 : Integer.parseInt(rs[0].toString()); //rs.getInt("ID");
        return recordId;
    }


    @Override
    protected String prepareStatementForGet() throws HibernateException {
        StringBuilder strSQL = new StringBuilder(64).append("SELECT * FROM ").append(claimData.getTableName()).append(" WHERE ID=!!1"); //$NON-NLS-1$ //$NON-NLS-2$
        return strSQL.toString();
    }


    @Override
    protected String setParamForGet(String stmt, Integer key) throws HibernateException {
        //stmt.setInt(1, key);
        return stmt.replaceFirst("!!1", String.valueOf(key)); //$NON-NLS-1$
    }


    @Override
    protected List<IField> getResultDirect(List<Object[]> list) throws HibernateException {
        List<IField> fields = new ArrayList<IField>();

        List<IFieldHeader> headers = claimData.getFieldInformations();
        /*
         * TODO 20131203 listの内容は Object[]の場合とObjectとなる場合があるので、この判定が必要
         * （selectでの１行あたりの取得個数は１個の場合Objectとなってしまう。）
         */
        Object[] rs = null;
        if (list.get(0) instanceof Object[]) {
            rs = list.get(0);
        } else {
            rs = new Object[1];
            rs[0] = list.get(0);
        }
        for (IFieldHeader header : headers) {

            String value = CoronaIoUtils.convertToString(rs[header.getId() - 1]);
            if (value.equals("null")) { //$NON-NLS-1$
                //value = ""; //$NON-NLS-1$
                // TODO 20131224 従来のmysql版ではこの場合、nullをそのままセット
                value = null;
            }
            Field field = new Field(header);
            field.setValue(value);
            fields.add(field);
        }
        return fields;
    }


    @Override
    protected String prepareStatementForCommit() {
        String dbName = claimData.getTableName();
        StringBuilder strField = new StringBuilder("ID"); //$NON-NLS-1$
        for (IFieldHeader header : claimData.getFieldInformations()) {
            strField.append(","); //$NON-NLS-1$
            strField.append(header.getName());
        }
        StringBuilder strData = new StringBuilder("!!1"); //$NON-NLS-1$
        int loopSize = claimData.getFieldInformations().size();
        for (int i = 0, j = 2; i < loopSize; i++, j++) {
            strData.append(",'!!").append(String.valueOf(j) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        StringBuilder strSQL = new StringBuilder(512);
        strSQL.append("MERGE INTO ").append(dbName).append(" (").append(strField.toString()).append(") "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        strSQL.append(" KEY (ID) "); //$NON-NLS-1$
        strSQL.append("VALUES(").append(strData.toString()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        // TODO 20131206 MERGEの使用
        //        String strSQL = "INSERT INTO " + dbName + " (" + strField.toString() + ") VALUES"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //        strSQL += "(" + strData.toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        //        strSQL += " ON DUPLICATE KEY UPDATE DATA =VALUES(DATA)"; //$NON-NLS-1$

        return strSQL.toString();
    }


    @Override
    protected String setParamForCommit(String stmt, Integer key, List<IField> value) throws HibernateException {
        //stmt.setInt(1, key); // recordId
        stmt.replaceFirst("!!1", String.valueOf(key)); //$NON-NLS-1$
        int i = 2;
        for (IField field : value) {
            String keyStr = "!!" + String.valueOf(i); //$NON-NLS-1$
            stmt = stmt.replaceFirst(keyStr, (String) (field.getValue()));
            i++;
        }

        // TODO 20131111 addBatch()はjdbc版では、対となるexecuteBatchが存在しない為、機能として移行しない。
        //stmt.addBatch();

        return stmt;
    }


    @Override
    protected String prepareStatementForCommit(int size) {
        return prepareStatementForCommit();
    }


    @Override
    protected String setParamForCommit(String stmt, List<Data> datas) throws HibernateException {
        stmt.replaceFirst("!!1", String.valueOf(datas.get(0).key)); //$NON-NLS-1$
        int i = 2;
        for (IField field : datas.get(0).value) {
            String keyStr = "!!" + String.valueOf(i); //$NON-NLS-1$
            stmt = stmt.replaceFirst(keyStr, (String) (field.getValue()));
            i++;
        }
        // TODO 20131111 addBatch()はjdbc版では、対となるexecuteBatchが存在しない為、機能として移行しない。
        //stmt.addBatch();

        return stmt;
    }
}