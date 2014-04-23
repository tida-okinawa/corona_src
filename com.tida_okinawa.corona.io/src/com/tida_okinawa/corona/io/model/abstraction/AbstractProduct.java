/**
 * @version $Id: AbstractProduct.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.abstraction;


import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * @author shingo-takahashi
 * 
 */
public abstract class AbstractProduct extends CoronaObject implements ICoronaProduct {
    protected int _id;

    protected int _projectId;

    protected String _name;

    protected List<ICoronaDic> _dics;

    protected ICoronaProject _parent;


    /**
     * @param name
     */
    public AbstractProduct(String name) {
        this._name = name;
    }


    /**
     * @param name
     * @param projectId
     */
    public AbstractProduct(String name, int projectId) {
        super();
        this._name = name;
        this._projectId = projectId;
        _parent = IoService.getInstance().getProject(projectId);
    }


    public AbstractProduct(String name, ICoronaProject parent) {
        super();
        this._name = name;
        this._projectId = parent.getId();
        this._parent = parent;
    }


    @Override
    public List<ICoronaDic> getDictionarys(Class<?> cls) {
        return getDictionarysDam(cls, _id);
    }


    @Override
    public ICoronaDic getDictionary(int id) {
        if (_dics == null)
            getDictionarysDam(ICoronaDic.class, getId());
        for (ICoronaDic dic : _dics) {
            if (dic.getId() == id) {
                return dic;
            }
        }
        return null;
    }


    @Override
    public boolean addDictionary(ICoronaDic dic) {
        if (addDictionaryDam(dic)) {
            if (!_dics.contains(dic)) {
                _dics.add(dic);
            }
            return true;
        }
        return false;
    }


    @Override
    public void removeDictionary(int id) {
        if (_dics == null) {
            // TODO getDictionarysでやっている辞書の全更新処理を、updateDictionaries()に分離する
            getDictionarys(ICoronaDic.class);
        }
        removeDictionaryDam(id);
        for (Iterator<ICoronaDic> itr = _dics.iterator(); itr.hasNext();) {
            ICoronaDic dic = itr.next();
            if (dic.getId() == id) {
                itr.remove();
                break;
            }
        }
    }


    @Override
    public Date getClaimLasted(int claimId, ClaimWorkDataType type) {
        return null;
    }


    public void setId(int id) {
        this._id = id;
    }


    @Override
    public int getId() {
        return _id;
    }


    public void setProjectId(int projectId) {
        this._projectId = projectId;
        _parent = IoService.getInstance().getProject(projectId);
    }


    @Override
    public int getProjectId() {
        return _projectId;
    }


    public void setName(String name) {
        this._name = name;
    }


    @Override
    public String getName() {
        return _name;
    }


    @Override
    public Set<IClaimWorkData> getClaimWorkDatas() {
        return getClaimWorkDatasDam();
    }


    @Override
    public IClaimWorkData getClaimWorkData(int claimId, ClaimWorkDataType type, int fieldNo) {
        return getClaimWorkDataDam(claimId, type, fieldNo);
    }


    abstract protected List<ICoronaDic> getDictionarysDam(Class<?> cls, int productId);


    abstract protected Set<IClaimWorkData> getClaimWorkDatasDam();


    abstract protected IClaimWorkData getClaimWorkDataDam(int claimId, ClaimWorkDataType type, int fieldNo);


    abstract protected boolean addDictionaryDam(ICoronaDic dic);


    abstract protected void removeDictionaryDam(int id);


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractProduct)) {
            return false;
        }
        AbstractProduct p2 = (AbstractProduct) obj;
        return (_id == p2._id);
    }
}
