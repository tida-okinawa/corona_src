/**
 * @version $Id: UserDicDataProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/24 11:17:41
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.io.model.dic.UserDicFieldType;
import com.tida_okinawa.corona.ui.editors.DicDataProvider;
import com.tida_okinawa.corona.ui.editors.IPagingDataProvider;

/**
 * @author kousuke-morishima
 */
public class UserDicDataProvider extends DicDataProvider {
    private IUserDic dic;


    /**
     * @param dic
     * @param pageSize
     *            一度に取得するデータの件数を指定する。全件取得するなら
     *            {@link IPagingDataProvider#SHOW_ALL}。
     */
    public UserDicDataProvider(IUserDic dic, int pageSize) {
        super(dic, pageSize);

        this.dic = dic;
        setOrder(UserDicFieldType.READING, true);
        setFilter(null);
    }


    /* ****************************************
     * ページ遷移
     */
    @Override
    public void setOrder(Object sortField, boolean asc) {
        if (sortField == null) {
            clearOrder();
        } else {
            Assert.isLegal(sortField instanceof UserDicFieldType);
            super.setOrder(sortField, asc);
        }
        dic.setSortOrder((UserDicFieldType) sortField, asc);
    }


    @Override
    public UserDicFieldType getOrder() {
        return (UserDicFieldType) super.getOrder();
    }


    @Override
    public void setFilter(Map<Object, Object[]> filters) {
        super.setFilter(filters);
        dic.clearFilter();
        for (Entry<Object, Object[]> e : getFilters().entrySet()) {
            dic.addFilter((UserDicFieldType) e.getKey(), e.getValue());
        }
        dic.setAcceptOrNot(true);
    }

    private ITerm currentEmptyData;


    public ITerm addEmptyData() {
        ITerm emptyRow = null;
        /* 最終データの場合のみ、末尾に入力行を追加できる */
        if (!hasNext()) {
            emptyRow = IoActivator.getDicFactory().createTerm("", "", TermPart.NONE.getName(), "", "", "");
            current(false).add(emptyRow);
        }
        currentEmptyData = emptyRow;
        return emptyRow;
    }


    @Override
    public Object addData(Object data) {
        super.addData(data);

        if (data instanceof ITerm) {
            if ((currentEmptyData == null) || !currentEmptyData.equals(data)) {
                current(false).add(data);
                return data;
            }
        }
        return null;
    }

}
