/**
 * @version $Id: FlucDic.java 997 2013-06-05 01:58:11Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.bean.DicFlucBean;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.bean.FlucTblBean;
import com.tida_okinawa.corona.io.bean.RelFlucBean;
import com.tida_okinawa.corona.io.bean.RelFlucPKBean;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDependSub;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.abstraction.AbstractFlucDic;
import com.tida_okinawa.corona.io.model.dic.impl.FlucItem;
import com.tida_okinawa.corona.io.model.dic.impl.FlucSubItem;
import com.tida_okinawa.corona.io.model.dic.impl.Term;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * @author yukihiro-kinjo
 * 
 */
public final class FlucDic extends AbstractFlucDic {
    /**
     * アイテムの更新を行ったかどうか
     */
    private boolean bRefreshRecords = false;


    /**
     * @param id
     *            ID
     * @param name
     *            辞書名
     * @param parentId
     *            親辞書ID
     */
    public FlucDic(int id, String name, int parentId) {
        super(id, name, null);
        this.addParentId(parentId);
    }


    /**
     * @param id
     *            Id
     * @param name
     *            辞書名
     * @param lasted
     *            更新日時
     * @param parentId
     *            親辞書ID
     */
    public FlucDic(int id, String name, Date lasted, Set<Integer> parentId) {
        super(id, name, lasted, parentId);
    }


    @Override
    protected boolean doCommit(boolean bRecords, IProgressMonitor monitor) {

        Session session = IoService.getInstance().getSession();

        try {

            @SuppressWarnings("unchecked")
            List<DicTableBean> dicTableList = session.createQuery("from DicTableBean where dicId = :dicId") //$NON-NLS-1$
                    .setInteger("dicId", this.getId()).list(); //$NON-NLS-1$

            if (dicTableList != null && dicTableList.size() > 0) {
                for (DicTableBean dic : dicTableList) {
                    dic.setParentId(CoronaIoUtils.intListToString(this.getParentIds()));
                    dic.setDicName(this.getName());
                    /* トランザクション開始 */
                    session.beginTransaction();
                    session.save(dic);
                    session.flush();
                    /* トランザクションコミット */
                    session.getTransaction().commit();
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

        if (!bRecords) {
            return true;
        }

        int work = 0;
        int count = 0;
        if (delItems != null) {
            count += delItems.size();
        }
        if (items != null) {
            count += items.size();
        }
        monitor.beginTask("辞書の更新", count); //$NON-NLS-1$

        /* 代表語削除 */

        try {

            if ((this.delItems != null) && (this.delItems.size() > 0)) {
                for (IDicItem i : this.delItems) {

                    FlucItem fluc = (FlucItem) i;

                    /* トランザクション開始 */
                    session.beginTransaction();

                    int flucId = fluc.getId();

                    // セッションより、主キーを元にデータを取り出す。
                    DicFlucBean insertDf = (DicFlucBean) session.get(DicFlucBean.class, flucId);

                    if (insertDf == null) {
                        insertDf = new DicFlucBean();
                        insertDf.setFlucId(fluc.getId());
                    }

                    insertDf.setDicId(getId());
                    insertDf.setItemId(fluc.getMain().getId());
                    insertDf.setInactive(true);

                    session.save(insertDf);
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();


                    List<FlucSubItem> list = new ArrayList<FlucSubItem>();

                    /* UI上で削除されているかもしれない従属語をリストへ追加 */
                    list.addAll(fluc.getDelSubs());

                    /* 代表語に紐づく従属語をリストへ追加 */
                    for (Entry<Integer, IDependSub> entry : fluc.getSubs().entrySet()) {
                        list.add((FlucSubItem) entry.getValue());
                    }

                    /* 代表語に紐づく従属語のINACTIVEをtrueにする */
                    for (FlucSubItem delSub : list) {

                        int subId = delSub.getId();

                        if (subId < 1) {
                            try {
                                FlucTblBean dt = (FlucTblBean) session.createQuery("from FlucTblBean where id = (select max(id) from FlucTblBean " //$NON-NLS-1$
                                        + "where dicId = :paramDicId and itemId = :paramItemId))").setInteger("paramDicId", getId()) //$NON-NLS-1$ //$NON-NLS-2$
                                        .setInteger("paramItemId", delSub.getTerm().getId()).uniqueResult(); //$NON-NLS-1$
                                if (dt != null) {
                                    subId = dt.getId();
                                }
                            } catch (HibernateException e) {
                                subId = -1;
                                e.printStackTrace();
                            }
                        }

                        /* トランザクション開始 */
                        session.beginTransaction();

                        // セッションより、主キーを元にデータを取り出す。
                        FlucTblBean insertFt = (FlucTblBean) session.get(FlucTblBean.class, subId);

                        if (insertFt == null) {
                            insertFt = new FlucTblBean();
                            insertFt.setId(delSub.getId());
                        }

                        insertFt.setDicId(getId());
                        insertFt.setItemId(delSub.getTerm().getId());
                        insertFt.setInactive(true);

                        session.save(insertFt);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();
                    }
                    monitor.worked(work++);

                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }

        for (IDicItem item : this.items) {

            /* 変更済か、ID無しの場合 */
            if (item.isDirty() || item.getId() == FlucItem.UNSAVED_ID) {

                FlucItem fluc = (FlucItem) item;

                int flucId = fluc.getId();

                try {
                    /* トランザクション開始 */
                    session.beginTransaction();

                    // セッションより、主キーを元にデータを取り出す。
                    DicFlucBean insertDf = (DicFlucBean) session.get(DicFlucBean.class, flucId);

                    /* 代表語追加 */

                    if (insertDf == null) {
                        insertDf = new DicFlucBean();
                        insertDf.setFlucId(flucId);
                    }

                    insertDf.setDicId(getId());
                    insertDf.setItemId(fluc.getMain().getId());
                    insertDf.setInactive(false);

                    session.save(insertDf);
                    session.flush();

                    /* トランザクションコミット */
                    session.getTransaction().commit();

                    DicFlucBean df = (DicFlucBean) session.createQuery("from DicFlucBean where flucId = (select max(flucId) from DicFlucBean " //$NON-NLS-1$
                            + "where dicId = :paramDicId and itemId = :paramItemId))").setInteger("paramDicId", getId()) //$NON-NLS-1$ //$NON-NLS-2$
                            .setInteger("paramItemId", fluc.getMain().getId()).uniqueResult(); //$NON-NLS-1$
                    if (df != null) {
                        fluc.setId(df.getFlucId());
                    } else {
                        fluc.setId(0);
                    }
                } catch (HibernateException e) {
                    fluc.setId(-1);
                    e.printStackTrace();
                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }

                /* 従属語削除 */
                for (FlucSubItem delSub : fluc.getDelSubs()) {

                    int subId = delSub.getId();

                    try {
                        /* トランザクション開始 */
                        session.beginTransaction();

                        FlucTblBean insertFt = (FlucTblBean) session.get(FlucTblBean.class, subId);

                        if (insertFt == null) {
                            insertFt = new FlucTblBean();
                            insertFt.setId(delSub.getId());
                        }

                        // セッションより、主キーを元にデータを取り出す。
                        insertFt.setId(subId);
                        insertFt.setDicId(getId());
                        insertFt.setItemId(delSub.getTerm().getId());
                        insertFt.setInactive(true);

                        session.save(insertFt);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();

                        if (subId < 1) {
                            subId = 0;
                            try {
                                FlucTblBean dt = (FlucTblBean) session.createQuery("from FlucTblBean where id = (select max(id) from FlucTblBean " //$NON-NLS-1$
                                        + "where dicId = :paramDicId and itemId = :paramItemId))").setInteger("paramDicId", getId()) //$NON-NLS-1$ //$NON-NLS-2$
                                        .setInteger("paramItemId", delSub.getTerm().getId()).uniqueResult(); //$NON-NLS-1$
                                if (dt != null) {
                                    subId = dt.getId();
                                }
                            } catch (HibernateException e) {
                                subId = -1;
                                e.printStackTrace();
                            }
                        }

                        if (delSub.isInActive() == false) {

                            /* トランザクション開始 */
                            session.beginTransaction();

                            Serializable pKey = new RelFlucPKBean(subId, fluc.getId());

                            // セッションより、主キーを元にデータを取り出す。
                            RelFlucBean insertRf = (RelFlucBean) session.get(RelFlucBean.class, pKey);

                            if (insertRf == null) {

                                insertRf = new RelFlucBean();
                                insertRf.setPrimaryKey((RelFlucPKBean) pKey);
                            }

                            insertRf.setStatus(delSub.getLevel());

                            session.save(insertRf);
                            session.flush();

                            /* トランザクションコミット */
                            session.getTransaction().commit();
                        }

                    } catch (HibernateException e) {
                        subId = -1;
                        e.printStackTrace();
                    } finally {
                        if (session.getTransaction().isActive()) {
                            /* トランザクションロールバック */
                            session.getTransaction().rollback();
                        }
                    }
                }
                fluc.getDelSubs().clear();

                /* 従属語追加 */
                for (Entry<Integer, IDependSub> entry : fluc.getSubs().entrySet()) {
                    FlucSubItem sub = (FlucSubItem) entry.getValue();
                    int subId = sub.getId();

                    try {

                        /* トランザクション開始 */
                        session.beginTransaction();

                        FlucTblBean insertFt = (FlucTblBean) session.get(FlucTblBean.class, subId);

                        if (insertFt == null) {
                            insertFt = new FlucTblBean();
                            insertFt.setId(sub.getId());
                        }

                        insertFt.setDicId(getId());
                        insertFt.setItemId(sub.getTerm().getId());
                        insertFt.setInactive(sub.isInActive());

                        session.save(insertFt);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();

                        if (subId < 1) {
                            try {
                                FlucTblBean dt = (FlucTblBean) session.createQuery("from FlucTblBean where id = (select max(id) from FlucTblBean " //$NON-NLS-1$
                                        + "where dicId = :paramDicId and itemId = :paramItemId))").setInteger("paramDicId", getId()) //$NON-NLS-1$ //$NON-NLS-2$
                                        .setInteger("paramItemId", sub.getTerm().getId()).uniqueResult(); //$NON-NLS-1$
                                if (dt != null) {
                                    subId = dt.getId();
                                }
                            } catch (HibernateException e) {
                                subId = -1;
                                e.printStackTrace();
                            }
                        }
                        if (sub.isInActive() == false) {

                            /* トランザクション開始 */
                            session.beginTransaction();

                            Serializable pKey = new RelFlucPKBean(subId, fluc.getId());

                            // セッションより、主キーを元にデータを取り出す。
                            RelFlucBean insertRf = (RelFlucBean) session.get(RelFlucBean.class, pKey);

                            if (insertRf == null) {
                                insertRf = new RelFlucBean();
                                insertRf.setPrimaryKey((RelFlucPKBean) pKey);
                            }

                            insertRf.setStatus(sub.getLevel());

                            session.save(insertRf);
                            session.flush();

                            /* トランザクションコミット */
                            session.getTransaction().commit();
                        }
                    } catch (HibernateException e) {
                        subId = -1;
                        e.printStackTrace();
                    } finally {
                        if (session.getTransaction().isActive()) {
                            /* トランザクションロールバック */
                            session.getTransaction().rollback();
                        }
                    }
                    sub.setId(subId);
                    sub.setDirty(false);
                }
                item.setDirty(false);
            }
            monitor.worked(work++);
        }

        if (delItems != null) {
            delItems.clear();
        }
        monitor.done();
        return true;
    }


    @Override
    public boolean update() {
        try {
            Session session = IoService.getInstance().getSession();
            String hql = "from DicTableBean as dt where dt.dicId = :dicIdValue and dt.inactive = false"; //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<DicTableBean> result = session.createQuery(hql).setInteger("dicIdValue", this.getId()).list(); //$NON-NLS-1$
            for (DicTableBean item : result) {
                this._name = item.getDicName();
                this._creationTime = item.getCreationTime();
                this._lasted = item.getDate();
            }
            bRefreshRecords = false;
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean updateRecords() {
        bRefreshRecords = true;
        Map<Integer, IDicItem> tmpMap = new TreeMap<Integer, IDicItem>();
        /* 従属語用一時保管マップ */
        Map<Integer, Map<Integer, IDependSub>> subMapMap = new TreeMap<Integer, Map<Integer, IDependSub>>();
        /* 代表表記情報取得 */
        List<ICoronaDic> parentDics = ((IoService) IoService.getInstance()).getDictionary(getParentIds());

        if (items == null) {
            items = new ArrayList<IDicItem>();
        }

        try {
            String hql = "from RelFlucBean rel, DicFlucBean dic, FlucTblBean sub " //$NON-NLS-1$
                    + "where dic.dicId = sub.dicId and dic.flucId = rel.primaryKey.flucId and rel.primaryKey.id = sub.id and dic.inactive = false and " //$NON-NLS-1$
                    + "sub.inactive = false and dic.dicId = :dicId"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            @SuppressWarnings("unchecked")
            List<Object[]> result = session.createQuery(hql).setInteger("dicId", getId()).list(); //$NON-NLS-1$

            for (Object[] resultItem : result) {
                RelFlucBean relFlucItem = (RelFlucBean) resultItem[0];
                DicFlucBean dicFlucItem = (DicFlucBean) resultItem[1];
                FlucTblBean flucTblItem = (FlucTblBean) resultItem[2];

                int flucId = dicFlucItem.getFlucId();

                FlucItem item = null;

                /* 代表語設定 */
                int itemId = dicFlucItem.getItemId();
                Term main = null;
                if (parentDics.size() > 0) {
                    for (ICoronaDic pdic : parentDics) {
                        if (pdic instanceof IUserDic) {
                            main = (Term) ((UserDic) pdic).getItem(itemId);
                            if (main != null) {
                                break;
                            }
                        }
                    }
                }
                if (main == null) {
                    /* 代表語が取得できなかった場合は、後続の処理をスキップ */
                    continue;
                }

                item = (FlucItem) tmpMap.get(flucId);
                if (item == null) {
                    item = new FlucItem(main);
                    item.setId(flucId);
                    item.setDicId(getId());
                    tmpMap.put(flucId, item);
                }

                /* 従属語設定 */
                int subId = flucTblItem.getId();
                if (subId > 0) {
                    int subItemId = flucTblItem.getItemId();
                    Term term = null;
                    if (parentDics.size() > 0) {
                        for (ICoronaDic pdic : parentDics) {
                            if (pdic instanceof IUserDic) {
                                term = (Term) ((UserDic) pdic).getItem(subItemId);
                                if (term != null) {
                                    break;
                                }
                            }
                        }
                    }
                    if (term == null) {
                        // ERROR
                    }
                    Map<Integer, IDependSub> subMap = subMapMap.get(flucId);
                    if (subMap == null) {
                        subMap = new HashMap<Integer, IDependSub>();
                        subMapMap.put(flucId, subMap);
                        item.setSubs(subMap);
                    }

                    FlucSubItem sub = (FlucSubItem) subMap.get(subItemId);
                    if (sub == null) {
                        sub = new FlucSubItem(subId, term, item);
                    }
                    item.getSubs().put(subItemId, sub);
                    sub.setDicId(item.getComprehensionDicId());
                    sub.setLevel(relFlucItem.getStatus());
                    sub.setDirty(false);
                }
                item.setDirty(false);
            }

            /* Memo モデルのフィールドのListインスタンスは、置き換えしない。 Morishima */
            items.clear();
            items.addAll(tmpMap.values());
            /* 削除用オブジェクトをクリア */
            this.delItems.clear();
            setDirty(false);

            String getDtHql = "from DicTableBean where dicId = :dicId"; //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<DicTableBean> dtList = session.createQuery(getDtHql).setInteger("dicId", this.getId()).list(); //$NON-NLS-1$
            Date updDate = new Date();
            if (dtList != null && dtList.size() > 0) {
                updDate = dtList.get(0).getDate();
            }
            this.setLasted(updDate);
            return true;

        } catch (HibernateException e) {
            bRefreshRecords = false;
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 
     * @param items
     * @param bImpFlg
     */
    public void setItems(List<IDicItem> items, boolean bImpFlg) {
        if (bImpFlg) {
            bRefreshRecords = true;
        }
        if (!bRefreshRecords) {
            updateRecords();
            bRefreshRecords = true;
        }
        super.setItems(items);
    }


    @Override
    public int getItemCount() {
        if (items == null) {
            try {
                Session session = IoService.getInstance().getSession();
                String hql = "select count(*) from DicFlucBean where dicId= :dicId and inactive=:inactive"; //$NON-NLS-1$
                Object count = session.createQuery(hql).setInteger("dicId", getId()).setBoolean("inactive", false).uniqueResult(); //$NON-NLS-1$ //$NON-NLS-2$
                return count == null ? 0 : Integer.parseInt(count.toString());
            } catch (HibernateException e) {
                e.printStackTrace();
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
     * アイテム取得
     * 
     * @param id
     * @return ITerm
     */
    @Override
    public IDicItem getItem(int id) {
        if (!bRefreshRecords || (itemsForSearch == null)) {
            itemsForSearch = new TreeMap<Integer, IDicItem>();
            List<IDicItem> items = getItems();
            for (IDicItem item : items) {
                itemsForSearch.put(item.getId(), item);
            }
        }
        return itemsForSearch.get(id);
    }


    @Deprecated
    @Override
    protected void importDicDam(String path) {
    }
}
