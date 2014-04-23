/**
 * @version $Id: LabelDic.java 997 2013-10-28 11:44:11Z yukihiro-kinjyo $
 *
 * 2013/10/28
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.bean.DicLabelBean;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.bean.LabelTreeBean;
import com.tida_okinawa.corona.io.bean.LabelTreePKBean;
import com.tida_okinawa.corona.io.bean.RelCommonLabelBean;
import com.tida_okinawa.corona.io.dam.hibernate.hql.CommonCreateQuery;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.abstraction.AbstractLabelDic;
import com.tida_okinawa.corona.io.model.dic.impl.LabelItem;
import com.tida_okinawa.corona.io.model.dic.impl.Term;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * ラベル辞書実装クラス
 * 
 * @author yukihiro-kinjyo
 * 
 */
public final class LabelDic extends AbstractLabelDic {
    static final int INSERT = 1;
    static final int DELETE = 2;

    /**
     * アイテムの更新を行ったかどうかのフラグ
     */
    private boolean bRefreshRecords = false;


    /**
     * @param id
     * @param name
     * @param lasted
     */
    public LabelDic(int id, String name, Date lasted) {
        super(id, name, lasted);
    }


    /**
     * @param id
     * @param name
     * @param lasted
     * @param parentId
     */
    public LabelDic(int id, String name, Date lasted, Set<Integer> parentId) {
        super(id, name, lasted, parentId);
    }


    /**
     * ラベルの作成
     * 
     * @param list
     * @return 処理が完了するとTrueが返る
     */
    public boolean commitLabel(List<ILabel> list) {

        Session session = IoService.getInstance().getSession();

        boolean result = true;

        for (ILabel item : list) {
            if (item.isDirty() || item.getId() == IDicItem.UNSAVED_ID) {

                try {

                    /* トランザクション開始 */
                    session.beginTransaction();

                    DicLabelBean bean;
                    if (item.getId() == IDicItem.UNSAVED_ID) {
                        // 新規登録の場合
                        bean = new DicLabelBean();
                        bean.setDicId(getId());
                    } else {
                        // ラベル名変更の場合
                        bean = (DicLabelBean) CommonCreateQuery.getDicLabelQuery(item.getId()).uniqueResult();
                    }
                    if (bean != null) {

                        bean.setLabelName(item.getName());
                        bean.setInactive(false);
                        session.save(bean);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();
                    } else {
                        result = false;
                    }
                    item.setId(bean.getLabelId());
                    item.setDirty(false);
                    /* 子供チェック */
                    if (item.getChildren().size() > 0) {
                        commitLabel(item.getChildren());
                    }

                } catch (HibernateException e) {
                    throw e;

                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }
            }
        }
        return result;
    }


    /**
     * ラベル削除
     * 
     * @param list
     * @return 処理が完了するとTrueが返る
     */
    public boolean deleteLabel(List<ILabel> list) throws HibernateException {
        Session session = IoService.getInstance().getSession();

        for (ILabel item : list) {
            boolean bflg = false;
            /*
             * ラベルを削除 (INACTIVE=trueに設定) ただし、label_id =
             * 0のものは登録前に削除されているので削除処理を回避する
             */
            if (item.getId() != IDicItem.UNSAVED_ID) {
                try {
                    DicLabelBean dicLabelBean = (DicLabelBean) session.get(DicLabelBean.class, item.getId());
                    if (dicLabelBean != null) {

                        /* トランザクション開始 */
                        session.beginTransaction();

                        dicLabelBean.setInactive(true);
                        session.save(dicLabelBean);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();

                        bflg = true;
                    }
                } catch (HibernateException e) {
                    throw e;

                } finally {
                    if (session.getTransaction().isActive()) {
                        /* トランザクションロールバック */
                        session.getTransaction().rollback();
                    }
                }

                if (bflg) {
                    /* 用語とのリレーションを削除 */
                    List<ITerm> terms = item.getTerms();
                    StringBuilder strId = new StringBuilder(100);
                    if (terms.size() > 0) {
                        for (ITerm term : terms) {
                            if (term != null) {
                                strId.append(",").append(term.getId()); //$NON-NLS-1$
                            }
                        }
                        /* 紐づけを削除 */
                        /* DicLabelDao.deleteRelCommonLabelを置換する。 */
                        try {
                            /* トランザクション開始 */
                            session.beginTransaction();

                            String strTermId = strId.substring(1);
                            String strSQL = "DELETE FROM REL_COMMON_LABEL WHERE LABEL_ID = " + item.getId(); //$NON-NLS-1$
                            if (strTermId.length() > 0) {
                                strSQL = strSQL + " AND ITEM_ID IN (" + strTermId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            session.createSQLQuery(strSQL).executeUpdate();
                            session.flush();

                            /* トランザクションコミット */
                            session.getTransaction().commit();
                        } catch (HibernateException e) {
                            throw e;
                        } finally {
                            if (session.getTransaction().isActive()) {
                                /* トランザクションロールバック */
                                session.getTransaction().rollback();
                            }
                        }
                    }
                }
                /* 子供チェック */
                if (item.getChildren().size() > 0) {
                    deleteLabel(item.getChildren());
                }
            }
        }
        return true;
    }


    @Override
    protected boolean doCommit(boolean bRecords, IProgressMonitor monitor) {

        Session session = IoService.getInstance().getSession();

        try {
            /* 辞書情報の更新 */
            // dic_tableの更新
            DicTableBean dicTable = (DicTableBean) CommonCreateQuery.getDicTableQuery(this.getId()).uniqueResult();
            if (dicTable != null) {

                /* トランザクション開始 */
                session.beginTransaction();

                dicTable.setDicName(this.getName());
                dicTable.setParentId(CoronaIoUtils.intListToString(this.getParentIds()));
                session.save(dicTable);
                session.flush();

                /* トランザクションコミット */
                session.getTransaction().commit();

            } else {
                return false;
            }

            if (!bRecords)
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
        try {
            List<ILabel> tmpList = new ArrayList<ILabel>();
            List<ILabel> tmpDelList = new ArrayList<ILabel>();

            if (getItems().size() > 0) {
                tmpList.addAll(getLabelsRecursive(getLabels()));
            }
            monitor.beginTask("辞書の更新", 4); //$NON-NLS-1$

            boolean result;
            result = commitLabel(getLabels());

            if (!result)
                return result;

            /* ラベルに紐付く用語をコミット */
            /* DicLabelDao.insertRecords を置換する。 */
            for (ILabel label : tmpList) {
                for (ITerm term : label.getTerms()) {
                    RelCommonLabelBean relComLabel = (RelCommonLabelBean) CommonCreateQuery.getRelCommonLabelQuery(label.getId(), getId(), term.getId())
                            .uniqueResult();
                    // rel_common_labelに無いラベルのみInsert
                    if (relComLabel == null) {

                        /* トランザクション開始 */
                        session.beginTransaction();

                        relComLabel = new RelCommonLabelBean();
                        relComLabel.setLabelId(label.getId());
                        relComLabel.setDicId(getId());
                        relComLabel.setItemId(term.getId());
                        relComLabel.setValue(0);
                        relComLabel.setMathFlag(false);
                        session.save(relComLabel);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();

                        relComLabel = (RelCommonLabelBean) CommonCreateQuery.getRelCommonLabelQuery(label.getId(), getId(), term.getId()).uniqueResult();
                        if (relComLabel == null) {
                            result = false;
                        }
                    } else {
                        // 「ON DUPLICATE KEY　．．．」に対応（UPDATE）
                        /* トランザクション開始 */
                        session.beginTransaction();

                        relComLabel.setValue(0);
                        relComLabel.setMathFlag(false);
                        session.save(relComLabel);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();
                    }
                }
            }

            if (!result)
                return result;
            monitor.worked(1);

            /* ラベルツリー構造をコミット */
            for (ILabel label : tmpList) {
                if (label.getChildren() == null) {
                    continue;
                }
                for (ILabel child : label.getChildren()) {
                    LabelTreePKBean pk = new LabelTreePKBean();
                    pk.setParentId(label.getId());
                    pk.setChildId(child.getId());
                    LabelTreeBean labelTree = (LabelTreeBean) session.get(LabelTreeBean.class, pk);

                    if (labelTree == null) {

                        /* トランザクション開始 */
                        session.beginTransaction();

                        labelTree = new LabelTreeBean();
                        labelTree.setPrimaryKeyBean(new LabelTreePKBean());
                        labelTree.getPrimaryKeyBean().setParentId(label.getId());
                        labelTree.getPrimaryKeyBean().setChildId(child.getId());
                        session.save(labelTree);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();

                    }
                }
            }
            if (!result)
                return result;
            monitor.worked(1);

            /* ラベルの紐付けを外した用語のリレーションを削除 */
            result = deleteRelCommon(tmpList);
            monitor.worked(1);

            /* 削除対象のデータをコミット */
            if (delItems.size() > 0) {
                for (IDicItem item : delItems) {
                    if (item != null) {
                        tmpDelList.add((ILabel) item);
                    }
                }
                result = deleteLabel(tmpDelList);
            }
            monitor.worked(1);

            /* 検索用Map作成 */
            itemsForSearch.clear();
            for (ILabel item : getLabelsRecursive(getLabels())) {
                itemsForSearch.put(item.getId(), item);
            }

            delItems.clear();
            monitor.done();
            return result;
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
     * リレーション削除
     * 
     * @param list
     * @return boolean
     */
    private static boolean deleteRelCommon(List<ILabel> list) throws HibernateException {
        boolean result = true;
        try {
            for (ILabel label : list) {
                /* 用語とのリレーションを削除 */
                List<ITerm> terms = ((LabelItem) label).getDelTerms();
                StringBuilder strId = new StringBuilder(100);
                if (terms.size() > 0) {
                    for (ITerm term : terms) {
                        if (term != null) {
                            strId.append(",").append(term.getId()); //$NON-NLS-1$
                        }
                    }
                    /* 紐づけを削除 */
                    String strSQL = "DELETE FROM REL_COMMON_LABEL WHERE LABEL_ID = " + label.getId(); //$NON-NLS-1$
                    if (strId.substring(1).length() > 0) {
                        strSQL = strSQL + " AND ITEM_ID IN (" + strId.substring(1) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    /* トランザクション開始 */
                    Session session = IoService.getInstance().getSession();
                    session.beginTransaction();
                    session.createSQLQuery(strSQL).executeUpdate();
                    session.flush();
                    /* トランザクションコミット */
                    session.getTransaction().commit();
                    ((LabelItem) label).clearDelTerms();
                }
            }
        } catch (HibernateException e) {
            throw e;
        } finally {
            if (IoService.getInstance().getSession().getTransaction().isActive()) {
                /* トランザクションロールバック */
                IoService.getInstance().getSession().getTransaction().rollback();
            }
        }

        return result;
    }


    /**
     * ラベルリストを取得
     * 
     * @param list
     */
    @Override
    public List<ILabel> getLabelsRecursive(List<ILabel> list) {
        List<ILabel> outList = new ArrayList<ILabel>();
        for (ILabel item : list) {
            outList.add(item);

            /* 子供チェック */
            if (item.getChildren().size() > 0) {
                outList.addAll(getLabelsRecursive(item.getChildren()));
            }
        }
        return outList;
    }


    /**
     * 辞書情報の更新
     * 
     * @return 情報取得したらtrue、失敗したらfalse
     */
    public boolean updateDicInformation() {
        try {
            DicTableBean dicTable = (DicTableBean) CommonCreateQuery.getDicTableQuery(this.getId()).uniqueResult();
            if (dicTable != null) {
                if (!dicTable.isInactive()) {
                    this._name = dicTable.getDicName();
                    this._creationTime = dicTable.getCreationTime();
                    this._lasted = dicTable.getDate();
                    this.setParentIds(CoronaIoUtils.stringToIntSet(dicTable.getParentId()));
                    this.setDirty(false);
                }
            } else {
                /* 更新対象が無い場合、失敗と判定（jdbc版では成功としている） */
                return false;
            }
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean update() {
        updateDicInformation();
        bRefreshRecords = false;
        return true;
    }


    @Override
    public boolean updateRecords() {
        try {
            // 取得した結果のインデックス
            int labelIdNum = 0;
            int dicIdNum = 1;
            int labelNameNum = 2;
            int parentIdNum = 4;

            bRefreshRecords = true;
            if (items == null) {
                items = new ArrayList<IDicItem>();
                itemsForSearch = new TreeMap<Integer, IDicItem>();
            } else {
                items.clear();
            }
            delItems.clear(); /* itemsが更新されるので、削除マークアイテムはクリアされるべき by Morishima */

            /*
             * 最後までここに残っていたものは、削除されたアイテム DBを複数人で使う場合以外には効力を発揮しないと思われる
             */
            List<IDicItem> deletedItems = new ArrayList<IDicItem>(itemsForSearch.values());
            @SuppressWarnings("unchecked")
            List<Object[]> resultList = CommonCreateQuery.getDicLabelJoinLabelTreeQuery(getId()).list();
            /* 取得結果編集 */
            for (Object[] result : resultList) {
                int labelId = result[labelIdNum] == null ? 0 : Integer.parseInt(result[labelIdNum].toString());
                ILabel newLabel = findItem(labelId);
                if (newLabel == null) {
                    newLabel = DicFactory.getInstance().createLabel((String) result[labelNameNum], null);
                    newLabel.setId(labelId);
                    itemsForSearch.put(newLabel.getId(), newLabel);
                } else {
                    newLabel.setName((String) result[labelNameNum]);
                    newLabel.getChildren().clear();
                    deletedItems.remove(newLabel);
                }
                // 複数親は持たない
                ((LabelItem) newLabel).setParentId(result[parentIdNum] == null ? 0 : Integer.parseInt(result[parentIdNum].toString()));
                ((LabelItem) newLabel).setDicId((int) result[dicIdNum]);
                newLabel.setDirty(false); // updateLabelTermでもやっているから冗長ではある
            }

            for (IDicItem label : deletedItems) {
                itemsForSearch.remove(label.getId());
            }

            /* 親子関係の帳尻を合わせる */
            for (IDicItem item : itemsForSearch.values()) {
                /* 自身の親をマッピング */
                LabelItem label = (LabelItem) item;
                ILabel parent = findItem(label.getParentId());

                if (parent == null) {
                    items.add(item);
                } else {
                    label.setParent(parent);
                }
                item.setDirty(false);
            }
            /* 用語を更新 */
            updateLabelTerms();
            this.setDirty(false);

            /* 更新日を最新へ設定 */
            @SuppressWarnings("unchecked")
            List<DicTableBean> dicTableList = CommonCreateQuery.getDicTableQuery(getId()).list();
            if (dicTableList != null) {
                for (DicTableBean dicTable : dicTableList) {
                    this.setLasted(dicTable.getDate());
                }
            }

            return true;
        } catch (HibernateException e) {
            bRefreshRecords = false;
            e.printStackTrace();
            return false;
        }
    }


    private boolean updateLabelTerms() {
        /* 代表表記情報取得 */
        List<ICoronaDic> parentDics = ((IoService) IoService.getInstance()).getDictionary(getParentIds());

        try {
            String para1 = "DIC_ID"; //$NON-NLS-1$
            String hql = "FROM RelCommonLabelBean AS a, DicCommonBean AS b WHERE a.dicId=:"; //$NON-NLS-1$
            hql += para1 + " AND a.itemId = b.itemId AND b.inactive = false"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();

            @SuppressWarnings("unchecked")
            List<Object[]> relComLabelList = session.createQuery(hql).setInteger(para1, getId()).list();

            /* データ取得処理でエラー発生でない場合に、以下が実施される。 */

            /* DBに保存されていない「ラベル-単語」のひもづけを消すためのSet */
            for (Entry<Integer, IDicItem> e : itemsForSearch.entrySet()) {
                ((ILabel) e.getValue()).getTerms().clear();
            }

            for (Object[] rs : relComLabelList) {
                RelCommonLabelBean relComLabel = (RelCommonLabelBean) rs[0];
                ILabel label = findItem(relComLabel.getLabelId());
                if (label != null) {

                    int itemId = relComLabel.getItemId();
                    Term term = null;
                    for (ICoronaDic pdic : parentDics) {
                        if (pdic instanceof IUserDic) {
                            term = (Term) ((UserDic) pdic).getItem(itemId);
                            if (term != null) {
                                break;
                            }
                        }
                    }
                    if (term != null) {
                        label.addTerm(term);
                    }
                }
            }
            for (Entry<Integer, IDicItem> e : itemsForSearch.entrySet()) {
                e.getValue().setDirty(false);
            }
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public int getItemCount() {
        if (items == null) {
            try {
                // DicCommonDao.getItemCount の機能を移設
                String hql = "Select count(*) From DicLabelBean where dicId = :DIC_ID and inactive = false "; //$NON-NLS-1$
                Object rs = IoService.getInstance().getSession().createQuery(hql).setInteger("DIC_ID", getId()).uniqueResult(); //$NON-NLS-1$
                int ret = rs == null ? 0 : Integer.parseInt(rs.toString());
                return ret;
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
    public List<ILabel> getLabels() {
        List<ILabel> list = new ArrayList<ILabel>();
        if (!bRefreshRecords || (items == null)) {
            updateRecords();
        }
        for (IDicItem item : items) {
            list.add((ILabel) item);
        }
        return list;
    }


    @Override
    public void addItem(IDicItem item) {
        if (!bRefreshRecords || (items == null)) {
            updateRecords();
        }
        super.addItem(item);
    }


    @Override
    public void removeItem(IDicItem item) {
        if (!bRefreshRecords || (items == null)) {
            updateRecords();
        }
        super.removeItem(item);
    }


    /**
     * ラベルを検索
     * 
     * @param itemId
     * @return itemIdに一致するラベルアイテムを返す
     */
    private ILabel findItem(int itemId) {
        if (itemId <= IDicItem.UNSAVED_ID) {
            return null;
        }
        return (ILabel) itemsForSearch.get(itemId);
    }


    /**
     * ラベルアイテムをセットする
     * 
     * @param items
     * @param bImpFlg
     */
    public void setItems(List<IDicItem> items, boolean bImpFlg) {
        /*
         * TODO どうせitemsで上書きされるんだから、updateRecordsする意味はない.
         * そして上書き後、flatLabelMapの更新、delItemsのクリアはしないといけない
         * 現状では、このメソッドの直後にdoCommitが呼ばれ、そこで処理されているので何もしない
         */
        bRefreshRecords = true;
        super.setItems(items);
    }


    /**
     * アイテム取得
     * 
     * @param id
     * @return ITerm
     */
    @Override
    public IDicItem getItem(int id) {
        if (!bRefreshRecords) {
            updateRecords();
        }
        return itemsForSearch.get(id);
    }


    @Override
    public boolean isDicRelationDirty(int dicId) {
        List<ILabel> tmpList = new ArrayList<ILabel>();
        if (getItems().size() > 0) {
            tmpList.addAll(getLabelsRecursive(getLabels()));
        }
        for (ILabel label : tmpList) {
            LabelItem item = (LabelItem) label;
            if (label.isDirty()) {
                /* 追加のチェック */
                for (ITerm term : label.getTerms()) {
                    if (term.getComprehensionDicId() == dicId) {
                        return true;
                    }
                }
                /* 削除のチェック */
                for (ITerm term : item.getDelTerms()) {
                    if (term.getComprehensionDicId() == dicId) {
                        return true;
                    }
                }
            }
        }
        /* 削除ラベルのチェック */
        for (IDicItem item : delItems) {
            ILabel label = (ILabel) item;
            for (ITerm term : label.getTerms()) {
                if (term.getComprehensionDicId() == dicId) {
                    return true;
                }
            }
        }
        return false;
    }
}
