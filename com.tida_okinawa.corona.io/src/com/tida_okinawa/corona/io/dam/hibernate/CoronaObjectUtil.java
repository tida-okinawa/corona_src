/**
 * @version $Id: CoronaObjectUtil.java 968 2013-03-05 12:25:25Z kousuke-morishima $
 * 
 * 2012/09/03 19:14:38
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.hibernate.HibernateException;

import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicFactory;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * @author yukihiro-kinjo
 * 
 */
class CoronaObjectUtil {

    /**
     * 指定されたDicTableBeanが示す辞書情報をもとに、辞書オブジェクトを生成して返す。
     * 
     * @param rs
     *            辞書情報
     * @param categoryList
     *            分野名一覧
     * @return 辞書オブジェクト。DicTableBeanからの読み込み失敗時にはnull
     */
    static ICoronaDic createDicByCurrentData(DicTableBean dt, List<TextItem> categoryList) {
        IDicFactory factory = DicFactory.getInstance();
        ICoronaDic dicData = null;
        try {
            switch (DicType.valueOf(dt.getDicType())) {
            case JUMAN: /* JUMAN辞書 */
            case COMMON: /* 一般辞書 */
            case CATEGORY: /* 分野辞書 */
            case SPECIAL: /* 固有辞書 */
                dicData = factory.createUserDic(dt.getDicName(), dt.getDicFileName(), DicType.valueOf(dt.getDicType()));
                /* カテゴリIDがNULLの場合は処理しない */
                if (dt.getCategoryId() != null) {
                    for (TextItem item : categoryList) {
                        if (dt.getCategoryId() == item.getId()) {
                            ((IUserDic) dicData).setDicCategory(item);
                        }
                    }
                }
                break;
            case LABEL: /* ラベル辞書 */
                dicData = factory.createLabelDic(dt.getDicName());
                ((LabelDic) dicData).setParentIds(CoronaIoUtils.stringToIntSet(dt.getParentId()));
                break;
            case FLUC: /* ゆらぎ辞書 */
                dicData = factory.createFlucDic(dt.getDicName(), CoronaIoUtils.stringToIntSet(dt.getParentId()));
                break;
            case SYNONYM: /* 同義語辞書 */
                dicData = factory.createSynonymDic(dt.getDicName(), CoronaIoUtils.stringToIntSet(dt.getParentId()));
                break;
            case PATTERN: /* パターン辞書 */
                dicData = factory.createPatternDic(dt.getDicName());
                break;
            default:
                CoronaIoUtils.setErrorLog(IStatus.WARNING, "辞書タイプ誤り:ID=" + dt.getDicId(), null); //$NON-NLS-1$
            }

            dicData.setId(dt.getDicId()); /* 辞書ID設定 */
            dicData.setLasted(dt.getDate()); /* 更新日時設定 */
            dicData.setCreationTime(dt.getCreationTime()); /* 作成日時 */

            return dicData;
        } catch (HibernateException e) {
            e.printStackTrace();
            return null;
        }
    }

}
