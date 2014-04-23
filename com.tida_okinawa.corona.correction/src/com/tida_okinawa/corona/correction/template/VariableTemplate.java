/**
 * @version $Id: VariableTemplate.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/12/26 11:32:29
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;


/**
 * @author s.takuro
 * 
 */
public class VariableTemplate extends Template implements IVariableTemplate {

    /**
     * 要素が固定か可変かを設定、取得
     * 
     * @param parent
     *            親要素
     */
    public VariableTemplate(TemplateContainer parent) {
        super(parent);
    }


    /** 固定かどうかの判定 */
    private boolean state = true;


    @Override
    public void setFixCheck(boolean check) {
        if (this.state == check) {
            return;
        }
        this.state = check;
        propertyChanged();
    }


    @Override
    public boolean getFixCheck() {
        return state;
    }
}
