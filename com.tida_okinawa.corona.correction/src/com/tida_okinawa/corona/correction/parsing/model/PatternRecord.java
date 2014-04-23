/**
 * @version $Id: PatternRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:49:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.correction.parsing.model.IPatternListener.PatternEvent;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.IPattern;

/**
 * @author kousuke-morishima
 */
public class PatternRecord extends PatternContainer {

    private boolean executed = true;
    private Pattern pattern = null;
    private IPattern iPattern;


    public PatternRecord() {
        this(null);
    }


    public PatternRecord(IPattern iPattern) {
        super(null);
        init(iPattern);
    }


    protected void init(IPattern iPattern) {
        /* 実際にPatternを作成するのは、子を要求されたとき */
        if (iPattern == null) {
            iPattern = IoActivator.getDicFactory().createPattern("", "", -99, false); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.iPattern = iPattern;
    }


    @Override
    protected void createChildren() {
        super.createChildren();

        /* 読み出したデータがあれば変換 */
        pattern = DicPatternUtil.convertFrom(iPattern.getText());
        if (pattern != null) {
            /* トップ要素には親が設定されていないので、セットする */
            pattern.setParent(this);
            /* 追加イベントが起きてしまうので、直接addしている */
            children.add(pattern);
        }
    }


    /* ****************************************
     * プロパティフィールド
     */
    /**
     * @return パターンのID。0ならDB未登録
     */
    public int getId() {
        return iPattern.getId();
    }


    /**
     * @return パターンの名称。not null
     */
    public String getLabel() {
        return iPattern.getLabel();
    }


    public void setLabel(String label) {
        if (iPattern.getLabel().equals(label)) {
            return;
        }
        iPattern.setLabel((label == null) ? "" : label); //$NON-NLS-1$
        propertyChanged();
    }


    /**
     * @return パターンの種別
     */
    public int getType() {
        return iPattern.getPatternType();
    }


    public void setType(int type) {
        if (iPattern.getPatternType() == type) {
            return;
        }
        iPattern.setPatternType(type);
        propertyChanged();
    }


    /**
     * @return パターン解析できたかできなかったかを表すフラグ
     */
    public boolean isExecuted() {
        // 対応するものがIPatternにない
        return executed;
    }


    /**
     * パターン解析できたかできなかったかを表すフラグ。構文解析をしてみて、解析できない形式のパターンを含んでいた場合に呼び出される
     * 
     * @param executed
     *            trueなら解析できた。falseなら解析できなかった。
     */
    public void setExecuted(boolean executed) {
        this.executed = executed;
    }


    /**
     * @return 使い回しするパーツかどうか
     */
    public boolean isPart() {
        return iPattern.isParts();
    }


    public void setPart(boolean part) {
        if (iPattern.isParts() == part) {
            return;
        }
        iPattern.setParts(part);
        propertyChanged();
    }


    /* ****************************************
     * パターン
     */
    /**
     * @return パターン(UI表示モデル)
     */
    public Pattern getPattern() {
        return pattern;
    }


    public void setPattern(Pattern pattern) {
        if (this.pattern == null) {
            if (pattern == null) {
                return;
            }
        } else {
            if (this.pattern.equals(pattern)) {
                return;
            }
        }
        this.pattern = pattern;
        propertyChanged();
    }


    /**
     * @return パターン
     */
    public IPattern getIPattern() {
        return iPattern;
    }


    /* ****************************************
     * 子供の管理
     */
    @Override
    public boolean canHaveChild(PatternKind kind) {
        switch (kind) {
        case MODIFICATION_SOURCE:
            return false;
        case MODIFICATION_DESTINATION:
            return false;
        default:
            break;
        }
        if (!childCreated) {
            /* 作られていないのであれば、作ったあとで判定 */
            createChildren();
        }
        return pattern == null;
    }


    @Override
    public boolean hasChildren() {
        if (!childCreated) {
            return super.hasChildren();
        }
        return pattern != null;
    }


    /* ****************************************
     * 表示更新イベント管理
     */
    @Override
    public void addChild(Pattern child) {
        if (pattern == null) {
            pattern = child;
            super.addChild(child);
        }
    }


    @Override
    public void addChild(int index, Pattern child) {
        addChild(child);
    }


    @Override
    public void removeChild(Pattern child) {
        if (child == null) {
            return;
        }

        if ((pattern != null) && pattern.equals(child)) {
            pattern = null;
            super.removeChild(child);
        }
    }


    @Override
    void childAdded(PatternContainer parent, Pattern child, int position) {
        iPattern.setText(DicPatternUtil.convertFrom(pattern));
        patternAdded(parent, child, position);
    }


    @Override
    void childRemoved(PatternContainer parent, Pattern child) {
        iPattern.setText(DicPatternUtil.convertFrom(pattern));
        patternRemoved(parent, child);
    }


    @Override
    protected void propertyChanged(Pattern own) {
        if (!isIgnoreChange()) {
            if (!childCreated) {
                createChildren();
            }
            iPattern.setText(DicPatternUtil.convertFrom(pattern));
            patternChanged(own);
        }
    }

    /* ****************************************
     * 外部へのリスナー
     */
    private ListenerList listeners = new ListenerList();


    public void addPatternListener(IPatternListener listener) {
        listeners.add(listener);
    }


    public void removePatternListener(IPatternListener listener) {
        listeners.remove(listener);
    }


    protected void patternAdded(PatternContainer parent, Pattern child, int position) {
        Object[] listeners = this.listeners.getListeners();
        PatternEvent event = new PatternEvent(parent, child, position);
        for (Object l : listeners) {
            ((IPatternListener) l).patternAdded(event);
        }
    }


    protected void patternRemoved(PatternContainer parent, Pattern child) {
        Object[] listeners = this.listeners.getListeners();
        PatternEvent event = new PatternEvent(parent, child);
        for (Object l : listeners) {
            ((IPatternListener) l).patternRemoved(event);
        }
    }


    protected void patternChanged(Pattern target) {
        PatternEvent event = new PatternEvent(null, target);
        for (Object l : listeners.getListeners()) {
            ((IPatternListener) l).patternChanged(event);
        }
    }


    /* ****************************************
     * その他
     */
    @Override
    public String toString() {
        return getLabel();
    }


    @Override
    protected PatternContainer ownClone() {
        PatternRecord rec = new PatternRecord(null);
        rec.setLabel(getLabel());
        rec.setPart(isPart());
        rec.setExecuted(isExecuted());
        rec.setType(getType());
        return rec;
    }


    @Override
    public PatternKind getKind() {
        return PatternKind.ROOT;
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
                    new TextPropertyDescriptor("label", "パターン名") };
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

}
