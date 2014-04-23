/**
 * @version $Id: ClaimWorkPatternDaoConnector.java 1083 2013-08-05 08:54:28Z hajime-uchihara $
 * 
 * 2011/10/28 11:43:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate.connector.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.common.Pair;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.dam.hibernate.ClaimWorkPattern;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.dam.hibernate.connector.DaoConnecter;
import com.tida_okinawa.corona.io.model.cleansing.HitPositionConverter;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * 構文パターン結果のDB読み書き
 * 
 * DBは、{recordId, patternId} で1レコードだが、データは {recordId, {patternId, ...}}
 * 
 * @author imai
 * 
 */
public class ClaimWorkPatternDaoConnector implements DaoConnecter<Integer, Map<IPattern, List<String>>> {
    /*
     * 保存先がusr_work_xxx_yyyではないので、ClaimWorkDataDaoConnectorを継承できない。
     * 内部でAbstractDaoConnectorを保持するので、AbstractDaoConnectorを継承していない。
     */

    final ClaimWorkPattern claimWorkData;

    InnerDaoConnector inner = new InnerDaoConnector();

    class InnerDaoConnector extends AbstractDaoConnector<Integer, Pair<IPattern, List<String>>> {
        @Override
        Session getConnection() throws HibernateException {
            Session conn = ((IoService) IoService.getInstance()).getSession();
            return conn;
        }


        @Override
        protected String createStatementForGetKeys() {
            StringBuilder sql = new StringBuilder(128);
            sql.append("Select rec_id, pattern_id From ").append(claimWorkData.getRelDbName()); //$NON-NLS-1$
            sql.append(" Where work_id=").append(claimWorkData.getWorkdataId()); //$NON-NLS-1$
            sql.append(" And fld_id=").append(claimWorkData.getFieldId()); //$NON-NLS-1$
            sql.append(" And history=").append(claimWorkData.getHistoryId()); //$NON-NLS-1$
            return sql.toString();
        }


        @Override
        protected Integer getKey(Object[] rs) throws HibernateException {
            /* note: recordID と同時に patternIdを取得しMapに記憶 */
            int patternId = rs[1] == null ? 0 : Integer.parseInt(rs[1].toString());
            int recordId = rs[0] == null ? 0 : Integer.parseInt(rs[0].toString());
            List<Integer> patternIds = map.get(recordId);
            if (patternIds == null) {
                patternIds = new ArrayList<Integer>();
                map.put(recordId, patternIds);
            }
            patternIds.add(patternId);
            return recordId;
        }


        @Override
        protected String prepareStatementForGet() throws HibernateException {
            StringBuilder sql = new StringBuilder(256);
            sql.append("Select pattern_id, hit_info ,rec_id From ").append(claimWorkData.getRelDbName()); //$NON-NLS-1$
            // TODO 20131206 Hibernate版H2DataBaseでは、このままではNGの為、カットしておく。
            //            sql.append(" USE INDEX (").append(claimWorkData.getRelDbName()).append("_INDEX)"); //$NON-NLS-1$ //$NON-NLS-2$
            sql.append(" Where work_id=").append(claimWorkData.getWorkdataId()); //$NON-NLS-1$
            sql.append(" And fld_id=").append(claimWorkData.getFieldId()); //$NON-NLS-1$
            sql.append(" And rec_id=!!1 And history=!!2"); //$NON-NLS-1$
            return sql.toString();
        }


        protected String prepareStatementForAllRecordsGet() throws HibernateException {
            StringBuilder sql = new StringBuilder(256);
            sql.append("Select pattern_id, hit_info, rec_id From ").append(claimWorkData.getRelDbName()); //$NON-NLS-1$
            sql.append(" Where work_id=").append(claimWorkData.getWorkdataId()); //$NON-NLS-1$
            sql.append(" And fld_id=").append(claimWorkData.getFieldId()); //$NON-NLS-1$
            sql.append(" And history=!!1"); //$NON-NLS-1$
            return sql.toString();
        }


        @Deprecated
        @Override
        protected String setParamForGet(String stmt, Integer key) throws HibernateException {
            System.err.println("Deprecated ClaimWorkPatternDaoConnector#setparamForGet(PreparedStatement, Integer)"); //$NON-NLS-1$ //debug
            // never use
            return ""; //$NON-NLS-1$
        }


        /**
         * ヒット位置情報を取得するためのパラメータを設定する
         * 
         * @param stmt
         *            PreparedStatement
         * @param key
         *            Integer レコードID
         * @param history
         *            ｉｎｔ　履歴ＩＤ
         * @throws HibernateException
         *             HQL例外処理
         */
        protected String setParamForGet(String stmt, Integer key, int history) throws HibernateException {
            stmt = stmt.replaceFirst("!!1", String.valueOf(key)); //$NON-NLS-1$
            stmt = stmt.replaceFirst("!!2", String.valueOf(history)); //$NON-NLS-1$
            return stmt;
        }


        protected String setParamForGet(String stmt, int history) throws HibernateException {
            try {
                stmt = stmt.replaceFirst("!!1", String.valueOf(history)); //$NON-NLS-1$
            } catch (Exception e) {
                // エラー処理はなし
            }
            return stmt;
        }

        Map<Integer, IPattern> pdicList = new HashMap<Integer, IPattern>();


        /**
         * rsから結果をひとつ取って返す。
         * 該当するIPatternを取得できないとき、nullを返す
         */
        @Override
        protected Pair<IPattern, List<String>> getResultDirect(List<Object[]> list) throws HibernateException {
            //int patternId = rs.getInt(1);
            int patternId = 0;
            Object[] rs = null;
            /*
             * TODO 20131203 listの内容は Object[]の場合とObjectとなる場合があるので、この判定が必要
             * （selectでの１行あたりの取得個数は１個の場合Objectとなってしまう。）
             */
            if (list.get(0) instanceof Object[]) {
                rs = list.get(0);
                patternId = rs[0] == null ? 0 : Integer.parseInt(rs[0].toString()); //(int) rs[0];
            } else {
                rs = new Object[2];
                rs[0] = list.get(0);
                rs[1] = null;
                patternId = rs[0] == null ? 0 : Integer.parseInt(rs[0].toString()); //(int) rs[0];
            }
            // IPatternを取得する
            if (!pdicList.containsKey(patternId)) {
                IPattern iPattern = (IPattern) IoActivator.getDicUtil().getItem(patternId, DicType.PATTERN);
                if (iPattern == null) {
                    return null;
                }
                pdicList.put(patternId, iPattern);
            }
            String hitString = CoronaIoUtils.convertToString(rs[1]);
            List<String> hitInfos = HitPositionConverter.split(hitString);
            return new Pair<IPattern, List<String>>(pdicList.get(patternId), hitInfos);
        }


        @Override
        protected String prepareStatementForCommit() {
            StringBuilder sql = new StringBuilder(128);
            sql.append("Insert Into ").append(claimWorkData.getRelDbName()); //$NON-NLS-1$
            sql.append(" (WORK_ID,FLD_ID,HISTORY,REC_ID,PATTERN_ID,HIT_INFO) VALUES("); //$NON-NLS-1$
            sql.append(claimWorkData.getWorkdataId()).append(","); //$NON-NLS-1$
            sql.append(claimWorkData.getFieldId()).append(","); //$NON-NLS-1$
            sql.append(claimWorkData.getHistoryId()).append(",!!1,!!2,'!!3')"); //$NON-NLS-1$
            return sql.toString();
        }


        @Override
        protected String setParamForCommit(String stmt, Integer key, Pair<IPattern, List<String>> value) throws HibernateException {
            // record ID
            stmt = stmt.replaceFirst("!!1", String.valueOf(key)); //$NON-NLS-1$
            // pattern ID
            stmt = stmt.replaceFirst("!!2", String.valueOf(value.getValue1().getId())); //$NON-NLS-1$
            // hit informations
            stmt = stmt.replaceFirst("!!3", createHitString(value.getValue2())); //$NON-NLS-1$
            return stmt;
        }


        @Override
        public void clear() {
            // nothing to do
        }


        @Override
        protected String prepareStatementForCommit(int size) {
            StringBuilder sql = new StringBuilder(64 + size * 10);
            sql.append("Insert Into ").append(claimWorkData.getRelDbName()); //$NON-NLS-1$
            sql.append(" (WORK_ID,FLD_ID,HISTORY,REC_ID,PATTERN_ID,HIT_INFO) VALUES"); //$NON-NLS-1$
            for (int i = 0, j = 1; i < size; i++) {
                sql.append("("); //$NON-NLS-1$
                sql.append(claimWorkData.getWorkdataId()).append(","); //$NON-NLS-1$
                sql.append(claimWorkData.getFieldId()).append(","); //$NON-NLS-1$
                sql.append(claimWorkData.getHistoryId()).append(","); //$NON-NLS-1$
                sql.append("!!").append(String.valueOf(j++)); //$NON-NLS-1$
                sql.append(",!!").append(String.valueOf(j++)); //$NON-NLS-1$
                sql.append(",'!!").append(String.valueOf(j++)); //$NON-NLS-1$
                sql.append("'),"); //$NON-NLS-1$
            }
            return sql.deleteCharAt(sql.length() - 1).toString();
        }


        @Override
        protected String setParamForCommit(String stmt, List<Data> datas) throws HibernateException {
            int i = 1;
            for (Data data : datas) {
                // record ID
                stmt = stmt.replaceFirst("!!" + String.valueOf(i++), String.valueOf(data.key)); //$NON-NLS-1$
                // pattern ID
                stmt = stmt.replaceFirst("!!" + String.valueOf(i++), String.valueOf(data.value.getValue1().getId())); //$NON-NLS-1$
                // hit informations
                stmt = stmt.replaceFirst("!!" + String.valueOf(i++), createHitString(data.value.getValue2())); //$NON-NLS-1$
            }
            return stmt;
        }


        private final String createHitString(List<String> hitInfos) {
            /* 1ヒットあたり25文字xヒット分と仮定 */
            StringBuilder hitString = new StringBuilder(hitInfos.size() * 25);
            for (String hitInfo : hitInfos) {
                hitString.append(hitInfo);
            }
            return hitString.toString();
        }
    };

    /**
     * データ量は少ないので全部もっておく
     */
    Map<Integer, List<Integer>> map = null;


    /**
     * @param claimWorkData
     *            問い合わせデータ
     */
    public ClaimWorkPatternDaoConnector(ClaimWorkPattern claimWorkData) {
        this.claimWorkData = claimWorkData;
    }


    @Override
    public Set<Integer> getKeys() {
        if (map == null)
            map = new TreeMap<Integer, List<Integer>>();
        return inner.getKeys();
    }


    @Override
    public Map<IPattern, List<String>> get(Integer recordId) {
        return get(recordId, claimWorkData.getHistoryId());
    }


    /**
     * 構文解析結果を取得するメソッド。
     * レコードIDとヒットしたパターンリストを返却する。
     * 
     * @return　Map<Pair<レコードID,履歴ID>,Map<パターン,List<ヒット位置>>>
     */
    public Map<Pair<Integer, Integer>, Map<IPattern, List<String>>> get() {
        return getResultMap(claimWorkData.getHistoryId());
    }

    protected String stmtForGet;

    private Map<Pair<Integer, Integer>, Map<IPattern, List<String>>> cache = new WeakHashMap<Pair<Integer, Integer>, Map<IPattern, List<String>>>();


    /**
     * 履歴IDを基に全ての構文解析結果レコードを取得するメソッド
     * 
     * @param history
     *            履歴ID
     * @return　Map<Pair<Integer, Integer>, Map<IPattern, List<String>>>　
     *         Map<Pair<レコードID, 履歴ID>, Map<ヒットパターン, List<ヒット位置>>>
     */
    private Map<Pair<Integer, Integer>, Map<IPattern, List<String>>> getResultMap(int history) {
        //ResultSet rs = null;
        cache.clear();
        try {
            String sql = inner.prepareStatementForAllRecordsGet();
            Session session = ((IoService) IoService.getInstance()).getSession();
            List<Object[]> rsList = new ArrayList<Object[]>();

            /* inner.setParamForGet(stmtForGet, history); を置換する。 */
            stmtForGet = inner.setParamForGet(sql, history);
            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(stmtForGet).list();

            Map<Pair<Integer, Integer>, Map<IPattern, List<String>>> resultMap = new HashMap<Pair<Integer, Integer>, Map<IPattern, List<String>>>();
            for (Object[] rs : list) {
                Map<IPattern, List<String>> pairList = null;
                /*
                 * Pair<Integer, Integer> key = new Pair<Integer,
                 * Integer>(rs.getInt(3), history); を置換する。
                 */
                int para1 = rs[2] == null ? 0 : Integer.parseInt(rs[2].toString()); //(int) rs[2] 
                Pair<Integer, Integer> key = new Pair<Integer, Integer>(para1, history);
                rsList.clear();
                rsList.add(rs);
                if (resultMap.containsKey(key)) {
                    // 他のパターンにもヒットしていればPairを追加する
                    pairList = resultMap.get(key);
                    Pair<IPattern, List<String>> pairResult = inner.getResultDirect(rsList);
                    // TODO 20130730 エラー発生を警告に緩和
                    if (pairResult == null) {

                        /* 解析データと使用辞書との整合性がとれない場合に発生 */
                        String detail = String.valueOf(key.getValue1()) + "," + String.valueOf(key.getValue2()); //$NON-NLS-1$
                        throw new NullPointerException("non Error:Type000" + detail); //$NON-NLS-1$
                    }

                    pairList.put(pairResult.getValue1(), pairResult.getValue2());
                } else {
                    Pair<IPattern, List<String>> pair = inner.getResultDirect(rsList);
                    // TODO 20130730 エラー発生を警告に緩和
                    if (pair == null) {

                        /* 解析データと使用辞書との整合性がとれない場合に発生 */
                        String detail = String.valueOf(key.getValue1()) + "," + String.valueOf(key.getValue2()); //$NON-NLS-1$
                        throw new NullPointerException("non Error:Type000" + detail); //$NON-NLS-1$
                    }

                    pairList = new HashMap<IPattern, List<String>>();
                    pairList.put(pair.getValue1(), pair.getValue2());
                }
                resultMap.put(key, pairList);
                this.cache.put(key, pairList);
            }
            return resultMap;
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * レコードIDと履歴IDを基に対象となる構文解析結果レコードを取得する。
     * 
     * @param recordId
     *            レコードID
     * @param history
     *            履歴ID
     * @return　Map<IPattern, List<String>>　
     *         Map<構文パターン,List<ヒット位置>>
     */
    public Map<IPattern, List<String>> get(Integer recordId, int history) {
        Pair<Integer, Integer> key = new Pair<Integer, Integer>(recordId, history);
        Map<IPattern, List<String>> cache = this.cache.get(key);
        if (cache != null) {
            return cache;
        }
        getResultMap(history);
        if (!this.cache.containsKey(key)) {
            //全件取得で取得できない場合は1件ずつ取得。
            getRecord(recordId, history);
        }
        return cache = this.cache.get(key);
    }


    private void getRecord(Integer recordId, int history) {
        Pair<Integer, Integer> key = new Pair<Integer, Integer>(recordId, history);
        List<Object[]> rsList = new ArrayList<Object[]>();
        try {
            String sql = inner.prepareStatementForGet();
            Session session = ((IoService) IoService.getInstance()).getSession();
            /* inner.setParamForGet(stmtForGet, recordId, history); を置換する。 */
            stmtForGet = inner.setParamForGet(sql, recordId, history);
            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(stmtForGet).list();

            Map<IPattern, List<String>> result = new HashMap<IPattern, List<String>>();
            for (Object[] rs : list) {
                rsList.clear();
                rsList.add(rs);
                Pair<IPattern, List<String>> pair = inner.getResultDirect(rsList);
                result.put(pair.getValue1(), pair.getValue2());
            }
            this.cache.put(key, result);
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int size() {
        if (map == null)
            getKeys();
        return map.size();
    }


    @Override
    public void clear() {
        // TODO 20130718 historyIdが更新されない事
        String dbName = claimWorkData.getRelDbName();
        int fieldId = claimWorkData.getFieldId();
        int workDataId = claimWorkData.getWorkdataId();
        int historyId = claimWorkData.getHistoryId();

        if (inner != null) {
            inner.clear();
        }
        if (map != null) {
            map.clear();
        }

        // TODO 20130718 historyIdが更新されない事
        /* Memo 解析履歴を残さない対応：リレーションレコードを削除（旧結果と異なる場合を許容するため） */
        StringBuilder sql = new StringBuilder(128);
        sql.append("DELETE FROM ").append(dbName); //$NON-NLS-1$
        sql.append(" WHERE WORK_ID = ").append(workDataId); //$NON-NLS-1$
        sql.append(" AND FLD_ID = ").append(fieldId); //$NON-NLS-1$
        sql.append(" AND HISTORY = ").append(historyId); //$NON-NLS-1$
        System.out.println(sql.toString());
        Session session = ((IoService) IoService.getInstance()).getSession();
        try {
            /* トランザクション開始 */
            session.beginTransaction();

            session.createSQLQuery(sql.toString()).executeUpdate();
            session.flush();

            /* トランザクションコミット */
            session.getTransaction().commit();

        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
    }


    /**
     * 
     * @param key
     *            レコードID
     * @param value
     *            該当したパターンIDの配列
     */
    @Override
    public void commit(Integer key, Map<IPattern, List<String>> value) {
        for (Entry<IPattern, List<String>> e : value.entrySet()) {
            Pair<IPattern, List<String>> pair = new Pair<IPattern, List<String>>(e.getKey(), e.getValue());
            inner.commit(key, pair);
        }
    }


    @Override
    public void close() {
        inner.close();
    }
}
