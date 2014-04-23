/**
 * @version $Id: AndOperator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:53:01
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;


/**
 * @author kousuke-morishima
 */
public class AndOperator extends ScopePattern implements IModification {

    /**
     * ルートにAndパターンを作る
     */
    public AndOperator() {
        super(null);
    }


    /**
     * 指定された親パターンの下にAndを作る
     * 
     * @param parent
     *            親パターン
     */
    public AndOperator(PatternContainer parent) {
        super(parent);
    }


    @Override
    public boolean canHaveChild(PatternKind kind) {
        switch (kind) {
        case MODIFICATION_SOURCE:
            return false;
        case MODIFICATION_DESTINATION:
            return false;
        default:
            return true;
        }
    }


    @Override
    public String toString() {
        String search = (getScope() == null) ? "" : getScope().getName(); //$NON-NLS-1$

        return "検索範囲 ： " + search + " (" + getKind() + ")"; //$NON-NLS-2$ //$NON-NLS-3$
    }


    @Override
    protected PatternContainer ownClone() {
        AndOperator and = new AndOperator(null);
        and.setScope(getScope());
        return and;
    }


    @Override
    public PatternKind getKind() {
        return PatternKind.AND;
    }
}
