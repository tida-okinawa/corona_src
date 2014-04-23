/**
 * @version $Id: DicDataProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/21 21:25:43
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * @author kousuke-morishima
 */
public abstract class DicDataProvider extends FilteringDataProvider {
    private ICoronaDic dic;
    private int pagingSize;
    private int currentIndex;
    private List<Object> current;


    /**
     * @param dic
     * @param pagingSize
     *            一度に取得するデータの件数を指定する。全件取得するなら
     *            {@link IPagingDataProvider#SHOW_ALL}。
     */
    public DicDataProvider(ICoronaDic dic, int pagingSize) {
        this.dic = dic;
        this.pagingSize = pagingSize;
    }


    private void init() {
        current = new ArrayList<Object>();
        current(true);
    }


    /* ****************************************
     * ページ遷移
     */
    @Override
    public List<Object> current(boolean refresh) {
        if (refresh) {
            if (pagingSize == SHOW_ALL) {
                setCurrent(dic.getItemsPaging(1, dic.getItemCount()));
            } else {
                setCurrent(dic.getItemsPaging(currentIndex / pagingSize + 1, pagingSize));
            }
        }
        if (current == null) {
            init();
        }
        return current;
    }


    @Override
    public List<Object> next() {
        if (pagingSize == SHOW_ALL) {
            return current(false);
        }

        currentIndex += pagingSize;
        return setCurrent(dic.getItemsPaging(currentIndex / pagingSize + 1, pagingSize));
    }


    @Override
    public List<Object> prev() {
        if (pagingSize == SHOW_ALL) {
            return current(false);
        }
        currentIndex -= pagingSize;
        return setCurrent(dic.getItemsPaging(currentIndex / pagingSize + 1, pagingSize));
    }


    @Override
    public List<Object> first() {
        if (pagingSize == SHOW_ALL) {
            return current(false);
        }
        currentIndex = 0;
        return setCurrent(dic.getItemsPaging(1, pagingSize));
    }


    @Override
    public List<Object> last() {
        if (pagingSize == SHOW_ALL) {
            return current(false);
        }

        int total = totalCount();
        int pageNum = total / pagingSize;
        if (pageNum * pagingSize < total) {
            pageNum++;
        }
        currentIndex = (pageNum - 1) * pagingSize;
        return setCurrent(dic.getItemsPaging(pageNum, pagingSize));
    }


    /* ********************
     * ページ遷移判定
     */
    @Override
    public boolean hasNext() {
        if (pagingSize == SHOW_ALL) {
            return false;
        }

        /* レコードをカット（削除）して１ページのデータ数がpagingSizeを下回った時、次のデータがあっても遷移しない問題に対応 */
        int viewSize = current(false).size();
        return currentIndex + viewSize < totalCount();
    }


    @Override
    public boolean hasPrev() {
        return currentIndex > 0;
    }


    /* ****************************************
     * getter/setter
     */
    @Override
    public int getPagingSize() {
        return pagingSize;
    }


    @Override
    public int totalCount() {
        return ((IUserDic) dic).getRecCount();
    }


    @Override
    public int currentIndex() {
        return currentIndex;
    }


    /**
     * @return データ取得対象の辞書
     */
    protected ICoronaDic getDictionary() {
        return dic;
    }


    /* ****************************************
     * データ操作
     */
    /**
     * 辞書にデータを追加する。
     */
    @Override
    public Object addData(Object data) {
        dic.addItem((IDicItem) data);
        return data;
    }


    @Override
    public boolean removeData(Object data) {
        if (data != null) {
            dic.removeItem((IDicItem) data);
            boolean ret = current(false).remove(data);
            return ret;
        }
        return false;
    }


    public boolean commit(IProgressMonitor monitor) {
        return dic.commit(monitor);
    }


    /* ****************************************
     */
    protected List<Object> setCurrent(List<?> data) {
        if (current == null) {
            current = new ArrayList<Object>();
        } else {
            current.clear();
        }
        current.addAll(data);
        return current;
    }
}
