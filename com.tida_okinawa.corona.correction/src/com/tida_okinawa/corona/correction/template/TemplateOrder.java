/**
 * @version $Id: TemplateOrder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 17:53:34
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateOrder extends ScopeTemplate {

    /**
     * 順序
     * 
     * @param parent
     *            親要素
     */
    public TemplateOrder(TemplateContainer parent) {
        super(parent);
    }


    @Override
    public String toString() {
        String search = (getScope() == null) ? "" : getScope().getName(); //$NON-NLS-1$

        return Messages.TEMPLATE_STRING_SEARCH_SCOPE + search + Messages.TEMPLATE_ORDER_STRING;
    }
}
