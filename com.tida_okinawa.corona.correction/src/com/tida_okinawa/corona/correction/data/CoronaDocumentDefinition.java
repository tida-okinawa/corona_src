/**
 * @version $Id:
 *
 * 2012/07/30 10:51:11
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.data;

/**
 * ドキュメントの分割情報を定義するクラス
 * 
 * @author shingo-takahashi
 * 
 */
public class CoronaDocumentDefinition {
    /** 分割情報の適用範囲。指定された分割情報がどこで出てきても分割することを表す */
    public static final int WHOLE = 0;
    /** 分割情報の適用範囲。指定された分割情報が文頭に出てきたら分割することを表す */
    public static final int PHRASE = 1;

    /** 定義情報の種別。指定された文字のうち、いずれかが出てきたら分割することを表す */
    public static final int CHAR = 0;
    /** 定義情報の種別。指定された文字列が出てきたら分割することを表す */
    public static final int STRING = 1;

    /** 分割情報文字列の除去指定。分割情報が見つかった時、分割情報をテキストから除外しない */
    public static final int NO_TRIM = 0;
    /** 分割情報文字列の除去指定。分割情報が見つかった時、分割情報をテキストから除外する */
    public static final int TRIM = 1;

    /** {@link #WHOLE} に対応する文字列 */
    public static final String STR_WHOLE = Messages.CoronaDocumentDefinition_StringValueOf_Whole;
    /** {@link #PHRASE} に対応する文字列 */
    public static final String STR_PHRASE = Messages.CoronaDocumentDefinition_StringValueOf_Phrase;

    /** {@link #CHAR} に対応する文字列 */
    public static final String STR_CHAR = Messages.CoronaDocumentDefinition_StringValueOf_Char;
    /** {@link #STRING} に対応する文字列 */
    public static final String STR_STRING = Messages.CoronaDocumentDefinition_StringValueOf_String;

    /** {@link #TRIM} に対応する文字列 */
    public static final String STR_TRUE = Messages.CoronaDocumentDefinition_StringValueOf_True;
    /** {@link #NO_TRIM} に対応する文字列 */
    public static final String STR_FALSE = Messages.CoronaDocumentDefinition_StringValueOf_False;

    private String definition = ""; //定義 //$NON-NLS-1$
    private boolean enabled = true; //有効無効
    private int position = PHRASE; //位置
    private int type = STRING; //種別

    private int trim = 0; //トリム


    /**
     * @return 分割定義情報
     */
    public String getDefinition() {
        return definition;
    }


    /**
     * @param definition
     *            分割定義情報
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }


    /**
     * @return この分割定義情報が現在有効ならtrue
     */
    public boolean isEnabled() {
        return enabled;
    }


    /**
     * @param enabled
     *            この分割定義情報を有効にするならtrue
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    /**
     * @return 定義情報の適用範囲
     * @see #WHOLE
     * @see #PHRASE
     */
    public int getPosition() {
        return position;
    }


    /**
     * @param position
     *            定義情報の適用範囲
     * @see #WHOLE
     * @see #PHRASE
     */
    public void setPosition(int position) {
        this.position = position;
    }


    /**
     * @return 定義情報の適用範囲の文字列表現
     */
    public String getPositionStr() {
        if (position == WHOLE) {
            return STR_WHOLE;
        } else if (position == PHRASE) {
            return STR_PHRASE;
        }
        return ""; //$NON-NLS-1$
    }


    /**
     * @return 定義情報の種別
     * @see #CHAR
     * @see #STRING
     */
    public int getType() {
        return type;
    }


    /**
     * @param type
     *            定義情報の種別
     * @see #CHAR
     * @see #STRING
     */
    public void setType(int type) {
        this.type = type;
    }


    /**
     * @return 定義情報の種別の文字表現
     */
    public String getTypeStr() {
        if (type == CHAR) {
            return STR_CHAR;
        }
        if (type == STRING) {
            return STR_STRING;
        }
        return ""; //$NON-NLS-1$
    }


    /**
     * @return 分割情報が見つかった時、分割情報文字列を除去するなら {@link #TRIM}, しないなら {@link #NO_TRIM}
     */
    public int getTrim() {
        return trim;
    }


    /**
     * @param trim
     *            分割情報が見つかった時、分割情報文字列を除去するなら {@link #TRIM}, しないなら
     *            {@link #NO_TRIM}
     * @see #TRIM
     * @see #NO_TRIM
     */
    public void setTrim(int trim) {
        this.trim = trim;
    }


    /**
     * @return 除去指定の文字列表現
     */
    public String getTrimStr() {
        if (trim == TRIM) {
            return STR_TRUE;
        }
        if (trim == NO_TRIM) {
            return STR_FALSE;
        }
        return ""; //$NON-NLS-1$
    }

}
