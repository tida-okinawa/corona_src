/**
 * @version $Id: AbstractModelFactory.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/19 13:41:03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.abstraction;

import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.IModelFactory;

/**
 * @author shingo-takahashi
 */
public abstract class AbstractModelFactory implements IModelFactory {

    protected List<ICoronaProject> projects = new ArrayList<ICoronaProject>();
    protected List<ICoronaProduct> products = new ArrayList<ICoronaProduct>();


    /**
     * @return prjs
     */
    public List<ICoronaProject> getProjects() {
        return projects;
    }


    /**
     * @return prds
     */
    public List<ICoronaProduct> getProducts() {
        return products;
    }


    @Override
    public ICoronaProject createProject(String name) {
        ICoronaProject prj = createProjectDam(name);
        projects.add(prj);
        return prj;
    }


    @Override
    public ICoronaProduct createProduct(String name, ICoronaProject parent) {
        ICoronaProduct product = createProductDam(name, parent);
        if (product != null) {
            products.add(product);
        }
        return product;
    }


    /**
     * 指定された名前でプロジェクトを作成する。
     * 
     * @param name
     * @return 同名のプロジェクトがある場合は、その既存プロジェクト。
     */
    abstract protected ICoronaProject createProjectDam(String name);


    /**
     * 指定されたプロジェクトに、ターゲットを作成する。
     * 
     * @param name
     * @param parent
     * @return 作成したターゲット。同名のターゲットがある場合はnull
     */
    abstract protected ICoronaProduct createProductDam(String name, ICoronaProject parent);

}
