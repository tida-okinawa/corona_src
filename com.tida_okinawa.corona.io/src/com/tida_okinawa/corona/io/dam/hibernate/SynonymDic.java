/**
 * @version $Id: SynonymDic.java 997 2013-06-05 01:58:11Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author yukihiro-kinjo
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

import com.tida_okinawa.corona.io.bean.DicSynonymBean;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.bean.RelSynonymBean;
import com.tida_okinawa.corona.io.bean.RelSynonymPKBean;
import com.tida_okinawa.corona.io.bean.SynonymTblBean;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDependSub;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.abstraction.AbstractSynonymDic;
import com.tida_okinawa.corona.io.model.dic.impl.SynonymItem;
import com.tida_okinawa.corona.io.model.dic.impl.SynonymSubItem;
import com.tida_okinawa.corona.io.model.dic.impl.Term;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * @author yukihiro-kinjo
 * 
 */
public final class SynonymDic extends AbstractSynonymDic {
    /**
     * アイテムの更新を行ったかどうか
     */
    private boolean bRefreshRecords = false;


    /**
     * @param id
     * @param name
     * @param parentId
     */
    public SynonymDic(int id, String name, int parentId) {
        super(id, name, null);
        this.addParentId(parentId);
    }


    /**
     * @param id
     *            ID
     * @param name
     *            辞書名
     * @param lasted
     *            更新日時
     * @param parentId
     *            親辞書ID
     */
    public SynonymDic(int id, String name, Date lasted, Set<Integer> parentId) {
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
        if ((this.delItems != null) && (this.delItems.size() > 0)) {
            for (IDicItem i : this.delItems) {

                SynonymItem synonym = (SynonymItem) i;

                try {

                    session.beginTransaction();

                    // セッションより、主キーを元にデータを取り出す。
                    DicSynonymBean insertDs = (DicSynonymBean) session.get(DicSynonymBean.class, synonym.getId());

                    if (insertDs == null) {
                        insertDs = new DicSynonymBean();
                        insertDs.setSynonymId(synonym.getId());
                    }

                    insertDs.setDicId(getId());
                    insertDs.setItemId(synonym.getMain().getId());
                    insertDs.setInactive(true);

                    session.save(insertDs);
                    session.flush();

                    session.getTransaction().commit();

                } catch (HibernateException e) {
                    e.printStackTrace();

                } finally {
                    if (session.getTransaction().isActive()) {
                        session.getTransaction().rollback();
                    }
                }

                try {


                    List<SynonymSubItem> list = new ArrayList<SynonymSubItem>();
                    /* UI上で削除されているかもしれない従属語をリストへ追加 */
                    list.addAll(synonym.getDelSubs());

                    /* 代表語に紐づく従属語をリストへ追加 */
                    for (Entry<Integer, IDependSub> entry : synonym.getSubs().entrySet()) {
                        list.add((SynonymSubItem) entry.getValue());
                    }
                    /* 代表語に紐づく従属語のINACTIVEをtrueにする */
                    for (SynonymSubItem delSub : list) {

                        session.beginTransaction();

                        // セッションより、主キーを元にデータを取り出す。
                        SynonymTblBean insertSt = (SynonymTblBean) session.get(SynonymTblBean.class, delSub.getId());

                        if (insertSt == null) {
                            insertSt = new SynonymTblBean();
                            insertSt.setId(delSub.getId());
                        }

                        insertSt.setDicId(getId());
                        insertSt.setItemId(delSub.getTerm().getId());
                        insertSt.setInactive(true);

                        session.save(insertSt);
                        session.flush();

                        session.getTransaction().commit();

                    }

                } catch (HibernateException e) {
                    e.printStackTrace();

                } finally {
                    if (session.getTransaction().isActive()) {
                        session.getTransaction().rollback();
                    }
                }
                monitor.worked(work++);
            }
        }

        for (IDicItem item : this.items) {
            /* 変更済か、ID無しの場合 */
            if (item.isDirty() || item.getId() == SynonymItem.UNSAVED_ID) {

                SynonymItem synonym = (SynonymItem) item;

                try {
                    session.beginTransaction();

                    /* 代表語追加 */
                    DicSynonymBean insertDs = (DicSynonymBean) session.get(DicSynonymBean.class, synonym.getId());

                    if (insertDs == null) {
                        insertDs = new DicSynonymBean();
                        insertDs.setSynonymId(synonym.getId());
                    }

                    insertDs.setDicId(getId());
                    insertDs.setItemId(synonym.getMain().getId());
                    insertDs.setInactive(false);

                    session.save(insertDs);
                    session.flush();

                    session.getTransaction().commit();

                    DicSynonymBean ds = (DicSynonymBean) session
                            .createQuery("from DicSynonymBean where synonymId = (select max(synonymId) from DicSynonymBean " //$NON-NLS-1$
                                    + "where dicId = :paramDicId and itemId = :paramItemId))").setInteger("paramDicId", getId()) //$NON-NLS-1$ //$NON-NLS-2$
                            .setInteger("paramItemId", synonym.getMain().getId()).uniqueResult(); //$NON-NLS-1$
                    if (ds != null) {
                        synonym.setId(ds.getSynonymId());
                    } else {
                        synonym.setId(0);
                    }
                } catch (HibernateException e) {
                    synonym.setId(-1);
                    e.printStackTrace();
                } finally {
                    if (session.getTransaction().isActive()) {
                        session.getTransaction().rollback();
                    }
                }

                /* 従属語削除 */
                for (SynonymSubItem delSub : synonym.getDelSubs()) {

                    int subId = delSub.getId();

                    try {
                        session.beginTransaction();


                        SynonymTblBean insertSt = (SynonymTblBean) session.get(SynonymTblBean.class, subId);

                        if (insertSt == null) {
                            insertSt = new SynonymTblBean();
                            insertSt.setId(delSub.getId());

                        }
                        insertSt.setDicId(getId());
                        insertSt.setItemId(delSub.getTerm().getId());
                        insertSt.setInactive(true);

                        session.save(insertSt);
                        session.flush();

                        session.getTransaction().commit();


                    } catch (HibernateException e) {
                        e.printStackTrace();

                    } finally {
                        if (session.getTransaction().isActive()) {
                            session.getTransaction().rollback();
                        }
                    }

                    if (subId < 1) {
                        subId = 0;
                        try {
                            SynonymTblBean dt = (SynonymTblBean) session.createQuery("from SynonymTblBean where id = (select max(id) from SynonymTblBean " //$NON-NLS-1$
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

                    try {

                        if (delSub.isInActive() == false) {

                            session.beginTransaction();

                            Serializable pKey = new RelSynonymPKBean(subId, synonym.getId());

                            // セッションより、主キーを元にデータを取り出す。
                            RelSynonymBean insertRs = (RelSynonymBean) session.get(RelSynonymBean.class, pKey);

                            if (insertRs == null) {
                                insertRs = new RelSynonymBean();
                                insertRs.setPrimaryKey((RelSynonymPKBean) pKey);
                            }

                            insertRs.setStatus(delSub.getLevel());

                            session.save(insertRs);
                            session.flush();

                            session.getTransaction().commit();

                        }
                    } catch (HibernateException e) {
                        subId = -1;
                        e.printStackTrace();

                    } finally {
                        if (session.getTransaction().isActive()) {
                            session.getTransaction().rollback();
                        }
                    }
                }
                synonym.getDelSubs().clear();

                /* 従属語追加 */
                for (Entry<Integer, IDependSub> entry : synonym.getSubs().entrySet()) {
                    SynonymSubItem sub = (SynonymSubItem) entry.getValue();
                    int subId = sub.getId();

                    try {

                        session.beginTransaction();

                        SynonymTblBean insertSt = (SynonymTblBean) session.get(SynonymTblBean.class, subId);

                        if (insertSt == null) {
                            insertSt = new SynonymTblBean();
                            insertSt.setId(sub.getId());
                        }

                        insertSt.setDicId(getId());
                        insertSt.setItemId(sub.getTerm().getId());
                        insertSt.setInactive(sub.isInActive());

                        session.save(insertSt);
                        session.flush();

                        session.getTransaction().commit();


                    } catch (HibernateException e) {
                        e.printStackTrace();

                    } finally {
                        if (session.getTransaction().isActive()) {
                            session.getTransaction().rollback();
                        }
                    }

                    if (subId < 1) {
                        try {
                            SynonymTblBean dt = (SynonymTblBean) session.createQuery("from SynonymTblBean where id = (select max(id) from SynonymTblBean " //$NON-NLS-1$
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

                    try {

                        if (sub.isInActive() == false) {

                            session.beginTransaction();

                            Serializable pKey = new RelSynonymPKBean(subId, synonym.getId());

                            RelSynonymBean insertRs = (RelSynonymBean) session.get(RelSynonymBean.class, pKey);

                            if (insertRs == null) {
                                insertRs = new RelSynonymBean();
                                insertRs.setPrimaryKey((RelSynonymPKBean) pKey);
                            }

                            insertRs.setStatus(sub.getLevel());

                            session.save(insertRs);
                            session.flush();

                            session.getTransaction().commit();

                        }
                    } catch (HibernateException e) {
                        subId = -1;
                        e.printStackTrace();
                    } finally {
                        if (session.getTransaction().isActive()) {
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
            String hql = "from RelSynonymBean rel, DicSynonymBean dic, SynonymTblBean sub " //$NON-NLS-1$
                    + "where dic.dicId = sub.dicId and dic.synonymId = rel.primaryKey.synonymId and rel.primaryKey.id = sub.id and dic.inactive = false and " //$NON-NLS-1$
                    + "sub.inactive = false and dic.dicId = :dicId"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            @SuppressWarnings("unchecked")
            List<Object[]> result = session.createQuery(hql).setInteger("dicId", getId()).list(); //$NON-NLS-1$

            for (Object[] resultItem : result) {
                RelSynonymBean relSynonymItem = (RelSynonymBean) resultItem[0];
                DicSynonymBean dicSynonymItem = (DicSynonymBean) resultItem[1];
                SynonymTblBean synonymTblItem = (SynonymTblBean) resultItem[2];

                int synonymId = dicSynonymItem.getSynonymId();

                SynonymItem item = null;

                /* 代表語設定 */
                int itemId = dicSynonymItem.getItemId();
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

                item = (SynonymItem) tmpMap.get(synonymId);
                if (item == null) {
                    item = new SynonymItem(main);
                    item.setId(synonymId);
                    item.setDicId(getId());
                    tmpMap.put(synonymId, item);
                }

                /* 従属語設定 */
                int subId = synonymTblItem.getId();
                if (subId > 0) {
                    int subItemId = synonymTblItem.getItemId();
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
                    Map<Integer, IDependSub> subMap = subMapMap.get(synonymId);
                    if (subMap == null) {
                        subMap = new HashMap<Integer, IDependSub>();
                        subMapMap.put(synonymId, subMap);
                        item.setSubs(subMap);
                    }

                    SynonymSubItem sub = (SynonymSubItem) subMap.get(subItemId);
                    if (sub == null) {
                        sub = new SynonymSubItem(subId, term, item);
                    }
                    item.getSubs().put(subItemId, sub);
                    sub.setDicId(item.getComprehensionDicId());
                    sub.setLevel(relSynonymItem.getStatus());
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


    @Override
    public void setItems(List<IDicItem> items) {
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
                String hql = "select count(*) from DicSynonymBean where dicId= :dicId and inactive=:inactive"; //$NON-NLS-1$
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


    /**
     * 外部辞書インポート
     * 
     * @param path
     * 
     */
    @Deprecated
    @Override
    protected void importDicDam(String path) {
    }
}
