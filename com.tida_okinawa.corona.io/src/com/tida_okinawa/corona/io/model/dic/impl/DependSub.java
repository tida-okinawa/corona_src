/**
 * @version $Id: DependSub.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/26 18:00:39
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.impl;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.dic.IDepend;
import com.tida_okinawa.corona.io.model.dic.IDependSub;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.abstraction.DicItem;

/**
 * @author kousuke-morishima
 */
public class DependSub extends DicItem implements IDependSub {
    protected int level;
    protected ITerm term;
    protected IDepend parent;


    public DependSub(int id, ITerm term, IDepend parent) {
        super(id);
        this.term = term;
        this.parent = parent;
    }


    @Override
    public IDepend getParent() {
        return parent;
    }


    @Override
    public void setParent(IDepend parent) {
        this.parent = parent;
    }


    @Override
    public ITerm getTerm() {
        return term;
    }


    @Override
    public void setTerm(ITerm term) {
        if ((term == null) || this.term.equals(term)) {
            return;
        }

        this.term = term;
        setDirty(true);
    }


    @Override
    public int getLevel() {
        return level;
    }


    @Override
    public void setLevel(int level) {
        this.level = level;
        setDirty(true);
    }


    @Override
    public String toString() {
        return term.getValue();
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("id", "ID"), new TextPropertyDescriptor("name", "代表語"),
                new TextPropertyDescriptor("sub", "従属語"), };
        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals("id")) {
            return String.valueOf(getId());
        }
        if (id.equals("name")) {
            if (getParent() != null && getParent().getMain() != null) {
                return getParent().getMain().getValue();
            }
        }
        if (id.equals("sub")) {
            if (getTerm() != null) {
                return getTerm().getValue();
            }
        }
        return null;
    }


    @Override
    public int hashCode() {
        if (_id == UNSAVED_ID) {
            return 47;
        }
        return _id;
    }


    @Override
    public boolean equals(Object object) {
        // TODO: equals実装漏れ
        assert false;
        return super.equals(object);
    }
}
