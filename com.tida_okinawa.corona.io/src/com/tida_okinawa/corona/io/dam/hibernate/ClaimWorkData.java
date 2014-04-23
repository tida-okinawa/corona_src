package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.io.bean.DicPriorityBean;
import com.tida_okinawa.corona.io.bean.DicPriorityPKBean;
import com.tida_okinawa.corona.io.bean.RecentDicPriBean;
import com.tida_okinawa.corona.io.bean.TablesBean;
import com.tida_okinawa.corona.io.bean.WorkdatasBean;
import com.tida_okinawa.corona.io.dam.hibernate.connector.impl.ClaimWorkDataDaoConnector;
import com.tida_okinawa.corona.io.dam.hibernate.connector.impl.CorrectionMistakesDataDaoConnector;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.CorrectionMistakesType;
import com.tida_okinawa.corona.io.model.ICoronaDicPri;
import com.tida_okinawa.corona.io.model.abstraction.AbstractClaimWorkData;
import com.tida_okinawa.corona.io.model.table.ITextRecord;

/**
 * @author Kamakura
 * 
 */
public class ClaimWorkData extends AbstractClaimWorkData implements Cloneable {
    protected int workTableId = 0;
    protected int workdataId = 0;
    protected String name = null;
    protected String dbName = null;


    /**
     * @param claimId
     *            問い合わせID
     * @param fieldId
     *            フィールドID
     * @param workDataType
     *            種別
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     */
    public ClaimWorkData(int claimId, int fieldId, ClaimWorkDataType workDataType, int projectId, int productId) {
        super(claimId, fieldId, workDataType, projectId, productId);
        ClaimWorkDataDaoConnector connector;

        if (ClaimWorkDataType.CORRECTION_MISTAKES.equals(workDataType)) {
            /* 誤記補正の場合、誤記補正用のDaoConnectorを使用する */
            connector = new CorrectionMistakesDataDaoConnector(this);
        } else {
            /* 誤記補正以外の場合、現行のClaimWorkDataDaoConnectorを使用 */
            connector = new ClaimWorkDataDaoConnector(this);
        }
        _records = new ClaimWorkDataRecordList(connector);
    }


    /**
     * 名前設定
     * 
     * @param name
     *            問い合わせデータ名
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * 名前取得
     * 
     * @return 名前
     */
    public String getName() {
        return name;
    }


    /**
     * DB名を設定する
     * 
     * @param dbName
     *            テーブル名
     */
    public void setDbName(String dbName) {
        if (dbName == null) {
            System.err.println(new Exception("セットしようとしているdbNameがnullです(" + toString()).getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.dbName = dbName;
    }


    /**
     * DB名を取得
     * 
     * @return テーブル名
     */
    public String getDbName() {
        if (dbName == null)
            System.err.println(new Exception("dbName is null in " + toString()).getMessage()); //$NON-NLS-1$
        return dbName;
    }


    /**
     * 内部で誤記補正処理を行ったかどうかを見るフラグ
     * 
     * @param flg
     *            誤記補正処理フラグ
     */
    public void setInternalCorrectionMistakes(boolean flg) {
        this.internal = flg;
    }


    /**
     * 外部で誤記補正したデータを取り込んだかどうかを見るフラグ
     * 
     * @param flg
     *            誤記補正処理フラグ
     */
    public void setExternalCorrectionMistakes(boolean flg) {
        this.external = flg;
    }


    /**
     * 問い合わせ中間データテーブルIDを返す
     * 
     * @return workTableId
     */
    public int getWorkTableId() {
        return workTableId;
    }


    /**
     * 問い合わせ中間データテーブルIDをセットする
     * 
     * @param workTableId
     *            問い合わせ中間データテーブルID
     */
    public void setWorkTableId(int workTableId) {
        this.workTableId = workTableId;
    }


    @Override
    public void setLasted(Date date) {
        super.setLasted(date);
    }


    @Override
    public int getWorkdataId() {
        return workdataId;
    }


    /**
     * 問い合わせ中間データIDをセットする
     * 
     * @param workdataId
     *            問い合わせ中間データテーブルID
     */
    public void setWorkdataId(int workdataId) {
        this.workdataId = workdataId;
    }


    @Deprecated
    @Override
    public boolean beginTransaction() {

        /* オートコミットをオフ */
        /* トランザクションを開始 */
        /* TODO 自動生成されたメソッド・スタブ */
        throw new UnsupportedOperationException("まだ未実装"); //$NON-NLS-1$
    }


    @Deprecated
    @Override
    public boolean commitTransaction() {

        /* コミットし、トランザクションを完了 */
        /* オートコミットをオン */
        /* TODO 自動生成されたメソッド・スタブ */
        throw new UnsupportedOperationException("まだ未実装"); //$NON-NLS-1$
    }


    @Deprecated
    @Override
    public boolean rollbackTransaction() {

        /* ロールバックし、トランザクションを完了 */
        /* オートコミットをオン */
        /* TODO 自動生成されたメソッド・スタブ */
        throw new UnsupportedOperationException("まだ未実装"); //$NON-NLS-1$
    }


    @Override
    public boolean update() {
        Session session = IoService.getInstance().getSession();
        boolean result = false;

        try {
            if (getClaimWorkDataType() == ClaimWorkDataType.CORRECTION_MISTAKES) {
                StringBuilder sql = new StringBuilder(128);
                //                sql.append("FROM TablesBean i,TablesBean o, ClaimsBean c , WorkdatasBean w ").append("WHERE i.id = c.id AND ") //$NON-NLS-1$ //$NON-NLS-2$
                //                        .append("o.id = c.workTableId AND ").append("w.inputTableId = i.id AND ").append("i.id = :claimId AND ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                //                        .append("c.targetFields like :fieldId "); //$NON-NLS-1$

                sql.append("FROM TablesBean i,TablesBean o, ClaimsBean c , WorkdatasBean w ").append("WHERE i.id = c.id AND ") //$NON-NLS-1$ //$NON-NLS-2$
                        .append("o.id = c.workTableId AND ").append("w.projectId = 0 AND ").append("w.productId = 0 AND ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .append("w.inputTableId = i.id AND ").append("i.id = :claimId AND ") //$NON-NLS-1$ //$NON-NLS-2$
                        .append("c.targetFields like :fieldId "); //$NON-NLS-1$

                @SuppressWarnings("unchecked")
                List<Object[]> list = session.createQuery(sql.toString()).setInteger("claimId", getClaimId()) //$NON-NLS-1$
                        //                        .setString("fieldId", "%" + getFieldId() + "%").list(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .setString("fieldId", "%" + String.format("%03d", getFieldId()) + "%").list(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

                for (Object[] objects : list) {
                    // Tables i = (Tables)objects[0];
                    TablesBean o = (TablesBean) objects[1];
                    // Claim c = (Claim)objects[2];
                    WorkdatasBean w = (WorkdatasBean) objects[3];

                    setWorkTableId(o.getId());
                    setName(o.getName());
                    setDbName(o.getDbname());
                    setLasted(w.getLasted());
                    setWorkdataId(w.getId());
                    setNote(w.getLink());

                    result = true;
                }
            } else {
                switch (getClaimWorkDataType()) {
                case BASE:
                case LASTED:
                    throw new UnsupportedOperationException("未実装"); //$NON-NLS-1$
                default:
                    /* テーブル一覧から問い合わせデータのみを取得 */
                    StringBuilder sql = new StringBuilder(512);
                    sql.append("Select o.ID TBL_ID, o.NAME NAME, o.DBNAME DBNAME,w.ID WORK_ID, w.LASTED LASTED, w.LINK LINK "); //$NON-NLS-1$
                    sql.append("From TABLES i,TABLES o, WORKDATAS w, REL_CLM_PRODUCT r "); //$NON-NLS-1$
                    sql.append("Where i.ID=r.TBL_ID And o.ID=r.WORK_TBL_ID And w.PROJECT_ID=r.PRJ_ID"); //$NON-NLS-1$
                    sql.append(" And w.PRODUCT_ID=r.PRODUCT_ID And r.TBL_ID=w.INPUT_TABLE_ID"); //$NON-NLS-1$
                    sql.append(" And i.ID=").append(getClaimId()).append(" And r.PRJ_ID=").append(this._projectId); //$NON-NLS-1$ //$NON-NLS-2$
                    sql.append(" And r.PRODUCT_ID=").append(this._productId).append(" And w.TYPE=").append(getClaimWorkDataType().getIntValue()); //$NON-NLS-1$ //$NON-NLS-2$
                    sql.append(" And r.TGT_FLDS Like '%").append(String.format("%03d", getFieldId())).append("%'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                    @SuppressWarnings("unchecked")
                    List<Object[]> list = session.createSQLQuery(sql.toString()).list();
                    for (Object[] rs : list) {
                        setWorkTableId(rs[0] == null ? 0 : Integer.parseInt(rs[0].toString()));
                        setName((String) rs[1]);
                        setDbName((String) rs[2]);
                        setLasted((Date) rs[4]);
                        setWorkdataId(rs[3] == null ? 0 : Integer.parseInt(rs[3].toString()));
                        setNote((String) rs[5]);
                    }
                    result = true;
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public boolean commit(IProgressMonitor monitor) {
        return commit(true, monitor);
    }


    @Override
    public boolean commit(boolean bRecords, IProgressMonitor monitor) {
        /* 構築されていない場合 */
        if (_claimId == 0) {
            update();
        }

        /* コミットしていないレコードをコミット */
        if (bRecords) {
            _records.commit();
        }

        Session session = null;

        try {

            session = IoService.getInstance().getSession();
            /* クレンジング元履歴IDをコミット */
            int formerHistoryId = 1;
            if (!ClaimWorkDataType.CORRECTION_MISTAKES.equals(getClaimWorkDataType())) {
                /* 誤記補正以外の場合、クレンジング元履歴ID更新対象 */
                /* クレンジング元履歴IDを1に設定する(形態素・係り受け登録時の値) */
                if (!ClaimWorkDataType.DEPENDENCY_STRUCTURE.equals(getClaimWorkDataType())) {
                    /* 形態素・係り受け以外の場合、クレンジング元履歴IDの最新を取得 */
                    formerHistoryId = getUsrWorkHistoryId();
                }
            }

            /* WORKDATASの更新(ノート、更新日、クレンジング元履歴ID) */
            WorkdatasBean workDatas = (WorkdatasBean) session.get(WorkdatasBean.class, getWorkdataId());
            if (workDatas != null) {
                if (formerHistoryId > 0) {
                    workDatas.setFormerHistoryId(formerHistoryId);
                }
                workDatas.setLink(this.getNote());
                workDatas.setLasted(new Date());
                /* トランザクション開始 */
                session.beginTransaction();
                session.save(workDatas);
                session.flush();
                /* トランザクションコミット */
                session.getTransaction().commit();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
        setLasted(new Date(System.currentTimeMillis()));
        return true;
    }


    /**
     * ユーザワークテーブル最新履歴ID取得
     * 対象の１つ前の入力種別データを元に最新の履歴IDを取得する
     * 
     * @return ユーザワークテーブルの種別に対する最新履歴ID、取得できなかった場合
     *         もしくはクレンジング元の種別タイプが空白だった場合、0を返す
     */
    protected int getUsrWorkHistoryId() {
        /* クレンジング元の履歴IDを取得する。 */
        String[] types = this.getNote().split(","); //$NON-NLS-1$
        String dataType = ""; //$NON-NLS-1$
        /* 入力データ種別と自分の種別の２つはあるはず　 */
        if (types.length > 1) {
            dataType = types[types.length - 2];
        }
        if (dataType.contains(":")) { //$NON-NLS-1$
            dataType = dataType.substring(0, dataType.indexOf(":")); //$NON-NLS-1$
        }
        if (!"".equals(dataType)) { //$NON-NLS-1$
            /* 履歴ID取得 */
            Session session = IoService.getInstance().getSession();
            int ret = 0;
            try {
                // SQL作成
                StringBuilder sql = new StringBuilder(192);
                sql.append("SELECT MAX(usrWork.HISTORY_ID) FROM WORKDATAS work, "); //$NON-NLS-1$
                sql.append(getDbName()).append(" usrWork").append(" WHERE ").append("work.id = usrWork.work_id").append(" AND ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        .append(" work.input_table_id = ").append(getClaimId()).append(" AND ").append(" work.project_id = ").append(getProjectId()) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .append(" AND ").append(" work.product_id = ").append(getProductId()).append(" AND ").append(" work.type = ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        .append(ClaimWorkDataType.valueOfName(dataType).getIntValue());
                ret = (int) (session.createSQLQuery(sql.toString()).uniqueResult());
            } catch (ClassCastException ex) {
                ex.printStackTrace();
                ret = -1;
            } catch (HibernateException e) {
                e.printStackTrace();
                ret = -1;
            }
            return ret;
        }
        return 0;
    }


    @Override
    public boolean setCorrectionMistakesType(CorrectionMistakesType type) {
        return true;
    }


    @Override
    public String getClaimWorkData(int recordId) {
        ITextRecord record = _records.getRecord(recordId);
        if (record == null) {
            /* TODO 自動生成されるIDフィールドに誤記補正をかけるとここにくるっぽい */
            return null;
        }
        return record.getText();
    }


    @Override
    public String getClaimWorkData(int recordId, int historyId) {
        ITextRecord record = _records.getRecord(recordId, historyId);
        if (record == null) {
            /* TODO 自動生成されるIDフィールドに誤記補正をかけるとここにくるっぽい */
            return null;
        }
        return record.getText();
    }


    @Override
    public boolean addClaimWorkData(int recordId, String data) {
        return super.addClaimWorkData(recordId, data);
    }


    @Override
    public List<ITextRecord> getClaimWorkDatas() {
        return super.getClaimWorkDatas();
    }


    @Override
    public List<ITextRecord> getClaimWorkDatas(String productName) {
        return super.getClaimWorkDatas();
    }


    @Override
    public Object clone() {
        try {
            ClaimWorkData clone = (ClaimWorkData) super.clone();
            ClaimWorkDataDaoConnector connector;

            if (ClaimWorkDataType.CORRECTION_MISTAKES.equals(clone.getClaimWorkDataType())) {
                // 誤記補正の場合、CorrectionMistakesDataDaoConnectorでコピー
                connector = new CorrectionMistakesDataDaoConnector(clone);
            } else {
                // 誤記補正以外の場合、ClaimWorkDataDaoConnectorでコピー
                connector = new ClaimWorkDataDaoConnector(clone);
            }

            clone._records = new ClaimWorkDataRecordList(connector);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }


    @Override
    public List<ITextRecord> getClaimWorkDatas(String productName, int page) {
        /* 構築されていない場合 */
        if (_claimId == 0) {
            update();
        }
        if (_claimId == 0) {
            return null;
        }
        return super.getClaimWorkDatas();
    }


    @Override
    protected String getDispIdDam(int claimId, int recId) {

        ClaimData claim = (ClaimData) IoService.getInstance().getClaimData(this.getClaimId());
        String recordIdDBField = claim.getFieldInformation(claim.getDispIdField()).getName();

        Session session = IoService.getInstance().getSession();
        StringBuilder sql = new StringBuilder(128);
        sql.append("SELECT ").append(recordIdDBField).append(" FROM ").append(claim.getTableName()); //$NON-NLS-1$ //$NON-NLS-2$
        sql.append(" WHERE ID = ").append(recId); //$NON-NLS-1$
        String ret = null;
        try {
            ret = (String) session.createSQLQuery(sql.toString()).uniqueResult();
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + sql.toString()); //$NON-NLS-1$
            e.printStackTrace();
        }
        return ret;
    }


    @Override
    public List<ICoronaDicPri> getDicPrioritys() {
        return doGetDicPri(getWorkdataId(), getFieldId(), "dic_pri"); //$NON-NLS-1$
    }


    @Override
    public void setDicPrioritys(List<ICoronaDicPri> list) {
        int priority = 1;
        Session session = null;
        try {
            session = IoService.getInstance().getSession();
            /* トランザクション開始 */
            session.beginTransaction();
            // TODO 20131121 PKの指定が不足しているので.get()は使用できず
            String strSQL = "DELETE FROM DIC_PRI WHERE ID = " + getWorkdataId() + " AND FLD_ID = " + getFieldId(); //$NON-NLS-1$ //$NON-NLS-2$
            session.createSQLQuery(strSQL).executeUpdate();
            session.flush();
            /* トランザクション確定 */
            session.getTransaction().commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
        /* 辞書プライオリティの追加 */
        for (ICoronaDicPri pri : list) {
            insertDicPri(getWorkdataId(), getFieldId(), pri.getDicId(), priority++, pri.isInActive(), false);
        }
        setRecentDicPriorities(list);
    }


    @Override
    public ICoronaDicPri createDicPriority(int dicId) {
        return new CoronaDicPri(dicId);
    }


    @Override
    public boolean deleteDicPriority(int dicId) {
        Session session = null;
        try {
            session = IoService.getInstance().getSession();
            DicPriorityPKBean pk = new DicPriorityPKBean();
            pk.setId(getWorkdataId());
            pk.setFieldId(getFieldId());
            pk.setDicId(dicId);
            DicPriorityBean dicPriority = (DicPriorityBean) session.get(DicPriorityBean.class, pk);
            /* トランザクション開始 */
            session.beginTransaction();
            if (dicPriority != null) {
                session.delete(dicPriority);
                session.flush();
            }
            /* トランザクション確定 */
            session.getTransaction().commit();
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
        }
        return false;
    }


    @Override
    public boolean addDicPriority(int dicId) {
        List<ICoronaDicPri> list = getDicPrioritys();
        if (list.size() == 0) {
            return false;
        }
        insertDicPri(getWorkdataId(), getFieldId(), dicId, -1, true, true);
        return true;
    }


    @Override
    public List<ICoronaDicPri> getDicPrioritysCom() {
        return doGetDicPri(getWorkdataId(), 0, "dic_pri"); //$NON-NLS-1$

    }


    @Override
    public void setDicPrioritysCom(List<ICoronaDicPri> list) {
        int priority = 1;
        Session session = null;
        try {

            session = IoService.getInstance().getSession();
            // TODO 20131121 PKの指定が不足しているので.get()は使用できず
            StringBuilder strSQL = new StringBuilder(128);
            strSQL.append("DELETE FROM DIC_PRI WHERE ID = ").append(getWorkdataId()).append(" AND FLD_ID = 0"); //$NON-NLS-1$ //$NON-NLS-2$
            /* トランザクション開始 */
            session.beginTransaction();
            session.createSQLQuery(strSQL.toString()).executeUpdate();
            session.flush();
            /* トランザクション確定 */
            session.getTransaction().commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
        for (ICoronaDicPri pri : list) {
            insertDicPri(getWorkdataId(), 0, pri.getDicId(), priority++, pri.isInActive(), false);
        }
        setRecentDicPriorities(list);
    }


    @Override
    public boolean deleteDicPriorityCom(int dicId) {
        Session session = null;
        try {
            session = IoService.getInstance().getSession();
            DicPriorityPKBean pk = new DicPriorityPKBean();
            pk.setId(getWorkdataId());
            pk.setFieldId(0);
            pk.setDicId(dicId);
            DicPriorityBean dicPriority = (DicPriorityBean) session.get(DicPriorityBean.class, pk);
            if (dicPriority != null) {
                /* トランザクション開始 */
                session.beginTransaction();
                session.delete(dicPriority);
                session.flush();
                /* トランザクション確定 */
                session.getTransaction().commit();
            }
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
        }
        return false;
    }


    @Override
    public boolean addDicPriorityCom(int dicId) {
        List<ICoronaDicPri> list = getDicPrioritysCom();
        if (list.size() == 0) {
            return false;
        }
        insertDicPri(getWorkdataId(), 0, dicId, -1, true, true);
        return true;
    }


    /**
     * 履歴IDを返却(メモリ上の値)
     * 
     * @return 履歴ID
     */
    public int getHistoryId() {
        return _records.historyId;
    }


    /**
     * Memo 現状、UIから呼ぶ予定はないので、interfaceには公開していない
     * 
     * @return 直近の処理で使用した辞書のリスト
     */
    public List<ICoronaDicPri> getRecentDicPriorities() {
        /*
         * 構文解析結果エディタで、解析に使われていないパターン分類を出さない対応で作成
         */

        // テーブル「recent_dic_pri」は固定でhibernate版では存在する運用となるので、作成処理は不要

        int workId = getWorkdataId();
        int fldId = getFieldId();
        Session session = IoService.getInstance().getSession();

        List<ICoronaDicPri> ret = new ArrayList<ICoronaDicPri>();
        StringBuilder sql = new StringBuilder(128);
        sql.append("From RecentDicPriBean Where primaryKey.id=:workId And primaryKey.fieldId=:fldId Order By priority"); //$NON-NLS-1$
        try {
            @SuppressWarnings("unchecked")
            List<RecentDicPriBean> recentDicPri = session.createQuery(sql.toString()).setInteger("workId", workId).setInteger("fldId", fldId).list(); //$NON-NLS-1$ //$NON-NLS-2$
            for (RecentDicPriBean rc : recentDicPri) {
                ret.add(new CoronaDicPri(rc.getPrimaryKey().getDicId(), rc.isInactive(), rc.getPriority()));
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return ret;
    }


    private void setRecentDicPriorities(List<ICoronaDicPri> priorities) {
        int workId = getWorkdataId();
        int fieldId = getFieldId();

        Session session = null;
        try {
            session = IoService.getInstance().getSession();
            /* 前回値を消す */
            StringBuilder delSql = new StringBuilder(128);
            delSql.append("Delete From recent_dic_pri Where id=").append(workId).append(" And fld_id=").append(fieldId); //$NON-NLS-1$ //$NON-NLS-2$
            /* トランザクション開始 */
            session.beginTransaction();
            session.createSQLQuery(delSql.toString()).executeUpdate();
            session.flush();

            if (priorities.size() > 0) {
                StringBuilder sql = new StringBuilder(256 * priorities.size());
                /* VALUESを作成 */
                int priority = 1;
                for (ICoronaDicPri pri : priorities) {
                    sql.append(",(").append(workId).append(",").append(fieldId).append(","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    sql.append(pri.getDicId()).append(",").append(priority++).append(",").append(pri.isInActive()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                sql.delete(0, 1).append(" "); /* カンマ除去 *///$NON-NLS-1$

                /* 命令部を先頭に付加 */
                StringBuilder commandStr = new StringBuilder(192);
                commandStr.append("MERGE Into recent_dic_pri (ID, FLD_ID, DIC_ID, PRIORITY, INACTIVE) "); //$NON-NLS-1$
                commandStr.append(" KEY(ID, FLD_ID, DIC_ID) VALUES"); //$NON-NLS-1$
                sql.insert(0, commandStr.toString());
                //                sql.insert(0, "Insert Into recent_dic_pri (ID, FLD_ID, DIC_ID, PRIORITY, INACTIVE) VALUES"); //$NON-NLS-1$
                //                sql.append("ON DUPLICATE KEY UPDATE PRIORITY=VALUES(PRIORITY), INACTIVE=VALUES(INACTIVE)"); //$NON-NLS-1$
                session.createSQLQuery(sql.toString()).executeUpdate();
                session.flush();
            }
            /* トランザクション確定 */
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


    @Override
    public void upgreadHistoryId() {
        _records.upgradeHistoryId();
    }


    @Override
    public int getFormerHistoryId() {
        int ret = 0;
        try {
            Session session = IoService.getInstance().getSession();
            WorkdatasBean workDatas = (WorkdatasBean) session.get(WorkdatasBean.class, workdataId);
            if (workDatas != null) {
                ret = workDatas.getFormerHistoryId();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            ret = -1;
        }
        return ret;
    }


    @Override
    public int getCleansingHistoryId() {
        int ret = 0;
        try {
            Session session = IoService.getInstance().getSession();
            WorkdatasBean workDatas = (WorkdatasBean) session.get(WorkdatasBean.class, workdataId);
            if (workDatas != null) {
                ret = workDatas.getHistoryId();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            ret = -1;
        }
        return ret;
    }


    @Override
    public void clearWorkData() {
        if (_records.records != null)
            _records.records.clear();

        StringBuilder sql = new StringBuilder(192);
        sql.append("DELETE FROM ").append(this.dbName); //$NON-NLS-1$
        sql.append(" WHERE WORK_ID = ").append(getWorkdataId()); //$NON-NLS-1$
        sql.append(" AND FLD_ID = ").append(getFieldId()); //$NON-NLS-1$
        sql.append(" AND HISTORY_ID = ").append(getHistoryId()); //$NON-NLS-1$

        Session session = null;
        try {
            session = IoService.getInstance().getSession();
            /* トランザクション開始 */
            session.beginTransaction();
            session.createSQLQuery(sql.toString()).executeUpdate();
            session.flush();
            /* トランザクション確定 */
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
     * @param workId
     *            作業ID
     * @param fieldId
     *            フィールドID
     * @param tableName
     *            only dic_pri or recent_dic_pri
     * @return
     */
    private static List<ICoronaDicPri> doGetDicPri(int workId, int fieldId, String tableName) {

        /* DicPriDaoより移設する */

        List<ICoronaDicPri> ret = new ArrayList<ICoronaDicPri>();
        try {
            StringBuilder sql = new StringBuilder(128);
            // #1969 優先度テーブルから優先順位のデータを取得する際、削除されている辞書は除外する条件を追加
            sql.append("Select pri.dic_id, pri.inactive, pri.priority From ").append(tableName).append(" pri, dic_table table ");
            sql.append(" Where pri.id=").append(workId).append(" And pri.fld_id=").append(fieldId);
            sql.append(" And pri.dic_id = table.dic_id And table.inactive = false");
            sql.append(" Order By pri.priority"); //$NON-NLS-1$ 
            Session session = IoService.getInstance().getSession();

            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(sql.toString()).list();
            if (list != null) {
                for (Object[] rs : list) {
                    int para0 = rs[0] == null ? 0 : Integer.parseInt(rs[0].toString());
                    int para1 = rs[1] == null ? 0 : ("false".equals(rs[1].toString()) ? 0 : 1); //$NON-NLS-1$
                    int para2 = rs[2] == null ? 0 : Integer.parseInt(rs[2].toString());
                    ret.add(new CoronaDicPri(para0, para1 == 0 ? false : true, para2));
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return ret;
    }


    /**
     * @param workId
     *            作業ID
     * @param fldId
     *            フィールドID
     * @param dicId
     *            辞書ID
     * @param priority
     *            ０以下なら、優先度最低とする
     * @param inactive
     *            INACTIVE
     * @param add
     *            優先リストにすでに同じ値があるときは何もしないならtrue。すでにある値の更新ならfalse
     */
    private static void insertDicPri(int workId, int fldId, int dicId, int priority, boolean inactive, boolean add) {

        /* DicPriDaoから移設 */

        if (priority <= 0) {
            /* #1301 クレンジング時プライオリティ値がマイナスの場合はマイナスにする */
            priority = getMinPriority(workId, fldId);
            priority--;
        }

        Session session = IoService.getInstance().getSession();

        if (add) {
            int pri = 0;
            try {
                /* CommonDao.getFirstInt を置換 */
                DicPriorityPKBean pk = new DicPriorityPKBean();
                pk.setId(workId);
                pk.setFieldId(fldId);
                pk.setDicId(dicId);
                DicPriorityBean dicPri = (DicPriorityBean) session.get(DicPriorityBean.class, pk);
                if (dicPri != null) {
                    pri = dicPri.getPriority();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                pri = -1;
            }
            if (pri > 0) {
                return;
            }
        }

        // 最後にマッピング
        StringBuilder strSQL = new StringBuilder(128);
        strSQL.append("MERGE INTO DIC_PRI (ID, FLD_ID, DIC_ID, PRIORITY, INACTIVE) KEY (ID, FLD_ID, DIC_ID) "); //$NON-NLS-1$
        strSQL.append("VALUES (").append(workId).append(", ").append(fldId).append(", ").append(dicId).append(", ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                .append(priority).append(",").append(inactive).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            /* トランザクション開始 */
            session.beginTransaction();
            /* CommonDao.executeSQL(strSQL); を置換する。 */
            session.createSQLQuery(strSQL.toString()).executeUpdate();
            session.flush();
            /* トランザクション確定 */
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
     * #1301 プライオリティの最小値取得
     * 
     * @param workId
     * @param fieldId
     * @return
     */
    private static int getMinPriority(int workId, int fieldId) {

        /* DicPriDaoから移設 */

        StringBuilder sql = new StringBuilder(128);
        sql.append("Select min(priority) From dic_pri Where id=").append(workId).append(" And fld_id=").append(fieldId); //$NON-NLS-1$ //$NON-NLS-2$
        int min = 0;
        try {
            /* CommonDao.getFirstInt(sql.toString()); を置換する。 */
            Session session = IoService.getInstance().getSession();
            min = (int) (long) session.createSQLQuery(sql.toString()).uniqueResult();
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + sql.toString()); //$NON-NLS-1$
            e.printStackTrace();
            min = -1;
        }
        if (min > 0) {
            min = 0;
        }
        return min;
    }
}
