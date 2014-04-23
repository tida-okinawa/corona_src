/**
 * @version $Id: NotOperator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:53:10
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

/**
 * @author kousuke-morishima
 */
public class NotOperator extends PatternContainer {

    /**
     * ルートにNotパターンを作る
     */
    public NotOperator() {
        this(null);
    }


    /**
     * 指定された親パターンの下にNotを作る
     * 
     * @param parent
     *            親パターン
     */
    public NotOperator(PatternContainer parent) {
        super(parent);
    }


    @Override
    public String toString() {
        return " (" + getKind() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }


    @Override
    public boolean canHaveChild(PatternKind kind) {
        if (!PatternKind.TERM.equals(kind)) {
            return false;
        }

        if (!childCreated) {
            createChildren();
        }
        return (children.size() == 0);
    }


    @Override
    protected PatternContainer ownClone() {
        NotOperator not = new NotOperator(null);
        return not;
    }


    @Override
    public PatternKind getKind() {
        return PatternKind.NOT;
    }
}
