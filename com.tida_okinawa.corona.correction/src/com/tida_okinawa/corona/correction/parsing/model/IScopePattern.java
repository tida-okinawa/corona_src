/**
 * @version $Id: IScopePattern.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/25 21:31:25
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

/**
 * 
 * @author kyohei-miyazato
 */
public interface IScopePattern {

    /**
     * スコープを返す。
     * 
     * @return scope
     */
    public SearchScopeType getScope();


    /**
     * 与えられらスコープを設定する。
     * 
     * @param scope
     */
    public void setScope(SearchScopeType scope);
}
