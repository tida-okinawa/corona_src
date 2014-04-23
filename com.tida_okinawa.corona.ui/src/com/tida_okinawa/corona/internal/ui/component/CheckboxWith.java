/**
 * @version $Id: CheckboxWith.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/25 19:30:29
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.component;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * チェックボックスの値によってコントロールの有効/無効を切り替える
 * 
 * @author kousuke-morishima
 */
public abstract class CheckboxWith extends AbstractWidget {

    public CheckboxWith(Composite parent, String label, int withControlStyle) {
        root = CompositeUtil.defaultComposite(parent, 2);
        root.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        createControl(root, label, withControlStyle);

        setChecked(false);
    }

    Button check;
    protected Control withControl;


    private void createControl(Composite parent, String label, int withControlStyle) {
        check = CompositeUtil.createBtn(parent, SWT.CHECK, label, listener);
        withControl = createWithControl(parent, withControlStyle);
    }


    /**
     * @return コントロールが有効ならばtrue
     */
    public boolean isEnabled() {
        return check.isEnabled();
    }


    /**
     * このウィジェット全体の有効/無効を切り替える
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        check.setEnabled(enabled);
        if (withControl != null) {
            withControl.setEnabled(enabled);
        }
    }


    /**
     * @return チェックボックスがチェックされていればtrue
     */
    public boolean isChecked() {
        return check.getSelection();
    }


    /**
     * 入力欄の有効/無効を切り替える
     * 
     * @param checked
     */
    public void setChecked(boolean checked) {
        check.setSelection(checked);
        if (withControl != null) {
            withControl.setEnabled(checked);
        }
    }


    /**
     * チェックボックスの横につけるコントロールを作る。
     * Compositeじゃないほうが都合がいい。
     * 
     * @param parent
     * @param style
     * @return may be null
     */
    protected abstract Control createWithControl(Composite parent, int style);

    /* ****************************************
     * getter/setter
     */
    private Composite root;


    public Composite getRoot() {
        return root;
    }


    /**
     * @return rootCompositeのレイアウトデータ
     */
    public Object getLayoutData() {
        return root.getLayoutData();
    }


    /**
     * @param layoutData
     *            rootCompositeのレイアウトデータ
     */
    public void setLayoutData(Object layoutData) {
        root.setLayoutData(layoutData);
    }


    /**
     * @return withControlの値。コントロールが無効でも、値が入っていればその値を返す。may be null.
     */
    public abstract Object getValue();

    /* ****************************************
     * Listeners
     */
    ListenerList listeners = new ListenerList();


    public void addCheckStateChangedListener(SelectionListener listener) {
        listeners.add(listener);
    }


    public void removeCheckStateChangedListener(SelectionListener listener) {
        listeners.remove(listener);
    }

    private SelectionListener listener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            setChecked(((Button) e.widget).getSelection());
            for (Object l : listeners.getListeners()) {
                ((SelectionListener) l).widgetSelected(e);
            }
        }
    };


    /* ****************************************
     * other
     */
    @Override
    public void setFocus() {
        if (withControl != null) {
            withControl.setFocus();
        } else {
            check.setFocus();
        }
    }


    @Override
    public void dispose() {
        check.removeSelectionListener(listener);
    }


    @Override
    protected int handling() {
        return 0;
    };
}
