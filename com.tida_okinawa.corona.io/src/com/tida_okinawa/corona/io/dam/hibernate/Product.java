/**
 * @version $Id: Product.java 997 2013-06-05 01:58:11Z yukihiro-kinjyo $
 *
 * 2013/11/12 11:49:24
 * @author yukihiro-kinjo
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.io.File;
import java.io.FilenameFilter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.bean.DicPriorityBean;
import com.tida_okinawa.corona.io.bean.DicPriorityPKBean;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.bean.ProductBean;
import com.tida_okinawa.corona.io.bean.RelPrjProductBean;
import com.tida_okinawa.corona.io.bean.RelProductDicBean;
import com.tida_okinawa.corona.io.bean.RelProductDicPKBean;
import com.tida_okinawa.corona.io.bean.TablesBean;
import com.tida_okinawa.corona.io.bean.WorkdatasBean;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkFAData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.abstraction.AbstractProduct;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.table.TableType;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * @author yukihiro-kinjo
 */
public final class Product extends AbstractProduct {

    /**
     * {@link IClaimWorkData} の コンパレータ
     */
    final private Comparator<IClaimWorkData> comparator = new Comparator<IClaimWorkData>() {
        @Override
        public int compare(IClaimWorkData o1, IClaimWorkData o2) {
            /* nullは後ろに持っていく */
            if (o1 == null)
                return 1;
            if (o2 == null)
                return -1;

            /* update() できてない? */
            if (o1.getFieldId() == 0)
                o1.update();
            if (o2.getFieldId() == 0)
                o2.update();

            /* 問い合わせデータが後のものを前に */
            int n1 = o2.getClaimId() - o1.getClaimId();
            if (n1 != 0) {
                return n1;
            }
            /* フィールドが後のものを前に */
            int n2 = o2.getFieldId() - o1.getFieldId();
            if (n2 != 0) {
                return n2;
            }
            /* 処理手順が後のものを前に */
            int n3 = o2.getClaimWorkDataType().getIntValue() - o1.getClaimWorkDataType().getIntValue();
            return n3;
        }
    };

    private Set<IClaimWorkData> _claimWorks = new TreeSet<IClaimWorkData>(comparator);
    private Map<Integer, Set<Integer>> _miningTargets = new HashMap<Integer, Set<Integer>>();
    /**
     * 処理結果のレコードが更新されたかどうかのフラグ
     */
    private boolean bInitRecords = false;


    /**
     * コンストラクタ
     * 
     * @param name
     *            ターゲット名
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     */
    public Product(String name, int projectId, int productId) {
        super(name, projectId);
        _id = productId;
    }


    /**
     * コンストラクタ
     * 
     * @param name
     *            ターゲット名
     * @param parent
     *            プロジェクト
     * 
     */
    public Product(String name, ICoronaProject parent) {
        super(name, parent);
    }


    /**
     * コンストラクタ
     * 
     * @param name
     *            ターゲット名
     * @param projectId
     *            プロジェクトID
     */
    public Product(String name, int projectId) {
        super(name, projectId);
    }


    /**
     * 辞書情報取得(ターゲットに紐づく辞書を取得)
     * 
     * @param productId
     * @return List<ICoronaDic>
     * 
     */
    @Override
    protected List<ICoronaDic> getDictionarysDam(Class<?> cls, int productId) {
        List<ICoronaDic> wrkList = new ArrayList<ICoronaDic>(); /* 編集用List */
        if (_dics == null) {
            _dics = new ArrayList<ICoronaDic>();
        }
        /* 最新のカテゴリ情報を取得 */
        List<TextItem> categoryList = ((IoService) IoService.getInstance()).getCategorys();

        try {
            /* HQL実行 */
            String hql = "from DicTableBean as a, RelProductDicBean as b where a.inactive = :inActive and a.dicId = b.primaryKey.dicId and b.primaryKey.productId = :Product_Id"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            @SuppressWarnings("unchecked")
            List<Object[]> resultList = session.createQuery(hql).setBoolean("inActive", false).setInteger("Product_Id", productId).list(); //$NON-NLS-1$//$NON-NLS-2$
            /* 取得結果編集 */
            for (Object[] result : resultList) {
                DicTableBean dtItem = (DicTableBean) result[0];
                ICoronaDic dic = null;

                /* 既存データチェック */
                int dicId = dtItem.getDicId();
                dic = ((IoService) IoActivator.getService()).getDictionary(dicId);
                if (dic == null) {
                    /* 新規データ */
                    dic = CoronaObjectUtil.createDicByCurrentData(dtItem, categoryList);
                } else {
                    dic.setName(dtItem.getDicName());
                    dic.setLasted(dtItem.getDate());
                    dic.setCreationTime(dtItem.getCreationTime());
                    /* dirtyはそのままにする */
                }
                /* wrkListの追加 */
                if (dic != null) {
                    wrkList.add(dic);
                }
            }
            /* 最新データの更新 */
            _dics = wrkList;

        } catch (HibernateException e) {
            e.printStackTrace();
        }

        /* 返却用データ編集 */
        List<ICoronaDic> dicList = new ArrayList<ICoronaDic>(); /* 結果返却用List */
        for (ICoronaDic d : _dics) {
            if (cls.isAssignableFrom(d.getClass())) {
                dicList.add(d);
            }
        }
        return dicList;
    }


    @Override
    protected Set<IClaimWorkData> getClaimWorkDatasDam() {
        if (!bInitRecords) {
            updateRecords();
        }
        return _claimWorks;
    }


    @Override
    protected IClaimWorkData getClaimWorkDataDam(int claimId, ClaimWorkDataType type, int fieldId) {
        if (!bInitRecords) {
            updateRecords();
        }
        switch (type) {
        case LASTED:
            /* 更新日でチェックする */
            int intType = 0;
            try {
                String hql = "from WorkdatasBean where lasted = (select max(lasted) from WorkdatasBean as w where ((projectId = 0 and productId = 0) or " //$NON-NLS-1$
                        + "(projectId = :projectId and productId = :productId )) and inputTableId = :claimId and type != 9)"; //$NON-NLS-1$
                Session session = IoService.getInstance().getSession();
                @SuppressWarnings("unchecked")
                List<WorkdatasBean> workdatas = session.createQuery(hql).setInteger("projectId", this.getProjectId()) //$NON-NLS-1$
                        .setInteger("productId", getId()) //$NON-NLS-1$
                        .setInteger("claimId", claimId).list(); //$NON-NLS-1$
                if (workdatas != null && workdatas.size() > 0) {
                    intType = (workdatas.get(0)).getType();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                intType = -1;
            }
            /* もう一回、実タイプで探す。 */
            return getClaimWorkDataDam(claimId, ClaimWorkDataType.valueOf(intType), fieldId);
        case LASTED_EXEC:
            /* 更新日でチェックする */
            int intType2 = 0;
            try {
                String hql = "from WorkdatasBean where lasted = (select max(lasted) from WorkdatasBean as w where ((projectId = 0 and productId = 0) or " //$NON-NLS-1$
                        + "(projectId = :projectId and productId = :productId )) and inputTableId = :claimId and type != 9 and type != 7)"; //$NON-NLS-1$
                Session session = IoService.getInstance().getSession();
                @SuppressWarnings("unchecked")
                List<WorkdatasBean> workdatas = session.createQuery(hql).setInteger("projectId", this.getProjectId()) //$NON-NLS-1$
                        .setInteger("productId", getId()) //$NON-NLS-1$
                        .setInteger("claimId", claimId).list(); //$NON-NLS-1$
                if (workdatas != null && workdatas.size() > 0) {
                    intType2 = (workdatas.get(0)).getType();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                intType2 = -1;
            }
            /* もう一回、実タイプで探す。 */
            return getClaimWorkDataDam(claimId, ClaimWorkDataType.valueOf(intType2), fieldId);
        case CORRECTION_MISTAKES:
        case DEPENDENCY_STRUCTURE:
        case MORPHOLOGICAL:
        case CORRECTION_FLUC:
        case CORRECTION_SYNONYM:
        case RESLUT_PATTERN: /* #112 の対応 */
        case FREQUENTLY_APPERING:
            for (IClaimWorkData work : _claimWorks) {
                if (work.getClaimId() == claimId && work.getFieldId() == fieldId && work.getClaimWorkDataType() == type) {
                    return work;
                }
            }
            break;
        default:
            // ERROR
            break;
        }
        return null;
    }


    @Override
    protected boolean addDictionaryDam(ICoronaDic dic) {
        int dicId = 0; /* 辞書ID */

        Session session = IoService.getInstance().getSession();
        try {

            /* 辞書情報のチェック (_dicsがNullの場合は情報を取得する。) */
            if (_dics == null) {
                _dics = getDictionarys(ICoronaDic.class);
            }

            /* 登録済チェック */
            String strHql = "from DicTableBean where dicName = :dicName"; //$NON-NLS-1$
            try {
                @SuppressWarnings("unchecked")
                List<DicTableBean> dt = session.createQuery(strHql).setString("dicName", dic.getName()).list(); //$NON-NLS-1$
                dicId = (dt != null && dt.size() > 0) ? dt.get(0).getDicId() : 0;
            } catch (HibernateException e) {
                dicId = -1;
            }

            if (dicId == -1) {
                throw new HibernateException(Messages.Product_systemErrorGetDic);
            } else if (dicId == 0) {
                /* 辞書が未登録の場合、辞書を登録 */
                DicTableBean insertDt = new DicTableBean();
                String nonStr = ""; //$NON-NLS-1$
                if (dic instanceof UserDic) {
                    /* ユーザ辞書の場合 */
                    IUserDic userDic = (IUserDic) dic;
                    /* ファイル名の拡張子を".dic"へ変更 */
                    String dicFileName = userDic.getFileName().substring(0, userDic.getFileName().indexOf(".")) + Messages.Product_extensionDic; //$NON-NLS-1$
                    userDic.setFileName(dicFileName);
                    /* 分野辞書・一般辞書・専門辞書の場合 */
                    insertDt.setParentId(nonStr);
                    insertDt.setDicName(userDic.getName());
                    insertDt.setDicFileName(userDic.getFileName());
                    insertDt.setDicType(userDic.getDicType().getIntValue());
                    if (userDic.getDicType().getIntValue() == DicType.CATEGORY.getIntValue()) {
                        /* 分野辞書の場合 */
                        insertDt.setCategoryId(userDic.getDicCategory().getId());
                    }

                } else if (dic instanceof ILabelDic) {
                    /* ラベル辞書の場合 */
                    ILabelDic lblDic = (ILabelDic) dic;
                    insertDt.setParentId(CoronaIoUtils.intListToString(lblDic.getParentIds()));
                    insertDt.setDicName(lblDic.getName());
                    insertDt.setDicFileName(nonStr);
                    insertDt.setDicType(DicType.LABEL.getIntValue());

                } else if (dic instanceof IFlucDic) {
                    /* ゆらぎ辞書の場合 */
                    IFlucDic flucDic = (IFlucDic) dic;
                    insertDt.setParentId(CoronaIoUtils.intListToString(flucDic.getParentIds()));
                    insertDt.setDicName(flucDic.getName());
                    insertDt.setDicFileName(nonStr);
                    insertDt.setDicType(DicType.FLUC.getIntValue());

                } else if (dic instanceof IPatternDic) {
                    /* パターン辞書の場合 */
                    IPatternDic ptnDic = (IPatternDic) dic;
                    insertDt.setDicName(ptnDic.getName());
                    insertDt.setDicFileName(nonStr);
                    insertDt.setDicType(DicType.PATTERN.getIntValue());

                } else if (dic instanceof ISynonymDic) {
                    /* 同義語辞書の場合 */
                    ISynonymDic synDic = (ISynonymDic) dic;
                    insertDt.setParentId(CoronaIoUtils.intListToString(synDic.getParentIds()));
                    insertDt.setDicName(synDic.getName());
                    insertDt.setDicFileName(nonStr);
                    insertDt.setDicType(DicType.SYNONYM.getIntValue());
                }
                Date date = new Date();
                insertDt.setDate(date);
                insertDt.setInactive(false);
                insertDt.setCreationTime(date);

                try {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    session.save(insertDt);
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();
                } catch (HibernateException e) {
                    throw new HibernateException(Messages.Product_systemErrorSetDic);
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
                /* 辞書IDを再取得 */
                try {
                    @SuppressWarnings("unchecked")
                    List<DicTableBean> dt = session.createQuery(strHql).setString("dicName", dic.getName()).list(); //$NON-NLS-1$
                    dicId = (dt != null && dt.size() > 0) ? dt.get(0).getDicId() : 0;
                } catch (HibernateException e) {
                    dicId = -1;
                }

            } else {
                /* 辞書が存在する場合 、インアクティブフラグを落とす */
                DicTableBean dt = (DicTableBean) session.get(DicTableBean.class, dicId);
                if (dt != null) {
                    String prefix = "_%deleted%_"; //$NON-NLS-1$
                    String name = dt.getDicName();
                    if (name.indexOf(prefix) != -1) {
                        name = name.substring(prefix.length());
                    }
                    dt.setInactive(false);
                    dt.setDicName(name);

                    /* トランザクション開始 */
                    session.beginTransaction();

                    /* INACTIVEの変更では更新日付の更新は不要の為、ここでは日付の更新は実施しない。 */
                    session.save(dt);
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                    /* 辞書が存在する場合 、新日時を取得し、設定 */
                    dic.setLasted(dt.getDate());
                } else {
                    /* 辞書が存在する場合 、新日時を取得し、設定 */
                    dic.setLasted(new Date());
                }
                dic.setId(dicId);
            }

            /* ターゲットと辞書の紐づけチェック */
            RelProductDicPKBean pk = new RelProductDicPKBean();
            pk.setDicId(dicId);
            pk.setProductId(getId());
            RelProductDicBean Testrpd = (RelProductDicBean) session.get(RelProductDicBean.class, pk);
            if (Testrpd != null) {
                if (session.getTransaction().isActive()) {
                    /* トランザクションコミット */
                    session.getTransaction().commit();
                }

                /* 既に登録済の場合 */
                return true;
            }
            /* ターゲットと辞書を紐づける */
            RelProductDicBean rpd = new RelProductDicBean();
            int setProductId = getId();
            rpd.setPrimaryKey(new RelProductDicPKBean());
            rpd.getPrimaryKey().setDicId(dicId);
            rpd.getPrimaryKey().setProductId(setProductId);
            try {
                /* トランザクション開始 */
                session.beginTransaction();

                session.save(rpd);
                session.flush();

                /* トランザクションコミット */
                session.getTransaction().commit();
            } catch (HibernateException e) {
                throw new HibernateException(Messages.Product_systemErrorSetDicRelation);
            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションコミット */
                    session.getTransaction().commit();
                }
            }

            /* 辞書情報へIDを反映 */
            dic.setId(dicId);
            /*
             * ラベル辞書作成時、dicのインスタンスが置き換えられてしまい親子関係をつけられなかったので、
             * updateDictionarysから変えた
             */
            ((IoService) IoActivator.getService()).addDictionary(dic); // IoServiceの辞書リストを更新

            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
    }


    /**
     * 辞書情報紐づけ削除(ターゲットと辞書を紐づけを削除)
     * 
     * @param id
     */
    @Override
    protected void removeDictionaryDam(int id) {
        Session session = IoService.getInstance().getSession();
        try {
            /* リレーションの存在チェック */
            try {
                String strHql = "from RelProductDicBean where primaryKey.dicId = :dicId and primaryKey.productId = :productId"; //$NON-NLS-1$
                session.createQuery(strHql).setInteger("dicId", id).setInteger("productId", this._id).uniqueResult(); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (HibernateException e) {
                throw new HibernateException(Messages.Product_systemErrorGetDicId);
            }

            /* トランザクション開始 */
            session.beginTransaction();

            /* リレーションの削除 */
            try {
                int dic_id = id;
                int product_id = this._id;

                String strSQL = "delete from rel_product_dic where dic_id = " + dic_id + " AND product_id = " + product_id; //$NON-NLS-1$ //$NON-NLS-2$
                session.createSQLQuery(strSQL).executeUpdate();
                session.flush();
            } catch (HibernateException e) {
                throw new HibernateException(Messages.Product_systemErrorGetDicRelation);
            }

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


    @Override
    public boolean addClaimWorkData(IClaimWorkData data) throws HibernateException {
        int tableId = 0;
        int workdataId = 0;

        /* 誤記補正が来る場合はDBにすでに存在するため、クローン処理のみを行う */
        if (ClaimWorkDataType.CORRECTION_MISTAKES == data.getClaimWorkDataType()) {
            ClaimWorkData cm = (ClaimWorkData) ((ClaimWorkData) data).clone();
            cm.setProjectId(getProjectId());
            cm.setProductId(getId());
            return _claimWorks.add(cm);
        }

        Session session = IoService.getInstance().getSession();
        String dbName = ""; //$NON-NLS-1$
        /* クレームDB名を取得 */
        TablesBean tables = (TablesBean) session.get(TablesBean.class, data.getClaimId());
        if ((tables == null) || tables.getName().equals("")) { //$NON-NLS-1$
            throw new HibernateException(Messages.Product_errorGetDataFile);
        } else {
            dbName = tables.getDbname();
        }
        if (((ClaimWorkData) data).getDbName() == null) {
            ((ClaimWorkData) data).setDbName(dbName);
        }
        /* ワークデータテーブルが登録済みかチェック */
        String workTableName = CoronaIoUtils.createWorkTableName(dbName, TableType.WORK_DATA, getProjectId());

        try {
            // ワークデータテーブルを作成
            tableId = createClaimWorkDataTable(TableType.WORK_DATA.toString(), dbName, TableType.WORK_DATA, getProjectId(), getId(), data.getClaimId(),
                    this.getMiningFields(data.getClaimId()));
        } finally {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
        }
        /* Memo 保存のときに、dbNameが違って怒られるから、WorkDataのテーブル名を設定 Morishima */
        /*
         * Memo 誤記補正済みデータもworkDataだけどここにこないようなので、上のsetDbNameはやらないでいいんじゃないか？
         * Morishima
         */
        /*
         * Memo 再検討
         * dbNameには、問合せデータのテーブル名を入れておくようにするなら、構文解析処理のように、出力先テーブル名を随時置き換える処理が必要
         */

        /* ワークデータに必要な情報を詰め込む */
        ((ClaimWorkData) data).setDbName(workTableName);
        ((ClaimWorkData) data).setWorkTableId(tableId);

        /* WORKDATASに中間データ情報を追加する */
        try {
            String projectId = "project_Id"; //$NON-NLS-1$
            String productId = "product_Id"; //$NON-NLS-1$
            String claimId = "claim_Id"; //$NON-NLS-1$
            String type = "Type"; //$NON-NLS-1$
            StringBuilder strHQL = new StringBuilder(192).append("FROM WorkdatasBean WHERE projectId =:"); //$NON-NLS-1$
            strHQL.append(projectId).append(" AND productId =:").append(productId) //$NON-NLS-1$
                    .append(" AND inputTableId =:").append(claimId).append(" AND type= :").append(type); //$NON-NLS-1$ //$NON-NLS-2$
            @SuppressWarnings("unchecked")
            List<WorkdatasBean> list = session.createQuery(strHQL.toString()).setInteger(projectId, getProjectId()).setInteger(productId, getId())
                    .setInteger(claimId, data.getClaimId()).setInteger(type, data.getClaimWorkDataType().getIntValue()).list();
            if (list != null && list.size() > 0) {
                workdataId = list.get(0).getId();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            workdataId = -1;
        }

        if (workdataId <= 0) {
            /* WORKDATASの最大履歴IDを取得し、最大履歴ID+1の値を新規に作成する中間データ情報の履歴IDとする */
            try {
                int historyId = 0;
                String projectId = "ProjectId"; //$NON-NLS-1$
                String productId = "ProductId"; //$NON-NLS-1$
                String claimId = "claim_Id"; //$NON-NLS-1$
                String type = "Type"; //$NON-NLS-1$
                StringBuilder hql = new StringBuilder(192).append("SELECT MAX(historyId) FROM WorkdatasBean WHERE "); //$NON-NLS-1$
                hql.append("projectId = :").append(projectId).append(" AND ").append("productId = :") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .append(productId).append(" AND ").append("type = :").append(type); //$NON-NLS-1$ //$NON-NLS-2$
                try {
                    /* 最大履歴ID取得 */
                    /*
                     * WORKDATASよりプロジェクトID・ターゲットID・問い合わせID・種別が同一のレコード内で最大となる履歴IDを取得
                     */
                    @SuppressWarnings("unchecked")
                    List<Object> list = session.createQuery(hql.toString()).setInteger(projectId, getProjectId()).setInteger(productId, getId())
                            .setInteger(type, data.getClaimWorkDataType().getIntValue()).list();
                    if (list != null && list.size() > 0) {
                        Object rs = list.get(0);
                        historyId = rs == null ? 0 : Integer.parseInt(rs.toString());
                    }
                } catch (HibernateException e) {
                    e.printStackTrace();
                    historyId = -1;
                }
                historyId++;

                /* 問い合わせ中間データ登録 */
                WorkdatasBean workdatas = new WorkdatasBean();
                workdatas.setId(workdataId);
                workdatas.setProjectId(getProjectId());
                workdatas.setProductId(getId());
                workdatas.setInputTableId(data.getClaimId());
                workdatas.setType(data.getClaimWorkDataType().getIntValue());
                if (ClaimWorkDataType.CORRECTION_MISTAKES.equals(type)) {
                    // 以降はjdbc版では全てnull
                    workdatas.setHistoryId(null);
                    workdatas.setLink(null);
                    workdatas.setLasted(null);
                } else {
                    workdatas.setHistoryId(historyId);
                    workdatas.setLink(null);
                    workdatas.setLasted(null);
                }
                try {

                    WorkdatasBean readWorkdatas = (WorkdatasBean) session.get(WorkdatasBean.class, workdataId);
                    if (readWorkdatas == null) {
                        /* トランザクション開始 */
                        session.beginTransaction();

                        // 「INSERT IGNORE INTO WORKDATAS....」の機能置換
                        session.save(workdatas);
                        session.flush();
                        /* トランザクションコミット */
                        session.getTransaction().commit();
                    }

                } catch (HibernateException e) {
                    e.printStackTrace();
                } finally {
                    if (session.getTransaction().isActive()) {
                        session.getTransaction().rollback();
                    }
                }

                StringBuilder strHql = new StringBuilder(128).append("FROM WorkdatasBean WHERE projectId =:").append(projectId); //$NON-NLS-1$
                strHql.append(" AND productId = :").append(productId).append(" AND inputTableId = :").append(claimId); //$NON-NLS-1$ //$NON-NLS-2$
                strHql.append(" AND type= :").append(type); //$NON-NLS-1$
                try {
                    @SuppressWarnings("unchecked")
                    List<WorkdatasBean> list = session.createQuery(strHql.toString()).setInteger(projectId, getProjectId()).setInteger(productId, getId())
                            .setInteger(claimId, data.getClaimId()).setInteger(type, data.getClaimWorkDataType().getIntValue()).list();
                    if (list != null && list.size() > 0) {
                        workdataId = list.get(0).getId();
                    }
                } catch (HibernateException e) {
                    CoronaActivator.debugLog("Error HQL : " + strHql); //$NON-NLS-1$
                    e.printStackTrace();
                    workdataId = -1;
                }
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
        if (workdataId > 0) {
            ((ClaimWorkData) data).setWorkdataId(workdataId);
            _claimWorks.add(data);
            data.update();
            return true;
        }
        return false;
    }


    @Override
    public boolean removeClaimWorkData(IClaimWorkData work) {
        /* 誤記補正結果は、ワークテーブルにはないので処理しない */
        ClaimWorkDataType type = work.getClaimWorkDataType();
        if (!ClaimWorkDataType.CORRECTION_MISTAKES.equals(type)) {
            String tableName = CoronaIoUtils.createWorkTableName(((ClaimWorkData) work).getDbName(), TableType.WORK_DATA, work.getProjectId());
            int workId = work.getWorkdataId();
            int fieldId = work.getFieldId();
            if (deleteWorkData(tableName, workId, fieldId, type)) {
                String work_Id = "work_Id"; //$NON-NLS-1$
                String fld_Id = "fld_Id"; //$NON-NLS-1$
                String strHQL = "DELETE FROM DicPriorityBean WHERE primaryKey.id = :" + work_Id //$NON-NLS-1$
                        + " AND primaryKey.fieldId = :" + fld_Id; //$NON-NLS-1$
                Session session = IoService.getInstance().getSession();
                /* DicPriDao.deleteDicPri を置換する。 */
                try {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    session.createQuery(strHQL).setInteger(work_Id, workId).setInteger(fld_Id, fieldId).executeUpdate();
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    e.printStackTrace();
                    // TODO 20131118 jdbc版ではSQLExceptionが発生しても、継続させている。
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
                try {
                    StringBuilder checkSql = new StringBuilder(128);
                    checkSql.append("Select * From ").append(tableName).append(" Where work_id=").append(workId); //$NON-NLS-1$ //$NON-NLS-2$
                    /* !CommonDao.isMatch を置換する。「rs.next()の結果を返す」 */
                    @SuppressWarnings("unchecked")
                    List<Object[]> list = session.createSQLQuery(checkSql.toString()).list();
                    if (list == null || list.size() == 0) {
                        /*
                         * 今消そうとしているのと同じ処理タイプの結果がほかにもあったら
                         * 共通の辞書優先度も管理テーブルも残す。
                         * 処理結果レコードがない処理結果があるときにのみ、このコードがいきる。
                         */
                        int sameTypeCount = 0;
                        for (IClaimWorkData workData : _claimWorks) {
                            if (type.equals(workData.getClaimWorkDataType())) {
                                sameTypeCount++;
                                if (sameTypeCount >= 2) {
                                    break;
                                }
                            }
                        }
                        if (sameTypeCount == 1) {
                            /* 共通の辞書優先度も消す */
                            /* DicPriDao.deleteDicPri(workId, 0); を置換する。 */
                            try {
                                /* トランザクション開始 */
                                session.beginTransaction();

                                session.createQuery(strHQL).setInteger(work_Id, workId).setInteger(fld_Id, 0).executeUpdate();
                                session.flush();

                                /* トランザクションコミット */
                                session.getTransaction().commit();
                            } catch (HibernateException e) {
                                e.printStackTrace();
                                // TODO 20131118 jdbc版ではSQLExceptionが発生しても、継続させている。
                            } finally {
                                if (session.getTransaction().isActive()) {
                                    /* トランザクションロールバック */
                                    session.getTransaction().rollback();
                                }
                            }

                            /* ひとつも処理結果がなくなったら管理テーブルから消す */
                            /*
                             * ClaimWorkDataDao.deleteWorkFromManageTable(workId)
                             * ; を置換する。
                             */
                            WorkdatasBean delWorkdatas = (WorkdatasBean) session.get(WorkdatasBean.class, workId);
                            if (delWorkdatas != null) {
                                /* トランザクション開始 */
                                session.beginTransaction();

                                session.delete(delWorkdatas);
                                session.flush();

                                /* トランザクションコミット */
                                session.getTransaction().commit();
                            }
                        }
                    }
                } catch (HibernateException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
            } else {
                return false;
            }
        }

        _claimWorks.remove(work);
        return true;
    }


    @Override
    public List<IClaimData> getClaimDatas() {
        List<IClaimData> ret = new ArrayList<IClaimData>();
        String sql = null;
        {
            StringBuilder sqlB = new StringBuilder(128);
            sqlB.append("Select tbl_id From rel_clm_product Where prj_id=").append(getProjectId()).append(" And product_id=").append(getId()); //$NON-NLS-1$ //$NON-NLS-2$
            sql = sqlB.toString();
        }
        try {
            final List<Integer> claimIds = new ArrayList<Integer>();

            try {
                Session session = IoService.getInstance().getSession();
                @SuppressWarnings("unchecked")
                List<Object> list = session.createSQLQuery(sql).list();
                for (Object rs : list) {
                    claimIds.add(rs == null ? 0 : Integer.parseInt(rs.toString()));
                }
            } catch (HibernateException e) {
                throw e;
            }

            ICoronaProject project = IoService.getInstance().getProject(this.getProjectId());
            if (project != null) {
                for (IClaimData claim : project.getClaimDatas()) {
                    if (claimIds.contains(claim.getId())) {
                        ret.add(claim);
                    }
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return ret;
    }


    @Override
    public Set<Integer> getMiningFields(int claimId) {
        if (!bInitRecords) {
            updateRecords();
        }
        Set<Integer> ret = _miningTargets.get(claimId);
        if (ret == null) {
            ret = new HashSet<Integer>();
            _miningTargets.put(claimId, ret);
        }
        return ret;
    }


    @Override
    public void addMiningField(int claimId, int fieldId) {
        if (!bInitRecords) {
            updateRecords();
        }
        getMiningFields(claimId).add(fieldId);

        /* クレーム-ターゲットリレーションを追加 */
        insertRelationClaimData(getProjectId(), getId(), claimId, 0, 0, _miningTargets.get(claimId));

        /* 誤記補正を割り当て */
        ClaimWorkData cm = (ClaimWorkData) ((ClaimWorkData) IoService.getInstance().getClaimData(claimId).getCorrectionMistakes(fieldId)).clone();
        cm.setProjectId(getProjectId());
        cm.setProductId(getId());
        _claimWorks.add(cm);
    }


    @Override
    public void removeMiningFeild(int claimId, int fieldId) {
        if (!bInitRecords) {
            updateRecords();
        }
        _miningTargets.get(claimId).remove(fieldId);
        /* クレーム-ターゲットのリレーションをDBに反映 */
        insertRelationClaimData(getProjectId(), getId(), claimId, 0, 0, _miningTargets.get(claimId));
    }


    @Override
    public boolean update() {

        try {

            /* SQL編集 */
            String SQLexe = null;
            {
                /*
                 * ClaimWorkDataDao.getClaimWorkInfomations(getProjectId(),
                 * getId()); を置換する。
                 */
                StringBuilder strSQL = new StringBuilder(260);
                strSQL.append("SELECT i.PRJ_ID,i.PRODUCT_ID,i.TBL_ID,i.WORK_TBL_ID,i.REL_TBL_ID,i.TGT_FLDS,w.TYPE,w.LASTED "); //$NON-NLS-1$
                strSQL.append("FROM REL_CLM_PRODUCT AS i LEFT JOIN WORKDATAS AS w ON w.INPUT_TABLE_ID=i.TBL_ID AND "); //$NON-NLS-1$
                strSQL.append("((w.PROJECT_ID=i.PRJ_ID AND w.PRODUCT_ID=i.PRODUCT_ID) OR (w.PROJECT_ID=0 AND w.PRODUCT_ID=0))"); //$NON-NLS-1$
                strSQL.append("WHERE i.PRJ_ID=").append(getProjectId()).append(" AND i.PRODUCT_ID=").append(getId()); //$NON-NLS-1$ //$NON-NLS-2$
                SQLexe = strSQL.toString();
            }

            Session session = IoService.getInstance().getSession();

            /* SQL実行 */
            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(SQLexe).list();

            if (list != null) {
                /* SELECT結果編集 */
                for (Object[] rs : list) {
                    int claimId = rs[2] == null ? 0 : Integer.parseInt(rs[2].toString()); //(int) rs[2];//rs.getInt("i.TBL_ID");

                    ClaimWorkDataType type = ClaimWorkDataType.valueOf(rs[6] == null ? 0 : Integer.parseInt(CoronaIoUtils.convertToString(rs[6]))); //(int) rs[6] //rs.getInt("w.TYPE")
                    _miningTargets.put(claimId, CoronaIoUtils.stringToIntSet(CoronaIoUtils.convertToString(rs[5]))); //rs.getString("i.TGT_FLDS")
                    /*
                     * 中間データ構築
                     * マイニング対象フィールドに対して、typeのクレンジング実行をしていれば追加する
                     */
                    for (Integer fieldId : _miningTargets.get(claimId)) {
                        switch (type) {
                        case CORRECTION_MISTAKES:
                            /* 誤記補正の場合は、クローン設定し、プロジェクト、プロダクトを設定 */
                            ClaimWorkData cm = (ClaimWorkData) ((ClaimWorkData) IoService.getInstance().getClaimData(claimId).getCorrectionMistakes(fieldId))
                                    .clone();
                            cm.setProjectId(getProjectId());
                            cm.setProductId(getId());
                            if (_claimWorks.add(cm)) {
                                /*
                                 * TODO 20131203
                                 * ここでのコメントは、既存のmysql版から記述されているので、残している。
                                 */

                                // /* TODO コピー処理がここにあると、プロジェクトツリーを展開するだけで時間がかかる。 */
                                // レコード取得の前にできないかな。
                                // //List<ITextRecord> fixRecs = new
                                // ArrayList<ITextRecord>();
                                // List<ITextRecord> tmpRecs = new
                                // ArrayList<ITextRecord>(cm.getClaimWorkDatas());
                                // cm.getClaimWorkDatas().clear();
                                // /* オリジナルの問合せデータレコードを取得 */
                                // for (IRecord rec : claim.getRecords()) {
                                // Object prodVal =
                                // rec.getField(prodField).getValue();
                                // if (prodVal.equals(getName())) {
                                // Integer id = Integer.parseInt((String)
                                // rec.getField(1).getValue());
                                // for (Iterator<ITextRecord> itr =
                                // tmpRecs.iterator(); itr.hasNext();) {
                                // ITextRecord tRec = itr.next();
                                // if (tRec.getId() == id) {
                                // cm.addClaimWorkData(id, tRec.getText());
                                // itr.remove();
                                // break;
                                // }
                                // }
                                // }
                                // }
                                // //cm.getClaimWorkDatas().addAll(fixRecs);
                            }
                            break;
                        case RESLUT_PATTERN:
                            IClaimWorkPattern workPattern = new ClaimWorkPattern(claimId, fieldId, type, getProjectId(), getId(), true);
                            if (isExecuted(workPattern)) {
                                _claimWorks.add(workPattern);
                            }
                            break;
                        case FREQUENTLY_APPERING:
                            IClaimWorkFAData workFreq = new ClaimWorkFAData(claimId, fieldId, getProjectId(), getId());
                            if (isExecuted(workFreq)) {
                                _claimWorks.add(workFreq);
                            }
                            break;
                        default:
                            IClaimWorkData work = new ClaimWorkData(claimId, fieldId, type, getProjectId(), getId());
                            if (isExecuted(work)) {
                                _claimWorks.add(work);
                            }
                            break;
                        }
                    }

                }
            }

            /* 処理結果を取り直して、中身は更新されていないのでfalseにする */
            bInitRecords = false;

            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 渡された中間データの処理結果があるかどうか判断する。
     * 
     * @param work
     * @return
     */
    private boolean isExecuted(IClaimWorkData work) {
        if (ClaimWorkDataType.CORRECTION_MISTAKES.equals(work.getClaimWorkDataType())) {
            return true;
        }
        /* workId, dbNameが入ってないため、一度updateする */
        work.update();

        String tableName = CoronaIoUtils.createWorkTableName(((ClaimWorkData) work).getDbName(), TableType.WORK_DATA, getProjectId());
        StringBuilder checkSql = new StringBuilder(128);
        checkSql.append("Select * From ").append(tableName).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
        checkSql.append("Where work_id=").append(work.getWorkdataId()).append(" And "); //$NON-NLS-1$ //$NON-NLS-2$
        checkSql.append("fld_id=").append(work.getFieldId()).append(" limit 1"); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            Session session = IoService.getInstance().getSession();
            /* CommonDao.isMatch を置換する。「rs.next()の結果を返す」 */
            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(checkSql.toString()).list();
            if (list != null && list.size() > 0) {
                return true;
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean commit(IProgressMonitor monitor) {
        return commit(true, monitor);
    }


    @Override
    public boolean commit(boolean bRecords, IProgressMonitor monitor) {

        ICoronaProduct product = null;
        try {

            /* プロダクトの存在チェック */
            String HQLexe = null;
            String projectId = "project_Id"; //$NON-NLS-1$
            {
                /* ProductDao.getPrjProductInfo(this.getId()); を置換する。 */
                StringBuilder strHQL = new StringBuilder(192);
                strHQL.append("FROM ProductBean a, RelPrjProductBean b "); //$NON-NLS-1$
                strHQL.append("WHERE a.productId = b.productId AND "); //$NON-NLS-1$
                strHQL.append("b.projectId = :").append(projectId).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
                HQLexe = strHQL.toString();

            }
            /* HQL実行 */
            Session session = IoService.getInstance().getSession();

            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createQuery(HQLexe).setInteger(projectId, this.getId()).list();
            if (list != null) {
                for (Object[] rs : list) {
                    ProductBean productBean = (ProductBean) rs[0];
                    RelPrjProductBean rel = (RelPrjProductBean) rs[1];
                    product = new Product(productBean.getProductName(), rel.getPrimaryKey().getProjectId());
                    setId(productBean.getProductId());
                }
            }

            /* プロダクトが存在しなかった場合、エラー */
            if (product == null) {
                throw new HibernateException(Messages.Product_errorNotProduct);
            }

            if (!product.getName().equals(this.getName())) {
                /* プロダクト名が相違する場合、プロダクト名を更新 */
                try {
                    ProductBean productBean = (ProductBean) session.get(ProductBean.class, this.getId());
                    if (productBean != null) {
                        productBean.setProductName(this.getName());

                        /* トランザクション開始 */
                        session.beginTransaction();

                        session.save(productBean);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();
                    } else {
                        throw new HibernateException(Messages.Product_errorModifyProductName);
                    }
                } catch (HibernateException e) {
                    e.printStackTrace();
                    throw new HibernateException(Messages.Product_errorModifyProductName);
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
            }

            return true;

        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * レコード更新
     * 
     * @return 更新完了でTrueが返る
     */
    public boolean updateRecords() {
        update();
        for (IClaimWorkData work : _claimWorks) {
            work.update();
        }
        bInitRecords = true;
        return true;
    }


    /**
     * 
     * 辞書エクスポート
     * 
     * @param outputPath
     *            出力先パス
     * @param encoding
     *            文字コード
     * @return true/false
     * 
     */
    @Override
    public boolean exportDictionarys(String outputPath, String encoding) {
        boolean exported = false;

        List<ICoronaDic> expDic = new ArrayList<ICoronaDic>();

        /* プロジェクトに紐づく辞書を取得 */
        List<ICoronaDic> dicList = getDictionarys(IUserDic.class);
        for (ICoronaDic dic : dicList) {
            IUserDic udic = (IUserDic) dic;
            if (udic.getDicType().getIntValue() != DicType.JUMAN.getIntValue()) {
                /* 作成チェックフラグを初期化 */
                ((UserDic) dic).setExpFlg(true);
                /* ユーザー辞書をターゲットとして保持 */
                expDic.add(dic);
            }
        }

        /* パス先の辞書フォルダから、dicを取得 */
        File dir = new File(outputPath);
        File[] files = dir.listFiles(getFileExtensionFilter(Messages.Product_extensionDic));

        for (File file : files) {
            boolean existFlg = false;
            for (ICoronaDic dic : expDic) {
                String fileName = ((IUserDic) dic).getFileName();
                if (fileName.equals(file.getName())) {
                    /* 一致するファイルが存在 */
                    existFlg = true;
                    Date last = dic.getLasted();
                    Date fileDate = new Date(file.lastModified());

                    /* 日付比較 */
                    if (last.compareTo(fileDate) < 0) {
                        /* DBの日付が小さい場合、エクスポート対象からはずす */
                        ((UserDic) dic).setExpFlg(false);
                    }
                    break;
                }
            }

            /* DBに一致するファイル名がなかった場合、フォルダよりファイルを削除 */
            if (existFlg == false) {
                // System.out.println("ファイル削除：" + file.getName());
                /* TODO:α版ではファイル削除しないため、コメント */
                // file.delete();
            }
        }

        /* ファイルのエクスポート */
        for (ICoronaDic dic : expDic) {
            if (((UserDic) dic).isExpFlg() == true) {
                dic.exportDic(outputPath, encoding);
                exported = true;
            }
        }
        return exported;
    }


    /**
     * ファイル情報取得
     * 
     * @param extension
     *            拡張子
     * @return 取得できたらTrue、できなかったらFalse
     */
    public static FilenameFilter getFileExtensionFilter(String extension) {
        final String _extension = extension;
        return new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                boolean ret = name.endsWith(_extension);
                return ret;
            }
        };
    }


    @Override
    public boolean getRelation(int projectId, int dicId) {
        int id = 0;
        StringBuilder strSQL = new StringBuilder(96);
        strSQL.append("SELECT PROJECT_ID FROM REL_PRJ_DIC WHERE PROJECT_ID = ") //$NON-NLS-1$
                .append(projectId).append(" AND DIC_ID = ").append(dicId); //$NON-NLS-1$
        try {
            Session session = IoService.getInstance().getSession();
            @SuppressWarnings("unchecked")
            List<Object> list = session.createSQLQuery(strSQL.toString()).list();
            if (list != null && list.size() > 0) {
                Object rs = list.get(0);
                int para0 = rs == null ? 0 : Integer.parseInt(rs.toString());
                id = para0;
            }
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strSQL.toString()); //$NON-NLS-1$
            e.printStackTrace();
            // TODO 20131119 本来のgetFirstIntでは-1だが、ここではメソッドの返り値をfalseとする為の処置を実施する。
            //id = -1;
            id = 0;
        }
        if (id == 0) {
            return false;
        }
        return true;
    }


    @Override
    public boolean delDicPriority(String strIds, int dicId) {
        /* return DicPriDao.deleteDicPriority(strIds, dicId); を置換する */
        boolean ret = true;
        Session session = IoService.getInstance().getSession();
        try {
            String strSQL = "DELETE FROM DIC_PRI WHERE DIC_ID = " + dicId + " AND ID IN (" + strIds + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            /* トランザクション開始 */
            session.beginTransaction();

            session.createSQLQuery(strSQL).executeUpdate();
            session.flush();

            /* トランザクションコミット */
            session.getTransaction().commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            ret = false;
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
        return ret;
    }


    @Override
    public Map<Integer, String> getWorkData(int productId) {

        Map<Integer, String> wkMap = new HashMap<Integer, String>();
        try {

            /* SQL編集 */
            String strSQL = null;
            /* ClaimWorkDataDao.getWorkDataPrj(productId); を置換する。 */
            strSQL = "SELECT wk.ID, wk.PROJECT_ID, wk.TYPE FROM WORKDATAS wk, REL_PRJ_PRODUCT rel WHERE rel.PRODUCT_ID = " + productId //$NON-NLS-1$
                    + " AND wk.PROJECT_ID = rel.PROJECT_ID AND wk.PRODUCT_ID = rel.PRODUCT_ID ORDER BY PROJECT_ID, ID"; //$NON-NLS-1$

            Session session = IoService.getInstance().getSession();

            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(strSQL).list();

            /* SQL実行 */
            if (list != null) {
                /* SELECT結果編集 */
                int chkId = 0;
                String strIds = ""; //$NON-NLS-1$
                String strTypes = ""; //$NON-NLS-1$
                for (Object[] rs : list) {
                    /* マップ使ってプロジェクト毎のIDを集約 */
                    int prdId = rs[1] == null ? 0 : Integer.parseInt(rs[1].toString()); //(int) rs[1]; //rs.getInt(Messages.Product_projectId);
                    int para0 = rs[0] == null ? 0 : Integer.parseInt(rs[0].toString());
                    int para2 = rs[2] == null ? 0 : Integer.parseInt(rs[2].toString());
                    if (chkId == 0) {
                        strIds = String.valueOf(para0);
                        strTypes = String.valueOf(para2);
                        chkId = prdId;
                    } else if (chkId == prdId) {
                        strIds = strIds + "," + String.valueOf(para0); //$NON-NLS-1$
                        strTypes = strTypes + "," + String.valueOf(para2); //$NON-NLS-1$
                    } else {
                        wkMap.put(chkId, strIds + "/" + strTypes); //$NON-NLS-1$
                        strIds = ""; //$NON-NLS-1$
                        strTypes = ""; //$NON-NLS-1$
                        strIds = String.valueOf(para0);
                        strTypes = String.valueOf(para2);
                        chkId = prdId;
                    }
                }
                wkMap.put(chkId, strIds + "/" + strTypes); //$NON-NLS-1$
            }
            return wkMap;

        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean getDicPriority(int id, int dicId) {
        /* プライオリティテーブルにIDに対応する辞書が存在するかチェック */
        int count = 0;
        String strSQL = null;
        try {
            /*
             * CommonDao.getFirstInt(DicPriDao.getDicPriCount(id, dicId));
             * を置換する。
             */
            strSQL = "SELECT COUNT(*) FROM DIC_PRI WHERE ID = " + id + " AND DIC_ID = " + dicId; //$NON-NLS-1$ //$NON-NLS-2$
            Session session = IoService.getInstance().getSession();
            count = ((BigInteger) session.createSQLQuery(strSQL).uniqueResult()).intValue();
        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strSQL); //$NON-NLS-1$
            e.printStackTrace();
            // TODO 20131119 本来のgetFirstIntでは-1だが、ここではメソッドの返り値をfalseとする為の処置を実施する。
            //count = -1;
            count = 0;
        }

        if (count == 0) {
            return false;
        }
        return true;
    }


    @Override
    public void addDicPriority(int id, int dicId) {

        try {
            /* SQL編集 */
            /* DicPriDao.getFields(id); を置換する。 */
            String strSQL = "SELECT FLD_ID FROM DIC_PRI WHERE ID = " //$NON-NLS-1$
                    + id + " GROUP BY FLD_ID ORDER BY FLD_ID"; //$NON-NLS-1$
            List<Integer> fldList = new ArrayList<Integer>();
            /* SQL実行 */
            Session session = IoService.getInstance().getSession();
            @SuppressWarnings("unchecked")
            List<Object> list = session.createSQLQuery(strSQL).list();

            if (list != null) {
                for (Object rs : list) {
                    fldList.add(rs == null ? 0 : Integer.parseInt(rs.toString())); //rs.getInt(Messages.Product_fldId)
                }
            }

            /* 取得したフィールドIDと引数の値をもとにプライオリティを追加 */
            for (int fldId : fldList) {
                insertDicPri(id, fldId, dicId, -1, true, true);
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }


    /**
     * 問い合わせ中間テーブル生成
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
     * @throws HibernateException
     */
    private static int createClaimWorkDataTable(String name, String dbName, TableType type, int projectId, int productId, int claimId, Set<Integer> tgts)
            throws HibernateException {

        /* ClaimWorkDataDaoより移設 */

        StringBuilder strSQL = new StringBuilder(192).append("CREATE TABLE "); //$NON-NLS-1$
        if (dbName == null) {
            throw new HibernateException("Not find dbname"); //$NON-NLS-1$
        }
        /* テーブルを作成 */
        String strWork = CoronaIoUtils.createWorkTableName(dbName, type, projectId);
        Session session = IoService.getInstance().getSession();

        int workId = 0;
        try {
            /* TablesDao.getTableId(strWork); を置換する。 */
            @SuppressWarnings("unchecked")
            List<TablesBean> list = session.createQuery("FROM TablesBean WHERE dbname = :dbName") //$NON-NLS-1$
                    .setString("dbName", strWork) //$NON-NLS-1$
                    .list();
            if (list != null && list.size() > 0) {
                workId = list.get(0).getId();
            }
        } catch (HibernateException e) {
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
                session.beginTransaction();

                /* CommonDao.executeSQL4Throws(strSQL.toString()); を置換する。 */
                session.createSQLQuery(strSQL.toString()).executeUpdate();
                session.flush();

                /* トランザクションコミット */
                session.getTransaction().commit();

            } catch (HibernateException e) {
                CoronaActivator.debugLog("Error SQL : " + strSQL.toString()); //$NON-NLS-1$
                throw e;
            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }
            /* テーブルリストに登録 */
            insertTable(name, strWork, type);
            workId = 0;
            try {
                /* TablesDao.getTableId(strWork); を置換する。 */
                @SuppressWarnings("unchecked")
                List<TablesBean> list = session.createQuery("FROM TablesBean WHERE dbname = :dbName") //$NON-NLS-1$
                        .setString("dbName", strWork) //$NON-NLS-1$
                        .list();
                if (list != null && list.size() > 0) {
                    workId = list.get(0).getId();
                }
            } catch (HibernateException e) {
                throw e;
            }

            if (workId == 0) {
                throw new HibernateException("not find workId"); //$NON-NLS-1$
            }

            // 検索処理速度改善対策でUSR_CLAIM＿xxxにIndexを登録
            if (!TableType.CORRECTION_MISTAKES_DATA.equals(type)) {
                StringBuilder strCreateIndex = new StringBuilder("CREATE INDEX "); //$NON-NLS-1$
                strCreateIndex.append(strWork).append("_INDEX ON ").append(strWork).append("(WORK_ID, HISTORY_ID, REC_ID ASC);"); //$NON-NLS-1$ //$NON-NLS-2$

                try {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    /*
                     * CommonDao.executeSQL4Throws(strCreateIndex.toString());
                     * を置換する。
                     */
                    session.createSQLQuery(strCreateIndex.toString()).executeUpdate();
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    CoronaActivator.debugLog("Error SQL : " + strCreateIndex.toString()); //$NON-NLS-1$
                    throw e;
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
            }
        }

        if (type == TableType.WORK_DATA) {
            /* TODO:パターンリレーションテーブルを作成 */
            String strRelPtn = CoronaIoUtils.createWorkTableName(dbName, TableType.RESULT_DATA, projectId);
            int relId = 0;
            try {
                /* TablesDao.getTableId(strRelPtn); を置換する。 */
                @SuppressWarnings("unchecked")
                List<TablesBean> list = session.createQuery("FROM TablesBean WHERE dbname = :dbName") //$NON-NLS-1$
                        .setString("dbName", strRelPtn) //$NON-NLS-1$
                        .list();
                if (list != null && list.size() > 0) {
                    relId = list.get(0).getId();
                }
            } catch (HibernateException e) {
                throw e;
            }

            if (relId == 0) {
                StringBuilder createSql = new StringBuilder(128);
                createSql.append("Create Table ").append(strRelPtn); //$NON-NLS-1$
                createSql.append("(work_id Int Not NULL,").append("fld_id Int Not NULL,"); //$NON-NLS-1$ //$NON-NLS-2$
                createSql.append("history Int Not NULL,").append("rec_id Int Not NULL,"); //$NON-NLS-1$ //$NON-NLS-2$
                createSql.append("pattern_id Int Not NULL,").append("hit_info MediumText Not NULL)"); //$NON-NLS-1$ //$NON-NLS-2$

                try {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    /*
                     * CommonDao.executeSQL4Throws(createSql.toString()););
                     * を置換する。
                     */
                    session.createSQLQuery(createSql.toString()).executeUpdate();
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    CoronaActivator.debugLog("Error SQL : " + createSql.toString()); //$NON-NLS-1$
                    throw e;
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }

                // テーブルリストに登録
                insertTable(TableType.RESULT_DATA.toString(), strRelPtn, TableType.RESULT_DATA);
                relId = 0;
                try {
                    /* TablesDao.getTableId(strRelPtn); を置換する。 */
                    @SuppressWarnings("unchecked")
                    List<TablesBean> list = session.createQuery("FROM TablesBean WHERE dbname = :dbName") //$NON-NLS-1$
                            .setString("dbName", strRelPtn) //$NON-NLS-1$
                            .list();
                    if (list != null && list.size() > 0) {
                        relId = list.get(0).getId();
                    }
                } catch (HibernateException e) {
                    throw e;
                }

                if (relId == 0) {
                    throw new HibernateException("not find workId"); //$NON-NLS-1$
                }

                StringBuffer strCreateIndex = new StringBuffer().append("CREATE INDEX "); //$NON-NLS-1$
                strCreateIndex.append(strRelPtn).append("_INDEX ON ").append(strRelPtn).append("(WORK_ID, HISTORY, REC_ID ASC);"); //$NON-NLS-1$ //$NON-NLS-2$
                try {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    /*
                     * CommonDao.executeSQL4Throws(strCreateIndex.toString());
                     * を置換する。
                     */
                    session.createSQLQuery(strCreateIndex.toString()).executeUpdate();
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    CoronaActivator.debugLog("Error SQL : " + strCreateIndex.toString()); //$NON-NLS-1$
                    throw e;
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
            }

            /* クレーム-ターゲットリレーションを追加 */
            insertRelationClaimData(projectId, productId, claimId, workId, relId, tgts);

        }

        /* テーブルIDを取得 */
        //        return TablesDao.getTableId(strWork);
        workId = 0;
        try {
            /* TablesDao.getTableId(strWork); を置換する。 */
            @SuppressWarnings("unchecked")
            List<TablesBean> list = session.createQuery("FROM TablesBean WHERE dbname = :dbName") //$NON-NLS-1$
                    .setString("dbName", strWork) //$NON-NLS-1$
                    .list();
            if (list != null && list.size() > 0) {
                workId = list.get(0).getId();
            }
        } catch (HibernateException e) {
            throw e;
        }
        return workId;
    }


    /**
     * 指定した処理結果のレコードを削除する。typeが誤記補正結果なら何もしない(戻り値はtrue)。
     * 
     * @param dbName
     *            usr_work_***形式のテーブル名
     * @param workId
     * @param fieldId
     * @param type
     *            {@link ClaimWorkDataType#CORRECTION_MISTAKES}以外の値
     * @return
     */
    private static boolean deleteWorkData(String dbName, int workId, int fieldId, ClaimWorkDataType type) {

        /* ProductDaoから移設 */

        if (!ClaimWorkDataType.CORRECTION_MISTAKES.equals(type)) {
            StringBuilder sql = new StringBuilder(64);
            sql.append("Delete From ").append(dbName).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
            sql.append("Where work_id=").append(workId).append(" And "); //$NON-NLS-1$ //$NON-NLS-2$
            sql.append("fld_id=").append(fieldId); //$NON-NLS-1$

            Session session = IoService.getInstance().getSession();
            try {
                /* トランザクション開始 */
                session.beginTransaction();

                session.createSQLQuery(sql.toString()).executeUpdate();
                session.flush();

                /* トランザクションコミット */
                session.getTransaction().commit();

            } catch (HibernateException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }
        }
        return true;
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
    private static boolean insertRelationClaimData(int projectId, int productId, int claimId, int workId, int relId, Collection<Integer> tgts) {

        /* ProductDaoから移設 */

        String strTgts = CoronaIoUtils.intListToString(tgts);
        StringBuilder strSQL = new StringBuilder(1024);

        strSQL.append("MERGE INTO REL_CLM_PRODUCT (PRJ_ID, PRODUCT_ID, TBL_ID, "); //$NON-NLS-1$
        if (workId != 0) {
            strSQL.append("WORK_TBL_ID, "); //$NON-NLS-1$
        }
        if (relId != 0) {
            strSQL.append("REL_TBL_ID, "); //$NON-NLS-1$
        }
        strSQL.append("TGT_FLDS) "); //$NON-NLS-1$
        strSQL.append(" KEY (PRJ_ID, PRODUCT_ID, TBL_ID) "); //$NON-NLS-1$
        strSQL.append("VALUES(").append(projectId).append(",").append(productId).append(",").append(claimId).append(","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        if (workId != 0) {
            strSQL.append(workId).append(","); //$NON-NLS-1$
        }
        if (relId != 0) {
            strSQL.append(relId).append(","); //$NON-NLS-1$
        }
        strSQL.append("'").append(strTgts).append("')"); //$NON-NLS-1$ //$NON-NLS-2$

        //        strSQL.append("INSERT INTO REL_CLM_PRODUCT (PRJ_ID, PRODUCT_ID, TBL_ID, "); //$NON-NLS-1$
        //        if (workId != 0) {
        //            strSQL.append("WORK_TBL_ID, "); //$NON-NLS-1$
        //        }
        //        if (relId != 0) {
        //            strSQL.append("REL_TBL_ID, "); //$NON-NLS-1$
        //        }
        //        strSQL.append("TGT_FLDS) VALUES"); //$NON-NLS-1$
        //        strSQL.append("(").append(projectId).append(",").append(productId).append(",").append(claimId).append(","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        //        if (workId != 0) {
        //            strSQL.append(workId).append(","); //$NON-NLS-1$
        //        }
        //        if (relId != 0) {
        //            strSQL.append(relId).append(","); //$NON-NLS-1$
        //        }
        //        strSQL.append("'").append(strTgts).append("')"); //$NON-NLS-1$ //$NON-NLS-2$
        //        strSQL.append(" ON DUPLICATE KEY UPDATE TGT_FLDS=VALUES(TGT_FLDS)"); //$NON-NLS-1$
        //        if (workId != 0) {
        //            strSQL.append(",WORK_TBL_ID=VALUES(WORK_TBL_ID)"); //$NON-NLS-1$
        //        }
        //        if (relId != 0) {
        //            strSQL.append(",REL_TBL_ID=VALUES(REL_TBL_ID)"); //$NON-NLS-1$
        //        }

        Session session = IoService.getInstance().getSession();
        try {
            if (!session.getTransaction().isActive()) {
                /* トランザクション開始 */
                session.beginTransaction();
            }
            session.createSQLQuery(strSQL.toString()).executeUpdate();
            session.flush();

            /* トランザクションコミット */
            session.getTransaction().commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
        return true;
    }


    /**
     * @param workId
     * @param fldId
     * @param dicId
     * @param priority
     *            ０以下なら、優先度最低とする
     * @param inactive
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
        String sep = ","; //$NON-NLS-1$
        StringBuilder sql = new StringBuilder(512);
        sql.append("MERGE INTO DIC_PRI (ID, FLD_ID, DIC_ID, PRIORITY, INACTIVE) KEY (ID, FLD_ID, DIC_ID) VALUES ("); //$NON-NLS-1$
        sql.append(workId).append(sep).append(fldId).append(sep).append(dicId).append(sep).append(priority);
        sql.append(sep).append(inactive).append(") "); //$NON-NLS-1$
        String strSQL = sql.toString();
        //        String strSQL = "INSERT INTO DIC_PRI (ID, FLD_ID, DIC_ID, PRIORITY, INACTIVE) VALUES (" + workId + ", " + fldId + "," + dicId + "," + priority + "," //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        //                + inactive + ") ON DUPLICATE KEY UPDATE PRIORITY =VALUES(PRIORITY), INACTIVE =VALUES(INACTIVE)"; //$NON-NLS-1$
        try {
            /* トランザクション開始 */
            session.beginTransaction();

            /* CommonDao.executeSQL(strSQL); を置換する。 */
            session.createSQLQuery(strSQL).executeUpdate();
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
            BigInteger res = BigInteger.valueOf((Integer) session.createSQLQuery(sql.toString()).uniqueResult());
            min = res.intValue();
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


    /**
     * @param tableName
     * @param dbName
     * @param claimData
     * @return
     * @throws HibernateException
     */
    private static boolean insertTable(String tableName, String dbName, TableType type) throws HibernateException {

        // TODO 20131205 ここでは既にトランザクションが確立されている。エラーの場合、throwされた先でロールバックする。

        Session session = IoService.getInstance().getSession();

        // 「INSERT IGNORE INTO WORKDATAS....」の機能置換
        StringBuilder strCheckHql = new StringBuilder(128).append("select count(*) from TablesBean where (name = :NAME AND dbname = :DBNAME AND type = :TYPE)"); //$NON-NLS-1$
        try {
            Object rs = session.createQuery(strCheckHql.toString()).setString("NAME", tableName).setString("DBNAME", dbName) //$NON-NLS-1$ //$NON-NLS-2$
                    .setInteger("TYPE", type.getIntValue()).uniqueResult(); //$NON-NLS-1$
            if (rs != null && 0 != Integer.parseInt(rs.toString())) {
                return true;
            }

        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strCheckHql.toString()); //$NON-NLS-1$
            e.printStackTrace();
            throw e;
        }

        StringBuilder strSql = new StringBuilder(128).append("INSERT INTO TABLES (NAME, DBNAME, TYPE, LASTED) VALUES('"); //$NON-NLS-1$
        strSql.append(tableName).append("','").append(dbName).append("',").append(type.getIntValue()).append(",now())"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        /* return CommonDao.executeSQL4Throws(strSql.toString()); を置換する。 */
        try {
            /* トランザクション開始 */
            session.beginTransaction();

            session.createSQLQuery(strSql.toString()).executeUpdate();
            session.flush();

            /* トランザクションコミット */
            session.getTransaction().commit();

        } catch (HibernateException e) {
            CoronaActivator.debugLog("Error SQL : " + strSql.toString()); //$NON-NLS-1$
            e.printStackTrace();
            throw e;
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
        return true;
    }
}
