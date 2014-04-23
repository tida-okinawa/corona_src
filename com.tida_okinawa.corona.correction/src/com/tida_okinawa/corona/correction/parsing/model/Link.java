/**
 * @version $Id: Link.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:59:20
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;


/**
 * @author kousuke-morishima
 */
public class Link extends Pattern implements IModification, IPatternListener {
    public static final int NO_ID = -1;


    /**
     * ルートに参照パターンを作る
     */
    public Link() {
        this(null);
    }


    /**
     * 指定された親パターンの下に参照を作る
     * 
     * @param parent
     *            親パターン
     */
    public Link(PatternContainer parent) {
        super(parent);
        id = NO_ID;
        label = ""; //$NON-NLS-1$
    }

    private int id;
    private String label;
    private IPattern linkPattern;


    /**
     * @return 参照するパターンのID(IPatternのID)
     */
    public int getId() {
        return id;
    }


    void setId(int id) {
        if (this.id == id) {
            return;
        }
        this.id = id;
        propertyChanged();
    }


    /**
     * @return 参照するパターンの名称
     */
    public String getLabel() {
        IPattern pattern = getLinkPattern();
        if (pattern != null) {
            return pattern.getLabel();
        } else {
            if (getId() != -1) {
                IDicItem item = IoActivator.getDicUtil().getItem(getId(), DicType.PATTERN);
                if (item != null) {
                    return ((IPattern) item).getLabel();
                }
            }
        }
        return label;
    }


    public void setLabel(String label) {
        label = (label == null) ? "" : label; //$NON-NLS-1$
        if (this.label.equals(label)) {
            return;
        }
        this.label = label;
        propertyChanged();
    }


    /**
     * 保存するとき、参照しているパターンがまだ部品属性かどうか確認するために使用する
     * 
     * @param iPattern
     */
    public void setLinkPattern(IPattern iPattern) {
        this.linkPattern = iPattern;
        if (iPattern != null) {
            setId(iPattern.getId());
            setLabel(iPattern.getLabel());
        } else {
            setId(-1);
            setLabel(""); //$NON-NLS-1$
        }
    }


    /**
     * @return 参照しているパターンがないとき、null
     */
    public IPattern getLinkPattern() {
        if ((id != NO_ID) && (linkPattern == null)) {
            List<ICoronaDic> dics = IoActivator.getService().getDictionarys(IPatternDic.class);
            for (ICoronaDic dic : dics) {
                IDicItem item = dic.getItem(id);
                if (item != null) {
                    linkPattern = (IPattern) item;
                    break;
                }
            }
        }
        return linkPattern;
    }


    /* ****************************************
     * implements IPatternListener
     */
    @Override
    public void patternAdded(PatternEvent event) {
    }


    @Override
    public void patternRemoved(PatternEvent event) {
        if (event.child instanceof PatternRecord) {
            if ((linkPattern != null) && (linkPattern.equals(((PatternRecord) event.child).getIPattern()))) {
                setLinkPattern(null);
            }
        }
    }


    @Override
    public void patternChanged(PatternEvent event) {
    }


    @Override
    public String toString() {
        return getLabel() + " (" + getKind() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }


    @Override
    public Pattern clone() {
        Link link = new Link(null);
        link.setLinkPattern(getLinkPattern());
        link.setId(getId());

        return link;
    }


    @Override
    public PatternKind getKind() {
        return PatternKind.LINK;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySource.class)) {
            return source;
        }
        return null;
    }

    private IPropertySource source = new IPropertySource() {
        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("id", "ID"), new TextPropertyDescriptor("kind", "パターン型"),
                    new TextPropertyDescriptor("label", "パターン名"), };
            return descriptor;
        }


        @Override
        public Object getPropertyValue(Object id) {
            if (id.equals("id")) {
                return String.valueOf(getId());
            }
            if (id.equals("label")) {
                return getLabel();
            }
            if (id.equals("kind")) {
                return getKind();
            }
            return null;
        }


        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }


        @Override
        public void resetPropertyValue(Object id) {
        }


        @Override
        public void setPropertyValue(Object id, Object value) {
        }


        @Override
        public Object getEditableValue() {
            return null;
        }

    };


    public String getDicName() {
        return IoActivator.getService().getDictionary(linkPattern.getComprehensionDicId()).getName();
    }
}
