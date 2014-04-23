/**
 * @version $Id: FrequentRecord.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/26 10:35:11
 * @author kenta-uechi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.frequent;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.IUserDic;


/**
 * Frequentの処理結果
 * 
 * 品詞の情報 {@link Term} と頻出数をもつ
 * 
 * 辞書の登録情報はもたない（表示時に動的に生成する）
 * 
 * エディタで編集されることを想定している。
 */
public class FrequentRecord implements Comparable<FrequentRecord>, IPropertySource {
    /**
     * 品詞情報
     */
    final Term term;

    /**
     * 登場回数
     */
    int count = 0;

    final static String COUNT_DELIM = "#"; //$NON-NLS-1$

    /**
     * このレコードが持つ単語を新しく登録する辞書
     */
    IUserDic destDic;

    private boolean undefine;


    /**
     * @return この単語が未定義語ならtrue
     */
    public boolean isUndefine() {
        // TODO (undefine || "未定義語".equals(term.getHinshi()) がいいんじゃないかな
        return undefine;
    }


    /**
     * @param undefine
     *            trueならこの単語を未定義語としてマークする
     */
    public void setUndefine(boolean undefine) {
        this.undefine = undefine;
    }


    /**
     * @param term
     *            カウント対象の単語
     */
    public FrequentRecord(Term term) {
        this.term = term;
    }


    /**
     * @return 単語の原形
     */
    public String getGenkei() {
        return term.getGenkei();
    }


    /**
     * @param genkei
     *            単語に設定する原形
     */
    public void setGenkei(String genkei) {
        term.setGenkei(genkei);
    }


    /**
     * @return 単語の読み
     */
    public String getYomi() {
        return term.getYomi();
    }


    /**
     * @param yomi
     *            単語に設定するよみ
     */
    public void setYomi(String yomi) {
        term.setYomi(yomi);
    }


    /**
     * @return 単語の品詞
     */
    public String getHinshi() {
        return term.getHinshi();
    }


    /**
     * @param hinshi
     *            単語に設定する品詞
     */
    public void setHinshi(String hinshi) {
        term.setHinshi(hinshi);
    }


    /**
     * @return 単語の品詞細分類
     */
    public String getHinshiSaibunrui() {
        return term.getHinshiSaibunrui();
    }


    /**
     * @param saibunrui
     *            単語に設定する品詞細分類
     */
    public void setHinshiSaibunrui(String saibunrui) {
        term.setHinshiSaibunrui(saibunrui);
    }


    /**
     * @return 単語の活用詳細
     */
    public String getCform() {
        return term.getCform().getName();
    }


    /**
     * @return 単語の出現頻度
     */
    public int getCount() {
        return count;
    }


    /**
     * @return このレコードが持つ単語を新しく登録する辞書
     */
    public IUserDic getDestDictionary() {
        return destDic;
    }


    /**
     * @param destDic
     *            この単語を登録する辞書
     */
    public void setDestDictionary(IUserDic destDic) {
        this.destDic = destDic;
    }

    private String registeredDics = null;


    /**
     * @return この単語を登録している辞書群(カンマ区切りで複数の辞書名が返る)
     */
    public String getRegisteredDics() {
        return (registeredDics == null) ? "" : registeredDics; //$NON-NLS-1$
    }


    /**
     * 検索対象の辞書群から、この単語が登録されている辞書を探す
     * 
     * @param searchDics
     *            検索対象の辞書群
     * @throws SQLException
     *             接続が確立できなかった
     */
    public void createRegisteredDics(List<IUserDic> searchDics) throws SQLException {
        if (registeredDics == null) {
            registeredDics = ""; //$NON-NLS-1$
            try {
                if (!getHinshi().equals(Messages.FrequentRecord_UNDEFINED_WORD)) {
                    /* 代表語が登録されている辞書を取得 */
                    Collection<IUserDic> dicList = IoActivator.getService().searchParentDic(searchDics, getGenkei(), getYomi(), getHinshi(),
                            getHinshiSaibunrui(), getCform());
                    for (IUserDic dic : dicList) {
                        registeredDics += dic.getName() + ","; //$NON-NLS-1$
                    }
                }
            } catch (SQLException e) {
                throw e;
            }
        }
    }


    /**
     * DBのテキストから
     * 
     * @param text
     */
    public FrequentRecord(String text) {
        this.term = Term.createTerm(text);
        String count_str = text.substring(text.indexOf(COUNT_DELIM) + 1);
        this.count = Integer.parseInt(count_str);
    }


    /**
     * DBに登録する形式にする
     * 
     * 空白区切りで、次のように並んでいる<br />
     * <p>
     * 原型 読み 品詞 品詞詳細 活用関係 出現回数
     * </p>
     */
    @Override
    public String toString() {
        return term.toString() + " " + COUNT_DELIM + count; //$NON-NLS-1$
    }


    @Override
    public int compareTo(FrequentRecord o) {
        return term.compareTo(o.term);
    }


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] {
                new TextPropertyDescriptor("token", Messages.FrequentRecord_WORD), new TextPropertyDescriptor("reading", Messages.FrequentRecord_READING), //$NON-NLS-1$ //$NON-NLS-2$ 
                new TextPropertyDescriptor("part", Messages.FrequentRecord_PART), new TextPropertyDescriptor("class", Messages.FrequentRecord_CLASS), new TextPropertyDescriptor("form", Messages.FrequentRecord_FORM), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
                new TextPropertyDescriptor("count", Messages.FrequentRecord_COUNT), new TextPropertyDescriptor("dics", Messages.FrequentRecord_DICS), }; //$NON-NLS-1$ //$NON-NLS-2$ 
        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals("reading")) { //$NON-NLS-1$
            return getYomi();
        }
        if (id.equals("token")) { //$NON-NLS-1$
            return getGenkei();
        }
        if (id.equals("part")) { //$NON-NLS-1$
            return getHinshi();
        }
        if (id.equals("class")) { //$NON-NLS-1$
            return getHinshiSaibunrui();
        }
        if (id.equals("form")) { //$NON-NLS-1$
            return getCform();
        }
        if (id.equals("count")) { //$NON-NLS-1$
            return getCount();
        }
        if (id.equals("dics")) { //$NON-NLS-1$
            return getRegisteredDics();
        }
        return null;
    }


    @Override
    public Object getEditableValue() {
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
