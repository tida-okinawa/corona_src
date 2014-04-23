/**
 * @version $Id: Term.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:53:50
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * @author kousuke-morishima
 */
public class Term extends Pattern implements IModification, IQuantifier, IHitMorphemeHolder {


    /**
     * ルートに単語パターンを作る
     */
    public Term() {
        this(null);
    }


    /**
     * 指定された親の下に単語パターンを作る
     * 
     * @param parent
     *            親パターン
     */
    public Term(PatternContainer parent) {
        super(parent);
        word = ""; //$NON-NLS-1$
        label = ""; //$NON-NLS-1$
    }

    private String word;
    private TermPart part = TermPart.NONE;
    private TermClass wordClass = TermClass.NONE;
    private String label;
    private QuantifierType quant = QuantifierType.QUANT_NONE;


    /**
     * @return 単語。非null
     */
    public String getWord() {
        return word;
    }


    public void setWord(String word) {
        word = (word == null) ? "" : word; //$NON-NLS-1$
        if (this.word.equals(word)) {
            return;
        }
        this.word = word;
        propertyChanged();
    }


    /**
     * 品詞
     * 
     * @return not null
     */
    public TermPart getPart() {
        return part;
    }


    public void setPart(TermPart part) {
        part = (part == null) ? TermPart.NONE : part;
        if (this.part.equals(part)) {
            return;
        }
        this.part = part;
        propertyChanged();
    }


    /**
     * 品詞詳細
     * 
     * @return not null
     */
    public TermClass getWordClass() {
        return wordClass;
    }


    public void setWordClass(TermClass wordClass) {
        wordClass = (wordClass == null) ? TermClass.NONE : wordClass;
        if (this.wordClass.equals(wordClass)) {
            return;
        }
        this.wordClass = wordClass;
        propertyChanged();
    }


    /**
     * @return ラベル（ラベル辞書に登録されている必要がある）。非null
     */
    public String getLabel() {
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


    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append(word).append(" : ");
        buf.append((part == null) ? "" : part.getName()).append(":"); //$NON-NLS-1$ //$NON-NLS-2$
        buf.append((wordClass == null) ? "" : wordClass.getName()).append(":"); //$NON-NLS-1$ //$NON-NLS-2$
        buf.append(label).append(":"); //$NON-NLS-1$
        buf.append((quant == null) ? "" : quant.getName()); //$NON-NLS-1$
        buf.append("(").append(getKind()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        return buf.toString();
    }


    @Override
    public Pattern clone() {
        Term term = new Term(null);
        term.setWord(getWord());
        term.setPart(getPart());
        term.setWordClass(getWordClass());
        term.setLabel(getLabel());
        term.setQuant(getQuant());
        return term;
    }


    @Override
    public QuantifierType getQuant() {
        return quant;
    }


    @Override
    public void setQuant(QuantifierType quant) {
        quant = (quant == null) ? QuantifierType.QUANT_NONE : quant;
        if (this.quant.equals(quant)) {
            return;
        }
        this.quant = quant;
        propertyChanged();
    }

    /* ****************************************
     * 構文解析処理で使う
     */
    private MorphemeElement hitElement;


    @Override
    public void setHitElement(MorphemeElement morphemeElement) {
        this.hitElement = morphemeElement;
    }


    @Override
    public MorphemeElement getHitElement() {
        return hitElement;
    }

    private MorphemeElement topElement;


    @Override
    public MorphemeElement getTopMorpheme() {
        return topElement;
    }


    @Override
    public void setTopMorpheme(MorphemeElement morphemeElement) {
        this.topElement = morphemeElement;
    }


    /* ****************************************
     * プロパティ
     */
    @Override
    public PatternKind getKind() {
        return PatternKind.TERM;
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
            IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { //
            new TextPropertyDescriptor("kind", "パターン型"), //
                    new TextPropertyDescriptor("word", "単語"), //
                    new TextPropertyDescriptor("part", "品詞"), //
                    new TextPropertyDescriptor("class", "品詞詳細"), //
                    new TextPropertyDescriptor("label", "ラベル"), //
                    new TextPropertyDescriptor("quant", "数量子"), //
            };
            return descriptor;
        }


        @Override
        public Object getPropertyValue(Object id) {
            if (id.equals("word")) {
                return getWord();
            }
            if (id.equals("part")) {
                return getPart().getName();
            }
            if (id.equals("class")) {
                return getWordClass().getName();
            }
            if (id.equals("label")) {
                return getLabel();
            }
            if (id.equals("quant")) {
                return getQuant().getName();
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
