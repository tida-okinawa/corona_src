/**
 * @version $Id: TemplateTerm.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 17:51:12
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import com.tida_okinawa.corona.correction.parsing.model.IQuantifier;
import com.tida_okinawa.corona.correction.parsing.model.QuantifierType;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateTerm extends VariableTemplate implements IQuantifier, ITemplateTermType {

    /**
     * 単語（Term）
     * 
     * @param parent
     *            親要素
     */
    public TemplateTerm(TemplateContainer parent) {
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


    /**
     * @param word
     *            単語（Word）
     */
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


    /**
     * @param part
     *            品詞
     */
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


    /**
     * @param wordClass
     *            品詞詳細
     */
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


    /**
     * @param label
     *            ラベル
     */
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
        buf.append(word).append(Messages.TEMPLATE_COLON1);
        buf.append((part == null) ? "" : part.getName()).append(Messages.TEMPLATE_COLON2); //$NON-NLS-1$
        buf.append((wordClass == null) ? "" : wordClass.getName()).append(Messages.TEMPLATE_COLON2); //$NON-NLS-1$
        buf.append(label).append(Messages.TEMPLATE_COLON2);
        buf.append((quant == null) ? "" : quant.getName()); //$NON-NLS-1$
        buf.append(Messages.TEMPLATE_TERM_STRING);
        return buf.toString();
    }


    /* ****************************************
     * 数量子
     */
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


    /** 不要だがインターフェースに含まれるので */
    @Override
    public MorphemeElement getHitElement() {
        return null;
    }


    /** 不要だがインターフェースに含まれるので */
    @Override
    public void setHitElement(MorphemeElement hitElement) {
    }


    /* ****************************************
     * 単語の種類（固定or可変）
     */

    /** 単語（Word）or ラベル(Label)を判定 */
    private String type = null;


    @Override
    public void setState(String type) {
        if (this.type != null) {
            if (this.type.equals(type)) {
                return;
            }
        } else {
            if (type == null) {
                return;
            }
        }
        this.type = type;
        propertyChanged();
    }


    @Override
    public String getState() {
        return type;
    }

}
