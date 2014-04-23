/**
 * @version $Id: ScopeTemplate.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 18:33:59
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class ScopeTemplate extends TemplateContainer implements IScopeTemplate {

    /**
     * ひな型用のスコープの取得、設定
     * 
     * @param parent
     *            親要素
     */
    public ScopeTemplate(TemplateContainer parent) {
        super(parent);
    }

    private SearchScopeType scope = SearchScopeType.SEARCH_ALL;


    @Override
    public SearchScopeType getScope() {
        return scope;
    }


    @Override
    public void setScope(SearchScopeType scope) {
        scope = (scope == null) ? SearchScopeType.SEARCH_ALL : scope;
        if (this.scope.equals(scope)) {
            return;
        }
        this.scope = scope;
        propertyChanged();
    }
}
