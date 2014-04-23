/**
 * @version $Id: Sequence.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:53:36
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;


/**
 * @author kousuke-morishima
 */
public class Sequence extends ScopePattern implements IModification {


    /**
     * ルートに連続パターンを作る
     */
    public Sequence() {
        this(null);
    }


    /**
     * 指定された親の下に連続パターンを作る
     * 
     * @param parent
     *            親パターン
     */
    public Sequence(PatternContainer parent) {
        super(parent);
    }


    @Override
    public boolean canHaveChild(PatternKind kind) {
        switch (kind) {
        case AND:
            return false;
        case ORDER:
            return false;
        case SEQUENCE:
            return false;
        case MODIFICATION:
            return false;
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
        String scope = (getScope() == null) ? "" : getScope().getName(); //$NON-NLS-1$
        return "検索範囲 ： " + scope + " (" + getKind() + ")"; //$NON-NLS-2$ //$NON-NLS-3$
    }


    @Override
    protected PatternContainer ownClone() {
        Sequence seq = new Sequence(null);
        seq.setScope(getScope());
        return seq;
    }


    @Override
    public PatternKind getKind() {
        return PatternKind.SEQUENCE;
    }
}
