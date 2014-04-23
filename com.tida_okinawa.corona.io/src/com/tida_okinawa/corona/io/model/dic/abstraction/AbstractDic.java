/**
 * @version $Id: AbstractDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.abstraction;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.PropertyUtil;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.exception.CoronaError;
import com.tida_okinawa.corona.io.model.abstraction.CoronaObject;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDependDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;


/**
 * @author shingo-takahashi
 * 
 */
public abstract class AbstractDic extends CoronaObject implements ICoronaDic {

    protected int _id = UNSAVED_ID;

    protected String _name;

    protected Date _lasted = null;

    protected Date _creationTime;

    private boolean dirty = false;

    // TODO 更新フラグをここに持ってくる。subclassで個別に持っているし、UserDicでは使ってすらいないので統一する
    // /**
    // * アイテムの更新を行ったかどうか
    // */
    // protected boolean bRefreshRecords = false;

    /**
     * {@link IDicItem#getId()}, IDicItemのマップ。検索用。
     */
    protected Map<Integer, IDicItem> itemsForSearch;

    protected Set<Integer> parentDics = new HashSet<Integer>();

    protected List<CoronaError> errors = new ArrayList<CoronaError>();

    private Set<IDicItem> dirtyItems;


    /**
     * 辞書クラスのコンストラクタ
     * 
     * @param id
     *            辞書ID
     * @param name
     *            辞書名
     * @param lasted
     *            最終更新日時
     */
    public AbstractDic(int id, String name, Date lasted) {
        this._id = id;
        this._name = name;
        if (lasted != null) {
            this._lasted = (Date) lasted.clone();
            _creationTime = (Date) lasted.clone();// TODO:ダミー設定
        }
        this.dirtyItems = new HashSet<IDicItem>();
    }


    /**
     * 辞書クラスのコンストラクタ
     * 
     * @param id
     *            辞書ID
     * @param name
     *            辞書名
     * @param lasted
     *            最終更新日時
     * @param parents
     *            親辞書のID
     */
    public AbstractDic(int id, String name, Date lasted, Set<Integer> parents) {
        this(id, name, lasted);
        this.parentDics = parents;
    }


    @Override
    public void setId(int id) {
        this._id = id;
    }


    @Override
    public int getId() {
        return _id;
    }


    @Override
    public String getName() {
        return _name;
    }


    @Override
    public void setName(String name) {
        if ((name == null) || _name.equals(name)) {
            return;
        }
        this._name = name;
        // setDirty(true); // TODO 名前でdirtyをつける必要があるのか？
    }


    @Override
    public Date getLasted() {
        return (Date) _lasted.clone();
    }


    @Override
    public void setLasted(Date date) {
        if (date != null) {
            this._lasted = (Date) date.clone();
        } else {
            this._lasted = null;
        }
    }


    @Override
    public Date getCreationTime() {
        if (_creationTime != null) {
            return (Date) _creationTime.clone();
        }
        return null;
    }


    @Override
    public void setCreationTime(Date date) {
        if (date != null) {
            _creationTime = (Date) date.clone();
        } else {
            _creationTime = null;
        }
    }


    // TODO 将来的には、getItemをここに持ってきたい。
    // @Override
    // public IDicItem getItem(int id) {
    // if (!bRefreshRecords || (itemsForSearch == null)) {
    // itemsForSearch = new HashMap<Integer, IDicItem>();
    // List<IDicItem> items = getItems();
    // for (IDicItem item : items) {
    // itemsForSearch.put(item.getId(), item);
    // }
    // }
    //
    // return itemsForSearch.get(id);
    // }

    @Override
    public void addParentId(int id) {
        int pId = UNSAVED_ID;
        for (int i : this.parentDics) {
            if (i == id) {
                /* 既にparentDics内に存在する場合ははじく */
                pId = id;
                break;
            }
        }
        if (pId == UNSAVED_ID) {
            this.parentDics.add(id);
        }
        // setDirty(true); // Memo プロパティ的な変更なので、dirtyは立てない。立てるとすれば、propDirty的な
        // */
    }


    @Override
    public void removeParentId(int id) {
        parentDics.remove(id);
    }


    @Override
    public int getParentId() {
        if (parentDics.size() > 0) {
            return (Integer) parentDics.toArray()[0];
        }
        return -1; // 親なし
    }


    @Override
    public void setParentId(int id) {
        this.parentDics.clear();
        this.parentDics.add(id);
    }


    @Override
    public Set<Integer> getParentIds() {
        return parentDics;
    }


    @Override
    public void setParentIds(Set<Integer> id) {
        this.parentDics = id;
    }


    @Override
    public boolean isDirty() {
        return dirty;
    }


    @Override
    public void setDirty(boolean dirty) {
        if (this.dirty == dirty) {
            return;
        }
        this.dirty = dirty;
        if (!dirty) {
            dirtyItems.clear();
        }
    }


    public void addDirty(IDicItem dirtyItem) {
        dirtyItems.add(dirtyItem);
        setDirty(true);
    }


    public void removeDirty(IDicItem dirtyItem) {
        dirtyItems.remove(dirtyItem);
        if (dirtyItems.size() == 0) {
            setDirty(false);
        }
    }


    @Override
    public List<?> getItemsPaging(int page, int limit) {
        // Memo ユーザ辞書以外では、ページ取得に対応していないため、すべて返す
        return getItems();
    }


    @Override
    public boolean commit(IProgressMonitor monitor) {
        return commit(true, monitor);
    }


    @Override
    public boolean commit(boolean bRecords, IProgressMonitor monitor) {
        if (doCommit(bRecords, monitor)) {
            setDirty(false);

            /* 更新日時更新 */
            // CommonDao.executeSQL(DicTableDao.updateLasted(getId())) を置換する
            Session session = IoService.getInstance().getSession();
            try {
                DicTableBean dicTable = (DicTableBean) session.get(DicTableBean.class, getId());
                if (dicTable != null) {
                    dicTable.setDate(new Date());

                    if (!session.getTransaction().isActive()) {
                        /* トランザクション開始 */
                        session.beginTransaction();
                    }
                    session.save(dicTable);
                    session.flush();
                    /* トランザクションコミット */
                    session.getTransaction().commit();

                    // CommonDao.getUpdateDate(DicTableDao.getDicLasted(getId()))を置換する。
                    DicTableBean dicTableLast = (DicTableBean) session.get(DicTableBean.class, getId());
                    Date lasted = dicTableLast.getDate();
                    if (lasted == null) {
                        return false;
                    }
                    setLasted(lasted);
                    return true;

                }

            } catch (HibernateException e) {
                e.printStackTrace();
            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }

            //            if (CommonDao.executeSQL(DicTableDao.updateLasted(getId()))) {
            //                Date lasted = CommonDao.getUpdateDate(DicTableDao.getDicLasted(getId()));
            //                if (lasted == null) {
            //                    return false;
            //                }
            //                setLasted(lasted);
            //                return true;
            //            }
        }
        return false;
    }


    /**
     * このメソッドでは引数を利用していないが
     * 継承したクラスでオーバーライド・拡張される。
     */
    @SuppressWarnings("unused")
    protected boolean doCommit(boolean bRecords, IProgressMonitor monitor) {
        return true;
    }


    @Override
    public boolean update() {
        setDirty(false);
        return true;
    }


    @Override
    public boolean updateRecords() {
        return false;
    }


    @Override
    public void importDic(String path, String parentDicName, DicType dicType) {
        // TODO IUserDicで定義するべきでは？
    }


    @Override
    public void importDic(String path) {
        // TODO 自動生成されたメソッド・スタブ
    }


    @Override
    public void exportDic(String path, String encoding) {
        // TODO 自動生成されたメソッド・スタブ

    }


    @Override
    public List<CoronaError> getErrors() {
        return errors;
    }


    /* ****************************************
     * Property
     */
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        int size = 6;
        boolean isUserDic = (this instanceof IUserDic);
        boolean isCategoryDic = isUserDic && (((IUserDic) this).getDicType()).equals(DicType.CATEGORY);
        boolean canHaveParentDic = (this instanceof ILabelDic) || (this instanceof IDependDic);
        size += (isUserDic) ? 3 : 0;
        size += (isCategoryDic) ? 1 : 0;
        size += (canHaveParentDic) ? 1 : 0;
        PropertyUtil prop = new PropertyUtil();
        int i = 0;
        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[size];
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_DIC_TYPE);
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CREATEDATE);
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_LASTMODIFIED);
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_EDITABLE);
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_NAME);
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_RECORDS);

        if (isUserDic) {
            descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CHILD_LABEL);
            descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CHILD_FLUC);
            descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CHILD_SYNONYM);
        }
        if (isCategoryDic) {
            descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CATEGORY);
        }
        if (canHaveParentDic) {
            descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_PARENT_NAME);
        }
        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {
        if (PropertyItem.PROP_DIC_TYPE.getKey().equals(id)) {
            if (this instanceof IUserDic) {
                return ((IUserDic) this).getDicType().getName();
            } else if (this instanceof ILabelDic) {
                return DicType.LABEL.getName();
            } else if (this instanceof IFlucDic) {
                return DicType.FLUC.getName();
            } else if (this instanceof ISynonymDic) {
                return DicType.SYNONYM.getName();
            } else if (this instanceof IPatternDic) {
                return DicType.PATTERN.getName();
            }
        } else if (PropertyItem.PROP_CREATEDATE.getKey().equals(id)) {
            Date date = getCreationTime();
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss"); //$NON-NLS-1$
                return sdf.format(date);
            }
            return ""; //$NON-NLS-1$

        } else if (PropertyItem.PROP_LASTMODIFIED.getKey().equals(id)) {
            Date date = this.getLasted();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss"); //$NON-NLS-1$
            return sdf.format(date);

        } else if (PropertyItem.PROP_EDITABLE.getKey().equals(id)) {
            if (this instanceof IUserDic) {
                IUserDic uDic = (IUserDic) this;
                /* 　juman辞書はfalse */
                if (DicType.JUMAN.equals(uDic.getDicType())) {
                    return "false"; //$NON-NLS-1$
                }
            }
            return "true"; //$NON-NLS-1$

        } else if (PropertyItem.PROP_NAME.getKey().equals(id)) {
            return getName();

        } else if (PropertyItem.PROP_PARENT_NAME.getKey().equals(id)) {
            String parentName = ""; //$NON-NLS-1$
            StringBuilder parentNameBuffer = new StringBuilder(PropertyUtil.DEFAULT_VALUE);
            /* 親のID */
            for (int pId : getParentIds()) {
                // 親辞書取得
                ICoronaDic dic = IoService.getInstance().getDictionary(pId);
                if (dic != null) {
                    /* 　親辞書名取得　 */
                    parentNameBuffer.append(dic.getName());
                    parentNameBuffer.append(","); //$NON-NLS-1$
                }
            }
            parentName = parentNameBuffer.toString();


            if (parentName.length() > 0) {
                parentName = parentName.substring(0, parentName.lastIndexOf(",")); //$NON-NLS-1$
            }
            return parentName;

        } else if (PropertyItem.PROP_CATEGORY.getKey().equals(id)) {
            if (this instanceof IUserDic) {
                IUserDic dic = (IUserDic) this;
                /* 　分野辞書の場合に取得　 */
                if (DicType.CATEGORY.equals(dic.getDicType())) {
                    return dic.getDicCategory().getText();
                }
            }
            return null;
        } else if (PropertyItem.PROP_CHILD_LABEL.getKey().equals(id)) {
            return getChildName(IoActivator.getService().getDictionarys(ILabelDic.class));
        } else if (PropertyItem.PROP_CHILD_FLUC.getKey().equals(id)) {
            return getChildName(IoActivator.getService().getDictionarys(IFlucDic.class));
        } else if (PropertyItem.PROP_CHILD_SYNONYM.getKey().equals(id)) {
            return getChildName(IoActivator.getService().getDictionarys(ISynonymDic.class));
        } else if (PropertyItem.PROP_RECORDS.getKey().equals(id)) {
            return getItemCount();
        }
        return super.getPropertyValue(id);
    }


    private String getChildName(List<ICoronaDic> dics) {
        StringBuilder childName = new StringBuilder(50);
        int myId = getId();
        /* 　自分を親に持つ辞書取得　 */
        for (ICoronaDic dic : dics) {
            for (int pId : dic.getParentIds()) {
                if (pId == myId) {
                    /* 　子辞書名取得　 */
                    childName.append(", ").append(dic.getName()); //$NON-NLS-1$
                }
            }
        }
        if (childName.length() > 0) {
            return childName.substring(2);
        }
        return PropertyUtil.DEFAULT_VALUE;
    }


    /* ****************************************
     * Object method
     */
    @Override
    public String toString() {
        return _name;
    }


    @Override
    public int hashCode() {
        return _id;
    }


    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (!(obj instanceof AbstractDic)) {
            return false;
        }

        AbstractDic d2 = (AbstractDic) obj;
        return _id == d2._id;
    }

}
