/**
 * @version $Id: TemplateOr.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 17:56:29
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateOr extends ScopeTemplate {

    /**
     * Or
     * 
     * @param parent
     *            親要素
     */
    public TemplateOr(TemplateContainer parent) {
        super(parent);
    }


    @Override
    public String toString() {
        String search = (getScope() == null) ? "" : getScope().getName(); //$NON-NLS-1$
        return Messages.TEMPLATE_STRING_SEARCH_SCOPE + search + Messages.TEMPLATE_OR_STRING;
    }
}
