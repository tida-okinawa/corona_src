/**
 * @version $Id: IoService.java 1135 2013-09-10 00:33:04Z hajime-uchihara $
 *　
 * 2013/10/28 10:56:24
 * @author hajime-uchihara
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.PreferenceInitializer;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.bean.CategoryBean;
import com.tida_okinawa.corona.io.bean.ClaimsBean;
import com.tida_okinawa.corona.io.bean.CoronaDbVersionBean;
import com.tida_okinawa.corona.io.bean.DicCommonBean;
import com.tida_okinawa.corona.io.bean.DicPriorityBean;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.bean.FieldsBean;
import com.tida_okinawa.corona.io.bean.ProductBean;
import com.tida_okinawa.corona.io.bean.ProjectBean;
import com.tida_okinawa.corona.io.bean.RelClmProductBean;
import com.tida_okinawa.corona.io.bean.RelPrjClmBean;
import com.tida_okinawa.corona.io.bean.RelPrjProductBean;
import com.tida_okinawa.corona.io.bean.TablesBean;
import com.tida_okinawa.corona.io.bean.TypePatternBean;
import com.tida_okinawa.corona.io.bean.WorkdatasBean;
import com.tida_okinawa.corona.io.dam.hibernate.hql.CommonCreateQuery;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicFactory;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.dic.TermCForm;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.model.table.TableType;
import com.tida_okinawa.corona.io.model.table.impl.FieldHeader;
import com.tida_okinawa.corona.io.service.IIoService;
import com.tida_okinawa.corona.io.service.abstraction.AbstractIoService;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;
import com.tida_okinawa.corona.license.LicenseActivator;

/**
 * @author wataru-higa
 */
public final class IoService extends AbstractIoService {

    /**
     * インスタンス
     */
    private static IIoService _instance = new IoService();


    /**
     * このクラスはシングルトン
     */
    private IoService() {

    }


    /**
     * インスタンス取得
     * 
     * @return _instance
     */
    public static IIoService getInstance() {
        return _instance;
    }

    /* ****************************************
     * DB接続系
     */
    /**
     * SQLExceptionがDB接続に失敗したことを表すとき、この値をSQLStateとして保持する
     */
    public static final String ER_CODE_CONNECTION_FAILED = "Corona:0001"; //$NON-NLS-1$

    /**
     * データベースコネクション
     */
    private volatile Session session = null;


    /**
     * コネクション接続
     * 
     * @param connect
     * @param user
     * @param passwd
     * @return true/false
     */
    @Override
    protected synchronized Boolean connectDam(String connect, String user, String passwd) {
        // TODO getPatternTypes()を呼んでいない
        try {
            Configuration config = new org.hibernate.cfg.Configuration().configure().setProperty("hibernate.connection.url", connect) //$NON-NLS-1$
                    .setProperty("hibernate.connection.username", user).setProperty("hibernate.connection.password", passwd); //$NON-NLS-1$ //$NON-NLS-2$
            ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
            SessionFactory sessionFactory = null; // config.buildSessionFactory(serviceRegistry);
            try {
                sessionFactory = config.buildSessionFactory(serviceRegistry);
            } catch (HibernateException ee) {
                throw ee;
            } catch (Exception ex) {
                ex.printStackTrace();
                /* 接続不能の場合、 NullPointerExceptionが発生するので、それを判定基準にする。 */
                if (ex instanceof NullPointerException) {
                    return false;
                }
                throw ex;
            }

            this.session = sessionFactory.openSession();
            this.session.setFlushMode(FlushMode.MANUAL);

            /* トランザクション開始 */
            this.session.beginTransaction();
            if (!isConnect()) {
                // DBバージョンにレコードが存在しない場合は、新規にレコードを作成
                LicenseActivator license = LicenseActivator.getDefault();
                String dbVersion = license.getDbVersion().trim();
                CoronaDbVersionBean bean = new CoronaDbVersionBean();
                bean.setVersion(dbVersion);
                this.session.save(bean);
                this.session.flush();
            }

            this.getPatternTypes();
            this.strConnect = connect;
            this.strUser = user;
            this.strPasswd = passwd;
            if (CoronaActivator.getDefault() != null) {
                CoronaActivator.getDefault().getLogger().getOutStream().println(Messages.IoService_connectDatabaseSuccess + strConnect);
            }
            /* トランザクション確定 */
            this.session.getTransaction().commit();
            return true;
        } catch (HibernateException e) {
            System.err.println(Messages.IoService_errorConnectDatabase);
            e.printStackTrace();
            if (this.session != null) {
                if (this.session.isConnected()) {
                    this.session.disconnect();
                    this.session.close();
                }
            }
            return false;
        } finally {
            if (session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
    }


    /**
     * コネクション切断
     * 
     * @return true/false
     */
    @Override
    protected synchronized Boolean disConnectDam() {
        try {
            this.session.disconnect();
            this.session.close();
            if (this.session.isOpen()) {
                return false;
            } else {
                CoronaActivator.getDefault().getLogger().getOutStream().println(Messages.IoService_connectDatabaseFail + strConnect);
                return true;
            }
        } catch (HibernateException e) {
            System.err.println(Messages.IoService_errorDisconnectionDatabase);
            e.printStackTrace();
            return false;
        }
    }


    /**
     * コネクション状態チェック
     * 
     * @return true/false
     */
    @Override
    protected Boolean isConnectDam() {
        try {
            if (this.session == null) {
                return false;
            }
            Object result = CommonCreateQuery.getCoronaDbVersionQuery(this.session).uniqueResult();
            if (result != null) {
                return true;
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    /**
     * TODO:設定画面でのテスト接続確認のメソッド。できれば画面から消して同メソッドも削除したい。
     */
    @Override
    public String[] connectTest(String connect, String user, String passwd) {
        boolean connected = false;
        Session session = null;
        try {
            Configuration config = new Configuration().configure()
                    .setProperty("hibernate.connection.url", connect).setProperty("hibernate.connection.username", user) //$NON-NLS-1$ //$NON-NLS-2$
                    .setProperty("hibernate.connection.password", passwd); //$NON-NLS-1$
            ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
            SessionFactory sessionFactory = config.buildSessionFactory(serviceRegistry);
            session = sessionFactory.openSession();
            Object isConnected = CommonCreateQuery.getCoronaDbVersionQuery(session).uniqueResult();
            if (isConnected != null) {
                connected = true;
            }
            session.close();
            session = null;
        } catch (HibernateException e) {
            return new String[] { e.getMessage() };
        } finally {
            if (session != null) {
                if (session.isOpen()) {
                    session.close();
                    session = null;
                }
            }
        }
        if (connected) {
            return new String[0];
        } else {
            return new String[] { "接続できていません。" }; //$NON-NLS-1$
        }
    }


    /**
     * コネクション取得
     * 
     * @return SQLConnection。接続が確立していなければ、再接続を試みる。
     */
    @Override
    public Session getSession() {
        if (!isConnectDam()) {
            CoronaActivator activator = CoronaActivator.getDefault();
            if (activator != null) {
                final int retryCount = activator.getPreferenceStore().getInt(PreferenceInitializer.PREF_DB_RETRY_CNT);

                PrintStream out = activator.getLogger().getOutStream();
                out.println(Messages.IoService_reconnectDatabase);

                for (int i = 1; i <= retryCount; i++) {
                    out.print(Messages.bind(Messages.IoService_reconnect, i));
                    reConnect();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        CoronaActivator.debugLog(Messages.IoService_interrupt);
                    }

                    if (this.session.isConnected() && isConnectDam()) {
                        out.println(Messages.IoService_connectionSuccess);
                        return this.session;
                    }
                    out.println(Messages.IoService_connectionFail);
                }

                /* エラー出力 */
                HibernateException connectError = new HibernateException(Messages.IoService_errorReconnectDatabaseFail + " " + ER_CODE_CONNECTION_FAILED); //$NON-NLS-1$
                IStatus status = new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, Messages.IoService_errorReconnectDatabaseFail, connectError);
                activator.getLog().log(status);
                try {
                    throw connectError;
                } catch (HibernateException e1) {
                    e1.printStackTrace();
                }
            }
            throw new RuntimeException(Messages.IoService_errorEndCoronaActivator);
        }

        return this.session;
    }


    /* ****************************************
     * プロジェクト系
     */
    /**
     * プロジェクト情報取得
     * 
     * @return List<ICoronaProject>
     * 
     */
    @Override
    public Map<Integer, ICoronaProject> getProjectsMap() {
        if (_projectList == null) {
            updateProjects();
        }
        return _projectList;
    }


    /**
     * プロジェクト情報追加
     * 
     * @param project
     * @return true/false
     */
    @Override
    protected Boolean addProjectDam(ICoronaProject project) {
        ProjectBean result = null;

        try {
            /* プロジェクトの存在チェック */
            @SuppressWarnings("unchecked")
            List<ProjectBean> list = CommonCreateQuery.getProjectQuery(project.getName()).list();
            if (list != null && list.size() > 0) {
                result = list.get(0);
            }
            if (result != null) {
                project.setId(result.getProjectId());
                project.update();
                return true;
            }

            try {
                // プロジェクト作成
                ProjectBean projectBean = new ProjectBean();
                projectBean.setProjectName(project.getName());

                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.save(projectBean);
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();

                // INSERT後のプロジェクトIDをメモリへ反映
                project.setId(projectBean.getProjectId());

                /* 辞書の更新 */
                updateDictionarys();

                // JUMAN辞書をデフォルトの辞書としてプロジェクトに追加
                List<ICoronaDic> dics = getDictionarys(IUserDic.class);
                for (ICoronaDic dic : dics) {
                    if (((IUserDic) dic).getDicType().getIntValue() == DicType.JUMAN.getIntValue()) {
                        project.addDictionary(dic);
                    }
                }

                return true;
            } catch (HibernateException ee) {
                ee.printStackTrace();
                throw new HibernateException(Messages.IoService_errorSetProjectInfo);
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
    }


    /**
     * プロジェクトを削除する
     */
    @Override
    public Boolean removeProject(ICoronaProject project) {


        try {
            boolean result = true;
            /* 中間データの削除 */
            for (ICoronaProduct product : project.getProducts()) {
                /* ConcurrentModificationException対策 */
                List<IClaimWorkData> works = new ArrayList<IClaimWorkData>(product.getClaimWorkDatas());
                for (IClaimWorkData workData : works) {
                    if (workData.getClaimWorkDataType() != ClaimWorkDataType.CORRECTION_MISTAKES) {
                        product.removeClaimWorkData(workData);
                    }
                }
            }

            /* ターゲット関連の削除 */
            if (project.getProducts().size() > 0) {
                for (ICoronaProduct product : project.getProducts()) {
                    try {
                        RelPrjProductBean relPrjPrd = (RelPrjProductBean) CommonCreateQuery.getRelPrjProductQuery(project.getId(), product.getId())
                                .uniqueResult();
                        if (relPrjPrd != null) {
                            /* トランザクション開始 */
                            this.session.beginTransaction();
                            this.session.delete(relPrjPrd);
                            this.session.flush();
                            /* トランザクション確定 */
                            this.session.getTransaction().commit();
                        }
                        relPrjPrd = (RelPrjProductBean) CommonCreateQuery.getRelPrjProductQuery(project.getId(), product.getId()).uniqueResult();
                        /* プロジェクト-ターゲットリレーションの削除 */
                        if (relPrjPrd != null) {
                            result = false;
                        }
                    } catch (HibernateException ee) {
                        ee.printStackTrace();
                        result = false;
                    } finally {
                        if (this.session.getTransaction().isActive()) {
                            this.session.getTransaction().rollback();
                        }
                    }
                }
            }

            /* クレーム-プロジェクト(ターゲット)リレーションの削除 */
            /* rel_clm_product、rel_prj_clmテーブルのリレーション削除 */
            for (IClaimData claim : project.getClaimDatas()) {

                // rel_clm_productテーブルからの削除
                int claimId = claim.getId();
                int id = project.getId();
                try {
                    StringBuilder sql = new StringBuilder(128);
                    sql.append("DELETE FROM REL_CLM_PRODUCT WHERE TBL_ID = ").append(claimId).append(" AND PRJ_ID = ").append(id); //$NON-NLS-1$ //$NON-NLS-2$
                    /* トランザクション開始 */
                    this.session.beginTransaction();
                    /* rel_clm_productテーブルの削除 */
                    this.session.createSQLQuery(sql.toString()).executeUpdate();
                    this.session.flush();
                    /* rel_prj_clmテーブルからの削除 */
                    RelPrjClmBean delRelPrjClm = (RelPrjClmBean) CommonCreateQuery.getRelPrjClmQuery(claim.getId(), project.getId()).uniqueResult();
                    if (delRelPrjClm != null) {
                        this.session.delete(delRelPrjClm);
                        this.session.flush();
                    }
                    /* トランザクション確定 */
                    this.session.getTransaction().commit();

                    delRelPrjClm = (RelPrjClmBean) CommonCreateQuery.getRelPrjClmQuery(claim.getId(), project.getId()).uniqueResult();
                    if (delRelPrjClm != null) {
                        result = false;
                    }

                } catch (HibernateException ee) {
                    ee.printStackTrace();
                    result = false;
                } finally {
                    if (this.session.getTransaction().isActive()) {
                        this.session.getTransaction().rollback();
                    }
                }
            }

            /* プロジェクト-辞書リレーションの削除 */
            /* rel_prj_dicテーブルのリレーション削除 */
            try {
                int projectId = project.getId();
                StringBuilder sql = new StringBuilder(64);
                sql.append("DELETE FROM REL_PRJ_DIC WHERE PROJECT_ID = ").append(projectId); //$NON-NLS-1$
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.createSQLQuery(sql.toString()).executeUpdate();
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
            } catch (HibernateException ex) {
                ex.printStackTrace();
                result = false;
            } finally {
                if (this.session.getTransaction().isActive()) {
                    this.session.getTransaction().rollback();
                }
            }

            /* プロジェクトの削除 */
            /* projectテーブルからの削除 */
            if (result) {
                try {
                    ProjectBean delProject = (ProjectBean) CommonCreateQuery.getProjectQuery(project.getName()).uniqueResult();
                    /* トランザクション開始 */
                    this.session.beginTransaction();
                    if (delProject != null) {
                        this.session.delete(delProject);
                        this.session.flush();
                    }
                    /* トランザクション確定 */
                    this.session.getTransaction().commit();
                    delProject = (ProjectBean) CommonCreateQuery.getProjectQuery(project.getName()).uniqueResult();
                    if (delProject != null) {
                        result = false;
                    }
                } catch (HibernateException ee) {
                    ee.printStackTrace();
                    result = false;
                } finally {
                    if (this.session.getTransaction().isActive()) {
                        this.session.getTransaction().rollback();
                    }
                }
                // メモリ上から削除
                super.removeProject(project);
            }
            return result;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 
     * プロジェクト情報更新
     * 
     * @return　IStatus
     */
    private IStatus updateProjects() {
        if (_projectList == null) {
            _projectList = new HashMap<Integer, ICoronaProject>();
        }
        try {
            @SuppressWarnings("unchecked")
            List<ProjectBean> projects = CommonCreateQuery.getProjectQuery().list();
            for (ProjectBean project : projects) {
                ICoronaProject coronaProject = _projectList.get(project.getProjectId());
                if (coronaProject == null) {
                    coronaProject = new Project(project.getProjectName(), project.getProjectId());
                    _projectList.put(project.getProjectId(), coronaProject);
                } else {
                    coronaProject.setName(project.getProjectName());
                }
            }
            return Status.OK_STATUS;
        } catch (HibernateException e) {
            e.printStackTrace();
            return new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, Messages.IoService_errorGetProjectInfo + e.getLocalizedMessage(), e);
        }
    }


    /* ****************************************
     * 問い合わせデータ系
     */
    /**
     * claimList
     */
    private Map<Integer, IClaimData> _claimList;


    @Override
    public Map<Integer, IClaimData> getClaimDatasMap() {
        if (_claimList == null) {
            updateClaimDatas();
        }
        return _claimList;
    }


    @Override
    public IClaimData getClaimData(int claimId) {
        if (_claimList == null) {
            updateClaimDatas();
        }
        return _claimList.get(claimId);
    }


    /**
     * 問い合わせデータ更新
     * 
     * @return IStatus
     */
    private IStatus updateClaimDatas() {
        if (_claimList == null) {
            _claimList = new HashMap<Integer, IClaimData>();
        }

        try {
            @SuppressWarnings("unchecked")
            Iterator<TablesBean> tableList = CommonCreateQuery.getTablesQuery(TableType.CLAIM_DATA.getIntValue()).iterate();
            while (tableList.hasNext()) {
                TablesBean table = tableList.next();
                if (!_claimList.containsKey(table.getId())) {
                    _claimList.put(table.getId(), new ClaimData(table.getName(), table.getDbname(), table.getId()));
                }
            }

            /* TODO いずれなくす。各オブジェクトが、必要な時にupdateすればいい。 */
            for (Entry<Integer, IClaimData> entry : _claimList.entrySet()) {
                entry.getValue().update();
            }
            return Status.OK_STATUS;
        } catch (HibernateException e) {
            return new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, Messages.IoService_getDataFileFail + e.getLocalizedMessage(), e);
        }
    }


    @Override
    public Boolean removeClaimData(int claimId) {
        boolean result = true;
        ClaimData claim = (ClaimData) this.getClaimData(claimId);
        if (claim == null)
            return false;

        String clmTblName = claim.getTableName();
        String cmTblName = CoronaIoUtils.createWorkTableName(clmTblName, TableType.CORRECTION_MISTAKES_DATA, 0);

        /* 問い合わせデータテーブルからの削除 */
        // claimsテーブルからの削除
        try {
            ClaimsBean delClaim = (ClaimsBean) this.session.get(ClaimsBean.class, claimId);
            if (delClaim != null) {
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.delete(delClaim);
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
                delClaim = (ClaimsBean) this.session.get(ClaimsBean.class, claimId);
                if (delClaim != null) {
                    result = false;
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        /* フィールド情報からの削除 */
        // fieldsテーブルからの削除
        try {
            FieldsBean delField = (FieldsBean) this.session.get(FieldsBean.class, claimId);
            if (delField != null) {
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.delete(delField);
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
                delField = (FieldsBean) this.session.get(FieldsBean.class, claimId);
                if (delField != null) {
                    result = false;
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        /* 中間データ管理テーブルからの削除（誤記補正） */
        // dic_pri、workdatasテーブルから削除
        String strQuery = null;
        try {
            Query query = CommonCreateQuery.getWorkdatasQuery(0, 0, claimId, ClaimWorkDataType.CORRECTION_MISTAKES);
            strQuery = query.toString();
            @SuppressWarnings("unchecked")
            List<WorkdatasBean> delWorkDatas = query.list();
            /* トランザクション開始 */
            this.session.beginTransaction();
            for (WorkdatasBean workdata : delWorkDatas) {
                /* 辞書優先度テーブルから削除 */
                @SuppressWarnings("unchecked")
                List<DicPriorityBean> delDicPriList = CommonCreateQuery.getDicPriQuery(workdata.getId()).list();
                for (DicPriorityBean dicPri : delDicPriList) {
                    // dic_priテーブルからの削除
                    this.session.delete(dicPri);
                    this.session.flush();
                }
                // workdatasテーブルからの削除
                this.session.delete(workdata);
                this.session.flush();
            }
            /* トランザクション確定 */
            this.session.getTransaction().commit();

            /* 削除確認チェック */
            @SuppressWarnings("unchecked")
            List<WorkdatasBean> checkDeleteWorkDatas = query.list();
            if (checkDeleteWorkDatas != null && checkDeleteWorkDatas.size() > 0) {
                /* 削除失敗 */
                result = false;
            }

        } catch (HibernateException e) {
            CoronaActivator.debugLog("CommonCreateQuery.getWorkdatasQuery[ " + strQuery + " ]"); //$NON-NLS-1$ //$NON-NLS-2$
            e.printStackTrace();
            result = false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        /* テーブルリストからの削除（誤記補正） */
        // tablesテーブルからの削除
        TablesBean delTable = null;
        boolean delExecFlag = false;
        try {
            @SuppressWarnings("unchecked")
            List<TablesBean> list = CommonCreateQuery.getTablesQuery(cmTblName).list();
            if (list != null && list.size() > 0) {
                delTable = list.get(0);
            }
            if (delTable != null) {
                delExecFlag = true;
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.delete(delTable);
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();

                @SuppressWarnings("unchecked")
                List<TablesBean> checkList = CommonCreateQuery.getTablesQuery(cmTblName).list();
                if (checkList != null && checkList.size() > 0) {
                    result = false;
                }
            }
            // TODO　Sessionから削除しないといけない。
            /* 誤記補正用中間データテーブルの削除 */
            // usr_claim_xxxテーブルの削除
            if (delExecFlag) {
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.createSQLQuery("DROP TABLE " + cmTblName).executeUpdate(); //$NON-NLS-1$
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        // // TODO　Sessionから削除しないといけない。
        // /* 誤記補正用中間データテーブルの削除 */
        // // usr_claim_xxxテーブルの削除
        // try {
        // if (delExecFlag) {
        // /* トランザクション開始 */
        // this.session.beginTransaction();
        //                this.session.createSQLQuery("DROP TABLE " + cmTblName).executeUpdate(); //$NON-NLS-1$
        // this.session.flush();
        // /* トランザクション確定 */
        // this.session.getTransaction().commit();
        // }
        // } catch (HibernateException e) {
        // e.printStackTrace();
        // result = false;
        // } finally {
        // if (this.session.getTransaction().isActive()) {
        // this.session.getTransaction().rollback();
        // }
        // }

        /* Productとのリレーションを削除 */
        // rel_clm_productテーブルからの削除
        try {
            @SuppressWarnings("unchecked")
            List<RelClmProductBean> delRelClmProductList = CommonCreateQuery.getRelClmProductQuery(claimId).list();
            /* トランザクション開始 */
            this.session.beginTransaction();
            for (RelClmProductBean delRelClmProduct : delRelClmProductList) {
                this.session.delete(delRelClmProduct);
            }
            this.session.flush();
            /* トランザクション確定 */
            this.session.getTransaction().commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        /* Projectとのリレーションを削除（原文） */
        // rel_prj_clmテーブルからの削除
        try {
            @SuppressWarnings("unchecked")
            List<RelPrjClmBean> delRelPrjClmList = CommonCreateQuery.getRelPrjClmQuery(claimId).list();
            /* トランザクション開始 */
            this.session.beginTransaction();
            for (RelPrjClmBean delRelPrjClm : delRelPrjClmList) {
                if (delRelPrjClm != null) {
                    this.session.delete(delRelPrjClm);
                }
            }
            this.session.flush();
            /* トランザクション確定 */
            this.session.getTransaction().commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        /* テーブルリストからの削除（原文） */
        try {
            TablesBean delTableOrg = (TablesBean) this.session.get(TablesBean.class, claimId);
            if (delTableOrg != null) {
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.delete(delTableOrg);
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        /* クレーム原文テーブルの削除 */
        try {
            /* トランザクション開始 */
            this.session.beginTransaction();
            this.session.createSQLQuery("DROP TABLE " + clmTblName).executeUpdate(); //$NON-NLS-1$
            this.session.flush();
            /* トランザクション確定 */
            this.session.getTransaction().commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        this._claimList.remove(claimId);
        this.updateClaimDatas();

        return result;
    }


    /**
     * 問い合わせデータインポート
     * 
     * @param path
     * @param definePath
     * @param tableName
     * @return IClaimData
     * 
     */
    @Override
    protected IClaimData importClaimDataDam(String path, String definePath, String tableName, boolean headFlg) throws SQLException, IOException {
        /* 動的テーブルの実装を含むため保留 */
        String strSQL;
        String fileName;
        String dbName = tableName;

        /* DB_NAME構築 */
        fileName = path;
        int idx = path.lastIndexOf(File.separatorChar);
        if (idx > 0) {
            fileName = path.substring(idx + 1);
        }
        dbName = CoronaIoUtils.createWorkTableName(dbName, TableType.CLAIM_DATA, -1);

        strSQL = null;
        try {

            /* トランザクション開始 */
            this.session.beginTransaction();
            /* 問い合わせデータテーブル構築 */
            Query query = CommonCreateQuery.createUsrClaimTableSQLQuery(definePath, dbName);
            strSQL = query.toString();
            query.executeUpdate();
            this.session.flush();

            /* カラム情報取得 */
            String[] columns = null;
            strSQL = "SHOW COLUMNS FROM " + dbName; //$NON-NLS-1$
            List<String> tableColumns = new ArrayList<String>();
            @SuppressWarnings("unchecked")
            List<Object[]> columnsList = session.createSQLQuery(strSQL).list();
            if (columnsList != null) {
                for (Object[] rs : columnsList) {
                    tableColumns.add((String) rs[0]);
                }
                columns = tableColumns.toArray(new String[tableColumns.size()]);
            }

            /* IDを除外し、カラム名を編集 */
            StringBuilder strColumns = new StringBuilder(100);
            for (int i = 1; i < columns.length; i++) {
                strColumns.append(",").append(columns[i]); //$NON-NLS-1$
            }
            /* 問い合わせデータインポート */
            strSQL = "loadDataInUsrClaimTableSQLQuery()"; //$NON-NLS-1$


            query = CommonCreateQuery.loadDataInUsrClaimTableSQLQuery(path, dbName, strColumns.substring(1), headFlg);
            if (query == null) {
                return null;
            }

            /* エラー時のログ出力用にSQLを保持 */
            strSQL = query.toString();

            /* インポート処理実行 */
            query.executeUpdate();
            this.session.flush();

            /* トランザクション確定 */
            this.session.getTransaction().commit();

            String strExeSql = null;
            // 「INSERT IGNORE INTO TABLES....」の機能置換
            StringBuilder strCheckHQL = new StringBuilder(150).append("from TablesBean where name = :NAME and dbname = :DBNAME and type = :TYPE"); //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<TablesBean> tableBeanlist = session.createQuery(strCheckHQL.toString()).setString("NAME", fileName) //$NON-NLS-1$
                    .setString("DBNAME", dbName) //$NON-NLS-1$
                    .setInteger("TYPE", TableType.CLAIM_DATA.getIntValue()) //$NON-NLS-1$
                    .list();

            if (tableBeanlist != null && tableBeanlist.size() == 0) {
                StringBuilder strSql = new StringBuilder(128).append("INSERT INTO TABLES (NAME, DBNAME, TYPE, LASTED) VALUES('"); //$NON-NLS-1$
                strSql.append(fileName).append("','").append(dbName).append("',").append(TableType.CLAIM_DATA.getIntValue()).append(",now())"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                strExeSql = strSql.toString();
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.createSQLQuery(strExeSql).executeUpdate();
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
            }

            int claimId = 0;
            int keyId = 1;
            /* テーブル存在チェック */
            strExeSql = new StringBuilder(64).append("SELECT ID FROM TABLES WHERE DBNAME ='").append(dbName).append("'").toString(); //$NON-NLS-1$ //$NON-NLS-2$
            @SuppressWarnings("unchecked")
            List<Object> idList = this.session.createSQLQuery(strExeSql).list();
            if (idList != null && idList.size() > 0) {
                Object rs = idList.get(0);
                claimId = Integer.parseInt(rs.toString());
            }

            /* ワークID取得 */
            int workId = 0;
            workId = createClaimWorkDataTable(TableType.CORRECTION_MISTAKES_DATA.toString(), dbName, TableType.CORRECTION_MISTAKES_DATA, 0, 0, 0, null);

            /* 問い合わせデータ情報追加 */
            /*
             * ClaimWorkDataDao.insertCorrectionMistakes(claimId, keyId,
             * workId);
             * を置換する。
             */
            ClaimsBean claims = (ClaimsBean) this.session.get(ClaimsBean.class, claimId);
            if (claims == null) {
                // データなし
                claims = new ClaimsBean();
                claims.setId(claimId);
            }
            claims.setKeyFieldId(keyId);
            claims.setProductFieldId(0);
            claims.setTargetFields("0"); //$NON-NLS-1$
            claims.setExternalFlag(false);
            claims.setInternalFlag(false);
            claims.setWorkTableId(workId);
            /* トランザクション開始 */
            this.session.beginTransaction();
            this.session.save(claims);
            this.session.flush();
            /* トランザクション開始 */
            this.session.getTransaction().commit();

            /* 問い合わせデータ更新 */
            updateClaimDatas();
            /* 対象データ取得 */
            return getClaimData(claimId);

        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strSQL); //$NON-NLS-1$
            e.printStackTrace();
            return null;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
    }


    /**
     * 問い合わせデータインポート（ドキュメント用）
     * 
     * @param path
     *            ファイルパス
     * @param tableName
     *            テーブル名
     * @param target
     *            ターゲット名
     * @param records
     *            レコード
     * @return IClaimData
     */
    @Override
    protected IClaimData importClaimDataDamForDocument(String path, String tableName, String target, List<String> records) throws SQLException, IOException,
            HibernateException {
        String fileName;
        String dbName = tableName;
        String strExeSql = null;

        /* DB_NAME構築 */
        fileName = path;
        int idx = path.lastIndexOf(File.separatorChar);
        if (idx > 0) {
            fileName = path.substring(idx + 1);
        }
        dbName = CoronaIoUtils.createWorkTableName(dbName, TableType.CLAIM_DATA, -1);

        /* 問い合わせデータテーブル構築 */
        if (!createClaimDataTableForDocument(dbName, target, records)) {
            return null;
        }

        /* テーブルリストへ登録 */
        /* TablesDao.insertTable(fileName, dbName, TableType.CLAIM_DATA) を置換する。 */
        try {
            // 「INSERT IGNORE INTO WORKDATAS....」の機能置換
            StringBuilder strCheckHQL = new StringBuilder(150).append("from TablesBean where name = :NAME and dbname = :DBNAME and type = :TYPE"); //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<TablesBean> list = this.session.createQuery(strCheckHQL.toString()).setString("NAME", fileName) //$NON-NLS-1$
                    .setString("DBNAME", dbName) //$NON-NLS-1$
                    .setInteger("TYPE", TableType.CLAIM_DATA.getIntValue()) //$NON-NLS-1$
                    .list();

            if (list != null && list.size() == 0) {
                // StringBuilder strSql = new StringBuilder(128).append("INSERT IGNORE INTO TABLES (NAME, DBNAME, TYPE, LASTED) VALUES('"); //$NON-NLS-1$
                StringBuilder strSql = new StringBuilder(128).append("INSERT INTO TABLES (NAME, DBNAME, TYPE, LASTED) VALUES('"); //$NON-NLS-1$
                strSql.append(fileName).append("','").append(dbName).append("',").append(TableType.CLAIM_DATA.getIntValue()).append(",now())"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                strExeSql = strSql.toString();
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.createSQLQuery(strExeSql).executeUpdate();
                this.session.flush();
                /* トランザクション終了 */
                this.session.getTransaction().commit();
            }
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
            throw e;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        int claimId = 0;
        int keyId = 1;
        /* TablesDao.getTableId(dbName); を置換する */
        try {
            strExeSql = new StringBuilder(64).append("SELECT ID FROM TABLES WHERE DBNAME ='").append(dbName).append("'").toString(); //$NON-NLS-1$ //$NON-NLS-2$
            @SuppressWarnings("unchecked")
            List<Object> list = this.session.createSQLQuery(strExeSql).list();
            if (list != null && list.size() > 0) {
                Object rs = list.get(0);
                claimId = Integer.parseInt(rs.toString());
            }
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
            throw e;
        }

        /* 誤記補正用テーブル構築 */
        int workId = 0;
        try {
            workId = createClaimWorkDataTable(TableType.CORRECTION_MISTAKES_DATA.toString(), dbName, TableType.CORRECTION_MISTAKES_DATA, 0, 0, 0, null);
        } catch (HibernateException e) {
            e.printStackTrace();
            throw e;
        }
        /* 問い合わせデータ情報追加 */
        /*
         * ClaimWorkDataDao.insertCorrectionMistakes(claimId, keyId, workId);
         * を置換する。
         */
        try {
            StringBuilder sql = new StringBuilder(512);
            sql.append("MERGE INTO CLAIMS (ID, KEY_FLD_ID, PRODUCT_FLD_ID, TGT_FLDS, EXTERNAL_FLG, INTERNAL_FLG, WORK_TBL_ID) "); //$NON-NLS-1$
            sql.append("KEY (ID) "); //$NON-NLS-1$
            sql.append("VALUES (").append(claimId).append(",").append(keyId).append(",0,0,false,false,").append(workId).append(") "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            // TODO 20131206 MERGEの使用
            //            sql.append("INSERT INTO CLAIMS (ID, KEY_FLD_ID, PRODUCT_FLD_ID, TGT_FLDS, EXTERNAL_FLG, INTERNAL_FLG, WORK_TBL_ID) "); //$NON-NLS-1$
            //            sql.append("VALUES (").append(claimId).append(",").append(keyId).append(",0,0,false,false,").append(workId).append(") "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            //            sql.append("ON DUPLICATE KEY UPDATE KEY_FLD_ID =VALUES(KEY_FLD_ID)"); //$NON-NLS-1$
            //            sql.append(",PRODUCT_FLD_ID =VALUES(PRODUCT_FLD_ID)"); //$NON-NLS-1$
            //            sql.append(",TGT_FLDS =VALUES(TGT_FLDS),EXTERNAL_FLG =VALUES(EXTERNAL_FLG)"); //$NON-NLS-1$
            //            sql.append(",INTERNAL_FLG =VALUES(INTERNAL_FLG),WORK_TBL_ID =VALUES(WORK_TBL_ID)"); //$NON-NLS-1$
            strExeSql = sql.toString();
            /* トランザクション開始 */
            this.session.beginTransaction();
            this.session.createSQLQuery(strExeSql).executeUpdate();
            this.session.flush();
            /* トランザクション確定 */
            this.session.getTransaction().commit();
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
            e.printStackTrace();
            throw e;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        /* 問い合わせデータ更新 */
        updateClaimDatas();
        /* 対象データ取得 */
        return getClaimData(claimId);
    }


    /* ****************************************
     * 辞書系
     */
    @Override
    public Map<Integer, ICoronaDic> getDictionarysMap() {
        if (_dics == null) {
            /* 辞書情報更新 */
            updateDictionarys();
        }
        return _dics;
    }


    @Override
    public ICoronaDic getDictionary(int id) {
        if (_dics == null) {
            updateDictionarys();
        }
        /* 辞書に紐づく用語を更新し渡す */
        return _dics.get(id);
    }


    @Override
    public ICoronaDic getDictionary(String name) {
        if (_dics == null) {
            updateDictionarys();
        }
        for (Entry<Integer, ICoronaDic> entry : _dics.entrySet()) {
            if (entry.getValue().getName().equals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }


    /**
     * 辞書情報取得(辞書ID)
     * 
     * @param id
     *            辞書ID
     * @return 辞書情報リスト
     */
    public List<ICoronaDic> getDictionary(Set<Integer> id) {
        List<ICoronaDic> list = new ArrayList<ICoronaDic>();
        if (_dics == null) {
            updateDictionarys();
        }
        for (int i : id) {
            /* 辞書に紐づく用語を更新し渡す */
            ICoronaDic dic = _dics.get(i);
            if (dic != null) {
                list.add(dic);
            }
        }
        return list;
    }


    /**
     * 辞書を一覧に追加する
     * 
     * @param dic
     *            辞書
     */
    @Override
    public boolean addDictionary(ICoronaDic dic) {
        if (_dics == null) {
            updateDictionarys();
        }
        _dics.put(dic.getId(), dic);
        return true;
    }


    @Override
    public void removeDictionary(int dicId) {
        /* インアクティブフラグを設定する */
        try {
            DicTableBean dicTbl = (DicTableBean) session.get(DicTableBean.class, dicId);
            if (dicTbl != null) {
                dicTbl.setInactive(true);
                dicTbl.setDicName("_%deleted%_" + dicTbl.getDicName()); //$NON-NLS-1$
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.save(dicTbl);
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
            }

            /* 辞書のプライオリティテーブルから削除する */
            @SuppressWarnings("unchecked")
            List<DicPriorityBean> dicPriList = CommonCreateQuery.getDicPriQuery(dicId).list();
            if (dicPriList != null) {
                /* トランザクション開始 */
                this.session.beginTransaction();
                for (DicPriorityBean dicPri : dicPriList) {
                    this.session.delete(dicPri);
                }
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }

        if (_dics == null) {
            updateDictionarys();
        }
        _dics.remove(dicId);
    }


    @Override
    public boolean exportDictionarys(String outputPath, String encoding) {
        /* TODO 自動生成されたメソッド・スタブ */
        return false;
    }


    @Override
    public Collection<IUserDic> searchParentDic(List<IUserDic> searchDics, String header, String reading, String part, String cls, String cform) {
        Set<IUserDic> out = new TreeSet<IUserDic>(new Comparator<IUserDic>() {
            @Override
            public int compare(IUserDic o1, IUserDic o2) {
                return o1.getId() - o2.getId();
            }
        });
        try {
            @SuppressWarnings("unchecked")
            List<DicCommonBean> dicCommonList = CommonCreateQuery.getDicCommonQuery(searchDics, header, reading, TermPart.valueOfName(part),
                    TermClass.valueOfName(cls), TermCForm.valueOf(cform, TermPart.valueOfName(part), TermClass.valueOfName(cls))).list();
            for (DicCommonBean dicCommon : dicCommonList) {
                for (IUserDic usrDic : searchDics) {
                    if (usrDic.getId() == dicCommon.getDicId()) {
                        out.add(usrDic);
                    }
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return out;
    }


    /**
     * 辞書情報更新
     * 
     * @return List<ICoronaDic>
     */
    public IStatus updateDictionarys() {
        Map<Integer, ICoronaDic> workMap = new TreeMap<Integer, ICoronaDic>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });

        if (_dics == null) {
            _dics = new TreeMap<Integer, ICoronaDic>();
        }

        updateCategorys();
        try {
            @SuppressWarnings("unchecked")
            List<DicTableBean> dicTableList = CommonCreateQuery.getDicTableQuery(false).list();
            IDicFactory factory = DicFactory.getInstance();
            for (DicTableBean dicTable : dicTableList) {
                ICoronaDic dic = this._dics.get(dicTable.getDicId());
                if (dic == null) {
                    switch (DicType.valueOf(dicTable.getDicType())) {
                    case JUMAN: /* JUMAN辞書 */
                    case COMMON: /* 一般辞書 */
                    case CATEGORY: /* 分野辞書 */
                    case SPECIAL: /* 固有辞書 */
                        dic = factory.createUserDic(dicTable.getDicName(), dicTable.getDicFileName(), DicType.valueOf(dicTable.getDicType()));
                        /* カテゴリIDがNULLの場合は処理しない */
                        if (dicTable.getCategoryId() != null) {
                            for (TextItem item : _categoryList) {
                                if (dicTable.getCategoryId() == item.getId()) {
                                    ((IUserDic) dic).setDicCategory(item);
                                }
                            }
                        }
                        break;
                    case LABEL: /* ラベル辞書 */
                        dic = factory.createLabelDic(dicTable.getDicName());
                        dic.setParentIds(CoronaIoUtils.stringToIntSet(dicTable.getParentId()));
                        break;
                    case FLUC: /* ゆらぎ辞書 */
                        dic = factory.createFlucDic(dicTable.getDicName(), CoronaIoUtils.stringToIntSet(dicTable.getParentId()));
                        break;
                    case SYNONYM: /* 同義語辞書 */
                        dic = factory.createSynonymDic(dicTable.getDicName(), CoronaIoUtils.stringToIntSet(dicTable.getParentId()));
                        break;
                    case PATTERN: /* パターン辞書 */
                        dic = factory.createPatternDic(dicTable.getDicName());
                        break;
                    default:
                        CoronaIoUtils.setErrorLog(IStatus.WARNING, "辞書タイプ誤り:ID=" + dicTable.getDicId(), null); //$NON-NLS-1$
                    }
                } else if (DicType.valueOf(dicTable.getDicType()).equals(DicType.JUMAN) && dicTable.getDicType() != ((UserDic) dic).getDicType().getIntValue()) {
                    /* juman辞書の取りこみがあった場合は同期を取り直す */
                    dic = factory.createUserDic(dicTable.getDicName(), dicTable.getDicFileName(), DicType.valueOf(dicTable.getDicType()));
                }
                if (dic != null) {
                    dic.setId(dicTable.getDicId()); /* 辞書ID設定 */
                    dic.setLasted(dicTable.getDate()); /* 更新日時設定 */
                    dic.setCreationTime(dicTable.getCreationTime()); /* 作成日時 */
                    workMap.put(dicTable.getDicId(), dic);
                }
            }
            _dics = workMap;
            return Status.OK_STATUS;

        } catch (HibernateException e) {
            e.printStackTrace();
            return new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, Messages.IoService_getDicInfoFail + e.getLocalizedMessage(), e);
        }
    }


    /* ****************************************
     * 分野名系
     */
    @Override
    public List<TextItem> updateCategorys() {
        List<TextItem> workList = new ArrayList<TextItem>();

        if (_categoryList == null) {
            _categoryList = new ArrayList<TextItem>();
        }
        try {
            @SuppressWarnings("unchecked")
            List<CategoryBean> categoryList = CommonCreateQuery.getCategoryQuery().list();
            for (CategoryBean category : categoryList) {
                TextItem item = null;
                for (TextItem i : _categoryList) {
                    if (i.getId() == category.getId()) {
                        i.setText(category.getName());
                        item = i;
                        break;
                    }
                }
                if (item == null) {
                    item = new TextItem(category.getId(), category.getName());
                }
                workList.add(item);
            }
            /* 最新データ更新 */
            _categoryList = workList;
            return _categoryList;
        } catch (HibernateException e) {
            e.printStackTrace();
            return _categoryList;
        }
    }


    @Override
    protected int addCategoryDam(String category) {
        try {
            int id = 0;
            CategoryBean result = (CategoryBean) CommonCreateQuery.getCategoryQuery(category).uniqueResult();
            if (result != null) {
                id = result.getId();
                if (id != 0) {
                    return 0;
                }
            }
            CategoryBean addCategory = new CategoryBean();
            addCategory.setName(category);
            /* トランザクション開始 */
            this.session.beginTransaction();
            this.session.save(addCategory);
            this.session.flush();
            /* トランザクション確定 */
            this.session.getTransaction().commit();
            result = (CategoryBean) CommonCreateQuery.getCategoryQuery(category).uniqueResult();
            if (result.getId() <= 0) {
                throw new HibernateException(Messages.IoService_systemErrorGetCategoryInfo);
            }
            return result.getId();
        } catch (HibernateException e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
    }


    @Override
    public void removeCategory(String categoryName) {
        CategoryBean category = (CategoryBean) CommonCreateQuery.getCategoryQuery(categoryName).uniqueResult();
        if (category != null) {
            try {
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.delete(category);
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
            } catch (HibernateException e) {
                e.printStackTrace();
            } finally {
                if (this.session.getTransaction().isActive()) {
                    this.session.getTransaction().rollback();
                }
            }
            category = (CategoryBean) CommonCreateQuery.getCategoryQuery(categoryName).uniqueResult();
            if (category != null) {
                throw new HibernateException("システムエラーが発生しました。(カテゴリ情報削除)"); //$NON-NLS-1$
            }
        }
        for (Iterator<TextItem> itr = _categoryList.iterator(); itr.hasNext();) {
            TextItem item = itr.next();
            if (item.getText().equals(categoryName)) {
                itr.remove();
                break;
            }
        }
    }


    @Override
    public boolean modifyCategory(int categoryId, String newCategoryName) {
        try {
            CategoryBean category = (CategoryBean) CommonCreateQuery.getCategoryQuery(categoryId).uniqueResult();
            if (category != null) {
                category.setName(newCategoryName);
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.save(category);
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
                for (TextItem item : _categoryList) {
                    if (item.getId() == categoryId) {
                        item.setText(newCategoryName);
                        break;
                    }
                }
                return true;
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
        return false;
    }


    /* ****************************************
     * パターン分類系
     */
    @Override
    public PatternType addPatternType(String name) {
        TypePatternBean typePattern = new TypePatternBean();
        try {
            typePattern.setName(name);
            /* トランザクション開始 */
            this.session.beginTransaction();
            this.session.save(typePattern);
            this.session.flush();
            /* トランザクション確定 */
            this.session.getTransaction().commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
        int id = 0;
        try {
            @SuppressWarnings("unchecked")
            List<TypePatternBean> list = CommonCreateQuery.getTypePatternQuery(name).list();
            if (list != null && list.size() > 0) {
                typePattern = list.get(0);
                id = typePattern.getId();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            id = -1;
        }
        PatternType patternType = new PatternType(id, name);
        PatternType.addPatternType(patternType);
        return patternType;
    }


    @Override
    public boolean removePatternType(PatternType type) {
        TypePatternBean typePattern = (TypePatternBean) CommonCreateQuery.getTypePatternQuery(type.getId()).uniqueResult();
        try {
            if (typePattern != null) {
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.delete(typePattern);
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
                typePattern = (TypePatternBean) CommonCreateQuery.getTypePatternQuery(type.getId()).uniqueResult();
                if (typePattern == null) {
                    PatternType.removePatternType(type);
                    return true;
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
        return false;
    }


    @Override
    public PatternType[] getPatternTypes() {
        try {
            @SuppressWarnings("unchecked")
            List<TypePatternBean> typePatternList = CommonCreateQuery.getTypePatternQuery().list();
            for (TypePatternBean typePattern : typePatternList) {
                int id = typePattern.getId();
                PatternType type = PatternType.getPatternType(id);
                if (type == null) {
                    type = new PatternType(typePattern.getId(), typePattern.getName());
                    PatternType.addPatternType(type);
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return PatternType.getPatternTypes();
    }


    /**
     * 情報更新処理
     * 
     * @return true/false
     */
    @Override
    public boolean update() {
        /* TODO IStatusに直そうなー(codeは適当) */
        MultiStatus retStatus = new MultiStatus(IoActivator.PLUGIN_ID, 320, Messages.IoService_modifyProject, null);

        /* プロジェクト情報再取得 */
        IStatus status = updateProjects();
        retStatus.add(status);

        /* 辞書情報を再取得 */
        status = updateDictionarys();
        retStatus.add(status);

        /* 問い合わせデータ情報再取得 */
        status = updateClaimDatas();
        retStatus.add(status);

        return retStatus.isOK();
    }


    @Override
    public boolean commit(IProgressMonitor monitor) {
        return commit(true, monitor);
    }


    @Deprecated
    @Override
    public boolean commit(boolean bRecords, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getProductName(int productId) {
        ProductBean product = (ProductBean) CommonCreateQuery.getProductQuery(productId).uniqueResult();
        if (product != null) {
            return product.getProductName();
        }
        return null;
    }


    @Override
    public List<IFieldHeader> getTableColumnsDam(String path, String definePath, String tableName) throws SQLException, IOException, HibernateException {
        /*
         * 1．ダミーテーブルを作成
         * 2.カラム情報をList<IFieldHeader>で作成
         * 3．作成したダミーテーブルを削除
         * 4.カラム情報を返却
         */
        String strSQL;
        String dbName = tableName + Messages.IoService_dummy;
        List<IFieldHeader> list = new ArrayList<IFieldHeader>();

        /* 問い合わせデータテーブル構築 */
        strSQL = createClaimDataTable(definePath, dbName);
        try {
            /* トランザクション開始 */
            this.session.beginTransaction();
            this.session.createSQLQuery(strSQL).executeUpdate();
            this.session.flush();

            /* カラム情報取得 */
            String[] columns = null;
            String strColSQL = "Show Columns From " + dbName; //$NON-NLS-1$
            List<String> tableColumns = new ArrayList<String>();
            @SuppressWarnings("unchecked")
            List<Object[]> colList = this.session.createSQLQuery(strColSQL).list();
            if (colList != null) {
                for (Object[] rs : colList) {
                    tableColumns.add((String) rs[0]);
                }
                columns = tableColumns.toArray(new String[tableColumns.size()]);
            }

            for (int i = 0; i < columns.length; i++) {
                /* DB名・フィールド名は同一のを入れる */
                IFieldHeader header = new FieldHeader(columns[i], columns[i], "", i + 1); //$NON-NLS-1$
                list.add(header);
            }

            /* 作成したテーブルの削除 */
            StringBuilder strExeSql = new StringBuilder(128);
            strExeSql.append("DROP TABLE ").append(dbName); //$NON-NLS-1$
            this.session.createSQLQuery(strExeSql.toString()).executeUpdate();
            this.session.flush();

            /* トランザクション終了 */
            this.session.getTransaction().commit();
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strSQL); //$NON-NLS-1$
            return null;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
        return list;
    }


    @Override
    public boolean chkRelPrjClm(int id) {
        @SuppressWarnings("unchecked")
        List<RelPrjClmBean> relPrjClm = CommonCreateQuery.getRelPrjClmQuery(id).list();
        if (relPrjClm != null && relPrjClm.size() > 0) {
            return true;
        }
        return false;
    }


    /**
     * DB内のDBバージョン情報の取得
     * 
     * @param result
     *            実行結果をセット（0:正常)
     * @return バージョン情報（取得失敗時は空文字列）
     */
    @Override
    public String getDbVersionDam(int result[]) {
        CoronaDbVersionBean version = (CoronaDbVersionBean) CommonCreateQuery.getCoronaDbVersionQuery(this.session).uniqueResult();
        if (version != null) {
            return version.getVersion();
        }
        return null;
    }


    /**
     * 問い合わせ中間テーブル生成
     * TODO:トランザクション開始中にエラーが発生すると、上位のfinallyでクローズされるはず
     * 
     * @param name
     *            問い合わせデータ名
     * @param dbName
     *            問い合わせデータを格納しているテーブル名
     * @param type
     *            テーブルタイプ
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     * @param claimId
     *            問い合わせデータID
     * @param tgts
     *            ターゲットフィールドID
     * @return 登録後の中間データテーブルID
     * @throws SQLException
     *             , HibernateException
     */
    private int createClaimWorkDataTable(String name, String dbName, TableType type, int projectId, int productId, int claimId, Set<Integer> tgts)
            throws SQLException, HibernateException {

        // ClaimWorkDataDaoより移設

        StringBuilder strSQL = new StringBuilder(192).append("CREATE TABLE "); //$NON-NLS-1$
        if (dbName == null) {
            throw new HibernateException("Not find dbname"); //$NON-NLS-1$
        }
        /* テーブルを作成 */
        String strExeSql = ""; //$NON-NLS-1$
        String strWork = CoronaIoUtils.createWorkTableName(dbName, type, projectId);
        int workId = 0;

        /* TablesDao.getTableId(strWork);　を置換する。 */
        try {
            strExeSql = new StringBuilder(64).append("SELECT ID FROM TABLES WHERE DBNAME ='").append(strWork).append("'").toString(); //$NON-NLS-1$ //$NON-NLS-2$
            @SuppressWarnings("unchecked")
            List<Object> list = this.session.createSQLQuery(strExeSql).list();
            if (list != null && list.size() > 0) {
                Object rs = list.get(0);
                workId = Integer.parseInt(rs.toString());
            }
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
            throw e;
        }
        if (workId == 0) {
            // ここはUSR_WORK_もしくはUSR_CM_を作成する。USR_CM_(誤記補正)の場合は現状のままでテーブルを作る。
            strSQL.append(strWork).append(" (").append("WORK_ID INT NOT NULL, ").append("FLD_ID INT NOT NULL, ").append("REC_ID INT NOT NULL, "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            if (TableType.CORRECTION_MISTAKES_DATA.equals(type)) {
                // USR_CM_の場合
                strSQL.append("DATA MEDIUMTEXT, ").append("PRIMARY KEY (WORK_ID, FLD_ID, REC_ID));"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                // USR_WORK_を作成する場合、履歴IDフィールドを持たせる。
                strSQL.append("HISTORY_ID INT, "); //$NON-NLS-1$
                strSQL.append("DATA MEDIUMTEXT, ").append("PRIMARY KEY (WORK_ID, FLD_ID, REC_ID, HISTORY_ID));"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            try {
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.createSQLQuery(strSQL.toString()).executeUpdate();
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
            } catch (HibernateException e) {
                CoronaActivator.debugLog("Error SQL : " + strSQL.toString()); //$NON-NLS-1$
                throw e;
            } finally {
                if (this.session.getTransaction().isActive()) {
                    this.session.getTransaction().rollback();
                }
            }

            /* テーブルリストに登録 */
            /* TablesDao.insertTable(name, strWork, type);　を置換する。 */
            try {
                // 「INSERT IGNORE INTO TABLES....」の機能置換
                StringBuilder strCheckHQL = new StringBuilder(150).append("from TablesBean where name = :NAME and dbname = :DBNAME and type = :TYPE"); //$NON-NLS-1$
                @SuppressWarnings("unchecked")
                List<TablesBean> list = this.session.createQuery(strCheckHQL.toString()).setString("NAME", name) //$NON-NLS-1$
                        .setString("DBNAME", strWork) //$NON-NLS-1$
                        .setInteger("TYPE", type.getIntValue()) //$NON-NLS-1$
                        .list();
                if (list != null && list.size() == 0) {
                    StringBuilder strSql = new StringBuilder(128).append("INSERT INTO TABLES (NAME, DBNAME, TYPE, LASTED) VALUES('"); //$NON-NLS-1$
                    strSql.append(name).append("','").append(strWork).append("',").append(type.getIntValue()).append(",now())"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    strExeSql = strSql.toString();
                    /* トランザクション開始 */
                    this.session.beginTransaction();
                    this.session.createSQLQuery(strExeSql).executeUpdate();
                    this.session.flush();
                    /* トランザクション確定 */
                    this.session.getTransaction().commit();
                }

            } catch (HibernateException e) {
                CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
                throw e;
            } finally {
                if (this.session.getTransaction().isActive()) {
                    this.session.getTransaction().rollback();
                }
            }
            /* workId = TablesDao.getTableId(strWork); を置換する。 */
            try {
                String strSql = new StringBuilder(64).append("SELECT ID FROM TABLES WHERE DBNAME ='").append(dbName).append("'").toString(); //$NON-NLS-1$ //$NON-NLS-2$
                strExeSql = strSql;
                @SuppressWarnings("unchecked")
                List<Object> list = this.session.createSQLQuery(strExeSql).list();
                if (list != null && list.size() > 0) {
                    Object rs = list.get(0);
                    workId = Integer.parseInt(rs.toString());
                }
            } catch (HibernateException e) {
                CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
                throw e;
            }
            if (workId == 0) {
                throw new HibernateException("not find workId"); //$NON-NLS-1$
            }

            // 検索処理速度改善対策でUSR_CLAIM＿xxxにIndexを登録
            if (!TableType.CORRECTION_MISTAKES_DATA.equals(type)) {
                StringBuilder strCreateIndex = new StringBuilder("CREATE INDEX "); //$NON-NLS-1$
                strCreateIndex.append(strWork).append("_INDEX ON ").append(strWork).append("(WORK_ID, HISTORY_ID, REC_ID ASC);"); //$NON-NLS-1$ //$NON-NLS-2$
                // CommonDao.executeSQL4Throws(strCreateIndex.toString());
                // を置換する。
                try {
                    strExeSql = strCreateIndex.toString();
                    /* トランザクション開始 */
                    this.session.beginTransaction();
                    this.session.createSQLQuery(strExeSql).executeUpdate();
                    this.session.flush();
                    /* トランザクション確定 */
                    this.session.getTransaction().commit();
                } catch (HibernateException e) {
                    CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
                    throw e;
                } finally {
                    if (this.session.getTransaction().isActive()) {
                        this.session.getTransaction().rollback();
                    }
                }
            }
        }

        if (type == TableType.WORK_DATA) {
            /* TODO:パターンリレーションテーブルを作成 */
            String strRelPtn = CoronaIoUtils.createWorkTableName(dbName, TableType.RESULT_DATA, projectId);
            int relId = 0;
            /* TablesDao.getTableId(strRelPtn); を置換する。 */
            try {
                String strSql = new StringBuilder(64).append("SELECT ID FROM TABLES WHERE DBNAME ='").append(strRelPtn).append("'").toString(); //$NON-NLS-1$ //$NON-NLS-2$
                strExeSql = strSql;
                @SuppressWarnings("unchecked")
                List<Object> list = this.session.createSQLQuery(strExeSql).list();
                if (list != null && list.size() > 0) {
                    Object rs = list.get(0);
                    relId = Integer.parseInt(rs.toString());
                }
            } catch (HibernateException e) {
                CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
                throw e;
            }

            if (relId == 0) {
                StringBuilder createSql = new StringBuilder(128);
                createSql.append("Create Table ").append(strRelPtn); //$NON-NLS-1$
                createSql.append("(work_id Int Not NULL,").append("fld_id Int Not NULL,"); //$NON-NLS-1$ //$NON-NLS-2$
                createSql.append("history Int Not NULL,").append("rec_id Int Not NULL,"); //$NON-NLS-1$ //$NON-NLS-2$
                createSql.append("pattern_id Int Not NULL,").append("hit_info MediumText Not NULL)"); //$NON-NLS-1$ //$NON-NLS-2$
                // CommonDao.executeSQL4Throws(createSql.toString());
                try {
                    strExeSql = createSql.toString();
                    /* トランザクション開始 */
                    this.session.beginTransaction();
                    this.session.createSQLQuery(strExeSql).executeUpdate();
                    this.session.flush();
                    /* トランザクション確定 */
                    this.session.getTransaction().commit();
                } catch (HibernateException e) {
                    CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
                    throw e;
                } finally {
                    if (this.session.getTransaction().isActive()) {
                        this.session.getTransaction().rollback();
                    }
                }
                // テーブルリストに登録
                /*
                 * TablesDao.insertTable(TableType.RESULT_DATA.toString(),
                 * strRelPtn, TableType.RESULT_DATA); を置換する。
                 */
                try {
                    String paraName = TableType.RESULT_DATA.toString();
                    String paraDbName = strRelPtn;
                    int paraType = TableType.RESULT_DATA.getIntValue();

                    // 「INSERT IGNORE INTO WORKDATAS....」の機能置換
                    StringBuilder strCheckHQL = new StringBuilder(150).append("from TablesBean where name = :NAME and dbname = :DBNAME and type = :TYPE"); //$NON-NLS-1$
                    @SuppressWarnings("unchecked")
                    List<TablesBean> list = this.session.createQuery(strCheckHQL.toString()).setString("NAME", paraName) //$NON-NLS-1$
                            .setString("DBNAME", paraDbName) //$NON-NLS-1$
                            .setInteger("TYPE", paraType) //$NON-NLS-1$
                            .list();
                    if (list != null && list.size() == 0) {
                        StringBuilder strSql = new StringBuilder(128).append("INSERT INTO TABLES (NAME, DBNAME, TYPE, LASTED) VALUES('"); //$NON-NLS-1$
                        strSql.append(paraName).append("','").append(paraDbName).append("',").append(paraType).append(",now())"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        strExeSql = strSql.toString();
                        /* トランザクション開始 */
                        this.session.beginTransaction();
                        this.session.createSQLQuery(strExeSql).executeUpdate();
                        this.session.flush();
                        /* トランザクション確定 */
                        this.session.getTransaction().commit();
                    }
                } catch (HibernateException e) {
                    CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
                    throw e;
                } finally {
                    if (this.session.getTransaction().isActive()) {
                        this.session.getTransaction().rollback();
                    }
                }
                /* relId = TablesDao.getTableId(strRelPtn); を置換 */
                relId = 0;
                try {
                    String strSql = new StringBuilder(64).append("SELECT ID FROM TABLES WHERE DBNAME ='").append(strRelPtn).append("'").toString(); //$NON-NLS-1$ //$NON-NLS-2$
                    strExeSql = strSql;
                    @SuppressWarnings("unchecked")
                    List<Object> list = this.session.createSQLQuery(strExeSql).list();
                    if (list != null && list.size() > 0) {
                        Object rs = list.get(0);
                        relId = Integer.parseInt(rs.toString());
                    }
                } catch (HibernateException e) {
                    CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
                    throw e;
                }
                if (relId == 0) {
                    throw new HibernateException("not find workId"); //$NON-NLS-1$
                }

                StringBuffer strCreateIndex = new StringBuffer().append("CREATE INDEX "); //$NON-NLS-1$
                strCreateIndex.append(strRelPtn).append("_INDEX ON ").append(strRelPtn).append("(WORK_ID, HISTORY, REC_ID ASC);"); //$NON-NLS-1$ //$NON-NLS-2$
                /*
                 * CommonDao.executeSQL4Throws(strCreateIndex.toString());
                 * を置換する。
                 */
                try {
                    strExeSql = strCreateIndex.toString();
                    /* トランザクション開始 */
                    this.session.beginTransaction();
                    this.session.createSQLQuery(strExeSql).executeUpdate();
                    this.session.flush();
                    /* トランザクション確定 */
                    this.session.getTransaction().commit();
                } catch (HibernateException e) {
                    CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
                    throw e;
                } finally {
                    if (this.session.getTransaction().isActive()) {
                        this.session.getTransaction().rollback();
                    }
                }

            }

            /* クレーム-ターゲットリレーションを追加 */
            insertRelationClaimData(projectId, productId, claimId, workId, relId, tgts);


        }

        /* テーブルIDを取得 */
        /* return TablesDao.getTableId(strWork); を置換する。 */
        try {
            strExeSql = new StringBuilder(64).append("SELECT ID FROM TABLES WHERE DBNAME ='").append(strWork).append("'").toString(); //$NON-NLS-1$ //$NON-NLS-2$
            workId = 0;
            @SuppressWarnings("unchecked")
            List<Object> list = this.session.createSQLQuery(strExeSql).list();
            if (list != null && list.size() > 0) {
                Object rs = list.get(0);
                workId = Integer.parseInt(rs.toString());
            }
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
            throw e;
        }
        return workId;
    }


    /**
     * あるプロジェクトのあるターゲットと問合せデータを紐づけを作成する。<br/>
     * 今保存されている値は、リストの値に置き換えられる。
     * 
     * @param projectId
     *            あるプロジェクトのID
     * @param productId
     *            あるターゲットのID
     * @param claimId
     *            問い合わせデータのID
     * @param workId
     *            処理結果格納テーブルのID。<br/>
     *            0なら無視される。
     * @param relId
     *            構文パターンを保持するテーブルのID。<br/>
     *            0なら無視される。
     * @param tgts
     *            設定するマイニング対象フィールドIDのリスト
     * @return 正常に追加できたらtrue。そうでなければfalse
     */
    private boolean insertRelationClaimData(int projectId, int productId, int claimId, int workId, int relId, Collection<Integer> tgts) {

        // ProductDaoより移設
        String strTgts = CoronaIoUtils.intListToString(tgts);

        StringBuilder strSQL = new StringBuilder(1024);
        strSQL.append("MERGE INTO REL_CLM_PRODUCT ").append("(PRJ_ID, PRODUCT_ID, TBL_ID, WORK_TBL_ID, REL_TBL_ID, TGT_FLD) "); //$NON-NLS-1$ //$NON-NLS-2$
        strSQL.append(" KEY (PRJ_ID, PRODUCT_ID, TBL_ID) VALUES ("); //$NON-NLS-1$
        strSQL.append(projectId).append(",").append(productId).append(",").append(claimId).append(","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        strSQL.append(workId).append(",").append(relId).append(",").append("'").append(strTgts).append("')"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        /* return CommonDao.executeSQL(strSQL.toString()); を置換する。 */
        String strExeSql = null;
        try {
            strExeSql = strSQL.toString();
            /* トランザクション開始 */
            this.session.beginTransaction();
            this.session.createSQLQuery(strExeSql).executeUpdate();
            this.session.flush();
            /* トランザクション確定 */
            this.session.getTransaction().commit();
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
            e.printStackTrace();
            return false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
        return true;
    }


    /**
     * 問い合わせテーブル登録SQL生成(ドキュメント用）
     * 
     * @param tableName
     *            テーブル名
     * @return SQL文字列
     * @throws SQLException
     *             データ登録に失敗した場合
     */
    private boolean createClaimDataTableForDocument(String tableName, String target, List<String> records) throws SQLException {

        // ClaimDataDaoより移設

        String strExeSql = null;
        try {
            strExeSql = "CREATE TABLE " + tableName + "(ID INT NOT NULL AUTO_INCREMENT, BODY TEXT, TARGET VARCHAR(256), PRIMARY KEY (ID))"; //$NON-NLS-1$ //$NON-NLS-2$
            /* トランザクション開始 */
            this.session.beginTransaction();
            this.session.createSQLQuery(strExeSql).executeUpdate();
            this.session.flush();
            /* トランザクション確定 */
            this.session.getTransaction().commit();
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
            e.printStackTrace();
            return false;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
        StringBuilder strSQL = new StringBuilder(200);
        strSQL.append("INSERT INTO ").append(tableName).append(" (BODY,TARGET) VALUES"); //$NON-NLS-1$ //$NON-NLS-2$
        for (String s : records) {
            strSQL.append("('").append(s).append("','").append(target).append("'),"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        strSQL = strSQL.deleteCharAt(strSQL.length() - 1);
        try {
            strExeSql = strSQL.toString();
            /* トランザクション開始 */
            this.session.beginTransaction();
            this.session.createSQLQuery(strExeSql).executeUpdate();
            this.session.flush();
            /* トランザクション確定 */
            this.session.getTransaction().commit();
            return true;
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strExeSql); //$NON-NLS-1$
            // CommonDao.deleteTable(tableName);
            try {
                strExeSql = "DROP TABLE " + tableName; //$NON-NLS-1$
                /* トランザクション開始 */
                this.session.beginTransaction();
                this.session.createSQLQuery(strExeSql).executeUpdate();
                this.session.flush();
                /* トランザクション確定 */
                this.session.getTransaction().commit();
            } catch (HibernateException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            if (this.session.getTransaction().isActive()) {
                this.session.getTransaction().rollback();
            }
        }
    }


    /**
     * 問い合わせテーブル登録SQL生成
     * 
     * @param definePath
     *            定義ファイル名
     * @param tableName
     *            作成テーブル名
     * @return 問い合わせテーブル登録用SQL文字列
     * @throws IOException
     *             ファイルの読み込みに失敗した場合
     */
    private static String createClaimDataTable(String definePath, String tableName) throws IOException {

        // ClaimDataDao より移設

        BufferedReader br = null;
        String line;
        StringBuilder strSQL = new StringBuilder(200);
        try {
            br = new BufferedReader(new FileReader(definePath));

            while ((line = br.readLine()) != null) {
                strSQL.append(line);
            }

            /* テーブル名を置換 */
            String suffix = "("; //$NON-NLS-1$
            String prefix = "CREATE TABLE"; //$NON-NLS-1$
            int idx = strSQL.indexOf(suffix);
            if (idx > -1) {
                strSQL.delete(0, idx);
                strSQL.insert(0, " ").insert(0, tableName).insert(0, ' ').insert(0, prefix); //$NON-NLS-1$
            }
        } catch (IOException e) {
            throw e;
        } finally {
            /* ファイルを閉じる */
            if (br != null) {
                br.close();
            }
        }
        return strSQL.toString();
    }
}
