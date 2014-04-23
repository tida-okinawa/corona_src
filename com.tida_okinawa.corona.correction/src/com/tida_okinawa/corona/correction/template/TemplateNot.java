/**
 * @version $Id: TemplateNot.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 17:57:17
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateNot extends TemplateContainer {

    /**
     * Not
     * 
     * @param parent
     *            親要素
     */
    public TemplateNot(TemplateContainer parent) {
        super(parent);
    }


    @Override
    public String toString() {
        return Messages.TEMPLATE_NOT_STRING;
    }
}
