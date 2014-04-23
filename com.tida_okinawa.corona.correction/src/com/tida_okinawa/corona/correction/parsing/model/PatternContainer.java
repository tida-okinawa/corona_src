/**
 * @version $Id: PatternContainer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:51:03
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.correction.parsing.model.IPatternListener.PatternEvent;


/**
 * @author kousuke-morishima
 */
public abstract class PatternContainer extends Pattern {
    public static final int ALL = 0;
    public static final int SENTENCE = 1;
    public static final int CHUNK = 2;

    protected boolean childCreated = false;


    /**
     * {@link #children}を空で作成する
     */
    protected void createChildren() {
        childCreated = true;
        children = new ArrayList<Pattern>();
    }


    public PatternContainer(PatternContainer parent) {
        super(parent);
    }

    protected List<Pattern> children;


    /**
     * @return never not null
     */
    public List<Pattern> getChildren() {
        if (!childCreated) {
            createChildren();
        }
        return children;
    }


    /**
     * 子を追加する
     * 
     * @param child
     */
    public void addChild(Pattern child) {
        if (!childCreated) {
            createChildren();
        }
        children.add(child);
        childAdded(this, child, PatternEvent.NO_POS);
    }


    public void addChild(int index, Pattern child) {
        /* validate index range */
        if ((index < 0) || (index > children.size())) {
            return;
        }
        if (!childCreated) {
            createChildren();
        }
        children.add(index, child);
        childAdded(this, child, index);
    }


    void childAdded(PatternContainer parent, Pattern child, int position) {
        if (getParent() != null) {
            getParent().childAdded(parent, child, position);
        }
    }


    /**
     * 子を削除する
     * 
     * @param child
     */
    public void removeChild(Pattern child) {
        if (!childCreated) {
            createChildren();
        }
        children.remove(child);
        childRemoved(this, child);
    }


    void childRemoved(PatternContainer parent, Pattern child) {
        if (getParent() != null) {
            getParent().childRemoved(parent, child);
        }
    }


    /**
     * 指定されたパターン種別のパターンを子にすることができるかどうか。<br />
     * 保持数制限なども考慮して判定する
     * 
     * @param kind
     *            検査対象のパターン種別
     * @return このパターン種別のパターンを作れるならtrue
     */
    public boolean canHaveChild(PatternKind kind) {
        return true;
    }


    @Override
    public boolean hasChildren() {
        if (!childCreated) {
            return true;
        }

        return children.size() != 0;
    }


    @Override
    public Pattern clone() {
        PatternContainer clone = ownClone();
        clone.setIgnoreChange(true);
        for (Pattern child : getChildren()) {
            Pattern cloneChild = child.clone();
            clone.addChild(cloneChild);
            cloneChild.setParent(clone);
        }
        clone.setIgnoreChange(false);
        return clone;
    }


    /**
     * @return 親なし状態の自身のクローンを作る
     */
    protected abstract PatternContainer ownClone();
}
