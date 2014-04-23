/**
 * @version $Id: OrOperator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:52:41
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * @author kousuke-morishima
 */
public class OrOperator extends ScopePattern implements IModification, IHitMorphemeHolder {

    private MorphemeElement me = null;


    /**
     * ルートにOrパターンを作る
     */
    public OrOperator() {
        this(null);
    }


    /**
     * 指定された親の下にOrパターンを作る
     * 
     * @param parent
     *            親パターン
     */
    public OrOperator(PatternContainer parent) {
        super(parent);
    }


    @Override
    public boolean canHaveChild(PatternKind kind) {
        if (isUnderSequence()) {
            return PatternKind.TERM.equals(kind);
        }

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


    /**
     * @return 自身がSequenceの配下ならtrue
     */
    private boolean isUnderSequence() {
        PatternContainer parent = getParent();
        while (parent != null) {
            if (PatternKind.SEQUENCE.equals(parent.getKind())) {
                return true;
            } else if (PatternKind.OR.equals(parent.getKind())) {
                parent = parent.getParent();
            } else {
                return false;
            }
        }
        return false;
    }


    @Override
    public void setTopMorpheme(MorphemeElement morphemeElement) {
        me = morphemeElement;
    }


    @Override
    public MorphemeElement getTopMorpheme() {
        return me;
    }


    @Override
    public String toString() {
        String search = (getScope() == null) ? "" : getScope().getName(); //$NON-NLS-1$
        StringBuilder string = new StringBuilder(12);
        return string.append("検索範囲 ： ").append(search).append(" (").append(getKind()).append(")").toString(); //$NON-NLS-2$ //$NON-NLS-3$
    }


    @Override
    protected PatternContainer ownClone() {
        OrOperator or = new OrOperator(null);
        or.setScope(getScope());
        return or;
    }


    @Override
    public PatternKind getKind() {
        return PatternKind.OR;
    }
}
