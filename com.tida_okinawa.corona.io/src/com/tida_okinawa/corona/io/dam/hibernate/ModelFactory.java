/**
 * @version $Id: ModelFactory.java 968 2013-03-05 12:25:25Z hajime-uchihara $
 *
 * 2011/08/19 13:58:41
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkFAData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.IModelFactory;
import com.tida_okinawa.corona.io.model.abstraction.AbstractModelFactory;

/**
 * @author hajime-uchihara
 */
public final class ModelFactory extends AbstractModelFactory {

    private static ModelFactory _instance = new ModelFactory();


    private ModelFactory() {
        /* このクラスはシングルトン */
    }


    /**
     * モデルファクトリーのインスタンスを取得
     * 
     * @return モデルファクトリーのインスタンス(Singleton)
     */
    public static IModelFactory getInstance() {
        return _instance;
    }


    @Override
    protected ICoronaProject createProjectDam(String name) {
        /* 同一名称のプロジェクトが無いかチェック */
        for (ICoronaProject prj : IoService.getInstance().getProjects()) {
            if (prj.getName().equals(name)) {
                return prj;
            }
        }
        return new Project(name);
    }


    @Override
    protected ICoronaProduct createProductDam(String name, ICoronaProject parent) {
        /* 同一名称のターゲットが無いかチェック */
        for (ICoronaProduct product : parent.getProducts()) {
            if (product.getName().equals(name)) {
                return null;
            }
        }
        return new Product(name, parent);
    }


    @Override
    public IClaimWorkData createClaimWorkData(int claimId, int fieldId, ClaimWorkDataType type, int projectId, int productId) {
        if (ClaimWorkDataType.FREQUENTLY_APPERING.equals(type)) {
            /* clearを実行できるようにするため */
            return createClaimFAData(claimId, fieldId, projectId, productId);
        }
        return new ClaimWorkData(claimId, fieldId, type, projectId, productId);
    }


    @Override
    public IClaimWorkPattern createClaimWorkPattern(int claimId, int fieldId, ClaimWorkDataType type, int projectId, int productId) {
        return new ClaimWorkPattern(claimId, fieldId, type, projectId, productId, false);
    }


    @Override
    public IClaimWorkFAData createClaimFAData(int claimId, int fieldId, int projectId, int productId) {
        return new ClaimWorkFAData(claimId, fieldId, projectId, productId);
    }
}
