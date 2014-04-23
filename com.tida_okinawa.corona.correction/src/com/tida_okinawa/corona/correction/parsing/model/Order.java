/**
 * @version $Id: Order.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:53:28
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;


/**
 * @author kousuke-morishima
 */
public class Order extends ScopePattern implements IModification {

    /**
     * ルートに順序パターンを作る
     */
    public Order() {
        this(null);
    }


    /**
     * 指定された親パターンの下に順序パターンを作る
     * 
     * @param parent
     *            親パターン
     */
    public Order(PatternContainer parent) {
        super(parent);
    }


    @Override
    public boolean canHaveChild(PatternKind kind) {
        switch (kind) {
        case MODIFICATION_SOURCE:
            return false;
        case MODIFICATION_DESTINATION:
            return false;
        case NOT:
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
        Order order = new Order(null);
        order.setScope(getScope());
        return order;
    }


    @Override
    public PatternKind getKind() {
        return PatternKind.ORDER;
    }
}
