/**
 * @version $Id: AbstractProject.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.abstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.io.PropertyUtil;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * @author shingo-takahashi
 * 
 */
public abstract class AbstractProject extends CoronaObject implements ICoronaProject {

    protected int _id;

    protected String _name;

    protected Map<Integer, ICoronaProduct> _products = new HashMap<Integer, ICoronaProduct>();

    protected List<ICoronaDic> _dics;

    protected List<IClaimData> _cliamDatas = new ArrayList<IClaimData>();


    public AbstractProject(String name) {
        super();
        this._name = name;
    }


    public AbstractProject(String name, int id) {
        super();
        this._name = name;
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


    /**
     * @see com.tida_okinawa.corona.io.model.ICoronaProject#getProducts()
     */
    @Override
    public List<ICoronaProduct> getProducts() {
        List<ICoronaProduct> list = new ArrayList<ICoronaProduct>();
        for (Entry<Integer, ICoronaProduct> entry : getProductsMap().entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }


    /**
     * @see com.tida_okinawa.corona.io.model.ICoronaProject#addProduct(com.tida_okinawa.corona.io.model.AbstractProduct)
     */
    @Override
    public boolean addProduct(ICoronaProduct product) {
        return addProductDam(product);
    }


    /**
     * @see com.tida_okinawa.corona.io.model.ICoronaProject#removeProduct(com.tida_okinawa.corona.io.model.AbstractProduct)
     */
    @Override
    public void removeProduct(ICoronaProduct product) {
        removeProductDam(product);
    }


    /**
     * @see com.tida_okinawa.corona.io.model.ICoronaProject#addClaimData(int)
     */
    @Override
    public void addClaimData(int id) {
        for (IClaimData data : IoService.getInstance().getClaimDatas()) {
            if (data.getId() == id) {
                _cliamDatas.add(data);
            }
        }

    }


    @Override
    public void addClaimData(IClaimData iClaimData) {
        _cliamDatas.add(iClaimData);
    }


    /**
     * @see com.tida_okinawa.corona.io.model.ICoronaProject#removeClaimData(int)
     * 
     * 
     */
    @Override
    public void removeClaimData(int id) {
        Iterator<IClaimData> i = _cliamDatas.iterator();
        while (i.hasNext()) {
            IClaimData claim = i.next();
            if (claim.getId() == id) {
                i.remove();
                break;
            }
        }
    }


    /**
     * 
     * 辞書情報取得(プロジェクト単位)
     * 
     * @param cls
     * @return List<ICoronaDic>
     * 
     */
    @Override
    public List<ICoronaDic> getDictionarys(Class<?> cls) {
        /* 辞書情報を取得 */
        return getDictionarysDam(cls, this._id);
    }


    /**
     * 
     * 辞書情報取得(辞書ID)
     * 
     * @param id
     * @return ICoronaDic
     * 
     */
    @Override
    public ICoronaDic getDictionary(int id) {
        if (_dics == null) {
            // TODO getDictionarysでやっている辞書の全更新処理を、updateDictionaries()に分離する
            getDictionarys(ICoronaDic.class);
        }
        for (ICoronaDic dic : _dics) {
            if (dic.getId() == id) {
                return dic;
            }
        }
        return null;
    }


    /**
     * 辞書情報追加
     * (プロジェクトと辞書情報を紐づける)
     * 
     * @param dic
     * 
     */
    @Override
    public boolean addDictionary(ICoronaDic dic) {
        if (addDictionaryDam(dic)) {
            _dics.add(dic);
            return true;
        }
        return false;
    }


    /**
     * @see com.tida_okinawa.corona.io.model.ICoronaDics#removeDictionary(int)
     * 
     */
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
    public void setId(int id) {
        this._id = id;
    }


    @Override
    public void setName(String name) {
        this._name = name;
    }


    @Override
    public void saveKnpConfig(ProjectKnpConfig config) {
        saveKnpConfigDam(config.getKey());
    }


    @Override
    public ProjectKnpConfig getKnpConfig() {
        return ProjectKnpConfig.get(getKnpConfigDam());
    }


    abstract protected Map<Integer, ICoronaProduct> getProductsMap();


    /**
     * ターゲット情報登録
     * 
     * @param product
     * @return 登録に成功したらtrue
     */
    abstract protected boolean addProductDam(ICoronaProduct product);


    abstract protected void removeProductDam(ICoronaProduct product);


    abstract protected List<ICoronaDic> getDictionarysDam(Class<?> cls, int projectId);


    abstract protected boolean addDictionaryDam(ICoronaDic dic);


    abstract protected void removeDictionaryDam(int id);


    abstract protected void saveKnpConfigDam(int config);


    abstract protected int getKnpConfigDam();


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractProject)) {
            return false;
        }

        AbstractProject p2 = (AbstractProject) obj;
        return (_id == p2._id) && (_name.equals(p2._name));
    }


    /**
     * プロジェクトの PropertyDescriptors を返す
     * 
     * <pre>
     * 1. 編集可能か
     * 2. プロジェクト名
     * 3. 係り受け解析の有無(コンボボックス)
     * </pre>
     */
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        PropertyUtil prop = new PropertyUtil();

        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { prop.getDescriptor(PropertyItem.PROP_EDITABLE),
                prop.getDescriptor(PropertyItem.PROP_NAME), PropertyUtil.getComboDescriptor(PropertyItem.PROP_DOKNP, ProjectKnpConfig.getConfigDescriptions()), };
        return descriptor;
    }


    @Override
    public void setPropertyValue(Object id, Object value) {
        if (!PropertyItem.PROP_DOKNP.getKey().equals(id)) {
            return;
        }

        saveKnpConfig(ProjectKnpConfig.get(((Integer) value).intValue()));
    }


    /**
     * プロジェクトのプロパティ値を返す
     * 
     * <pre>
     * editable : 編集可能か否か。プロジェクトでは常に "true" を返す
     * name     : プロジェクト名を返す。
     * doknp    : このプロパティは ComboBox なので、初期表示するアイテムのインデックスを返す
     * </pre>
     */
    @Override
    public Object getPropertyValue(Object id) {
        if (PropertyItem.PROP_EDITABLE.getKey().equals(id)) {
            return "true";
        } else if (PropertyItem.PROP_NAME.getKey().equals(id)) {
            return getName();
        } else if (PropertyItem.PROP_DOKNP.getKey().equals(id)) {
            return getKnpConfig().getKey();
        }
        return PropertyUtil.DEFAULT_VALUE;
    }
}
