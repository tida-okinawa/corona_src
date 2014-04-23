/**
 * @file ClaimWorkPattern.java
 * @version $Id$
 * 
 * 2013/11/07 10:08:43
 * @author hajime-uchihara
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.bean.TmpTypePatternTableBean;
import com.tida_okinawa.corona.io.bean.WorkdatasBean;
import com.tida_okinawa.corona.io.dam.hibernate.connector.impl.ClaimWorkPatternDaoConnector;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.CorrectionMistakesType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.ICoronaDicPri;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.ResultCoronaPattern;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.io.model.table.TableType;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * @author hajime-uchihara
 * 
 */
public class ClaimWorkPattern extends ClaimWorkData implements IClaimWorkPattern {
    private String relDbName;
    private List<IPatternDic> pdics;

    /**
     * Integer: レコードID -> パターン結果
     */
    protected ClaimWorkPatternRecordMap _results;


    /**
     * コンストラクタ
     * 
     * @param claimId
     *            問い合わせデータID
     * @param fieldId
     *            フィールドID
     * @param type
     *            種別
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     * @param created
     *            作成フラグ
     */
    public ClaimWorkPattern(int claimId, int fieldId, ClaimWorkDataType workDataType, int projectId, int productId, boolean created) {
        super(claimId, fieldId, workDataType, projectId, productId);
        ClaimWorkPatternDaoConnector p_connector = new ClaimWorkPatternDaoConnector(this);

        /* パターン辞書の一覧 */
        pdics = new ArrayList<IPatternDic>();
        ICoronaProject project = IoService.getInstance().getProject(projectId);
        ICoronaProduct product = null;
        for (ICoronaProduct p : project.getProducts()) {
            if (p.getId() == productId) {
                product = p;
                break;
            }
        }

        /* TODO 解析に使ったものだけにしたい。(その場合、辞書リストが構築されていないので、解析後に辞書リストを作る) */
        if (!created) {/* 新規作成か、既存ワークデータ構築か？ */
            for (ICoronaDic dic : project.getDictionarys(IPatternDic.class)) {
                pdics.add((IPatternDic) dic);
            }
            for (ICoronaDic dic : product.getDictionarys(IPatternDic.class)) {
                pdics.add((IPatternDic) dic);
            }
        } else {
            /* TODO:テンポラリ辞書を取得 */
            int workdataId = getClaimWorkDataId(projectId, productId, claimId, workDataType);
            pdics.add(new TmpPatternDic(0, "", null, workdataId, this.getFieldId())); //$NON-NLS-1$
        }
        this._results = new ClaimWorkPatternRecordMap(p_connector, _records, pdics.toArray(new IPatternDic[pdics.size()]));
    }


    /**
     * @return relDbName
     */
    public String getRelDbName() {
        return relDbName;
    }


    /**
     * @param relDbName
     *            セットする relDbName
     */
    public void setRelDbName(String relDbName) {
        this.relDbName = relDbName;
    }


    @Override
    public boolean setClaimWorkPattern(int recordId, IResultCoronaPattern data) {
        super.addClaimWorkData(recordId, data.getData());
        assert data instanceof ResultCoronaPattern;
        ((ResultCoronaPattern) data).setId(recordId);
        /* TODO: Commitorからしか呼ばれないので、キューへ登録だけして、最後に読み直せば良いのでは？ */
        /* _results.put(recordId, data); */
        // TODO 要テスト。#817
        this._results.connector.commit(recordId, data.getHitPositions(0));

        return true;
    }


    @Override
    public IResultCoronaPattern getClaimWorkPattern(int recordId) {
        IResultCoronaPattern rt = this._results.get(recordId);
        return rt;
    }


    @Override
    public List<IResultCoronaPattern> getClaimWorkPatterns() {
        List<IResultCoronaPattern> list = new ArrayList<IResultCoronaPattern>();

        for (Entry<Integer, IResultCoronaPattern> entry : this._results.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }


    /**
     * @deprecated use {@link #setClaimWorkPattern(int, IResultCoronaPattern)}
     * @see com.tida_okinawa.corona.io.dam.mysql.ClaimWorkData#getClaimWorkDatas()
     */
    @Deprecated
    @Override
    public boolean addClaimWorkData(int recordId, String data) {
        throw new UnsupportedOperationException("未サポートメソッド"); //$NON-NLS-1$
    }


    @Deprecated
    @Override
    public boolean isExternalCorrectionMistakes() {
        throw new UnsupportedOperationException("未サポートメソッド"); //$NON-NLS-1$
    }


    @Deprecated
    @Override
    public boolean isInternalCorrectionMistakes() {
        throw new UnsupportedOperationException("未サポートメソッド"); //$NON-NLS-1$
    }


    @Deprecated
    @Override
    public boolean setCorrectionMistakesType(CorrectionMistakesType type) {
        throw new UnsupportedOperationException("未サポートメソッド"); //$NON-NLS-1$
    }


    @Override
    public boolean commit(IProgressMonitor monitor) {
        /* コミットしていないレコードをコミット */
        _results.commit();

        List<IPatternDic> pdics = new ArrayList<IPatternDic>();
        /* テンポラリパターン辞書の更新 */
        deleteTmpPatternDic(getWorkdataId(), getFieldId());
        copyTmpPatternDic(_results.getPatternDics(), getWorkdataId(), getFieldId());
        this._results.clearPatternDics();
        pdics.add(new TmpPatternDic(0, "", null, workdataId, this.getFieldId())); //$NON-NLS-1$
        this._results.setPatternDics(pdics.toArray(new IPatternDic[pdics.size()]));

        return super.commit(monitor);
    }


    @Override
    public boolean update() {
        boolean result = super.update();
        String tmp = this.getDbName();/* TODO:仮？なぜかクレームテーブル */
        this.setDbName(CoronaIoUtils.createWorkTableName(tmp, TableType.WORK_DATA, this._projectId));
        this.setRelDbName(CoronaIoUtils.createWorkTableName(tmp, TableType.RESULT_DATA, this._projectId));
        return result;
    }


    @Override
    public List<IResultCoronaPattern> getClaimWorkPatterns(String productName) {

        List<IResultCoronaPattern> list = new ArrayList<IResultCoronaPattern>();

        IClaimData claim = IoService.getInstance().getClaimData(this.getClaimId());
        for (IRecord rec : claim.getRecords()) {
            if (rec.getField(claim.getProductField()).getValue().equals(productName)) {
                list.add(this._results.get(rec.getRecordId()));
            }
        }
        return list;
    }


    @Override
    public void clearRelPattern() {
        _results.clear();
    }


    @Override
    public List<Integer> getRecord(IPattern pattern) {
        List<Integer> list = new ArrayList<Integer>();
        for (Entry<Integer, IResultCoronaPattern> entry : this._results.entrySet()) {
            // TODO 要テスト。#817
            if (entry.getValue().getHitPositions(0).containsKey(pattern)) {
                list.add(entry.getKey());
            }
        }
        return list;
    }


    @Override
    public PatternType[] getPatternTypes() {
        /*
         * この処理結果の辞書優先度を取得（使用したパターン辞書の、辞書ID）
         * Select type_id from dic_pattern where dic_id in(使った辞書id, ...) group
         * by type_id;
         * その辞書ID内のパターンで使用しているパターン分類を取得（パターン分類ID）
         * Select id, name From type_pattern Where id in(上のSQL);
         */

        StringBuilder sql = new StringBuilder(128);
        /* type_patternテーブルからでは、解析時の状態を取れないので、tmpから取得 */
        sql.append("Select id, name From ").append("tmp_type_pattern_").append(getWorkdataId()).append("_").append(getFieldId()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        /* この変更が適用される前に解析された結果を開いたときは、全部のパターン分類を返す */
        List<ICoronaDicPri> priorities = getRecentDicPriorities();
        if (priorities.size() > 0) {
            sql.append("Where id in("); //$NON-NLS-1$
            sql.append("Select type_id From dic_pattern Where dic_id in("); //$NON-NLS-1$
            StringBuilder id = new StringBuilder(64);
            for (ICoronaDicPri priority : priorities) {
                if (!priority.isInActive()) {
                    id.append(",").append(priority.getDicId()); //$NON-NLS-1$
                }
            }
            if (id.length() > 0) {
                id.delete(0, 1);
                sql.append(id);
                sql.append(") And inactive=false Group By type_id"); //$NON-NLS-1$
            } else {
                sql.setLength(sql.length() - 3);
            }
            sql.append(")"); //$NON-NLS-1$
        }
        try {
            Session session = IoService.getInstance().getSession();
            List<PatternType> types = new ArrayList<PatternType>();
            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(sql.toString()).list();
            for (Object[] rc : list) {
                int id = rc[0] == null ? 0 : Integer.parseInt(rc[0].toString()); //(int)rc[0];
                String name = (String) rc[1];
                types.add(new PatternType(id, name));
            }
            return types.toArray(new PatternType[types.size()]);
        } catch (HibernateException e) {
            e.printStackTrace();
        }

        return new PatternType[0];
    }


    @Override
    public PatternType[] getAllPatternTypes() {
        List<PatternType> ret = new ArrayList<PatternType>();
        try {
            /* SQL編集 */
            String strSQL = new StringBuilder(64).append("SELECT * FROM TMP_TYPE_PATTERN_") //$NON-NLS-1$
                    .append(this.workdataId).append("_") //$NON-NLS-1$
                    .append(this._fieldId).toString();

            /* SQL実行 */
            Session session = IoService.getInstance().getSession();
            @SuppressWarnings("unchecked")
            List<TmpTypePatternTableBean> list = session.createSQLQuery(strSQL).addEntity(TmpTypePatternTableBean.class).list();
            for (TmpTypePatternTableBean rc : list) {
                int id = rc.getId();
                String name = rc.getName();
                PatternType type = PatternType.getPatternType(id);
                if (type == null) {
                    PatternType t = new PatternType(id, name);
                    ret.add(t);
                    /* 解析後、消した分類がまたパターン辞書の分類コンボに出てきちゃうので、コメントアウト */
                    // PatternType.addPatternType(t);
                    //TODO 20131107 対応待ちの箇所
                    //                } else {
                    //                    // testH25 20131011 追加
                    //                    // testH25 20131015 チケット#1803に起票済み
                    //                    ret.add(type);
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }

        return ret.toArray(new PatternType[ret.size()]);
    }


    @Override
    public IPattern getPattern(int id) {
        return this._results.getPattern(id);
    }


    @Override
    public List<IPatternDic> getPatternDics() {
        return this.pdics;
    }


    @Override
    public void addPatternDic(List<IPatternDic> dics) {
        this.pdics = dics;
        this._results.setPatternDics(dics.toArray(new IPatternDic[dics.size()]));
    }


    /**
     * 構文解析用テンポラリパターン辞書/タイプコピー
     * 
     * @param iPatternDics
     *            保存する辞書
     * @param workId
     *            解析ID
     * @param fldId
     *            解析したフィールド
     * @return 処理成功ならtrue。現在は、常にtrue
     */
    private static boolean copyTmpPatternDic(IPatternDic[] iPatternDics, int workId, int fldId) {
        // DicPatternDaoより移設

        Session session = IoService.getInstance().getSession();

        try {
            /* トランザクション開始 */
            session.beginTransaction();

            /* 解析時のパターン辞書を記憶。解析結果エディタでポップアップするため */
            String dicTableName = new StringBuilder(27).append("tmp_dic_pattern_").append(workId).append("_").append(fldId).toString(); //$NON-NLS-1$ //$NON-NLS-2$
            StringBuilder strBuff = new StringBuilder(100);
            //strBuff.append("CREATE TABLE ").append(dicTableName).append(" LIKE DIC_PATTERN;"); //$NON-NLS-1$ //$NON-NLS-2$
            strBuff.append("CREATE TABLE ").append(dicTableName).append(" as select * from DIC_PATTERN;"); //$NON-NLS-1$ //$NON-NLS-2$
            session.createSQLQuery(strBuff.toString()).executeUpdate();
            session.flush();
            strBuff.setLength(0);
            strBuff.append("INSERT INTO ").append(dicTableName).append(" SELECT * FROM DIC_PATTERN"); //$NON-NLS-1$ //$NON-NLS-2$
            session.createSQLQuery(strBuff.toString()).executeUpdate();
            session.flush();

            /* 解析時のパターン分類を記憶。 */
            String typeTableName = new StringBuilder(28).append("tmp_type_pattern_").append(workId).append("_").append(fldId).toString(); //$NON-NLS-1$ //$NON-NLS-2$
            strBuff.setLength(0);
            strBuff.append("CREATE TABLE ").append(typeTableName).append(" as  SELECT * FROM TYPE_PATTERN;"); //$NON-NLS-1$ //$NON-NLS-2$
            //            strBuff.append("CREATE TABLE ").append(typeTableName).append(" LIKE TYPE_PATTERN;"); //$NON-NLS-1$ //$NON-NLS-2$
            session.createSQLQuery(strBuff.toString()).executeUpdate();
            session.flush();
            strBuff.setLength(0);
            strBuff.append("INSERT INTO ").append(typeTableName).append(" SELECT * FROM TYPE_PATTERN;"); //$NON-NLS-1$ //$NON-NLS-2$
            session.createSQLQuery(strBuff.toString()).executeUpdate();
            session.flush();

            /* トランザクションコミット */
            session.getTransaction().commit();

        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
        return true;
    }


    /**
     * 構文解析用テンポラリパターン辞書/タイプ削除
     * 
     * @param workId
     * @param fldId
     * @return
     */
    private static boolean deleteTmpPatternDic(int workId, int fldId) {
        // DicPatternDaoより移設

        boolean ret = true;
        Session session = IoService.getInstance().getSession();
        try {
            /* トランザクション開始 */
            session.beginTransaction();

            String strSQL = new StringBuilder(64).append("DROP TABLE IF EXISTS TMP_DIC_PATTERN_").append(workId).append("_").append(fldId).toString(); //$NON-NLS-1$ //$NON-NLS-2$
            session.createSQLQuery(strSQL).executeUpdate();
            session.flush();
            strSQL = new StringBuilder(64).append("DROP TABLE IF EXISTS TMP_TYPE_PATTERN_").append(workId).append("_").append(fldId).toString(); //$NON-NLS-1$ //$NON-NLS-2$
            session.createSQLQuery(strSQL).executeUpdate();
            session.flush();

            /* トランザクションコミット */
            session.getTransaction().commit();

        } catch (HibernateException e) {
            ret = false;
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
        return ret;
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
    private static int getClaimWorkDataId(int projectId, int productId, int claimId, ClaimWorkDataType type) {
        // ClaimWorkDataDaoより移設

        Session session = IoService.getInstance().getSession();
        String hql = "FROM WorkdatasBean WHERE projectId =:project AND productId =:product AND inputTableId =:claim AND type= :typeVal"; //$NON-NLS-1$
        int id = 0;
        try {
            @SuppressWarnings("unchecked")
            List<WorkdatasBean> workdatas = session.createQuery(hql).setInteger("project", projectId) //$NON-NLS-1$
                    .setInteger("product", productId) //$NON-NLS-1$
                    .setInteger("claim", claimId) //$NON-NLS-1$
                    .setInteger("typeVal", type.getIntValue()) //$NON-NLS-1$
                    .list();
            if (workdatas.size() > 0) {
                id = workdatas.get(0).getId();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            id = -1;
        }
        return id;
    }
}
