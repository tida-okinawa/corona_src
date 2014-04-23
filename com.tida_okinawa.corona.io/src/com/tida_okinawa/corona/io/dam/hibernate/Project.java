/**
 * @version $Id: Project.java 997 2013-06-05 01:58:11Z yukihiro-kinjyo $
 *
 * 2011/08/08 0:17:44
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.bean.ProductBean;
import com.tida_okinawa.corona.io.bean.ProjectBean;
import com.tida_okinawa.corona.io.bean.RelClmProductBean;
import com.tida_okinawa.corona.io.bean.RelPrjClmBean;
import com.tida_okinawa.corona.io.bean.RelPrjClmPKBean;
import com.tida_okinawa.corona.io.bean.RelPrjDicBean;
import com.tida_okinawa.corona.io.bean.RelPrjDicPKBean;
import com.tida_okinawa.corona.io.bean.RelPrjProductBean;
import com.tida_okinawa.corona.io.bean.RelPrjProductPKBean;
import com.tida_okinawa.corona.io.bean.RelProductDicBean;
import com.tida_okinawa.corona.io.bean.RelProductDicPKBean;
import com.tida_okinawa.corona.io.bean.TablesBean;
import com.tida_okinawa.corona.io.dam.hibernate.hql.CommonCreateQuery;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.abstraction.AbstractProject;
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
 * @author yukihiro-kinjyo
 */
public final class Project extends AbstractProject {

    /**
     * コンストラクター
     * 
     * @param name
     *            プロジェクト名
     */
    public Project(String name) {
        super(name);
    }


    /**
     * コンストラクタ
     * 
     * @param name
     * @param id
     */
    public Project(String name, int id) {
        super(name, id);
    }


    /**
     * ターゲット情報取得
     * 
     * @param なし
     * @return List<ICoronaProject>
     */
    @Override
    protected Map<Integer, ICoronaProduct> getProductsMap() {
        /* メモリ上に存在する場合、そのまま返す */
        if (_products.size() > 0) {
            return _products;
        }
        return updateProducts();
    }


    /**
     * ターゲット情報更新
     * 
     * @return
     */
    private Map<Integer, ICoronaProduct> updateProducts() {

        /* コネクション確立 */
        try {
            Session session = IoService.getInstance().getSession();

            /* SQL実行 */
            String strSQL = getPrjProductInfo(this.getId());
            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(strSQL).list();
            if (list != null) {
                /* 取得結果編集 */
                for (Object[] rs : list) {
                    int para0 = rs[0] == null ? 0 : Integer.parseInt(rs[0].toString());
                    ICoronaProduct product = null;
                    for (Entry<Integer, ICoronaProduct> entry : _products.entrySet()) {
                        if (entry.getKey() == para0) {
                            product = entry.getValue();
                            ((Product) product).setName((String) rs[1]);
                            break;
                        }
                    }
                    if (product == null) {
                        product = new Product((String) rs[1], this.getId(), para0);
                    }
                    _products.put(para0, product);
                }
            }

        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return _products;
    }


    @Override
    protected boolean addProductDam(ICoronaProduct product) {
        int projectId = 0;
        int productId = 0;

        Session session = IoService.getInstance().getSession();
        try {

            /* ターゲット情報登録済チェック */
            productId = getProductId(product.getName());
            if (productId == -1) {
                throw new HibernateException("システムエラーが発生しました。(ターゲット情報取得)"); //$NON-NLS-1$
            } else if (productId == 0) {
                /* ターゲットが未登録の場合、ターゲットを登録 */
                productId = insertProduct(product.getName());
            }
            /* 紐づけチェック */
            projectId = 0;
            try {
                Query query = CommonCreateQuery.getRelPrjProductQuery(product.getProjectId(), productId);
                RelPrjProductBean rel = (RelPrjProductBean) query.uniqueResult();
                if (rel != null) {
                    projectId = rel.getPrimaryKey().getProjectId();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                projectId = -1;
            }

            if (projectId == -1) {
                throw new HibernateException("システムエラーが発生しました。(プロジェクト-ターゲットリレーション存在チェック)"); //$NON-NLS-1$
            } else if (projectId != 0) {
                System.out.println("すでにプロジェクト登録されているターゲットです。"); //$NON-NLS-1$
                return true;
            }
            /* プロジェクトとターゲットを紐づける */
            /*
             * RelPrjProductDao.insertRelPrjProduct(product.getProjectId(),
             * productId)を置換する。
             */
            try {
                /* トランザクション開始 */
                session.beginTransaction();

                RelPrjProductBean rel = new RelPrjProductBean();
                RelPrjProductPKBean pk = new RelPrjProductPKBean();
                pk.setProjectId(product.getProjectId());
                pk.setProductId(productId);
                rel.setPrimaryKey(pk);
                session.save(rel);
                session.flush();

                /* トランザクションコミット */
                session.getTransaction().commit();

            } catch (HibernateException e) {
                e.printStackTrace();
                throw new HibernateException("システムエラーが発生しました。(プロジェクト-ターゲットリレーション登録)"); //$NON-NLS-1$
            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }

            /* プロダクトの情報をメモリへ設定 */
            ((Product) product).setId(productId);
            _products.put(productId, product);

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
     * ターゲット紐づけ削除(プロジェクトとターゲットの紐付けを削除)
     * 
     * @param ICoronaproduct
     * 
     */
    @Override
    protected void removeProductDam(ICoronaProduct product) {
        int projectId = 0;

        Session session = IoService.getInstance().getSession();
        try {
            /* リレーションの存在チェック */
            try {
                Query query = CommonCreateQuery.getRelPrjProductQuery(this._id, product.getId());
                RelPrjProductBean rel = (RelPrjProductBean) query.uniqueResult();
                if (rel != null) {
                    projectId = rel.getPrimaryKey().getProjectId();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                projectId = -1;
            }
            if (projectId == -1) {
                throw new HibernateException("システムエラーが発生しました。(プロジェクト-ターゲットリレーション存在チェック)"); //$NON-NLS-1$
            }

            /* リレーションの削除 */
            try {
                // RelPrjProductDao.deleteRelPrjProduct(this._id, product.getId())を用いた削除処理を移設する。
                // TODO 20131203 pkがPROJECT_ID＝this._idなので、下記の処理に置換する。

                //session = IoService.getInstance().getSession();
                RelPrjProductBean rel = new RelPrjProductBean();
                RelPrjProductPKBean pk = new RelPrjProductPKBean();
                pk.setProjectId(this._id);
                pk.setProductId(product.getId());
                rel = (RelPrjProductBean) session.get(RelPrjProductBean.class, pk);

                if (rel != null) {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    session.delete(rel);
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                throw new HibernateException("システムエラーが発生しました。(プロジェクト-ターゲットリレーション削除)"); //$NON-NLS-1$
            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }
            try {
                //session = IoService.getInstance().getSession();

                // ProductDao.deleteRelationClaimDataで実行の削除処理を移設
                // ここでの対象テーブルrel_clm_productは３個の主キーの内２個の指定しか存在しないので.delete()は
                // 使用していません。
                StringBuilder sql = new StringBuilder(128);
                sql.append("Delete From rel_clm_product Where prj_id=") //$NON-NLS-1$
                        .append(getId()).append(" And product_id=").append(product.getId()); //$NON-NLS-1$

                /* トランザクション開始 */
                session.beginTransaction();

                session.createSQLQuery(sql.toString()).executeUpdate();
                session.flush();

                /* トランザクションコミット */
                session.getTransaction().commit();

            } catch (HibernateException e) {
                e.printStackTrace();
                // TODO 20131205 Hibernate版で追記
                throw new HibernateException("システムエラーが発生しました。(ターゲット-問い合わせリレーション削除)"); //$NON-NLS-1$
            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }

            // TODO 20131205 以降での更新系の処理では、各自でトランザクションを確立している。

            // ConcurrentModificationException対策 */
            List<IClaimWorkData> works = new ArrayList<IClaimWorkData>(product.getClaimWorkDatas());
            for (IClaimWorkData work : works) {
                product.removeClaimWorkData(work);
            }
            /* ターゲットリストより該当データを削除 */
            this._products.remove(product.getId());
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
     * 辞書情報取得(プロジェクトに紐づく辞書を取得)
     * 
     * @param productId
     * @return List<ICoronaDic>
     * 
     */
    @Override
    protected List<ICoronaDic> getDictionarysDam(Class<?> cls, int projectId) {

        List<ICoronaDic> wrkList = new ArrayList<ICoronaDic>(); /* 編集用List */

        if (_dics == null) {
            _dics = new ArrayList<ICoronaDic>();
        }
        /* 最新のカテゴリ情報を取得 */
        List<TextItem> categoryList = IoActivator.getService().getCategorys();
        try {
            /* ステートメント生成 */
            Session session = IoService.getInstance().getSession();

            /* SQL実行 */
            // DicTableDao.getPrjDicInfo()での機能を移設(HQLでの記述への変更は実施する)
            String para1 = "ProjectId"; //$NON-NLS-1$
            String strSQL = "FROM " //$NON-NLS-1$
                    + "DicTableBean a, RelPrjDicBean b WHERE a.inactive = false AND a.dicId = b.primaryKey.dicId AND b.primaryKey.projectId = :" + para1; //$NON-NLS-1$

            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createQuery(strSQL).setInteger(para1, projectId).list();

            if (list != null) {
                /* 取得結果編集 */
                for (Object[] rs : list) {
                    DicTableBean rt = (DicTableBean) rs[0];
                    ICoronaDic dic = null;

                    /* 既存データチェック */
                    int dicId = rt.getDicId();
                    dic = ((IoService) IoActivator.getService()).getDictionary(dicId);
                    if (dic == null) {
                        /* 新規データ */
                        dic = CoronaObjectUtil.createDicByCurrentData(rt, categoryList);
                    } else {
                        dic.setName(rt.getDicName());
                        dic.setCreationTime(rt.getCreationTime());
                        dic.setLasted(rt.getDate());
                        // dirtyはそのままにする(ここではdirtyの変更なし)
                    }
                    /* wrkListの追加 */
                    if (dic != null) {
                        wrkList.add(dic);
                    }
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


    /**
     * 辞書情報追加(プロジェクトと辞書を紐づける)
     * 
     * @param dic
     * @return なし
     */
    @Override
    protected boolean addDictionaryDam(ICoronaDic dic) {

        int dicId = 0; /* 辞書ID */
        int projectId = 0; /* プロジェクトID */

        Session session = IoService.getInstance().getSession();
        try {
            /* 辞書情報のチェック (_dicsがNullの場合は情報を取得する。) */
            if (_dics == null) {
                _dics = getDictionarys(ICoronaDic.class);
            }
            /* 登録済チェック */
            /*
             * dicId =
             * CommonDao.getFirstInt(DicTableDao.getDicId(dic.getName()));
             * を置換する。
             */
            try {
                // DicTableDao.getDicIdで作成したSQLでの処理を移設(HQLへの変更は実施)
                //Session session = IoService.getInstance().getSession();
                String para1 = "DicName"; //$NON-NLS-1$

                String strSQL = "FROM DicTableBean WHERE dicName = :" + para1; //$NON-NLS-1$
                @SuppressWarnings("unchecked")
                List<DicTableBean> list = session.createQuery(strSQL).setString(para1, dic.getName()).list();
                if (list != null && list.size() > 0) {
                    DicTableBean rt = list.get(0);
                    dicId = rt.getDicId();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                dicId = -1;
            }

            if (dicId == -1) {
                throw new HibernateException("システムエラーが発生しました。(辞書情報取得)"); //$NON-NLS-1$
            } else if (dicId == 0) {
                DicTableBean dicTable = new DicTableBean();
                // DicTableDao.insertDicTableで作成するSQLと同等の処理を実現させる。
                //（insert時のcategoryIdでの項目セットの有無は実現させない。）

                /* 辞書が未登録の場合、辞書を登録 */
                if (dic instanceof IUserDic) {
                    /* ユーザ辞書の場合 */
                    IUserDic userDic = (IUserDic) dic;
                    /* ファイル名の拡張子を".dic"へ変更 */
                    String dicFileName = userDic.getFileName().substring(0, userDic.getFileName().indexOf(".")) + ".dic"; //$NON-NLS-1$ //$NON-NLS-2$
                    String parents = CoronaIoUtils.intListToString(null);
                    userDic.setFileName(dicFileName);
                    dicTable.setParentId(parents);
                    dicTable.setDicName(userDic.getName());
                    dicTable.setDicFileName(userDic.getFileName());
                    dicTable.setDicType(userDic.getDicType().getIntValue());
                    if (userDic.getDicType().getIntValue() == DicType.CATEGORY.getIntValue()) {
                        /* 分野辞書の場合 */
                        dicTable.setCategoryId(userDic.getDicCategory().getId());
                    } else {
                        /* 一般辞書・専門辞書の場合 */
                        dicTable.setCategoryId(0);
                    }
                } else if (dic instanceof ILabelDic) {
                    /* ラベル辞書の場合 */
                    ILabelDic lblDic = (ILabelDic) dic;
                    String parents = CoronaIoUtils.intListToString(lblDic.getParentIds());
                    dicTable.setParentId(parents);
                    dicTable.setDicName(lblDic.getName());
                    dicTable.setDicFileName(""); //$NON-NLS-1$
                    dicTable.setCategoryId(0);
                    dicTable.setDicType(DicType.LABEL.getIntValue());
                } else if (dic instanceof IFlucDic) {
                    /* ゆらぎ辞書の場合 */
                    IFlucDic flucDic = (IFlucDic) dic;
                    String parents = CoronaIoUtils.intListToString(flucDic.getParentIds());
                    dicTable.setParentId(parents);
                    dicTable.setDicName(flucDic.getName());
                    dicTable.setDicFileName(""); //$NON-NLS-1$
                    dicTable.setCategoryId(0);
                    dicTable.setDicType(DicType.FLUC.getIntValue());
                } else if (dic instanceof IPatternDic) {
                    /* パターン辞書の場合 */
                    IPatternDic ptnDic = (IPatternDic) dic;
                    String parents = CoronaIoUtils.intListToString(null);
                    dicTable.setParentId(parents);
                    dicTable.setDicName(ptnDic.getName());
                    dicTable.setDicFileName(""); //$NON-NLS-1$
                    dicTable.setCategoryId(0);
                    dicTable.setDicType(DicType.PATTERN.getIntValue());
                } else if (dic instanceof ISynonymDic) {
                    /* 同義語辞書の場合 */
                    ISynonymDic synDic = (ISynonymDic) dic;
                    String parents = CoronaIoUtils.intListToString(synDic.getParentIds());
                    dicTable.setParentId(parents);
                    dicTable.setDicName(synDic.getName());
                    dicTable.setDicFileName(""); //$NON-NLS-1$
                    dicTable.setCategoryId(0);
                    dicTable.setDicType(DicType.SYNONYM.getIntValue());
                }

                try {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    //Session session = IoService.getInstance().getSession();
                    session.save(dicTable);
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    e.printStackTrace();
                    throw new HibernateException("システムエラーが発生しました。(辞書情報登録)"); //$NON-NLS-1$
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }

                /* 辞書IDを再取得 */
                dicId = 0;
                try {
                    // DicTableDao.getDicIdで作成したSQLでの処理を移設(HQLへの変更は実施)
                    //Session session = IoService.getInstance().getSession();
                    String para1 = "DicName"; //$NON-NLS-1$

                    String strSQL = "FROM DicTableBean WHERE dicName = :" + para1; //$NON-NLS-1$
                    @SuppressWarnings("unchecked")
                    List<DicTableBean> list = session.createQuery(strSQL).setString(para1, dic.getName()).list();
                    if (list != null && list.size() > 0) {
                        DicTableBean rt = list.get(0);
                        dicId = rt.getDicId();
                    }
                } catch (HibernateException e) {
                    e.printStackTrace();
                    dicId = -1;
                }
            } else {

                // インアクティブフラグを落とす
                setInactive(dicId, false);

                /* 辞書が存在する場合 、更新日時を取得し、設定 */
                Date updDate = new Date();
                try {
                    //Session session = IoService.getInstance().getSession();
                    DicTableBean dicTable = (DicTableBean) session.get(DicTableBean.class, dicId);
                    if (dicTable != null) {
                        updDate = dicTable.getDate();
                    }
                } catch (HibernateException e) {
                    e.printStackTrace();
                    updDate = null;
                }
                dic.setLasted(updDate);

                dic.setId(dicId);
            }

            /* 紐づけチェック */
            try {
                // RelPrjDicDao.getProjectIdで作成されるSQLの実行した結果と同等
                projectId = 0;
                //Session session = IoService.getInstance().getSession();
                String strHql = "FROM RelPrjDicBean WHERE primaryKey.projectId=:PRJ_ID AND primaryKey.dicId=:DIC_ID"; //$NON-NLS-1$
                @SuppressWarnings("unchecked")
                List<RelPrjDicBean> list = session.createQuery(strHql).setInteger("PRJ_ID", getId()) //$NON-NLS-1$
                        .setInteger("DIC_ID", dicId) //$NON-NLS-1$
                        .list();
                if (list != null && list.size() > 0) {
                    RelPrjDicBean rs = list.get(0);
                    projectId = rs.getPrimaryKey().getProjectId();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                projectId = -1;
            }

            if (projectId == -1) {
                throw new HibernateException("システムエラーが発生しました。(プロジェクト-辞書リレーション存在チェック)"); //$NON-NLS-1$
            } else if (projectId != 0) {
                if (session.getTransaction().isActive()) {
                    /* トランザクションコミット */
                    session.getTransaction().commit();
                }
                /* 既に紐づけあり */
                return true;
            }

            /* プロジェクトと辞書を紐づける */
            try {
                RelPrjDicBean rel = new RelPrjDicBean();
                RelPrjDicPKBean primaryKey = new RelPrjDicPKBean();
                primaryKey.setProjectId(getId());
                primaryKey.setDicId(dicId);
                rel.setPrimaryKey(primaryKey);
                //Session session = IoService.getInstance().getSession();

                /* トランザクション開始 */
                session.beginTransaction();

                session.save(rel);
                session.flush();

                /* トランザクションコミット */
                session.getTransaction().commit();

            } catch (HibernateException e) {
                e.printStackTrace();
                throw new HibernateException("システムエラーが発生しました。(プロジェクト-辞書リレーション登録)"); //$NON-NLS-1$
            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }

            /* 辞書情報へIDを反映 */
            dic.setId(dicId);
            /*
             * ラベル辞書作成時、dicのインスタンスが置き換えられてしまい親子関係をつけられなかったので、
             * updateDictionarysから変えた
             */
            ((IoService) IoActivator.getService()).addDictionary(dic);

            if (session.getTransaction().isActive()) {
                /* トランザクションコミット */
                session.getTransaction().commit();
            }

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
     * 
     * 辞書情報紐づけ削除(プロジェクトと辞書を紐づけを削除)
     * 
     * @param id
     */
    @Override
    protected void removeDictionaryDam(int id) {
        int projectId = 0;

        Session session = IoService.getInstance().getSession();
        try {
            /* リレーションの存在チェック */
            try {
                // RelPrjDicDao.getProjectIdで作成されるSQLの実行した結果と同等
                //Session session = IoService.getInstance().getSession();
                String strHql = "FROM RelPrjDicBean WHERE primaryKey.projectId=:PRJ_ID AND primaryKey.dicId=:DIC_ID"; //$NON-NLS-1$
                @SuppressWarnings("unchecked")
                List<RelPrjDicBean> list = session.createQuery(strHql).setInteger("PRJ_ID", this._id) //$NON-NLS-1$
                        .setInteger("DIC_ID", id) //$NON-NLS-1$
                        .list();
                if (list != null && list.size() > 0) {
                    RelPrjDicBean rs = list.get(0);
                    projectId = rs.getPrimaryKey().getProjectId();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                projectId = -1;
            }

            if (projectId == -1) {
                throw new HibernateException("システムエラーが発生しました。(プロジェクト-辞書情報：辞書ID取得)"); //$NON-NLS-1$
            }

            /* トランザクション開始 */
            session.beginTransaction();

            /* リレーション削除 */
            try {
                RelPrjDicBean rel = new RelPrjDicBean();
                RelPrjDicPKBean primaryKey = new RelPrjDicPKBean();
                primaryKey.setProjectId(this._id);
                primaryKey.setDicId(id);
                // rel.setPrimaryKey(primaryKey);

                rel = (RelPrjDicBean) session.get(RelPrjDicBean.class, primaryKey);
                if (rel != null) {
                    session.delete(rel);
                    session.flush();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                throw new HibernateException("システムエラーが発生しました。(プロジェクト-辞書情報リレーション削除)"); //$NON-NLS-1$
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


    /**
     * プロジェクト情報更新
     * 
     * @return true/false
     */
    @Override
    public boolean update() {

        // TODO 20131205 ここでは、トランザクションは個々の処理にて確立させる。

        /* クレームデータ更新 */
        updateClaimDatas();

        /* プロダクトデータ更新 */
        updateProducts();
        for (ICoronaProduct product : getProducts()) {
            product.update();
        }

        /* プロジェクト名チェック */
        String name = null;
        try {
            Session session = IoService.getInstance().getSession();
            ProjectBean project = (ProjectBean) session.get(ProjectBean.class, this._id);
            if (project != null) {
                name = project.getProjectName();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            name = null;
        }
        if (name == null) {
            new Exception("プロジェクトが存在しません。").printStackTrace(); //$NON-NLS-1$
            return false;
        }

        /* 取得したプロジェクト名と現在のプロジェクト名が相違する場合、取得したプロジェクト名に書き換える */
        if (!name.equals(this._name)) {
            this.setName(name);
        }

        return true;
    }


    @Override
    public boolean commit(IProgressMonitor monitor) {
        return commit(true, monitor);
    }


    @Override
    public boolean commit(boolean bRecords, IProgressMonitor monitor) {
        /* 名称変更 */
        ProjectBean projectBean;

        Session session = IoService.getInstance().getSession();
        try {
            //session = IoService.getInstance().getSession();
            projectBean = (ProjectBean) session.get(ProjectBean.class, this._id);
            /*
             * jdbc版での使用SQL(ProjectDao.updateProjectNameで作成)が 「UPDATE
             * 」なので存在しない場合は処理を実行しない。
             */
            if (projectBean == null) {
                throw new HibernateException("プロジェクトが存在しません。"); //$NON-NLS-1$
            }
            /* トランザクション開始 */
            session.beginTransaction();

            projectBean.setProjectName(this._name);
            session.save(projectBean);
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

        if (!bRecords) {
            /* トランザクションコミット */
            if (session.getTransaction().isActive()) {
                session.getTransaction().commit();
            }
            return true;
        }
        ICoronaProject project = null;

        /* クレームデータのコミット */
        //commitClaimDatas();
        if (!commitClaimDatas()) {
            // 更新失敗
            return false;
        }

        try {
            /* projectを再取得 */
            projectBean = (ProjectBean) session.get(ProjectBean.class, this._id);
            if (projectBean != null) {
                project = new Project(projectBean.getProjectName(), projectBean.getProjectId());
            }

            /* プロジェクトが存在しなかった場合、エラー */
            if (project == null) {
                throw new HibernateException("プロジェクトが存在しません。"); //$NON-NLS-1$
            }

            if (!project.getName().equals(this.getName())) {
                session = IoService.getInstance().getSession();
                /* プロジェクト名が相違する場合、プロジェクト名を更新 */
                try {
                    projectBean = (ProjectBean) session.get(ProjectBean.class, this._id);
                    /*
                     * jdbc版での使用SQL(ProjectDao.updateProjectNameで作成)が 「UPDATE
                     * 」なので存在しない場合は処理を実行しない。
                     */
                    if (projectBean == null) {
                        throw new HibernateException("プロジェクトが存在しません。"); //$NON-NLS-1$
                    }
                    projectBean.setProjectName(this._name);
                    /* トランザクション開始 */
                    session.beginTransaction();

                    session.save(projectBean);
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    e.printStackTrace();
                    throw new HibernateException("システムエラーが発生しました。(プロジェクト名更新)"); //$NON-NLS-1$
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
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
    }


    /**
     * クレームデータ追加(クレームデータ)
     * 
     * @param iClaimData
     */
    @Override
    public void addClaimData(IClaimData iClaimData) {
        super.addClaimData(iClaimData);
        commitClaimDatas();
    }


    /**
     * クレームデータ追加(id)
     * 
     * @param id
     */
    @Override
    public void addClaimData(int id) {
        super.addClaimData(id);
        commitClaimDatas();
    }


    /**
     * クレームデータ取得
     * 
     * @return List&lt;IClaimData&gt;
     */
    @Override
    public List<IClaimData> getClaimDatas() {
        if (_cliamDatas == null || _cliamDatas.isEmpty()) {
            IoActivator.getService().getClaimDatas();
            updateClaimDatas();
        }
        /* #580 に対応するには、このfor文を消す。 */
        if (_cliamDatas.size() > 0) {
            for (IClaimData claim : _cliamDatas) {
                claim.update();
            }
        }
        return _cliamDatas;
    }


    /**
     * クレームデータ更新
     * 
     * @return true/false
     */
    private boolean updateClaimDatas() {
        List<IClaimData> list = new ArrayList<IClaimData>();
        Map<Integer, IClaimData> claimMap = ((IoService) IoService.getInstance()).getClaimDatasMap();

        try {
            /* HQL編集 */
            String para1 = "PRJ_ID"; //$NON-NLS-1$
            String strSQL = "FROM RelPrjClmBean WHERE primaryKey.projectId =:" + para1; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            /* HQL実行 */
            @SuppressWarnings("unchecked")
            List<RelPrjClmBean> result = session.createQuery(strSQL).setInteger(para1, getId()).list();
            if (result != null) {
                /* SELECT結果編集 */
                for (RelPrjClmBean rs : result) {
                    int claimId = rs.getPrimaryKey().getTableId();
                    /* IoServiceのクレームリストと付きあわせ */
                    IClaimData claim = claimMap.get(claimId);
                    list.add(claim);
                }
            }
            this._cliamDatas = list;

            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 
     * クレームデータコミット
     * 
     * @return true/false
     */
    private boolean commitClaimDatas() {

        /* TODO 20131205 トランザクションはこのメソッドを起動する側で確立する。 */

        /* ProjectDao.insertRelationClaimDatasでの機能を設置する。 */
        boolean ret = true;
        try {
            if (getClaimDatas().size() > 0) {
                RelPrjClmBean rel = new RelPrjClmBean();
                RelPrjClmPKBean relPk = new RelPrjClmPKBean();
                Session session = IoService.getInstance().getSession();
                int id = getId();
                List<IClaimData> records = getClaimDatas();
                for (IClaimData rec : records) {
                    relPk.setProjectId(id);
                    relPk.setTableId(rec.getId());
                    /* jdbc版での「INSERT IGNORE INTO 」(重複するレコードはすべて無視され、挿入されない)への対応 */
                    RelPrjClmBean relCheck = (RelPrjClmBean) session.get(RelPrjClmBean.class, relPk);
                    if (relCheck == null) {
                        /* 重複していない事が確認できたので、保存（追加）する。 */
                        rel.setPrimaryKey(relPk);
                        try {
                            /* トランザクション開始 */
                            session.beginTransaction();

                            session.save(rel);
                            session.flush();

                            /* トランザクションコミット */
                            session.getTransaction().commit();

                        } finally {
                            if (session.getTransaction().isActive()) {
                                /* トランザクションロールバック */
                                session.getTransaction().rollback();
                            }
                        }
                    }
                }
            }
            return ret;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 
     * 辞書エクスポート
     * 
     * @see com.tida_okinawa.corona.io.model.ICoronaDics#exportDictionarys(java.lang
     *      .String)
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
        File[] files = dir.listFiles(getFileExtensionFilter(Messages.Project_extensionDic));

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
                /* TODO:α版ではファイル削除しないのでコメント */
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
    public void removeClaimData(int id) {
        /* TODO 途中でこけても処理を続けていいのか？ロールバックも考えないと。戻り値もvoidでいいの？ */

        Session session = null;
        try {
            boolean result = true; //TODO 20131120 mysql版でも未使用となっていた。
            ClaimData claim = null;
            for (IClaimData c : this.getClaimDatas()) {
                if (c.getId() == id) {
                    claim = (ClaimData) c;
                    break;
                }
            }
            if (claim == null) {
                return;
            }

            String workTblName = CoronaIoUtils.createWorkTableName(claim.getTableName(), TableType.WORK_DATA, this.getId());
            String relTblName = CoronaIoUtils.createWorkTableName(claim.getTableName(), TableType.RESULT_DATA, this.getId());
            int workTblId = 0;
            int relTblId = 0;

            session = IoService.getInstance().getSession();
            try {
                /* TablesDao.getTableIdでの機能を設置する。 */
                String para1 = "DBNAME"; //$NON-NLS-1$
                String strHQL = "FROM TablesBean WHERE dbname =:" + para1; //$NON-NLS-1$
                @SuppressWarnings("unchecked")
                List<TablesBean> wkList = session.createQuery(strHQL).setString(para1, workTblName).list();
                if (wkList != null && wkList.size() > 0) {
                    TablesBean tb = wkList.get(0);
                    workTblId = tb.getId();
                }
                @SuppressWarnings("unchecked")
                List<TablesBean> relList = session.createQuery(strHQL).setString(para1, relTblName).list();
                if (relList != null && relList.size() > 0) {
                    TablesBean tb = relList.get(0);
                    relTblId = tb.getId();
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                return;
            }

            /* 中間データ管理テーブルから対象レコードの削除 */
            if (!deleteClaimWorkData(this.getId(), -1, id, ClaimWorkDataType.NONE)) {
                result = false;
            }

            /* クレーム－ターゲットリレーションテーブルからの削除 */
            /* クレーム－プロジェクトリレーションテーブルからの削除 */
            try {
                // rel_clm_productテーブルからの削除
                boolean proc = true;
                RelClmProductBean delRelClmProduct = (RelClmProductBean) CommonCreateQuery.getRelClmProductQuery(id, this.getId()).uniqueResult();
                /* トランザクション開始 */
                session.beginTransaction();
                if (delRelClmProduct != null) {
                    session.delete(delRelClmProduct);
                    session.flush();
                }
                /* トランザクションコミット */
                session.getTransaction().commit();

                delRelClmProduct = (RelClmProductBean) CommonCreateQuery.getRelClmProductQuery(id, this.getId()).uniqueResult();
                if (delRelClmProduct != null) {
                    result = false;
                    proc = false;
                }

                if (proc) {
                    // rel_prj_clmテーブルからの削除
                    RelPrjClmBean delRelPrjClm = (RelPrjClmBean) CommonCreateQuery.getRelPrjClmQuery(id, this.getId()).uniqueResult();
                    /* トランザクション開始 */
                    session.beginTransaction();

                    if (delRelPrjClm != null) {
                        session.delete(delRelPrjClm);
                        session.flush();
                    }

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                    delRelPrjClm = (RelPrjClmBean) CommonCreateQuery.getRelPrjClmQuery(id, this.getId()).uniqueResult();
                    if (delRelPrjClm != null) {
                        result = false;
                    }
                }
            } catch (HibernateException e) {
                e.printStackTrace();
                result = false;
            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }

            if (workTblId != 0) {
                /* テーブルリストからの対象レコードの削除 */
                try {
                    /* TablesDao.deleteTable の置き換え */
                    TablesBean tables = (TablesBean) session.get(TablesBean.class, workTblId);
                    /* トランザクション開始 */
                    session.beginTransaction();

                    if (tables != null) {
                        session.delete(tables);
                        session.flush();
                    }

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    e.printStackTrace();
                    result = false;
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
                /* 中間データのテーブル削除 */
                try {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    /* CommonDao.deleteTable の置き換え */
                    String strSQL = "DROP TABLE " + workTblName; //$NON-NLS-1$
                    session.createSQLQuery(strSQL).executeUpdate();
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    e.printStackTrace();
                    result = false;
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }

                for (ICoronaProduct prd : getProducts()) {
                    for (IClaimWorkData work : prd.getClaimWorkDatas()) {
                        if (work.getClaimWorkDataType() == ClaimWorkDataType.CORRECTION_MISTAKES) {
                            continue;
                        }
                        int workId = work.getWorkdataId();
                        int fldId = work.getFieldId();
                        /* テンポラリパターン辞書テーブルの削除 */
                        /* DicPatternDao.deleteTmpPatternDic の置き換え */
                        try {
                            /* トランザクション開始 */
                            session.beginTransaction();

                            String strSQL = new StringBuilder(64)
                                    .append("DROP TABLE IF EXISTS TMP_DIC_PATTERN_").append(workId).append("_").append(fldId).toString(); //$NON-NLS-1$ //$NON-NLS-2$
                            session.createSQLQuery(strSQL).executeUpdate();
                            session.flush();

                            /* トランザクションコミット */
                            session.getTransaction().commit();

                        } catch (HibernateException e) {
                            e.printStackTrace();
                            // TODO 20131205 Hibernate版で追加 
                            result = false;
                        } finally {
                            if (session.getTransaction().isActive()) {
                                /* トランザクションロールバック */
                                session.getTransaction().rollback();
                            }
                        }
                        try {
                            /* トランザクション開始 */
                            session.beginTransaction();

                            String strSQL = new StringBuilder(64)
                                    .append("DROP TABLE IF EXISTS TMP_TYPE_PATTERN_").append(workId).append("_").append(fldId).toString(); //$NON-NLS-1$ //$NON-NLS-2$
                            session.createSQLQuery(strSQL).executeUpdate();
                            session.flush();

                            /* トランザクションコミット */
                            session.getTransaction().commit();

                        } catch (HibernateException e) {
                            e.printStackTrace();
                            // TODO 20131205 Hibernate版で追加 
                            result = false;
                        } finally {
                            if (session.getTransaction().isActive()) {
                                /* トランザクションロールバック */
                                session.getTransaction().rollback();
                            }
                        }
                        /* 辞書プライオリティテーブルからの削除 */
                        /* DicPatternDao.deleteTmpPatternDic の置き換え */
                        try {
                            /* トランザクション開始 */
                            session.beginTransaction();

                            String strSQL = "DELETE FROM DIC_PRI WHERE ID = " + workId + " AND FLD_ID = " + fldId; //$NON-NLS-1$ //$NON-NLS-2$
                            session.createSQLQuery(strSQL).executeUpdate();
                            session.flush();

                            /* トランザクションコミット */
                            session.getTransaction().commit();

                        } catch (HibernateException e) {
                            e.printStackTrace();
                            // TODO 20131205 Hibernate版で追加 
                            result = false;
                        } finally {
                            if (session.getTransaction().isActive()) {
                                /* トランザクションロールバック */
                                session.getTransaction().rollback();
                            }
                        }

                        // TODO 20131205 このループ内での処理ではmysql版では例外エラー発生でも処理を継続させる処理となっている。
                        //   Hibernate版では、エラー発生の場合、ロールバックとする。
                    }
                }
            }

            if (relTblId != 0) {
                /* テーブルリストからの対象レコードの削除 */
                try {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    /* TablesDao.deleteTable の置き換え */
                    String strSQL = "DELETE FROM TABLES WHERE ID = " + relTblId; //$NON-NLS-1$
                    session.createSQLQuery(strSQL).executeUpdate();
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    e.printStackTrace();
                    result = false;
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
                /* 出力テーブルのテーブル削除 */
                try {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    /* CommonDao.deleteTable の置き換え */
                    String strSQL = "DROP TABLE " + relTblName; //$NON-NLS-1$
                    session.createSQLQuery(strSQL).executeUpdate();
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    e.printStackTrace();
                    result = false;
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
            }

            if (result) {
                if (session.getTransaction().isActive()) {
                    /* トランザクションコミット */
                    session.getTransaction().commit();
                }
            }
            super.removeClaimData(id);
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
    }


    /**
     * 係り受け解析実行設定を更新する
     */
    @Override
    protected void saveKnpConfigDam(int knpConfig) {

        Session session = IoService.getInstance().getSession();
        try {
            /* トランザクション開始 */
            session.beginTransaction();

            /* ProjectDao.updateKnpConfig で作成したSQLへの実行の置き換え */
            //Session session = IoService.getInstance().getSession();
            int projectId = this.getId();
            String strSQL = "UPDATE PROJECT SET KNP_CONFIG = " + knpConfig + " WHERE PROJECT_ID = " + projectId; //$NON-NLS-1$ //$NON-NLS-2$
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
     * 係り受け解析実行設定を取得する
     * 
     * @see {@link ProjectKnpConfig}
     * 
     * @return ProjectKnpConfig のキー
     */
    @Override
    protected int getKnpConfigDam() {
        int knpConfg = ProjectKnpConfig.INHERIT.getKey();

        try {
            /* ProjectDao.getKnpConfig で作成したSQLへの実行の置き換え */
            Session session = IoService.getInstance().getSession();

            // TODO 20131120 PK条件での取得なので、取得方法を改善する。
            ProjectBean project = (ProjectBean) session.get(ProjectBean.class, this.getId());
            if (project != null) {
                session.refresh(project);
            }
            if (project != null) {
                knpConfg = project.getKnpConfig();
            }
        } catch (HibernateException e) {
            /* TODO 自動生成された catch ブロック */
            e.printStackTrace();
        }

        return knpConfg;
    }


    @Override
    public boolean getRelation(int productId, int dicId) {
        try {
            Session session = IoService.getInstance().getSession();
            RelProductDicPKBean relPk = new RelProductDicPKBean();
            relPk.setDicId(dicId);
            relPk.setProductId(productId);
            RelProductDicBean rel = (RelProductDicBean) session.get(RelProductDicBean.class, relPk);
            if (rel != null) {
                if (rel.getPrimaryKey().getDicId() == 0) {
                    return false;
                }
            }
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        // TODO 20131113 jdbcではtrueとなっていたが、falseで返す。
        return false;
    }


    @Override
    public boolean delDicPriority(String strIds, int dicId) {
        Session session = IoService.getInstance().getSession();
        try {
            /* トランザクション開始 */
            session.beginTransaction();

            /* DicPriDao.deleteDicPriority の置き換え */
            //Session session = IoService.getInstance().getSession();
            String strSQL = "DELETE FROM DIC_PRI WHERE DIC_ID = " + dicId + " AND ID IN (" + strIds + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            session.createSQLQuery(strSQL).executeUpdate();
            session.flush();

            /* トランザクションコミット */
            session.getTransaction().commit();

            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }
        return false;
    }


    @Override
    public Map<Integer, String> getWorkData(int projectId) {

        Map<Integer, String> wkMap = new HashMap<Integer, String>();
        try {

            /* SQL編集 */
            /* ClaimWorkDataDao.getWorkDataPrd(projectId) で作成されたSQLの置き換え */
            String strSQL = "SELECT wk.ID, wk.PRODUCT_ID, wk.TYPE FROM WORKDATAS wk, REL_PRJ_PRODUCT rel WHERE rel.PROJECT_ID = " + projectId //$NON-NLS-1$
                    + " AND wk.PROJECT_ID = rel.PROJECT_ID AND wk.PRODUCT_ID = rel.PRODUCT_ID ORDER BY PRODUCT_ID, ID"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();

            /* SQL実行 */
            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(strSQL).list();
            if (list != null) {
                /* SELECT結果編集 */
                int chkId = 0;
                String strIds = ""; //$NON-NLS-1$
                String strTypes = ""; //$NON-NLS-1$
                for (Object[] rs : list) {
                    /* マップ使ってプロジェクト毎のIDを集約 */
                    int projId = rs[0] == null ? 0 : Integer.parseInt(rs[0].toString());
                    int prdId = rs[1] == null ? 0 : Integer.parseInt(rs[1].toString());
                    int type = rs[2] == null ? 0 : Integer.parseInt(rs[2].toString());

                    // TODO 20131113 wkMap.put( )でのデータ格納は以下の処理（jdbc版と同等）でよいのか？ 
                    if (chkId == 0) {
                        strIds = String.valueOf(projId);
                        strTypes = String.valueOf(type);
                        chkId = prdId;
                    } else if (chkId == prdId) {
                        strIds = strIds + "," + String.valueOf(projId); //$NON-NLS-1$
                        strTypes = strTypes + "," + String.valueOf(type); //$NON-NLS-1$
                    } else {
                        wkMap.put(chkId, strIds + "/" + strTypes); //$NON-NLS-1$
                        strIds = ""; //$NON-NLS-1$
                        strTypes = ""; //$NON-NLS-1$
                        strIds = String.valueOf(projId);
                        strTypes = String.valueOf(type);
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
        try {
            /* ProjectDao.getKnpConfig で作成したSQLへの実行の置き換え */
            Session session = IoService.getInstance().getSession();

            String strSQL = "SELECT COUNT(*) FROM DIC_PRI WHERE ID = " + id + " AND DIC_ID = " + dicId; //$NON-NLS-1$ //$NON-NLS-2$
            Object res = session.createSQLQuery(strSQL).uniqueResult();
            count = Integer.parseInt(res.toString());
        } catch (ClassCastException | HibernateException e) {
            e.printStackTrace();
            return false;
        }
        if (count == 0) {
            return false;
        }
        return true;
    }


    @Override
    public void addDicPriority(int id, int dicId) {

        Session session = IoService.getInstance().getSession();
        try {
            /* SQL編集 */
            /* DicPriDao.getFields(id) で作成されたSQLの置き換え */
            String strSQL = "SELECT FLD_ID FROM DIC_PRI WHERE ID = " + id + " GROUP BY FLD_ID ORDER BY FLD_ID"; //$NON-NLS-1$ //$NON-NLS-2$
            //Session session = IoService.getInstance().getSession();
            /* SQL実行 */
            @SuppressWarnings("unchecked")
            List<Object> list = session.createSQLQuery(strSQL).list();

            List<Integer> fldList = new ArrayList<Integer>();
            if (list != null) {
                for (Object rs : list) {
                    fldList.add((Integer) rs);
                }
            }

            /* 取得したフィールドIDと引数の値をもとにプライオリティを追加 */
            for (int fldId : fldList) {
                /* トランザクションは当メソッド内にて実施 */
                insertDicPri(id, fldId, dicId, -1, true, true);
            }

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
     * プロジェクトに紐づくターゲット情報取得
     * 
     * @param id
     * @return
     */
    private static String getPrjProductInfo(int projectId) {
        // ProductDaoより移設

        StringBuilder strSQL = new StringBuilder(192);
        strSQL.append("SELECT a.PRODUCT_ID, a.PRODUCT_NAME, b.PROJECT_ID "); //$NON-NLS-1$
        strSQL.append("FROM PRODUCT a, REL_PRJ_PRODUCT b "); //$NON-NLS-1$
        strSQL.append("WHERE a.PRODUCT_ID = b.PRODUCT_ID AND "); //$NON-NLS-1$
        strSQL.append("b.PROJECT_ID = '").append(projectId).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
        return strSQL.toString();
    }


    /**
     * ターゲット情報存在チェック
     * 
     * @param name
     * @return
     */
    private static int getProductId(String name) {
        // ProductDaoより移設

        Session session = IoService.getInstance().getSession();
        /* ターゲットID取得 */
        String strHQL = "FROM ProductBean WHERE productName = :ProductName"; //$NON-NLS-1$
        int ret = 0;
        try {
            @SuppressWarnings("unchecked")
            List<ProductBean> list = session.createQuery(strHQL).setString("ProductName", name) //$NON-NLS-1$
                    .list();
            if (list != null && list.size() > 0) {
                ProductBean rs = list.get(0);
                ret = rs.getProductId();
            }
        } catch (HibernateException e) {
            ret = -1;
        }
        return ret;
    }


    /**
     * ターゲット情報登録
     * 
     * @param name
     * @return
     */
    private static int insertProduct(String name) {
        // ProductDaoより移設

        Session session = IoService.getInstance().getSession();
        try {
            /* トランザクション開始 */
            session.beginTransaction();

            ProductBean product = new ProductBean();
            product.setProductName(name);
            session.save(product);
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

        /* 登録したレコードのターゲットIDを取得 */
        return getProductId(name);
    }


    /**
     * @param dicId
     * @param inactive
     * @return
     */
    private static Boolean setInactive(int dicId, boolean inactive) {
        // DicTableDaoより移設(Hibernateを使用)

        String prefix = "_%deleted%_"; //$NON-NLS-1$
        Session session = IoService.getInstance().getSession();
        DicTableBean dicTable = (DicTableBean) session.get(DicTableBean.class, dicId);
        if (dicTable == null) {
            return false;
        }
        String name = dicTable.getDicName();
        if (name != null) {
            if (inactive) {
                name = prefix + name;
            } else {
                if (name.indexOf(prefix) != -1) {
                    name = name.substring(prefix.length());
                }
            }
            dicTable.setDicName(name);
            dicTable.setInactive(inactive);
            try {
                /* トランザクション開始 */
                session.beginTransaction();

                session.save(dicTable);
                session.flush();

                /* トランザクションコミット */
                session.getTransaction().commit();

            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }
        }
        return false;
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

        Session session = IoService.getInstance().getSession();
        try {
            List<Integer> ids = null;
            try {
                @SuppressWarnings("unchecked")
                List<Integer> list = session.createSQLQuery(selectSql.toString()).list();
                ids = list;
            } catch (HibernateException e) {
                e.printStackTrace();
                ids = new ArrayList<Integer>();
            }

            /* 辞書優先度テーブルから削除 */
            String deleteSql = "DELETE FROM DIC_PRI WHERE ID = "; //$NON-NLS-1$
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

            /* トランザクションコミット */
            session.getTransaction().commit();

            /* ワークデータテーブルから削除 */
            return deleteWorkFromManageTable(projectId, productId, claimId, type);
        } catch (HibernateException e) {
            CoronaActivator.debugLog(selectSql.toString());
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
        Session session = IoService.getInstance().getSession();
        try {
            /* トランザクション開始 */
            session.beginTransaction();
            session.createSQLQuery(deleteSql.toString()).executeUpdate();
            session.flush();

            /* トランザクションコミット */
            session.getTransaction().commit();

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
     * @param workId
     * @param fldId
     * @param dicId
     * @param priority
     *            ０以下なら、優先度最低とする
     * @param inactive
     * @param add
     *            優先リストにすでに同じ値があるときは何もしないならtrue。すでにある値の更新ならfalse
     * @return 正常に追加できたらtrue。そうでなければfalse
     */
    private static boolean insertDicPri(int workId, int fldId, int dicId, int priority, boolean inactive, boolean add) {

        // DicPriDao.insertDicPriを移設

        if (priority <= 0) {
            /* #1301 クレンジング時プライオリティ値がマイナスの場合はマイナスにする */
            priority = getMinPriority(workId, fldId);
            priority--;
        }
        if (add) {
            int pri = 0;
            try {
                String strSQL = "SELECT PRIORITY FROM DIC_PRI WHERE ID = " + workId + " AND FLD_ID = " + fldId + " AND DIC_ID = " + dicId; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                Session session = IoService.getInstance().getSession();
                @SuppressWarnings("unchecked")
                List<Object> list = session.createSQLQuery(strSQL).list();
                if (list != null && list.size() > 0) {
                    Object rs = list.get(0);
                    pri = Integer.parseInt(rs.toString());
                }
            } catch (ClassCastException ex) {
                ex.printStackTrace();
                pri = -1;
            } catch (HibernateException e) {
                e.printStackTrace();
                pri = -1;
            }

            if (pri > 0) {
                return true;
            }
        }

        // 最後にマッピング
        String sep = ","; //$NON-NLS-1$
        StringBuilder sql = new StringBuilder(512);
        sql.append("MERGE INTO DIC_PRI (ID, FLD_ID, DIC_ID, PRIORITY, INACTIVE) KEY (ID, FLD_ID, DIC_ID) VALUES ("); //$NON-NLS-1$
        sql.append(workId).append(sep).append(fldId).append(sep).append(dicId).append(sep).append(priority).append(sep);
        sql.append(inactive).append(") "); //$NON-NLS-1$
        String strSQL = sql.toString();
        //        String strSQL = "INSERT INTO DIC_PRI (ID, FLD_ID, DIC_ID, PRIORITY, INACTIVE) VALUES (" + workId + ", " + fldId + "," + dicId + "," + priority + "," //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        //                + inactive + ") ON DUPLICATE KEY UPDATE PRIORITY =VALUES(PRIORITY), INACTIVE =VALUES(INACTIVE)"; //$NON-NLS-1$
        Session session = IoService.getInstance().getSession();
        try {
            /* トランザクション開始 */
            session.beginTransaction();

            session.createSQLQuery(strSQL).executeUpdate();

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
     * #1301 プライオリティの最小値取得
     * 
     * @param workId
     * @param fieldId
     * @return
     */
    private static int getMinPriority(int workId, int fieldId) {

        // DicPriDao.getMinPriorityを移設

        StringBuilder sql = new StringBuilder(128);
        sql.append("Select min(priority) From dic_pri Where id=").append(workId).append(" And fld_id=").append(fieldId); //$NON-NLS-1$ //$NON-NLS-2$
        int min = 0;
        try {
            Session session = IoService.getInstance().getSession();
            @SuppressWarnings("unchecked")
            List<Object> list = session.createSQLQuery(sql.toString()).list();
            if (list != null && list.size() > 0) {
                Object rs = list.get(0);
                min = Integer.parseInt(rs.toString());
            }
        } catch (ClassCastException ex) {
            ex.printStackTrace();
            min = -1;
        } catch (HibernateException e) {
            e.printStackTrace();
            min = -1;
        }

        if (min > 0) {
            min = 0;
        }
        return min;
    }
}