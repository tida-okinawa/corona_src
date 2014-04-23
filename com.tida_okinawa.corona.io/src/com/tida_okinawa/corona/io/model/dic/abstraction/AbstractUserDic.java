/**
 * @version $Id: AbstractUserDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.abstraction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.UserDicFieldType;


/**
 * @author shingo-takahashi
 *
 */
/**
 * @author shingo-takahashi
 * 
 */
public abstract class AbstractUserDic extends AbstractDic implements IUserDic {

    protected List<ITerm> items;
    protected String _fileName;
    protected DicType _dicType;
    protected TextItem _dicCategory;
    protected List<IDicItem> delItems = new ArrayList<IDicItem>();


    public AbstractUserDic(int id, String name, String file, DicType dicType, Date lasted) {
        super(id, name, lasted);
        this._dicType = dicType;
        this._fileName = file;
    }


    /* ****************************************
     * property
     */
    @Override
    public DicType getDicType() {
        return _dicType;
    }


    @Override
    public void setName(String name) {
        super.setName(name);
        /* 一緒に出力ファイル名を変更 */
        String namePart;
        if (name.indexOf('.') != -1) {
            namePart = _name.substring(0, _name.lastIndexOf('.'));
        } else {
            namePart = name;
        }
        setFileName(namePart + ".dic");
    }


    @Override
    public String getFileName() {
        return _fileName;
    }


    @Override
    public void setFileName(String filename) {
        this._fileName = filename;
    }


    @Override
    public void setDicCategory(TextItem dicCategory) {
        this._dicCategory = dicCategory;
    }


    @Override
    public TextItem getDicCategory() {
        if (_dicCategory == null) {
            _dicCategory = new TextItem(0, "");
        }
        return _dicCategory;
    }

    /* ********************
     * ソート
     */
    private Map<UserDicFieldType, Object[]> filterFields = new HashMap<UserDicFieldType, Object[]>();


    protected Map<UserDicFieldType, Object[]> getFilters() {
        return filterFields;
    }

    private boolean acceptOrNot;


    protected boolean isAcceptOrNot() {
        return acceptOrNot;
    }


    @Override
    public void setAcceptOrNot(boolean acceptOrNot) {
        this.acceptOrNot = acceptOrNot;
    }


    @Override
    public void setFilter(UserDicFieldType filterField, Object[] values, boolean acceptOrNot) {
        filterFields.clear();
        filterFields.put(filterField, (values == null) ? new Object[0] : values);
        setAcceptOrNot(acceptOrNot);
    }


    @Override
    public void addFilter(UserDicFieldType filterField, Object[] values) {
        filterFields.put(filterField, (values == null) ? new Object[0] : values);
    }


    @Override
    public void clearFilter() {
        filterFields.clear();
        setAcceptOrNot(false);
    }

    /* ********************
     * フィルタ
     */
    private UserDicFieldType sortField = UserDicFieldType.NONE;


    protected UserDicFieldType getSortField() {
        return sortField;
    }

    private boolean sortAsc;


    protected boolean isAscending() {
        return sortAsc;
    }


    @Override
    public void setSortOrder(UserDicFieldType sortField, boolean asc) {
        this.sortField = sortField;
        this.sortAsc = asc;
    }


    /* ****************************************
     * 更新
     */
    @Override
    public boolean update() {
        addFilter(UserDicFieldType.NONE, new Object[0]);
        setSortOrder(UserDicFieldType.READING, true);
        return super.update();
    }


    /* ****************************************
     * 編集
     */
    @Override
    public void addItem(IDicItem item) {
        if (items == null) {
            updateRecords();
        }
        assert item instanceof ITerm;

        if (items.add((ITerm) item)) {
            ((DicItem) item).setInActive(false);
            ((DicItem) item).setDicId(getId());
            delItems.remove(item);
            setDirty(true);
        }
    }


    @Override
    public void removeItem(IDicItem item) {
        if (item == null)
            return;
        if (items == null) {
            updateRecords();
        }
        if (items.remove(item)) {
            ((DicItem) item).setInActive(true);
            ((DicItem) item).setDicId(UNSAVED_ID);
            delItems.add(item);
            setDirty(true);
        }
    }


    /* ****************************************
     * import/export
     */
    @Override
    public void importDic(String path, String parentDicName, DicType dicType) {
        importDicDam(path, parentDicName, dicType);
    }


    /**
     * @param path
     */
    abstract protected void importDicDam(String path, String parentDicName, DicType dicType);


    @Override
    public void exportDic(String path, String encoding) {
        exportDicDam(path, encoding);
    }


    /**
     * 辞書エクスポート
     * 
     * @param path
     * @param encoding
     */
    abstract protected void exportDicDam(String path, String encoding);

}
