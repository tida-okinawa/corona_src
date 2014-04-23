/**
 * @version $Id: FilteringDataProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/16 10:25:22
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kousuke-morishima
 */
public abstract class FilteringDataProvider implements IFilteringDataProvider {

    public FilteringDataProvider() {
        filters = new HashMap<Object, Object[]>(3);
        ascending = true;
    }

    private Object sortField;
    private boolean ascending;


    @Override
    public void setOrder(Object sortField, boolean asc) {
        this.sortField = sortField;
        this.ascending = asc;
    }


    @Override
    public Object getOrder() {
        return sortField;
    }


    @Override
    public boolean isAscending() {
        return ascending;
    }


    @Override
    public void clearOrder() {
        sortField = null;
        ascending = true;
    }

    private Map<Object, Object[]> filters;


    @Override
    public void addFilter(Object filterField, Object[] matchValues) {
        filters.put(filterField, matchValues);
    }


    @Override
    public Map<Object, Object[]> getFilters() {
        return filters;
    }


    @Override
    public void setFilter(Map<Object, Object[]> filters) {
        if (filters == null) {
            this.filters.clear();
        } else {
            this.filters = filters;
        }
    }


    @Override
    public void clearFilter() {
        filters.clear();
    }

}
