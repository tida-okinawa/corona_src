/**
 * @version $Id: PatternDic.java 997 2013-06-05 01:58:11Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.bean.DicPatternBean;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.abstraction.AbstractPatternDic;
import com.tida_okinawa.corona.io.model.dic.impl.PatternItem;

/**
 * @author yukihiro-kinjyo
 * 
 */
public class PatternDic extends AbstractPatternDic {
    /**
     * アイテムの更新を行ったかどうか
     */
    protected boolean bRefreshRecords = false;


    /**
     * @param id
     *            辞書ID(なければ
     *            {@link com.tida_okinawa.corona.io.model.dic.ICoronaDic#UNSAVED_ID}
     *            )
     * @param name
     *            辞書名
     * @param lasted
     *            更新日
     */
    public PatternDic(int id, String name, Date lasted) {
        super(id, name, lasted);
    }


    @Override
    protected boolean doCommit(boolean bRecords, IProgressMonitor monitor) {

        int count = 0;
        count += addItems.size();
        if (delItems != null) {
            count += delItems.size();
        }
        if (items != null) {
            count += items.size();
        }
        monitor.beginTask("辞書のコミット", count); //$NON-NLS-1$

        /* 辞書名の変更 */
        int id = getId();
        Session session = IoService.getInstance().getSession();
        DicTableBean dicTable = null;

        try {
            // session.clear();
            dicTable = (DicTableBean) session.get(DicTableBean.class, id);
            session.evict(dicTable); // 一度取得したオブジェクトを、キャッシュからでなく再度DBから取得
            session.refresh(dicTable); // オブジェクトの再取得

            /* トランザクション開始 */
            session.beginTransaction();

            dicTable.setDicName(this.getName());
            dicTable.setDicFileName(""); //$NON-NLS-1$
            session.save(dicTable);
            session.flush();

            /* トランザクションコミット */
            session.getTransaction().commit();

        } catch (HibernateException e) {
            return false;

        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }

        if (!bRecords)
            return true;

        items.addAll(addItems);
        boolean result = insertRecords(id, items);
        addItems.clear();
        monitor.worked(items.size());
        /* 用語削除 */
        /* 削除に成功したアイテムを、リストからも削除する */
        items.removeAll(deleteRecords(id, delItems));
        delItems.clear();
        monitor.worked(items.size() + delItems.size());

        /* これがないと、updateRecords内でIDicItemのインスタンスが変わってしまう */
        if (itemsForSearch == null) {
            itemsForSearch = new HashMap<Integer, IDicItem>();
        } else {
            itemsForSearch.clear();
        }
        for (IDicItem item : items) {
            itemsForSearch.put(item.getId(), item);
        }
        updateRecords();

        monitor.done();
        return result;
    }


    /**
     * @param dbName
     * @param fieldId
     * @param workId
     * @param records
     * @return
     */
    private static boolean insertRecords(int dicId, List<IDicItem> records) {

        /* DicPatternDaoより移設 */
        if (records.size() == 0)
            return true;

        /* 削除処理を正常に動かすために、INACTIVEの定義をテーブルに追加した。そのため、追加にもINACTIVEを追加 */
        Session session = IoService.getInstance().getSession();
        DicPatternBean dicPattern = null;
        for (IDicItem rec : records) {
            final IPattern ptn = (IPattern) rec;

            if (rec.isDirty()) {
                int id = ptn.getId();
                try {
                    dicPattern = (DicPatternBean) session.get(DicPatternBean.class, id);

                    /* トランザクション開始 */
                    session.beginTransaction();

                    if (dicPattern == null) {
                        /* DBよりデータ取得がなし */
                        dicPattern = new DicPatternBean();
                        dicPattern.setId(id);
                    }
                    dicPattern.setDicId(dicId);
                    dicPattern.setName(ptn.getLabel());
                    dicPattern.setPattern(ptn.getText());
                    dicPattern.setTypeId(ptn.getPatternType());
                    dicPattern.setParts(ptn.isParts());
                    dicPattern.setInactive(false);

                    // INSERTとUPDATEで区分けせず、saveを実行する。
                    session.save(dicPattern);
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                    // レコードIDによって、INSERTとUPDATEに処理を振り分ける
                    if (ptn.getId() == IDicItem.UNSAVED_ID) {
                        // INSERT
                        String getIdHql = "Select Max(id) From DicPatternBean"; //$NON-NLS-1$
                        int maxId = -1;
                        try {
                            maxId = (int) session.createQuery(getIdHql).uniqueResult();
                        } catch (HibernateException e) {
                            maxId = -1;
                        }
                        if (maxId > 0) {
                            ptn.setId(maxId);
                        }
                    }
                } catch (HibernateException e) {
                    // TODO 20131120
                    // mysql版ではここでの例外エラーでの処理はなし（CommonDao.executeSQL使用）

                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
            }
        }
        return true;
    }


    /**
     * @param dicId
     *            削除するアイテムを保持している辞書のID
     * @param records
     *            削除対象のアイテム
     * @return 削除に成功したアイテムのリスト
     */
    private static List<IDicItem> deleteRecords(int dicId, List<IDicItem> records) {

        // DicPatternDaoに配置されていたメソッド

        List<IDicItem> ret = new ArrayList<IDicItem>();
        if (records.size() == 0)
            return ret;

        Session session = IoService.getInstance().getSession();
        DicPatternBean dicPattern = null;
        for (IDicItem rec : records) {
            try {
                dicPattern = (DicPatternBean) session.get(DicPatternBean.class, rec.getId());

                if (dicPattern != null) {

                    /* トランザクション開始 */
                    session.beginTransaction();

                    dicPattern.setInactive(true);

                    session.save(dicPattern);
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                    ret.add(rec);
                }
            } catch (HibernateException e) {
                // 処理なし

            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }
        }
        return ret;
    }


    @Override
    public boolean update() {
        updateDicInformation();
        bRefreshRecords = false;
        return true;
    }


    boolean updateDicInformation() {

        Session session = IoService.getInstance().getSession();
        DicTableBean dicTable = null;
        try {
            dicTable = (DicTableBean) session.get(DicTableBean.class, this.getId());
            if (dicTable != null) {
                if (!dicTable.isInactive()) {
                    this._name = dicTable.getDicName();
                    this._creationTime = dicTable.getCreationTime();
                    this._lasted = dicTable.getDate();
                    setDirty(false);
                }
            }
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean updateRecords() {
        bRefreshRecords = true;
        List<IDicItem> list = new ArrayList<IDicItem>();
        if (items == null) {
            items = new ArrayList<IDicItem>();
            itemsForSearch = new TreeMap<Integer, IDicItem>();
        }

        Session session = IoService.getInstance().getSession();
        String strHQL = "FROM DicPatternBean WHERE dicId=:DicId AND inactive=false"; //$NON-NLS-1$
        try {

            /* 辞書に紐づく用語を取得 */
            /* DIC_IDをキーにデータを取得 */
            /* String strSQL = DicPatternDao.getRecords(getId()); を置換する。 */
            /* SQL実行 */
            @SuppressWarnings("unchecked")
            List<DicPatternBean> result = session.createQuery(strHQL).setInteger("DicId", getId()).list(); //$NON-NLS-1$
            if (result != null) {
                /* 取得結果編集 */
                for (DicPatternBean rp : result) {
                    int id = rp.getId();
                    /* 既存のリストから検索 */
                    IPattern pattern = (IPattern) getItem(id);
                    /* 既存の削除リストから検索 */
                    if (pattern == null && delItems.size() > 0) {
                        for (IDicItem p : delItems) {
                            if (p.getId() == id) {
                                pattern = (IPattern) p;
                                break;
                            }
                        }
                    }
                    if (pattern == null) {
                        pattern = new PatternItem(rp.getName(), rp.getPattern(), rp.getTypeId(), rp.isParts());
                        pattern.setId(id);
                    } else {
                        pattern.setLabel(rp.getName());
                        pattern.setText(rp.getPattern());
                        pattern.setPatternType(rp.getTypeId());
                        pattern.setParts(rp.isParts());
                    }
                    ((PatternItem) pattern).setDicId(getId());
                    pattern.setDirty(false);
                    list.add(pattern);

                }
                /* 構築したリストに入れ替える */
                items.clear();
                items.addAll(list);

                itemsForSearch.clear();
                for (IDicItem item : items) {
                    itemsForSearch.put(item.getId(), item);
                }

                setDirty(false);
                delItems.clear();
                addItems.clear();
            }

            /* 更新日を最新へ設定 */
            Date updDate = new Date();
            DicTableBean dicTable = (DicTableBean) session.get(DicTableBean.class, this.getId());
            if (dicTable != null) {
                updDate = dicTable.getDate();
            }
            this.setLasted(updDate);

            return true;
        } catch (HibernateException e) {
            bRefreshRecords = false;
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public int getItemCount() {
        if (items == null) {
            String hql = "Select count(*) from DicPatternBean where dicId=:DicId and inactive=false "; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            try {
                int resultCnt = (int) ((long) session.createQuery(hql).setInteger("DicId", getId()) //$NON-NLS-1$
                        .uniqueResult());
                return resultCnt;
            } catch (HibernateException e) {
                return 0;
            }
        }
        return items.size();
    }


    @Override
    public List<IDicItem> getItems() {
        if (!bRefreshRecords) {
            updateRecords();
        }
        return super.getItems();
    }


    @Override
    public void addItem(IDicItem item) {
        if (!bRefreshRecords) {
            updateRecords();
        }
        super.addItem(item);
    }


    @Override
    public void removeItem(IDicItem item) {
        if (!bRefreshRecords) {
            updateRecords();
        }
        super.removeItem(item);
    }


    /**
     * パターンアイテム取得
     * 
     * @param id
     *            パターンID
     * @return 該当するパターン
     */
    @Override
    public IDicItem getItem(int id) {
        if (!bRefreshRecords) {
            updateRecords();
        }
        return itemsForSearch.get(id);
    }
}
