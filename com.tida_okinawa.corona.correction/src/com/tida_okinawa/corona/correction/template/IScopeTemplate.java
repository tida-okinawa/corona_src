/**
 * @version $Id: IScopeTemplate.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 18:15:42
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 *         ひな型用のスコープの取得、設定
 */
public interface IScopeTemplate {

    /**
     * スコープを返す
     * 
     * @return SearchScopeType（全体/一文/文節）
     * 
     */
    public SearchScopeType getScope();


    /**
     * 与えられらスコープを設定する
     * 
     * @param scope
     *            SearchScopeType(全体/一文/文節)
     */
    public void setScope(SearchScopeType scope);
}
