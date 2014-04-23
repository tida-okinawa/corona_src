/**
 * @version $Id: TemplateFactory.java 997 2013-06-05 01:58:11Z yukihiro-kinjyo $
 * 
 * 2012/11/27 0:44:18
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.bean.TemplatePatternBean;
import com.tida_okinawa.corona.io.model.dic.ITemplateItem;
import com.tida_okinawa.corona.io.model.dic.abstraction.AbstractTemplate;
import com.tida_okinawa.corona.io.model.dic.impl.TemplateItem;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateFactory extends AbstractTemplate {

    private static TemplateFactory _instance = new TemplateFactory();


    /**
     * ひな型オブジェクト生成クラスのインスタンスを取得する
     * 
     * @return iDicFactoryを実装したDicFactoryのインスタンス
     */
    public static TemplateFactory getInstance() {
        return _instance;
    }


    /**
     * ひな型オブジェクト生成クラス
     */
    public TemplateFactory() {
        super();
    }


    /* ****************************************
     * ICoronaComponent
     */
    @Override
    public boolean update() {
        return false;
    }


    @Override
    public boolean commit(boolean bRecords, IProgressMonitor monitor) {
        /* プログレス・モニタの初期設定 */
        int count = 0;
        count += addItems.size();
        if (removeItems != null) {
            count += removeItems.size();
        }
        if (items != null) {
            count += items.size();
        }
        if (monitor != null)
            monitor.beginTask("ひな型の更新", count); //$NON-NLS-1$

        /* Insert */
        items.addAll(addItems);
        boolean result = insertRecords(items);
        addItems.clear();
        if (monitor != null)
            monitor.worked(items.size());

        /*
         * Remove
         * ⇒ 削除に成功したアイテムのみリストから消去
         */
        items.removeAll(deleteRecords(removeItems));
        removeItems.clear();
        if (monitor != null)
            monitor.worked(removeItems.size());

        /* itemsの再構築 */
        updateRecords();

        if (monitor != null)
            monitor.done();
        return result;
    }


    @Override
    public boolean commit(IProgressMonitor monitor) {
        /* bRecordsによる処理制限は不要なのでtrue/falseは不問 */
        return commit(true, monitor);
    }


    /* ****************************************
     * ITemplateFactory
     */
    @Override
    public List<ITemplateItem> getItems() {
        updateRecords();
        return items;
    }


    @Override
    public Set<ITemplateItem> getAddItems() {
        return addItems;
    }


    @Override
    public boolean updateRecords() {
        List<ITemplateItem> list = new ArrayList<ITemplateItem>();
        if (items == null) {
            items = new ArrayList<ITemplateItem>();
        }


        try {
            Session session = IoService.getInstance().getSession();
            /* データを取得 */
            /* TemplateDao.getRecords(); を置換する。 */
            String strHQL = "FROM TemplatePatternBean WHERE inactive=false"; //$NON-NLS-1$

            /* HQL実行 */
            @SuppressWarnings("unchecked")
            List<TemplatePatternBean> beanList = session.createQuery(strHQL).list();

            if (beanList != null) {
                /* 取得結果編集 */
                for (TemplatePatternBean rs : beanList) {
                    ITemplateItem template = null;
                    int id = rs.getId();
                    /* 既存データかチェック */
                    if (items.size() > 0) {
                        for (ITemplateItem t : items) {
                            if (t.getId() == id) {
                                template = t;
                                break;
                            }
                        }
                    }
                    /* 存在しないデータの場合 */
                    if (template == null) {
                        template = new TemplateItem(rs.getTemplateId(), rs.getName(), rs.getTemplate(), rs.getTypeId(), rs.isParts());
                        template.setId(id);
                    }
                    /* 存在するデータの場合 */
                    else {
                        template.setTemplateId(rs.getTemplateId());
                        template.setName(rs.getName());
                        template.setText(rs.getTemplate());
                        template.setPatternType(rs.getTypeId());
                        template.setParts(rs.isParts());
                    }
                    template.setDirty(false);
                    list.add(template);
                }
                /* 構築したリストに入れ替える */
                items.clear();
                items.addAll(list);
            }

        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    public int getItemCount() {
        return 0;
    }


    /**
     * ひな型更新
     * 
     * @param records
     *            登録対象のアイテム
     * @return true
     */
    private static boolean insertRecords(List<ITemplateItem> records) {

        //TemplateDaoから移設

        if (records.size() == 0)
            return true;

        try {
            TemplatePatternBean tpBean = new TemplatePatternBean();
            Session session = IoService.getInstance().getSession();
            for (ITemplateItem rec : records) {
                if (rec.isDirty()) {
                    final ITemplateItem ptn = rec;

                    tpBean.setId(ptn.getId());
                    tpBean.setTemplateId(ptn.getTemplateId());
                    tpBean.setName(ptn.getName());
                    tpBean.setTemplate(ptn.getText());
                    tpBean.setTypeId(ptn.getPatternType());
                    tpBean.setParts(ptn.isParts());
                    tpBean.setInactive(false);

                    try {
                        /* トランザクション開始 */
                        session.beginTransaction();

                        /* INSERT or UPDATE */
                        session.save(tpBean);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();

                    } finally {
                        if (session.getTransaction().isActive()) {
                            /* トランザクションロールバック */
                            session.getTransaction().rollback();
                        }
                    }
                    if (ptn.getId() == ITemplateItem.UNSAVED_ID) {
                        /* INSERT */

                        /* 登録したアイテムのID取得 */
                        String getIdSql = "Select Max(id) From template_pattern"; //$NON-NLS-1$
                        int id = 0;
                        try {
                            @SuppressWarnings("unchecked")
                            List<Object> idList = session.createSQLQuery(getIdSql).list();
                            if (idList != null && idList.size() > 0) {
                                Object rs = idList.get(0);
                                id = Integer.parseInt(rs.toString());
                            }
                        } catch (HibernateException e) {
                            id = -1;
                        }
                        if (id > 0) {
                            ptn.setId(id);
                        }
                    }
                }
            }
        } catch (HibernateException e) {
            //jdbc版では処理なし
        }
        return true;
    }


    /**
     * ひな型削除
     * 
     * @param records
     *            削除対象のアイテム
     * @return 削除に成功したアイテムのリスト
     */
    private static List<ITemplateItem> deleteRecords(List<ITemplateItem> records) {
        List<ITemplateItem> ret = new ArrayList<ITemplateItem>();
        if (records.size() == 0)
            return ret;

        try {
            Session session = IoService.getInstance().getSession();
            for (ITemplateItem rec : records) {
                TemplatePatternBean tpBean = (TemplatePatternBean) session.get(TemplatePatternBean.class, rec.getId());
                if (tpBean != null) {
                    try {
                        /* トランザクション開始 */
                        session.beginTransaction();

                        session.delete(tpBean);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();

                        ret.add(rec);
                    } catch (HibernateException e) {
                        e.printStackTrace();
                    } finally {
                        if (session.getTransaction().isActive()) {
                            /* トランザクションロールバック */
                            session.getTransaction().rollback();
                        }
                    }
                }
            }
        } catch (HibernateException e) {
            //jdbc版では処理なし
        }

        return ret;
    }
}
