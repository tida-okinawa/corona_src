package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.io.bean.ClaimsBean;
import com.tida_okinawa.corona.io.bean.FieldsBean;
import com.tida_okinawa.corona.io.bean.TablesBean;
import com.tida_okinawa.corona.io.bean.WorkdatasBean;
import com.tida_okinawa.corona.io.dam.hibernate.connector.impl.ClaimDataDaoConnector;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimDataRecordList;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.abstraction.AbstractClaimData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.IRecord;
import com.tida_okinawa.corona.io.model.table.ITextRecord;
import com.tida_okinawa.corona.io.model.table.impl.FieldHeader;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * @author Kamakura
 * 
 */
public final class ClaimData extends AbstractClaimData {

    private boolean bRefreshRecords = false;/* レコードリフレッシュ用フラグ */


    /**
     * @param Name
     *            問い合わせデータ名
     * @param dbName
     *            テーブル名
     * @param id
     *            ID
     */
    public ClaimData(String Name, String dbName, int id) {
        super(Name, dbName, id);

        ClaimDataDaoConnector connector = new ClaimDataDaoConnector(this);
        _records = new ClaimDataRecordList(connector);
    }


    @Override
    protected IClaimWorkData getCorrectionMistakesDam(int fieldNo) {
        if (!bRefreshRecords) {
            updateInformations();
        }

        for (IClaimWorkData workData : _correctionMistakes) {
            if (workData.getClaimId() == getId() && workData.getFieldId() == fieldNo) {
                return workData;
            }
        }
        return null;
    }


    /**
     * ターゲット列よりターゲット名を取得する
     * 
     */
    @Override
    protected List<String> getProductsDam() {
        /* 範囲外チェック */
        if (this._productField <= 0 || this._headers.size() < this._productField) {
            return null;
        }

        List<String> strs = new ArrayList<String>();
        try {
            /* SQL生成 */
            StringBuilder sql = new StringBuilder(128);
            sql.append("SELECT ").append(this._headers.get(this._productField - 1).getName()).append(" FROM ").append(this._dbName); //$NON-NLS-1$ //$NON-NLS-2$
            sql.append(" GROUP BY ").append(this._headers.get(this._productField - 1).getName()); //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            /* SQL実行 */
            @SuppressWarnings("unchecked")
            List<Object> list = session.createSQLQuery(sql.toString()).list();

            /* SELECT結果編集 */
            if (list != null) {
                for (Object rs : list) {
                    /* ターゲット名を取得 */
                    String name = CoronaIoUtils.convertToString(rs);
                    if (name != null) {
                        name = name.trim();
                        if (name.length() == 0) {
                            name = Messages.ClaimData_NoNameTarget;
                        }
                    } else {
                        name = Messages.ClaimData_NoNameTarget;
                    }
                    strs.add(name);
                }
            }

        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return strs;
    }


    /**
     * {@link #getFieldInformations()}の中身を更新する
     * 
     * @return 更新出来た場合はtrue
     */
    private boolean updateFieldHeader() throws HibernateException {
        Session session = IoService.getInstance().getSession();

        /* カラム情報取得 */
        String[] columns = null;
        try {
            /*
             * String[] columns = CommonDao.getColumsInfo(this.getTableName());
             * を置換する。
             */
            StringBuilder strSQL = new StringBuilder(128);
            strSQL.append("SHOW COLUMNS FROM ").append(this.getTableName());//$NON-NLS-1$

            List<String> tableColumns = new ArrayList<String>();

            @SuppressWarnings("unchecked")
            List<Object[]> columnslist = session.createSQLQuery(strSQL.toString()).list();
            if (columnslist != null) {
                for (Object[] rs : columnslist) {
                    tableColumns.add((String) rs[0]);
                }
                columns = tableColumns.toArray(new String[tableColumns.size()]);
            }
        } catch (HibernateException ex) {
            throw ex;
        }

        for (int i = 0; i < columns.length; i++) {
            IFieldHeader header = null;
            if (_headers.size() > 0) {
                for (IFieldHeader h : _headers) {
                    if (h.getName().equals(columns[i])) {
                        header = h;
                        break;
                    }
                }
            }

            /* フィールド表示名を取得する。 */
            String name = null;
            String table_Id = "table_Id"; //$NON-NLS-1$
            String db_Name = "db_Name"; //$NON-NLS-1$
            /* ClaimDataDao.getFieldDispName(getId(), columns[i]) を置換する。 */
            StringBuilder sql = new StringBuilder(128);
            sql.append("FROM FieldsBean WHERE tableId = :").append(table_Id).append(" AND dbName = :").append(db_Name); //$NON-NLS-1$ //$NON-NLS-2$
            try {
                @SuppressWarnings("unchecked")
                List<FieldsBean> list = session.createQuery(sql.toString()).setInteger(table_Id, getId()).setString(db_Name, columns[i]).list();
                if (list != null) {
                    for (FieldsBean rs : list) {
                        name = rs.getName();
                    }
                }
            } catch (HibernateException ex) {
                CoronaActivator.debugLog("Error SQL : " + sql); //$NON-NLS-1$
                ex.printStackTrace();
            }
            if ((name == null) || name.isEmpty()) {
                /* 無かったら、DBフィールド名 */
                name = columns[i];
            }
            if (header == null) {
                header = new FieldHeader(columns[i], name, "", i + 1); //$NON-NLS-1$
                this._headers.add(header);
            } else {
                header.setDispName(name);
            }

        }
        return true;
    }


    @Override
    public boolean update() {
        boolean updHead = false;

        /* Memo 毎回更新する意味あんのか？ */
        /* フィールドヘッダ更新 */
        try {
            updHead = updateFieldHeader();
        } catch (HibernateException e) {
            e.printStackTrace();
        }

        /* 問い合わせ＋誤記補正情報更新 */
        boolean updInfo = updateInformations();
        /* 中間データ更新 */
        for (IClaimWorkData work : _correctionMistakes) {
            work.update();
        }
        return updHead & updInfo;
    }


    /**
     * 情報更新<br>
     * メモリ上の、問い合わせデータと誤記補正データの主要情報を更新する。
     * 
     * @return always true
     */
    private boolean updateInformations() {
        Session session = IoService.getInstance().getSession();

        try {
            StringBuilder sql = new StringBuilder("From TablesBean i, TablesBean o, ClaimsBean c where i.id=c.id and o.id=c.workTableId AND i.id=:id"); //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createQuery(sql.toString()).setInteger("id", getId()).list(); //$NON-NLS-1$

            for (Object[] objects : list) {
                TablesBean o = (TablesBean) objects[1];
                ClaimsBean c = (ClaimsBean) objects[2];

                setLasted(o.getLasted());
                setDispIdField(c.getKeyFieldId());
                setProductField(c.getProductFieldId());

                _miningFields = CoronaIoUtils.stringToIntSet(c.getTargetFields());
                if (_miningFields.size() > 0) {
                    for (int fieldId : _miningFields) {
                        ClaimWorkData work = null;
                        if (fieldId > 0 && _correctionMistakes.size() > 0) {
                            for (IClaimWorkData claimWorkData : _correctionMistakes) {
                                if (fieldId == claimWorkData.getFieldId()) {
                                    work = (ClaimWorkData) claimWorkData;
                                    break;
                                }
                            }
                        }
                        if (work == null) {
                            _correctionMistakes.add(work = new ClaimWorkData(getId(), fieldId, ClaimWorkDataType.CORRECTION_MISTAKES, 0, 0));
                        }
                        /* ClaimWorkDataの値更新 */
                        work.setWorkTableId(o.getId());
                        work.setName(o.getName());
                        // work.setTableName(o.getDbname());
                        work.setDbName(o.getDbname());
                        work.setLasted(o.getLasted());
                    }
                }
            }
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean commit() {
        /* 子の誤記補正情報を変更する */
        /*
         * ClaimWorkDataDao.setCorrectionMistakes(this, this._id, false, false);
         * を置換する。
         */
        Session session = null;
        try {
            session = IoService.getInstance().getSession();

            String strFlds = CoronaIoUtils.intListToString(this.getCorrectionMistakesFields());
            ClaimsBean claims = (ClaimsBean) session.get(ClaimsBean.class, this._id);
            if (claims != null) {
                claims.setKeyFieldId(this.getDispIdField());
                claims.setProductFieldId(this.getProductField());
                claims.setTargetFields(strFlds);
                claims.setExternalFlag(false);
                claims.setInternalFlag(false);
                /* トランザクション開始 */
                session.beginTransaction();
                session.save(claims);
                session.flush();
                /* トランザクション確定 */
                session.getTransaction().commit();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
        }
        return true;
    }


    @Override
    public boolean addCorrectionMistakes(IClaimWorkData data) {
        /* クレーム情報を取得 */
        if (!updateCorrectionMistake((ClaimWorkData) data)) {
            return false;
        }

        Session session = null;
        try {
            session = IoService.getInstance().getSession();
            /* トランザクション開始 */
            session.beginTransaction();

            /* フィールド情報を取得 */
            String strFields = CoronaIoUtils.intListToString(this.getCorrectionMistakesFields());
            ClaimsBean claims = (ClaimsBean) session.get(ClaimsBean.class, this._id);
            if (claims != null) {
                claims.setKeyFieldId(this.getDispIdField());
                claims.setProductFieldId(this.getProductField());
                claims.setTargetFields(strFields);
                claims.setExternalFlag(((ClaimWorkData) data).isExternalCorrectionMistakes());
                claims.setInternalFlag(((ClaimWorkData) data).isInternalCorrectionMistakes());
                session.save(claims);
                session.flush();
            }
            /* トランザクション確定 */
            session.getTransaction().commit();

            /* WORKDATAS存在チェック */
            int workdataId = 0;
            String projectId = "projectId"; //$NON-NLS-1$
            String productId = "productId"; //$NON-NLS-1$
            String inputTableId = "inputTableId"; //$NON-NLS-1$
            String type = "type"; //$NON-NLS-1$

            StringBuilder strSQL = new StringBuilder(128).append("FROM WorkdatasBean WHERE projectId = :").append(projectId); //$NON-NLS-1$
            strSQL.append(" AND productId = :").append(productId).append(" AND inputTableId = :").append(inputTableId); //$NON-NLS-1$ //$NON-NLS-2$
            strSQL.append(" AND type= :").append(type); //$NON-NLS-1$

            @SuppressWarnings("unchecked")
            List<WorkdatasBean> workdatasList = session.createQuery(strSQL.toString()).setInteger(projectId, 0).setInteger(productId, 0)
                    .setInteger(inputTableId, getId()).setInteger(type, data.getClaimWorkDataType().getIntValue()).list();

            if (workdatasList != null && workdatasList.size() > 0) {
                /* WORKDATASにデータが存在する場合はIDを取得 */
                WorkdatasBean rs = workdatasList.get(0);
                workdataId = rs.getId();
            }

            /* トランザクション開始 */
            session.beginTransaction();

            /* WORKDATAS追加 */
            WorkdatasBean workdatas = null;
            if (workdataId <= 0) {
                /* WORKDATASへデータの追加 */
                workdatas = new WorkdatasBean();
                workdatas.setId(((ClaimWorkData) data).getWorkdataId());
                workdatas.setProjectId(0);
                workdatas.setProductId(0);
                workdatas.setInputTableId(getId());
                workdatas.setType(data.getClaimWorkDataType().getIntValue());
                workdatas.setHistoryId(0);
                workdatas.setLink(null);
                workdatas.setLasted(null);
                session.save(workdatas);
                session.flush();
            }
            /* トランザクション確定 */
            session.getTransaction().commit();

            if (workdatas != null) {
                /* WORKDATASが新規に追加されたので、追加されたIDをCLAIMSへ反映 */
                ((ClaimWorkData) data).setWorkdataId(workdatas.getId());
                _correctionMistakes.add(data);
                return true;
            } else if (workdataId > 0) {
                /* workdataIdが0より大きい場合も取得したIDをCLAIMSへ反映 */
                ((ClaimWorkData) data).setWorkdataId(workdataId);
                _correctionMistakes.add(data);
                return true;
            }
            return false;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
        }
    }


    /**
     * 誤記補正データを更新する
     * 
     * @note 誤記補正は特殊で、CLAIMSテーブルに問い合わせデータインポート時にフィールド自体は追加を行う。<br>
     *       ただし、ClaimWorkDataはフィールド単位で持つため、生成時にはフィールドが未確定のため、作成しない。<br>
     *       フィールドが確定するUIからの追加処理時に、足りない情報のみをDB側から更新を行う
     * 
     * @param work
     *            誤記補正データ
     * @return true
     */
    private boolean updateCorrectionMistake(ClaimWorkData work) {
        if (work == null) {
            return false;
        }

        boolean alive = false;

        Session session = IoService.getInstance().getSession();

        try {
            /* SQL生成 */
            /* テーブル一覧から問い合わせデータのみを取得 */
            /* String strHQL = ClaimDataDao.getInfomations(getId()); を置換する。 */
            String claimId = "claimId"; //$NON-NLS-1$
            StringBuilder strHQL = new StringBuilder(270);

            strHQL.append("FROM TablesBean i,TablesBean o, ClaimsBean c "); //$NON-NLS-1$
            strHQL.append("WHERE i.id=c.id AND o.id=c.workTableId AND i.id= :").append(claimId); //$NON-NLS-1$

            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createQuery(strHQL.toString()).setInteger(claimId, getId()).list();
            if (list != null && list.size() > 0) {
                for (Object[] rs : list) {
                    // TablesBean i = (TablesBean)rs[0];
                    TablesBean o = (TablesBean) rs[1];
                    ClaimsBean c = (ClaimsBean) rs[2];
                    /* ClaimDataの値更新 */
                    _miningFields = CoronaIoUtils.stringToIntSet(c.getTargetFields());
                    if (this._miningFields.size() != 0) {
                        for (Integer j : this._miningFields) {
                            if (j == work.getFieldId()) {
                                alive = true;
                            }
                        }
                    }
                    if (alive == false) {
                        this._miningFields.add(work.getFieldId());
                    }

                    /* ClaimWorkDataの値更新 */
                    work.setWorkTableId(o.getId());
                    work.setName(o.getName());
                    work.setDbName(o.getDbname());
                    work.setLasted(o.getLasted());

                }
            }
            return true;

        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean removeCorrectionMistakes(IClaimWorkData data) {
        if (deleteClaimWorkData(0, 0, getId(), data.getClaimWorkDataType())) {
            _correctionMistakes.remove(data);
            return true;
        }
        return false;
    }


    @Override
    public List<IRecord> getRecords() {
        return super.getRecords();
    }


    @Deprecated
    @Override
    public List<IRecord> getRecords(int page) {
        /* TODO 自動生成されたメソッド・スタブ */
        return null;
    }


    @Override
    public IRecord getRecord(int recordId) {
        return ((IClaimDataRecordList) _records).getRecord(recordId);
    }


    @Override
    public List<ITextRecord> getTextRecords(int fieldNo) {

        return super.getTextRecords(fieldNo);
    }


    @Deprecated
    @Override
    public List<ITextRecord> getTextRecords(int fieldNo, int page) {
        /* TODO 自動生成されたメソッド・スタブ */
        return null;
    }


    /**
     * 指定された処理結果を削除する。
     * 処理結果が辞書優先度を保持していれば、すべて削除する。
     * 
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     * @param claimId
     *            問い合わせデータID
     * @param type
     *            問い合わせ種別
     * @return 処理結果。失敗した場合false
     */
    private static boolean deleteClaimWorkData(int projectId, int productId, int claimId, ClaimWorkDataType type) {
        // ClaimWorkDataDaoから移設

        /* 辞書優先度テーブルからデータを消すため、ワークIDを取得 */
        StringBuilder selectSql = new StringBuilder(128);
        selectSql.append("SELECT ID FROM WORKDATAS WHERE PROJECT_ID = ").append(projectId).append(" AND "); //$NON-NLS-1$ //$NON-NLS-2$
        selectSql.append("INPUT_TABLE_ID = ").append(claimId); //$NON-NLS-1$

        if (productId > 0) {
            selectSql.append(" AND PRODUCT_ID = ").append(productId); //$NON-NLS-1$
        }

        if (ClaimWorkDataType.NONE.equals(type)) {
            selectSql.append(" AND TYPE != ").append(ClaimWorkDataType.CORRECTION_MISTAKES.getIntValue()); //$NON-NLS-1$
        } else {
            selectSql.append(" AND TYPE = ").append(type.getIntValue()); //$NON-NLS-1$
        }

        Session session = null;
        try {

            // TODO 20131113 jdbc版ではCommonDao.executeWithReturnを使用
            List<Integer> ids = null;

            session = IoService.getInstance().getSession();
            @SuppressWarnings("unchecked")
            List<Integer> list = session.createSQLQuery(selectSql.toString()).list();
            if (list != null && list.size() > 0) {
                ids = list;
            } else {
                ids = new ArrayList<Integer>();
            }

            /* 辞書優先度テーブルから削除 */
            StringBuilder deleteSql = new StringBuilder(128);
            deleteSql.append("DELETE FROM DIC_PRI WHERE ID = "); //$NON-NLS-1$
            /* トランザクション開始 */
            session.beginTransaction();
            for (Integer id : ids) {
                try {
                    session.createSQLQuery(deleteSql + String.valueOf(id)).executeUpdate();
                } catch (HibernateException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            session.flush();
            /* トランザクション確定 */
            session.getTransaction().commit();
            /* ワークデータテーブルから削除 */
            return deleteWorkFromManageTable(projectId, productId, claimId, type);
        } catch (HibernateException e) {
            CoronaActivator.debugLog(selectSql.toString());
            e.printStackTrace();
            return false;
        } finally {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
        }
    }


    /**
     * 指定した処理結果を管理テーブルから消す。
     * 
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     * @param claimId
     *            問い合わせデータID
     * @param type
     *            中間データ種別
     * @return 処理結果。削除に失敗した場合はfalse
     */
    private static boolean deleteWorkFromManageTable(int projectId, int productId, int claimId, ClaimWorkDataType type) {
        // ClaimWorkDataDaoから移設

        StringBuilder deleteSql = new StringBuilder(128);
        deleteSql.append("DELETE FROM WORKDATAS WHERE PROJECT_ID=").append(projectId); //$NON-NLS-1$
        deleteSql.append(" AND INPUT_TABLE_ID=").append(claimId); //$NON-NLS-1$
        if (productId > 0) {
            deleteSql.append(" AND PRODUCT_ID=").append(productId); //$NON-NLS-1$
        }
        if (ClaimWorkDataType.NONE.equals(type)) {
            deleteSql.append(" And type !=").append(ClaimWorkDataType.CORRECTION_MISTAKES.getIntValue()); //$NON-NLS-1$
        } else {
            deleteSql.append(" AND TYPE=").append(type.getIntValue()); //$NON-NLS-1$
        }

        Session session = null;
        try {
            session = IoService.getInstance().getSession();
            /* トランザクション開始 */
            session.beginTransaction();
            session.createSQLQuery(deleteSql.toString()).executeUpdate();
            session.flush();
            /* トランザクション確定 */
            session.getTransaction().commit();
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
        }

    }
}