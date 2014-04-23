/**
 * @version $Id: Modification.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 18:01:47
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * 係り受けパターンを示すクラス
 * 
 * @author kousuke-morishima
 */
public class Modification extends PatternContainer implements IModification {

    public static final int TYPE_UNSET = -1;
    public static final int TYPE_DEPEND = 0;
    public static final int TYPE_PARALLEL = 1;


    /**
     * ルートに係り受けパターンを作る
     */
    public Modification() {
        this(null, false);
    }


    /**
     * @param parent
     *            親パターン
     * @param createDefaultChild
     *            インスタンス化したときに、子のSourceとDestinationを一緒に作るか
     */
    public Modification(PatternContainer parent, boolean createDefaultChild) {
        super(parent);
        if (createDefaultChild) {
            createChildren();
            source = new ModificationElement(this, PatternKind.MODIFICATION_SOURCE);
            children.add(source);
            destination = new ModificationElement(this, PatternKind.MODIFICATION_DESTINATION);
            children.add(destination);
        }
    }

    private int type = TYPE_UNSET;
    private ModificationElement source;
    private ModificationElement destination;


    /**
     * @return 係り受けタイプ
     */
    public int getType() {
        return type;
    }


    public void setType(int type) {
        if (this.type == type) {
            return;
        }
        this.type = type;
        propertyChanged();
    }


    /**
     * @return 係り受け元
     */
    public ModificationElement getSource() {
        return source;
    }


    /**
     * @return 係り受け先
     */
    public ModificationElement getDestination() {
        return destination;
    }


    @Override
    public boolean canHaveChild(PatternKind kind) {
        switch (kind) {
        case MODIFICATION_SOURCE:
            return source == null;
        case MODIFICATION_DESTINATION:
            return destination == null;
        default:
            return false;
        }
    }


    @Override
    public void addChild(Pattern child) {
        if (child instanceof ModificationElement) {
            switch (((ModificationElement) child).getKind()) {
            case MODIFICATION_SOURCE:
                source = (ModificationElement) child;
                break;
            case MODIFICATION_DESTINATION:
                destination = (ModificationElement) child;
                break;
            default:
                break;
            }

            super.addChild(child);
        }
    }


    @Override
    public void removeChild(Pattern child) {
        if (child instanceof ModificationElement) {
            switch (((ModificationElement) child).getKind()) {
            case MODIFICATION_SOURCE:
                if (child.equals(source)) {
                    source = null;
                    super.removeChild(child);
                }
                break;
            case MODIFICATION_DESTINATION:
                if (child.equals(destination)) {
                    destination = null;
                    super.removeChild(child);
                }
                break;
            default:
                break;
            }
        }
    }


    @Override
    public String toString() {
        String typeStr = ""; //$NON-NLS-1$
        if (type == TYPE_DEPEND) {
            typeStr = "依存";
        } else if (type == TYPE_PARALLEL) {
            typeStr = "並列";
        }
        return typeStr + " (" + getKind() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }


    @Override
    protected PatternContainer ownClone() {
        Modification modifi = new Modification(null, false);
        modifi.setType(getType());
        return modifi;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySource.class)) {
            return source1;
        }
        return null;
    }


    @Override
    public PatternKind getKind() {
        return PatternKind.MODIFICATION;
    }

    private IPropertySource source1 = new IPropertySource() {
        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("kind", "パターン型"),
                    new TextPropertyDescriptor("type", "係り受け種別"), new TextPropertyDescriptor("src", "係り元"), new TextPropertyDescriptor("dist", "係り先"), };
            return descriptor;
        }


        @Override
        public Object getPropertyValue(Object id) {
            if (id.equals("kind")) {
                return getKind();
            }
            if (id.equals("type")) {
                return getType();
            }
            if (id.equals("src")) {
                if (getSource() != null) {
                    return getSource().getAdapter(IPropertySource.class);
                }
                return ""; //$NON-NLS-1$
            }
            if (id.equals("dist")) {
                if (getDestination() != null) {
                    return getDestination().getAdapter(IPropertySource.class);
                }
                return ""; //$NON-NLS-1$
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
