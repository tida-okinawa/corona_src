/**
 * 
 * 2012/11/28 18:01:23
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import com.tida_okinawa.corona.correction.parsing.model.CompType;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateCompare extends Template {

    private String label;
    private CompType type;
    private int value;


    /**
     * 比較
     * 
     * @param parent
     *            親要素
     */
    public TemplateCompare(TemplateContainer parent) {
        super(parent);
        label = ""; //$NON-NLS-1$
    }


    /**
     * 数値ラベル取得
     * 
     * @return 数値ラベル
     */
    public String getLabel() {
        return label;
    }


    /**
     * 数値ラベル設定
     * 
     * @param label
     *            数値ラベル
     */
    public void setLabel(String label) {
        if (this.label.equals(label)) {
            return;
        }
        this.label = label;
        propertyChanged();
    }


    /**
     * 数値ラベルと値の比較方法の取得
     * 
     * @return 数値ラベルと値の比較方法
     */
    public CompType getType() {
        return type;
    }


    /**
     * 数値ラベルと値の比較方法の設定
     * 
     * @param type
     *            数値ラベルと値の比較方法
     */
    public void setType(CompType type) {
        if (this.type.equals(type)) {
            return;
        }
        this.type = type;
        propertyChanged();
    }


    /**
     * 数値ラベルと比較する値の取得
     * 
     * @return 数値ラベルと比較する値
     */
    public int getValue() {
        return value;
    }


    /**
     * 数値ラベルと比較する値の設定
     * 
     * @param value
     *            数値ラベルと比較する値
     */
    public void setValue(int value) {
        if (this.value == value) {
            return;
        }
        this.value = value;
        propertyChanged();
    }


    @Override
    public String toString() {
        if (type == null) {
            return Messages.TEMPLATE_COMPARE_STRING_EN;
        }
        return label + " " + type.getLabel() + " " + value + Messages.TEMPLATE_COMPARE_STRING_JP; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
