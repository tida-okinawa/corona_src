/**
 * @version $Id: Term.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/02 01:03:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.frequent;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.TermCForm;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * 頻出用語検出用の品詞情報
 * 
 * @author imai
 * 
 */
public class Term implements ITerm, Comparable<Term> {
    int id;

    boolean isDirty = false;

    final static int GENKEI = 0;
    final static int YOMI = 1;
    final static int HINSHI = 2;
    final static int HINSHI_SAIBUN = 3;
    final static int CFORM = 4;

    String[] parts = new String[5];


    /**
     * ファイルの1行からRecordを作る
     * 
     * @param text
     *            形態素解析結果形式の文字列
     * @return スペースで分割した時、分割数が８より少ない場合、nullを返す
     */
    static Term createTerm(String text) {
        String[] parts = text.split(" ");
        if (parts.length >= 5) {
            return new Term(parts);
            // TODO: 代表表記の読みを取得
        }
        return null;
    }


    public static Term createTerm(MorphemeElement me) {
        String[] parts = { me.getGenkei(), me.getYomi(), me.getHinshi(), me.getHinshiSaibunrui(), me.getCform(), };
        return new Term(parts);
    }


    Term(String[] parts) {
        this.parts = parts;
    }


    public String getGenkei() {
        return parts[GENKEI];
    }


    public void setGenkei(String genkei) {
        parts[GENKEI] = genkei;
    }


    public String getYomi() {
        return parts[YOMI];
    }


    public void setYomi(String yomi) {
        parts[YOMI] = yomi;
    }


    public String getHinshi() {
        return parts[HINSHI];
    }


    public void setHinshi(String hinshi) {
        parts[HINSHI] = hinshi;
    }


    public String getHinshiSaibunrui() {
        return parts[HINSHI_SAIBUN];
    }


    public void setHinshiSaibunrui(String saibunrui) {
        parts[HINSHI_SAIBUN] = saibunrui;
    }


    @Override
    public TermCForm getCform() {
        String s = parts[CFORM];
        TermCForm c = TermCForm.valueOfName(s);
        return c;
    }


    @Override
    public int compareTo(Term o) {
        // note. Morpheme#getText() だと余分な情報もあるので必要な情報だけを比較
        int c = getGenkei().compareTo(o.getGenkei());
        if (c != 0)
            return c;

        c = getYomi().compareTo(o.getYomi());
        if (c != 0)
            return c;

        c = getHinshi().compareTo(o.getHinshi());
        if (c != 0)
            return c;

        c = getHinshiSaibunrui().compareTo(o.getHinshiSaibunrui());
        if (c != 0)
            return c;

        c = getCform().compareTo(o.getCform());

        return c;
    }


    @Override
    public String toString() {
        // note: new MorphemeElement(String) で解析できる形にする
        // 余分な情報を出さない
        return getGenkei() + " " + getYomi() + " " + getHinshi() + " " + getHinshiSaibunrui() + " " + getCform();
    }


    @Override
    public void setId(int id) {
        this.id = id;
        isDirty = true;
    }


    @Override
    public int getId() {
        return id;
    }


    @Override
    public String getValue() {
        return getGenkei();
    }


    @Override
    public void setValue(String value) {
        set(GENKEI, value);
    }


    @Override
    public TermPart getTermPart() {
        String s = getHinshi();
        return TermPart.valueOfName(s);
    }


    @Override
    public void setTermPart(TermPart termPart) {
        set(HINSHI, termPart.getName());
    }


    @Override
    public TermClass getTermClass() {
        String s = getHinshiSaibunrui();
        return TermClass.valueOfName(s);
    }


    @Override
    public void setTermClass(TermClass termClass) {
        set(HINSHI, termClass.getName());
    }


    @Override
    public String getReading() {
        return getYomi();
    }


    @Override
    public void setReading(String reading) {
        set(YOMI, reading);
    }


    @Override
    public void setCform(TermCForm cform) {
        set(CFORM, cform.getName());
    }


    @Override
    public boolean isDirty() {
        return isDirty;
    }


    @Override
    public String getJumanBase() {
        return getGenkei();
    }


    @Override
    public void setJumanBase(String jumanBase) {
        // TODO: 代表語にする
        set(GENKEI, jumanBase);
    }

    int dicId = -1; // -1: unset


    @Override
    public int getComprehensionDicId() {
        return dicId;
    }


    void set(int index, String value) {
        parts[index] = value;
        isDirty = true;
    }


    @Override
    public String getKeyword() {
        return "";
    }


    @Override
    public void setDirty(boolean dirty) {
        if (isDirty == dirty) {
            return;
        }
        isDirty = dirty;
    }


    @Override
    public boolean isInActive() {
        return false;
    }


    @Override
    public boolean isError() {
        // dummy implements
        return false;
    }


    @Override
    public Object clone() {
        String[] cloneParts = new String[parts.length];
        System.arraycopy(parts, 0, cloneParts, 0, parts.length);
        Term ret = new Term(cloneParts);
        return ret;
    }


    /*
     * プロパティビュー対応でICoronaDicにIPropertySourceをextendsしたので、
     * 空のメソッド群を追加
     */
    @Override
    public Object getEditableValue() {
        return null;
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return null;
    }


    @Override
    public Object getPropertyValue(Object id) {
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
}
