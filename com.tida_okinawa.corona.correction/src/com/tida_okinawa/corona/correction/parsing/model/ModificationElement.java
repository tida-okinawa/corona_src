/**
 * @version $Id: ModificationElement.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 18:03:09
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * 係り元、係り先パターンを表すクラス
 * 
 * @author kousuke-morishima
 */
public class ModificationElement extends PatternContainer implements IHitMorphemeHolder {
    private final PatternKind kind;


    /**
     * @param parent
     *            親
     * @param kind
     *            {@link PatternKind#MODIFICATION_SOURCE} or
     *            {@link PatternKind#MODIFICATION_DESTINATION}
     */
    public ModificationElement(PatternContainer parent, PatternKind kind) {
        super(parent);
        this.kind = kind;
    }


    @Override
    public boolean canHaveChild(PatternKind kind) {
        /* implements IModification はtrue */
        switch (kind) {
        case AND:
            return true;
        case LINK:
            return true;
        case TERM:
            return true;
        case OR:
            return true;
        case ORDER:
            return true;
        case SEQUENCE:
            return true;
        case MODIFICATION:
            return true;
        default:
            return false;
        }
    }


    @Override
    public void addChild(Pattern child) {
        if (child instanceof IModification) {
            super.addChild(child);
        }
    }


    @Override
    public void removeChild(Pattern child) {
        if (child instanceof IModification) {
            super.removeChild(child);
        }
    }


    @Override
    public String toString() {
        return " (" + kind + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }


    @Override
    public PatternKind getKind() {
        return kind;
    }


    @Override
    protected PatternContainer ownClone() {
        ModificationElement element = new ModificationElement(null, kind);
        return element;
    }

    private MorphemeElement me = null;


    @Override
    public void setTopMorpheme(MorphemeElement morphemeElement) {
        me = morphemeElement;
    }


    @Override
    public MorphemeElement getTopMorpheme() {
        return me;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySource.class)) {
            return source1;
        }
        return null;
    }

    private IPropertySource source1 = new IPropertySource() {
        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            int length = getChildren().size();
            String kind = null;
            if (length != 1) {
                kind = ModificationElement.this.getKind().toString();
            } else if (length == 1) {
                kind = getChildren().get(0).getKind().toString();
            }
            IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("type", kind), };
            return descriptor;
        }


        @Override
        public Object getPropertyValue(Object id) {
            if (id.equals("kind")) {
                return getKind();
            }
            if (id.equals("type")) {
                if (getChildren().size() == 0) {
                    return ""; //$NON-NLS-1$
                }
                if (getChildren().size() == 1) {
                    return getChildren().get(0).getAdapter(IPropertySource.class);
                }
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

}
