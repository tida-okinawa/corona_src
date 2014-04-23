/**
 * @version $Id: ScopePattern.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/25 22:18:15
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;


/**
 * 
 * @author kyohei-miyazato
 */
public abstract class ScopePattern extends PatternContainer implements IScopePattern {
    /**
     * @param parent
     */
    public ScopePattern(PatternContainer parent) {
        super(parent);
    }

    private SearchScopeType scope = SearchScopeType.SEARCH_ALL;


    @Override
    public SearchScopeType getScope() {
        return scope;
    }


    @Override
    public void setScope(SearchScopeType scope) {
        scope = (scope == null) ? SearchScopeType.SEARCH_ALL : scope;
        if (this.scope.equals(scope)) {
            return;
        }
        this.scope = scope;
        propertyChanged();
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
            IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("kind", "パターン型"),
                    new TextPropertyDescriptor("scope", "検索範囲") };
            return descriptor;
        }


        @Override
        public Object getPropertyValue(Object id) {
            if (id.equals("scope")) {
                return getScope().getName();
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
}
