/**
 * @version $Id: TmpPatternDic.java 997 2013-06-05 01:58:11Z yukihiro-kinjyo $
 * 
 * 2011/12/14 17:06:54
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.bean.TmpDicPatternTableBean;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.impl.PatternItem;

/**
 * @author shingo-takahashi
 */
public class TmpPatternDic extends PatternDic {
    private int workId;
    private int fldId;
    private Map<Integer, String> patternName = new HashMap<Integer, String>();


    /**
     * @param id
     * @param name
     * @param lasted
     * @param workId
     * @param fldId
     */
    public TmpPatternDic(int id, String name, Date lasted, int workId, int fldId) {
        super(id, name, lasted);
        this.workId = workId;
        this.fldId = fldId;
    }


    @Deprecated
    @Override
    protected boolean doCommit(boolean bRecords, IProgressMonitor monitor) {
        throw new UnsupportedOperationException("未サポートメソッド"); //$NON-NLS-1$
    }


    @Override
    public boolean update() {
        bRefreshRecords = false;
        return true;
    }


    @Override
    boolean updateDicInformation() {
        return true;
    }


    @Override
    public boolean updateRecords() {
        bRefreshRecords = true;
        List<IDicItem> list = new ArrayList<IDicItem>();
        if (items == null) {
            items = new ArrayList<IDicItem>();
        }

        try {
            /* 辞書に紐づく用語を取得 */
            /* DIC_IDをキーにデータを取得 */
            /*
             * String strSQL = DicPatternDao.getTmpRecords(this.workId,
             * this.fldId); を置換する。
             */
            StringBuilder strSQL = new StringBuilder(70).append("SELECT * FROM TMP_DIC_PATTERN_") //$NON-NLS-1$
                    .append(this.workId).append("_").append(this.fldId); //$NON-NLS-1$
            strSQL.append(" WHERE INACTIVE=false"); //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            @SuppressWarnings("unchecked")
            List<TmpDicPatternTableBean> listBean = session.createSQLQuery(strSQL.toString()).addEntity(TmpDicPatternTableBean.class).list();
            if (listBean != null) {
                for (TmpDicPatternTableBean rs : listBean) {
                    int id = rs.getId();
                    IPattern pattern = null;
                    /* 既存のリストから検索 */
                    if (items.size() > 0) {
                        for (IDicItem p : items) {
                            if (p.getId() == id) {
                                pattern = (IPattern) p;
                                break;
                            }
                        }
                    }
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
                        pattern = new PatternItem(rs.getName(), rs.getPattern(), rs.getTypeId(), rs.isParts());
                        pattern.setId(id);
                        ((PatternItem) pattern).setDicId(getId());
                    } else {
                        pattern.setLabel(rs.getName());
                        pattern.setText(rs.getPattern());
                        pattern.setPatternType(rs.getTypeId());
                        pattern.setParts(rs.isParts());
                    }
                    pattern.setDirty(false);
                    list.add(pattern);
                    patternName.put(pattern.getId(), pattern.getLabel());
                }
                /* 構築したリストに入れ替える */
                items.clear();
                items.addAll(list);

                setDirty(false);
                delItems.clear();
            }

            return true;
        } catch (HibernateException e) {
            bRefreshRecords = false;
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public String getItemName(int id) {
        return this.patternName.get(id);
    }
}
