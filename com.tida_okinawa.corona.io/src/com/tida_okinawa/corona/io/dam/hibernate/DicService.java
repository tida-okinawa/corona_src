/**
 * @version $Id: DicService.java 997 2013-10-28 14:50:11Z yukihiro-kinjo $
 *
 * 2013/10/28 14:51:29
 * @author yukihiro-kinjo
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.bean.DicCommonBean;
import com.tida_okinawa.corona.io.bean.DicFlucBean;
import com.tida_okinawa.corona.io.bean.DicPatternBean;
import com.tida_okinawa.corona.io.bean.DicSynonymBean;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IDicName;
import com.tida_okinawa.corona.io.model.dic.abstraction.DicName;
import com.tida_okinawa.corona.io.service.IDicService;

/**
 * @author yukihiro-kinjo
 */
public class DicService implements IDicService {
    private static DicService instance = new DicService();


    private DicService() {
        /* このクラスはシングルトン */
    }


    /**
     * DicServiceのインスタンスを取得する
     * 
     * @return このクラス(DicService)のインスタンスを取得する
     */
    public static DicService getInstance() {
        return instance;
    }


    @Override
    public DicType getDicType(int dicId) {
        String hql = "from DicTableBean where dicId=:searchDicId"; //$NON-NLS-1$
        try {
            Session session = IoService.getInstance().getSession();
            DicTableBean result = (DicTableBean) session.createQuery(hql).setInteger("searchDicId", dicId).uniqueResult(); //$NON-NLS-1$
            if (result != null) {
                return DicType.valueOf(result.getDicType());
            }
            return null;
        } catch (HibernateException e) {
            return null;
        }
    }


    @Override
    public IDicItem getItem(int itemId, DicType type) {
        switch (type) {
        case SPECIAL:
        case COMMON:
        case CATEGORY:
        case JUMAN:
            return getDicCommonItem(itemId);
        case FLUC:
            return getDicFlucItem(itemId);
        case SYNONYM:
            return getDicSynonymItem(itemId);
        case PATTERN:
            return getDicPatternItem(itemId);
        default:
            throw new IllegalArgumentException();
        }
    }


    private static IDicItem getDicCommonItem(int itemId) {
        try {
            String hql = "from DicCommonBean where itemId=:itemId"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            DicCommonBean result = (DicCommonBean) session.createQuery(hql).setInteger("itemId", itemId).uniqueResult(); //$NON-NLS-1$
            if (result != null && result.getDicId() > 0) {
                ICoronaDic dic = IoActivator.getService().getDictionary(result.getDicId());
                if (dic != null) {
                    return dic.getItem(itemId);
                }
            }
            return null;
        } catch (HibernateException e) {
            return null;
        }
    }


    private static IDicItem getDicFlucItem(int itemId) {
        try {
            String hql = "from DicFlucBean where flucId=:itemId"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            DicFlucBean result = (DicFlucBean) session.createQuery(hql).setInteger("itemId", itemId).uniqueResult(); //$NON-NLS-1$
            if (result != null && result.getDicId() > 0) {
                ICoronaDic dic = IoActivator.getService().getDictionary(result.getDicId());
                if (dic != null) {
                    return dic.getItem(itemId);
                }
            }
            return null;
        } catch (HibernateException e) {
            return null;
        }
    }


    private static IDicItem getDicSynonymItem(int itemId) {
        try {
            String hql = "from DicSynonymBean where synonymId=:itemId"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            DicSynonymBean result = (DicSynonymBean) session.createQuery(hql).setInteger("itemId", itemId).uniqueResult(); //$NON-NLS-1$
            if (result != null && result.getDicId() > 0) {
                ICoronaDic dic = IoActivator.getService().getDictionary(result.getDicId());
                if (dic != null) {
                    return dic.getItem(itemId);
                }
            }
            return null;
        } catch (HibernateException e) {
            return null;
        }
    }


    private static IDicItem getDicPatternItem(int itemId) {
        try {
            String hql = "from DicPatternBean where id=:itemId"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            DicPatternBean result = (DicPatternBean) session.createQuery(hql).setInteger("itemId", itemId).uniqueResult(); //$NON-NLS-1$
            if (result != null && result.getDicId() > 0) {
                ICoronaDic dic = IoActivator.getService().getDictionary(result.getDicId());
                if (dic != null) {
                    return dic.getItem(itemId);
                }
            }
            return null;
        } catch (HibernateException e) {
            return null;
        }
    }


    /** #177 パターン自動生成（係り受け抽出） */
    @Override
    public IDicName[] getDicName(DicType type) {
        List<IDicName> dicNames = new ArrayList<IDicName>(100);
        String hql = "from DicTableBean as dt where dt.dicType=:dtype"; //$NON-NLS-1$
        try {
            Session session = IoService.getInstance().getSession();
            @SuppressWarnings("unchecked")
            List<DicTableBean> result = session.createQuery(hql).setInteger("dtype", type.getIntValue()).list(); //$NON-NLS-1$
            for (DicTableBean item : result) {
                if (item.isInactive() != true) {
                    dicNames.add(new DicName(item.getDicId(), item.getDicName(), item.isInactive()));
                }
            }
            return dicNames.toArray(new IDicName[dicNames.size()]);
        } catch (HibernateException e) {
            return new IDicName[0];
        }
    }
}
